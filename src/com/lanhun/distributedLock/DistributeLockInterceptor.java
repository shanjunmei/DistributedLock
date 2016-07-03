package com.lanhun.distributedLock;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
/**
 * 
 * @author vincent
 *
 */
public class DistributeLockInterceptor implements MethodInterceptor {

	private Lock lock;

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	@Override
	public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {

		return DistributeLockMethodExcute.lockMethodExcute(target, method, args, lock);
	}

}