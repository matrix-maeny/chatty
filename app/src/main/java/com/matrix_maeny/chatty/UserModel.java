package com.matrix_maeny.chatty;

public class UserModel {


    private String username, email, password, profilePicUrl = "";//,bannerUrl="";
    private String about = "No about provided";
    private String name;


    public UserModel() {
    }

    public UserModel(String username, String email, String password,String name) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

//    public String getBannerUrl() {
//        return bannerUrl;
//    }
//
//    public void setBannerUrl(String bannerUrl) {
//        this.bannerUrl = bannerUrl;
//    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
