package com.netty.message;

import java.io.Serializable;

/**
 * Created by zhenghuasheng on 2016/11/18.
 */
public class AskParams implements Serializable{
    private static final long serialVersionUID = 1L;
    private String auth;

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
