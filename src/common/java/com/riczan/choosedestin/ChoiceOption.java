package com.riczan.choosedestin;

import java.util.List;

public final class ChoiceOption {
    private final String label;
    private final List<String> effects;

    public ChoiceOption(String label, List<String> effects) {
        this.label = label;
        this.effects = effects;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getEffects() {
        return effects;
    }
}
