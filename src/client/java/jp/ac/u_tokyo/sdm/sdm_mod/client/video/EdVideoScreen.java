package jp.ac.u_tokyo.sdm.sdm_mod.client.video;

import com.mojang.blaze3d.pipeline.RenderPipeline;
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

public final class EdVideoScreen extends Screen {
    private static final Identifier VIDEO_TEXTURE_ID = Identifier.of("sdm_mod", "ed_video_frame");
    private static final Text SKIP_TEXT = Text.literal("スキップ");

    // VLCJ
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;

    // 映像フレーム管理（ダブルバッファ）
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
    private long videoImagePtr;

    private boolean cleanedUp;

    public EdVideoScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        int btnW = 60;
        int btnH = 20;
        int margin = 1;
        addDrawableChild(
            ButtonWidget.builder(SKIP_TEXT, btn -> closeScreen())
                .dimensions(margin, margin, btnW, btnH)
                .build()
        );
        startVideo();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        handleTextureInit();
        uploadFrameIfReady();
        if (videoTexture != null) {
            drawVideoFullscreen(context);
        }
        super.render(context, mouseX, mouseY, delta);
        if (videoFinished) {
            closeScreen();
        }
    }

    private void handleTextureInit() {
        if (!needsTextureInit || videoWidth == 0 || videoHeight == 0) {
            return;
        }
        needsTextureInit = false;

        if (videoTexture != null) {
            client.getTextureManager().destroyTexture(VIDEO_TEXTURE_ID);
            videoTexture = null;
            videoImage = null;
            videoImagePtr = 0;
        }

        videoImage = new NativeImage(NativeImage.Format.RGBA, videoWidth, videoHeight, false);
        videoImagePtr = resolveNativeImagePointer(videoImage);
        videoTexture = new NativeImageBackedTexture(() -> "sdm_mod_ed_video", videoImage);
        client.getTextureManager().registerTexture(VIDEO_TEXTURE_ID, videoTexture);
    }

    private void uploadFrameIfReady() {
        if (!frameReady || videoTexture == null) {
            return;
        }
        synchronized (bufSwapLock) {
            if (!frameReady) return;
            ByteBuffer tmp = vlcjBuf;
            vlcjBuf = renderBuf;
            renderBuf = tmp;
            frameReady = false;
        }

        long src = MemoryUtil.memAddress(renderBuf.position(0));
        int n = videoWidth * videoHeight;
        if (videoImagePtr != 0) {
            for (int i = 0; i < n; i++) {
                long off = i * 4L;
                int bgra = MemoryUtil.memGetInt(src + off);
                int rgba = (bgra & 0xFF00FF00) | ((bgra & 0xFF) << 16) | ((bgra >>> 16) & 0xFF);
                MemoryUtil.memPutInt(videoImagePtr + off, rgba);
            }
        } else {
            for (int i = 0; i < n; i++) {
                videoImage.setColorArgb(i % videoWidth, i / videoWidth, MemoryUtil.memGetInt(src + i * 4L));
            }
        }
        videoTexture.upload();
    }

    private void drawVideoFullscreen(DrawContext context) {
        RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
        context.drawTexture(pipeline, VIDEO_TEXTURE_ID, 0, 0, 0.0f, 0.0f, width, height, width, height);
    }

    private void startVideo() {
        String videoPath = DevVideoConfig.getEdVideoPath();
        if (videoPath.isEmpty()) {
            closeScreen();
            return;
        }

        client.getMusicTracker().stop();

        new NativeDiscovery().discover();
        mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show", "--quiet");
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
                }
            },
            new RenderCallback() {
                @Override
                public void display(MediaPlayer mp, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
                    ByteBuffer src = nativeBuffers[0];
                    src.rewind();
                    synchronized (bufSwapLock) {
                        vlcjBuf.rewind();
                        vlcjBuf.put(src);
                        frameReady = true;
                    }
                }
            },
            true
        );

        mediaPlayer.videoSurface().set(surface);
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mp) {
                videoFinished = true;
            }
        });

        mediaPlayer.media().play(videoPath);
    }

    private void closeScreen() {
        cleanup();
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
