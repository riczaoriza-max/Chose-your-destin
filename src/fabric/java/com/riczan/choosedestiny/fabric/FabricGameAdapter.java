package com.riczan.choosedestiny.fabric;

import com.riczan.choosedestiny.Choice;
import com.riczan.choosedestiny.ChoiceEffect;
import com.riczan.choosedestiny.GameAdapter;
import com.riczan.choosedestiny.GamePlayer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class FabricGameAdapter implements GameAdapter {
    static final Identifier OPEN_CHOICE_PACKET = new Identifier("choose_your_destiny", "open_choice");

    private static final int EFFECT_DURATION_TICKS = 20 * 60 * 5;
    private static final UUID DAMAGE_MODIFIER_ID = UUID.fromString("e44a7d2b-5f9b-4d45-97f7-8f584fc35c21");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("7c6d72cf-e76a-4c9a-9a0b-920724d1d5d5");
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("e1905e90-2255-47da-8e2b-441900de0efe");
    private static final UUID KNOCKBACK_MODIFIER_ID = UUID.fromString("b6f8102e-ec1e-451a-b89b-81ac2d0f5198");

    private final MinecraftServer server;
    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();
    private final Map<UUID, EnumSet<ChoiceEffect>> activeEffects = new HashMap<>();

    public FabricGameAdapter(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    @Override
    public List<GamePlayer> getOnlinePlayers() {
        List<GamePlayer> players = new ArrayList<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            players.add(new FabricGamePlayer(player));
        }
        return players;
    }

    @Override
    public void updateBossBar(GamePlayer player, String title, float progress) {
        ServerPlayerEntity handle = unwrap(player);
        if (handle == null) {
            return;
        }
        ServerBossBar bossBar = bossBars.computeIfAbsent(handle.getUuid(), key -> new ServerBossBar(
            Text.literal(title),
            BossBar.Color.GREEN,
            BossBar.Style.NOTCHED_10
        ));
        float normalizedProgress = Math.max(0.0f, Math.min(1.0f, progress));
        bossBar.setName(Text.literal(title));
        bossBar.setPercent(normalizedProgress);
        updateBossBarStyle(bossBar, normalizedProgress);
        if (!bossBar.getPlayers().contains(handle)) {
            bossBar.addPlayer(handle);
        }
    }

    @Override
    public void clearBossBar(GamePlayer player) {
        ServerPlayerEntity handle = unwrap(player);
        if (handle == null) {
            return;
        }
        ServerBossBar bossBar = bossBars.remove(handle.getUuid());
        if (bossBar != null) {
            bossBar.removePlayer(handle);
        }
    }

    @Override
    public void applyEffects(GamePlayer player, List<String> effects) {
        ServerPlayerEntity handle = unwrap(player);
        if (handle == null) {
            return;
        }
        EnumSet<ChoiceEffect> effectSet = EnumSet.noneOf(ChoiceEffect.class);
        for (String effect : effects) {
            ChoiceEffect.fromId(effect).ifPresent(effectSet::add);
        }
        activeEffects.put(handle.getUuid(), effectSet);
        applyEffectSet(handle, effectSet);
    }

    @Override
    public void clearEffects(GamePlayer player) {
        ServerPlayerEntity handle = unwrap(player);
        if (handle == null) {
            return;
        }
        EnumSet<ChoiceEffect> effectSet = activeEffects.remove(handle.getUuid());
        if (effectSet != null) {
            removeEffectSet(handle, effectSet);
        }
    }

    @Override
    public long getGameTimeSeconds() {
        return server.getOverworld().getTime() / 20L;
    }

    @Override
    public void show(GamePlayer player, Choice choice) {
        ServerPlayerEntity handle = unwrap(player);
        if (handle == null) {
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(choice.getPrompt());
        buf.writeInt(choice.getOptions().size());
        choice.getOptions().forEach(option -> buf.writeString(option.getLabel()));
        ServerPlayNetworking.send(handle, OPEN_CHOICE_PACKET, buf);
    }

    @Override
    public void close(GamePlayer player) {
        ServerPlayerEntity handle = unwrap(player);
        if (handle != null) {
            handle.closeHandledScreen();
        }
    }

    private ServerPlayerEntity unwrap(GamePlayer player) {
        if (player instanceof FabricGamePlayer fabricPlayer) {
            return fabricPlayer.getHandle();
        }
        return null;
    }


    private void updateBossBarStyle(ServerBossBar bossBar, float progress) {
        if (progress > 0.66f) {
            bossBar.setColor(BossBar.Color.GREEN);
            bossBar.setStyle(BossBar.Style.NOTCHED_10);
        } else if (progress > 0.33f) {
            bossBar.setColor(BossBar.Color.YELLOW);
            bossBar.setStyle(BossBar.Style.NOTCHED_12);
        } else {
            bossBar.setColor(BossBar.Color.RED);
            bossBar.setStyle(BossBar.Style.NOTCHED_20);
        }
    }

    private void applyEffectSet(ServerPlayerEntity player, EnumSet<ChoiceEffect> effects) {
        for (ChoiceEffect effect : effects) {
            switch (effect) {
                case JUMP_BOOST_III -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, EFFECT_DURATION_TICKS, 2, false, true));
                case SPEED_II -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, EFFECT_DURATION_TICKS, 1, false, true));
                case SLOWNESS_I -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, EFFECT_DURATION_TICKS, 0, false, true));
                case MINING_SPEED_2X -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, EFFECT_DURATION_TICKS, 1, false, true));
                case MINING_SPEED_0_8X -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, EFFECT_DURATION_TICKS, 0, false, true));
                case PLACE_SPEED_2X -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, EFFECT_DURATION_TICKS, 1, false, true));
                case NIGHT_VISION -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, EFFECT_DURATION_TICKS, 0, false, true));
                case REGENERATION_I -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, EFFECT_DURATION_TICKS, 0, false, true));
                case DOLPHINS_GRACE -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, EFFECT_DURATION_TICKS, 0, false, true));
                case HUNGER_DRAIN_MULTIPLIER_1_5X -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, EFFECT_DURATION_TICKS, 0, false, true));
                case AIR_LOSS_MULTIPLIER_1_5X -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, EFFECT_DURATION_TICKS, 0, false, true));
                case LOOT_MULTIPLIER_1_3X -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, EFFECT_DURATION_TICKS, 0, false, true));
                case MOB_SPAWN_MULTIPLIER_1_3X -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, EFFECT_DURATION_TICKS, 0, false, true));
                case DAMAGE_MULTIPLIER_1_25X -> applyAttribute(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, effect.getValue() - 1.0, Operation.MULTIPLY_TOTAL);
                case ATTACK_SPEED_MULTIPLIER_0_85X -> applyAttribute(player, EntityAttributes.GENERIC_ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID, effect.getValue() - 1.0, Operation.MULTIPLY_TOTAL);
                case ARMOR_MULTIPLIER_0_8X -> applyAttribute(player, EntityAttributes.GENERIC_ARMOR, ARMOR_MODIFIER_ID, effect.getValue() - 1.0, Operation.MULTIPLY_TOTAL);
                case KNOCKBACK_RESISTANCE -> applyAttribute(player, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID, effect.getValue(), Operation.ADDITION);
                default -> {
                }
            }
        }
    }

    private void removeEffectSet(ServerPlayerEntity player, EnumSet<ChoiceEffect> effects) {
        for (ChoiceEffect effect : effects) {
            switch (effect) {
                case JUMP_BOOST_III -> player.removeStatusEffect(StatusEffects.JUMP_BOOST);
                case SPEED_II -> player.removeStatusEffect(StatusEffects.SPEED);
                case SLOWNESS_I -> player.removeStatusEffect(StatusEffects.SLOWNESS);
                case MINING_SPEED_2X -> player.removeStatusEffect(StatusEffects.HASTE);
                case MINING_SPEED_0_8X -> player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
                case PLACE_SPEED_2X -> player.removeStatusEffect(StatusEffects.HASTE);
                case NIGHT_VISION -> player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                case REGENERATION_I -> player.removeStatusEffect(StatusEffects.REGENERATION);
                case DOLPHINS_GRACE -> player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
                case HUNGER_DRAIN_MULTIPLIER_1_5X -> player.removeStatusEffect(StatusEffects.HUNGER);
                case AIR_LOSS_MULTIPLIER_1_5X -> player.removeStatusEffect(StatusEffects.WATER_BREATHING);
                case LOOT_MULTIPLIER_1_3X -> player.removeStatusEffect(StatusEffects.LUCK);
                case MOB_SPAWN_MULTIPLIER_1_3X -> player.removeStatusEffect(StatusEffects.BAD_OMEN);
                case DAMAGE_MULTIPLIER_1_25X -> removeAttribute(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE_MODIFIER_ID);
                case ATTACK_SPEED_MULTIPLIER_0_85X -> removeAttribute(player, EntityAttributes.GENERIC_ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID);
                case ARMOR_MULTIPLIER_0_8X -> removeAttribute(player, EntityAttributes.GENERIC_ARMOR, ARMOR_MODIFIER_ID);
                case KNOCKBACK_RESISTANCE -> removeAttribute(player, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID);
                default -> {
                }
            }
        }
    }

    private void applyAttribute(ServerPlayerEntity player, EntityAttribute attribute, UUID id, double amount, Operation operation) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance == null) {
            return;
        }
        EntityAttributeModifier existing = instance.getModifier(id);
        if (existing != null) {
            instance.removeModifier(id);
        }
        instance.addPersistentModifier(new EntityAttributeModifier(id, "choose_your_destiny_effect", amount, operation));
    }

    private void removeAttribute(ServerPlayerEntity player, EntityAttribute attribute, UUID id) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    double getMultiplier(ServerPlayerEntity player, ChoiceEffect effect, double fallback) {
        EnumSet<ChoiceEffect> effectSet = activeEffects.get(player.getUuid());
        if (effectSet != null && effectSet.contains(effect)) {
            return effect.getValue();
        }
        return fallback;
    }
}
