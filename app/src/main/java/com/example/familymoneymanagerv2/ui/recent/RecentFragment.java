package com.example.familymoneymanagerv2.ui.recent; // Make sure this matches

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.familymoneymanagerv2.R;
import com.example.familymoneymanagerv2.Transaction;
// We need to create TransactionAdapter in this package, or move it
// Let's move it.
// 1. In your project, drag 'TransactionAdapter.java' from the 'home' package
//    to the 'recent' package. Click "Refactor" when it asks.

// This import should be correct after moving the file
import com.example.familymoneymanagerv2.ui.recent.TransactionAdapter;
import com.example.familymoneymanagerv2.databinding.FragmentRecentBinding; // This is the new binding
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RecentFragment extends Fragment {

    private FragmentRecentBinding binding; // Use the new binding class

    // --- Declare Firebase and Adapter variables ---
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private TransactionAdapter adapter;
    private RecyclerView recyclerView;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRecentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // --- Initialize Firebase ---
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        // Find the RecyclerView
        // We need to change the ID in fragment_recent.xml
        recyclerView = root.findViewById(R.id.rvTransactions); // Find it by ID

        // --- Setup the list ---
        setupRecyclerView();

        return root;
    }

    private void setupRecyclerView() {
        Query query = fStore.collection("transactions")
                .whereEqualTo("ownerId", currentUserId)
                .orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Transaction> options = new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build();

        adapter = new TransactionAdapter(options);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
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