package com.broll.networklib.network;

public interface INetworkRequestAttempt<T> extends INetworkRequest<T> {

    void failure(String reason);
}
