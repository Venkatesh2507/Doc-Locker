package com.example.doclocker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.doclocker.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
   ActivitySplashBinding binding;
   FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                user = FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null){
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
                    finish();
                }
            }
        },3000);
    }
}