package com.developer.bsince.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import com.developer.bsince.core.assist.HttpConstants;
import com.developer.bsince.core.assist.HttpGlobalConfiguration;
import com.developer.bsince.data.MulitDataSet.FileWrapper;
import com.developer.bsince.data.MulitDataSet.StreamWrapper;
import com.developer.bsince.extras.IOHelper;

public class JSONStreamDataSet extends AbstractDataSet {

	private final byte[] elapsedField;
	private final boolean useGzip;
	private static final byte[] JSON_TRUE = "true".getBytes();
	private static final byte[] JSON_FALSE = "false".getBytes();
	private static final byte[] JSON_NULL = "null".getBytes();
	private static final byte[] STREAM_NAME = escape("name");
    private static final byte[] STREAM_TYPE = escape("type");
    private static final byte[] STREAM_CONTENTS = escape("contents");
	// Size of the byte-array buffer used in I/O streams.
	private static final int BUFFER_SIZE = 4096;

	// Buffer used for reading from input streams.
	private final byte[] buffer = new byte[BUFFER_SIZE];

	public JSONStreamDataSet(boolean useGZipCompression, String elapsedField) {
		this.elapsedField = TextUtils.isEmpty(elapsedField) ? null
				: escape(elapsedField);
		this.useGzip = useGZipCompression;
		
	}

	@Override
	public void write(final OutputStream out) throws IOException {

		if (out == null) {
			throw new IllegalStateException("Output stream cannot be null.");
		}

		// Record the time when uploading started.
		long now = System.currentTimeMillis();

		// Use GZIP compression when sending streams, otherwise just use
		// a buffered output stream to speed things up a bit.
		OutputStream os = useGzip ? new GZIPOutputStream(out, BUFFER_SIZE)
				: out;
		// Always send a JSON object.
		os.write('{');

		// Keys used by the HashMaps.
		Set<String> keys = params.keySet();

		int keysCount = keys.size();
		if (0 < keysCount) {
			int keysProcessed = 0;
			boolean isFileWrapper;

			// Go over all keys and handle each's value.
			for (String key : keys) {
				// Indicate that this key has been processed.
				keysProcessed++;

				try {
					// Evaluate the value (which cannot be null).
					Object value = params.get(key);

					// Write the JSON object's key.
					os.write(escape(key));
					os.write(':');

					// Bail out prematurely if value's null.
					if (value == null) {
						os.write(JSON_NULL);
					} else {
						// Check if this is a FileWrapper.
						isFileWrapper = value instanceof FileWrapper;

						// If a file should be uploaded.
						if (isFileWrapper
								|| value instanceof StreamWrapper) {
							// All uploads are sent as an object containing the
							// file's details.
							os.write('{');

							// Determine how to handle this entry.
							if (isFileWrapper) {
								writeToFromFile(os,
										(FileWrapper) value);
							} else {
								writeToFromStream(os,
										(StreamWrapper) value);
							}

							// End the file's object and prepare for next one.
							os.write('}');
						} else if (value instanceof org.json.JSONObject) {
							os.write(value.toString().getBytes());
						} else if (value instanceof org.json.JSONArray) {
							os.write(value.toString().getBytes());
						} else if (value instanceof Boolean) {
							os.write((Boolean) value ? JSON_TRUE : JSON_FALSE);
						} else if (value instanceof Long) {
							os.write((((Number) value).longValue() + "")
									.getBytes());
						} else if (value instanceof Double) {
							os.write((((Number) value).doubleValue() + "")
									.getBytes());
						} else if (value instanceof Float) {
							os.write((((Number) value).floatValue() + "")
									.getBytes());
						} else if (value instanceof Integer) {
							os.write((((Number) value).intValue() + "")
									.getBytes());
						} else {
							os.write(escape(value.toString()));
						}
					}
				} finally {
					// Separate each K:V with a comma, except the last one.
					if (elapsedField != null || keysProcessed < keysCount) {
						os.write(',');
					}
				}
			}

			// Calculate how many milliseconds it took to upload the contents.
			long elapsedTime = System.currentTimeMillis() - now;

			// Include the elapsed time taken to upload everything.
			// This might be useful for somebody, but it serves us well since
			// there will almost always be a ',' as the last sent character.
			if (elapsedField != null) {
				os.write(elapsedField);
				os.write(':');
				os.write((elapsedTime + "").getBytes());
			}

			Log.i(TAG, "Uploaded JSON in " + Math.floor(elapsedTime / 1000)
					+ " seconds");
		}

		// Close the JSON object.
		os.write('}');	
		os.flush();
		os.close();
		
	}

