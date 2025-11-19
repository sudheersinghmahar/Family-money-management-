package com.example.familymoneymanagerv2; // Make sure this matches your package name

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Firebase Auth
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Firebase Firestore (Database)
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etFamilyName, etFullName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    // Declare Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        etFamilyName = findViewById(R.id.etRegisterFamilyName);
        etFullName = findViewById(R.id.etRegisterFullName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to Login screen
                finish();
            }
        });
    }

    private void registerUser() {
        String familyName = etFamilyName.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // --- Data Validation ---
        if (TextUtils.isEmpty(familyName)) {
            etFamilyName.setError("Family Name is required.");
            return;
        }
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full Name is required.");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required.");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters.");
            return;
        }

        // --- 1. Create User in Firebase Authentication ---
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "createUserWithEmail:success");
                    FirebaseUser fUser = mAuth.getCurrentUser();
                    String userId = fUser.getUid();

                    // --- 2. Save User Details to Firestore Database ---
                    // Create a "users" collection and a new document for this user
                    DocumentReference userRef = fStore.collection("users").document(userId);

                    // Create a data object
                    Map<String, Object> user = new HashMap<>();
                    user.put("familyName", familyName);
                    user.put("fullName", fullName);
                    user.put("email", email);

                    // Save the data to the cloud
                    userRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: User Profile created for " + userId);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + e.toString());
                        }
                    });

                    // --- 3. Go to the Main App Screen ---
                    goToMainActivity();

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        // Clear the back stack so the user can't press "back" to the login/register screens
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close RegisterActivity
    }
}