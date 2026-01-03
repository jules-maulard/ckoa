package com.example.ckoa.views;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ckoa.R;
import com.example.ckoa.managers.GameHandler;

import java.util.Collections;
import java.util.List;

public class DailyFlagActivity extends AppCompatActivity {

    private GameHandler gameHandler;
    private String targetIso;
    private TextView textTargetCountry;
    private ImageButton[] flagButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_flag);

        gameHandler = new GameHandler(this);
        initViews();
        startNewRound();
    }

    private void initViews() {
        textTargetCountry = findViewById(R.id.textTargetCountry);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        flagButtons = new ImageButton[]{
                findViewById(R.id.btnFlag1), findViewById(R.id.btnFlag2),
                findViewById(R.id.btnFlag3), findViewById(R.id.btnFlag4),
                findViewById(R.id.btnFlag5), findViewById(R.id.btnFlag6)
        };
    }

    private void startNewRound() {
        List<String> isos = gameHandler.getRandomIsoCodes(6);
        if (isos.size() < 6) return;

        targetIso = isos.get(0);

        String targetName = gameHandler.getCountryName(targetIso);
        textTargetCountry.setText(targetName);

        Collections.shuffle(isos);

        for (int i = 0; i < flagButtons.length; i++) {
            String iso = isos.get(i);
            ImageButton button = flagButtons[i];

            button.setImageResource(getFlagResId(iso));
            button.setTag(iso); // On stocke l'ISO dans le bouton
            button.setOnClickListener(this::onFlagClicked);
        }
    }

    private void onFlagClicked(View view) {
        String selectedIso = (String) view.getTag();

        if (selectedIso.equals(targetIso)) {
            // Victoire -> Recharger une manche ou changer d'écran
            startNewRound();
        } else {
            // Défaite
            new AlertDialog.Builder(this)
                    .setTitle("Faux !")
                    .setMessage("Mauvais drapeau.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private int getFlagResId(String isoCode) {
        // Supposons que tes images s'appellent "flag_fra", "flag_deu", etc.
        String resourceName = "flag_" + isoCode.toLowerCase();
        int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
        return (resId != 0) ? resId : R.drawable.ic_launcher_background;
    }
}