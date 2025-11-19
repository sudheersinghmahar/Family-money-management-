package com.example.familymoneymanagerv2; // Make sure this matches

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

// --- NEW IMPORTS ---
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditAccountActivity extends AppCompatActivity {

    private EditText etAccountName, etStartingBalance;
    private Spinner spinnerAccountType;
    private Button btnUpdateAccount;

    private FirebaseFirestore fStore;
    private DocumentReference accountRef;
    private String accountId;

    private String[] accountTypes = {"Cash", "Bank Account", "Credit Card", "Savings", "Wallet"};
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        // Get the Account ID from the Intent
        accountId = getIntent().getStringExtra("ACCOUNT_ID");
        if (accountId == null || accountId.isEmpty()) {
            Toast.makeText(this, "Error: Account ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fStore = FirebaseFirestore.getInstance();
        accountRef = fStore.collection("accounts").document(accountId);

        etAccountName = findViewById(R.id.etAccountName);
        etStartingBalance = findViewById(R.id.etStartingBalance);
        spinnerAccountType = findViewById(R.id.spinnerAccountType);
        btnUpdateAccount = findViewById(R.id.btnUpdateAccount);

        // Setup Spinner
        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, accountTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccountType.setAdapter(spinnerAdapter);

        // Load the account's current data
        loadAccountData();

        btnUpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccount();
            }
        });
    }

    private void loadAccountData() {
        accountRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Account account = documentSnapshot.toObject(Account.class);
                    etAccountName.setText(account.getName());
                    etStartingBalance.setText(String.format("%.2f", account.getCurrentBalance()));

                    // Set spinner to correct type
                    int spinnerPosition = spinnerAdapter.getPosition(account.getType());
                    spinnerAccountType.setSelection(spinnerPosition);
                } else {
                    Toast.makeText(EditAccountActivity.this, "Account not found.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAccount() {
        String accountName = etAccountName.getText().toString().trim();
        String accountType = spinnerAccountType.getSelectedItem().toString();

        if (TextUtils.isEmpty(accountName)) {
            etAccountName.setError("Name is required.");
            return;
        }

        // Update the fields in Firestore
        accountRef.update("name", accountName, "type", accountType)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditAccountActivity.this, "Account Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditAccountActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}