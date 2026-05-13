package jp.ac.u_tokyo.sdm.sdm_mod.poster;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PosterRegistry {
    private static final Map<String, PosterDefinition> REGISTRY = new LinkedHashMap<>();

    private PosterRegistry() {}

    public static void register(String id, Identifier texture, float width, float height) {
        REGISTRY.put(id, new PosterDefinition(id, texture, width, height));
    }

    @Nullable
    public static PosterDefinition get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<String> getIds() {
        return REGISTRY.keySet();
    }
}
