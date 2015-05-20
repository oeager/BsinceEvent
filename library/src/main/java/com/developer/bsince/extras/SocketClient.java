package com.developer.bsince.extras;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.developer.bsince.data.LoadingListener;
import com.developer.bsince.data.MulitDataSet.FileWrapper;

public class SocketClient {
	private static final String BOUNDARY = "---------------------------7da2137580612"; // 数据分隔线
	private static final String endline = "--" + BOUNDARY + "--\r\n";// 数据结束标志
	private static final int DOWNLOAD_START = 0;
	private static final int DOWNLOAD_DOWNLOADING = 1;
	private static final int DOWNLOAD_COMPLETE = 2;
	private static final int DOWNLOAD_ERROR = 3;

	public static void doUpload(final String path,
			final Map<String, String> params, final FileWrapper[] files,
			final LoadingListener uploadListener) {
		new Thread(new Runnable() {

			Handler handler = new Handler(Looper.getMainLooper()) {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case DOWNLOAD_START:
						uploadListener.onLoadingStarted();
						break;

					case DOWNLOAD_DOWNLOADING:
						Object[] data = (Object[]) msg.obj;
						uploadListener.onLoadProgressChanged((int) data[0],
								(int) data[1]);
						break;
					case DOWNLOAD_COMPLETE:
						uploadListener.onLoadingEnd(true,msg.obj);
						break;
					case DOWNLOAD_ERROR:
						uploadListener.onLoadingError((Exception) msg.obj);
						break;
					}
				};
			};

			@Override
			public void run() {
				try {
					handler.sendEmptyMessage(DOWNLOAD_START);
					int currentSize= 0;
					int fileDataLength = 0;
					for (FileWrapper uploadFile : files) {// 得到文件类型数据的总长度
						StringBuilder fileExplain = new StringBuilder();
						fileExplain.append("--");
						fileExplain.append(BOUNDARY);
						fileExplain.append("\r\n");
						fileExplain.append("Content-Disposition: form-data;name=\""
								+ uploadFile.paramName + "\";filename=\""
								+ uploadFile.customFileName + "\"\r\n");
						fileExplain.append("Content-Type: " + uploadFile.contentType
								+ "\r\n\r\n");
						fileDataLength += fileExplain.length();
						fileDataLength+=uploadFile.file.length();
						fileDataLength += "\r\n".length();
					}
					StringBuilder textEntity = new StringBuilder();
					for (Map.Entry<String, String> entry : params.entrySet()) {// 构造文本类型参数的实体数据
						textEntity.append("--");
						textEntity.append(BOUNDARY);
						textEntity.append("\r\n");
						textEntity.append("Content-Disposition: form-data; name=\""
								+ entry.getKey() + "\"\r\n\r\n");
						textEntity.append(entry.getValue());
						textEntity.append("\r\n");
					}
					// 计算传输给服务器的实体数据总长度
					int dataLength = textEntity.toString().getBytes().length
							+ fileDataLength + endline.getBytes().length;
					/*这此之前均为计算长度*/
					
					URL url = new URL(path);
					int port = url.getPort() == -1 ? 80 : url.getPort();
					Socket socket = new Socket(InetAddress.getByName(url.getHost()), port);
					OutputStream outStream = socket.getOutputStream();
					// 下面完成HTTP请求头的发送
					String requestmethod = "POST " + url.getPath() + " HTTP/1.1\r\n";
					outStream.write(requestmethod.getBytes());
					
					String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
					outStream.write(accept.getBytes());
					String language = "Accept-Language: zh-CN\r\n";
					outStream.write(language.getBytes());
					String contenttype = "Content-Type: multipart/form-data; boundary="
							+ BOUNDARY + "\r\n";
					outStream.write(contenttype.getBytes());
					String contentlength = "Content-Length: " + dataLength + "\r\n";
					outStream.write(contentlength.getBytes());
					String alive = "Connection: Keep-Alive\r\n";
					outStream.write(alive.getBytes());
					String host = "Host: " + url.getHost() + ":" + port + "\r\n";
					outStream.write(host.getBytes());
					// 写完HTTP请求头后根据HTTP协议再写一个回车换行
					outStream.write("\r\n".getBytes());
					// 把所有文本类型的实体数据发送出来
					outStream.write(textEntity.toString().getBytes());
					currentSize += textEntity.toString().getBytes().length;
					Message msg = handler.obtainMessage(DOWNLOAD_DOWNLOADING, new Object[]{currentSize,dataLength});
					handler.sendMessage(msg);
					// 把所有文件类型的实体数据发送出来
					for (FileWrapper uploadFile : files) {
						StringBuilder fileEntity = new StringBuilder();
						fileEntity.append("--");
						fileEntity.append(BOUNDARY);
						fileEntity.append("\r\n");
						fileEntity.append("Content-Disposition: form-data;name=\""
								+ uploadFile.paramName + "\";filename=\""
								+ uploadFile.customFileName + "\"\r\n");
						fileEntity.append("Content-Type: " + uploadFile.contentType
								+ "\r\n\r\n");
						byte [] bytes = fileEntity.toString().getBytes();
						outStream.write(bytes);
						currentSize += bytes.length;
						handler.sendMessage(handler.obtainMessage(DOWNLOAD_DOWNLOADING, new Object[]{currentSize,dataLength}));
						InputStream is = new FileInputStream(uploadFile.file);
						byte[] buffer = new byte[1024];
						int len = 0;
						while ((len = is.read(buffer, 0, 1024)) != -1) {
							outStream.write(buffer, 0, len);
							//----------------------
							currentSize += buffer.length;
							
							handler.sendMessage(handler.obtainMessage(DOWNLOAD_DOWNLOADING, new Object[]{currentSize,dataLength}));
						}
						is.close();
						outStream.write("\r\n".getBytes());
					}
					// 下面发送数据结束标志，表示数据已经结束
					outStream.write(endline.getBytes());
					outStream.flush();
					outStream.close();
					BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String statusLine = bf.readLine();
					if(statusLine.indexOf("200")==-1){
						handler.sendMessage(handler.obtainMessage(DOWNLOAD_ERROR, new Exception("上传失败,状态行内容为"+statusLine)));
					}else{
						StringBuilder sb = new StringBuilder();
						String content;
						while((content=bf.readLine())!=null){
							sb.append(content).append("\n");
						}
						handler.sendMessage(handler.obtainMessage(DOWNLOAD_COMPLETE, sb.toString()));
					}
					bf.close();
				
					socket.close();
				} catch (Exception e) {
					 e.printStackTrace();
					handler.sendMessage(handler.obtainMessage(DOWNLOAD_ERROR, e));
				}
			}

		}).start();
	}
}
