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

import javax.json.Json;
import javax.json.JsonObject;


/**
 * 
 * {@code Attachment} implement the operations on attachment from Resource.
 * It provides the data, meta data, uri and so on.
 * 
 * <p>This object can be created by {@code AttachmentSet}.
 * The following code shows how to create {@code Attachment} using {@code AttachmentSet} Constructor
 * </p>
 * <pre>
 * <code>
 * Attachment att = new Attachment();
 * att = new AttachmentSet().create("DOCLINKS", att);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to build a new {@code Attachment}</p>
 * <pre>
 * <code>
 * Attachment att = new Attachment();
 * Attachment att = new Attachment(attachmenturi,maximoconnector);
 * Attachment att = new Attachment(attachmentJsonObject,maximoconnector);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to set maximoconnector, name, description, data, metadata, wwwURI to {@code Attachment}</p>
 * <pre>
 * <code>
 * att.mc(maximoconnector).name(filename).description(description)
 * att.data(byte[] data).meta(type, storeas).wwwURI(wwwURI);
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
 * The following examples show how to get information from {@code Attachment}
 * For file data:</p>
 * <pre> 
 * <code>
 * byte[] data = att.toDoc();
 * String uri = att.getURI();
 * String name = att.getName();
 * String description = att.getDescription();
 * String meta = att.getMeta();
 * </code>
 * </pre>
 * 
 * <p>
 * For file metadata:</p>
 * <pre>
 * <code>
 * JsonObject jo = att.toDocMeta(); 
 * byte[] jodata = att.toDocMetaBytes();
 * </code>
 * </pre>
 * 
 * <p>The following example shows how to delete the {@code Attachment}
 * <pre>
 * <code>
 * att.delete(); //if the attachment is deleted as ats.att.detele(), please reload attachmentset after.
 * </code>
 * </pre>
 *
 */




public class Attachment {
	private String name;
	private String description;
	private String meta;
	private String uri;
	private byte[] data;
	private JsonObject jo;
	private MaximoConnector mc;
	private boolean isUploaded = false;
	private boolean isLoaded = false;
	private boolean isMetaLoaded = false;
	
	
	public Attachment() {
		// TODO Auto-generated constructor stub
	}
	
	public Attachment(String uri, MaximoConnector mc){
		this.uri = uri;
		this.mc = mc;
		isUploaded = true;
	}
	
	public Attachment(JsonObject obj, MaximoConnector mc) {
		// TODO Auto-generated constructor stub
		this.jo = obj;
		String docUri = new String();
		if(obj.containsKey("rdf:about")){
			docUri = obj.getString("rdf:about");
		}else if(obj.containsKey("rdf:resource")){
			docUri = obj.getString("rdf:resource");
		}else{
			docUri = obj.getString("href");
		}
		if(docUri.contains("meta")){
			String[] strs = docUri.split("/");
			String id = strs[strs.length-1];
			docUri = docUri.replace("meta/" + id, id);
		}
		this.uri = docUri;
		this.mc = mc;
		isUploaded = true;
	}

	/**
	 * Attachment att = new Attachment().mc(params)
	 * @param mc
	 */
	
	public Attachment maximoConnector(MaximoConnector mc){
		this.mc = mc;
		return this;
	}
	
	public Attachment name(String name){
		this.name = name;
		return this;
	}

	public Attachment description(String description){
		this.description = description;
		return this;
	}
	
	public Attachment meta(String type, String storeas){
		String headerValue;
		if(type!=null){
			headerValue = type + "/" +storeas;
		}else{
			headerValue = storeas;
		}
		this.meta = headerValue;
		return this;
	}
	
	public Attachment wwwURI(String uri){
		this.uri =uri;
		return this;
	}
	
	public Attachment data(byte[] data){
		this.data = data;
		return this;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public String getMeta(){
		return this.meta;
	}
	
	public byte[] toDoc() throws IOException, OslcException{
		if(!isUploaded){
			return this.data;
		}
		else if(!isLoaded){
			load();
		}
		return this.data;
	}
	
	/**
	 * Get current URI
	 *  
	 */
	public String getURI(){
		return this.uri;
	}
	
	/**
	 * Get Attachment data in JSON
	 *  
	 * @throws IOException
	 * @throws OslcException
	 */
	public JsonObject toDocMeta() throws IOException, OslcException{
		if(!isMetaLoaded){
			loadMeta();
		}
		return this.jo;
	}
	/**
	 * Get Attachment data in JSONBytes
	 *  
	 * @throws IOException
	 * @throws OslcException
	 */
	public byte[] toDocMetaBytes() throws OslcException,IOException{
		if(!isMetaLoaded){
			loadMeta();
		}
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		Json.createWriter(bo).writeObject(this.jo);
		bo.close();
		return bo.toByteArray();
	}
	/**
	 * load attachment data
	 * @throws OslcException 
	 * @throws IOException 
	 */
	public Attachment load() throws IOException, OslcException {
		if(isLoaded){
			throw new OslcException("The attachment has been loaded, please call reload for refreshing");
		}
		this.data = this.mc.getAttachmentData(this.uri);
		isLoaded = true;
		return this;
	}
	
	public Attachment reload() throws IOException, OslcException {
		isLoaded = false;
		load();
		return this;
	}
	
	/**
	 * load attachment meta data
	 *  
	 * @throws IOException
	 * @throws OslcException
	 */
	public Attachment loadMeta() throws IOException, OslcException{
		if(isMetaLoaded){
			throw new OslcException("The attachment has been loaded, please call reloadMeta for refreshing");
		}
		StringBuilder metauri = new StringBuilder();
		if(this.uri.contains("meta")){
			jo = this.mc.get(this.uri);
			isMetaLoaded = true;
			return this;
		}
		String[] temp = this.uri.split("/");
		for(String str:temp){
			metauri.append(str).append("/");
			if(str.equals("DOCLINKS")){
				metauri.append("meta").append("/");
			}
		}
		if(metauri.toString().endsWith("/")){
			metauri.deleteCharAt(metauri.length()-1);
		}
		
		jo = this.mc.get(metauri.toString());
		
		if(jo.containsKey("rdf:about")){
			this.name = jo.getString("dcterms:title");
			this.description = jo.getString("dcterms:description");
			this.meta = jo.getString("spi:urlType") + "/" + "Attachments"; 	
		} else {
			this.name = jo.getString("title");
			this.description = jo.getString("description");
			this.meta = jo.getString("urlType") + "/" + "Attachments";
		}
		isMetaLoaded= true;
		return this;
	}
	
	public Attachment reloadMeta() throws IOException, OslcException{
		isMetaLoaded = false;
		loadMeta();
		return this;
	}
	
	public JsonObject fetchDocMeta() throws IOException, OslcException{
		isMetaLoaded = false;
		loadMeta();
		return this.jo;
	}
	
	/**
	 * Delete the attachment
	 * @throws IOException
	 * @throws OslcException
	 */
	public void delete() throws IOException, OslcException{
		this.mc.delete(this.uri);
	}
	
}
