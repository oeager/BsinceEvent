package com.developer.bsince.core;

import com.developer.bsince.event.HttpEventImp;
import com.developer.bsince.response.NetworkResponse;


public interface IWork {

	public <T> NetworkResponse requestEvent(HttpEventImp<T> task) throws Exception;
}
