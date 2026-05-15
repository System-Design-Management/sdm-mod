package jp.ac.u_tokyo.sdm.sdm_mod.client.video;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.StoryVideoStartPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;

public final class OpVideoScreen extends Screen {
    private static final Identifier VIDEO_TEXTURE_ID = Identifier.of("sdm_mod", "op_video_frame");
    private static final Text START_TEXT = Text.literal("GAME START");
    private static final Text SKIP_TEXT = Text.literal("スキップ");

    // VLCJ
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;

    // 映像フレーム管理（VLCJ スレッド → レンダースレッド）
    // ダブルバッファ: vlcjBuf に VLCJ が書き込み、renderBuf からレンダースレッドが変換する。
    // スワップ時のみ bufSwapLock を取得し、変換処理中はロック不要。
    private ByteBuffer vlcjBuf;
    private ByteBuffer renderBuf;
    private final Object bufSwapLock = new Object();
    private volatile boolean frameReady;
    private int videoWidth;
    private int videoHeight;
    private volatile boolean needsTextureInit;
    private volatile boolean videoFinished;

    // Minecraft テクスチャ
    private NativeImageBackedTexture videoTexture;
    private NativeImage videoImage;
    // NativeImage の生ポインタ（リフレクションで取得してキャッシュ）
    private long videoImagePtr;

    private boolean playing;
    private volatile boolean playbackStarted;
    private boolean cleanedUp;

    public OpVideoScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        if (!playing) {
            int btnW = 100;
            int btnH = 40;
            addDrawableChild(
                ButtonWidget.builder(START_TEXT, btn -> startVideo())
                    .dimensions(width / 2 - btnW / 2, height / 2 - btnH / 2, btnW, btnH)
                    .build()
            );
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        if (playing) {
            VideoSoundSilencer.silenceStoryNoise();
            handleTextureInit();
            uploadFrameIfReady();
            if (videoTexture != null) {
                drawVideoFullscreen(context);
            }
            super.render(context, mouseX, mouseY, delta);
            if (videoFinished) {
                finishAndStartStory();
            }
        } else {
            super.render(context, mouseX, mouseY, delta);
        }
    }

    private void handleTextureInit() {
        if (!needsTextureInit || videoWidth == 0 || videoHeight == 0) {
            return;
        }
        needsTextureInit = false;

        // 同じ解像度なら既存テクスチャを再利用する。
        // シーク後に getBufferFormat() が再呼び出しされた場合でも、テクスチャを
        // 破棄して空（黒）テクスチャを作り直さないことで黒フラッシュを防ぐ。
        if (videoTexture != null && videoImage != null
                && videoImage.getWidth() == videoWidth && videoImage.getHeight() == videoHeight) {
            return;
        }

        if (videoTexture != null) {
            client.getTextureManager().destroyTexture(VIDEO_TEXTURE_ID);
            videoTexture = null;
            videoImage = null;
            videoImagePtr = 0;
        }

        videoImage = new NativeImage(NativeImage.Format.RGBA, videoWidth, videoHeight, false);
        videoImagePtr = resolveNativeImagePointer(videoImage);
        videoTexture = new NativeImageBackedTexture(() -> "sdm_mod_op_video", videoImage);
        client.getTextureManager().registerTexture(VIDEO_TEXTURE_ID, videoTexture);
    }

    private void uploadFrameIfReady() {
        if (!frameReady || videoTexture == null) {
            return;
        }
        // バッファをスワップ: VLCJ は次のフレームを新しい vlcjBuf に書き込める
        synchronized (bufSwapLock) {
            if (!frameReady) return;
            ByteBuffer tmp = vlcjBuf;
            vlcjBuf = renderBuf;
            renderBuf = tmp;
            frameReady = false;
        }

        // ロック不要: renderBuf はこのスレッドが占有、vlcjBuf は VLCJ が占有
        long src = MemoryUtil.memAddress(renderBuf.position(0));
        int n = videoWidth * videoHeight;
        if (videoImagePtr != 0) {
            // 高速パス: NativeImage の生ポインタへ直接書き込み（setColorArgb のオーバーヘッドを排除）
            // BGRA (LE int: B|G<<8|R<<16|A<<24) → RGBA (LE int: R|G<<8|B<<16|A<<24)
            // G と A はそのまま、B と R を入れ替える
            for (int i = 0; i < n; i++) {
                long off = i * 4L;
                int bgra = MemoryUtil.memGetInt(src + off);
                int rgba = (bgra & 0xFF00FF00) | ((bgra & 0xFF) << 16) | ((bgra >>> 16) & 0xFF);
                MemoryUtil.memPutInt(videoImagePtr + off, rgba);
            }
        } else {
            // フォールバック: BGRA LE int はそのまま ARGB として setColorArgb に渡せる
            for (int i = 0; i < n; i++) {
                videoImage.setColorArgb(i % videoWidth, i / videoWidth, MemoryUtil.memGetInt(src + i * 4L));
            }
        }
        videoTexture.upload();
    }

