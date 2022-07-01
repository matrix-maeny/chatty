package com.matrix_maeny.chatty.fragments.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.chatty.ExposeDialogs;
import com.matrix_maeny.chatty.R;
import com.matrix_maeny.chatty.UserModel;
import com.matrix_maeny.chatty.databinding.ChatFriendModelBinding;
import com.matrix_maeny.chatty.fragments.profile.ProfileActivity;
import com.matrix_maeny.chatty.registerActivities.LoginActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class ChatFriendAdapter extends RecyclerView.Adapter<ChatFriendAdapter.viewHolder> {

    private final Context context;
    private final List<ChatFriendModel> list;

    private final ChatFriendAdapterListener listener;

    public ChatFriendAdapter(Context context, List<ChatFriendModel> list) {
        this.context = context;
        this.list = list;

        listener = (ChatFriendAdapterListener) context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.chat_friend_model, parent, false);
        return new viewHolder(view);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        ChatFriendModel model = list.get(position);

        holder.binding.chatUsernameTv.setText(model.getName());

        setProfilePic(model, holder.binding);

        holder.binding.cardView.setOnClickListener(v -> {
            ChatActivity.chatFriendModel = model;
            context.startActivity(new Intent(context.getApplicationContext(), ChatActivity.class));
        });

        holder.binding.cardView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.binding.cardView);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()) {
                    case R.id.show_profile:
                        // show profile
                        showUserProfile(model.getUserUid());
                        break;
                    case R.id.delete_chat:
                        // delete chat
                        deleteUser(model);
                }
                return true;
            });
            popupMenu.show();
            return true;
        });

    }

    private void setProfilePic(@NonNull ChatFriendModel model, ChatFriendModelBinding binding) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(model.getUserUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    if (userModel != null) {
                        try {
                            Picasso.get().load(userModel.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.chatUserPicIv);
                        } catch (Exception e) {
                            e.printStackTrace();
                            binding.chatUserPicIv.setImageResource(R.drawable.profile_pic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteUser(@NonNull ChatFriendModel model) {
        ExposeDialogs exposeDialogs = new ExposeDialogs(context);
        exposeDialogs.showProgressDialog("Deleting...", "wait...");
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Friends")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

//        userRef.child(model.getUserUid()).removeValue().addOnCompleteListener(task -> {
//            if(task.isSuccessful()){
//                exposeDialogs.dismissProgressDialog();
//                exposeDialogs.showToast("Deleted...",1);
//            }else exposeDialogs.showToast(Objects.requireNonNull(task.getException()).getMessage(),1);
//        }).addOnFailureListener(e -> {
//            exposeDialogs.dismissProgressDialog();
//            exposeDialogs.showToast(e.getMessage(),0);
//        });

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    FriendsListModel friendsListModel = snapshot.getValue(FriendsListModel.class);

                    if (friendsListModel != null) {

                        for (int i = 0; i < friendsListModel.getFriendsList().size(); i++) {
                            if (friendsListModel.getFriendsList().get(i).getUserUid().equals(model.getUserUid())) {
                                friendsListModel.getFriendsList().remove(i);
                                break;
                            }
                        }
                    }

                    userRef.setValue(friendsListModel).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            exposeDialogs.dismissProgressDialog();
                            exposeDialogs.showToast("Deleted", 1);
                        } else
                            exposeDialogs.showToast(Objects.requireNonNull(task.getException()).getMessage(), 1);
                    }).addOnFailureListener(e -> {
                        exposeDialogs.dismissProgressDialog();
                        exposeDialogs.showToast(e.getMessage(), 0);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 0);
            }
        });
    }

    private void showUserProfile(String userUid) {
        ExposeDialogs exposeDialogs = new ExposeDialogs(context);
        exposeDialogs.showProgressDialog("Loading...", "wait...");

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userUid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    UserModel model = snapshot.getValue(UserModel.class);
                    if (model != null) {
                        ProfileActivity.userModel = model;
                        context.startActivity(new Intent(context.getApplicationContext(), ProfileActivity.class));

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

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface ChatFriendAdapterListener {

    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        ChatFriendModelBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ChatFriendModelBinding.bind(itemView);
        }
    }
}
