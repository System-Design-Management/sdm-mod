package jp.ac.u_tokyo.sdm.sdm_mod.warp;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;

public final class WarpWorldState {
    private static final List<WarpDestination> DESTINATIONS = List.of(
        new WarpDestination("総合図書館", "warp.sdm_mod.destination.library", new BlockPos(-160, 25, -590)),
        new WarpDestination("赤門前", "warp.sdm_mod.destination.akamonmae", new BlockPos(-235, 25, -828)),
        new WarpDestination("安田講堂", "warp.sdm_mod.destination.yasuda", new BlockPos(-100, 24, -450))
    );

    private WarpWorldState() {
    }

    public static List<WarpDestination> getDestinations() {
        return DESTINATIONS;
    }

    public static Optional<WarpDestination> getDestinationByIndex(int index) {
        if (index < 0 || index >= DESTINATIONS.size()) {
            return Optional.empty();
        }

        return Optional.of(DESTINATIONS.get(index));
    }
}