    private void drawVideoFullscreen(DrawContext context) {
        RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
        // textureWidth/Height に screenWidth/Height を渡すことで UV が 0→1 になり映像全体を描画
        context.drawTexture(pipeline, VIDEO_TEXTURE_ID, 0, 0, 0.0f, 0.0f, width, height, width, height);
    }

    private void startVideo() {
        String videoPath = DevVideoConfig.getOpVideoPath();
        if (videoPath.isEmpty()) {
            // 動画パスが未設定の場合はスキップしてゲームを開始
            finishAndStartStory();
            return;
        }

        clearChildren();
        playing = true;
        client.getMusicTracker().stop();
        VideoSoundSilencer.silenceStoryNoise();

        int btnW = 60;
        int btnH = 20;
        int margin = 1;
        addDrawableChild(
            ButtonWidget.builder(SKIP_TEXT, btn -> finishAndStartStory())
                .dimensions(margin, margin, btnW, btnH)
                .build()
        );

        new NativeDiscovery().discover();
        // --start-paused: VLC がデコーダを初期化してもクロックを進めず t=0 で待機する。
        // これにより、A/V 同期によるフレームドロップを防ぎ、最初のフレームから再生できる。
        mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show", "--quiet", "--start-paused");
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        CallbackVideoSurface surface = mediaPlayerFactory.videoSurfaces().newVideoSurface(
            new BufferFormatCallback() {
                @Override
                public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                    videoWidth = sourceWidth;
                    videoHeight = sourceHeight;
                    int size = sourceWidth * sourceHeight * 4;
                    synchronized (bufSwapLock) {
                        if (vlcjBuf != null) MemoryUtil.memFree(vlcjBuf);
                        if (renderBuf != null) MemoryUtil.memFree(renderBuf);
                        vlcjBuf = MemoryUtil.memAlloc(size);
                        renderBuf = MemoryUtil.memAlloc(size);
                    }
                    needsTextureInit = true;
                    return new RV32BufferFormat(sourceWidth, sourceHeight);
                }

                @Override
                public void allocatedBuffers(ByteBuffer[] buffers) {
                    // VLCJ が内部バッファを確保した後に呼ばれる（特に処理不要）
                }
            },
            new RenderCallback() {
                @Override
                public void display(MediaPlayer mp, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
                    // VLCJ スレッドで呼ばれる。vlcjBuf にコピーしてフラグを立てる。
                    ByteBuffer src = nativeBuffers[0];
                    src.rewind();
                    synchronized (bufSwapLock) {
                        vlcjBuf.rewind();
                        vlcjBuf.put(src);
                        frameReady = true;
                    }
                    // --start-paused の場合、初回 display() は t=0 のフレームで呼ばれる。
                    // ここで play() を呼ぶことで t=0 から再生を開始する。
                    // paused() イベントより display() が先に呼ばれた場合はこちらが有効になる。
                    if (!playbackStarted) {
                        playbackStarted = true;
                        new Thread(() -> {
                            EmbeddedMediaPlayer p = mediaPlayer;
                            if (p != null) p.controls().play();
                        }).start();
                    }
                }
            },
            true // ロック有効（VLCJ がフレームを上書きする前に display() が完了するのを保証）
        );

        mediaPlayer.videoSurface().set(surface);
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            // --start-paused でデコーダ初期化後に paused() が呼ばれる。
            // display() より先に呼ばれた場合のフォールバックとして play() を起動する。
            @Override
            public void paused(MediaPlayer mp) {
                if (!playbackStarted) {
                    playbackStarted = true;
                    new Thread(mp.controls()::play).start();
                }
            }

            @Override
            public void finished(MediaPlayer mp) {
                videoFinished = true;
            }
        });

        mediaPlayer.media().play(videoPath);
    }

    private void finishAndStartStory() {
        cleanup();
        ClientPlayNetworking.send(StoryVideoStartPayload.INSTANCE);
        MinecraftClient.getInstance().setScreen(null);
    }

    private void cleanup() {
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;

        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaPlayerFactory != null) {
            mediaPlayerFactory.release();
            mediaPlayerFactory = null;
        }
        if (videoTexture != null && client != null) {
            client.getTextureManager().destroyTexture(VIDEO_TEXTURE_ID);
            videoTexture = null;
            videoImage = null;
            videoImagePtr = 0;
        }
        synchronized (bufSwapLock) {
            if (vlcjBuf != null) {
                MemoryUtil.memFree(vlcjBuf);
                vlcjBuf = null;
            }
            if (renderBuf != null) {
                MemoryUtil.memFree(renderBuf);
                renderBuf = null;
            }
        }
    }

    private static long resolveNativeImagePointer(NativeImage image) {
        try {
            java.lang.reflect.Field f = NativeImage.class.getDeclaredField("pointer");
            f.setAccessible(true);
            return f.getLong(image);
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    @Override
    public void removed() {
        cleanup();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        // ESC で閉じさせない（shouldCloseOnEsc で制御）
    }
}
