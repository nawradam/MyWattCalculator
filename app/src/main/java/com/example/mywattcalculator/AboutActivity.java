package com.example.mywattcalculator;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    TextView textGithubUrl;
    Button buttonBackAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle("About");

        textGithubUrl = findViewById(R.id.textGithubUrl);
        buttonBackAbout = findViewById(R.id.buttonBackAbout);

        textGithubUrl.setMovementMethod(LinkMovementMethod.getInstance());

        buttonBackAbout.setOnClickListener(v -> finish());
    }
}