package com.riczan.choosedestiny;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public final class ChoiceManager {
    private final GameAdapter adapter;
    private final MenuChoicePresenter menuPresenter;
    private final List<Choice> choices;
    private final Map<UUID, ActiveChoice> activeChoices = new HashMap<>();
    private final Map<UUID, Deque<Choice>> choiceQueues = new HashMap<>();

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
        Choice choice = nextChoice(player);
        ActiveChoice activeChoice = new ActiveChoice(choice, adapter.getGameTimeSeconds());
        activeChoices.put(player.getId(), activeChoice);
        menuPresenter.open(player, choice);
    }

    public void selectOption(GamePlayer player, int optionIndex) {
        ActiveChoice activeChoice = activeChoices.get(player.getId());
        if (activeChoice == null || activeChoice.getSelectedIndex() >= 0) {
            return;
        }
        if (optionIndex < 0 || optionIndex >= activeChoice.getChoice().getOptions().size()) {
            return;
        }
        activeChoice.setSelectedIndex(optionIndex);
        ChoiceOption option = activeChoice.getChoice().getOptions().get(optionIndex);
        adapter.clearEffects(player);
        adapter.applyEffects(player, option.getEffects());
        menuPresenter.close(player);
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
            adapter.updateBossBar(player, formatBossBarTitle(remaining), progress);
            if (remaining <= 0) {
                adapter.clearEffects(player);
                adapter.clearBossBar(player);
                menuPresenter.close(player);
                startNewChoice(player);
            }
        }
    }

    private String formatBossBarTitle(long remaining) {
        return "✦ Escolha ativa ✦ §7(" + remaining + "s)";
    }

    private Choice nextChoice(GamePlayer player) {
        Deque<Choice> queue = choiceQueues.computeIfAbsent(player.getId(), key -> buildQueue(player));
        if (queue.isEmpty()) {
            queue.addAll(buildQueue(player));
        }
        Choice next = queue.pollFirst();
        if (next == null) {
            throw new IllegalStateException("No choices available to start a new cycle");
        }
        return next;
    }

    private Deque<Choice> buildQueue(GamePlayer player) {
        List<Choice> shuffled = new ArrayList<>(choices);
        long seed = player.getId().getMostSignificantBits() ^ player.getId().getLeastSignificantBits() ^ System.nanoTime();
        Collections.shuffle(shuffled, new Random(seed));
        return new ArrayDeque<>(shuffled);
    }
}
