package com.developer.bsince.response;
import com.developer.bsince.core.IEngine;
import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.event.Event;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ResponseHandler implements ResponseController{

	private static final String TAG="ResponseHandler";
	private final IEngine mEngine;
	
	public ResponseHandler (IEngine mEngine){
		this.mEngine = mEngine;
	}
	
	private final Handler responseHandle = new Handler(Looper.getMainLooper());
	
	
	@Override
	public void postExpireTask(Event<?> task) {
		this.mEngine.finish(task);
	}

	@Override
	public void postResponse(Event<?> task, ResponseResult<?> responseResult) {
		postResponse(task, responseResult, null);
	}

	@Override
	public void postResponse(Event<?> task, ResponseResult<?> responseResult,
			Runnable runnable) {
		task.markReceiveResult();
		responseHandle.post(new ResponseRunnable(task, responseResult, runnable));
	}

	@Override
	public void postError(Event<?> task, Exception error) {
		responseHandle.post(new ResponseRunnable(task, ResponseResult.error(error), null));
	}
	
	private class ResponseRunnable implements Runnable {

		@SuppressWarnings("rawtypes")
		private final Event task;
		private final ResponseResult<?> mResponseResult;
		private final Runnable mRunnable;

		public ResponseRunnable(Event<?> task, ResponseResult<?> response,
				Runnable runnable) {
			this.task = task;
			mResponseResult = response;
			mRunnable = runnable;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {

			if (task.isCanceled()) {
				if(HttpGlobalConfiguration.debug){
					Log.d(TAG, "cancel event,last size :"+mEngine.ofEventCount());
				}
                mEngine.finish(task);
				return;
			}

			if(task.responseListener!=null){
				if (mResponseResult.isSuccess()) {
					task.responseListener.onSuccessResponse(mResponseResult.result);
				} else {
					task.responseListener.onErrorResponse(mResponseResult.error);
				}
			}
			

			if (mResponseResult.intermediate) {
				if(HttpGlobalConfiguration.debug){
					Log.d(TAG, "the task intermediate");
				}
			} else {
				
                mEngine.finish(task);
                if(HttpGlobalConfiguration.debug){
					Log.d(TAG, "done,last size :"+mEngine.ofEventCount());
				}
			}

			if (mRunnable != null) {
				mRunnable.run();
			}

		}

	}

}
