package com.lanhun.distributedLock.test;

import com.lanhun.distributedLock.DistributeLock;
import com.lanhun.distributedLock.Lock;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

public class LockTest {

    public static void main(String[] args) {
        String type = "test";
        String host = "127.0.0.1";
        int port = 6379;
        String master="mymaster";
        String password="ffzx6102";
        Set<String> x=new HashSet<>();
        x.add("192.168.2.195:26379");

       // JedisPool jedisPool = new JedisPool(host, port);
        JedisSentinelPool jedisPool= new JedisSentinelPool(master,x,password);
        Lock lock = new DistributeLock();
        ((DistributeLock) lock).setJedisPool(jedisPool);
         lock.lock(type, "");


        //lock.lock(type+"12");

        //lock.unLock(type+"12");
        //lock.unLock(type);
        for (int i = 0; i < 1; i++) {
            t(type, lock);
        }
        lock.unLock(type,"");
        System.out.println("end");
    }

    private static void t(String type, Lock lock) {
        boolean x = false;
        long t = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            x = lock.lock(type, i+"");
           // System.out.println(x);
           // lock.unLock(type, "");
        }
        t = System.currentTimeMillis() - t;
        System.out.println(t + " ms");
    }

}
