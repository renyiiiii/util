package com.test.util.ratelimit.ticket;

public class SimpleTicketBucket extends AbstractTicketBucket{
	private volatile int times;
	private volatile long lastAccess;

	public SimpleTicketBucket(double capacity, long duration) {
		super(capacity, duration);
	}


	@Override
	protected int getTimes() {
		return this.times;
	}

	@Override
	protected void setTimes(int times) {
		this.times = times;
	}

	@Override
	protected long getLastAccess() {
		return this.lastAccess;
	}

	@Override
	protected void setLastAccess(long lastAccess) {
		this.lastAccess = lastAccess;
	}

}
