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
import java.util.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class QuerySelect {
	
	//a.b.*,a.c,x.y.z,x.f.g,x.y.e == a{b{*},c},x{y{z,e},f{g}}
	private Map map = new HashMap();
	public String select(String... selectClause)
	{
		StringBuffer strb = new StringBuffer();
		
		for(String s : selectClause)
		{
			if(s.startsWith("$"))//dynamic attributes
			{
				strb.append(s.substring(1)+",");
			}
			else if(s.indexOf('.')>0)
			{
				String[] tokens = s.split("\\.");
				this.handleTokens(tokens, 0, map);
			}
			else
			{
				strb.append(s+",");
			}
		}
		this.map2String(strb, map);
		if(strb.toString().endsWith(","))
		{
			strb.deleteCharAt(strb.toString().length()-1);
		}

		return strb.toString();
	}
	
	private void map2String(StringBuffer strb, Map map)
	{
		//if(map.size()==0) return;
		//if(strb.length()>0 && strb.toString().charAt(strb.toString().length()-1)!='{') strb.append(",");
		Set<Map.Entry> set = map.entrySet();
		for(Map.Entry entry : set)
		{
			String key = (String)entry.getKey();
			Map value = (Map)entry.getValue();
			if(value == null || value.size()==0)
			{
				strb.append(key+",");
			}
			else
			{
				strb.append(key+"{");
				this.map2String(strb,value);
				if(strb.toString().endsWith(","))
				{
					strb.deleteCharAt(strb.toString().length()-1);
				}
				strb.append("},");
			}
		}
	}
	
	private void handleTokens(String[] tokens, int index, Map selectMap)
	{
		if(tokens.length<index+1) return;
		String key = tokens[index];
		Map map2 = (Map)selectMap.get(key);
		if(map2==null)
		{
			map2 = new HashMap();
			selectMap.put(key, map2);
		}
		handleTokens(tokens,index+1,map2);
	}
	
	public static void main(String[] args)
	{
		
		System.out.println(new QuerySelect().select("a.b.*","a.c","x.y.z","x.f.g","x.y.e"));
	}

}
