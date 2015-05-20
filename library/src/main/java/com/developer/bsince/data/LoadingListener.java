package com.developer.bsince.data;

public interface LoadingListener {

	void onLoadingStarted();
	
	void onLoadProgressChanged(int currentSize, int totalSize);
	
	void onLoadingError(Exception e);
	
	void onLoadingEnd(boolean complete, Object... args);
}
