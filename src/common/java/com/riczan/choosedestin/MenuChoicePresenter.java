package com.riczan.choosedestin;

import java.util.Objects;

public final class MenuChoicePresenter {
    private final ChoiceMenu menu;

    public MenuChoicePresenter(ChoiceMenu menu) {
        this.menu = Objects.requireNonNull(menu, "menu");
    }

    public void open(GamePlayer player, Choice choice) {
        menu.show(player, choice);
    }

    public void close(GamePlayer player) {
        menu.close(player);
    }
}
