package com.matrix_maeny.chatty.fragments.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matrix_maeny.chatty.R;
import com.matrix_maeny.chatty.databinding.ChatModelBinding;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.viewHolder> {

    private final Context context;
    private final List<ChatModel> list;

    public ChatAdapter(Context context, List<ChatModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_model, parent, false);
        return new viewHolder(view);
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        ChatModel chatModel = list.get(position);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.binding.chatTv.getLayoutParams();

        if (ChatActivity.chatFriendModel.getUserUid().equals(chatModel.getUserUid())) {
            layoutParams.gravity = Gravity.CENTER | Gravity.LEFT;
            holder.binding.chatTv.setBackgroundResource(R.drawable.reciever_chat_bg);

        } else {
            layoutParams.gravity = Gravity.CENTER | Gravity.RIGHT;
            holder.binding.chatTv.setBackgroundResource(R.drawable.sender_chat_bg);

        }

        holder.binding.chatTv.setLayoutParams(layoutParams);

        holder.binding.chatTv.setText(chatModel.getMessage());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        ChatModelBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ChatModelBinding.bind(itemView);
        }
    }
}
