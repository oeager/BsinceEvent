package com.developer.bsince.event;

import java.util.concurrent.Callable;

import android.util.Log;

import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.request.Priority.Sequence;
import com.developer.bsince.response.ResponseListener;
import com.developer.bsince.response.ResponseResult;

public abstract class Event<T> implements Callable<ResponseResult<T>>, Comparable<Event<T>> {

	private static final String TAG="ITask";
	
	private int taskSerialNumber;

	private int priority;

	private boolean isCanceled;

	private boolean isReceiveResult;
	
	private Object tag;
	
	public final ResponseListener<T> responseListener;
	
	

	public Event(ResponseListener<T> responseListener) {
		this.responseListener = responseListener;
	}

	@Override
	public int compareTo(Event<T> another) {
		int left = this.getPriority();
		int right = another.getPriority();
		return left == right ? this.getTaskSerialNumber() - another.getTaskSerialNumber() : right
				- left;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public int getTaskSerialNumber() {
		return taskSerialNumber;
	}

	public void setTaskSerialNumber(int taskSerialNumber) {
		this.taskSerialNumber = taskSerialNumber;
	}

	@Sequence
	public int getPriority() {
		return priority;
	}

	public void setPriority(@Sequence int priority) {
		this.priority = priority;
	}

	public void cancel() {
		isCanceled = true;
		if(HttpGlobalConfiguration.debug){
			Log.i(TAG, "[Task<number:"+getTaskSerialNumber()+">] be canceled");
		}
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void markReceiveResult() {
		this.isReceiveResult = true;
	}

	public boolean hasHadResponseDelivered() {
		return isReceiveResult;
	}
	
	public abstract void runExtras(ResponseResult<?> respon);
}
