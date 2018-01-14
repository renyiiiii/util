package com.test.util.ratelimit;

import java.util.List;

public class RateLimiterChain {
	private List<RateLimiter> rateLimiters;
	
	public boolean access(String url, String ip, String uid){
		if(rateLimiters != null){
			for (RateLimiter rateLimiter : rateLimiters) {
				if(rateLimiter.support(url, ip, uid)){
					if(rateLimiter.access(url, ip, uid)){
						return false;
					}
				}
			}
		}
		return true;
	}

	public List<RateLimiter> getRateLimiters() {
		return rateLimiters;
	}

	public void setRateLimiters(List<RateLimiter> rateLimiters) {
		this.rateLimiters = rateLimiters;
	}
	
	

}
