package com.lanhun.distributedLock;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

public class DistributeLock implements Lock {

	private Logger logger = LogManager.getFormatterLogger(getClass());

	private ConcurrentHashMap<String, String> lockCache = new ConcurrentHashMap<String, String>();

	private ThreadLocal<String> keyCache = new ThreadLocal<String>();

	private Jedis jedis;

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	// @Override
	public String prefix() {
		return "_lock";
	}

	// @Override
	protected String generateKey() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	@Override
	public boolean lock(String type) {
		return lock(type, 0);
	}

	@Override
	public boolean lock(String type, int timeout) {
		if (timeout == 0) {// 没有设置超时时间的锁，先校验本地缓存
			if (lockCache.containsKey(type)) {
				if (lockCache.get(type).equals(keyCache.get())) {
					return true;
				}
			}
		}

		String key = generateKey();
		type = prefix() + ":" + type;
		long r = jedis.setnx(type, key);
		if (timeout != 0) {
			jedis.expire(type, timeout);
		}

		if (r > 0) {
			if (timeout > 0) {
				keyCache.set(key);
				lockCache.put(type, key);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean tryLock(String type) {
		return tryLock(type, 0L);
	}

	@Override
	public boolean tryLock(String type, long timeout) {
		if (timeout == 0) {
			return lock(type);
		}
		long t = System.currentTimeMillis();
		while ((System.currentTimeMillis() - t) < timeout) {
			try {
				if (!lock(type)) {
					TimeUnit.MILLISECONDS.sleep(timeout - 10);
				} else {
					return true;
				}
			} catch (InterruptedException e) {
				logger.info(e);
			}
		}
		return false;
	}

	@Override
	public void unLock(String type) {
		type = prefix() + ":" + type;
		lockCache.remove(type, lockCache.get(type));
		keyCache.remove();
		jedis.del(type + "_" + keyCache.get());

	}
}
