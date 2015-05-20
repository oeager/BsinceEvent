package com.developer.bsince.cache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.graphics.Bitmap;

import com.developer.bsince.extras.ImageLoader.ImageCache;


public class WeakMemoryCache implements ImageCache {

	private final  Map<String, WeakReference<Bitmap>> softMap = Collections.synchronizedMap(new HashMap<String, WeakReference<Bitmap>>());
	@Override
	public void putBitmap(String key, Bitmap bitmap) {
		softMap.put(key, createReference(bitmap));
	}

	protected WeakReference<Bitmap> createReference(Bitmap value) {
		return new WeakReference<Bitmap>(value);
	}

	@Override
	public Bitmap getBitmap(String key) {
		Bitmap result = null;
		Reference<Bitmap> reference = softMap.get(key);
		if (reference != null) {
			result = reference.get();
		}
		return result;
	}

	public void remove(String key) {
		softMap.remove(key);
	}

	public Collection<String> keys() {
		synchronized (softMap) {
			return new HashSet<String>(softMap.keySet());
		}
	}

	public void clear() {
		softMap.clear();
	}

}
