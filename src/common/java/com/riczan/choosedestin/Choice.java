package com.riczan.choosedestin;

import java.util.List;

public final class Choice {
    private final String id;
    private final String prompt;
    private final List<ChoiceOption> options;
    private final int durationSeconds;

    public Choice(String id, String prompt, List<ChoiceOption> options, int durationSeconds) {
        this.id = id;
        this.prompt = prompt;
        this.options = options;
        this.durationSeconds = durationSeconds;
    }

    public String getId() {
        return id;
    }

    public String getPrompt() {
        return prompt;
    }

    public List<ChoiceOption> getOptions() {
        return options;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }
}
