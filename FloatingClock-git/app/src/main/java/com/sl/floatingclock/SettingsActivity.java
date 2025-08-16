package com.sl.floatingclock;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private SeekBar transparencySeekBar;
    private SeekBar textSizeSeekBar;
    /*private EditText widthEditText, heightEditText;*/
    private Button saveButton;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        transparencySeekBar = findViewById(R.id.seekbar_transparency);
        textSizeSeekBar = findViewById(R.id.seekbar_text_size);
        /*widthEditText = findViewById(R.id.edit_width);
        heightEditText = findViewById(R.id.edit_height); */
        saveButton = findViewById(R.id.btn_save);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        float transparency = prefs.getFloat("transparency", 0.7f);
        /*int windowWidth = prefs.getInt("window_width", 650);
        int windowHeight = prefs.getInt("window_height", 240);*/

        transparencySeekBar.setProgress((int) (transparency * 100));
        /*widthEditText.setText(String.valueOf(windowWidth));
        heightEditText.setText(String.valueOf(windowHeight));*/

        transparencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putFloat("transparency", progress / 100f).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        int textSize = prefs.getInt("text_size", 48);

        int max_text_size =  96;
        textSizeSeekBar.setProgress((int) (100f * textSize / max_text_size ));
        /*widthEditText.setText(String.valueOf(windowWidth));
        heightEditText.setText(String.valueOf(windowHeight));*/

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("text_size", (int) (max_text_size * progress / 100f)).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        /*widthEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int w = Integer.parseInt(s.toString());
                    editor.putInt("window_width", w).apply();
                } catch (NumberFormatException ignored) {}
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        heightEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int h = Integer.parseInt(s.toString());
                    editor.putInt("window_height", h).apply();
                } catch (NumberFormatException ignored) {}
            }
            @Override public void afterTextChanged(Editable s) {}
        });*/

        saveButton.setOnClickListener(v -> finish());
    }
}