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

import javax.json.*;
/**
 * 
 * {@code AttachmenSet} implement the operations on attachmentset from Resource.
 * It provides the set of Attachment.
 * 
 * <p>This object can be created by {@code AttachmentSet}.
 * The following code shows how to create {@code AttachmentSet} using {@code AttachmentSet} Constructor
 * </p>
 * <pre>
 * <code>
 * Resource res = new Resource();
 * AttachmentSet ats = res.attachmentSet(doclinAttrName,relName);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to build a new {@code AttachmentSet}</p>
 * <pre>
 * <code>
 * AttachmentSet ats = new AttachmentSet();
 * AttachmentSet ats = new AttachmentSet(jsonobject,maximoconnector);
 * AttachmentSet ats = new AttachmentSet(uri,maximoconnector);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to set uri and jsonobject to {@code Attachment}</p>
 * <pre>
 * <code>
 * ats.href(uri);
 * ats.JsonObject(jo);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to load and reload data</p>
 * <pre>
 * <code>
 * att.load();
 * att.reload();
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to get AttachmentSet data from {@code AttachmentSet}</p>
 * <pre> 
 * <code>
 * JsonObject jo = att.toJSON(); 
 * byte[] jodata = att.toJSONBytes();
 * String uri = att.getURI();
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to create, get and delete {@code Attachment} from{@code AttachmentSet}</p>
 * <pre>
 * <code>
 * Attachment att = ats.create(new Attachment());
 * Attachment att = ats.create(relation, new Attachment());
 * Attachment att = ats.member(index);
 * Attachment att = ats.member(id);
 * ats.delete(index);
 * ats.delete(id);
 * </code>
 * </pre>
 * 
 * <p>The following example shows how to get the this page size from {@code AttachmentSet}</p>
 * <pre>
 * <code>
 * int currentPageSize = ats.thisPageSize();
 * </code>
 * </pre>
 *
 */
public class AttachmentSet {
	private String href;
	private JsonObject jo;
	private MaximoConnector mc;
	private boolean isLoaded = false;
	
	public AttachmentSet(){
		
	}
	
	public AttachmentSet (MaximoConnector mc) {
		this.mc = mc;
	}
	
	public AttachmentSet(JsonObject jo, MaximoConnector mc) {
		this.jo = jo;
		if (jo.containsKey("rdf:about")) {
			this.href = jo.getString("rdf:about");
		} else {
			this.href = jo.getString("href");
		}
		this.mc = mc;
	}
	
	public AttachmentSet(String href, MaximoConnector mc){
		this.href = href;
		this.mc = mc;
	}
	/**
	 * Get current URI
	 * 
	 */
	public String getURI(){
		return this.href;
	}
	
