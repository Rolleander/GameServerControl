package com.broll.networklib.test;

public interface IAsyncTask<T> {

    void run(AsyncTaskCallback<T> callback);

}
