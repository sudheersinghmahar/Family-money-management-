package com.example.familymoneymanagerv2; // Make sure this matches your package name

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Import Firebase Auth and Firestore
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddAccountActivity extends AppCompatActivity {

    private EditText etAccountName, etStartingBalance;
    private Spinner spinnerAccountType;
    private Button btnSaveAccount;

    // Declare Firebase
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account); // This line replaces all the template code

        etAccountName = findViewById(R.id.etAccountName);
        etStartingBalance = findViewById(R.id.etStartingBalance);
        spinnerAccountType = findViewById(R.id.spinnerAccountType);
        btnSaveAccount = findViewById(R.id.btnSaveAccount);

        // Initialize Firebase
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) {
            // User is not logged in, send back to login
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = fUser.getUid();

        // --- Setup the Account Type Spinner (Dropdown) ---
        String[] accountTypes = {"Cash", "Bank Account", "Credit Card", "Savings", "Wallet"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, accountTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccountType.setAdapter(adapter);

        // --- Setup the Save Button ---
        btnSaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountToFirestore();
            }
        });
    }

    private void saveAccountToFirestore() {
        String accountName = etAccountName.getText().toString().trim();
        String accountType = spinnerAccountType.getSelectedItem().toString();
        String balanceStr = etStartingBalance.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(accountName)) {
            etAccountName.setError("Account Name is required.");
            return;
        }
        if (TextUtils.isEmpty(balanceStr)) {
            etStartingBalance.setError("Starting Balance is required.");
            return;
        }

        double startingBalance = Double.parseDouble(balanceStr);

        // --- Save to Firestore Database ---
        // Create a new "account" object
        Map<String, Object> account = new HashMap<>();
        account.put("name", accountName);
        account.put("type", accountType);
        account.put("currentBalance", startingBalance);
        account.put("ownerId", currentUserId); // Link this account to the user

        // Create a new document in the "accounts" collection
        fStore.collection("accounts")
                .add(account)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddAccountActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                        finish(); // Close this activity and go back
                    }
                })
                .addOnFailureListener(new OnFailureListener() { // <-- TYPO FIXED
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddAccountActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}