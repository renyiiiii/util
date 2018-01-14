package com.test.util.ratelimit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.test.util.ratelimit.ticket.TicketBucket;

public abstract class AbstractRateLimiter implements RateLimiter{
	private final Map<String,TicketBucket> ticketBucketMap = new HashMap<>();
	
	private static final ReadWriteLock rwl = new ReentrantReadWriteLock();
	private static final Lock readLock = rwl.readLock();
	private static final Lock writeLock = rwl.writeLock();
	

	@Override
	public boolean access(String url, String ip, String uid) {
		readLock.lock();
		TicketBucket ticketBucket = ticketBucketMap.get(buildKey(url, ip, uid));
		if(ticketBucket == null){
			readLock.unlock();
			writeLock.lock();
			try{
				if(null == ticketBucketMap.get(buildKey(url, ip, uid))){
					ticketBucket = newTicketBucket(url, ip, uid);
					ticketBucketMap.put(buildKey(url, ip, uid), ticketBucket);
				}
				ticketBucket = ticketBucketMap.get(buildKey(url, ip, uid));
				readLock.lock();
			}finally {
				writeLock.unlock();
			}
		}
		try{
			return ticketBucket.access();
		}finally {
			readLock.unlock();
		}
	}
	
	protected abstract String buildKey(String url, String ip, String uid);
	
	protected abstract TicketBucket newTicketBucket(String url, String ip, String uid);

}
