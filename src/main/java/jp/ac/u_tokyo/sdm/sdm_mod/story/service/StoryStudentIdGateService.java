package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.ModItems;
import jp.ac.u_tokyo.sdm.sdm_mod.ModSounds;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StoryStudentIdGateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoryStudentIdGateService.class);
    private static final long NOTIFICATION_COOLDOWN_TICKS = 20L;
    private static final long ACCEPT_SOUND_COOLDOWN_TICKS = 20L;
    private static final long GATE_OPEN_DURATION_TICKS = 30L;
    private static final int BLOCK_UPDATE_FLAGS = 3;
    private static final List<StudentIdGate> STUDENT_ID_GATES = List.of(
        StudentIdGate.create("gate-1", -167, -166),
        StudentIdGate.create("gate-2", -164, -163),
        StudentIdGate.create("gate-3", -161, -160),
        StudentIdGate.create("gate-4", -158, -157),
        StudentIdGate.create("gate-5", -155, -154)
    );
    private static final Vec3d FALLBACK_SAFE_POS = new Vec3d(-159.5, 29.0, -633.0);
    private static final Map<String, Boolean> INITIAL_GATE_CLOSED_STATES = new HashMap<>();
    private static final Map<UUID, SafePosition> LAST_SAFE_POSITIONS = new HashMap<>();
    private static final Map<UUID, Long> LAST_NOTIFICATION_TICKS = new HashMap<>();
    private static final Map<PlayerGateKey, Long> LAST_ACCEPT_SOUND_TICKS = new HashMap<>();
    private static final Map<String, OpenGateState> OPEN_GATES = new HashMap<>();

    private StoryStudentIdGateService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(StoryStudentIdGateService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            restoreAllOpenGates();
            INITIAL_GATE_CLOSED_STATES.clear();
            LAST_SAFE_POSITIONS.clear();
            LAST_NOTIFICATION_TICKS.clear();
            LAST_ACCEPT_SOUND_TICKS.clear();
            OPEN_GATES.clear();
        });
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive()) {
            restoreAllOpenGates();
            INITIAL_GATE_CLOSED_STATES.clear();
            LAST_SAFE_POSITIONS.clear();
            LAST_NOTIFICATION_TICKS.clear();
            LAST_ACCEPT_SOUND_TICKS.clear();
            OPEN_GATES.clear();
            return;
        }

        initializeGateClosedStatesIfNeeded(server.getOverworld());
        restoreExpiredGates();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handlePlayer(player);
        }
    }

    private static void handlePlayer(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        BlockPos currentBlockPos = player.getBlockPos();
        StudentIdGate gate = findGateAt(currentBlockPos);
        if (gate == null) {
            LAST_SAFE_POSITIONS.put(playerId, SafePosition.fromPlayer(player));
            return;
        }

        boolean hasStudentId = player.getMainHandStack().isOf(ModItems.STUDENT_ID);
        ServerWorld world = (ServerWorld) player.getWorld();
        boolean gateClosed = gate.isUpperRegionFilledWithStainedGlassPanes(world);
        if (gateClosed) {
            if (hasStudentId) {
                openGateIfNeeded(player, world, gate);
                return;
            }

            notifyMissingStudentId(player, gate, false);
            return;
        }

        if (hasStudentId) {
            if (isInitiallyOpen(gate) && !OPEN_GATES.containsKey(gate.id())) {
                playGateAcceptedSoundIfCooledDown(player, gate);
            }
            return;
        }

        SafePosition safePosition = LAST_SAFE_POSITIONS.getOrDefault(
            playerId,
            new SafePosition((ServerWorld) player.getWorld(), FALLBACK_SAFE_POS)
        );
        player.teleport(
            safePosition.world(),
            safePosition.pos().x,
            safePosition.pos().y,
            safePosition.pos().z,
            Set.<PositionFlag>of(),
            player.getYaw(),
            player.getPitch(),
            false
        );
        notifyMissingStudentId(player, gate, true);
    }

    private static StudentIdGate findGateAt(BlockPos pos) {
        for (StudentIdGate gate : STUDENT_ID_GATES) {
            if (gate.triggerRegion().contains(pos)) {
                return gate;
            }
        }

        return null;
    }

    private static void openGateIfNeeded(ServerPlayerEntity player, ServerWorld world, StudentIdGate gate) {
        if (OPEN_GATES.containsKey(gate.id())) {
            return;
        }

        Map<BlockPos, BlockState> originalStates = new HashMap<>();
        for (BlockPos pos : gate.gateBlocksToOpenPositions()) {
            originalStates.put(pos, world.getBlockState(pos));
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), BLOCK_UPDATE_FLAGS);
        }

        OPEN_GATES.put(gate.id(), new OpenGateState(world, originalStates, world.getTime() + GATE_OPEN_DURATION_TICKS));
        playGateAcceptedSoundIfCooledDown(player, gate);
    }

    private static void initializeGateClosedStatesIfNeeded(ServerWorld world) {
        if (INITIAL_GATE_CLOSED_STATES.size() == STUDENT_ID_GATES.size()) {
            return;
        }

        INITIAL_GATE_CLOSED_STATES.clear();
        for (StudentIdGate gate : STUDENT_ID_GATES) {
            INITIAL_GATE_CLOSED_STATES.put(gate.id(), gate.isUpperRegionFilledWithStainedGlassPanes(world));
        }
    }

    private static boolean isInitiallyOpen(StudentIdGate gate) {
        return !INITIAL_GATE_CLOSED_STATES.getOrDefault(gate.id(), true);
    }

    private static void restoreExpiredGates() {
        OPEN_GATES.entrySet().removeIf(entry -> {
            OpenGateState state = entry.getValue();
            if (state.world().getTime() < state.restoreTick()) {
                return false;
            }

            state.restore();
            return true;
        });
    }

    private static void restoreAllOpenGates() {
        for (OpenGateState state : OPEN_GATES.values()) {
            state.restore();
        }
    }

    private static void notifyMissingStudentId(ServerPlayerEntity player, StudentIdGate gate, boolean repelled) {
        long currentTick = player.getWorld().getTime();
        Long lastNotificationTick = LAST_NOTIFICATION_TICKS.get(player.getUuid());
        if (lastNotificationTick != null && currentTick - lastNotificationTick < NOTIFICATION_COOLDOWN_TICKS) {
            return;
        }

        LAST_NOTIFICATION_TICKS.put(player.getUuid(), currentTick);
        ((ServerWorld) player.getWorld()).playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            ModSounds.STUDENT_ID_GATE_REJECT,
            SoundCategory.BLOCKS,
            1.0F,
            1.0F
        );
        player.sendMessage(Text.literal("学生証を右手に持ってください。"), false);
        LOGGER.warn(
            "Rejected player {} at {} without student ID in main hand. repelled={}, playerPos={}.",
            player.getName().getString(),
            gate.id(),
            repelled,
            player.getBlockPos()
        );
    }

    private static void playGateAcceptedSoundIfCooledDown(ServerPlayerEntity player, StudentIdGate gate) {
        PlayerGateKey key = new PlayerGateKey(player.getUuid(), gate.id());
        long currentTick = player.getWorld().getTime();
        Long lastSoundTick = LAST_ACCEPT_SOUND_TICKS.get(key);
        if (lastSoundTick != null && currentTick - lastSoundTick < ACCEPT_SOUND_COOLDOWN_TICKS) {
            return;
        }

        LAST_ACCEPT_SOUND_TICKS.put(key, currentTick);
        playGateAcceptedSound((ServerWorld) player.getWorld(), gate);
    }

    private static void playGateAcceptedSound(ServerWorld world, StudentIdGate gate) {
        Vec3d soundPos = gate.triggerRegion().center();
        world.playSound(
            null,
            soundPos.x,
            soundPos.y,
            soundPos.z,
            ModSounds.STUDENT_ID_GATE_ACCEPT,
            SoundCategory.BLOCKS,
            1.0F,
            1.0F
        );
    }

    private record GateRegion(BlockPos cornerA, BlockPos cornerB) {
        private boolean contains(BlockPos pos) {
            return pos.getX() >= Math.min(cornerA.getX(), cornerB.getX())
                && pos.getX() <= Math.max(cornerA.getX(), cornerB.getX())
                && pos.getY() >= Math.min(cornerA.getY(), cornerB.getY())
                && pos.getY() <= Math.max(cornerA.getY(), cornerB.getY())
                && pos.getZ() >= Math.min(cornerA.getZ(), cornerB.getZ())
                && pos.getZ() <= Math.max(cornerA.getZ(), cornerB.getZ());
        }

        private boolean isFilledWithStainedGlassPanes(ServerWorld world) {
            for (int x = Math.min(cornerA.getX(), cornerB.getX()); x <= Math.max(cornerA.getX(), cornerB.getX()); x++) {
                for (int y = Math.min(cornerA.getY(), cornerB.getY()); y <= Math.max(cornerA.getY(), cornerB.getY()); y++) {
                    for (int z = Math.min(cornerA.getZ(), cornerB.getZ()); z <= Math.max(cornerA.getZ(), cornerB.getZ()); z++) {
                        BlockState state = world.getBlockState(new BlockPos(x, y, z));
                        if (!state.isIn(ConventionalBlockTags.GLASS_PANES)
                            || state.isIn(ConventionalBlockTags.GLASS_PANES_COLORLESS)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        private Vec3d center() {
            double centerX = (Math.min(cornerA.getX(), cornerB.getX()) + Math.max(cornerA.getX(), cornerB.getX()) + 1) / 2.0D;
            double centerY = (Math.min(cornerA.getY(), cornerB.getY()) + Math.max(cornerA.getY(), cornerB.getY()) + 1) / 2.0D;
            double centerZ = (Math.min(cornerA.getZ(), cornerB.getZ()) + Math.max(cornerA.getZ(), cornerB.getZ()) + 1) / 2.0D;
            return new Vec3d(centerX, centerY, centerZ);
        }
    }

    private record SafePosition(ServerWorld world, Vec3d pos) {
        private static SafePosition fromPlayer(ServerPlayerEntity player) {
            return new SafePosition((ServerWorld) player.getWorld(), player.getPos());
        }
    }

    private record StudentIdGate(
        String id,
        GateRegion upperGateRegion,
        GateRegion triggerRegion,
        List<BlockPos> gateBlocksToOpenPositions
    ) {
        private static StudentIdGate create(String id, int minX, int maxX) {
            return new StudentIdGate(
                id,
                new GateRegion(new BlockPos(minX, 31, -633), new BlockPos(maxX, 31, -633)),
                new GateRegion(new BlockPos(minX, 30, -634), new BlockPos(maxX, 32, -632)),
                List.of(new BlockPos(minX, 31, -633), new BlockPos(maxX, 31, -633))
            );
        }

        private boolean isUpperRegionFilledWithStainedGlassPanes(ServerWorld world) {
            return upperGateRegion.isFilledWithStainedGlassPanes(world);
        }
    }

    private record OpenGateState(ServerWorld world, Map<BlockPos, BlockState> originalStates, long restoreTick) {
        private void restore() {
            for (Map.Entry<BlockPos, BlockState> entry : originalStates.entrySet()) {
                world.setBlockState(entry.getKey(), entry.getValue(), BLOCK_UPDATE_FLAGS);
            }
        }
    }

    private record PlayerGateKey(UUID playerId, String gateId) {
    }
}
