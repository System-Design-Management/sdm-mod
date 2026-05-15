package jp.ac.u_tokyo.sdm.sdm_mod.poster;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import jp.ac.u_tokyo.sdm.sdm_mod.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PosterCommandInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PosterCommandInitializer.class);

    private PosterCommandInitializer() {}

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                literal("sdm_poster")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(literal("give")
                        .then(argument("poster_id", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                PosterRegistry.getIds().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(ctx -> executeGive(ctx, ctx.getSource().getPlayerOrThrow()))
                            .then(argument("player", EntityArgumentType.player())
                                .executes(ctx -> executeGive(ctx,
                                    EntityArgumentType.getPlayer(ctx, "player")))
                            )
                        )
                    )
            )
        );
    }

    private static int executeGive(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        try {
            String posterId = StringArgumentType.getString(context, "poster_id");
            PosterDefinition def = PosterRegistry.get(posterId);
            if (def == null) {
                LOGGER.warn("Unknown poster ID requested: {}", posterId);
                return 0;
            }

            ItemStack stack = new ItemStack(ModItems.POSTER_STICK);
            NbtCompound nbt = new NbtCompound();
            nbt.putString("poster_id", posterId);
            NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt);

            player.giveItemStack(stack);
            return 1;
        } catch (Exception e) {
            LOGGER.error("Failed to give poster item.", e);
            return 0;
        }
    }
}
