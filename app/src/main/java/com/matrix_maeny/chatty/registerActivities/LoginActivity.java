package com.matrix_maeny.chatty.registerActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.chatty.ExposeDialogs;
import com.matrix_maeny.chatty.MainActivity;
import com.matrix_maeny.chatty.UserModel;
import com.matrix_maeny.chatty.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private FirebaseAuth firebaseAuth;
    private ExposeDialogs exposeDialogs;

    private String email = null, password = null;
    public static UserModel currentUserModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Objects.requireNonNull(getSupportActionBar()).hide(); // for hiding toolbar

        FirebaseApp.initializeApp(LoginActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        // a function to initialize everything

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            getCurrentUserData();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // to create a translucent status of status bar
        initialize();
    }


    private void initialize() {



        exposeDialogs = new ExposeDialogs(LoginActivity.this);


        binding.lgSignUpTv.setOnClickListener(lgSignUpTvListener);
        binding.lgLoginBtn.setOnClickListener(lgLoginBtnListener);
    }

    private void getCurrentUserData() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(firebaseAuth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            UserModel model = snapshot.getValue(UserModel.class);
                            if(model != null) currentUserModel = model;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    // for login btn listener
    View.OnClickListener lgSignUpTvListener = v -> {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
    };
    View.OnClickListener lgLoginBtnListener = v -> login();


    private void login() {
        if (checkEmail() && checkPassword()) {
            exposeDialogs.showProgressDialog("Logging in...","Please wait few seconds");

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                exposeDialogs.dismissProgressDialog();
                if (task.isSuccessful()) {
                    getCurrentUserData();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                exposeDialogs.dismissProgressDialog();

            });
        }

    }


    private boolean checkEmail() {
        try {
            email = Objects.requireNonNull(binding.lgEmailEt.getText()).toString();
            if (!email.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkPassword() {
        try {
            password = Objects.requireNonNull(binding.lgPasswordEt.getText()).toString();
            if (!password.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
        return false;
    }
}