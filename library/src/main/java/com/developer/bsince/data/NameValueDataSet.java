package com.developer.bsince.data;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.text.TextUtils;

import com.developer.bsince.core.assist.HttpConstants;

public class NameValueDataSet extends AbstractDataSet {

	@Override
	protected void init() {
		setContentType(HttpConstants.CONTENT_TYPE_DEFAULT);
		setCharset(HttpConstants.DEFAULT_CHARSET);
	}

	@Override
	public void write(OutputStream os) throws IOException {
		Map<String, String> nameValuePairs = convertToMap(null, params);
		StringBuilder encodedParams = new StringBuilder();
		if (TextUtils.isEmpty(getCharset())) {
			setCharset(HttpConstants.DEFAULT_CHARSET);
		}
		for (Entry<String, String> entry : nameValuePairs.entrySet()) {
			encodedParams
					.append(URLEncoder.encode(entry.getKey(), getCharset()));
			encodedParams.append('=');
			encodedParams.append(URLEncoder.encode(entry.getValue().toString(),
					getCharset()));
			encodedParams.append('&');
		}
		os.write(encodedParams.toString().getBytes());

	}

	protected Map<String, String> convertToMap(String key, Object value) {
		Map<String, String> keyValuePairs = createDataSetCollections();

		if (value instanceof Map) {

			Map<?, ?> map = (Map<?, ?>) value;
			for (Entry<?, ?> entry : map.entrySet()) {
				if (entry.getKey() instanceof String) {
					Object val = entry.getValue();

					if (val != null) {
						keyValuePairs.putAll(convertToMap(
								key == null ? (String) entry.getKey() : String
										.format(Locale.US, "%s[%s]", key,
												entry.getKey()), val));
					}

				}
			}

		} else if (value instanceof List) {
			List<?> list = (List<?>) value;
			int listSize = list.size();
			for (int nestedValueIndex = 0; nestedValueIndex < listSize; nestedValueIndex++) {
				keyValuePairs.putAll(convertToMap(String.format(Locale.US,
						"%s[%d]", key, nestedValueIndex), list
						.get(nestedValueIndex)));
			}
		}else if (value instanceof byte[] ){
			keyValuePairs.put(key, new String((byte[])value,Charset.forName(getCharset())));
		} else if (value instanceof Object[]) {
			Object[] array = (Object[]) value;
			int arrayLength = array.length;
			for (int nestedValueIndex = 0; nestedValueIndex < arrayLength; nestedValueIndex++) {
				keyValuePairs.putAll(convertToMap(String.format(Locale.US,
						"%s[%d]", key, nestedValueIndex),
						array[nestedValueIndex]));
			}
		} else if (value instanceof Set) {//set集合里面将不可再存入集合或数据（只能是String）
			Set<?> set = (Set<?>) value;
			
			StringBuilder builder = new StringBuilder();
			for (Object nestedValue : set) {
				builder.append(String.valueOf(nestedValue));
				builder.append(',');
			}
			keyValuePairs.put(key, builder.substring(0, builder.length()-1));
		} else {
			keyValuePairs.put(key, String.valueOf(value));
		}
		return keyValuePairs;

	}

}
