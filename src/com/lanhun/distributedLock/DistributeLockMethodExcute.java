package com.lanhun.distributedLock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DistributeLockMethodExcute {
	
	/**
	 * 
	 * @param target
	 * @param method
	 * @param args
	 * @param lock
	 * @return
	 * @throws Exception
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object lockMethodExcute(Object target, Method method, Object[] args, Lock lock)
			throws Exception, IllegalAccessException, InvocationTargetException {
		UniqMethod uniqMethod = method.getAnnotation(UniqMethod.class);
		String type = null;
		if (uniqMethod != null) {
			type = uniqMethod.value();
			if ("".equals(type)) {
				type = method.getReturnType().getName() + "." + method.getName();
			}
			boolean hasLock = lock.lock(type);
			if (hasLock) {
				try {
					Object ret = method.invoke(target, args);
					return ret;
				} catch (Exception e) {
					// unLock
					throw e;
				} finally {
					lock.unLock(type);
				}
			} else {
				throw new RuntimeException("can't get the lock:" + type);

			}

		} else {
			return method.invoke(target, args);
		}
	}
}
