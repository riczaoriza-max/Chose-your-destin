package com.riczan.choosedestiny;

public final class ActiveChoice {
    private final Choice choice;
    private final long startTimeSeconds;
    private int selectedIndex;

    public ActiveChoice(Choice choice, long startTimeSeconds) {
        this.choice = choice;
        this.startTimeSeconds = startTimeSeconds;
        this.selectedIndex = -1;
    }

    public Choice getChoice() {
        return choice;
    }

    public long getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
}
