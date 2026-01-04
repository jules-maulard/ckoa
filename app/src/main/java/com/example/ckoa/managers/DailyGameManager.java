package com.example.ckoa.managers;

import com.example.ckoa.managers.data.GameRepository;
import com.example.ckoa.models.CountryBase;
import java.util.Calendar;
import java.util.Random;

public class DailyGameManager {

    private final GameRepository repository;

    public DailyGameManager(GameRepository repository) {
        this.repository = repository;
    }

    public CountryBase getDailyTarget() {
        int dailyId = calculateDailySeed();
        return repository.getCountryByOffset(dailyId - 1);
    }

    private int calculateDailySeed() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int seed = year * 1000 + dayOfYear;
        Random random = new Random(seed);
        return random.nextInt(193) + 1;
    }
}