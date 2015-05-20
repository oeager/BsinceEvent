package com.developer.bsince.data;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.developer.bsince.core.assist.HttpConstants;

import android.text.TextUtils;

public class JSONDataSet extends AbstractDataSet {


	private final JSONArray jsonArrayParam = new JSONArray();

	public void put(Object o) {

		jsonArrayParam.put(o);
	}

	public void put(int index,Object o) {

		try {
			jsonArrayParam.put(index,o);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(OutputStream os) throws IOException {

		String charset = getCharset();
		if(TextUtils.isEmpty(charset)){
			charset = HttpConstants.DEFAULT_CHARSET;
		}
		byte [] data;
		if(jsonArrayParam.length()>0){
			data = jsonArrayParam.toString().getBytes(charset);
		}else {
			try {
				data = convertToJson().toString().getBytes(charset);
			} catch (JSONException e) {
				throw new IOException(e);
			}
		}
		
		os.write(data);
		
	}

	protected JSONObject convertToJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();

		for (Map.Entry<String,?> entry : params.entrySet()) {
			jsonObject.put(entry.getKey(), entry.getValue());
		}
		return jsonObject;

	}

	@Override
	protected void init() {
		setContentType(HttpConstants.CONTENT_TYPE_JSON);
		setCharset(HttpConstants.DEFAULT_CHARSET);
	}
}
