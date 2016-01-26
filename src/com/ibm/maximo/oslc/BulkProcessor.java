package com.ibm.maximo.oslc;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class BulkProcessor {
	private JsonArrayBuilder bulkArray = Json.createArrayBuilder();
	private MaximoConnector mc;
	private String uri;
	
	public BulkProcessor(MaximoConnector mc, String uri){
		this.mc = mc;
		this.uri = uri;
	}

	public BulkProcessor create(JsonObject jo){
		JsonObject obj = Json.createObjectBuilder().add("_data", jo).build();
		this.bulkArray.add(obj);
		return this;
	}
	
	public BulkProcessor update(JsonObject jo, String uri, String... properties){
		JsonObjectBuilder objb = Json.createObjectBuilder().add("_data", jo);
		this.addMeta(objb, "PATCH", uri, properties);
		return this;
	}
	
	public BulkProcessor merge(JsonObject jo, String uri, String... properties){
		JsonObjectBuilder objb = Json.createObjectBuilder().add("_data", jo);
		this.addMeta(objb, "MERGE", uri, properties);
		return this;
	}
	
	public BulkProcessor delete(String uri){
		JsonObjectBuilder objb = Json.createObjectBuilder();
		this.addMeta(objb, "DELETE", uri);
		return this;
	}
	
	private void addMeta(JsonObjectBuilder objb, String method, String uri, String... properties){
		JsonObjectBuilder objBuilder = Json.createObjectBuilder();
		String propStr = this.propertiesBuilder(properties);
		if(propStr != null){
			objBuilder.add("properties", propStr);
		}
		if(method != null && !method.isEmpty()){
			objBuilder.add("method", method);
		}
		if(uri != null && !uri.isEmpty()){
			objBuilder.add("uri", uri);
		}
		JsonObject objMeta = objBuilder.build();
		if(!objMeta.isEmpty()){
			objb.add("_meta", objMeta);
		}
		this.bulkArray.add(objb.build());
	}
	
	public JsonArray processBulk(){
		JsonArray jarr = null;
		try {
			jarr = this.mc.bulk(this.uri, this.bulkArray.build());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OslcException e) {
			e.printStackTrace();
		}
		return jarr;
	}
	
	private String propertiesBuilder(String... properties){
		StringBuilder propStrb = new StringBuilder();
		for(String property: properties){
			propStrb.append(property).append(",");
		}
		if(propStrb.length()>0){
			if(propStrb.toString().endsWith(",")){
				return propStrb.substring(0,propStrb.length()-1);
			}else{
				return propStrb.toString();
			}
		}
		return null;
	}
	
	
}
