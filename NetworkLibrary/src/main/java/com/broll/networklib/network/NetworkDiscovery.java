package com.broll.networklib.network;

public class NetworkDiscovery<T> {

    private INetworkDiscoveryRequest<T> request;

    private int loaded;

    private int loadingCount;

    public NetworkDiscovery(INetworkDiscoveryRequest<T> request, int loadingCount){
        this.request = request;
        this.loadingCount = loadingCount;
        if(loadingCount==0){
            request.finished(0);
        }
    }

    public void discovered(T t){
        loaded++;
        request.receive(t);
        if(loaded == loadingCount){
            request.finished(loadingCount);
        }
    }
}