	/**
	 * Get AttahcmentSet data in JSON
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public JsonObject toJSON() throws IOException, OslcException{
		this.load();
		return this.jo;
	}
	/**
	 * Get AttahcmentSet data in JSONBytes
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public byte[] toJSONBytes() throws OslcException,IOException{
		this.load();
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		Json.createWriter(bo).writeObject(this.jo);
		bo.close();
		return bo.toByteArray();
	}
	
	public AttachmentSet href(String href){
		this.href = href;
		return this;
	}
	
	public AttachmentSet JsonObject(JsonObject jo){
		this.jo = jo;
		if (jo.containsKey("rdf:about")) {
			this.href = jo.getString("rdf:about");
		} else {
			this.href = jo.getString("href");
		}
		return this;
	}
	/**
	 * Load the data for attachmentset
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public AttachmentSet load() throws IOException, OslcException{
		this.load(null);
		return this;
	}
	
	public AttachmentSet load(Map<String ,Object> headers) throws IOException, OslcException{
		if(isLoaded){
			return this;
		}
		if(headers!=null && !headers.isEmpty()){
			this.jo = this.mc.get(this.href,headers);
		}else{
			this.jo = this.mc.get(this.href);
		}
		
		isLoaded = true;
		return this;
	}
	
	public AttachmentSet reload() throws IOException, OslcException{
		isLoaded = false;
		load();
		return this;
	}
	
	/**
	 * Create a new attachment
	 * @param att
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public Attachment create(Attachment att) throws IOException, OslcException{
		JsonObject obj = this.mc.createAttachment(this.href,att.toDoc(), att.getName(),att.getDescription(),att.getMeta());
		return new Attachment(obj,this.mc);
	}
	
	public Attachment create(String relation,Attachment att) throws IOException, OslcException{
		if(!this.href.contains(relation.toLowerCase()) || !this.href.contains(relation.toUpperCase())){
			this.href+="/"+relation;
		}
		JsonObject obj = this.mc.createAttachment(this.href,att.toDoc(), att.getName(),att.getDescription(),att.getMeta());
		return new Attachment(obj,this.mc);
	}
	
	public Attachment create(String relation,Attachment att, Map<String, Object> headers) throws IOException, OslcException{
		if(!this.href.contains(relation.toLowerCase()) || !this.href.contains(relation.toUpperCase())){
			this.href+="/"+relation;
		}
		JsonObject obj;
		if(headers != null && !headers.isEmpty()){
			obj = this.mc.createAttachment(this.href,att.toDoc(), att.getName(),att.getDescription(),att.getMeta(), headers);
		}else{
			obj = this.mc.createAttachment(this.href,att.toDoc(), att.getName(),att.getDescription(),att.getMeta());
		}
		return new Attachment(obj,this.mc);
	}
	
	/**
	 * Get the member of attachmentset
	 * @param index
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	
	public Attachment member(int index) throws IOException, OslcException{
		if(!isLoaded){
			load();
		}
		JsonArray arr = null;
		if(this.jo.containsKey("rdfs:member")){
			arr = this.jo.getJsonArray("rdfs:member");
		}else{
			arr = this.jo.getJsonArray("member");
		}
		JsonObject obj = (JsonObject)arr.get(index-1); 
		return new Attachment(obj,this.mc);
	}
	
	public Attachment member(String id) throws IOException, OslcException{
		if(!isLoaded){
			load();
		}
		JsonObject obj = null;
		JsonArray arr = null;
		if(this.jo.containsKey("rdfs:member")){
			arr = this.jo.getJsonArray("rdfs:member");
		}else{
			arr = this.jo.getJsonArray("member");
		}
		for(int i=0;i<arr.size();i++){
			obj = arr.getJsonObject(i);
			if(obj.containsKey("href") && obj.getString("href").contains(id)){
				break;
			}
			else if(obj.containsKey("rdf:about") && obj.getString("rdf:about").contains(id)){
				break;
			}
			obj = null;
		}
		if(obj == null){
			return null;
		}
		return new Attachment(obj,this.mc);
	}
	
	/**
	 * Delete the Attachment
	 * @param index
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public AttachmentSet delete(int index) throws IOException, OslcException{
			this.member(index).delete();
			reload();
			return this;
	}
	
	public AttachmentSet delete(String id) throws IOException, OslcException{
		this.member(id).delete();
		reload();
		return this;
	}
	
	public int thisPageSize() throws IOException, OslcException
	{
		if(!isLoaded){
			load();
		}
		int size = -1;
		if(this.jo.containsKey("member")){
			size = this.jo.getJsonArray("member").size();
		}else if(this.jo.containsKey("rdfs:member")){
			size = this.jo.getJsonArray("rdfs:member").size();
		}
		return size;
	}
	
	public Attachment fetchMember(String uri, String... properties)throws IOException, OslcException {	
		return this.fetchMember(uri, null, properties);
	}
	
	public Attachment fetchMember(String uri, Map<String, Object> headers, String... properties)throws IOException, OslcException {
		StringBuilder strb = new StringBuilder().append(uri);
		if (properties.length > 0) {
			strb.append(uri.contains("?") ? "" : "?").append("&oslc.properties=");
			for (String property : properties) {
				strb.append(property).append(",");
			}
			if (strb.toString().endsWith(",")) {
				strb = strb.deleteCharAt(strb.length() - 1);
			}
		}
		String[] strs = strb.toString().split("/");
		String id = strs[strs.length-1];
		String metaUri = strb.toString().replace(id, "meta");
		metaUri = metaUri + "/" + id;
		JsonObject jo;
		if(headers != null && !headers.isEmpty()){
			jo = this.mc.get(metaUri, headers);
		}else{
			jo = this.mc.get(metaUri);
		}
		return new Attachment(jo, this.mc);
	}
}
