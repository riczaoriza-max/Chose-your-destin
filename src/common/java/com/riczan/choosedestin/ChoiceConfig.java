package com.riczan.choosedestin;

import java.util.List;

public final class ChoiceConfig {
    private final int version;
    private final int durationSeconds;
    private final List<Choice> choices;

    public ChoiceConfig(int version, int durationSeconds, List<Choice> choices) {
        this.version = version;
        this.durationSeconds = durationSeconds;
        this.choices = choices;
    }

    public int getVersion() {
        return version;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public List<Choice> getChoices() {
        return choices;
    }
}
