package net.arkonix.luckydrops.lucky;

import net.arkonix.luckydrops.LuckyDropsForge;
import net.arkonix.luckydrops.config.LuckyDropsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class LuckyBlockCompat {
    private static boolean luckyLoaded;
    private static boolean reflectionReady;

    private static Block luckyBlock;
    private static Constructor<?> vec3Constructor;
    private static Method onLuckyBlockBreakMethod;

    private LuckyBlockCompat() {
    }

    public static void init(boolean detected) {
        luckyLoaded = detected;
        reflectionReady = false;

        if (!detected) {
            LuckyDropsForge.LOGGER.warn("LuckyDrops: Lucky Block mod is NOT installed. Fallback engine will be used.");
            return;
        }

        try {
            luckyBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("lucky", "lucky_block"));

            Class<?> vec3Class = Class.forName("mod.lucky.common.Vec3");
            Class<?> luckyBlockEntityDataClass = Class.forName("mod.lucky.java.game.LuckyBlockEntityData");
            Class<?> luckyBlockUtilsClass = Class.forName("mod.lucky.java.game.LuckyBlockUtilsKt");

            vec3Constructor = vec3Class.getConstructor(Number.class, Number.class, Number.class);

            onLuckyBlockBreakMethod = luckyBlockUtilsClass.getMethod(
                    "onLuckyBlockBreak",
                    Object.class,
                    Object.class,
                    vec3Class,
                    Object.class,
                    luckyBlockEntityDataClass,
                    boolean.class
            );

            reflectionReady = luckyBlock != null && vec3Constructor != null && onLuckyBlockBreakMethod != null;

            LuckyDropsForge.LOGGER.info(
                    "LuckyDrops: connected directly to Lucky Block reward engine. luckyBlock={}, ready={}",
                    luckyBlock,
                    reflectionReady
            );
        } catch (Throwable throwable) {
            LuckyDropsForge.LOGGER.error("LuckyDrops: failed to connect to Lucky Block reward engine.", throwable);
            reflectionReady = false;
        }
    }

    public static boolean isLuckyLoaded() {
        return luckyLoaded;
    }

    public static boolean isReflectionReady() {
        return reflectionReady;
    }

    public static boolean trigger(ServerLevel level, ServerPlayer player, BlockPos pos) {
        if (!luckyLoaded || !reflectionReady || luckyBlock == null) {
            return false;
        }

        try {
            Object luckyVec3 = vec3Constructor.newInstance(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );

            onLuckyBlockBreakMethod.invoke(
                    null,
                    luckyBlock,
                    level,
                    luckyVec3,
                    player,
                    null,
                    false
            );

            if (LuckyDropsConfig.debug) {
                LuckyDropsForge.LOGGER.info("LuckyDrops: used REAL Lucky Block DropEvaluator at {}", pos);
            }

            return true;
        } catch (Throwable throwable) {
            LuckyDropsForge.LOGGER.error("LuckyDrops: real Lucky Block reward engine failed at {}", pos, throwable);
            return false;
        }
    }
}