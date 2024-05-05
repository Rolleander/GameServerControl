package com.broll.networklib.test;

import com.broll.networklib.PackageReceiver;
import com.broll.networklib.network.AnnotationScanner;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class ScannerTest {

    @Test
    public void scansMethodsCorrectly() {
        Assert.assertArrayEquals(new String[]{"com.broll.networklib.test.ScannerTest$A:test"},
                scanPackageReceivers(new A()));
        Assert.assertArrayEquals(new String[]{"com.broll.networklib.test.ScannerTest$B:test",
                        "com.broll.networklib.test.ScannerTest$B:test2"},
                scanPackageReceivers(new B()));
        Assert.assertArrayEquals(new String[]{"com.broll.networklib.test.ScannerTest$A:test",
                        "com.broll.networklib.test.ScannerTest$C:test2"
                },
                scanPackageReceivers(new C()));
    }

    private String[] scanPackageReceivers(Object o) {
        return AnnotationScanner.findAnnotatedMethods(o, PackageReceiver.class).stream().map(m ->
                m.getDeclaringClass().getName() + ":" + m.getName()).sorted().toArray(String[]::new);
    }

    public static class A {

        @PackageReceiver
        void test() {

        }
    }

    public static class B extends A {
        @PackageReceiver
        @Override
        void test() {
            super.test();
        }

        @PackageReceiver
        void test2() {

        }

    }

    public static class C extends A {

        @PackageReceiver
        void test2() {

        }

    }
}

