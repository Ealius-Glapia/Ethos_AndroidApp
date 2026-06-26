package com.cardmaster.app.data.entity;

import java.io.Serializable;

public class GitHubRepository implements Serializable {
    private String url;
    private String password;

    public GitHubRepository(String url, String password) {
        this.url = url;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
