package net.arkonix.luckydrops.lucky;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public final class FallbackRewardEngine {
    private static final Random RANDOM = new Random();

    private FallbackRewardEngine() {
    }

    public static void trigger(ServerLevel level, ServerPlayer player, BlockPos pos) {
        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D,
                pos.getY() + 0.8D,
                pos.getZ() + 0.5D,
                24,
                0.35D,
                0.35D,
                0.35D,
                0.02D
        );

        level.playSound(
                null,
                pos,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        int roll = RANDOM.nextInt(18);

        switch (roll) {
            case 0 -> give(player, new ItemStack(Items.DIAMOND, RANDOM.nextInt(3) + 1));
            case 1 -> give(player, new ItemStack(Items.EMERALD, RANDOM.nextInt(5) + 2));
            case 2 -> give(player, new ItemStack(Items.GOLDEN_APPLE, 1));
            case 3 -> give(player, new ItemStack(Items.EXPERIENCE_BOTTLE, RANDOM.nextInt(12) + 4));
            case 4 -> player.giveExperiencePoints(RANDOM.nextInt(80) + 30);
            case 5 -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 1));
            case 6 -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 15, 0));
            case 7 -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 1));
            case 8 -> spawnMob(level, EntityType.ZOMBIE, pos);
            case 9 -> spawnMob(level, EntityType.SKELETON, pos);
            case 10 -> spawnMob(level, EntityType.CREEPER, pos);
            case 11 -> spawnTnt(level, pos);
            case 12 -> spawnLightning(level, pos);
            case 13 -> level.explode(
                    null,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    2.0F,
                    Level.ExplosionInteraction.NONE
            );
            case 14 -> give(player, new ItemStack(Items.IRON_INGOT, RANDOM.nextInt(10) + 4));
            case 15 -> give(player, new ItemStack(Items.LAPIS_LAZULI, RANDOM.nextInt(16) + 8));
            case 16 -> give(player, new ItemStack(Items.NETHERITE_SCRAP, 1));
            case 17 -> player.teleportTo(
                    level,
                    player.getX(),
                    player.getY() + 8.0D,
                    player.getZ(),
                    player.getYRot(),
                    player.getXRot()
            );
            default -> {
            }
        }
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        boolean added = player.getInventory().add(stack);
        if (!added) {
            player.drop(stack, false);
        }
    }

    private static void spawnMob(ServerLevel level, EntityType<?> type, BlockPos pos) {
        type.spawn(level, pos.above(), MobSpawnType.TRIGGERED);
    }

    private static void spawnTnt(ServerLevel level, BlockPos pos) {
        PrimedTnt tnt = new PrimedTnt(
                level,
                pos.getX() + 0.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D,
                null
        );
        tnt.setFuse(60);
        level.addFreshEntity(tnt);
    }

    private static void spawnLightning(ServerLevel level, BlockPos pos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos.above()));
            level.addFreshEntity(bolt);
        }
    }
}