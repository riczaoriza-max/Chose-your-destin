package com.riczan.choosedestin.fabric;

import com.google.gson.Gson;
import com.riczan.choosedestin.ChoiceConfig;
import com.riczan.choosedestin.ChoiceConfigLoader;
import com.riczan.choosedestin.ChoiceEffect;
import com.riczan.choosedestin.ChoiceRuntime;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChooseYourDestinFabric implements ModInitializer {
    public static final Identifier SELECT_CHOICE_PACKET = new Identifier("choose_your_destin", "select_choice");
    private static final Logger LOGGER = LoggerFactory.getLogger("choose_your_destin");

    private static ChoiceRuntime runtime;
    private static FabricGameAdapter adapter;
    private static final ThreadLocal<Boolean> DAMAGE_REENTRY = ThreadLocal.withInitial(() -> false);
    private static final String ENTITY_LOOT_PREFIX = "entities/";
    private static final String BLOCK_LOOT_PREFIX = "blocks/";

    @Override
    public void onInitialize() {
        LOGGER.info("We are alive (riczan mods)");
        ChoiceLootFunction.register();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            adapter = new FabricGameAdapter(server);
            ChoiceConfig config = loadConfig(FabricLoader.getInstance().getConfigDir());
            runtime = ChoiceRuntime.fromConfig(adapter, config);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (runtime != null) {
                runtime.start();
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (runtime != null) {
                runtime.tick();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SELECT_CHOICE_PACKET, (server, player, handler, buf, responseSender) -> {
            int index = buf.readInt();
            server.execute(() -> {
                if (runtime != null) {
                    runtime.select(new FabricGamePlayer(player), index);
                }
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            player.sendMessage(Text.literal("Thanks for playing Chose your destin by riczan :)"), false);
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (adapter == null || !(entity instanceof ServerPlayerEntity player)) {
                return true;
            }
            if (Boolean.TRUE.equals(DAMAGE_REENTRY.get())) {
                return true;
            }
            if ("fall".equals(source.getName())) {
                double multiplier = adapter.getMultiplier(player, ChoiceEffect.FALL_DAMAGE_MULTIPLIER_2X, 1.0);
                if (Double.compare(multiplier, 1.0) != 0) {
                    DAMAGE_REENTRY.set(true);
                    entity.damage(source, (float) (amount * multiplier));
                    DAMAGE_REENTRY.set(false);
                    return false;
                }
            }
            return true;
        });

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!"minecraft".equals(id.getNamespace())) {
                return;
            }
            String path = id.getPath();
            if (!path.startsWith(ENTITY_LOOT_PREFIX) && !path.startsWith(BLOCK_LOOT_PREFIX)) {
                return;
            }
            tableBuilder.modifyPools(poolBuilder -> poolBuilder.apply(ChoiceLootFunction.builder()));
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (adapter == null || !(source.getAttacker() instanceof ServerPlayerEntity player)) {
                return;
            }
            double multiplier = adapter.getMultiplier(player, ChoiceEffect.XP_MULTIPLIER_0_8X, 1.0);
            if (multiplier < 1.0) {
                int baseXp = entity.getXpToDrop();
                int reduction = (int) Math.floor(baseXp * (1.0 - multiplier));
                if (reduction > 0) {
                    player.addExperience(-reduction);
                }
            }
        });
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

    static double getPlayerMultiplier(ServerPlayerEntity player, ChoiceEffect effect, double fallback) {
        if (adapter == null) {
            return fallback;
        }
        return adapter.getMultiplier(player, effect, fallback);
    }
}
