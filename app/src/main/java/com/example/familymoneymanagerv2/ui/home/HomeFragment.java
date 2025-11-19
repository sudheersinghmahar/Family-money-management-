package com.example.familymoneymanagerv2.ui.home;

import android.content.Intent; // <-- Make sure this import is here
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Keep this

import com.example.familymoneymanagerv2.Account;
import com.example.familymoneymanagerv2.R; // <-- Make sure this import is here
import com.example.familymoneymanagerv2.Transaction;
import com.example.familymoneymanagerv2.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // --- Declare Firebase ---
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private String currentUserId;

    // --- Declare Views ---
    private TextView tvMonthYear, tvMonthlyIncome, tvMonthlyExpense, tvMonthlySaved, tvOverallBalance;
    private ProgressBar progressSaved;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // This line is for the "HomeViewModel", which we're not really using
        // but it's part of the template. It's fine to leave it.
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // --- Initialize Firebase ---
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        // --- Find Views from Layout (using binding) ---
        tvMonthYear = binding.tvMonthYear;
        tvMonthlyIncome = binding.tvMonthlyIncome;
        tvMonthlyExpense = binding.tvMonthlyExpense;
        tvMonthlySaved = binding.tvMonthlySaved;
        tvOverallBalance = binding.tvOverallBalance;
        progressSaved = binding.progressSaved;

        // --- THIS IS THE CORRECTED CODE FOR THE BUTTON ---
        // We find it manually using 'root.findViewById' to bypass the binding error
        android.widget.Button btnManageCategories = root.findViewById(R.id.btnManageCategories);

        // Set the click listener
        btnManageCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open CategoriesActivity
                Intent intent = new Intent(getActivity(), com.example.familymoneymanagerv2.CategoriesActivity.class);
                startActivity(intent);
            }
        });
        // --- END OF FIX ---

        return root;
    }

    // --- Load data every time the fragment is shown ---
    @Override
    public void onResume() {
        super.onResume();
        loadMonthlySummary();
        loadOverallBalance();
    }

    // This function calculates the monthly summary
    private void loadMonthlySummary() {
        // Set current month title
        Calendar cal = Calendar.getInstance();
        String monthYear = new SimpleDateFormat("MMMM yyyY", Locale.getDefault()).format(cal.getTime());
        tvMonthYear.setText(monthYear);

        final int currentMonth = cal.get(Calendar.MONTH) + 1;
        final int currentYear = cal.get(Calendar.YEAR);

        fStore.collection("transactions")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double totalIncome = 0;
                            double totalExpense = 0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Transaction tx = document.toObject(Transaction.class);

                                // Check if transaction is in the current month
                                if (isTransactionInCurrentMonth(tx.getDate(), currentMonth, currentYear)) {
                                    if (tx.getType().equals("Income")) {
                                        totalIncome += tx.getAmount();
                                    } else {
                                        totalExpense += tx.getAmount(); // Amount is already negative
                                    }
                                }
                            }

                            double saved = totalIncome + totalExpense; // (expense is negative)

                            // --- Update the UI ---
                            tvMonthlyIncome.setText(String.format("$%.2f", totalIncome));
                            tvMonthlyExpense.setText(String.format("$%.2f", totalExpense));
                            tvMonthlySaved.setText(String.format("$%.0f", saved));

                            // Update progress bar
                            if (totalIncome > 0) {
                                int progress = (int) ((saved / totalIncome) * 100);
                                progressSaved.setProgress(progress);
                            } else {
                                progressSaved.setProgress(0);
                            }
                        } else {
                            Log.w("HomeFragment", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    // --- NEW FUNCTION TO CALCULATE TOTAL BALANCE ---
    private void loadOverallBalance() {
        fStore.collection("accounts")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double totalBalance = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Account account = document.toObject(Account.class);
                                totalBalance += account.getCurrentBalance();
                            }
                            // --- Update the UI ---
                            tvOverallBalance.setText(String.format("$%.2f", totalBalance));
                        } else {
                            Log.w("HomeFragment", "Error getting accounts.", task.getException());
                        }
                    }
                });
    }

    private boolean isTransactionInCurrentMonth(String date, int currentMonth, int currentYear) {
        if (date == null || date.isEmpty()) return false;

        try {
            // Our date format is "d/M/yyyy" (e.g., 5/11/2025)
            String[] parts = date.split("/");
            // int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            return (month == currentMonth && year == currentYear);

        } catch (Exception e) {
            Log.e("HomeFragment", "Error parsing date: " + date, e);
            return false;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}