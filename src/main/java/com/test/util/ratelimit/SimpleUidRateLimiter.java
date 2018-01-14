package com.test.util.ratelimit;

import com.test.util.ratelimit.ticket.SimpleTicketBucket;
import com.test.util.ratelimit.ticket.TicketBucket;
/**
 * 针对每个用户的所有url调用次数做限制
 */
public class SimpleUidRateLimiter extends AbstractRateLimiter{
	private double times;
	private long durations;

	public SimpleUidRateLimiter(double times, long durations) {
		super();
		this.times = times;
		this.durations = durations;
	}

	@Override
	public boolean support(String url, String ip, String uid) {
		return true;
	}

	@Override
	protected String buildKey(String url, String ip, String uid) {
		if(uid == null) return "uid:allUrl";
		return uid + ":allUrl";
	}

	@Override
	protected TicketBucket newTicketBucket(String url, String ip, String uid) {
		TicketBucket ticketBucket = new SimpleTicketBucket(times, durations);
		return ticketBucket;
	}
	
}
