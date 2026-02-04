package com.riczan.choosedestin.neoforge;

import com.riczan.choosedestin.GamePlayer;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public final class NeoForgeGamePlayer implements GamePlayer {
    private final ServerPlayer player;

    public NeoForgeGamePlayer(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getHandle() {
        return player;
    }

    @Override
    public UUID getId() {
        return player.getUUID();
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }
}
