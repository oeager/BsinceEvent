package com.developer.bsince.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.developer.bsince.cache.HttpCache;
import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.event.Event;
import com.developer.bsince.event.EventFilter;
import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.response.ResponseController;
import com.developer.bsince.response.ResponseHandler;

import android.util.Log;


public class BsinceEngineImp implements IEngine {
	
	private static final String TAG = "IEngine";
	
	private AtomicInteger mSerialNumber = new AtomicInteger();

	private final Map<String, Queue<Event<?>>> mWaitingTasks = new HashMap<String, Queue<Event<?>>>();

	private final Set<Event<?>> mCurrentTasks = new HashSet<Event<?>>();

	private final PriorityBlockingQueue<Event<?>> assistQueue = new PriorityBlockingQueue<Event<?>>();

	private final PriorityBlockingQueue<Event<?>> majorQueue = new PriorityBlockingQueue<Event<?>>();

	private final HttpCache mCache;

	private final ResponseController mController;

	private MajorThread[] majorThreads;

	private CacheThread mCacheThread;
	
	public BsinceEngineImp(HttpCache mCache,int majorThreadSize) {
		this.mCache = mCache;
		this.mController = new ResponseHandler(this);
		majorThreads = new MajorThread[majorThreadSize];
		start();
	}

	

	private void start() {
		shutdown();
		for (int i = 0; i < majorThreads.length; i++) {
			majorThreads[i] = new MajorThread( mController,majorQueue);
			majorThreads[i].start();
		}
		mCacheThread = new CacheThread(this, assistQueue, majorQueue, mCache, mController);
		mCacheThread.start();

	}

	public void cancel(final int serialNumber) {
		synchronized (mCurrentTasks) {
			for (Event<?> task : mCurrentTasks) {
				if (task.getTaskSerialNumber() == serialNumber) {
					task.cancel();
					break;
				}
			}
		}
		
	}

	public void cancel(final Object tag) {
		if (tag == null) {
			return;
		}
		cancel(new EventFilter() {

			@Override
			public boolean apply(Event<?> task) {
				return task.getTag().equals(tag);
			}
		});
	}

	public void cancel(EventFilter mFilter) {
		synchronized (mCurrentTasks) {
			for (Event<?> task : mCurrentTasks) {
				if(mFilter.apply(task)){
					task.cancel();
				}
			}
		}
	}

	public void cancelAll() {
		synchronized (mCurrentTasks) {
			for (Event<?> task : mCurrentTasks) {
				task.cancel();
			}
		}
		
	}
	
	public void finish(Event<?> task) {
		synchronized (mCurrentTasks) {
			mCurrentTasks.remove(task);
			if(HttpGlobalConfiguration.debug){
				Log.i(TAG, "[Task<number:"+task.getTaskSerialNumber()+">] be removed");
			}
		}
		
		if(task instanceof HttpEventImp){
			HttpEventImp<?> httpTask = (HttpEventImp<?>) task;
			if (httpTask.mCacheModel!=CacheModel.CACHE_NEVER) {
				synchronized (mWaitingTasks) {
					String cacheKey = httpTask.getCacheKey();
					Queue<Event<?>> waittingTasks = mWaitingTasks.remove(cacheKey);
					if (waittingTasks != null) {

						if(HttpGlobalConfiguration.debug){
							Log.e(TAG, "Releasing " + waittingTasks.size()
								+ " waiting requests for cacheKey=" + cacheKey
								+ ".");
						}
						assistQueue.addAll(waittingTasks);
					}
				}
			}	
		}

		
	}


	@Override
	public <T> T execute(Event<T> runable) {
		if(HttpGlobalConfiguration.debug){
			Log.i(TAG, "[Synchronous<Task>]"+runable.toString());
		}
		try {
			T t= runable.call().result;
			return t;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public <T> void submit(Event<T> mTask) {
		if(HttpGlobalConfiguration.debug){
			Log.i(TAG, "[Asynchronous<Task>]"+mTask.toString());
		}

		synchronized (mCurrentTasks) {
			mCurrentTasks.add(mTask);
		}
		mTask.setTaskSerialNumber(mSerialNumber.incrementAndGet());
		if(mTask instanceof HttpEventImp){
			if(((HttpEventImp<?>)mTask).mCacheModel==CacheModel.CACHE_NEVER){
				majorQueue.add(mTask);
				return;
			}
			
			synchronized (mWaitingTasks) {
				String cacheKey = ((HttpEventImp<?>)mTask).getCacheKey();
				if (mWaitingTasks.containsKey(cacheKey)) {
					Queue<Event<?>> stagedRequests = mWaitingTasks.get(cacheKey);
					if (stagedRequests == null) {
						stagedRequests = new LinkedList<Event<?>>();
					}
					stagedRequests.add(mTask);
					mWaitingTasks.put(cacheKey, stagedRequests);

				} else {
					
					mWaitingTasks.put(cacheKey, null);
					assistQueue.add(mTask);
				}
			}
		}else{
			majorQueue.add(mTask);
		}
		
	
	}

	@Override
	public synchronized void shutdown() {
		mWaitingTasks.clear();
		mCurrentTasks.clear();	
		majorQueue.clear();
		assistQueue.clear();
		if (mCacheThread != null) {
			mCacheThread.quit();
		}
		if(majorThreads==null){
			return;
		}
		for (int i = 0; i < majorThreads.length; i++) {
			if (majorThreads[i] != null) {
				majorThreads[i].quit();
			}
		}
	}



	@Override
	public int ofEventCount() {
		if(HttpGlobalConfiguration.debug){
			Log.d(TAG, "mWaitting event size:"+mWaitingTasks.size());
		}
		return mCurrentTasks.size();
	}
	
	
}
