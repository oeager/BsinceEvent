package com.developer.bsince.parser;

import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.developer.bsince.core.Headers;
import com.developer.bsince.exceptions.ParseException;

public abstract class XmlParserImp<T> extends SimpleParserImp<T> {

	@Override
	public T parsed(String source,Headers mHeaders) throws ParseException {

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			xmlPullParser.setInput(new StringReader(source));
			return parsed(xmlPullParser,mHeaders);
		} catch (XmlPullParserException e) {
			throw new ParseException(e);
		}
		
	}

	public abstract    T parsed(XmlPullParser parser,Headers mHeaders) throws ParseException;
}
