package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/** Settings screen; preferences are stored in SharedPreferences so they survive restarts. */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final String KEY_ALMOST = "show_almost_there";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        SwitchCompat expiry = findViewById(R.id.switchExpiry);
        expiry.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        expiry.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, checked).apply());

        SwitchCompat almost = findViewById(R.id.switchAlmost);
        almost.setChecked(prefs.getBoolean(KEY_ALMOST, false));
        almost.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean(KEY_ALMOST, checked).apply());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
