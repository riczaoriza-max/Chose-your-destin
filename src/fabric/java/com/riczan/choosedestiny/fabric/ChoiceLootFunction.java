package com.riczan.choosedestiny.fabric;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.riczan.choosedestiny.ChoiceEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class ChoiceLootFunction extends ConditionalLootFunction {
    public static final Identifier ID = new Identifier("choose_your_destiny", "choice_multiplier");
    private static LootFunctionType type;

    private ChoiceLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    static void register() {
        if (type != null) {
            return;
        }
        type = Registry.register(Registries.LOOT_FUNCTION_TYPE, ID, new LootFunctionType(new Serializer()));
    }

    static Builder builder() {
        register();
        return new Builder();
    }

    @Override
    public LootFunctionType getType() {
        return type;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        ServerPlayerEntity player = findPlayer(context);
        if (player == null) {
            return stack;
        }
        double multiplier = ChooseYourDestinyFabric.getPlayerMultiplier(player, ChoiceEffect.RESOURCE_DROP_MULTIPLIER_0_7X, 1.0);
        if (multiplier >= 1.0) {
            return stack;
        }
        int newCount = (int) Math.max(1, Math.floor(stack.getCount() * multiplier));
        stack.setCount(newCount);
        return stack;
    }

    private static ServerPlayerEntity findPlayer(LootContext context) {
        PlayerEntity lastDamagePlayer = context.get(LootContextParameters.LAST_DAMAGE_PLAYER);
        if (lastDamagePlayer instanceof ServerPlayerEntity serverPlayer) {
            return serverPlayer;
        }
        Entity killer = context.get(LootContextParameters.KILLER_ENTITY);
        if (killer instanceof ServerPlayerEntity killerPlayer) {
            return killerPlayer;
        }
        Entity entity = context.get(LootContextParameters.THIS_ENTITY);
        if (entity instanceof ServerPlayerEntity thisPlayer) {
            return thisPlayer;
        }
        return null;
    }

    public static final class Builder extends ConditionalLootFunction.Builder<Builder> {
        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new ChoiceLootFunction(getConditions());
        }
    }

    public static final class Serializer extends ConditionalLootFunction.Serializer<ChoiceLootFunction> {
        @Override
        public void toJson(JsonObject json, ChoiceLootFunction object, JsonSerializationContext context) {
            super.toJson(json, object, context);
        }

        @Override
        public ChoiceLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new ChoiceLootFunction(conditions);
        }
    }
}
