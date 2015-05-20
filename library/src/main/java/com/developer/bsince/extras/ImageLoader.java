package com.developer.bsince.extras;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.developer.bsince.event.EventPublisher;
import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.parser.BitmapParserImp;
import com.developer.bsince.request.CacheModel;
import com.developer.bsince.response.ResponseListener;

import java.util.HashMap;
import java.util.LinkedList;


/**
 *图片加载类
 */
public class ImageLoader {

    /** 响应延时. */
    private int mBatchResponseDelayMs = 100;

    /** 图片缓存类 */
    private final ImageCache mCache;

    private final HashMap<String, BatchedImageEvent> mInFlightRequests =
            new HashMap<String, BatchedImageEvent>();


    private final HashMap<String, BatchedImageEvent> mBatchedResponses =
            new HashMap<String, BatchedImageEvent>();


    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable mRunnable;


    public interface ImageCache {
        public Bitmap getBitmap(String url);
        public void putBitmap(String url, Bitmap bitmap);
    }

    public ImageLoader(ImageCache imageCache) {
        mCache = imageCache;
    }

    public static ImageListener getImageListener(final ImageView view,
                                                 final int defaultImageResId, final int errorImageResId) {
        return new ImageListener() {
            @Override
            public void onErrorResponse(Exception error) {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                }
            }

            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.getBitmap());
                } else if (defaultImageResId != 0) {
                    view.setImageResource(defaultImageResId);
                }
            }
        };
    }

    public interface ImageListener  {
        public void onResponse(ImageContainer response, boolean isImmediate);
        public void onErrorResponse(Exception error);
    }

    public boolean isCached(String requestUrl, int maxWidth, int maxHeight) {
        throwIfNotOnMainThread();

        String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);
        return mCache.getBitmap(cacheKey) != null;
    }

    public ImageContainer get(String requestUrl, final ImageListener listener) {
        return get(requestUrl, listener, 0, 0);
    }

    public ImageContainer get(String requestUrl, ImageListener imageListener,
                              int maxWidth, int maxHeight) {
        // only fulfill requests that were initiated from the main thread.
        throwIfNotOnMainThread();

        final String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight);

        // Try to look up the request in the cache of remote images.
        Bitmap cachedBitmap = mCache.getBitmap(cacheKey);
        if (cachedBitmap != null) {
            // Return the cached bitmap.
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
            imageListener.onResponse(container, true);
            return container;
        }

        // The bitmap did not exist in the cache, fetch it!
        ImageContainer imageContainer =
                new ImageContainer(null, requestUrl, cacheKey, imageListener);

        // Update the caller to let them know that they should use the default bitmap.
        imageListener.onResponse(imageContainer, true);

        // Check to see if a request is already in-flight.
        BatchedImageEvent request = mInFlightRequests.get(cacheKey);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.addContainer(imageContainer);
            return imageContainer;
        }

        // The request is not already in flight. Send the new request to the network and
        // track it.
        HttpEventImp<?> newRequest = EventPublisher.connect(Bitmap.class,requestUrl)
                .parser(new BitmapParserImp())
                .cacheMode(CacheModel.CACHE_FOREVER)
                .bitmapExtras(maxWidth, maxHeight, Config.RGB_565)
                .submit(new ResponseListener<Bitmap>() {
                    @Override
                    public void onSuccessResponse(Bitmap response) {
                        onGetImageSuccess(cacheKey, response);
                    }

                    @Override
                    public void onErrorResponse(Exception error) {
                        onGetImageError(cacheKey, error);
                    }
                });

        mInFlightRequests.put(cacheKey,
                new BatchedImageEvent(newRequest, imageContainer));
        return imageContainer;
    }

    /**
     * Sets the amount of time to wait after the first response arrives before delivering all
     * responses. Batching can be disabled entirely by passing in 0.
     * @param newBatchedResponseDelayMs The time in milliseconds to wait.
     */
    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }

    /**
     * Handler for when an image was successfully loaded.
     * @param cacheKey The cache key that is associated with the image request.
     * @param response The bitmap that was returned from the network.
     */
    private void onGetImageSuccess(String cacheKey, Bitmap response) {
        // cache the image that was fetched.
        mCache.putBitmap(cacheKey, response);

        // remove the request from the list of in-flight requests.
        BatchedImageEvent request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Update the response bitmap.
            request.mResponseBitmap = response;

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Handler for when an image failed to load.
     * @param cacheKey The cache key that is associated with the image request.
     */
    private void onGetImageError(String cacheKey, Exception error) {
        // Notify the requesters that something failed via a null result.
        // Remove this request from the list of in-flight requests.
        BatchedImageEvent request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Set the error for this request
            request.setError(error);

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Container object for all of the data surrounding an image request.
     */
    public class ImageContainer {
        /**
         * The most relevant bitmap for the container. If the image was in cache, the
         * Holder to use for the final bitmap (the one that pairs to the requested URL).
         */
        private Bitmap mBitmap;

        private final ImageListener mListener;

        /** The cache key that was associated with the request */
        private final String mCacheKey;

        /** The request URL that was specified */
        private final String mEventUrl;

        /**
         * Constructs a BitmapContainer object.
         * @param bitmap The final bitmap (if it exists).
         * @param requestUrl The requested URL for this container.
         * @param cacheKey The cache key that identifies the requested URL for this container.
         */
        public ImageContainer(Bitmap bitmap, String requestUrl,
                              String cacheKey, ImageListener listener) {
            mBitmap = bitmap;
            mEventUrl = requestUrl;
            mCacheKey = cacheKey;
            mListener = listener;
        }

        /**
         * Releases interest in the in-flight request (and cancels it if no one else is listening).
         */
        public void cancelRequest() {
            if (mListener == null) {
                return;
            }

            BatchedImageEvent request = mInFlightRequests.get(mCacheKey);
            if (request != null) {
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                if (canceled) {
                    mInFlightRequests.remove(mCacheKey);
                }
            } else {
                // check to see if it is already batched for delivery.
                request = mBatchedResponses.get(mCacheKey);
                if (request != null) {
                    request.removeContainerAndCancelIfNecessary(this);
                    if (request.mContainers.size() == 0) {
                        mBatchedResponses.remove(mCacheKey);
                    }
                }
            }
        }

        /**
         * Returns the bitmap associated with the request URL if it has been loaded, null otherwise.
         */
        public Bitmap getBitmap() {
            return mBitmap;
        }

        /**
         * Returns the requested URL for this container.
         */
        public String getRequestUrl() {
            return mEventUrl;
        }
    }

    /**
     * Wrapper class used to map a Request to the set of active ImageContainer objects that are
     * interested in its results.
     */
    private class BatchedImageEvent {
        /** The request being tracked */
        private final HttpEventImp<?> mEvent;

        /** The result of the request being tracked by this item */
        private Bitmap mResponseBitmap;

        /** Error if one occurred for this response */
        private Exception mError;

        /** List of all of the active ImageContainers that are interested in the request */
        private final LinkedList<ImageContainer> mContainers = new LinkedList<ImageContainer>();

        /**
         * Constructs a new BatchedImageRequest object
         * @param request The request being tracked
         * @param container The ImageContainer of the person who initiated the request.
         */
        public BatchedImageEvent(HttpEventImp<?> request, ImageContainer container) {
            mEvent = request;
            mContainers.add(container);
        }

        /**
         * Set the error for this response
         */
        public void setError(Exception error) {
            mError = error;
        }

        /**
         * Get the error for this response
         */
        public Exception getError() {
            return mError;
        }

        /**
         * Adds another ImageContainer to the list of those interested in the results of
         * the request.
         */
        public void addContainer(ImageContainer container) {
            mContainers.add(container);
        }

        /**
         * Detatches the bitmap container from the request and cancels the request if no one is
         * left listening.
         * @param container The container to remove from the list
         * @return True if the request was canceled, false otherwise.
         */
        public boolean removeContainerAndCancelIfNecessary(ImageContainer container) {
            mContainers.remove(container);
            if (mContainers.size() == 0) {
                mEvent.cancel();
                return true;
            }
            return false;
        }
    }

    /**
     * Starts the runnable for batched delivery of responses if it is not already started.
     * @param cacheKey The cacheKey of the response being delivered.
     * @param request The BatchedImageRequest to be delivered.
     */
    private void batchResponse(String cacheKey, BatchedImageEvent request) {
        mBatchedResponses.put(cacheKey, request);
        // If we don't already have a batch delivery runnable in flight, make a new one.
        // Note that this will be used to deliver responses to all callers in mBatchedResponses.
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for (BatchedImageEvent bir : mBatchedResponses.values()) {
                        for (ImageContainer container : bir.mContainers) {
                            // If one of the callers in the batched request canceled the request
                            // after the response was received but before it was delivered,
                            // skip them.
                            if (container.mListener == null) {
                                continue;
                            }
                            if (bir.getError() == null) {
                                container.mBitmap = bir.mResponseBitmap;
                                container.mListener.onResponse(container, false);
                            } else {
                                container.mListener.onErrorResponse(bir.getError());
                            }
                        }
                    }
                    mBatchedResponses.clear();
                    mRunnable = null;
                }

            };
            // Post the runnable.
            mHandler.postDelayed(mRunnable, mBatchResponseDelayMs);
        }
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }
    /**
     * Creates a cache key for use with the L1 cache.
     * @param url The URL of the request.
     * @param maxWidth The max-width of the output.
     * @param maxHeight The max-height of the output.
     */
    private static String getCacheKey(String url, int maxWidth, int maxHeight) {
//        return ;

        return String.valueOf(new StringBuilder(url.length() + 12).append("#W").append(maxWidth)
                .append("#H").append(maxHeight).append(url).toString().hashCode());
    }
}
