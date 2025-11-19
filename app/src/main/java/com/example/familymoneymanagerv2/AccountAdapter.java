package com.example.familymoneymanagerv2.ui.accounts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.familymoneymanagerv2.Account;
import com.example.familymoneymanagerv2.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
// --- NEW IMPORTS ---
import com.google.firebase.firestore.DocumentSnapshot;

public class AccountAdapter extends FirestoreRecyclerAdapter<Account, AccountAdapter.AccountViewHolder> {

    // --- NEW: Click Listener Interface ---
    private OnItemClickListener listener;

    public AccountAdapter(@NonNull FirestoreRecyclerOptions<Account> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AccountViewHolder holder, int position, @NonNull Account model) {
        holder.tvAccountName.setText(model.getName());
        holder.tvAccountType.setText(model.getType());
        holder.tvAccountBalance.setText(String.format("$%.2f", model.getCurrentBalance()));
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_account, parent, false);
        return new AccountViewHolder(view);
    }

    // This class holds the views for each row
    class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountName;
        TextView tvAccountType;
        TextView tvAccountBalance;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            tvAccountType = itemView.findViewById(R.id.tvAccountType);
            tvAccountBalance = itemView.findViewById(R.id.tvAccountBalance);

            // --- NEW: Set click listener for the whole row ---
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    // Make sure position is valid and a listener exists
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        // Get the document snapshot for the clicked item
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    // --- NEW: Interface for click events ---
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    // --- NEW: Method to set the listener from our Fragment ---
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}