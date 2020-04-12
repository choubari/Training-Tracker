package com.choubapp.running;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CoachReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_reports);
    }

    public void BacktoDashboard(View v) {
        Intent intent = new Intent(CoachReportsActivity.this,CoachDashboardActivity.class);
        finish();
        startActivity(intent);
    }

}
