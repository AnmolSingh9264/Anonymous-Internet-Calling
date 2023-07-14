package com.banti.personalcalls.models;

public class requestmodel {
    String uid,uid2;
    public requestmodel(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid2() {
        return uid2;
    }

    public void setUid2(String uid2) {
        this.uid2 = uid2;
    }

    public requestmodel(String uid, String uid2) {
        this.uid = uid;
        this.uid2=uid2;
    }
}
