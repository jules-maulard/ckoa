package com.example.ckoa.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ckoa.R;
import com.example.ckoa.managers.GameHandler;

public class MainActivity extends AppCompatActivity {

    Button btnDaily, btnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDaily = findViewById(R.id.btnDaily);
        btnHistory = findViewById(R.id.btnHistory);

        btnDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DailyGameActivity.class);
                startActivity(intent);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO change DailyGameActivity to HistoryActivity
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                GameHandler gameHandler = new GameHandler(MainActivity.this);
                gameHandler.initData(MainActivity.this);
            }
        }).start();
    }
}
