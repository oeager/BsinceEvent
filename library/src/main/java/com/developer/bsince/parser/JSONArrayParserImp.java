package com.developer.bsince.parser;

import org.json.JSONArray;
import org.json.JSONException;

import com.developer.bsince.core.Headers;
import com.developer.bsince.exceptions.ParseException;

public abstract class JSONArrayParserImp<T> extends SimpleParserImp<T> {

	@Override
 public  T parsed(String source,Headers mHeaders) throws ParseException {
		try {
			JSONArray jsonArray = new JSONArray(source);
			return parsed(jsonArray,mHeaders);
		} catch (JSONException e) {
			throw new ParseException(e);
		}
	}

	public 	abstract T parsed(JSONArray array,Headers mHeaders)throws ParseException;
}
