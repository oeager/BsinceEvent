package com.developer.bsince.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.developer.bsince.core.assist.HttpConstants;
import com.developer.bsince.data.SoapHeader.Element;
import com.developer.bsince.extras.IOHelper;

public class SoapDataSet extends NameValueDataSet {

	private String nameSpace = null;

	private String soapAction = null;

	private String methodName = null;

	public boolean dotNet = false;

	private final int version;

	private final String soapEnvelop;

	public static final int SOAP_VER_10 = 10;

	public final static int SOAP_VER_11 = 11;

	public final static int SOAP_VER_12 = 12;

	private final List<SoapHeader> soapHeaders = new ArrayList<>();

	/** Envelope namespace, set by the constructor */
	public String env;
	/** Encoding namespace, set by the constructor */
	public String enc;
	/** Xml Schema instance namespace, set by the constructor */
	public String xsi;
	/** Xml Schema data namespace, set by the constructor */
	public String xsd;

	public static final String ENV2003 = "http://www.w3.org/2003/05/soap-envelope";
	public static final String ENC2003 = "http://www.w3.org/2003/05/soap-encoding";
	/** Namespace constant: http://schemas.xmlsoap.org/soap/envelope/ */
	public static final String ENV = "http://schemas.xmlsoap.org/soap/envelope/";
	/** Namespace constant: http://schemas.xmlsoap.org/soap/encoding/ */
	public static final String ENC = "http://schemas.xmlsoap.org/soap/encoding/";
	/** Namespace constant: http://www.w3.org/2001/XMLSchema */
	public static final String XSD = "http://www.w3.org/2001/XMLSchema";
	/** Namespace constant: http://www.w3.org/2001/XMLSchema */
	public static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
	/** Namespace constant: http://www.w3.org/1999/XMLSchema */
	public static final String XSD1999 = "http://www.w3.org/1999/XMLSchema";
	/** Namespace constant: http://www.w3.org/1999/XMLSchema */
	public static final String XSI1999 = "http://www.w3.org/1999/XMLSchema-instance";

	public SoapDataSet(int soapVersion) {
		
		if (soapVersion >= 12) {
			this.version = SOAP_VER_12;
			this.soapEnvelop = "soap12";
			setContentType(HttpConstants.CONTENT_TYPE_SOAP_XML);
		} else if (soapVersion <= 10) {
			this.version = SOAP_VER_10;
			this.soapEnvelop = "soap";
			setContentType(HttpConstants.CONTENT_TYPE_XML);
		} else {
			this.version = SOAP_VER_11;
			this.soapEnvelop = "soap";
			setContentType(HttpConstants.CONTENT_TYPE_XML);
		}
		setCharset(HttpConstants.DEFAULT_CHARSET);
		if (version == SOAP_VER_10) {
			xsi = XSI1999;
			xsd = XSD1999;
		} else {
			xsi = XSI;
			xsd = XSD;
		}
		if (version < SOAP_VER_12) {
			enc = ENC;
			env = ENV;
		} else {
			enc = ENC2003;
			env = ENV2003;
		}
	}
	@Override
	protected void init() {
		
	}

	public void putSoapHeader(SoapHeader soapHeader) {
		soapHeaders.add(soapHeader);
	}

	public SoapHeader putSoapHeader(String name) {

		return putSoapHeader(nameSpace, name);
	}

	public SoapHeader putSoapHeader(String nameSpace, String name) {
		SoapHeader soapheader = new SoapHeader(nameSpace, name);
		soapHeaders.add(soapheader);
		return soapheader;
	}

	@Override
	public void setPropertyBeforeConnect(HttpURLConnection conn) {
		if (this.version != SOAP_VER_12) {
			if (soapAction == null) {
				soapAction = "\"\"";
			}
			conn.setRequestProperty("SOAPAction", soapAction);
		}
	}

	@Override
	public void write(OutputStream os) throws IOException {
		byte [] data =serializeXml();
		os.write(data);
		os.flush();
	}

	public byte[] serializeXml() throws IllegalArgumentException,
			IllegalStateException, IOException {

		XmlSerializer serializer = Xml.newSerializer();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializer.setOutput(baos, getCharset());
		serializer.startDocument(getCharset(), null);
		serializer.setPrefix("xsi", xsi);
		serializer.setPrefix("xsd", xsd);
		serializer.setPrefix("encodingStyle", enc);
		serializer.setPrefix(soapEnvelop, env);
		serializer.startTag(env, "Envelope");
		serializer.startTag(env, "Header");
		writeHeader(serializer);
		serializer.endTag(env, "Header");
		serializer.startTag(env, "Body");
		writeBody(serializer);// write body
		serializer.endTag(env, "Body");
		serializer.endTag(env, "Envelope");
		serializer.endDocument();
		serializer.flush();
		baos.flush();
		IOHelper.silentCloseOutputStream(baos);
		return baos.toByteArray();
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
		
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	protected void writeHeader(XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {

		int len = soapHeaders.size();
		for (int i = 0 ; i <len;i++) {
			SoapHeader header = soapHeaders.get(i);
			serializer.startTag((dotNet) ? "" : header.getNameSpace(), header.getName());
			if(dotNet){
				serializer.attribute(null, "xmlns", header.getNameSpace());
			}
			int count = header.elements.size();
			for (int j=0;j<count;j++) {
				Element element = header.elements.get(j);
				serializer.startTag(null, element.getKey());
				serializer.text(element.getValue());
				serializer.endTag(null, element.getKey());
				element = null;
			}
			serializer.endTag((dotNet) ? "" : header.getNameSpace(), header.getName());
			header = null;
		}
		
	}

	protected void writeBody(XmlSerializer serializer)
			throws IllegalArgumentException, IllegalStateException, IOException {

		Map<String, String> keyValuePairs = convertToMap(null, params);

		serializer.startTag((dotNet) ? "" : nameSpace, methodName);
		if (dotNet) {
			serializer.attribute(null, "xmlns", nameSpace);
		}
		for (Map.Entry<String, String> entry : keyValuePairs.entrySet()) {
			serializer.startTag(null, entry.getKey());
			serializer.text(entry.getValue());
			serializer.endTag(null, entry.getKey());
		}

		serializer.endTag((dotNet) ? "" : nameSpace, methodName);

	}

}
