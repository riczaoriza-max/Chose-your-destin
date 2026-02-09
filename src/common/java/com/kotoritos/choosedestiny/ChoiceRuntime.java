package com.kotoritos.choosedestiny;

import java.util.List;
import java.util.Objects;

public final class ChoiceRuntime {
    private static final int FALLBACK_DURATION_SECONDS = 180;
    private final ChoiceManager manager;

    public ChoiceRuntime(ChoiceManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
    }

    public static ChoiceRuntime fromConfig(GameAdapter adapter, ChoiceConfig config) {
        Objects.requireNonNull(adapter, "adapter");
        Objects.requireNonNull(config, "config");

        List<Choice> original = config.getChoices() == null ? List.of() : config.getChoices();
        if (original.isEmpty()) {
            throw new IllegalStateException("Choice config must define at least one choice");
        }

        int defaultDuration = config.getDurationSeconds() > 0 ? config.getDurationSeconds() : FALLBACK_DURATION_SECONDS;
        List<Choice> normalized = new java.util.ArrayList<>();
        for (Choice choice : original) {
            if (choice == null) {
                continue;
            }
            List<ChoiceOption> options = normalizeOptions(choice.getOptions());
            int choiceDuration = choice.getDurationSeconds() > 0 ? choice.getDurationSeconds() : defaultDuration;
            normalized.add(new Choice(
                choice.getId(),
                choice.getPrompt(),
                options,
                choiceDuration
            ));
        }

        if (normalized.isEmpty()) {
            throw new IllegalStateException("Choice config must include valid non-null choices");
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
        List<ChoiceOption> source = options == null ? List.of() : options;
        List<ChoiceOption> normalized = new java.util.ArrayList<>(source);
        normalized.removeIf(Objects::isNull);
        if (normalized.size() > 2) {
            normalized = normalized.subList(0, 2);
        }
        while (normalized.size() < 2) {
            normalized.add(new ChoiceOption("Balanced path", java.util.List.of()));
        }
        return java.util.List.copyOf(normalized);
    }
}
