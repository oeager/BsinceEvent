package com.developer.bsince.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.developer.bsince.data.LoadingListener;
import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.exceptions.ParseException;
import com.developer.bsince.extras.IOHelper;
import com.developer.bsince.response.NetworkResponse;

public class FileDownloadParser implements IParser<File> {

	private final static int START = 0x1;
	private final static int PROGRESS = 0x2;
	private final static int ERROR = 0x3;
	private final static int END = 0x4;
	private LoadingListener loadingListener;
	private final String filePath;
	private final String fileName;

	public  FileDownloadParser(String filePath,String fileName){
		this.filePath = filePath;
		this.fileName = fileName;
	}
	
	
	private final Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case START:
				if(loadingListener!=null){
					loadingListener.onLoadingStarted();
				}
				break;

			case PROGRESS:
				if(loadingListener!=null){
					int [] data = (int[]) msg.obj;
					loadingListener.onLoadProgressChanged(data[0], data[1]);
				}
				break;
			case ERROR:
				if(loadingListener!=null){
					loadingListener.onLoadingError((Exception)msg.obj);
				}
				break;
			case END:
				if(loadingListener!=null){
					loadingListener.onLoadingEnd((boolean)msg.obj);
				}
				break;
			}
		};
	};

	public void setLoadingListener(LoadingListener loadingListener) {
		this.loadingListener = loadingListener;
	}


	private void checkParams(String fileName,String filePath) throws ParseException {
		if (TextUtils.isEmpty(fileName)) {
			throw new ParseException("fileName is null[" + fileName + "]");
		}
		if (TextUtils.isEmpty(filePath)) {
			throw new ParseException("filePath is null[" + filePath + "]");
		}
		File directory = new File(filePath);

		if (!directory.exists()) {
			directory.mkdirs();
		}
		if (!directory.exists()) {
			throw new ParseException("can't create directory[" + filePath + "]");
		}

	}

	@Override
	public File parsed(NetworkResponse response, HttpEventImp<File> task)
			throws ParseException {
        checkParams(fileName,filePath);
        handler.sendEmptyMessage(START);
		File targetFile = new File(filePath +File.separator+ fileName);

		if (targetFile.exists()) {
			targetFile.delete();
		}

		try {
			FileOutputStream fos = new FileOutputStream(targetFile);

			int length = response.contentLength;
			int current = 0;
			InputStream is = response.ioData;
			byte[] buffer = new byte[task.bufferSize];
			int len;
			try {
				while ((len = is.read(buffer)) != -1&&!task.isCanceled()) {
					fos.write(buffer, 0, len);
					current += len;
					handler.sendMessage(handler.obtainMessage(PROGRESS, new int []{current,length}));
				}
				IOHelper.silentCloseOutputStream(fos);
				IOHelper.silentCloseInputStream(is);
				handler.sendMessage(handler.obtainMessage(END,true));
				return targetFile;
			} catch (IOException e) {
				handler.sendMessage(handler.obtainMessage(ERROR, e));
				handler.sendMessage(handler.obtainMessage(END,false));
				throw new ParseException(e);
				
			}

		} catch (FileNotFoundException e) {
			handler.sendMessage(handler.obtainMessage(ERROR, e));
			handler.sendMessage(handler.obtainMessage(END,false));
			throw new ParseException(e);
		}

	}

}
