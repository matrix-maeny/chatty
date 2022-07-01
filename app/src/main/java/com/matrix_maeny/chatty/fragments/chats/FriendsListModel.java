package com.matrix_maeny.chatty.fragments.chats;

import java.util.ArrayList;
import java.util.List;

public class FriendsListModel {

    private List<ChatFriendModel> friendsList = null;

    public FriendsListModel() {
    }

    public List<ChatFriendModel> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(List<ChatFriendModel> friendsList) {
        this.friendsList = friendsList;
    }

    public void addFriend(ChatFriendModel friendModel){
        if(friendsList == null) friendsList = new ArrayList<>();
        friendsList.add(friendModel);
    }



}
