package com.test.util.ratelimit;
/**
 * 
 *
 */
public interface RateLimiter {
	
	/**
	 * 
	 * @param url
	 * @param ip
	 * @param uid
	 * @return true if support
	 */
	boolean support(String url, String ip, String uid);
	
	/**
	 * 
	 * @param url
	 * @param ip
	 * @param uid
	 * @return true if can access
	 */
	boolean access(String url, String ip, String uid);

}
