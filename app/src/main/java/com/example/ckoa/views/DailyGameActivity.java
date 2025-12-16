package com.example.ckoa.views;

import java.util.Calendar;
import java.util.Random;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import com.example.ckoa.views.GeoShapeView;
import java.io.InputStream;
import com.example.ckoa.R;

public class DailyGameActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_game);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });
        GeoShapeView geoShapeView = findViewById(R.id.geoShapeView);

        loadGeoJson(geoShapeView);
    }

    private void loadGeoJson(GeoShapeView view) {
        try {
            // Assure-toi d'avoir ton fichier dans res/raw/corse.json
            InputStream is = getResources().openRawResource(R.raw.france);

            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String jsonComplet = new String(buffer, "UTF-8");

            // C'est ici que la magie opère
            view.setGeoJson(jsonComplet);

        } catch (Exception e) {
            e.printStackTrace(); // Regarde le Logcat si ça ne s'affiche pas
        }
    }

    public static int getDailyCountryId() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int seed = year * 1000 + dayOfYear;

        Random random = new Random(seed);
        return random.nextInt(193) + 1;
    }


}
