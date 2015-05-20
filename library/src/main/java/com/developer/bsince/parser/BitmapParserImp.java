package com.developer.bsince.parser;

import java.io.InputStream;

import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.exceptions.ParseException;
import com.developer.bsince.response.NetworkResponse;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

public class BitmapParserImp implements IParser<Bitmap> {

	private static final Object sDecodeLock = new Object();

	public BitmapParserImp(){}
	
	public static final BitmapParserImp INSTANCE = new BitmapParserImp();
	
	@Override
	public Bitmap parsed(NetworkResponse response, HttpEventImp<Bitmap> task)
			throws ParseException {
		synchronized (sDecodeLock) {
			try {
				 int mMaxWidth;
				 int mMaxHeight;
				 Config mDecodeConfig;
				Object [] extra = task.extra;
				if(extra!=null&&extra.length>2){
					mMaxWidth = (int) extra[0];
					mMaxHeight = (int)extra[1];
					mDecodeConfig = (Config) extra[2];
					
				}else{
					mMaxWidth = 0;
					mMaxHeight = 0;
					mDecodeConfig = Config.RGB_565;
				}
				if(response.data!=null){
					return doParse(response.data,mMaxWidth,mMaxHeight,mDecodeConfig);
				}else if (response.ioData!=null){
					return doParse(response.ioData,mMaxWidth,mMaxHeight,mDecodeConfig);
				}else{
					throw new ParseException("[the inputstrem and the byte [] data are both empty]");
				}
			} catch (OutOfMemoryError e) {

				throw e;
			}
		}
	}
	
	private Bitmap doParse(InputStream is,int mMaxWidth,int mMaxHeight,Config mDecodeConfig)throws ParseException{
		BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		Bitmap bitmap = null;
		if (mMaxWidth == 0 && mMaxHeight == 0) {
			decodeOptions.inPreferredConfig = mDecodeConfig;
			bitmap = BitmapFactory.decodeStream(is, null, decodeOptions);
		} else {
			// If we have to resize this image, first get the natural bounds.
			decodeOptions.inJustDecodeBounds = true;
			 BitmapFactory.decodeStream(is, null, decodeOptions);
			int actualWidth = decodeOptions.outWidth;
			int actualHeight = decodeOptions.outHeight;

			// Then compute the dimensions we would ideally like to decode to.
			int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
					actualWidth, actualHeight);
			int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
					actualHeight, actualWidth);

			
			decodeOptions.inJustDecodeBounds = false;
		
			decodeOptions.inSampleSize = findBestSampleSize(actualWidth,
					actualHeight, desiredWidth, desiredHeight);
			Bitmap tempBitmap =  BitmapFactory.decodeStream(is, null, decodeOptions);

			if (tempBitmap != null
					&& (tempBitmap.getWidth() > desiredWidth || tempBitmap
							.getHeight() > desiredHeight)) {
				bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth,
						desiredHeight, true);
				tempBitmap.recycle();
			} else {
				bitmap = tempBitmap;
			}
		}
		if (bitmap == null) {
			throw new ParseException(
					"parse image erro ,because of the bitmap is null");
		} else {
			return bitmap;
		}
	
	
	}
	
	private Bitmap doParse(byte [] data,int mMaxWidth,int mMaxHeight,Config mDecodeConfig)throws ParseException{
		BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		Bitmap bitmap = null;
		if (mMaxWidth == 0 && mMaxHeight == 0) {
			decodeOptions.inPreferredConfig = mDecodeConfig;
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
					decodeOptions);
		} else {
			// If we have to resize this image, first get the natural bounds.
			decodeOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
			int actualWidth = decodeOptions.outWidth;
			int actualHeight = decodeOptions.outHeight;

			// Then compute the dimensions we would ideally like to decode to.
			int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
					actualWidth, actualHeight);
			int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
					actualHeight, actualWidth);

			// Decode to the nearest power of two scaling factor.
			decodeOptions.inJustDecodeBounds = false;
			// TODO(ficus): Do we need this or is it okay since API 8 doesn't
			// support it?
			// decodeOptions.inPreferQualityOverSpeed =
			// PREFER_QUALITY_OVER_SPEED;
			decodeOptions.inSampleSize = findBestSampleSize(actualWidth,
					actualHeight, desiredWidth, desiredHeight);
			Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0,
					data.length, decodeOptions);

			// If necessary, scale down to the maximal acceptable size.
			if (tempBitmap != null
					&& (tempBitmap.getWidth() > desiredWidth || tempBitmap
							.getHeight() > desiredHeight)) {
				bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth,
						desiredHeight, true);
				tempBitmap.recycle();
			} else {
				bitmap = tempBitmap;
			}
		}

		if (bitmap == null) {
			throw new ParseException(
					"parse image erro ,because of the bitmap is null");
		} else {
			return bitmap;
		}
	
	}

	

	private static int getResizedDimension(int maxPrimary, int maxSecondary,
			int actualPrimary, int actualSecondary) {
		// 如果最大值为空，返回实际值
		if (maxPrimary == 0 && maxSecondary == 0) {
			return actualPrimary;
		}
		// If primary is unspecified, scale primary to match secondary's scaling
		// ratio.
		if (maxPrimary == 0) {
			double ratio = (double) maxSecondary / (double) actualSecondary;
			return (int) (actualPrimary * ratio);
		}

		if (maxSecondary == 0) {
			return maxPrimary;
		}

		double ratio = (double) actualSecondary / (double) actualPrimary;
		int resized = maxPrimary;
		if (resized * ratio > maxSecondary) {
			resized = (int) (maxSecondary / ratio);
		}
		return resized;
	}

	/**
	 * Returns the largest power-of-two divisor for use in downscaling a bitmap
	 * that will not result in the scaling past the desired dimensions.
	 * 
	 * @param actualWidth
	 *            Actual width of the bitmap
	 * @param actualHeight
	 *            Actual height of the bitmap
	 * @param desiredWidth
	 *            Desired width of the bitmap
	 * @param desiredHeight
	 *            Desired height of the bitmap
	 */
	// Visible for testing.
	static int findBestSampleSize(int actualWidth, int actualHeight,
			int desiredWidth, int desiredHeight) {
		double wr = (double) actualWidth / desiredWidth;
		double hr = (double) actualHeight / desiredHeight;
		double ratio = Math.min(wr, hr);
		float n = 1.0f;
		while ((n * 2) <= ratio) {
			n *= 2;
		}

		return (int) n;
	}

}
