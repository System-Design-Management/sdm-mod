package jp.ac.u_tokyo.sdm.sdm_mod.client.video;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class DevVideoConfig {
    private static final String OP_VIDEO_PATH;
    private static final String ED_VIDEO_PATH;
    private static final String BAD_ED_VIDEO_PATH;

    static {
        Properties props = new Properties();
        // runClient の作業ディレクトリは run/ なので、親ディレクトリも試みる
        Path path = Paths.get("local.dev.properties");
        if (!Files.exists(path)) {
            path = Paths.get("../local.dev.properties");
        }
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            } catch (IOException ignored) {
            }
        }
        OP_VIDEO_PATH = props.getProperty("opVideoPath", "").trim();
        ED_VIDEO_PATH = props.getProperty("edVideoPath", "").trim();
        BAD_ED_VIDEO_PATH = props.getProperty("bad_edVideoPath", "").trim();
    }

    private DevVideoConfig() {
    }

    public static String getOpVideoPath() {
        return OP_VIDEO_PATH;
    }

    public static String getEdVideoPath() {
        return ED_VIDEO_PATH;
    }

    public static String getBadEdVideoPath() {
        return BAD_ED_VIDEO_PATH;
    }
}
