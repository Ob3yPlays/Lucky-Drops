package net.arkonix.luckydrops.event;

import net.arkonix.luckydrops.LuckyDropsForge;
import net.arkonix.luckydrops.config.LuckyDropsConfig;
import net.arkonix.luckydrops.lucky.FallbackRewardEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class BlockBreakHandler {
    private static final Set<BlockPos> INTERNAL_LUCKY_BREAKS = new HashSet<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos().immutable();

        if (INTERNAL_LUCKY_BREAKS.contains(pos)) {
            return;
        }

        if (!LuckyDropsConfig.enabled) return;

        boolean creative = serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
        if (creative && !LuckyDropsConfig.triggerInCreative) return;

        BlockState originalState = event.getState();
        if (originalState.isAir()) return;

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(originalState.getBlock());
        if (blockId != null && LuckyDropsConfig.blacklist.contains(blockId)) return;

        if (level.random.nextDouble() > LuckyDropsConfig.triggerChance) return;

        event.setCanceled(true);
        event.setExpToDrop(0);

        level.levelEvent(2001, pos, Block.getId(originalState));

        boolean luckyLoaded = ModList.get().isLoaded("lucky");

        if (luckyLoaded && LuckyDropsConfig.preferLuckyBlockMod) {
            Block luckyBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("lucky", "lucky_block"));

            if (luckyBlock != null && luckyBlock != Blocks.AIR) {
                level.setBlock(pos, luckyBlock.defaultBlockState(), Block.UPDATE_ALL);

                serverPlayer.getServer().execute(() -> {
                    INTERNAL_LUCKY_BREAKS.add(pos);

                    try {
                        serverPlayer.gameMode.destroyBlock(pos);

                        if (LuckyDropsConfig.debug) {
                            LuckyDropsForge.LOGGER.info(
                                    "LuckyDrops used real lucky:lucky_block break at {} for {}",
                                    pos,
                                    serverPlayer.getGameProfile().getName()
                            );
                        }
                    } catch (Exception exception) {
                        LuckyDropsForge.LOGGER.error("LuckyDrops failed to break placed lucky block at {}", pos, exception);
                    } finally {
                        INTERNAL_LUCKY_BREAKS.remove(pos);
                    }
                });

                return;
            }

            LuckyDropsForge.LOGGER.error("LuckyDrops could not find block lucky:lucky_block.");
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        if (LuckyDropsConfig.enableFallbackRewards) {
            FallbackRewardEngine.trigger(level, serverPlayer, pos);
        } else if (LuckyDropsConfig.debug) {
            LuckyDropsForge.LOGGER.warn("LuckyDrops fallback is disabled, so no reward was triggered at {}", pos);
        }
    }
}