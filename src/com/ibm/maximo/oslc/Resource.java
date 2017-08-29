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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.json.*;

/**
 * 
 * {@code Resource} implement the operations on Resource.
 * It provides the data, uri, attachment and so on.
 * 
 * <p>This object can be created by {@code ResourceSet} or {@code MaximoConnector}.
 * The following code shows how to create {@code Resource} using {@code ResourceSet}
 * or using the {@code MaximoConnector}
 * </p>
 * <pre>
 * <code>
 * Resource re = re.member(index);
 * Resource re = re.fetchMember(uri,properties);
 * Resource re = mc.resource(uri, properties);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to build a new {@code Resource}</p>
 * <pre>
 * <code>
 * Resource re = new Resource();
 * Resource re = new Resource(uri);
 * Resource re = new Resource(jsonObject);
 * Resource re = new Resource(uri,maximoConnector);
 * Resource re = new Resource(jsonObject, maximoConnector);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to set uri and maximoConnector to {@code Resource}</p>
 * <pre>
 * <code>
 * re.uri(URI).maximoConnector(maximoConnector);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to load and reload data</p>
 * <pre>
 * <code>
 * re.load();
 * re.reload();
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to get information from {@code Resource}</p>
 * <pre> 
 * <code>
 * JsonObject jo = re.toJSON();
 * byte[] joBytes = re.toJSONBytes();
 * </code>
 * </pre>
 * 
 * <p>The following examples show how to update, merge and delete the {&code Resource}</p>
 * <pre>
 * <code>
 * re.update(jsonObject,properties);
 * re.merge(jsonObject,properties);
 * re.delete(index); 
 * rs.re.delete(); //if the attachment is deleted as rs.re.detele(), please reload {@code ResourceSet} after.
 * </code>
 * </pre>
 *
 * <p>The following example show how to get attachmentSet, relatedResource and how to invoke action by {@code Resource}</p>
 * <pre><code>
 * AttachmentSet ats = re.attachmentSet(doclinkAttrName, relName);
 * Resource relationRe = re.relatedResource(attrName);
 * re.invokeAction(actionName,jsonObeject);
 * </code></pre>
 *
 */

public class Resource {
	
	private String href;
	private JsonObject jsonObject;
	private MaximoConnector mc;
	private boolean isLoaded = false;
	
	//constructor
	public Resource(JsonObject jo)
	{
		this.jsonObject = jo;
		if(jo.containsKey("rdf:about")){
			this.href = jo.getString("rdf:about");
		}else if(jo.containsKey("rdf:resoure")){
			this.href = jo.getString("rdf:resource");
		}else if(jo.containsKey("href")){
			this.href = jo.getString("href");
		}else if(jo.containsKey("localref")){
			this.href = jo.getString("localref");
		}
	}
	
	public Resource(JsonObject jo,MaximoConnector mc)
	{
		this.jsonObject = jo;
		if(jo.containsKey("rdf:about")){
			this.href = jo.getString("rdf:about");
		}else if(jo.containsKey("rdf:resource")){
			this.href = jo.getString("rdf:resource");
		}else if(jo.containsKey("href")){
			this.href = jo.getString("href");
		}else if(jo.containsKey("localref")){
			this.href = jo.getString("localref");
		}
		this.mc = mc;
	}
	
	public Resource(String href){
		this.href =href;
	}
	
	public Resource(String href, MaximoConnector mc){
		this.href = href;
		this.mc = mc;
	}
	
	public Resource uri(String href){
		this.href = href;
		return this;
	}
	
	public Resource maximoConnector(MaximoConnector mc){
		this.mc = mc;
		return this;
	}
	
	/**
	 * Get current URI
	 * 
	 */
	public String getURI(){
		return this.href;
	}
	
