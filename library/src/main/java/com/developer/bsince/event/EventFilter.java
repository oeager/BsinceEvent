package com.developer.bsince.event;




public interface EventFilter {
	public boolean apply(Event<?> task);
}
