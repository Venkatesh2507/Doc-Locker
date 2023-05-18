package com.example.doclocker;

import java.io.Serializable;
import java.util.ArrayList;


public class ModalPdfRetrieverClass implements Serializable {
    String name;
    String url;
    String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ModalPdfRetrieverClass(){}

    public ModalPdfRetrieverClass(String name,String url,String password) {
        this.name = name;
        this.url = url;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
