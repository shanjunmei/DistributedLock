package com.lanhun.distributedLock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 
 * @author vincent
 *
 */
public class DistributeLockProxy implements InvocationHandler {

	private Object target;

	private Lock lock;

	public DistributeLockProxy(Object target, Lock lock) {
		this.target = target;
		this.lock = lock;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return DistributeLockMethodExcutor.lockMethodExcute(target, method, args, lock);

	}

}
