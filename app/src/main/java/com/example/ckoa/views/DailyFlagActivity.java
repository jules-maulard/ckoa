package com.example.ckoa.views;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ckoa.R;
import com.example.ckoa.managers.FlagGameManager;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyFlagActivity extends AppCompatActivity {

    private FlagGameManager gameManager;
    private TextView textTargetCountry;
    private ImageButton[] flagButtons;
    private ExecutorService executor;
    private Handler handler;

    private boolean isRoundFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_flag);

        executor = Executors.newFixedThreadPool(1);
        handler = new Handler(Looper.getMainLooper());

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

        int loopLimit = flagButtons.length;

        for (int i = 0; i < loopLimit; i++) {
            String iso = options.get(i);
            ImageButton button = flagButtons[i];

            button.setTag(iso);
            button.setOnClickListener(this::onFlagClicked);

            loadFlagImage(iso, button);
        }
    }

    private void loadFlagImage(String iso, ImageButton button) {
        executor.execute(() -> {
            String flagUrl = gameManager.getFlagUrl(iso);

            handler.post(() -> {
                if (isDestroyed() || isFinishing()) return;

                Glide.with(this)
                        .load(flagUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .fitCenter()
                        .into(button);
            });
        });
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
                    .setPositiveButton("Finish", null)
                    .show();
        } else {
            view.setEnabled(false);
            view.setAlpha(0.5f);
            String countryName = gameManager.getCountryNameByIso(selectedIso);
            Toast.makeText(this, "Lost ! Wrong flag. Not " + countryName, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}