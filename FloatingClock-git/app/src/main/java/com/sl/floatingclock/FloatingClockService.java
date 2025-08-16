package com.sl.floatingclock;


import android.app.Activity;
import android.content.Context;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.DisplayMetrics;
import android.view.WindowManager;


public class FloatingClockService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private Handler handler;
    private Runnable updateClockRunnable;

    private int lastAction;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private boolean isDragging = false;
    private static final int DRAG_THRESHOLD = 10;
    /*private float transparency;
    private int windowWidth;
    private int windowHeight;*/
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private WindowManager.LayoutParams params;

    /*private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        transparency = prefs.getFloat("transparency", 0.85f);
        windowWidth = prefs.getInt("window_width", 700);
        windowHeight = prefs.getInt("window_height", 300);
    }*/


    private void setupPrefListener() {
        prefListener = (sharedPreferences, key) -> {
            if (key.equals("transparency") || key.equals("text_size") ) {
                updateWindowParams();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
    }

    private void updateWindowParams() {
        if (floatingView != null && params != null) {
            float transparency = prefs.getFloat("transparency", 0.7f);
            int textSize = prefs.getInt("text_size", 48);

            floatingView.setAlpha(transparency);
            TextView clock = floatingView.findViewById(R.id.text_time);
            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

            windowManager.updateViewLayout(floatingView, params);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setupPrefListener();

        /*int windowWidth = prefs.getInt("window_width", 650);
        int windowHeight = prefs.getInt("window_height", 240);*/
        float transparency = prefs.getFloat("transparency", 0.7f);
        int textSize = prefs.getInt("text_size", 48);

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_clock, null);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                /*windowWidth,
                windowHeight,*/
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;//Gravity.CENTER_HORIZONTAL;
        params.x = 0;
        params.y = 0;

        floatingView.setAlpha(transparency); // Translucency, eg: 0.85f

        windowManager.addView(floatingView, params);

        handler = new Handler();
        updateClockRunnable = new Runnable() {
            @Override
            public void run() {
                TextView clock = floatingView.findViewById(R.id.text_time);
                //String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                clock.setText(currentTime);

                int textSize = prefs.getInt("text_size", 48);
                clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateClockRunnable);

        // Setting Button
        ImageButton settingBtn = floatingView.findViewById(R.id.btn_setting);
        settingBtn.setOnClickListener(v -> {
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settingIntent);
        });

        // Close button
        ImageView closeBtn = floatingView.findViewById(R.id.img_close);
        closeBtn.setOnClickListener(v -> stopSelf());

        // Drag
        View dragArea = floatingView.findViewById(R.id.floating_root);
        dragArea.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    lastAction = event.getAction();

                    isDragging = false;
                    return true;
                case MotionEvent.ACTION_UP:
                    lastAction = event.getAction();
                    if(!isDragging) {
                        //v.performClick();
                        if (closeBtn.getVisibility() == View.GONE) {
                            closeBtn.setVisibility(View.VISIBLE);
                        } else {
                            closeBtn.setVisibility(View.GONE);
                        }

                        if (settingBtn.getVisibility() == View.GONE) {
                            settingBtn.setVisibility(View.VISIBLE);
                        } else {
                            settingBtn.setVisibility(View.GONE);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int)(event.getRawX() - initialTouchX);
                    int deltaY = (int)(event.getRawY() - initialTouchY);

                    // Inside an Activity or Context
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    /*WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager != null) {
                        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                    }*/

                    windowManager.getDefaultDisplay().getMetrics(displayMetrics);

                    int screenHeightPixels = displayMetrics.heightPixels;
                    int screenWidthPixels = displayMetrics.widthPixels;

                    params.x = initialX + deltaX;
                    params.x = Math.max(0, params.x); // Constrain to the left edge (0)
                    params.x = Math.min(screenWidthPixels - v.getWidth(), params.x);

                    params.y = initialY + deltaY;
                    params.y = Math.max(0, params.y);
                    params.y = Math.min(screenHeightPixels - v.getHeight(), params.y);

                    windowManager.updateViewLayout(floatingView, params);
                    lastAction = event.getAction();

                    if (Math.abs(deltaX) > DRAG_THRESHOLD || Math.abs(deltaY) > DRAG_THRESHOLD) {
                        isDragging = true;
                    }
                    return true;
            }
            return false;
        });

        /*// Start rabbit animation
        ImageView rabbitAnim = floatingView.findViewById(R.id.img_rabbit);
        rabbitAnim.setBackgroundResource(R.drawable.rabbit_anim);
        android.graphics.drawable.AnimationDrawable frameAnimation =
                (android.graphics.drawable.AnimationDrawable) rabbitAnim.getBackground();
        frameAnimation.start();*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
        if (handler != null) handler.removeCallbacks(updateClockRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}