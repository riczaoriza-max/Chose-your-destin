package com.riczan.choosedestin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ChoiceEffect {
    JUMP_BOOST_III("JUMP_BOOST_III", EffectCategory.POTION, 2),
    FALL_DAMAGE_MULTIPLIER_2X("FALL_DAMAGE_MULTIPLIER_2X", EffectCategory.MULTIPLIER, 2.0),
    MINING_SPEED_2X("MINING_SPEED_2X", EffectCategory.POTION, 1),
    RESOURCE_DROP_MULTIPLIER_0_7X("RESOURCE_DROP_MULTIPLIER_0_7X", EffectCategory.MULTIPLIER, 0.7),
    MINING_SPEED_0_8X("MINING_SPEED_0_8X", EffectCategory.POTION, 0),
    SPEED_II("SPEED_II", EffectCategory.POTION, 1),
    SLOWNESS_I("SLOWNESS_I", EffectCategory.POTION, 0),
    DAMAGE_MULTIPLIER_1_25X("DAMAGE_MULTIPLIER_1_25X", EffectCategory.ATTRIBUTE, 1.25),
    ATTACK_SPEED_MULTIPLIER_0_85X("ATTACK_SPEED_MULTIPLIER_0_85X", EffectCategory.ATTRIBUTE, 0.85),
    ARMOR_MULTIPLIER_0_8X("ARMOR_MULTIPLIER_0_8X", EffectCategory.ATTRIBUTE, 0.8),
    KNOCKBACK_RESISTANCE("KNOCKBACK_RESISTANCE", EffectCategory.ATTRIBUTE, 0.4),
    HUNGER_DRAIN_MULTIPLIER_1_5X("HUNGER_DRAIN_MULTIPLIER_1_5X", EffectCategory.POTION, 0),
    AIR_LOSS_MULTIPLIER_1_5X("AIR_LOSS_MULTIPLIER_1_5X", EffectCategory.POTION, 0),
    XP_MULTIPLIER_0_8X("XP_MULTIPLIER_0_8X", EffectCategory.MULTIPLIER, 0.8),
    LOOT_MULTIPLIER_1_3X("LOOT_MULTIPLIER_1_3X", EffectCategory.POTION, 0),
    MOB_SPAWN_MULTIPLIER_1_3X("MOB_SPAWN_MULTIPLIER_1_3X", EffectCategory.POTION, 0),
    PLACE_SPEED_2X("PLACE_SPEED_2X", EffectCategory.POTION, 1),
    NIGHT_VISION("NIGHT_VISION", EffectCategory.POTION, 0),
    REGENERATION_I("REGENERATION_I", EffectCategory.POTION, 0),
    DOLPHINS_GRACE("DOLPHINS_GRACE", EffectCategory.POTION, 0);

    private static final Map<String, ChoiceEffect> BY_ID;

    static {
        Map<String, ChoiceEffect> values = new HashMap<>();
        for (ChoiceEffect effect : values()) {
            values.put(effect.id, effect);
        }
        BY_ID = Collections.unmodifiableMap(values);
    }

    private final String id;
    private final EffectCategory category;
    private final double value;

    ChoiceEffect(String id, EffectCategory category, double value) {
        this.id = id;
        this.category = category;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public EffectCategory getCategory() {
        return category;
    }

    public double getValue() {
        return value;
    }

    public static Optional<ChoiceEffect> fromId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public enum EffectCategory {
        POTION,
        ATTRIBUTE,
        MULTIPLIER
    }
}
