package com.matrix_maeny.chatty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.matrix_maeny.chatty.databinding.ActivityAboutBinding;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.abToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("About");
    }
}