	/**
	 * Get Resource data in JSON
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public JsonObject toJSON() throws IOException, OslcException{
		if(!isLoaded){
			load();
		}
		return this.jsonObject;
	}
	/**
	 * Get Resource data in JSONBytes
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public byte[] toJSONBytes() throws OslcException,IOException{
		if(!isLoaded){
			load();
		}
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		Json.createWriter(bo).writeObject(this.jsonObject);
		bo.close();
		return bo.toByteArray();
	}
	
	/**
	 * Load current data with properties in header
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */ 
	
	public Resource load() throws OslcException,IOException {
		return this.loadWithAdditionalParamsAndHeaders(null, null);
	}
	
	public Resource load(String... properties) throws OslcException,IOException {
		return this.loadWithAdditionalParamsAndHeaders(null, null, properties);
	}
	
	public Resource loadWithAdditionalParams(Map<String, Object> params,  String... properties)throws OslcException, IOException {
		return this.loadWithAdditionalParamsAndHeaders(params, null, properties);
	}
	
	public Resource loadWithAdditionalHeaders(Map<String, Object> headers,  String... properties)throws OslcException, IOException {
		return this.loadWithAdditionalParamsAndHeaders(null, headers, properties);
	}
	
	public Resource loadWithAdditionalParamsAndHeaders(Map<String, Object> params, Map<String, Object> headers, String... properties)throws OslcException, IOException {
		if (isLoaded) {
			throw new OslcException(
					"The resource has been loaded, please call reload for refreshing");
		}
		if (this.href == null || this.href.isEmpty()) {
			throw new OslcException("The_resource_is_invalid");
		}
		StringBuilder strb = new StringBuilder();
		strb.append(this.href);
		if (properties.length > 0) {
			strb.append(this.href.contains("?") ? "" : "?").append("&oslc.properties=");
			StringBuilder paramsStrb = new StringBuilder();
			for (String property : properties) {
				paramsStrb.append(property).append(",");
			}
			if(paramsStrb.toString().endsWith(",")){
				paramsStrb = paramsStrb.deleteCharAt(paramsStrb.length() - 1);
			}
			strb.append(Util.urlEncode(paramsStrb.toString()));
		}
		if(params != null && !params.isEmpty()){
			strb.append(this.href.contains("?") ? "" : "?");
			Set<Map.Entry<String, Object>> entrySet = params.entrySet();
			for(Map.Entry<String, Object> entry: entrySet){
				StringBuilder singleParam = new StringBuilder();
				singleParam.append("&").append(entry.getKey()).append("=");
				singleParam.append(Util.urlEncode(entry.getValue().toString()));
				strb.append(singleParam.toString());
			}
		}
		if(headers!=null && !headers.isEmpty()){
			this.jsonObject = this.mc.get(strb.toString(),headers);
		}else{
			this.jsonObject = this.mc.get(strb.toString());
		}
		this.isLoaded = true;
		return this;
	}
	
	public Resource reload() throws OslcException, IOException{
		this.isLoaded = false;
		load();
		return this;
	}
	
	public Resource reload(String... properties) throws OslcException, IOException{
		this.isLoaded = false;
		load(properties);
		return this;
	}
	
	/**
	 * Update the Resource
	 * @param jo
	 * 
	 * @throws OslcException
	 * @throws IOException
	 */
	
	public Resource update(JsonObject jo,String... properties) throws OslcException, IOException
	{
		return this.update(jo,null, properties);
	}
	
	public Resource update(JsonObject jo, Map<String, Object> headers, String... properties) throws OslcException, IOException
	{
		if(this.href.isEmpty()){
			throw new OslcException("The_resource_is_invalid");
		}
		if(headers != null && !headers.isEmpty()){
			this.jsonObject = this.mc.update(this.href,jo, headers, properties);
		}else{
			this.jsonObject = this.mc.update(this.href,jo, properties);
		}
		if(properties!=null && properties.length>0){
			this.isLoaded = true;
		}else{
			this.reload();
		}
		
		return this;
	}
	
	public Resource merge(JsonObject jo,String... properties) throws OslcException, IOException
	{
		return this.merge(jo, null, properties);
	}
	
