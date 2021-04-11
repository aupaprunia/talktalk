package com.hackillionis.talktalk.data;

public class ConnectionData {

    String uid, name, email, mobile, image_link, status;

    public ConnectionData(String uid, String name, String email, String mobile, String image_link, String status) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.image_link = image_link;
        this.status = status;
    }

    public ConnectionData(){}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getImage_link() {
        return image_link;
    }

    public void setImage_link(String image_link) {
        this.image_link = image_link;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
