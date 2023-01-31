package com.orbotz.filipino_english_interpreter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.orbotz.filipino_english_interpreter.Services.SharedPref;

import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {
    public static SharedPref prefs;
    TextView tvVersion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        Objects.requireNonNull(getSupportActionBar()).hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_splash_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        prefs = new SharedPref(this);
        tvVersion = findViewById(R.id.tv_Version);
        String version = "Version: "+BuildConfig.VERSION_NAME;
        tvVersion.setText(version);
        FirstBootCheckInternetConnection();
    }
    private void FirstBootCheckInternetConnection() {
        if (!isNetworkAvailable() && !prefs.loadFirstState()) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.logo)
                    .setTitle("Filipino - Ingles Tagasalin")
                    .setMessage("Mangyaring Suriin ang Iyong Koneksyon sa Internet")
                    .setCancelable(false)
                    .setPositiveButton("Subukan Muli!", (dialogInterface, i) -> FirstBootCheckInternetConnection())
                    .setNegativeButton("Isarado", (dialog, which) -> {
                        finishAffinity();
                        System.exit(0);
                    })
                    .show();
        } else {
            prefs.setFirstLaunch(true);
            new Handler().postDelayed(() -> {
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }, 1000);
        }
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {

                    return true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                    return true;
                } else return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }
        }
        return false;
    }
    @Override
    public void onBackPressed() {

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }
}