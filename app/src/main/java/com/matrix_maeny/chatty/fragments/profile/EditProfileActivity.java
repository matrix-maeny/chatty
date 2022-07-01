package com.matrix_maeny.chatty.fragments.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matrix_maeny.chatty.ExposeDialogs;
import com.matrix_maeny.chatty.R;
import com.matrix_maeny.chatty.UserModel;
import com.matrix_maeny.chatty.databinding.ActivityEditProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;


public class EditProfileActivity extends AppCompatActivity {


    private ActivityEditProfileBinding binding;

    private Uri imageUri = null;
    private String username, about,name;

    private ExposeDialogs dialogs;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.editProfileToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Profile");


        FirebaseApp.initializeApp(EditProfileActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());


        initialize();

    }



    @SuppressLint("SetTextI18n")
    private void initialize() {
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.peAboutEt.setText("No about provided");

        dialogs = new ExposeDialogs(EditProfileActivity.this);
        dialogs.setAllCancellable(false);


        binding.peEditProfileCv.setOnClickListener(peEditProfileCvListener);

        fetchUserData();
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();

                if (data != null) {
                    imageUri = data.getData(); // assigning the uri of selected image
                    binding.peUserIv.setImageURI(imageUri);
                } else {
                    imageUri = null; // else make it null
                }
                dialogs.dismissProgressDialog(); // dismissing the dialog

            });

    View.OnClickListener peEditProfileCvListener = v -> chooseProfilePic();


    private void fetchUserData() {
        dialogs.showProgressDialog("Loading...", "Fetching details...");
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
                            setUserData(model);
//                            fetchPosts();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setUserData(UserModel model) {
        try {
            Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.peUserIv);
        } catch (Exception e) {
            e.printStackTrace();
            binding.peUserIv.setImageResource(R.drawable.profile_pic);
        }

        binding.peNameEt.setText(model.getName());
        binding.peUsernameEt.setText(model.getUsername());
        if (!model.getAbout().equals("")) {
            binding.peAboutEt.setText(model.getAbout());
        } else {
            binding.peAboutEt.setText("No about provided");
        }

        dialogs.dismissProgressDialog();

//        binding.prNoOfPostsTv.setText(model.getPostCount() + " Posts");
    }

    private void saveProfileData() {
        if (checkAbout()) {
            dialogs.showProgressDialog("Saving", "wait few seconds");

            if (imageUri != null) {

                final StorageReference storageReference = storage.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()));

                storageReference.putFile(imageUri).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            if (task.isSuccessful()) {

                                database.getReference().child("Users")
                                        .child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                UserModel model = snapshot.getValue(UserModel.class);
                                                if (model != null) {
                                                    model.setName(name);
                                                    model.setUsername(username);
                                                    model.setAbout(about);
                                                    model.setProfilePicUrl(uri.toString());

                                                    database.getReference().child("Users")
                                                            .child(auth.getUid())
                                                            .setValue(model).addOnCompleteListener(task1 -> {

                                                                if (task1.isSuccessful()) {
                                                                    Toast.makeText(EditProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                                                                    finish();

                                                                } else
                                                                    Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                                dialogs.dismissProgressDialog();

                                                            }).addOnFailureListener(e -> {
                                                                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                dialogs.dismissProgressDialog();

                                                            });
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialogs.dismissProgressDialog();
                                            }
                                        });

                            } else {
                                Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                dialogs.dismissProgressDialog();
                            }

                        }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

                    } else {
                        Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                        dialogs.dismissProgressDialog();
                    }

                }).addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialogs.dismissProgressDialog();


                });
            } else {
                database.getReference().child("Users")
                        .child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserModel model = snapshot.getValue(UserModel.class);
                                if (model != null) {
                                    model.setUsername(username);
                                    model.setAbout(about);

                                    database.getReference().child("Users")
                                            .child(auth.getUid())
                                            .setValue(model).addOnCompleteListener(task1 -> {

                                                if (task1.isSuccessful()) {
                                                    Toast.makeText(EditProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                                                    dialogs.dismissProgressDialog();

                                                    finish();

                                                } else
                                                    Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                dialogs.dismissProgressDialog();


                                            }).addOnFailureListener(e -> {
                                                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                dialogs.dismissProgressDialog();

                                            });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(EditProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                dialogs.dismissProgressDialog();

                            }
                        });

            }
        }
    }


    public void chooseProfilePic() {

        dialogs.showProgressDialog("Getting Image", "Wait few seconds"); // showing a waiting dialog

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        launcher.launch(Intent.createChooser(intent, "Select a picture"));
    }


    private boolean checkUsername() {
        try {
            username = Objects.requireNonNull(binding.peUsernameEt.getText()).toString();
            if (!username.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
        return false;
    }
    private boolean checkName() {
        try {
            name = Objects.requireNonNull(binding.peNameEt.getText()).toString();
            if (!name.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkAbout() {
        try {
            about = Objects.requireNonNull(binding.peAboutEt.getText()).toString();
            if (!about.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter About", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        checkAndSaveProfile();
        return super.onOptionsItemSelected(item);
    }

    private void checkAndSaveProfile() {
        if(!checkUsername()) return;
        if(!checkName()) return;
        dialogs.showProgressDialog("Checking Username","wait...");
        final DatabaseReference userRef = database.getReference().child("Users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(isNameAvailable(snapshot)){
                        return;
                    }
                }

                saveProfileData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogs.dismissProgressDialog();
                dialogs.showToast(error.getMessage(),1);
            }
        });
    }

    private boolean isNameAvailable(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot s : snapshot.getChildren()) {
            UserModel model = s.getValue(UserModel.class);

            if (!Objects.equals(s.getKey(), Objects.requireNonNull(auth.getCurrentUser()).getUid()) && model != null && model.getUsername().equals(username)) {
               dialogs.dismissProgressDialog();
                dialogs.showToast("Username already taken", 1);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseApp.initializeApp(EditProfileActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }


}