package com.example.ckoa.views;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ckoa.R;
import com.example.ckoa.managers.GameStatsManager;

public class HistoryActivity extends AppCompatActivity {

    private GameStatsManager statsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        statsManager = new GameStatsManager(this);
        setupUI();
    }

    private void setupUI() {
        TextView tvStreak = findViewById(R.id.tvStreakValue);
        int currentStreak = statsManager.getCurrentStreak();
        tvStreak.setText(currentStreak + " 🔥");

        int count1 = statsManager.getWinsForAttemptsCount(1);
        int count2 = statsManager.getWinsForAttemptsCount(2);
        int count3 = statsManager.getWinsForAttemptsCount(3);
        int count4 = statsManager.getWinsForAttemptsCount(4);
        int count5 = statsManager.getWinsForAttemptsCount(5);
        int count6 = statsManager.getWinsForAttemptsCount(6);
        int countFail = statsManager.getTotalFailures();

        int maxCount = Math.max(1, Math.max(count1, Math.max(count2, Math.max(count3,
                Math.max(count4, Math.max(count5, Math.max(count6, countFail)))))));

        updateBar(R.id.viewBar1, R.id.tvCount1, count1, maxCount);
        updateBar(R.id.viewBar2, R.id.tvCount2, count2, maxCount);
        updateBar(R.id.viewBar3, R.id.tvCount3, count3, maxCount);
        updateBar(R.id.viewBar4, R.id.tvCount4, count4, maxCount);
        updateBar(R.id.viewBar5, R.id.tvCount5, count5, maxCount);
        updateBar(R.id.viewBar6, R.id.tvCount6, count6, maxCount);
        updateBar(R.id.viewBarFail, R.id.tvCountFail, countFail, maxCount);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateBar(int barId, int countId, int value, int maxValue) {
        View barView = findViewById(barId);
        TextView countView = findViewById(countId);

        countView.setText(String.valueOf(value));

        float maxBarHeightDp = 120f;
        float density = getResources().getDisplayMetrics().density;

        float calculatedHeightDp = 0f;
        if (maxValue > 0) {
            calculatedHeightDp = ((float) value / maxValue) * maxBarHeightDp;
        }

        float finalHeight = Math.max(10f, calculatedHeightDp);

        ViewGroup.LayoutParams params = barView.getLayoutParams();
        params.height = (int) (finalHeight * density);
        barView.setLayoutParams(params);
    }
}