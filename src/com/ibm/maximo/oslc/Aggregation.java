package com.ibm.maximo.oslc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonArray;

public class Aggregation {
	private MaximoConnector mc;
	private StringBuilder urib = new StringBuilder();
	private Map<String, String> aliasMap = new HashMap<String, String>();
	private List<String> gbColsList = new ArrayList<String>();
	private Map<String, String> gbFiltersMap = new HashMap<String, String>();
	private List<String> gbSortByList = new ArrayList<String>();
	
	public Aggregation(MaximoConnector mc, String uri){
		this.mc = mc;
		this.urib.append(uri);
	}
	
	public Aggregation groupByOn(String... attributes){
		for (String attribute: attributes){
			this.gbColsList.add(attribute);
		}
		return this;
	}

	public Aggregation count(){
		this.count(null);
		return this;
	}
	
	public Aggregation count(String alias){
		if(alias == null)
		{
			this.aliasMap.put("count.*", "count.*");
		}else
		{
			this.aliasMap.put(alias, "count.*");
		}
		this.gbColsList.add("count.*");
		return this;
	}
	
	
	public Aggregation avgOn(String attribute){
		this.avgOn(attribute, null);
		return this;
	}
	
	public Aggregation avgOn(String attribute, String alias){
		this.aggregateOn("avg", attribute, alias);
		return this;
	}
	
	public Aggregation sumOn(String attribute){
		this.sumOn(attribute, null);
		return this;
	}
	
	public Aggregation sumOn(String attribute, String alias){
		this.aggregateOn("sum", attribute, alias);
		return this;
	}
	
	public Aggregation minOn(String attribute){
		this.minOn(attribute, null);
		return this;
	}
	
	public Aggregation minOn(String attribute, String alias){
		this.aggregateOn("min", attribute, alias);
		return this;
	}
	
	public Aggregation maxOn(String attribute){
		this.maxOn(attribute, null);
		return this;
	}
	
	public Aggregation maxOn(String attribute, String alias){
		this.aggregateOn("max", attribute, alias);
		return this;
	}
	
	public Aggregation aggregateOn(String function, String attribute, String alias){
		if(alias == null)
		{
			this.aliasMap.put(function + "." + attribute, "sum." + attribute);
		}else
		{
			this.aliasMap.put(alias, function + "." + attribute);
		}
		this.gbColsList.add(function + "." + attribute);
		return this;
	}
	
	public Aggregation having(String... conditions){
		for (String condition : conditions) {
			String[] parseCondition = condition.split(">=|<=|>|<|=");
			if (parseCondition.length > 1) {
				String operation = new String();
				if (condition.contains(">=")) {
					operation = ">=";
				} else if (condition.contains("<=")) {
					operation = "<=";
				} else if (condition.contains("=")) {
					operation = "=";
				} else if (condition.contains("<")) {
					operation = "<";
				} else if (condition.contains(">")) {
					operation = ">";
				}
				this.gbFiltersMap.put(parseCondition[0], operation + parseCondition[1]);
			}
		}
		return this;
	}
	
	public Aggregation sortBy(String... attributes){
		for (String attribute: attributes){
			this.gbSortByList.add(attribute);
		}
		return this;
	}
	
	public JsonArray processGroupBy(){
		if(!this.urib.toString().contains("?")){
			this.urib.append("?");
		}
		if(!this.gbColsList.isEmpty()){
			this.urib.append("&gbcols=");
			for(String str: this.gbColsList){
				try {
					this.urib.append(Util.urlEncode(str)).append(",");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if(this.urib.toString().endsWith(",")){
				this.urib = new StringBuilder(this.urib.substring(0, this.urib.length()-1));
			}	
		}
		if(!gbFiltersMap.isEmpty() && !this.aliasMap.isEmpty()){
			this.urib.append("&gbfilter=");
			Set<Map.Entry<String, String>> entries = this.gbFiltersMap.entrySet();
			for(Map.Entry<String, String> entry: entries){
				String key = entry.getKey();
				if(this.aliasMap.containsKey(key)){
					key = this.aliasMap.get(key);
				}
				try {
					this.urib.append(Util.urlEncode(key + entry.getValue())).append(" and ");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if(this.urib.toString().endsWith(" and ")){
				this.urib = new StringBuilder(this.urib.substring(0, this.urib.length()-5));
			}
		}
		if(!this.gbSortByList.isEmpty()){
			this.urib.append("&gbsortby=");
			for(String str: this.gbSortByList){
				String keys[] = str.split("\\+|\\-");
				String key = new String();
				if (keys.length>1){
					key = keys[1];
				}else
				{
					key = keys[0];
				}
				if (this.aliasMap.containsKey(key)){
					key = this.aliasMap.get(key);
					if(key.equals("count.*")){
						key = "count";
					}
				}
				if(str.contains("+")){
					key = "+" + key;
				}else{
					key = "-" + key;
				}
				try {
					this.urib.append(Util.urlEncode(key)).append(",");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			if(this.urib.toString().endsWith(",")){
				this.urib = new StringBuilder(this.urib.substring(0, this.urib.length()-1));
			}
		}
		
		JsonArray jarr = null;
		
		try {
			jarr = this.mc.groupBy(this.urib.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OslcException e) {
			e.printStackTrace();
		}
		
		return jarr;
	}
}