	private void writeToFromStream(OutputStream os,
			StreamWrapper entry) throws IOException {

		// Send the meta data.
		writeMetaData(os, entry.name, entry.contentType);

		int bytesRead;

		// Upload the file's contents in Base64.
		Base64OutputStream bos = new Base64OutputStream(os, Base64.NO_CLOSE
				| Base64.NO_WRAP);

		// Read from input stream until no more data's left to read.
		while ((bytesRead = entry.inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, bytesRead);
		}

		// Close the Base64 output stream.
		IOHelper.silentCloseOutputStream(bos);

		// End the meta data.
		endMetaData(os);

		// Close input stream.
		IOHelper.silentCloseInputStream(entry.inputStream);
	}

	private void writeToFromFile(OutputStream os,
			FileWrapper wrapper) throws IOException {

		// Send the meta data.
		writeMetaData(os, wrapper.file.getName(), wrapper.contentType);

		int bytesRead, bytesWritten = 0, totalSize = (int) wrapper.file
				.length();

		// Open the file for reading.
		FileInputStream in = new FileInputStream(wrapper.file);

		// Upload the file's contents in Base64.
		Base64OutputStream bos = new Base64OutputStream(os, Base64.NO_CLOSE
				| Base64.NO_WRAP);

		// Read from file until no more data's left to read.
		while ((bytesRead = in.read(buffer)) != -1) {
			bos.write(buffer, 0, bytesRead);
			bytesWritten += bytesRead;
			
			if(HttpGlobalConfiguration.debug){
				Log.i(TAG, "byteWritten="+bytesWritten+",and totalSize = "+totalSize);
			}
		}

		// Close the Base64 output stream.
		IOHelper.silentCloseOutputStream(bos);

		// End the meta data.
		endMetaData(os);

		// Safely close the input stream.
		IOHelper.silentCloseInputStream(in);
	}

	private void writeMetaData(OutputStream os, String name, String contentType)
			throws IOException {
		// Send the streams's name.
		os.write(STREAM_NAME);
		os.write(':');
		os.write(escape(name));
		os.write(',');

		// Send the streams's content type.
		os.write(STREAM_TYPE);
		os.write(':');
		os.write(escape(contentType));
		os.write(',');

		// Prepare the file content's key.
		os.write(STREAM_CONTENTS);
		os.write(':');
		os.write('"');
	}

	private void endMetaData(OutputStream os) throws IOException {
		os.write('"');
	}

	// Curtosy of Simple-JSON: http://goo.gl/XoW8RF
	// Changed a bit to suit our needs in this class.
	static byte[] escape(String string) {
		// If it's null, just return prematurely.
		if (string == null) {
			return JSON_NULL;
		}

		// Create a string builder to generate the escaped string.
		StringBuilder sb = new StringBuilder(128);

		// Surround with quotations.
		sb.append('"');

		int length = string.length(), pos = -1;
		while (++pos < length) {
			char ch = string.charAt(pos);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				// Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if ((ch >= '\u0000' && ch <= '\u001F')
						|| (ch >= '\u007F' && ch <= '\u009F')
						|| (ch >= '\u2000' && ch <= '\u20FF')) {
					String intString = Integer.toHexString(ch);
					sb.append("\\u");
					int intLength = 4 - intString.length();
					for (int zero = 0; zero < intLength; zero++) {
						sb.append('0');
					}
					sb.append(intString.toUpperCase(Locale.US));
				} else {
					sb.append(ch);
				}
				break;
			}
		}

		// Surround with quotations.
		sb.append('"');

		return sb.toString().getBytes();
	}
	
	public void put(String key, File file) throws FileNotFoundException {
		put(key, file, null, null);
	}
	
	public void put(String key, File file, String contentType) throws FileNotFoundException {
		if (file == null || !file.exists()) {
			throw new FileNotFoundException();
		}
		if (key != null) {
			params.put(key, new FileWrapper(file, contentType));
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
			params.put(key, StreamWrapper.newInstance(stream, name,
					contentType));
		}
	}

	@Override
	public String getEncoding() {

		return useGzip ? HttpConstants.ENCODING_GZIP : null;
	}

	@Override
	protected void init() {
		setContentType(HttpConstants.CONTENT_TYPE_JSON);
		setCharset(HttpConstants.DEFAULT_CHARSET);
	}
}
