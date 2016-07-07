package com.lanhun.distributedLock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class DistributeLock implements Lock {

	private Logger logger = LogManager.getLogger(getClass());

	private ConcurrentHashMap<String, String> lockCache = new ConcurrentHashMap<String, String>();

	private ThreadLocal<Map<String, String>> keyCache = new ThreadLocal<Map<String, String>>();

	private JedisPool jedisPool;

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
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
		type = prefix() + ":" + type;

		if (timeout == 0) {// 没有设置超时时间的锁，先校验本地缓存
			synchronized (lockCache) {
				if (lockCache.containsKey(type)) {
					if (lockCache.get(type).equals(keyCache.get())) {
						return true;
					}
				}
			}

		}

		String key = generateKey();

		Jedis jedis = jedisPool.getResource();

		long r = jedis.setnx(type, key);
		if (timeout != 0) {
			jedis.expire(type, timeout);
		}

		jedis.close();

		if (r > 0) {
			Map<String, String> cache = keyCache.get();
			if (cache == null) {
				cache = new HashMap<>();
				keyCache.set(cache);
			}
			cache.put(type, key);
			if (timeout == 0) {
				synchronized (lockCache) {
					lockCache.put(type, key);
				}
				
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
		Map<String, String> cache = keyCache.get();
		if (cache != null) {
			String lockCacheKey = cache.get(type);
			synchronized (lockCache) {
				lockCache.remove(type, lockCacheKey);
			}
			
			Jedis jedis = jedisPool.getResource();
			String key = jedis.get(type);
			if (key != null && !"".equals(key)) {
				if (key.equals(lockCacheKey)) {
					jedis.del(type);
				}
			}
			jedis.close();
		} else {
			// 忽略未加锁 进行解锁
		}

	}
}
