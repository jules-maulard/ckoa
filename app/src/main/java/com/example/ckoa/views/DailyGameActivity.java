package com.example.ckoa.views;

import java.util.Calendar;
import java.util.Random;

public class DailyGameActivity {

    public static int getDailyCountryId() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int seed = year * 1000 + dayOfYear;

        Random random = new Random(seed);
        return random.nextInt(300) + 1;
    }


}
