package com.matrix_maeny.chatty.fragments.chats;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.style.AlignmentSpan;
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
import com.matrix_maeny.chatty.databinding.FragmentChatsBinding;
import com.matrix_maeny.chatty.dialogs.AddFriendDialog;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {


    private FragmentChatsBinding binding;

    private ChatsFragmentListener listener;

    private List<ChatFriendModel> list;
    private ChatFriendAdapter adapter;

    private FirebaseDatabase firebaseDatabase;
    private String currentUid;
    private ExposeDialogs exposeDialogs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        listener = (ChatsFragmentListener) requireContext();
        listener.hideToolbar(false);

        initialize();
        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchFriends);

        return binding.getRoot();
    }

    private void initialize() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        currentUid = FirebaseAuth.getInstance().getUid();
        setupListeners();

        list = new ArrayList<>();
        adapter = new ChatFriendAdapter(requireContext(), list);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        exposeDialogs = new ExposeDialogs(requireContext());


        fetchFriends();

    }

    private void fetchFriends() {
        exposeDialogs.showProgressDialog("Fetching chats...", "wait...");

        final DatabaseReference friendRef = firebaseDatabase.getReference().child("Friends").child(currentUid);

        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if (snapshot.exists()) {

                    FriendsListModel listModel = snapshot.getValue(FriendsListModel.class);

                    if (listModel != null) {
                        list.addAll(listModel.getFriendsList());
                    }
                }

                exposeDialogs.dismissProgressDialog();
                binding.swipeRefreshLayout.setRefreshing(false);
                refreshAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exposeDialogs.dismissProgressDialog();
                exposeDialogs.showToast(error.getMessage(), 1);
            }
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    public void refreshAdapter() {
        adapter.notifyDataSetChanged();

        if (list.isEmpty()) binding.emptyTv.setVisibility(View.VISIBLE);
        else binding.emptyTv.setVisibility(View.GONE);

    }

    private void setupListeners() {

        final DatabaseReference friendsRef = firebaseDatabase.getReference().child("Friends");

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fetchFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public interface ChatsFragmentListener {
        void hideToolbar(boolean shouldHide);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchFriends();
    }
}