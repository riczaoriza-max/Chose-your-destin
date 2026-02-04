package com.riczan.choosedestin.neoforge;

import com.google.gson.Gson;
import com.riczan.choosedestin.ChoiceConfig;
import com.riczan.choosedestin.ChoiceConfigLoader;
import com.riczan.choosedestin.ChoiceRuntime;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLServerStartedEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.eventbus.api.SubscribeEvent;

@Mod("choose_your_destin")
public final class ChooseYourDestinNeoForge {
    private static ChoiceRuntime runtime;
    private static NeoForgeGameAdapter adapter;

    public ChooseYourDestinNeoForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        NeoForgeNetworking.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        adapter = new NeoForgeGameAdapter(server);
        ChoiceConfig config = loadConfig(FMLPaths.CONFIGDIR.get());
        runtime = ChoiceRuntime.fromConfig(adapter, config);
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        if (runtime != null) {
            runtime.start();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && runtime != null) {
            runtime.tick();
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (runtime == null || !(event.getEntity() instanceof ServerPlayer player) || adapter == null) {
            return;
        }
        if (event.getSource().getMsgId().equals("fall")) {
            double multiplier = adapter.getMultiplier(player, com.riczan.choosedestin.ChoiceEffect.FALL_DAMAGE_MULTIPLIER_2X, 1.0);
            if (multiplier != 1.0) {
                event.setAmount((float) (event.getAmount() * multiplier));
            }
        }
    }

    @SubscribeEvent
    public void onExperienceDrop(LivingExperienceDropEvent event) {
        if (runtime == null || !(event.getAttackingPlayer() instanceof ServerPlayer player) || adapter == null) {
            return;
        }
        double multiplier = adapter.getMultiplier(player, com.riczan.choosedestin.ChoiceEffect.XP_MULTIPLIER_0_8X, 1.0);
        if (multiplier != 1.0) {
            event.setDroppedExperience((int) Math.max(0, Math.round(event.getDroppedExperience() * multiplier)));
        }
    }

    @SubscribeEvent
    public void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (runtime == null || !(event.getEntity() instanceof ServerPlayer player) || adapter == null) {
            return;
        }
        double multiplier = adapter.getMultiplier(player, com.riczan.choosedestin.ChoiceEffect.RESOURCE_DROP_MULTIPLIER_0_7X, 1.0);
        if (multiplier < 1.0) {
            event.setCanHarvest(event.canHarvest() && Math.random() < multiplier);
        }
    }

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (runtime == null || !(event.getHarvester() instanceof ServerPlayer player) || adapter == null) {
            return;
        }
        double multiplier = adapter.getMultiplier(player, com.riczan.choosedestin.ChoiceEffect.RESOURCE_DROP_MULTIPLIER_0_7X, 1.0);
        if (multiplier < 1.0) {
            event.getDrops().forEach(stack -> stack.setCount((int) Math.max(1, Math.floor(stack.getCount() * multiplier))));
        }
    }

    static void selectOption(ServerPlayer player, int index) {
        if (runtime != null) {
            runtime.select(new NeoForgeGamePlayer(player), index);
        }
    }

    private ChoiceConfig loadConfig(Path configDir) {
        Path configPath = configDir.resolve("choose_your_destin/choices.json");
        try {
            if (Files.notExists(configPath)) {
                Files.createDirectories(configPath.getParent());
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("choose_your_destin/choices.json")) {
                    if (input != null) {
                        Files.copy(input, configPath);
                    }
                }
            }
            return new ChoiceConfigLoader(new Gson()).load(configPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load choices config", ex);
        }
    }
}
