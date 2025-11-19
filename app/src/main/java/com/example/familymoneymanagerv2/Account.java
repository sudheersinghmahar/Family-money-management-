package com.example.familymoneymanagerv2; // Make sure this matches your package

// This is a "POJO" (Plain Old Java Object)
// Firestore uses this to automatically turn data from the cloud into a Java object
import com.google.firebase.firestore.Exclude;
public class Account {
    // Variable names MUST match the keys in your Firestore database
    private String name;
    private String type;
    private double currentBalance;
    // We can add ownerId if needed, but it's not required for this list

    // --- IMPORTANT ---
    // Firestore needs an empty constructor to work
    @Exclude
    private String accountId;
    public Account() {
    }
    // ---

    public Account(String name, String type, double currentBalance) {
        this.name = name;
        this.type = type;
        this.currentBalance = currentBalance;
    }
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }


    // --- Getters and Setters ---
    // Firestore also needs these to read and write data
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }
}