package com.example.livestreamapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class StartingScreen1 extends AppCompatActivity {

    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_URLS = "urls";

    private ArrayAdapter<String> spinnerAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting_screen1);

        //Init Editext
        EditText editText = findViewById(R.id.user_url);


        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        //Init Spinner
        Spinner spinner = findViewById(R.id.history_list);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        // Load stored URLs from SharedPreferences
        loadUrls();

        //Select pre-url function
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                editText.setText(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });


        //Init submit button
        Button submit_button = findViewById(R.id.submit_button);

        //Set up onclick function
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Take the url
                EditText user_url = (EditText) findViewById(R.id.user_url);
                String url = user_url.getText().toString();

                //Store user url into recent url list
                spinnerAdapter.add(url);
                spinnerAdapter.notifyDataSetChanged();

                // Save the URLs to SharedPreferences
                saveUrls();

                //Submit and passing user url into URL_LIVE_STREAM

                Intent intent = new Intent(StartingScreen1.this, MainActivity.class);
                intent.putExtra("URL_LIVE_STREAM", url);
                startActivity(intent);
            }
        });
    }

    private void loadUrls() {
        String urls = sharedPreferences.getString(KEY_URLS, "");

        if (!urls.isEmpty()) {
            String[] urlArray = urls.split(",");

            for (String url : urlArray) {
                spinnerAdapter.add(url);
            }
        }
    }

    private void saveUrls() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < spinnerAdapter.getCount(); i++) {
            String url = spinnerAdapter.getItem(i);
            sb.append(url);

            if (i < spinnerAdapter.getCount() - 1) {
                sb.append(",");
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_URLS, sb.toString());
        editor.apply();
    }
}