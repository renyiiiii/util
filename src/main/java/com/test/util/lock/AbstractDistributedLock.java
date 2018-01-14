package com.test.util.lock;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 参考
 * {@link org.apache.curator.framework.recipes.locks.InterProcessMutex}
 *
 */
public abstract class AbstractDistributedLock implements DistributedLock {

	private final ConcurrentMap<Thread, LockData> threadData = new ConcurrentHashMap<>();

	private static class LockData {
		final Thread owningThread;
		final String lockValue;
		final AtomicInteger lockCount = new AtomicInteger(1);

		private LockData(Thread owningThread, String lockValue) {
			this.owningThread = owningThread;
			this.lockValue = lockValue;
		}
	}

	private static final String LOCK_NAME = "lock-";

	public void acquire(String lockKey) throws Exception {
		if(acquire(lockKey, -1, null) == null){
			throw new IOException("Lost connection while trying to acquire lock: " + lockKey);
		}
	}

	public String acquire(String lockKey, long time, TimeUnit unit) throws Exception {
		lockKey = this.preLockKey(lockKey);
		if(time < 0 || unit == null){
			return this.internalLock(lockKey, -1);
		}
		long millis = unit.toMillis(time);
       
		return this.internalLock(lockKey, millis);
	}

	public boolean release(String lockKey, String lockValue) {
		lockKey = this.preLockKey(lockKey);
		Thread currentThread = Thread.currentThread();
		LockData lockData = threadData.get(currentThread);
		if (lockData == null) {
			throw new IllegalMonitorStateException("You do not own the lock: " + lockKey);
		}

		if (!lockData.lockValue.equals(lockValue)) {
			throw new IllegalMonitorStateException("Wrong lockValue for the lock: " + lockKey);
		}

		int newLockCount = lockData.lockCount.decrementAndGet();
		if (newLockCount > 0) {
			return true;
		}
		if (newLockCount < 0) {
			throw new IllegalMonitorStateException("Lock count has gone negative for lock: " + lockKey);
		}
		try {
			return doRelease(lockKey, lockValue);
		} finally {
			threadData.remove(currentThread);
		}
	}

	/**
	 * 
	 * @param lockKey 
	 * @param millis 
	 * @return
	 */
	protected abstract String doAcquire(String lockKey, long millis);

	/**
	 * 
	 * @param lockKey
	 * @param lockValue
	 * @return
	 */
	protected abstract boolean doRelease(String lockKey, String lockValue);

	private String internalLock(String lockKey, long millis) throws Exception {

		Thread currentThread = Thread.currentThread();

		LockData lockData = threadData.get(currentThread);
		if (lockData != null) {
			// re-entering
			lockData.lockCount.incrementAndGet();
			return lockData.lockValue;
		}

		String lockValue = doAcquire(lockKey, millis);
		if (lockValue != null) {
			LockData newLockData = new LockData(currentThread, lockValue);
			threadData.put(currentThread, newLockData);
			return lockValue;
		}

		return null;
	}

	private String preLockKey(String lockKey) {
		if (lockKey == null)
			return LOCK_NAME;
		return LOCK_NAME + lockKey;
	}

}
