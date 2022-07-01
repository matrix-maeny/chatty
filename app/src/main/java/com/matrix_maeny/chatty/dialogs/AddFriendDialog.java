package com.matrix_maeny.chatty.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.chatty.ExposeDialogs;
import com.matrix_maeny.chatty.R;
import com.matrix_maeny.chatty.UserModel;
import com.matrix_maeny.chatty.databinding.AddFriendDialogBinding;
import com.matrix_maeny.chatty.fragments.chats.ChatActivity;
import com.matrix_maeny.chatty.fragments.chats.ChatFriendModel;
import com.matrix_maeny.chatty.fragments.chats.FriendsListModel;
import com.matrix_maeny.chatty.registerActivities.LoginActivity;

import java.util.Objects;

public class AddFriendDialog extends AppCompatDialogFragment {

    private AddFriendDialogBinding binding;

    private ExposeDialogs exposeDialogs;
    private FirebaseDatabase firebaseDatabase;
    private String currentUid;
    private String username = null;

    private ChatFriendModel friendModel = null;

    private FriendsListModel listModel = new FriendsListModel();
    private FriendsListModel listModel2 = new FriendsListModel();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert);
        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);

        @SuppressLint("InflateParams") View root = requireActivity().getLayoutInflater().inflate(R.layout.add_friend_dialog, null);
        binding = AddFriendDialogBinding.bind(root);
        builder.setView(binding.getRoot());

        firebaseDatabase = FirebaseDatabase.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();

        exposeDialogs = new ExposeDialogs(requireContext());

        binding.addBtn.setOnClickListener(v -> {
            if (checkUsername()) {
                searchAndAdd();
            }
        });

        return builder.create();
    }


    private boolean checkUsername() {
        try {
            username = Objects.requireNonNull(binding.usernameEt.getText()).toString().trim();
            if (!username.equals("")) {
                if (username.equals(LoginActivity.currentUserModel.getUsername())) {
                    exposeDialogs.showToast("Can't chat with yourself", 1);
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        exposeDialogs.showToast("Please enter username", 0);
        return false;
    }

    private void searchAndAdd() {

        exposeDialogs.showProgressDialog("Searching user...", "Please wait..");

        final DatabaseReference userRef = firebaseDatabase.getReference().child("Users");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    if (checkUsernameExists(snapshot)) {

                        addFriend();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 0);
            }
        });

    }

    private void addFriend() {


        final DatabaseReference currentFrRef = firebaseDatabase.getReference().child("Friends").child(currentUid);
        final DatabaseReference friendsRef = firebaseDatabase.getReference().child("Friends").child(friendModel.getUserUid());

        currentFrRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listModel = snapshot.getValue(FriendsListModel.class);

                    if (listModel != null) {
                        for (ChatFriendModel x : listModel.getFriendsList()) {
                            if (friendModel.getUserUid().equals(x.getUserUid())) {
                                exposeDialogs.dismissProgressDialog();
                                exposeDialogs.showToast("Person exists in your chat", 1);
                                return;
                            }
                        }
                        listModel.addFriend(friendModel);
                    }
                }

                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showProgressDialog("Creating connection", "Please wait..");
                setFriendsList(listModel, currentFrRef);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 0);
            }
        });
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listModel2 = snapshot.getValue(FriendsListModel.class);

                    if (listModel2 != null) {
                        for (ChatFriendModel x : listModel2.getFriendsList()) {
                            if (currentUid.equals(x.getUserUid())) {
//                                exposeDialogs.dismissProgressDialog();
//                                exposeDialogs.showToast("Person exists in your chat",1);
                                return;
                            }
                        }
                        listModel2.addFriend(friendModel);
                    }
                }

//                exposeDialogs.dismissProgressDialog();
//                exposeDialogs.showProgressDialog("Creating connection", "Please wait..");
                if (listModel2 == null) listModel2 = new FriendsListModel();

                listModel2.addFriend(new ChatFriendModel(currentUid,LoginActivity.currentUserModel.getName(),
                        LoginActivity.currentUserModel.getProfilePicUrl()));

                friendsRef.setValue(listModel2);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                exposeDialogs.dismissProgressDialog();
//                exposeDialogs.showToast(error.getMessage(), 0);
            }
        });


    }

    private void setFriendsList(FriendsListModel listModel, @NonNull DatabaseReference currentFrRef) {

        currentFrRef.setValue(listModel).addOnCompleteListener(task -> {
            exposeDialogs.dismissProgressDialog();

            if (task.isSuccessful()) {
                requireContext().startActivity(new Intent(requireContext().getApplicationContext(), ChatActivity.class));
            } else
                exposeDialogs.showToast(Objects.requireNonNull(task.getException()).getMessage(), 1);

            dismiss();
        }).addOnFailureListener(e -> {
            exposeDialogs.dismissProgressDialog();
            exposeDialogs.showToast(e.getMessage(), 0);
        });
    }

    private boolean checkUsernameExists(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot s : snapshot.getChildren()) {
            UserModel model = s.getValue(UserModel.class);

            if (model != null && model.getUsername().equals(username)) {
                friendModel = new ChatFriendModel(s.getKey(), model.getName(), model.getProfilePicUrl());
                ChatActivity.chatFriendModel = friendModel;
                listModel.addFriend(friendModel);
                return true;
            }
        }
        exposeDialogs.dismissProgressDialog();
        exposeDialogs.showToast("Not user found", 1);
        return false;
    }

}
