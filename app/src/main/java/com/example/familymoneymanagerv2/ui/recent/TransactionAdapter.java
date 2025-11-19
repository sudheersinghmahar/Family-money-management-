package com.example.familymoneymanagerv2.ui.recent; // Make sure this matches

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.familymoneymanagerv2.R;
import com.example.familymoneymanagerv2.Transaction;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class TransactionAdapter extends FirestoreRecyclerAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    public TransactionAdapter(@NonNull FirestoreRecyclerOptions<Transaction> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TransactionViewHolder holder, int position, @NonNull Transaction model) {
        holder.tvTransactionCategory.setText(model.getCategory());
        holder.tvTransactionDate.setText(model.getDate());

        // Set amount and color
        double amount = model.getAmount();
        holder.tvTransactionAmount.setText(String.format("$%.2f", amount));

        if (amount < 0) {
            // It's an Expense
            holder.tvTransactionAmount.setTextColor(Color.RED);
        } else {
            // It's an Income
            holder.tvTransactionAmount.setTextColor(Color.parseColor("#006400")); // Dark Green
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionCategory;
        TextView tvTransactionDate;
        TextView tvTransactionAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
        }
    }
}