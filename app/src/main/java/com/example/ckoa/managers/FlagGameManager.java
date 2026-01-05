package com.example.ckoa.managers;

import android.content.Context;

import com.example.ckoa.managers.data.GameRepository;
import com.example.ckoa.models.CountryBase;
import com.example.ckoa.models.CountryMeta;

import java.util.Collections;
import java.util.List;

public class FlagGameManager {

    private final GameRepository repository;
    private final DailyGameManager dailyManager;
    private CountryBase targetCountry;

    public static class FlagRoundData {
        public String targetName;
        public String targetIso;
        public List<String> optionsIso;

        public FlagRoundData(String targetName, String targetIso, List<String> optionsIso) {
            this.targetName = targetName;
            this.targetIso = targetIso;
            this.optionsIso = optionsIso;
        }
    }

    public FlagGameManager(Context context) {
        this.repository = new GameRepository(context);
        this.dailyManager = new DailyGameManager(this.repository);
    }

    public FlagRoundData prepareDailyRound() {
        targetCountry = dailyManager.getDailyTarget();
        if (targetCountry == null) return null;

        String correctIso = targetCountry.getIso3();

        List<String> options = repository.getRandomIsoCodes(6);

        if (!options.contains(correctIso)) {
            options.remove(0);
            options.add(correctIso);
        }
        Collections.shuffle(options);

        return new FlagRoundData(targetCountry.getNameFr(), correctIso, options);
    }

    public boolean checkGuess(String selectedIso) {
        if (targetCountry == null) return false;
        return selectedIso.equals(targetCountry.getIso3());
    }

    public String getFlagUrl(String iso3) {
        CountryMeta meta = repository.getCountryMeta(iso3);
        if (meta != null) {
            return meta.getFlag();
        }
        return null;
    }

    public String getCountryNameByIso(String iso) {
        return repository.getCountryNameFr(iso);
    }
}