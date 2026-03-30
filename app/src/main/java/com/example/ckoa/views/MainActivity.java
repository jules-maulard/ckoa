package com.example.ckoa.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ckoa.R;
import com.example.ckoa.data.GameRepository;
import com.example.ckoa.data.ProgressRepository;
import com.example.ckoa.managers.ShapeGameManager;
import com.example.ckoa.models.DailyStep;

public class MainActivity extends AppCompatActivity {

    private Button dailyButton;
    private Button historyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        dailyButton.setOnClickListener(view -> {
            ProgressRepository progressRepository = new ProgressRepository(this);
            DailyStep currentStep = progressRepository.getCurrentStep();
            navigateToStep(currentStep);
        });

        historyButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });

        new Thread(() -> {
            GameRepository repository = new GameRepository(this);
            repository.prepareDatabase();
        }).start();
    }

    private void initializeViews() {
        dailyButton = findViewById(R.id.btnDaily);
        historyButton = findViewById(R.id.btnHistory);
    }

    private void navigateToStep(DailyStep step) {
        Intent intent;
        if (step == DailyStep.FLAG) {
            intent = new Intent(this, DailyFlagActivity.class);
        } else if (step == DailyStep.DONE) {
            intent = new Intent(this, DailyShapeActivity.class);
        } else {
            intent = new Intent(this, DailyShapeActivity.class);
        }
        startActivity(intent);
    }
}