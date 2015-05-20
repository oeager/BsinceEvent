package com.developer.bsince.response;




public class ResponseResult<T> {


	

	public static <T> ResponseResult<T> success(T result) {
		return new ResponseResult<T>(result);
	}

	public static <T> ResponseResult<T> error(Exception error) {
		return new ResponseResult<T>(error);
	}

	public final T result;


	/** 详细的错误信息 <code>errorCode != OK</code>. */
	public final Exception error;

	/** 如果true表示 响应已经过期 */
	public boolean intermediate = false;
	

	/**
	 * 返回是否请求成功
	 */
	public boolean isSuccess() {
		return error == null;
	}

	private ResponseResult(T result) {
		this.result = result;
		this.error = null;
	}

	private ResponseResult(Exception error) {
		this.result = null;
		this.error = error;
	}

}
