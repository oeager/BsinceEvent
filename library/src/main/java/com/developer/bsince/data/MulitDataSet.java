package com.developer.bsince.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.developer.bsince.core.assist.HttpConstants;
import com.developer.bsince.extras.IOHelper;

public class MulitDataSet extends NameValueDataSet {

	private final static String boundary = "---------------------------7da2137580612";
	private final byte[] boundaryLine;
	private final byte[] boundaryEnd;
	private static final String STR_CR_LF = "\r\n";
	private static final byte[] CR_LF = STR_CR_LF.getBytes();
	private static final byte[] TRANSFER_ENCODING_BINARY = ("Content-Transfer-Encoding: binary" + STR_CR_LF)
			.getBytes();
	protected final Map<String, StreamWrapper> streamParams;

	protected final Map<String, FileWrapper> fileParams;

	private ProgressChangeListener mLoadingListener;
	
	private int totalSize;
	
	private Handler mHandler;
	
	private boolean needProcess = false;
	
	
	private static final int DOWNLOAD_DOWNLOADING = 1;
	

	public MulitDataSet() {
		streamParams = createDataSetCollections();
		fileParams = createDataSetCollections();
		boundaryLine = ("--" + boundary + "\r\n").getBytes();
		boundaryEnd = ("--" + boundary + "--\r\n").getBytes();

	}

	public void setProgressChangeListener(ProgressChangeListener listener) {
		this.mLoadingListener = listener;
	}

