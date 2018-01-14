package com.test.util.ratelimit;

import com.test.util.ratelimit.ticket.SimpleTicketBucket;
import com.test.util.ratelimit.ticket.TicketBucket;
/**
 * 针对每个用户的某些url调用次数做限制
 */
public class SimpleUidUrlRateLimiter extends AbstractRateLimiter{
	private String[] urls;
	private double[] times;
	private long[] durations;

	public SimpleUidUrlRateLimiter(String[] urls, double[] times, long[] durations) {
		super();
		this.urls = urls;
		this.times = times;
		this.durations = durations;
		if(urls.length != times.length || urls.length != durations.length || durations.length != times.length){
			throw new IllegalArgumentException("error length");
		}
	}

	@Override
	public boolean support(String url, String ip, String uid) {
		if(this.index(url) >= 0) return true;
		return false;
	}

	@Override
	protected String buildKey(String url, String ip, String uid) {
		if(uid == null) uid = "uid:";
		return uid + url;
	}

	@Override
	protected TicketBucket newTicketBucket(String url, String ip, String uid) {
		int index = this.index(url);
		TicketBucket ticketBucket = new SimpleTicketBucket(times[index], durations[index]);
		return ticketBucket;
	}
	
	private int index(String url){
		if(urls != null){
			for (int i=0; i<urls.length; i++) {
				if(urls[i].indexOf(url) >= 0){
					return i;
				}
			}
		}
		return -1;
	}

}
