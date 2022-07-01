package com.matrix_maeny.chatty.fragments.chats;

import java.util.ArrayList;
import java.util.List;

public class ChatList {

    private List<ChatModel> chats;

    public ChatList() {
    }


    public List<ChatModel> getChats() {
        return chats;
    }

    public void setChats(List<ChatModel> chats) {
        this.chats = chats;
    }

    public void addChat(ChatModel chatModel){
        if(chats==null) chats = new ArrayList<>();
        chats.add(chatModel);
    }
}
