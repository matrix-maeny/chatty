package com.matrix_maeny.chatty.fragments.chats;

public class ChatModel {

    private String userUid;
    private String message;

    public ChatModel() {
    }

    public ChatModel(String userUid, String message) {
        this.userUid = userUid;
        this.message = message;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
