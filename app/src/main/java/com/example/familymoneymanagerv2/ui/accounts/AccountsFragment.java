package com.example.familymoneymanagerv2.ui.accounts;

// --- NEW IMPORTS ---
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // New import

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.familymoneymanagerv2.Account;
import com.example.familymoneymanagerv2.AddAccountActivity;
import com.example.familymoneymanagerv2.EditAccountActivity; // New import
import com.example.familymoneymanagerv2.R;
import com.example.familymoneymanagerv2.databinding.FragmentAccountsBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// --- NEW IMPORTS ---
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot; // New import
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AccountsFragment extends Fragment {

    private FragmentAccountsBinding binding;

    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private AccountAdapter adapter;
    private RecyclerView recyclerView;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        recyclerView = binding.rvAccounts;
        FloatingActionButton fab = binding.fabAddAccount;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddAccountActivity.class);
                startActivity(intent);
            }
        });

        setupRecyclerView();

        return root;
    }

    private void setupRecyclerView() {
        Query query = fStore.collection("accounts")
                .whereEqualTo("ownerId", currentUserId)
                .orderBy("name", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Account> options = new FirestoreRecyclerOptions.Builder<Account>()
                .setQuery(query, Account.class)
                .build();

        adapter = new AccountAdapter(options);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // --- NEW: Set the click listener ---
        adapter.setOnItemClickListener(new AccountAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                // Get the ID of the clicked document
                String accountId = documentSnapshot.getId();
                // Get the Account object
                Account account = documentSnapshot.toObject(Account.class);

                showEditDeleteDialog(accountId, account);
            }
        });
    }

    // --- NEW: Method to show the Edit/Delete dialog ---
    private void showEditDeleteDialog(String accountId, Account account) {
        final String id = accountId; // make final for use in listener

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Options");
        String[] options = {"Edit Account", "Delete Account"};

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // "Edit" clicked
                    Intent intent = new Intent(getActivity(), EditAccountActivity.class);
                    // Pass the document ID to the edit activity
                    intent.putExtra("ACCOUNT_ID", id);
                    startActivity(intent);
                } else if (which == 1) {
                    // "Delete" clicked
                    // Show a second confirmation
                    showDeleteConfirmation(id, account.getCurrentBalance());
                }
            }
        });
        builder.show();
    }

    // --- NEW: Method for delete confirmation ---
    private void showDeleteConfirmation(String accountId, double balance) {
        // Prevent deleting an account with money in it (for safety)
        if (balance != 0) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Failed")
                    .setMessage("You cannot delete an account with a non-zero balance. Please adjust the balance first.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        final String id = accountId;
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account? This cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount(id);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- NEW: Method to delete from Firestore ---
    private void deleteAccount(String accountId) {
        fStore.collection("accounts").document(accountId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                        // The list will update automatically!
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}