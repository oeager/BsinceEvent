package com.developer.bsince.parser;

import com.developer.bsince.core.Headers;
import com.developer.bsince.exceptions.ParseException;

public class TextParserImp extends SimpleParserImp<String> {

	private TextParserImp(){}
	
	public final static TextParserImp INSTANCE = new TextParserImp();
	
	@Override
	public String parsed(String source,Headers mHeaders) throws ParseException {
		return source;
	}


}
