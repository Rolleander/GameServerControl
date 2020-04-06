package com.broll.networklib.test;

import java.util.List;

public class TestUtils {

    public static Object poll(List list, int timeout) {
        long start = System.currentTimeMillis();
        do {
            if (!list.isEmpty()) {
                Object t = list.get(0);
                list.remove(t);
                return t;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while ((System.currentTimeMillis() - start) < timeout);
        return null;
    }
}
