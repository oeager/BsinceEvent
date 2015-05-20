package com.developer.bsince.data;

import java.util.ArrayList;
import java.util.List;

public class SoapHeader {

	private final String nameSpace;
	private final String name;
	protected final List<Element> elements = new ArrayList<>();

	public SoapHeader(String nameSpace, String name) {
		this.name = name;
		this.nameSpace = nameSpace;

	}

	public  SoapHeader createElement(String key, String value) {

		elements.add(new Element(key, value));
		return this;
		
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public String getName() {
		return name;
	}

	public static class Element {

		private String key;

		private String value;

		public Element(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

}
