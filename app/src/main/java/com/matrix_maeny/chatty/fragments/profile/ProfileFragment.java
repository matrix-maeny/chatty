package com.matrix_maeny.chatty.fragments.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.chatty.ExposeDialogs;
import com.matrix_maeny.chatty.R;
import com.matrix_maeny.chatty.UserModel;
import com.matrix_maeny.chatty.databinding.FragmentProfileBinding;
import com.squareup.picasso.Picasso;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private ProfileFragmentListener listener;

    private FirebaseDatabase firebaseDatabase;
    private String currentUserUid;

    private ExposeDialogs exposeDialogs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        listener = (ProfileFragmentListener) requireContext();
        listener.hideToolbar(true);

        initialize();

        binding.prEditProfileCv.setOnClickListener(v -> {
//            exposeDialogs.showToast("Clicked",1);
            requireContext().startActivity(new Intent(requireContext().getApplicationContext(), EditProfileActivity.class));
        });

        binding.prLogoutCv.setOnClickListener(v -> {
            listener.logOut();
        });


        return binding.getRoot();
    }

    private void initialize() {
        currentUserUid = FirebaseAuth.getInstance().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();

        exposeDialogs = new ExposeDialogs(requireContext());

        fetchProfileData();
    }

    private void fetchProfileData() {
        exposeDialogs.showProgressDialog("Fetching data", "wait...");
        final DatabaseReference profileRef = firebaseDatabase.getReference().child("Users").child(currentUserUid);

        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    UserModel userModel = snapshot.getValue(UserModel.class);
                    if (userModel != null) {
                        setProfileData(userModel);
                    }
                }

                exposeDialogs.dismissProgressDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 1);
            }
        });
    }

    private void setProfileData(UserModel userModel) {


        try {
            Picasso.get().load(userModel.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.peUserIv);
        } catch (Exception e) {
            e.printStackTrace();
            binding.peUserIv.setImageResource(R.drawable.profile_pic);
        }

        binding.prNameTv.setText(userModel.getName());
        binding.prUsernameTv.setText(userModel.getUsername());
        binding.prAboutTv.setText(userModel.getAbout());

    }


    public interface ProfileFragmentListener {
        void hideToolbar(boolean shouldHide);
        void logOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchProfileData();
    }
}