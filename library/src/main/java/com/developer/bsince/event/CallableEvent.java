package com.developer.bsince.event;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.developer.bsince.request.Priority.Sequence;
import com.developer.bsince.response.ResponseListener;
import com.developer.bsince.response.ResponseResult;

public class CallableEvent<T> extends Event<T> {

	
	int retryCount;

	long executeTime;

	long delayTime;

	Callable<T> callable;

	public CallableEvent(Callable<T> callable) {
		this(callable,null);
	}
	public CallableEvent(Callable<T> callable,ResponseListener<T> listener) {
		super(listener);
		this.callable = callable;
	}

	@Override
	public ResponseResult<T> call() throws Exception {
		if (delayTime > 0) {
			Thread.sleep(delayTime);
		}
		int executeCount = 0;
		Exception ex = null;
		long startTime =System.currentTimeMillis();
		if(executeTime>0){
			FutureTask<T> f = new FutureTask<>(callable);
		
			T t = null;
			
			while(t==null&&executeCount<=retryCount){
				try {
					t=f.get(executeTime-(System.currentTimeMillis()-startTime), TimeUnit.MILLISECONDS);
					if(t!=null){
						return ResponseResult.success(t);
					}
				} catch (Exception e) {
					ex = e;
				}
			}
			return ResponseResult.error(ex);
			
		}else{
			while (retryCount >=executeCount) {
				executeCount++;
				try {
					return ResponseResult.success(callable.call());
				} catch (Exception e) {
					ex = e;
				}

			}
			return ResponseResult.error(ex);
		}
		
	}
	
	

	public static class Builder {

		Object mTag;

		int priority;

		int retryCount;

		long executeTime;

		long delayTime;
		
		public Builder tag(Object tag) {
			mTag = tag;
			return this;
		}

		public Builder priority(@Sequence int priority) {
			this.priority = priority;
			return this;
		}

		public Builder retryCount(int retryCount) {
			this.retryCount = retryCount;
			return this;
		}

		public Builder executeTime(long time) {
			this.executeTime = time;
			return this;
		}

		public Builder delayTime(long time) {
			this.delayTime = time;
			return this;
		}
		

		public <T> T execute(Callable<T> callale) {
			CallableEvent<T> task = new CallableEvent<T>(callale);
			task.setTag(mTag);
			task.setPriority(priority);
			task.retryCount = retryCount;
			task.delayTime = delayTime;
			task.executeTime = executeTime;
			return EventPublisher.execute(task);
		}
		public <T> CallableEvent<T> submit(Callable<T> callale,ResponseListener<T> response) {
			CallableEvent<T> task = new CallableEvent<T>(callale,response);
			task.setTag(mTag);
			task.setPriority(priority);
			task.retryCount = retryCount;
			task.delayTime = delayTime;
			task.executeTime = executeTime;
			EventPublisher.submit(task);
			return task;
		}
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("----CallableTask{");
		sb.append("[tag:"+getTag()+"],");
		sb.append("[retryCount:"+retryCount+"],");
		sb.append("[executeTime:"+executeTime+"],");
		sb.append("[delayTime:"+delayTime+"],}");
		return sb.toString();
	}

	@Override
	public void runExtras(ResponseResult<?> respon) {

	}

}
