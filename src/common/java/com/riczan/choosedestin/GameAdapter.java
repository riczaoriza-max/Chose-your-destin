package com.riczan.choosedestin;

import java.util.List;

public interface GameAdapter extends ChoiceMenu {
    List<GamePlayer> getOnlinePlayers();

    void updateBossBar(GamePlayer player, String title, float progress);

    void clearBossBar(GamePlayer player);

    void applyEffects(GamePlayer player, List<String> effects);

    void clearEffects(GamePlayer player);

    long getGameTimeSeconds();
}
