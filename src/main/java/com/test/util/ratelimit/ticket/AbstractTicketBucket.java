package com.test.util.ratelimit.ticket;

public abstract class AbstractTicketBucket implements TicketBucket{
	private double capacity;//容量
	private long duration;//时间段 毫秒
	
	public AbstractTicketBucket(double capacity, long duration) {
		super();
		this.capacity = capacity;
		this.duration = duration;
		this.setTimes((int)Math.round(capacity));
		this.setLastAccess(System.currentTimeMillis());
	}

	/**
	 * 分布式情况需要Override加分布式锁
	 * @return
	 */
	@Override
	public synchronized boolean access() {
		this.supply();
		if(getTimes() > 0){
			int times = getTimes() - 1;
			setTimes(times);
			return true;
		}
		return false;
	}
	
	
	
	protected abstract int getTimes();
	protected abstract void setTimes(int times);

	protected abstract long getLastAccess();
	protected abstract void setLastAccess(long lastAccess);


	private void supply(){
		long now = System.currentTimeMillis();
		double times = (now - getLastAccess())/duration * capacity + getTimes();
		if(times > capacity){
			setTimes((int)Math.round(capacity));
		}else{
			setTimes((int)Math.round(times));
		}
		setLastAccess(now);
	}

}
