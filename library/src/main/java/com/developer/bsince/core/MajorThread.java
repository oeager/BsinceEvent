package com.developer.bsince.core;

import java.util.concurrent.BlockingQueue;

import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.event.Event;
import com.developer.bsince.response.ResponseController;
import com.developer.bsince.response.ResponseResult;

import android.os.Process;
import android.util.Log;

public class MajorThread extends Thread {

	private final ResponseController mController;
	private final BlockingQueue<Event<?>> mQueue;
	private volatile boolean mQuit = false;
	private final String TAG = "MajorThread";
	public MajorThread(ResponseController mController,BlockingQueue<Event<?>> mQueue){
		this.mQueue = mQueue;
		this.mController = mController;
	}
	public void quit() {
		mQuit = true;
		interrupt();
	}
	@Override
	public  void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		Event<?> callableTask;
		while(true){

			// 将event从队列取出
			try {

				callableTask = mQueue.take();
			} catch (InterruptedException e) {
				// 因为可以quit的原因，所以有可能中断
				if (mQuit) {
					return;
				}
				continue;
			}
			if(callableTask.isCanceled()){
				mController.postExpireTask(callableTask);
				continue;
			}
			try {
				ResponseResult<?> result =callableTask.call();
				if(result==null){//返回Null只存在于当前任务过期时
					if(HttpGlobalConfiguration.debug){
						Log.d(TAG, "response it yet,and need not refresh,remove it");
					}
					mController.postExpireTask(callableTask);
					continue;
				}
				callableTask.runExtras(result);
				if(HttpGlobalConfiguration.debug){
					Log.d(TAG, "response it by network");
				}
				mController.postResponse(callableTask, result);
				
			} catch (Exception e) {
				mController.postError(callableTask, e);
			}
			
			
		
		}
	}
}
