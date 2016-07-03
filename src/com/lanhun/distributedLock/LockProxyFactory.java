package com.lanhun.distributedLock;

import java.lang.reflect.Proxy;

/**
 * 
 * @author vincent
 *
 */
public class LockProxyFactory {

	@SuppressWarnings("unchecked")
	public static <T> T createProxy(Class<?> interfaces[], Lock lock, T target) {
		return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), interfaces,
				new DistributeLockProxy(target, lock));
	}

}
