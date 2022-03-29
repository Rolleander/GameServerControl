package com.broll.networklib.network;

public interface INetworkRequest<T> {

    void receive(T response);
}
