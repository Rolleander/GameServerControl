package com.broll.networklib.network;

public interface INetworkDiscoveryRequest<T> extends INetworkRequestAttempt<T> {

    void finished(int discoveredCount);
}
