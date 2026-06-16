# LuckyDropsForge

Forge mod for Minecraft 1.20.2 / Forge 48.1.0 / Java 17.

Every survival block break can trigger a Lucky Block-style reward. If Player_in_distress' Lucky Block mod is installed, LuckyDrops tries to call its internal drop handler through reflection. If that fails or the mod is missing, LuckyDrops uses its own built-in reward engine.

## Build

Open this folder in IntelliJ IDEA Community as a Gradle project, then run:

```bash
gradle build
```

The mod jar will be created in `build/libs`.

## Optional dependency

Put `lucky-block-forge-1.20.2-13.0.jar` in your Minecraft mods folder together with LuckyDrops. It is optional and is not bundled in this project.
