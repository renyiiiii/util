package com.test.util.lock;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import redis.clients.jedis.JedisCluster;

public class RedisDistributedLock extends AbstractDistributedLock{
	private static final long defaultForever = 3600000;  //默认永久获取时间  3600秒
	private static final int defaultExpire  = 15;  //默认锁有效时间 防止没有释放 15秒
	
	private JedisCluster jedisCluster;
	
	private long forever;
	private int expire;
	
	public RedisDistributedLock() {
		super();
		this.forever = defaultForever;
		this.expire = defaultExpire;
	}

	public RedisDistributedLock(long forever, int expire) {
		super();
		this.forever = forever;
		this.expire = expire;
	}

	@Override
	protected String doAcquire(String lockKey, long millis) {
		long endLine = 0;
		long now = System.currentTimeMillis();
		if(millis < 0){
			endLine = now + forever;
		}else if(millis == 0){
			endLine = now;
		}else{
			endLine = now + millis;
		}
		String lockValue = UUID.randomUUID().toString();
		long setnx = 0;
		while(now <= endLine && setnx == 0){
			/* 在指定的 key 不存在时，为 key 设置指定的值
			 * 设置成功，返回 1 ; 设置失败，返回 0 
			 */
			setnx = jedisCluster.setnx(lockKey, lockValue);
			if(setnx == 1){
				jedisCluster.expire(lockKey, expire);
				return lockValue;
			}
			if(setnx == 0){
				//等待1s
				LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
				now = System.currentTimeMillis();
			}
			
		}
		return null;
	}

	@Override
	protected boolean doRelease(String lockKey, String lockValue) {
		String value = jedisCluster.get(lockKey);
		if(value == null){//expired
			return true;
		}
		if(value.equals(lockValue)){
			jedisCluster.del(lockKey);
			return true;
		}
		return false;
	}

	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}

	public void setJedisCluster(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
	}

	
}
