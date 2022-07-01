package com.matrix_maeny.chatty.fragments.chats;

public class ChatFriendModel {


    private String userUid,profilePic="", name ="";

    public ChatFriendModel() {
    }

    public ChatFriendModel(String userUid, String name,String profilePic) {
        this.userUid = userUid;
        this.name = name;
        this.profilePic = profilePic;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
