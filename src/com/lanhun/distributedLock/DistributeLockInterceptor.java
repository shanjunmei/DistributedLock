package com.lanhun.distributedLock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
/**
 * 
 * @author vincent
 *
 */
public class DistributeLockInterceptor implements InvocationHandler {

	private Object target;

	private Lock lock;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
				}finally {
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
