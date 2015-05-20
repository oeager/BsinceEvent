package com.developer.bsince.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.developer.bsince.extras.ImageLoader.ImageCache;



public class LRUMemoryCache extends  LruCache<String, Bitmap> implements ImageCache {

	public LRUMemoryCache(int maxSize) {
		super(maxSize);
	}


	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight();
	}
	
	@Override
	public Bitmap getBitmap(String url) {
		return get(url);
	}
 
	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		put(url, bitmap);
		
	}
	
	
}
