package com.hackillionis.talktalk.data;

public class ListenerData {

    String uid, status;

    public ListenerData(String uid, String status) {
        this.uid = uid;
        this.status = status;
    }

    public ListenerData(){}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
