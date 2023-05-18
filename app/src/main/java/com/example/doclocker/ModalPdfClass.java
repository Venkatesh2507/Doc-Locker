package com.example.doclocker;

import java.io.Serializable;

public class ModalPdfClass implements Serializable {
    String name , url , password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ModalPdfClass(String name, String url,String password) {
        this.name = name;
        this.url = url;
        this.password = password;
    }
}
