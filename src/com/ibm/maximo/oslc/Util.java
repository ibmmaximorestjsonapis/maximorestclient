/*
* Licensed Materials - Property of IBM
*
* (C) COPYRIGHT IBM CORP. 2015 All Rights Reserved
*
* US Government Users Restricted Rights - Use, duplication or
* disclosure restricted by GSA ADP Schedule Contract with
* IBM Corp.
 */

package com.ibm.maximo.oslc;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.datatype.DatatypeConfigurationException;

public class Util {
	
	public static String stringValue(Object o) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		if(o instanceof Date)
		{
	        TimeZone timeZone = TimeZone.getDefault();
	        GregorianCalendar gc = (GregorianCalendar)GregorianCalendar.getInstance(timeZone);
	        gc.setTime((Date)o);

			return "\""+javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(gc).toXMLFormat()+"\"";
		}
		else if(o instanceof Number)
		{
			return ""+ o;
		}
		else if(o instanceof Boolean){
			return (o == null)?"null":o.toString();
		}
		else{
			return "\""+(String)o+"\"";
		}
	}
	
	public static String urlEncode(String value) throws UnsupportedEncodingException
	{
		return URLEncoder.encode(value, "utf-8");
	}

	public static JsonArray readFile2JsonArray(String filePath) throws FileNotFoundException{
		File file = new File(filePath);
	    FileInputStream fis = new FileInputStream(file);
		JsonReader rdr = Json.createReader(fis);
		JsonArray jar = rdr.readArray();
		return jar;
	}
	
	public static JsonObject readFile2JsonObject(String filePath) throws FileNotFoundException{
		File file = new File(filePath);
	    FileInputStream fis = new FileInputStream(file);
		JsonReader rdr = Json.createReader(fis);
		JsonObject job = rdr.readObject();
		return job;
	}
	
	public static String getStringFromInputStream(InputStream is) throws IOException {
		 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
 
	}
	
	/**
	 * This printer only support JsonObject and JsonArray
	 * @param object
	 */
	
	public static void jsonPrettyPrinter(Object object){
		if (!(object instanceof JsonObject || object instanceof JsonArray)){
			System.out.println(object.toString());
			return;
		}
		String newLine = System.getProperty("line.separator");
		StringBuilder strb = new StringBuilder();
		char[] charArr = object.toString().toCharArray();
		int depth = 0;
		boolean newLineMark = false;
		for(char ch: charArr){
			if(ch == '{' ){
				depth++;
				strb.append(ch).append(newLine);
				for(int i=0;i<depth;i++){
					strb.append("    ");
				}
				continue;
			}
			if(ch == '"'){
				newLineMark = !newLineMark;
			}
			if(ch == '[' || ch == ']'){
				if( ch == ']'){
					depth--;
					strb.append(newLine);
					for(int i=0;i<depth;i++){
						strb.append("    ");
					}
				}
				strb.append(ch);
				if( ch == '['){
					strb.append(newLine);
					depth++;
					for(int i=0;i<depth;i++){
						strb.append("    ");
					}
				}
				continue;
			}
			if(newLineMark == false && ch == ',' ){
				strb.append(ch).append(newLine);
				for(int i=0;i<depth;i++){
					strb.append("    ");
				}
				continue;
			}
			if(ch == '}'){
				strb.append(newLine);
				depth--;
				for(int i=0;i<depth;i++){
					strb.append("    ");
				}
				strb.append(ch);
				continue;
			}
			if(ch == ':'){
				strb.append(ch);
				if(newLineMark == false){
					strb.append(" ");
				}
				continue;
			}
			strb.append(ch);
		}
		System.out.println(strb.toString());
	}

	public static String base64Encode(String in) throws UnsupportedEncodingException {
		return new String(Base64.encodeBase64(in.getBytes("UTF-8")));
	}

	public static String base64Encode(byte[] in) {
		return new String(Base64.encodeBase64(in));
	}
}
