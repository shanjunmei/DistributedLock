package com.lanhun.distributedLock;

import java.lang.reflect.Method;

public class DistributeLockMethodExecutor {

    /**
     * @param target 目标实例
     * @param method 目标方法
     * @param args   调用参数
     * @param lock   锁对象
     * @return
     * @throws Exception
     */
    public static Object lockMethodExecute(Object target, Method method, Object[] args, Lock lock)
            throws Exception {
        LockMethod uniqMethod = method.getAnnotation(LockMethod.class);
        String type = null;
        if (uniqMethod != null) {
            type = uniqMethod.value();
            int lockIndex = uniqMethod.lockIndex();
            if ("".equals(type)) {
                type = method.getReturnType().getName() + "." + method.getName();
            }
            if (lockIndex > -1) {
                type = type + ":" + args[lockIndex];
            }
            boolean hasLock = lock.lock(type, "");
            if (hasLock) {
                try {
                    return method.invoke(target, args);
                } finally {
                    lock.unLock(type, "");
                }
            } else {
                throw new RuntimeException("can't get the lock:" + type);

            }

        } else {
            return method.invoke(target, args);
        }
    }
}
