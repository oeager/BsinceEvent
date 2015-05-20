package com.developer.bsince.core;

import com.developer.bsince.event.Event;
import com.developer.bsince.event.EventFilter;


public interface IEngine {
	<T> T execute(Event<T> runable);

	<T> void submit(Event<T> callable);

	void shutdown();

	void finish(Event<?> task);

	void cancel(int serialNum);
	
	void cancel(Object tag);

	void cancel(EventFilter mFilter);

	void cancelAll();
	
	int ofEventCount();
}
