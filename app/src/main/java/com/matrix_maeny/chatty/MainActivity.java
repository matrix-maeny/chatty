package com.matrix_maeny.chatty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.matrix_maeny.chatty.databinding.ActivityMainBinding;
import com.matrix_maeny.chatty.dialogs.AddFriendDialog;
import com.matrix_maeny.chatty.fragments.chats.ChatFriendAdapter;
import com.matrix_maeny.chatty.fragments.chats.ChatsFragment;
import com.matrix_maeny.chatty.fragments.profile.ProfileFragment;
import com.matrix_maeny.chatty.registerActivities.LoginActivity;

import java.util.Objects;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity implements ProfileFragment.ProfileFragmentListener, ChatsFragment.ChatsFragmentListener ,
        ChatFriendAdapter.ChatFriendAdapterListener {

    private ActivityMainBinding binding;

    private ExposeDialogs exposeDialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.containerFrame, new ChatsFragment());
        fragmentTransaction.commit();

        exposeDialogs = new ExposeDialogs(MainActivity.this);

        binding.bottomBar.setOnItemSelectedListener((OnItemSelectedListener) i -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


            switch (i) {
                case 0:
                    transaction.replace(R.id.containerFrame, new ChatsFragment());
                    break;
                case 1:
                    transaction.replace(R.id.containerFrame, new ProfileFragment());
                    break;


            }
            transaction.commit();
            return true;
        });
    }

    @Override
    public void hideToolbar(boolean shouldHide) {
        if (shouldHide) {
            Objects.requireNonNull(getSupportActionBar()).hide();
        } else {
            Objects.requireNonNull(getSupportActionBar()).show();

        }
    }

    @Override
    public void logOut() {
        FirebaseAuth.getInstance().signOut();

        exposeDialogs.showProgressDialog("Logging out", "Please wait");

        new Handler().postDelayed(() -> {
            exposeDialogs.dismissProgressDialog();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }, 1500);
    }


    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return super.onCreatePanelMenu(featureId, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_friend:
                shoAddFriendDialog();
                break;
            case R.id.about_app:
                // go to about activity
                startActivity(new Intent(MainActivity.this,AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shoAddFriendDialog() {
        AddFriendDialog friendDialog = new AddFriendDialog();
        friendDialog.show(getSupportFragmentManager(),"Add Friend Dialog");
    }


}