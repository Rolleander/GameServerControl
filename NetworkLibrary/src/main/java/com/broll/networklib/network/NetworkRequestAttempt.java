package com.broll.networklib.network;

public interface NetworkRequestAttempt<T> extends NetworkRequest<T>{

    void failure(String reason);
}
