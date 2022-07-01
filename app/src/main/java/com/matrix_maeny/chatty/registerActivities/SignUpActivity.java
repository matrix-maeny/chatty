package com.matrix_maeny.chatty.registerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.chatty.ExposeDialogs;
import com.matrix_maeny.chatty.MainActivity;
import com.matrix_maeny.chatty.R;
import com.matrix_maeny.chatty.UserModel;
import com.matrix_maeny.chatty.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {


    private ActivitySignUpBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private ExposeDialogs exposeDialogs;

    private String username = null, email = null, password = null,name=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Objects.requireNonNull(getSupportActionBar()).hide(); // for hiding toolbar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // to create a translucent status of status bar

        FirebaseApp.initializeApp(SignUpActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        // a function to initialize everything
        initialize();
    }


    private void initialize() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();


        exposeDialogs = new ExposeDialogs(SignUpActivity.this);

        binding.suLoginTv.setOnClickListener(suLoginTvListener);
        binding.suSignUpBtn.setOnClickListener(suSignUpBtnListener);
    }


    View.OnClickListener suSignUpBtnListener = v -> signUp();
    View.OnClickListener suLoginTvListener = v -> {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    };


    private void signUp() {

        if (checkName() && checkUsername() && checkEmail() && checkPassword()) {

            exposeDialogs.showProgressDialog("Creating Account...", "Please wait few seconds");

            final DatabaseReference usernameRef = firebaseDatabase.getReference().child("Users");

            usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        if (isNameAvailable(snapshot)) {
                            exposeDialogs.dismissProgressDialog();
                            return;
                        }
                    }

                    registerUser();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    exposeDialogs.dismissProgressDialog();
                    exposeDialogs.showToast(error.getMessage(), 1);

                }
            });

        }
    }

    private void registerUser() {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        UserModel model = new UserModel(username, email, password,name);
                        String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();

                        firebaseDatabase.getReference().child("Users").child(uid).setValue(model).addOnCompleteListener(task1 -> {

                            exposeDialogs.dismissProgressDialog();
                            if (task1.isSuccessful()) {
                                LoginActivity.currentUserModel = model;
                                exposeDialogs.showToast("Account Created Successfully", 1);
                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                finish();
                            } else {
                                exposeDialogs.showToast("Error: " + Objects.requireNonNull(task1.getException()).getMessage(), 1);
                            }


                        }).addOnFailureListener(e -> {
                            exposeDialogs.dismissProgressDialog();
                            exposeDialogs.showToast(e.getMessage(), 1);
                        });
                    }

                })
                .addOnFailureListener(e -> {
                    exposeDialogs.showToast(e.getMessage(), 1);
                    exposeDialogs.dismissProgressDialog();

                });
    }

    private boolean isNameAvailable(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot s : snapshot.getChildren()) {
            UserModel model = s.getValue(UserModel.class);

            if (model != null && model.getUsername().equals(username)) {
                exposeDialogs.showToast("Username already taken", 1);
                return true;
            }
        }
        return false;
    }


    private boolean checkUsername() {
        try {
            username = Objects.requireNonNull(binding.suUserNameEt.getText()).toString();
            if (!username.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();

        return false;
    }
    private boolean checkName() {
        try {
            name = Objects.requireNonNull(binding.suNameEt.getText()).toString();
            if (!name.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();

        return false;
    }

    private boolean checkEmail() {
        try {
            email = Objects.requireNonNull(binding.suEmailEt.getText()).toString();
            if (!email.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkPassword() {
        try {
            password = Objects.requireNonNull(binding.suPasswordEt.getText()).toString();
            if (!password.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
        return false;
    }
}