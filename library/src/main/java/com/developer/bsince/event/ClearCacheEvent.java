package com.developer.bsince.event;

import java.util.concurrent.Callable;

import com.developer.bsince.cache.HttpCache;
import com.developer.bsince.request.Priority;

import android.os.Handler;
import android.os.Looper;

public class ClearCacheEvent extends CallableEvent<Object>{
	public ClearCacheEvent(HttpCache cache) {
		this(cache,null);
	}

	
	public ClearCacheEvent(final HttpCache cache,final Runnable callable) {
		super(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				cache.clear();
				if(callable!=null){
					Handler handler = new Handler(Looper.getMainLooper());
					handler.postAtFrontOfQueue(callable);
				}
				return null;
			}
		});
		setPriority(Priority.IMMEDIATE);
	}

}
