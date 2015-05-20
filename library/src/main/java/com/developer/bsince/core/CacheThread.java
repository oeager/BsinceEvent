package com.developer.bsince.core;

import java.util.concurrent.BlockingQueue;

import com.developer.bsince.cache.HttpCache;
import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.event.Event;
import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.response.NetworkResponse;
import com.developer.bsince.response.ResponseController;
import com.developer.bsince.response.ResponseResult;

import android.os.Process;
import android.util.Log;


public class CacheThread extends Thread {

	private final BlockingQueue<Event<?>> mAssisQueue;

	private final BlockingQueue<Event<?>> mMajorQueue;

	private final HttpCache mCache;

	private final ResponseController mController;

    private final IEngine engine;

	private volatile boolean mQuit = false;
	
	private final String TAG ="CacheThread";

	public CacheThread(IEngine engine,BlockingQueue<Event<?>> assisQueue,
			BlockingQueue<Event<?>> majorQueue, HttpCache cache,
			ResponseController delivery) {
        this.engine = engine;
        mAssisQueue = assisQueue;
        mMajorQueue = majorQueue;
		mCache = cache;
		mController = delivery;
	}

	public void quit() {
		mQuit = true;
		interrupt();
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		mCache.initialize();
		while (true) {
			try {
				Event<?> task = mAssisQueue.take();
				if (task.isCanceled()) {
                    engine.finish(task);
					continue;
				}

				if(!(task instanceof HttpEventImp)){
					engine.finish(task);
					continue;
				}
				final HttpEventImp<?> httpTask = (HttpEventImp<?>) task;
				HttpCache.Entry entry = mCache.get(httpTask.getCacheKey());
				if (entry == null) {
					mMajorQueue.put(task);
					if(HttpGlobalConfiguration.debug){
						Log.d(TAG, "cache is null,put in networkQueen");
					}
					continue;
				}
				
				if(CacheModel.mustCache(httpTask.mCacheModel)){
					ResponseResult<?> responseResult = httpTask
							.parseNetworkResponse(new NetworkResponse(entry.data,
									entry.responseHeaders));
					if(HttpGlobalConfiguration.debug){
						Log.d(TAG, "find cache,cachemodel is cacheforever ,response it");
					}
					mController.postResponse(httpTask, responseResult);
					
				}else{
					if (entry.isExpired()) {
						httpTask.setCacheEntry(entry);
						if(HttpGlobalConfiguration.debug){
							Log.d(TAG, "cache is expired,put in networkqueen");
						}
						mMajorQueue.put(httpTask);
						continue;
					}
					ResponseResult<?> responseResult = httpTask
							.parseNetworkResponse(new NetworkResponse(entry.data,
									entry.responseHeaders));

					if (!entry.refreshNeeded()) {
						//不需要刷新，就直接返回结果
						if(HttpGlobalConfiguration.debug){
							Log.d(TAG, "cache need not refresh,response it");
						}
						mController.postResponse(httpTask, responseResult);
					} else {
						if(HttpGlobalConfiguration.debug){
							Log.d(TAG, "cache need refresh,response and request by network too");
						}
						//我们可以提供缓存响应，但我们也需要发送请求到网络刷新。
						httpTask.setCacheEntry(entry);

						// 标记响应
						responseResult.intermediate = true;

						//post结果
						mController.postResponse(httpTask, responseResult,
								new Runnable() {
									@Override
									public void run() {
										try {
											mMajorQueue.put(httpTask);
										} catch (InterruptedException e) {
										}
									}
								});
					}
				}

			} catch (InterruptedException e) {
				if (mQuit) {
					return;
				}
				continue;
			}
		}
	}
}
