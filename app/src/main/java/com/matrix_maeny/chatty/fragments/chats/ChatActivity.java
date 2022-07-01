package com.matrix_maeny.chatty.fragments.chats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
import com.matrix_maeny.chatty.databinding.ActivityChatBinding;
import com.matrix_maeny.chatty.registerActivities.LoginActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    public static ChatFriendModel chatFriendModel;

    private ActivityChatBinding binding;
    private String message = null;

    private String chatKey = null;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private String currentUid = null;

    private ExposeDialogs exposeDialogs;

    private List<ChatModel> list;
    private ChatAdapter chatAdapter;

    private DatabaseReference chatRef;
    private boolean temp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        initialize();


        binding.sendBtn.setOnClickListener(v -> {

            if (checkMessage()) {
                binding.messageEt.setText("");
                sendMessage();
            }
        });
    }

    private void initialize() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUid = firebaseAuth.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        exposeDialogs = new ExposeDialogs(ChatActivity.this);

        list = new ArrayList<>();
        chatAdapter = new ChatAdapter(ChatActivity.this, list);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        binding.recyclerView.setAdapter(chatAdapter);

        if (chatFriendModel != null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(chatFriendModel.getName());
            getChatKey();
        }


//        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchChats);
        chatRef = firebaseDatabase.getReference().child("Chats");

        chatRef.addValueEventListener(valueEventListener);


    }


    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
//            if (chatKey==null) {
            if (list.isEmpty() && temp) {
                getChatKey();
            } else temp = true;
//            }else
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void getChatKey() {
        exposeDialogs.showProgressDialog("Loading...", "wait...");

        final DatabaseReference keyRef = firebaseDatabase.getReference().child("Chats");

        keyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot s : snapshot.getChildren()) {

                        if (Objects.requireNonNull(s.getKey()).contains(currentUid) &&
                                s.getKey().contains(chatFriendModel.getUserUid())) {

                            chatKey = s.getKey();

                        }
                    }
                }
                if (chatKey == null) {
                    chatKey = currentUid + chatFriendModel.getUserUid();
                }
                setupListeners();
                exposeDialogs.dismissProgressDialog();

                fetchChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 1);
            }
        });
    }

    private void setupListeners() {
        final DatabaseReference listenerRef = firebaseDatabase.getReference().child("Chats").child(chatKey);

        listenerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fetchChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void fetchChats() {
        final DatabaseReference chatRef = firebaseDatabase.getReference().child("Chats").child(chatKey);
//        exposeDialogs.showToast(chatKey,1);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if (snapshot.exists()) {
                    ChatList chatList = snapshot.getValue(ChatList.class);

                    if (chatList != null) {
                        list.addAll(chatList.getChats());
                    }
                }
//                binding.swipeRefreshLayout.setRefreshing(false);
                refreshAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 1);
            }
        });


    }

    private void sendMessage() {

        list.add(new ChatModel(currentUid, message));
        refreshAdapter();
        final DatabaseReference chatRef = firebaseDatabase.getReference().child("Chats").child(chatKey);

//        ChatList chatList = new ChatList();
//        chatList.addChat(new ChatModel(currentUid,message));

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChatList chatList = null;
                if (snapshot.exists()) {
                    chatList = snapshot.getValue(ChatList.class);

                    if (chatList != null) chatList.addChat(new ChatModel(currentUid, message));
                }

                if (chatList == null) {
                    chatList = new ChatList();
                    chatList.addChat(new ChatModel(currentUid, message));
                }
                uploadChat(chatList, chatRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 1);
            }
        });

        setupFriend();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        chatAdapter.notifyDataSetChanged();
        binding.recyclerView.scrollToPosition(list.size() - 1);
    }

    private void uploadChat(ChatList chatList, @NonNull DatabaseReference chatRef) {
        chatRef.setValue(chatList).addOnCompleteListener(task -> {
        }).addOnFailureListener(e -> {
            exposeDialogs.dismissProgressDialog();
            exposeDialogs.showToast(e.getMessage(), 1);
        });
    }


    private void setupFriend() {
        final DatabaseReference friendRef = firebaseDatabase.getReference().child("Friends").child(chatFriendModel.getUserUid());

        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FriendsListModel friendsListModel = null;
                if (snapshot.exists()) {
                    friendsListModel = snapshot.getValue(FriendsListModel.class);

                    if (friendsListModel != null) {
                        for (int i = 0; i < friendsListModel.getFriendsList().size(); i++) {
                            if (friendsListModel.getFriendsList().get(i).getUserUid().equals(currentUid)) {
                                return;
                            }
                        }
                    }


                }

                if (friendsListModel == null) friendsListModel = new FriendsListModel();
                friendsListModel.addFriend(new ChatFriendModel(currentUid, LoginActivity.currentUserModel.getName(), LoginActivity.currentUserModel.getProfilePicUrl()));
                friendRef.setValue(friendsListModel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 1);
            }
        });
    }

    private boolean checkMessage() {
        try {
            message = binding.messageEt.getText().toString().trim();
            if (!message.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        exposeDialogs.showToast("Please enter message", 0);
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        clearChat();
        return super.onOptionsItemSelected(item);
    }

    private void clearChat() {
        exposeDialogs.showProgressDialog("Deleting chats..","wait...");

        final DatabaseReference reference = firebaseDatabase.getReference().child("Chats").child(chatKey);

        reference.removeValue().addOnCompleteListener(task -> {
            exposeDialogs.dismissProgressDialog();
            if(task.isSuccessful()){
                exposeDialogs.showToast("All chats are deleted",1);
            }else exposeDialogs.showToast(Objects.requireNonNull(task.getException()).getMessage(),1);
        }).addOnFailureListener(e -> {
            exposeDialogs.dismissProgressDialog();
            exposeDialogs.showToast(e.getMessage(),1);
        });
    }
}