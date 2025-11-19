package com.example.familymoneymanagerv2; // Make sure this matches your package

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Import Firebase
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private Button btnExpense, btnIncome, btnSave;
    private Spinner spinnerAccount, spinnerCategory;
    private EditText etAmount, etDate;

    // Firebase
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private String currentUserId;

    // Data
    private String transactionType = "Expense"; // Default to Expense
    private ArrayList<String> accountNames = new ArrayList<>();
    private ArrayList<Account> accountList = new ArrayList<>(); // To store full account objects
    private Account selectedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction); // This is the correct layout

        // Initialize Firebase
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser == null) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = fUser.getUid();

        // Find Views
        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);
        btnSave = findViewById(R.id.btnSaveTransaction);
        spinnerAccount = findViewById(R.id.spinnerAccount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate); // <-- This is the corrected ID

        setupToggleButtons();
        setupCategorySpinner();
        setupDatePicker();
        loadUserAccounts();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });
    }

    private void setupToggleButtons() {
        // Set default state
        btnExpense.setBackgroundColor(Color.GRAY);
        btnIncome.setBackgroundColor(Color.LTGRAY);

        btnExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionType = "Expense";
                btnExpense.setBackgroundColor(Color.GRAY);
                btnIncome.setBackgroundColor(Color.LTGRAY);
            }
        });

        btnIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionType = "Income";
                btnIncome.setBackgroundColor(Color.GRAY);
                btnExpense.setBackgroundColor(Color.LTGRAY);
            }
        });
    }

    private void setupCategorySpinner() {
        // We'll use a simple list for now. Later, we can load this from Firestore.
        String[] categories = {"Food", "Transport", "Salary", "Groceries", "Entertainment", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddTransactionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
    }

    private void loadUserAccounts() {
        fStore.collection("accounts")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            accountNames.clear();
                            accountList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Save the full Account object and its ID
                                Account account = document.toObject(Account.class);
                                account.setAccountId(document.getId()); // <-- This relies on your Account.java fix
                                accountList.add(account);

                                // Add just the name to the spinner
                                accountNames.add(account.getName());
                            }
                            // Setup the spinner with the names
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTransactionActivity.this,
                                    android.R.layout.simple_spinner_item, accountNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerAccount.setAdapter(adapter);
                        } else {
                            Toast.makeText(AddTransactionActivity.this, "Error loading accounts.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String date = etDate.getText().toString().trim();
        int accountIndex = spinnerAccount.getSelectedItemPosition();

        // --- Validation ---
        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(date) || accountList.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an account", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        // Store expenses as negative, income as positive
        if (transactionType.equals("Expense")) {
            amount = -Math.abs(amount);
        } else {
            amount = Math.abs(amount);
        }

        // Get the selected account's document ID
        selectedAccount = accountList.get(accountIndex);
        String accountId = selectedAccount.getAccountId(); // This ID is from the Firestore document

        // --- 1. Create the Transaction Object ---
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("type", transactionType);
        transaction.put("amount", amount);
        transaction.put("category", category);
        transaction.put("date", date);
        transaction.put("ownerId", currentUserId);
        transaction.put("accountId", accountId); // Link transaction to the account

        // --- 2. Create final copies of the variables for the listener ---
        final double finalAmount = amount;
        final String finalAccountId = accountId;

        // --- 3. Save new transaction to "transactions" collection ---
        fStore.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // --- 4. Update the Account's Balance (using the final variables) ---
                        updateAccountBalance(finalAccountId, finalAmount);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddTransactionActivity.this, "Error saving transaction.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAccountBalance(String accountId, double transactionAmount) {
        DocumentReference accountRef = fStore.collection("accounts").document(accountId);

        // Calculate new balance
        double newBalance = selectedAccount.getCurrentBalance() + transactionAmount;

        // Update just the one field
        accountRef.update("currentBalance", newBalance)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddTransactionActivity.this, "Transaction Saved", Toast.LENGTH_SHORT).show();
                        finish(); // All done, close the activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddTransactionActivity.this, "Error updating balance.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}