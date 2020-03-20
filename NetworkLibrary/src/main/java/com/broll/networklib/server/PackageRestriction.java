package com.broll.networklib.server;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(value=RUNTIME)
@Target(value=METHOD)
public @interface PackageRestriction {

    RestrictionType value() default RestrictionType.IN_LOBBY;
}
