package com.lanhun.distributedLock;

/**
 * 
 * @author vincent
 *
 */
public interface Lock {

	//String generateKey();

	//String prefix();
	
	boolean lock(String type);

	boolean lock(String type, int timeout);

	boolean tryLock(String type);

	boolean tryLock(String type, long timeout);

	void unLock(String key);

}
