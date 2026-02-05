package com.riczan.choosedestin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ChoiceManager {
    private final GameAdapter adapter;
    private final MenuChoicePresenter menuPresenter;
    private final List<Choice> choices;
    private final Map<UUID, ActiveChoice> activeChoices = new HashMap<>();

    public ChoiceManager(GameAdapter adapter, List<Choice> choices) {
        this(adapter, choices, new MenuChoicePresenter(adapter));
    }

    public ChoiceManager(GameAdapter adapter, List<Choice> choices, MenuChoicePresenter menuPresenter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
        this.choices = new ArrayList<>(choices);
        this.menuPresenter = Objects.requireNonNull(menuPresenter, "menuPresenter");
    }

    public void startCycleForAllPlayers() {
        for (GamePlayer player : adapter.getOnlinePlayers()) {
            startNewChoice(player);
        }
    }

    public void startNewChoice(GamePlayer player) {
        Choice choice = ChoiceSelector.pickRandom(choices, player.getId());
        ActiveChoice activeChoice = new ActiveChoice(choice, adapter.getGameTimeSeconds());
        activeChoices.put(player.getId(), activeChoice);
        menuPresenter.open(player, choice);
    }

    public void selectOption(GamePlayer player, int optionIndex) {
        ActiveChoice activeChoice = activeChoices.get(player.getId());
        if (activeChoice == null) {
            return;
        }
        activeChoice.setSelectedIndex(optionIndex);
        ChoiceOption option = activeChoice.getChoice().getOptions().get(optionIndex);
        adapter.clearEffects(player);
        adapter.applyEffects(player, option.getEffects());
    }

    public void tick() {
        long now = adapter.getGameTimeSeconds();
        for (GamePlayer player : adapter.getOnlinePlayers()) {
            ActiveChoice activeChoice = activeChoices.get(player.getId());
            if (activeChoice == null) {
                startNewChoice(player);
                continue;
            }
            long elapsed = now - activeChoice.getStartTimeSeconds();
            int duration = activeChoice.getChoice().getDurationSeconds();
            long remaining = Math.max(0, duration - elapsed);
            float progress = duration == 0 ? 0.0f : (float) remaining / (float) duration;
            adapter.updateBossBar(player, "Active Choice · " + normalizePrompt(activeChoice.getChoice().getPrompt()), progress);
            if (remaining <= 0) {
                adapter.clearEffects(player);
                adapter.clearBossBar(player);
                menuPresenter.close(player);
                startNewChoice(player);
            }
        }
    }

    private String normalizePrompt(String prompt) {
        if (prompt == null) {
            return "Select Option 1 or Option 2";
        }
        return prompt.replaceAll("(?i)\\s+or\\s+no\\s+bonus\\??$", "");
    }
}
