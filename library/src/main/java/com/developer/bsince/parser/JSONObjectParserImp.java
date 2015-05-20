package com.developer.bsince.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.developer.bsince.core.Headers;
import com.developer.bsince.exceptions.ParseException;

public abstract class JSONObjectParserImp<T> extends SimpleParserImp<T> {

	@Override
	public  T parsed(String source,Headers mHeaders) throws ParseException {
		try {
			JSONObject jsonObject = new JSONObject(source);
			return parsed(jsonObject,mHeaders);
		} catch (JSONException e) {
			throw new ParseException(e);
		}
	}

	public  abstract  T parsed(JSONObject object,Headers mHeaders)throws ParseException;
}
