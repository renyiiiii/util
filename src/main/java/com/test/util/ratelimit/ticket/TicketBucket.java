package com.test.util.ratelimit.ticket;

public interface TicketBucket {
	boolean access();
}
