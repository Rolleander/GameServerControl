package com.broll.networklib.test;

public interface AsyncTaskCallback<T> {

    void done(T t);

    void failed(Exception e);
}
