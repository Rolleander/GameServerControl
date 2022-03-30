package com.broll.networklib;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadedListener {

    private ExecutorService threadPool;
    private final Listener listener;
    private final ThreadFactory threadFactory;
    private final int threadPoolSize;

    public ThreadedListener(Listener listener, String nameFormat, int threadPoolSize) {
        this.threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
        this.threadPoolSize = threadPoolSize;
        this.listener = listener;
    }

    public void attach(Server server) {
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
        server.addListener(new Listener.ThreadedListener(listener, threadPool));
    }

    public void attach(Client client) {
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
        client.addListener(new Listener.ThreadedListener(listener, threadPool));
    }

    public void remove(Server server) {
        server.removeListener(listener);
        if (this.threadPool != null) {
            this.threadPool.shutdown();
        }
    }

    public void remove(Client client) {
        client.removeListener(listener);
        if (this.threadPool != null) {
            this.threadPool.shutdown();
        }
    }
}
