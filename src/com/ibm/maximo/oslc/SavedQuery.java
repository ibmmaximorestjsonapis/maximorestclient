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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;

public class SavedQuery {
	private String name = null;
	private Map<String, Object> map = new HashMap<String,Object>();
	
	public SavedQuery() {
	}
	
	public SavedQuery(String name,Map<String,Object> map){
		this.name=name;
		this.map=map;
	}
	

	public SavedQuery name(String name){
		this.name = name;
		return this;
	}
	public SavedQuery params(Map<String,Object> params){
		this.map=params;
		return this;
	}
	public SavedQuery addParam(String key, Object value){
		map.put(key, value);
		return this;
	}
	
	public String savedQueryClause(){
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(this.name);
		Set<Map.Entry<String, Object>> set = this.map.entrySet();
		for(Map.Entry<String, Object> entry : set)
		{
			try {
				strBuilder.append("&").append("sqp:");
				strBuilder.append(entry.getKey());
				strBuilder.append("=").append(Util.stringValue(entry.getValue()));
			} catch (UnsupportedEncodingException e){
				
			}catch(DatatypeConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return strBuilder.toString();
	}
}
