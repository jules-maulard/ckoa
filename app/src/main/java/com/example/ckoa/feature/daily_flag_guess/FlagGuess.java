package com.example.ckoa.feature.daily_flag_guess;

public class FlagGuess {
    private String selectedIso3;
    private boolean isCorrect;

    public FlagGuess(String selectedIso3, boolean isCorrect) {
        this.selectedIso3 = selectedIso3;
        this.isCorrect = isCorrect;
    }

    public String getSelectedIso3() {
        return selectedIso3;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}