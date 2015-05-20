package com.developer.bsince.parser;

import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.exceptions.ParseException;
import com.developer.bsince.response.NetworkResponse;

public interface IParser<T> {

	T parsed(NetworkResponse response, HttpEventImp<T> task) throws ParseException;
}
