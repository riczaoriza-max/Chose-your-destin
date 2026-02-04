package com.riczan.choosedestin.quilt;

import com.google.gson.Gson;
import com.riczan.choosedestin.ChoiceConfig;
import com.riczan.choosedestin.ChoiceConfigLoader;
import com.riczan.choosedestin.ChoiceEffect;
import com.riczan.choosedestin.ChoiceRuntime;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.entity.event.v1.LivingEntityDamageEvents;
import net.fabricmc.fabric.api.entity.event.v1.LivingEntityDropEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.BlockDropItemsCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;

public final class ChooseYourDestinQuilt implements ModInitializer {
    public static final ResourceLocation SELECT_CHOICE_PACKET = new ResourceLocation("choose_your_destin", "select_choice");

    private static ChoiceRuntime runtime;
    private static QuiltGameAdapter adapter;

    @Override
    public void onInitialize(ModContainer mod) {
        ServerLifecycleEvents.STARTING.register(server -> {
            adapter = new QuiltGameAdapter(server);
            ChoiceConfig config = loadConfig(QuiltLoader.getConfigDir());
            runtime = ChoiceRuntime.fromConfig(adapter, config);
        });

        ServerLifecycleEvents.STARTED.register(server -> {
            if (runtime != null) {
                runtime.start();
            }
        });

        ServerTickEvents.END.register(server -> {
            if (runtime != null) {
                runtime.tick();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SELECT_CHOICE_PACKET, (server, player, handler, buf, responseSender) -> {
            int index = buf.readInt();
            server.execute(() -> {
                if (runtime != null) {
                    runtime.select(new QuiltGamePlayer(player), index);
                }
            });
        });

        LivingEntityDamageEvents.MODIFY_DAMAGE.register((entity, source, amount) -> {
            if (adapter == null || !(entity instanceof ServerPlayer player)) {
                return amount;
            }
            if ("fall".equals(source.getMsgId())) {
                double multiplier = adapter.getMultiplier(player, ChoiceEffect.FALL_DAMAGE_MULTIPLIER_2X, 1.0);
                return (float) (amount * multiplier);
            }
            return amount;
        });

        BlockDropItemsCallback.EVENT.register((world, player, pos, state, blockEntity, tool, drops) -> {
            if (adapter == null || !(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            double multiplier = adapter.getMultiplier(serverPlayer, ChoiceEffect.RESOURCE_DROP_MULTIPLIER_0_7X, 1.0);
            if (multiplier < 1.0) {
                drops.forEach(itemEntity -> itemEntity.getItem().setCount((int) Math.max(1, Math.floor(itemEntity.getItem().getCount() * multiplier))));
            }
        });

        LivingEntityDropEvents.MODIFY.register((entity, source, drops, recentlyHit) -> {
            if (adapter == null || !(source.getEntity() instanceof ServerPlayer player)) {
                return;
            }
            double multiplier = adapter.getMultiplier(player, ChoiceEffect.RESOURCE_DROP_MULTIPLIER_0_7X, 1.0);
            if (multiplier < 1.0) {
                drops.forEach(itemEntity -> itemEntity.getItem().setCount((int) Math.max(1, Math.floor(itemEntity.getItem().getCount() * multiplier))));
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (adapter == null || !(source.getEntity() instanceof ServerPlayer player) || !(entity instanceof LivingEntity livingEntity)) {
                return;
            }
            double multiplier = adapter.getMultiplier(player, ChoiceEffect.XP_MULTIPLIER_0_8X, 1.0);
            if (multiplier < 1.0) {
                int baseXp = livingEntity.getExperienceReward();
                int reduction = (int) Math.floor(baseXp * (1.0 - multiplier));
                if (reduction > 0) {
                    player.giveExperiencePoints(-reduction);
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
}
