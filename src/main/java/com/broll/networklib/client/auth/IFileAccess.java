package com.broll.networklib.client.auth;

public interface IFileAccess {

    boolean exists();
    String read();
    void write(String content);
    void delete();

}