	protected void initLoadingDataIfNeeded(Map<String, String> keyValuePairs) {
		if (mLoadingListener == null) {
			return;
		}
		if (fileParams.isEmpty()) {
			return;
		}
		needProcess = true;
		totalSize = calcLengthWithoutStreams(keyValuePairs);
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				
				case DOWNLOAD_DOWNLOADING:
					Object[] data = (Object[]) msg.obj;
					mLoadingListener.onLoadProgressChanged((int) data[0],
							(int) data[1]);
					break;
				
				}
			}
		};

	}
	
	public void postMessage(int currentSize){
		if(needProcess){
			mHandler.sendMessage(mHandler.obtainMessage(DOWNLOAD_DOWNLOADING, new Object[]{currentSize,totalSize}));
		}
	}

	@Override
	protected void init() {
		setContentType(HttpConstants.CONTENT_TYPE_MULTIPART);
		setCharset(HttpConstants.DEFAULT_CHARSET);
	}

	public void put(String key, File file) throws FileNotFoundException {
		put(key, file, null);
	}

	public void put(String key, File file, String contentType)
			throws FileNotFoundException {
		if (file == null || !file.exists()) {
			throw new FileNotFoundException();
		}
		if (key != null) {
			fileParams.put(key, new FileWrapper(file, contentType));
		}
	}

	public void put(String key, InputStream stream) {
		put(key, stream, null);
	}

	public void put(String key, InputStream stream, String name) {
		put(key, stream, name, null);
	}

	public void put(String key, InputStream stream, String name,
			String contentType) {
		if (key != null && stream != null) {
			streamParams.put(key,
					StreamWrapper.newInstance(stream, name, contentType));
		}
	}

	/**
	 * because we can not get the length of the inputstream,so we just don't
	 * calculate its lenght;
	 * 
	 * @see InputStream
	 */
	@Override
	public void write(OutputStream out) throws IOException {
		Map<String, String> keyValuePairs = convertToMap(null, params);
		
		initLoadingDataIfNeeded(keyValuePairs);
		int currentSize = 0;
		for (Map.Entry<String, String> entry : keyValuePairs.entrySet()) {
			out.write(boundaryLine);
			byte [] contentDispostion = createContentDisposition(entry.getKey());
			out.write(contentDispostion);
			byte [] contentType = createContentType(HttpConstants.CONTENT_TYPE_PLAIN);
			out.write(contentType);
			out.write(CR_LF);
			out.write(entry.getValue().getBytes());
			out.write(CR_LF);
			currentSize+=(boundaryLine.length+contentDispostion.length+contentType.length+CR_LF.length*2+entry.getValue().getBytes().length);
			postMessage(currentSize);
		}

		for (Map.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
			FileWrapper fw = entry.getValue();
			out.write(boundaryLine);
			byte [] contentDispostion = createContentDisposition(entry.getKey(),
					fw.customFileName);
			out.write(contentDispostion);
			byte[] contentType = createContentType(fw.contentType);
			out.write(contentType);
			out.write(TRANSFER_ENCODING_BINARY);
			out.write(CR_LF);
			currentSize+=(boundaryLine.length+contentDispostion.length+contentType.length+TRANSFER_ENCODING_BINARY.length+CR_LF.length);
			postMessage(currentSize);
			FileInputStream inputStream = new FileInputStream(fw.file);
			final byte[] tmp = new byte[4096];
			int l;
			while ((l = inputStream.read(tmp)) != -1) {
				out.write(tmp, 0, l);
				currentSize+=tmp.length;
				postMessage(currentSize);
			}
			out.write(CR_LF);
			currentSize+=CR_LF.length;
			postMessage(currentSize);
			out.flush();
			IOHelper.silentCloseInputStream(inputStream);
		}

		for (Map.Entry<String, StreamWrapper> entry : streamParams.entrySet()) {
			StreamWrapper sw = entry.getValue();
			out.write(boundaryLine);
			// Headers
			out.write(createContentDisposition(entry.getKey(), sw.name));
			out.write(createContentType(sw.contentType));
			out.write(TRANSFER_ENCODING_BINARY);
			out.write(CR_LF);

			// Stream (file)
			InputStream inputStream = sw.inputStream;

			final byte[] tmp = new byte[4096];
			int l;
			while ((l = inputStream.read(tmp)) != -1) {
				out.write(tmp, 0, l);
			}

			out.write(CR_LF);
			out.flush();
			IOHelper.silentCloseInputStream(inputStream);

		}
		out.write(boundaryEnd);
		currentSize+=boundaryEnd.length;
		postMessage(currentSize);

	}

	private int calcLengthWithoutStreams(Map<String, String> keyValuePairs) {
		int size = 0;
		for (Map.Entry<String, String> entry : keyValuePairs.entrySet()) {
			size += boundaryLine.length;
			size += createContentDisposition(entry.getKey()).length;
			size += createContentType(HttpConstants.CONTENT_TYPE_PLAIN).length;
			size += CR_LF.length;
			size += entry.getValue().getBytes().length;
			size += CR_LF.length;
		}
		for (Map.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
			FileWrapper fw = entry.getValue();
			size += boundaryLine.length;
			size += createContentDisposition(entry.getKey(), fw.customFileName).length;
			size += createContentType(fw.contentType).length;
			size += TRANSFER_ENCODING_BINARY.length;
			size += CR_LF.length;
			long len = fw.file.length();
			if(len%1024!=0)len = (len/1024+1)*1024;
			size += len;
			size +=CR_LF.length;
		}
		size += boundaryEnd.length;
		return size;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private byte[] createContentType(String type) {
		String result = HttpConstants.HEADER_CONTENT_TYPE + ": " + type
				+ "; charset=" + getCharset() + STR_CR_LF;
		return result.getBytes();
	}

	private byte[] createContentDisposition(final String key) {
		return (HttpConstants.HEADER_CONTENT_DISPOSITION
				+ ": form-data; name=\"" + key + "\"" + STR_CR_LF).getBytes();
	}

	private byte[] createContentDisposition(final String key,
			final String fileName) {
		return (HttpConstants.HEADER_CONTENT_DISPOSITION
				+ ": form-data; name=\"" + key + "\"" + "; filename=\""
				+ fileName + "\"" + STR_CR_LF).getBytes();
	}

	public static class FileWrapper {
		public final File file;
		public String paramName;
		public final String contentType;
		public final String customFileName;

		public FileWrapper(File file, String contentType, String customFileName) {
			this.file = file;
			this.contentType = contentType == null ? HttpConstants.CONTENT_TYPE_OCTET_STREAM
					: contentType;
			this.customFileName = customFileName;

		}

		public FileWrapper(File file, String contentType) {
			this.file = file;
			this.contentType = contentType == null ? HttpConstants.CONTENT_TYPE_OCTET_STREAM
					: contentType;
			this.customFileName = file.getName();
		}

		@Override
		public String toString() {
			return "FILE_WRAPER:" + customFileName;
		}
	}

	public static class StreamWrapper {
		public final InputStream inputStream;
		public final String name;
		public final String contentType;

		public StreamWrapper(InputStream inputStream, String name,
				String contentType) {
			this.inputStream = inputStream;
			this.name = name;
			this.contentType = contentType == null ? HttpConstants.CONTENT_TYPE_OCTET_STREAM
					: contentType;
		}

		static StreamWrapper newInstance(InputStream inputStream, String name,
				String contentType) {
			return new StreamWrapper(inputStream, name, contentType);
		}

		@Override
		public String toString() {
			return "STREAM_WRAPPER:" + name;
		}
	}

	@Override
	public String getContentType() {
		return HttpConstants.CONTENT_TYPE_MULTIPART + "; boundary=" + boundary;
	}

	@Override
	public boolean isChunkedStreamingMode() {
		return false;
	}
}
