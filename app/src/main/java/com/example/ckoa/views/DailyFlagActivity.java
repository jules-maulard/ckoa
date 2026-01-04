package com.example.ckoa.views;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ckoa.R;
import com.example.ckoa.managers.FlagGameManager;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class DailyFlagActivity extends AppCompatActivity {

    private FlagGameManager gameManager;
    private TextView textTargetCountry;
    private ImageButton[] flagButtons;

    private boolean isRoundFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_flag);

        gameManager = new FlagGameManager(this);

        initViews();
        setupDailyRound();
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

    private void setupDailyRound() {
        FlagGameManager.FlagRoundData data = gameManager.prepareDailyRound();

        if (data == null) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textTargetCountry.setText(data.targetName);
        List<String> options = data.optionsIso;

        int loopLimit = Math.min(options.size(), flagButtons.length);

        for (int i = 0; i < loopLimit; i++) {
            String iso = options.get(i);
            ImageButton button = flagButtons[i];

            button.setTag(iso);
            button.setOnClickListener(this::onFlagClicked);

            loadFlagImage(iso, button);
        }
    }

    private void loadFlagImage(String iso, ImageButton button) {
        new Thread(() -> {
            try {
                // 1. On récupère l'URL (peut prendre du temps si appel API)
                String flagUrl = gameManager.getFlagUrl(iso);

                // Si l'URL est nulle (API erreur), on s'arrête là
                if (flagUrl == null || flagUrl.isEmpty()) {
                    runOnUiThread(() -> button.setImageResource(R.drawable.ic_launcher_background));
                    return;
                }

                // 2. Connexion HTTP robuste pour éviter les images "vides"
                java.net.URL url = new java.net.URL(flagUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                // 3. On récupère le flux et on le transforme en Bitmap
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                // 4. Affichage
                if (bitmap != null) {
                    runOnUiThread(() -> button.setImageBitmap(bitmap));
                } else {
                    // Si le décodage a échoué malgré tout
                    runOnUiThread(() -> button.setImageResource(R.drawable.ic_launcher_background));
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> button.setImageResource(R.drawable.ic_launcher_background));
            }
        }).start();
    }

    private void onFlagClicked(View view) {
        if (isRoundFinished) return;

        String selectedIso = (String) view.getTag();
        boolean isCorrect = gameManager.checkGuess(selectedIso);

        if (isCorrect) {
            isRoundFinished = true;

            new AlertDialog.Builder(this)
                    .setTitle("Bravo !")
                    .setMessage("Correct answer!")
                    .setCancelable(false)
                    .setPositiveButton("Finish", (dialog, which) -> finish())
                    .show();
        } else {
            view.setEnabled(false);
            view.setAlpha(0.5f);
            new AlertDialog.Builder(this)
                    .setTitle("Lost !")
                    .setMessage("Wrong flag. Not " + selectedIso)
                    .setCancelable(false)
                    .setPositiveButton("Quit", null)
                    .show();
        }
    }
}