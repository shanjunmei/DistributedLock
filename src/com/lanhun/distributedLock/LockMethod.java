package com.lanhun.distributedLock;

/**
 * 
 * @author vincent
 *
 */
public @interface LockMethod {
	
	String value() default "";

	int lockIndex() default -1;

}
