package com.developer.bsince.response;

import com.developer.bsince.event.Event;

public interface ResponseController {

	public void postExpireTask(Event<?> task);

	public void postResponse(Event<?> task, ResponseResult<?> responseResult);

	public void postResponse(Event<?> task, ResponseResult<?> responseResul,
							 Runnable runnable);

	public void postError(Event<?> task, Exception error);

	
}
