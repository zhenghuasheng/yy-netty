package com.netty.message;

/**
 * Created by zhenghuasheng on 2016/11/18.
 */
public class Constants {
    private static String clientId;
    public static String getClientId() {
        return clientId;
    }
    public static void setClientId(String clientId) {
        Constants.clientId = clientId;
    }
}
