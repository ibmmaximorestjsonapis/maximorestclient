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


import java.util.LinkedHashMap;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.*;


public class QueryWhere {

	private StringBuffer strbWhere = new StringBuffer();
	private Map<String,Object> map = new LinkedHashMap<String,Object>();
	private String currentKey;
	
	public QueryWhere where(String name)
	{
		currentKey = name;
		return this;
	}
	
	public QueryWhere and(String name)
	{
		if(name.indexOf('.')>0)
		{
			String[] attrPath = name.split("\\.");
			Map<String,String> childMap = (Map)map.get(attrPath[0]);
			if(childMap == null)
			{
				childMap = new LinkedHashMap<String,String>();
				map.put(attrPath[0], childMap);
			}
		}
		currentKey = name;
		return this;
	}
	
	private Map getCurrentMap()
	{
		if(currentKey.indexOf('.')>0)
		{
			String[] attrPath = currentKey.split("\\.");
			return (Map)map.get(attrPath[0]);
		}
		return map;
	}
	
	private String getCurrentKey()
	{
		if(currentKey.indexOf('.')>0)
		{
			String[] attrPath = currentKey.split("\\.");
			return attrPath[1];
		}
		return currentKey;
	}
	
	private void setQueryToken(String s)
	{
		Map currMap = this.getCurrentMap();
		String currKey = this.getCurrentKey();
		if(currMap.containsKey(currKey))
		{
			currKey ="/"+currKey;
		}
		currMap.put(currKey, s);

	}
	
	public QueryWhere equalTo(Object value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue(value);
		this.setQueryToken("="+s);
		return this;
	}
	
	public QueryWhere startsWith(String value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue(value+"%");
		this.setQueryToken("="+s);
		return this;
	}

	public QueryWhere endsWith(String value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue("%"+value);
		this.setQueryToken("="+s);
		return this;
	}

	public QueryWhere like(String value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue("%"+value+"%");
		this.setQueryToken("="+s);
		return this;
	}

	public QueryWhere gt(Object value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue(value);
		this.setQueryToken(">"+s);
		return this;
	}
	
	public QueryWhere gte(Object value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue(value);
		this.setQueryToken(">="+s);
		return this;
	}

	public QueryWhere notEqualTo(Object value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue(value);
		this.setQueryToken("!="+s);
		return this;
	}
	
	public QueryWhere lt(Object value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue(value);
		this.setQueryToken("<"+s);
		return this;
	}
	
	public QueryWhere lte(Object value) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		String s = Util.stringValue(value);
		this.setQueryToken("<="+s);
		return this;
	}

	public QueryWhere in(Object... values) throws DatatypeConfigurationException, UnsupportedEncodingException
	{
		StringBuffer strb = new StringBuffer();
		//strb.append("[");
		for(Object o : values)
		{
			strb.append(Util.stringValue(o)+",");
		}
		String s = strb.toString();
		s = s.substring(0, s.length()-1);
		//strb.append("]");
		this.setQueryToken(" in "+"["+s+"]");
		return this;
	}

	public String whereClause()
	{
		Set<Map.Entry<String, Object>> set = map.entrySet();
		int cnt = 0;
		for(Map.Entry<String, Object> entry : set)
		{
			++cnt;
			String key = entry.getKey();
			if(key.startsWith("/"))
			{
				key = key.substring(1);
			}
			Object value = entry.getValue();
			strbWhere.append(key);
			if(value instanceof String)
			{
				strbWhere.append(value);
			}
			else
			{
				Map<String,String> childMap = (Map)value;
				strbWhere.append("{");
				Set<Map.Entry<String,String>> cset = childMap.entrySet();
				int ccnt = 0;
				for(Map.Entry<String,String> centry : cset)
				{
					++ccnt;
					String cKey = centry.getKey();
					if(cKey.startsWith("/"))
					{
						cKey = cKey.substring(1);
					}

					String cValue = centry.getValue();
					strbWhere.append(cKey);
					strbWhere.append(cValue);
					if(cset.size()>ccnt)
					{
						strbWhere.append(" and ");
					}
				}
				strbWhere.append("}");
			}
			if(set.size()>cnt)
			{
				strbWhere.append(" and ");
			}
		}
		String where = strbWhere.toString();
		return where;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException, DatatypeConfigurationException
	{
		String where = (new QueryWhere()).where("wonum").equalTo("hello").and("statusdate").gt(new Date()).and("statusdate").lte(new Date()).and("wotask.status").in("APPR","WAPPR").and("wotask.qty").lte(100).and("type").startsWith("CT").whereClause();
		System.out.println(where);
	}

}
