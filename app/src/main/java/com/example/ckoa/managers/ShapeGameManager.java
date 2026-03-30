package com.example.ckoa.managers;

import android.content.Context;
import android.location.Location;

import com.example.ckoa.data.GameRepository;
import com.example.ckoa.data.ProgressRepository;
import com.example.ckoa.models.CountryBase;
import com.example.ckoa.models.DailyStep;
import com.example.ckoa.models.ShapeGuess;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class ShapeGameManager {

    private final GameRepository repository;
    private final ProgressRepository progressRepository;
    private final GameStatsManager gameStatsManager;
    private CountryBase targetCountry;

    private int attemptsCount = 0;
    private boolean isGameFinished = false;
    private final int MAX_ATTEMPTS = 6;

    public ShapeGameManager(Context context) {
        this.repository = new GameRepository(context);
        this.progressRepository = new ProgressRepository(context);
        this.gameStatsManager = new GameStatsManager(context);
    }

    public void loadGameState() {
        List<ShapeGuess> history = getHistoryGuesses();
        attemptsCount = history.size();
        for (ShapeGuess guess : history) {
            if (guess.getIs_correct() || attemptsCount >= MAX_ATTEMPTS) {
                isGameFinished = true;
                break;
            }
        }
    }

    public boolean isGameFinished() {
        return isGameFinished;
    }

    public boolean isGameLost() {
        return isGameFinished && attemptsCount >= MAX_ATTEMPTS;
    }

    public List<ShapeGuess> getHistoryGuesses() {
        return progressRepository.getShapeGuesses();
    }

    public ShapeGuess processUserGuess(String guessName) {
        if (isGameFinished) return null;

        CountryBase guessCountry = repository.getCountryByName(guessName);
        if (guessCountry == null) return null;

        float[] results = new float[2];
        Location.distanceBetween(
                guessCountry.getCentroidLat(), guessCountry.getCentroidLon(),
                targetCountry.getCentroidLat(), targetCountry.getCentroidLon(),
                results
        );
        double distanceKm = results[0] / 1000.0;
        double bearing = results[1];

        boolean isWin = guessCountry.getIso3().equals(targetCountry.getIso3());
        ShapeGuess shapeGuess = new ShapeGuess(guessCountry.getIso3(), distanceKm, bearing, isWin);

        List<ShapeGuess> currentGuesses = progressRepository.getShapeGuesses();
        currentGuesses.add(shapeGuess);
        progressRepository.saveShapeGuesses(currentGuesses);

        attemptsCount++;

        if (isWin) {
            gameStatsManager.saveGameResult(attemptsCount, true);
            finishGame();
        } else if (attemptsCount >= MAX_ATTEMPTS) {
            gameStatsManager.saveGameResult(attemptsCount, false);
            finishGame();
        }

        return shapeGuess;
    }

    private void finishGame() {
        isGameFinished = true;
        advanceToNextStep();
    }

    private void advanceToNextStep() {
        progressRepository.saveCurrentStep(DailyStep.FLAG);
    }

    public String loadDailyTarget() {
        int dailyId = getDailyCountryId();
        this.targetCountry = repository.getCountryByOffset(dailyId - 1);

        if (this.targetCountry != null) {
            return this.targetCountry.getGeoShape();
        }
        return null;
    }

    public List<String> getAllCountryNames() {
        return repository.getAllCountryNames();
    }

    public String getTargetName() {
        return targetCountry != null ? targetCountry.getNameFr() : "";
    }

    private String getDirectionArrow(double bearing) {
        String arrows = "⬆↗➡↘⬇↙⬅↖";
        int index = (int) Math.round(((bearing % 360) + 360) % 360 / 45.0);
        return String.valueOf(arrows.charAt(index % 8));
    }

    public String formatHistoryItem(ShapeGuess shapeGuess, String countryName) {
        boolean isWin = shapeGuess.getIs_correct();
        String emoji = isWin ? "🏆" : "❌";
        String arrow = isWin ? "" : getDirectionArrow(shapeGuess.getBearing_deg());

        return isWin
                ? String.format("%s %s - Won!", emoji, countryName)
                : String.format("%s %s : %.0f km %s", emoji, countryName, shapeGuess.getDistance_km(), arrow);
    }

    private int getDailyCountryId() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int seed = year * 1000 + dayOfYear;
        Random random = new Random(seed);
        return random.nextInt(193) + 1;
    }

    public String getCountryNameByIso(String iso) {
        return repository.getCountryNameFr(iso);
    }
}