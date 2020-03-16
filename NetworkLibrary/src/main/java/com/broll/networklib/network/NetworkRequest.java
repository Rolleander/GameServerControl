package com.broll.networklib.network;

public interface NetworkRequest<T> {

    void receive(T response);
}
