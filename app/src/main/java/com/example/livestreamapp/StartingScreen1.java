package com.example.livestreamapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class StartingScreen1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_screen1);
        //Submit button init
        Button submit_button = findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText user_url = (EditText) findViewById(R.id.user_url);
                String url = user_url.getText().toString();
                Intent intent = new Intent(StartingScreen1.this, MainActivity.class);
                intent.putExtra("URL_LIVE_STREAM", url);

                startActivity(intent);
            }
        });
    }
}