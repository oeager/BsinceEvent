package com.developer.bsince.request;

import android.support.annotation.StringDef;


public class Method {

	private Method() {
	}

	public static final String GET = "GET";

	public static final String POST = "POST";

	public static final String PUT = "PUT";

	public static final String DELETE = "DELETE";

	public static final String HEAD = "HEAD";

	public static final String OPTIONS = "OPTIONS";

	public static final String TRACE = "TRACE";

	public static final String PATCH = "PATCH";

	public static boolean invalidatesCache(@HttpMethod String method) {

		return POST.equals(method) || PATCH.equals(method)
				|| PUT.equals(method) || DELETE.equals(method);
	}

	public static boolean requiresRequestBody(@HttpMethod String method) {
		return POST.equals(method) || PATCH.equals(method)
				|| PUT.equals(method);

	}

	public static boolean permitsRequestBody(@HttpMethod String method) {
		return requiresRequestBody(method) || DELETE.equals(method);
	}

	@StringDef({ Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.HEAD,
			Method.OPTIONS, Method.TRACE, Method.PATCH })
	public @interface HttpMethod {

	}

}
