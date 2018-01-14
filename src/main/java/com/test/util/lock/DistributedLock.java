package com.test.util.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {
	
	void acquire(String lockKey) throws Exception;
	
	/**
	 *  String lockValue = null;
	 *  try{
	 *      lockValue = lock.acquire(lockKey,time,unit);
	 *      ...
	 *      
	 *      return x;
	 *  }catch(Exception e){
	 *      e.printStackTrace();
	 *      return y;
	 *  }finnaly{
	 *     if(lockValue != null){
	 *        lock.release(lockKey, lockValue);
	 *     }
	 *  }
	 * 
	 * 
	 * @param lockKey
	 * @param time
	 * @param unit
	 * @return lockValue non-null if acquired, null if not
	 * @throws Exception
	 */
	String acquire(String lockKey, long time, TimeUnit unit) throws Exception;
	
	/**
	 * 
	 * @param lockKey
	 * @param lockValue
	 * @return true if released, false if not
	 */
	boolean release(String lockKey, String lockValue);

}
