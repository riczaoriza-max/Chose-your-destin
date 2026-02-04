package com.riczan.choosedestin.fabric;

import com.riczan.choosedestin.GamePlayer;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;

public final class FabricGamePlayer implements GamePlayer {
    private final ServerPlayerEntity player;

    public FabricGamePlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getHandle() {
        return player;
    }

    @Override
    public UUID getId() {
        return player.getUuid();
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }
}