	public Resource merge(JsonObject jo, Map<String, Object> headers, String... properties) throws OslcException, IOException
	{
		if(this.href.isEmpty()){
			throw new OslcException("The_resource_is_invalid");
		}
		if(headers !=null && !headers.isEmpty()){
			this.jsonObject = this.mc.merge(this.href,jo, headers, properties);
		}else{
			this.jsonObject = this.mc.merge(this.href,jo, properties);
		}
		if(properties!=null && properties.length>0){
			this.isLoaded = true;
		}else{
			this.reload();
		}
		return this;
	}
	
	
	/**
	 * Load the attachmentset for resource
	 * Note: there has to be a relation between them
	 * 
	 */
	
	public AttachmentSet attachmentSet(String doclinkAttrName, String relName){
		String str = null;
		if(doclinkAttrName == null){
			doclinkAttrName = "doclinks";
		}
		if(this.jsonObject.containsKey(doclinkAttrName)){
			JsonObject obj = jsonObject.getJsonObject(doclinkAttrName);
			str = obj.getString("href");
		}else if(this.jsonObject.containsKey("spi:"+ doclinkAttrName)){
			JsonObject obj = jsonObject.getJsonObject("spi:" + doclinkAttrName);
			if(obj.containsKey("rdf:about")){
				str = obj.getString("rdf:about");
			}else if(obj.containsKey("rdf:resource")){
				str = obj.getString("rdf:resource");
			}			
		}else{
			if(relName != null)
			{
				str = this.href + "/" + relName.toUpperCase();
			}
			else
				str = this.href + "/DOCLINKS";
		}
		return new AttachmentSet(str,this.mc);
	}
	
	public AttachmentSet attachmentSet() throws OslcException{
		String str = null;
		if(this.jsonObject.containsKey("doclinks")){
			JsonObject obj = jsonObject.getJsonObject("doclinks");
			str = obj.getString("href");
		}else if(this.jsonObject.containsKey("spi:"+ "doclinks")){
			JsonObject obj = jsonObject.getJsonObject("spi:" + "doclinks");
			if(obj.containsKey("rdf:about")){
				str = obj.getString("rdf:about");
			}else if(obj.containsKey("rdf:resource")){
				str = obj.getString("rdf:resource");
			}			
		}else{
			throw new OslcException("invalid_relation");
		}
		return new AttachmentSet(str,this.mc);
	}
	
	public Resource relatedResource(String attrName) throws IOException, OslcException
	{
		//get the uri value from the attrname json property
		//mc.resourceSet(osName).fetchMember
		String url = new String();
		JsonObject jo = null;
		if(this.jsonObject.containsKey(attrName)){
			jo = this.jsonObject.getJsonObject(attrName);
		}else if(this.jsonObject.containsKey("spi:" + attrName)){
			jo = this.jsonObject.getJsonObject("spi:" + attrName);			
		}else{
			return null;
		}
		
		if(jo.containsKey("href")){
			url = jo.getString("href");
		}else if(jo.containsKey("rdf:resource")){
			url = jo.getString("rdf:resource");
		}else if(jo.containsKey("rdf:about")){
			url = jo.getString("rdf:about");
		}else{
			return null;
		}
		
		return this.mc.resourceSet().fetchMember(url);
	}
	
	
	/**
	 * Invoke Action 
	 * @param actionName
	 * @param jo
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public Resource invokeAction(String actionName,JsonObject jo) throws IOException, OslcException{
		this.mc.update(this.href + (this.href.contains("?")?"":"?")+"&action="+actionName,jo);
		this.reload();
		return this;
	}
	
	public Resource invokeAction(String actionName,JsonObject jo, String... properties) throws IOException, OslcException{
		this.jsonObject = this.mc.update(this.href + (this.href.contains("?")?"":"?")+"&action="+actionName,jo, properties);
		return this;
	}
	
	public void delete() throws IOException, OslcException{
		this.mc.delete(this.href);
	}
	
	/**
	 * Support pre-load resource.
	 * @param isLoaded
	 */
	
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}
}
