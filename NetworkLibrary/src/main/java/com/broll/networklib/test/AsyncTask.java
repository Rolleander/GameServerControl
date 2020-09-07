package com.broll.networklib.test;

import com.google.common.util.concurrent.SettableFuture;

public class AsyncTask {

    public static <T> T doAsync(IAsyncTask<T> task) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SettableFuture<T> future = SettableFuture.create();
        task.run(new AsyncTaskCallback<T>() {
            @Override
            public void done(T t) {
                future.set(t);
            }

            @Override
            public void failed(Exception e) {
                future.setException(e);
            }
        });
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
