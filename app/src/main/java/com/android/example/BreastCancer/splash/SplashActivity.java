//This the very first activity of the app.
package com.android.example.BreastCancer.splash;

import android.os.Bundle;

import com.android.example.BreastCancer.main.MainActivity;
import com.android.example.BreastCancer.R;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashTheme);
        startActivity(MainActivity.getIntent(this));
        finish();
    }
}
