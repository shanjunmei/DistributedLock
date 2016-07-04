package com.lanhun.distributedLock.test;

import com.lanhun.distributedLock.DistributeLock;
import com.lanhun.distributedLock.Lock;

import redis.clients.jedis.Jedis;

public class LockTest {

	public static void main(String[] args) {
		String type = "test";

		Jedis jedis = new Jedis("127.0.0.1", 6379);
		Lock lock = new DistributeLock();
		((DistributeLock) lock).setJedis(jedis);
		lock.lock(type);
		lock.unLock(type);
		for(int i=0;i<10;i++){
			t(type, lock);
		}
	}

	private static void t(String type, Lock lock) {
		boolean x=false;
		long t = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			x = lock.lock(type);
			//System.out.println(x);
			lock.unLock(type);
		}
		t = System.currentTimeMillis() - t;
		System.out.println(t + " ms");
	}

}
