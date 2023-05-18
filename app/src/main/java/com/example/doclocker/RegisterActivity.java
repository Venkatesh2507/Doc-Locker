package com.example.doclocker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.doclocker.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    String email;
    String password;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String userName;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{4,}" +                // at least 4 characters
                    "$");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = binding.userNameEt.getEditText().getText().toString().trim();
                email = binding.emailEt.getEditText().getText().toString().trim();
                password = binding.passwordEt.getEditText().getText().toString().trim();
                validateEmail(email,password);
                registerUser(email,password);
            }
        });


        binding.signInTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });



    }

    private void registerUser(String email, String password) {
        binding.progressBar.setVisibility(View.VISIBLE);
        UserDetails userDetails = new UserDetails(userName,email,password);
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("User Details").child(userName).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                                    "Registration successful!",
                                    Toast.LENGTH_LONG)
                            .show();

                    // hide the progress bar
                    binding.progressBar.setVisibility(View.GONE);

                    // if the user created intent to login activity
                    Intent intent
                            = new Intent(RegisterActivity.this,
                            LoginActivity.class);
                    startActivity(intent);
                }
                else {

                    Toast.makeText(
                                    getApplicationContext(),
                                    "Registration failed!!"
                                            + " Please try again later",
                                    Toast.LENGTH_LONG)
                            .show();
                    Log.d("Errorrr", ""+task.getException().toString());

                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void validateEmail(String email, String password) {
        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email Verified !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Enter valid Email address !", Toast.LENGTH_SHORT).show();
        }
        if (password.isEmpty()) {
            binding.passwordEt.setError("Field can not be empty");
        }

        else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            binding.passwordEt.setError("Password is too weak");
        } else {
            binding.passwordEt.setError(null);
        }
    }

}