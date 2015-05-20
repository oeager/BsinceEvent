package com.developer.bsince.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.developer.bsince.core.Headers;
import com.developer.bsince.core.HttpHeaderParser;
import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.exceptions.ParseException;
import com.developer.bsince.extras.IOHelper;
import com.developer.bsince.response.NetworkResponse;

public abstract class SimpleParserImp<T> implements IParser<T> {

	@Override
	public   T parsed(NetworkResponse response, HttpEventImp<T> event)
			throws ParseException {
		String charset = HttpHeaderParser.parseCharset(response.headers);
		if (response.data == null) {

			InputStream is = response.ioData;
			byte[] buffer = new byte[event.bufferSize];
			StringBuffer sb = new StringBuffer();
			int len;
			try {
				while ((len = is.read(buffer)) != -1) {
					sb.append(new String(buffer, 0, len, charset));

				}

				IOHelper.silentCloseInputStream(is);
				String parsed = new String(sb.toString());
				return parsed(parsed,response.headers);
			} catch (IOException e) {
				e.printStackTrace();
				throw new ParseException(e);
			}

		} else {
			String parsed;
			try {
				parsed = new String(response.data, charset);
			} catch (UnsupportedEncodingException e) {
				parsed = new String(response.data);
			}

			return parsed(parsed,response.headers);
		}

	}

	
	public abstract T parsed(String source,Headers mHeaders) throws ParseException;
}
