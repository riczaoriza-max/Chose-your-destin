package com.riczan.choosedestin.forge;

import com.riczan.choosedestin.Choice;
import com.riczan.choosedestin.ChoiceEffect;
import com.riczan.choosedestin.GameAdapter;
import com.riczan.choosedestin.GamePlayer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ForgeGameAdapter implements GameAdapter {
    private static final int EFFECT_DURATION_TICKS = 20 * 60 * 5;
    private static final UUID DAMAGE_MODIFIER_ID = UUID.fromString("e44a7d2b-5f9b-4d45-97f7-8f584fc35c21");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("7c6d72cf-e76a-4c9a-9a0b-920724d1d5d5");
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("e1905e90-2255-47da-8e2b-441900de0efe");
    private static final UUID KNOCKBACK_MODIFIER_ID = UUID.fromString("b6f8102e-ec1e-451a-b89b-81ac2d0f5198");

    private final MinecraftServer server;
    private final Map<UUID, ServerBossEvent> bossBars = new HashMap<>();
    private final Map<UUID, EnumSet<ChoiceEffect>> activeEffects = new HashMap<>();

    public ForgeGameAdapter(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    @Override
    public List<GamePlayer> getOnlinePlayers() {
        List<GamePlayer> players = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            players.add(new ForgeGamePlayer(player));
        }
        return players;
    }

    @Override
    public void updateBossBar(GamePlayer player, String title, float progress) {
        ServerPlayer handle = unwrap(player);
        if (handle == null) {
            return;
        }
        ServerBossEvent bossBar = bossBars.computeIfAbsent(handle.getUUID(), key -> new ServerBossEvent(
            Component.literal(title),
            ServerBossEvent.BossBarColor.BLUE,
            ServerBossEvent.BossBarOverlay.PROGRESS
        ));
        bossBar.setName(Component.literal(title));
        bossBar.setProgress(progress);
        if (!bossBar.getPlayers().contains(handle)) {
            bossBar.addPlayer(handle);
        }
    }

    @Override
    public void clearBossBar(GamePlayer player) {
        ServerPlayer handle = unwrap(player);
        if (handle == null) {
            return;
        }
        ServerBossEvent bossBar = bossBars.remove(handle.getUUID());
        if (bossBar != null) {
            bossBar.removePlayer(handle);
        }
    }

    @Override
    public void applyEffects(GamePlayer player, List<String> effects) {
        ServerPlayer handle = unwrap(player);
        if (handle == null) {
            return;
        }
        EnumSet<ChoiceEffect> effectSet = EnumSet.noneOf(ChoiceEffect.class);
        for (String effect : effects) {
            ChoiceEffect.fromId(effect).ifPresent(effectSet::add);
        }
        activeEffects.put(handle.getUUID(), effectSet);
        applyEffectSet(handle, effectSet);
    }

    @Override
    public void clearEffects(GamePlayer player) {
        ServerPlayer handle = unwrap(player);
        if (handle == null) {
            return;
        }
        EnumSet<ChoiceEffect> effectSet = activeEffects.remove(handle.getUUID());
        if (effectSet != null) {
            removeEffectSet(handle, effectSet);
        }
    }

    @Override
    public long getGameTimeSeconds() {
        return server.getTickCount() / 20L;
    }

    @Override
    public void show(GamePlayer player, Choice choice) {
        ServerPlayer handle = unwrap(player);
        if (handle == null) {
            return;
        }
        ForgeNetworking.sendOpenChoice(handle, choice);
    }

    @Override
    public void close(GamePlayer player) {
        ServerPlayer handle = unwrap(player);
        if (handle != null) {
            handle.closeContainer();
        }
    }

    private ServerPlayer unwrap(GamePlayer player) {
        if (player instanceof ForgeGamePlayer forgePlayer) {
            return forgePlayer.getHandle();
        }
        return null;
    }

    private void applyEffectSet(ServerPlayer player, EnumSet<ChoiceEffect> effects) {
        for (ChoiceEffect effect : effects) {
            switch (effect) {
                case JUMP_BOOST_III -> player.addEffect(new MobEffectInstance(MobEffects.JUMP, EFFECT_DURATION_TICKS, 2, false, true));
                case SPEED_II -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EFFECT_DURATION_TICKS, 1, false, true));
                case SLOWNESS_I -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_TICKS, 0, false, true));
                case MINING_SPEED_2X -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, EFFECT_DURATION_TICKS, 1, false, true));
                case MINING_SPEED_0_8X -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, EFFECT_DURATION_TICKS, 0, false, true));
                case PLACE_SPEED_2X -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, EFFECT_DURATION_TICKS, 1, false, true));
                case NIGHT_VISION -> player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_DURATION_TICKS, 0, false, true));
                case REGENERATION_I -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION_TICKS, 0, false, true));
                case DOLPHINS_GRACE -> player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, EFFECT_DURATION_TICKS, 0, false, true));
                case HUNGER_DRAIN_MULTIPLIER_1_5X -> player.addEffect(new MobEffectInstance(MobEffects.HUNGER, EFFECT_DURATION_TICKS, 0, false, true));
                case AIR_LOSS_MULTIPLIER_1_5X -> player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, EFFECT_DURATION_TICKS, 0, false, true));
                case LOOT_MULTIPLIER_1_3X -> player.addEffect(new MobEffectInstance(MobEffects.LUCK, EFFECT_DURATION_TICKS, 0, false, true));
                case MOB_SPAWN_MULTIPLIER_1_3X -> player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, EFFECT_DURATION_TICKS, 0, false, true));
                case DAMAGE_MULTIPLIER_1_25X -> applyAttribute(player, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, effect.getValue() - 1.0, Operation.MULTIPLY_TOTAL);
                case ATTACK_SPEED_MULTIPLIER_0_85X -> applyAttribute(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID, effect.getValue() - 1.0, Operation.MULTIPLY_TOTAL);
                case ARMOR_MULTIPLIER_0_8X -> applyAttribute(player, Attributes.ARMOR, ARMOR_MODIFIER_ID, effect.getValue() - 1.0, Operation.MULTIPLY_TOTAL);
                case KNOCKBACK_RESISTANCE -> applyAttribute(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID, effect.getValue(), Operation.ADDITION);
                default -> {
                }
            }
        }
    }

    private void removeEffectSet(ServerPlayer player, EnumSet<ChoiceEffect> effects) {
        for (ChoiceEffect effect : effects) {
            switch (effect) {
                case JUMP_BOOST_III -> player.removeEffect(MobEffects.JUMP);
                case SPEED_II -> player.removeEffect(MobEffects.MOVEMENT_SPEED);
                case SLOWNESS_I -> player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                case MINING_SPEED_2X -> player.removeEffect(MobEffects.DIG_SPEED);
                case MINING_SPEED_0_8X -> player.removeEffect(MobEffects.DIG_SLOWDOWN);
                case PLACE_SPEED_2X -> player.removeEffect(MobEffects.DIG_SPEED);
                case NIGHT_VISION -> player.removeEffect(MobEffects.NIGHT_VISION);
                case REGENERATION_I -> player.removeEffect(MobEffects.REGENERATION);
                case DOLPHINS_GRACE -> player.removeEffect(MobEffects.DOLPHINS_GRACE);
                case HUNGER_DRAIN_MULTIPLIER_1_5X -> player.removeEffect(MobEffects.HUNGER);
                case AIR_LOSS_MULTIPLIER_1_5X -> player.removeEffect(MobEffects.WATER_BREATHING);
                case LOOT_MULTIPLIER_1_3X -> player.removeEffect(MobEffects.LUCK);
                case MOB_SPAWN_MULTIPLIER_1_3X -> player.removeEffect(MobEffects.BAD_OMEN);
                case DAMAGE_MULTIPLIER_1_25X -> removeAttribute(player, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID);
                case ATTACK_SPEED_MULTIPLIER_0_85X -> removeAttribute(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID);
                case ARMOR_MULTIPLIER_0_8X -> removeAttribute(player, Attributes.ARMOR, ARMOR_MODIFIER_ID);
                case KNOCKBACK_RESISTANCE -> removeAttribute(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID);
                default -> {
                }
            }
        }
    }

    private void applyAttribute(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id, double amount, Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (existing != null) {
            instance.removeModifier(id);
        }
        instance.addPermanentModifier(new AttributeModifier(id, "choose_your_destin_effect", amount, operation));
    }

    private void removeAttribute(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    double getMultiplier(ServerPlayer player, ChoiceEffect effect, double fallback) {
        EnumSet<ChoiceEffect> effectSet = activeEffects.get(player.getUUID());
        if (effectSet != null && effectSet.contains(effect)) {
            return effect.getValue();
        }
        return fallback;
    }
}
