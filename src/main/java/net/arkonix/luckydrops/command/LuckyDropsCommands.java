package net.arkonix.luckydrops.command;

import com.mojang.brigadier.CommandDispatcher;
import net.arkonix.luckydrops.config.LuckyDropsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

import static net.minecraft.commands.Commands.literal;

public final class LuckyDropsCommands {
    private LuckyDropsCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("luckydrops")
                        .requires(source -> source.hasPermission(2))

                        .then(literal("reload")
                                .executes(context -> {
                                    LuckyDropsConfig.reload();
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("LuckyDrops config reloaded.").withStyle(ChatFormatting.GREEN),
                                            true
                                    );
                                    return 1;
                                })
                        )

                        .then(literal("toggle")
                                .executes(context -> {
                                    boolean newState = !LuckyDropsConfig.enabled;
                                    LuckyDropsConfig.setEnabled(newState);

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("LuckyDrops is now " + (newState ? "enabled" : "disabled") + ".")
                                                    .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED),
                                            true
                                    );
                                    return 1;
                                })
                        )

                        .then(literal("on")
                                .executes(context -> {
                                    LuckyDropsConfig.setEnabled(true);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("LuckyDrops enabled.").withStyle(ChatFormatting.GREEN),
                                            true
                                    );
                                    return 1;
                                })
                        )

                        .then(literal("off")
                                .executes(context -> {
                                    LuckyDropsConfig.setEnabled(false);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("LuckyDrops disabled.").withStyle(ChatFormatting.RED),
                                            true
                                    );
                                    return 1;
                                })
                        )

                        .then(literal("debug")
                                .executes(context -> {
                                    boolean newState = !LuckyDropsConfig.debug;
                                    LuckyDropsConfig.setDebug(newState);

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("LuckyDrops debug is now " + (newState ? "enabled" : "disabled") + ".")
                                                    .withStyle(newState ? ChatFormatting.YELLOW : ChatFormatting.GRAY),
                                            true
                                    );
                                    return 1;
                                })
                        )

                        .then(literal("status")
                                .executes(context -> {
                                    boolean luckyLoaded = ModList.get().isLoaded("lucky");

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("LuckyDrops status:\n")
                                                    .append(Component.literal("Enabled: " + LuckyDropsConfig.enabled + "\n"))
                                                    .append(Component.literal("Trigger chance: " + LuckyDropsConfig.triggerChance + "\n"))
                                                    .append(Component.literal("Lucky Block mod loaded: " + luckyLoaded + "\n"))
                                                    .append(Component.literal("Prefer Lucky Block mod: " + LuckyDropsConfig.preferLuckyBlockMod + "\n"))
                                                    .append(Component.literal("Fallback rewards: " + LuckyDropsConfig.enableFallbackRewards + "\n"))
                                                    .append(Component.literal("Debug: " + LuckyDropsConfig.debug)),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );
    }
}