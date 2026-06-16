package net.arkonix.luckydrops;

import com.mojang.logging.LogUtils;
import net.arkonix.luckydrops.command.LuckyDropsCommands;
import net.arkonix.luckydrops.config.LuckyDropsConfig;
import net.arkonix.luckydrops.event.BlockBreakHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

@Mod(LuckyDropsForge.MOD_ID)
public class LuckyDropsForge {
    public static final String MOD_ID = "luckydrops";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LuckyDropsForge() {
        LuckyDropsConfig.load(FMLPaths.CONFIGDIR.get().resolve("luckydrops-common.toml"));

        boolean luckyLoaded = ModList.get().isLoaded("lucky");

        if (luckyLoaded) {
            LOGGER.info("LuckyDropsForge loaded. Lucky Block mod detected.");
        } else {
            LOGGER.warn("LuckyDropsForge loaded, but Lucky Block mod was NOT detected. Blocks will not trigger Lucky Block rewards unless fallback is enabled.");
        }

        MinecraftForge.EVENT_BUS.register(new BlockBreakHandler());
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        LuckyDropsCommands.register(event.getDispatcher());
    }
}