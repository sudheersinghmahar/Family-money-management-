package com.example.familymoneymanagerv2; // Make sure this matches your package

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private FloatingActionButton fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This line sets the correct layout file
        setContentView(R.layout.activity_categories);

        rvCategories = findViewById(R.id.rvCategories);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        fabAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: We will build AddCategoryActivity in the next step
                // Intent intent = new Intent(CategoriesActivity.this, AddCategoryActivity.class);
                // startActivity(intent);
                Toast.makeText(CategoriesActivity.this, "TODO: Open Add Category Screen", Toast.LENGTH_SHORT).show();
            }
        });

        // We will set up the RecyclerView list later
    }
}