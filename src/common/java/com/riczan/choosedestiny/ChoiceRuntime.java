package com.riczan.choosedestiny;

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
            List<ChoiceOption> options = normalizeOptions(choice.getOptions());
            if (choice.getDurationSeconds() <= 0) {
                normalized.add(new Choice(
                    choice.getId(),
                    choice.getPrompt(),
                    options,
                    config.getDurationSeconds()
                ));
            } else {
                normalized.add(new Choice(
                    choice.getId(),
                    choice.getPrompt(),
                    options,
                    choice.getDurationSeconds()
                ));
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

    private static List<ChoiceOption> normalizeOptions(List<ChoiceOption> options) {
        List<ChoiceOption> normalized = new java.util.ArrayList<>(options);
        if (normalized.size() > 2) {
            normalized = normalized.subList(0, 2);
        }
        while (normalized.size() < 2) {
            normalized.add(new ChoiceOption("Balanced path", java.util.List.of()));
        }
        return java.util.List.copyOf(normalized);
    }
}
