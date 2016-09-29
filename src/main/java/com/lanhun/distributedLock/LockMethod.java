package com.lanhun.distributedLock;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author vincent
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LockMethod {

    String value() default "";

    int lockIndex() default -1;

}
