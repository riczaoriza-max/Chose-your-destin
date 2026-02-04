package com.riczan.choosedestin;

import java.util.List;
import java.util.Objects;

public final class ChoiceRuntime {
    private final ChoiceManager manager;

    public ChoiceRuntime(ChoiceManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
    }

    public static ChoiceRuntime fromConfig(GameAdapter adapter, ChoiceConfig config) {
        List<Choice> original = config.getChoices();
        List<Choice> normalized = new java.util.ArrayList<>();
        for (Choice choice : original) {
            if (choice.getDurationSeconds() <= 0) {
                normalized.add(new Choice(
                    choice.getId(),
                    choice.getPrompt(),
                    choice.getOptions(),
                    config.getDurationSeconds()
                ));
            } else {
                normalized.add(choice);
            }
        }
        return new ChoiceRuntime(new ChoiceManager(adapter, normalized));
    }

    public void start() {
        manager.startCycleForAllPlayers();
    }

    public void tick() {
        manager.tick();
    }

    public void select(GamePlayer player, int optionIndex) {
        manager.selectOption(player, optionIndex);
    }
}
