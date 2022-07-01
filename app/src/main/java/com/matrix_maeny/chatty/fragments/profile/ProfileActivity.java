package com.matrix_maeny.chatty.fragments.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.matrix_maeny.chatty.R;
import com.matrix_maeny.chatty.UserModel;
import com.matrix_maeny.chatty.databinding.ActivityProfileBinding;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    public static UserModel userModel;

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(userModel != null){
            setProfileData();
        }
    }

    private void setProfileData() {


        try {
            Picasso.get().load(userModel.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.peUserIv);
        } catch (Exception e) {
            e.printStackTrace();
            binding.peUserIv.setImageResource(R.drawable.profile_pic);
        }

        binding.prNameTv2.setText(userModel.getName());
        binding.prUsernameTv.setText(userModel.getUsername());
        binding.prAboutTv.setText(userModel.getAbout());

    }

}