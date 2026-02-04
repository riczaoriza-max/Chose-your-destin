package com.riczan.choosedestin;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class ChoiceSelector {
    private ChoiceSelector() {
    }

    public static Choice pickRandom(List<Choice> choices, UUID seed) {
        if (choices.isEmpty()) {
            throw new IllegalStateException("Choices list is empty");
        }
        Random random = new Random(seed.getMostSignificantBits() ^ seed.getLeastSignificantBits());
        return choices.get(random.nextInt(choices.size()));
    }
}
