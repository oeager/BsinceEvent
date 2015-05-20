package com.developer.bsince.event;

import com.developer.bsince.response.ResponseListener;
import com.developer.bsince.response.ResponseResult;

public abstract class AbstractEvent<T> extends Event<T> {

	public AbstractEvent(){
		super(null);
	}
	public AbstractEvent(ResponseListener<T> responseListener) {
		super(responseListener);
	}

	@Override
	public ResponseResult<T> call() throws Exception {
		T t = run();
		return ResponseResult.success(t);
	}
	
	public abstract T run()throws Exception;

	@Override
	public void runExtras(ResponseResult<?> respon) {
		
	}
	
	public void submit(){
		EventPublisher.submit(this);
	}
	public T execute(){
		return EventPublisher.execute(this);
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("----AbstractTask{");
		sb.append("[tag:"+getTag()+"]}");
		
		return sb.toString();
	}

}
