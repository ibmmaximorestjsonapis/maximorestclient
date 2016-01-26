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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.*;

/**
 * 
 * {@code MaximoConnector} is a Connector between Oslc Client and Maximo Server.
 * It provides the authentication setting, connect, basic requests and disconnect for Server.
 * 
 * <p>This object can be created by {@code MaximoConnector} with {@code Options}.
 * The following code shows how to initial {@code MaximoConnector} using {@code MaximoConnector} and {@code Options}Constructor</p>
 * <pre>
 * <code>
 * MaximoConnector mc = new MaximoConnector(new Options().user(userName)
 * .password(password).mt(true).lean(false).auth(authMethod)
 * .host(hostAddress).port(portNum));
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to build a new {@code MaximoConnector}</p>
 * <pre>
 * <code>
 * Options op = new Options();
 * MaximoConnector mc = new MaximoConnector();
 * mc.options(op);
 * MaximoConnector mc = new MaximoConnector(Options);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to set authentication, method, cookie for session to {@code MaximoConnector}</p>
 * <pre>
 * <code>
 * mc.setAuth(authMethod);
 * mc.setMethod(httpConnection,method);
 * mc.setCookieForSession(httpConnection);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to connect, get, create, update, merge, delete and disconnect to Maximo Server by {@code MaximoConnector}.
 * The properties can be empty</p>
 * <pre>
 * <code>
 * mc.connect();
 * JsonObject jo = mc.get(uri);
 * byte[] docBytes = mc.getAttachmentData(uri);
 * byte[] docBytes = mc.attachedDoc(uri);
 * JsonObject jp = mc.getAttachmentMeta(uri);
 * JsonObject jo = mc.create(uri, jsonObject, properties);
 * JsonObject jo = mc.createAttachment(uri, data, name, decription, meta);
 * JsonObject jo = mc.update(uri, jsonObject, properties);
 * JsonObject jo = mc.merge(uri, jsonObject, properties);
 * mc.delete(uri);
 * mc.deleteResource(uri);
 * mc.deleteAttachment(uri);
 * mc.disconnect();
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to get {@code ResourceSet} or {@code Resource} or {@code Attachment} by {@code MaximoConnector}</p>
 * <pre><code>
 * ResourceSet rs = mc.resourceSet(osName);
 * ResourceSet rs = mc.resourceSet(url);
 * Resource re = mc.resource(uri);
 * Attachment att = mc.attachment(uri);
 * </code></pre>
 * 
 */

public class MaximoConnector {
	
	public static final Logger logger = Logger.getLogger(MaximoConnector.class.getName());
	private Options options;
	private boolean valid = true;
	private boolean debug = false;
	// private JsonObject jo;

	private List<String> cookies = null;
	private JsonObject version;
	private JsonObject userInfo;

	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_PATCH = "PATCH";
	public static final String HTTP_METHOD_MERGE = "MERGE";
	public static final String HTTP_METHOD_DELETE = "DELETE";
	public static final String HTTP_METHOD_BULK = "BULK";
	public static final String HTTP_METHOD_SYNC = "SYNC";
	public static final String HTTP_METHOD_MERGESYNC = "MERGESYNC";

	public String httpMethod = "GET";// by default it is get

	public MaximoConnector(){	
	}
	
	public MaximoConnector(Options options) {
		this.options = options;
	}

	public ResourceSet resourceSet() {
		return new ResourceSet(this);
	}
	
	public ResourceSet resourceSet(String osName) {
		return new ResourceSet(osName, this);
	}
	
	public ResourceSet resourceSet(URL url) throws IOException, OslcException{
		String[] strs = url.getPath().split("/");
		String osName = null;
		for(int i = 0;i<strs.length;i++){
			if(strs[i] == "os"){
				osName = strs[i+1];
				break;
			}
		}
		return new ResourceSet(osName,this);
	}
	

	
	
	public Resource resource(String uri, String... properties) throws IOException, OslcException{
		return new ResourceSet(this).fetchMember(uri, properties);
	}
	
	public Attachment attachment(String uri, String... properties) throws IOException, OslcException{
		return new AttachmentSet(this).fetchMember(uri, properties);
	}
	
	
//	public byte[] attachedDoc(String uri) throws IOException, OslcException{
//		return new AttachmentSet(this).fetchMember(uri).toDoc();
//	}
	
	public JsonObject attachmentDocMeta(String uri) throws IOException, OslcException{
		return new AttachmentSet(this).fetchMember(uri).fetchDocMeta();
	}
	
	public MaximoConnector options(Options op){
		this.options = op;
		return this;
	}

	public String getCurrentURI() {
		return this.options.getPublicURI();
	}

	public Options getOptions(){
		return this.options;
	}
	
	public MaximoConnector debug(boolean isDebug){
		debug = isDebug;
		if(debug){
			logger.setLevel(Level.FINE);
			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.FINE);
			logger.addHandler(consoleHandler);;
		}else{
			logger.setLevel(Level.INFO);
			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.INFO);
			logger.addHandler(consoleHandler);;
		}
		return this;
	}
	
	public boolean isDebug(){
		return debug;
	}
	
	boolean isGET() {
		return this.httpMethod.equals(HTTP_METHOD_GET);
	}

	boolean isPOST() {
		return this.httpMethod.equals(HTTP_METHOD_POST);
	}

	boolean isPATCH() {
		return this.httpMethod.equals(HTTP_METHOD_PATCH);
	}

	boolean isMERGE() {
		return this.httpMethod.equals(HTTP_METHOD_MERGE);
	}

	boolean isDELETE() {
		return this.httpMethod.equals(HTTP_METHOD_DELETE);
	}

	boolean isBULK() {
		return this.httpMethod.equals(HTTP_METHOD_BULK);
	}
	
	boolean isSYNC() {
		return this.httpMethod.equals(HTTP_METHOD_SYNC);
	}
	
	boolean isMERGESYNC() {
		return this.httpMethod.equals(HTTP_METHOD_MERGESYNC);
	}
	
	public boolean isValid(){
		return this.valid;
	}
	
	public boolean isLean(){
		return this.options.isLean();
	}

	/**
	 * Connect to Maximo Server
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */

	public void connect() throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		cookies = null;
		HttpURLConnection con = this.setAuth(this.options.getAppURI());
		if(!this.options.isFormAuth()){
			con = this.setMethod(con, "GET");
		}
		
		int i = con.getResponseCode();
		
		if( i == -1 ){
			throw new OslcException("Invalid_Request");
		}
		
		logger.fine(this.options.getAppURI());
		if(i<400){
			cookies = con.getHeaderFields().get("Set-Cookie");
			JsonObject oslcHome = this.get(this.options.getAppURI());
			if(oslcHome!=null){
//				this.version(oslcHome);
//				this.userInfo(oslcHome);
			}
		}
		if (cookies == null) {
			InputStream inStream = null;
			inStream = con.getErrorStream();
			if(inStream == null){
				inStream = con.getInputStream();
			}
			String str = Util.getStringFromInputStream(inStream);
			throw new OslcException(str);
		}
	}
	
	public synchronized JsonObject get(String uri) throws IOException, OslcException {
		return this.get(uri, null);
	}
	
	public synchronized JsonObject get(String uri, Map<String,Object> headers) throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "GET");
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		int resCode = con.getResponseCode();
		InputStream inStream = null;
		if (resCode >= 400) {
				inStream = con.getErrorStream();
				JsonReader rdr = Json.createReader(inStream);
				JsonObject obj = rdr.readObject();
				throw new OslcException(obj);
		} else {
			inStream = con.getInputStream();
		}
		JsonReader rdr = Json.createReader(inStream);
		JsonObject obj = rdr.readObject();
		con.disconnect();
		return obj;
	}
	
	/**
	 * 
	 * Fetch Group By data
	 * 
	 * @param uri
	 * @return JsonArray
	 * @throws IOException
	 * @throws OslcException
	 */
	
	public synchronized JsonArray groupBy(String uri) throws IOException, OslcException {
		return this.groupBy(uri, null);
	}
	
	public synchronized JsonArray groupBy(String uri, Map<String,Object> headers) throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL.openConnection();
		con = this.setMethod(con, "GET");
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		int resCode = con.getResponseCode();
		InputStream inStream = null;
		if (resCode >= 400) {
				inStream = con.getErrorStream();
				JsonReader rdr = Json.createReader(inStream);
				JsonObject obj = rdr.readObject();
				throw new OslcException(obj);
		} else {
			inStream = con.getInputStream();
		}
		JsonReader rdr = Json.createReader(inStream);
		JsonArray arr = rdr.readArray();
		con.disconnect();
		return arr;
	}
	
	/**
	 * Load DocumentData
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */

	public synchronized byte[] getAttachmentData(String uri) throws IOException, OslcException {
		return this.getAttachmentData(uri, null);
	}
	
	public synchronized byte[] getAttachmentData(String uri, Map<String,Object> headers) throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		//LOG.isLoggable(Level.info);
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
								.openConnection();
		con = this.setMethod(con, "GET");
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		int resCode = con.getResponseCode();
		InputStream inStream = null;
		if (resCode >= 400) {
				inStream = con.getErrorStream();
				JsonReader rdr = Json.createReader(inStream);
				JsonObject obj = rdr.readObject();
				throw new OslcException(obj);
		} else {
			inStream = con.getInputStream();
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[0xFFFF];

		for (int len; (len = inStream.read(buffer)) != -1;)
			bos.write(buffer, 0, len);

		bos.flush();

		return bos.toByteArray();

	}

	/**
	 * Create new Resource
	 * @param jo
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */

	public JsonObject create(String uri,JsonObject jo, String... properties)
			throws IOException, OslcException {
		return this.create(uri, jo, null, properties);
	}
	
	public JsonObject create(String uri,JsonObject jo, Map<String,Object> headers, String... properties)
			throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "POST", properties);
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		OutputStreamWriter writer = new OutputStreamWriter(
				con.getOutputStream());
		writer.write(jo.toString());
		writer.flush();
		writer.close();
		int resCode = con.getResponseCode();
		InputStream inStream;
		if (resCode >= 400) {
			inStream = con.getErrorStream();
			JsonReader rdr = Json.createReader(inStream);
			JsonObject obj = rdr.readObject();
			throw new OslcException(obj);
		}
		JsonObject obj;
		if(properties.length == 0){
			String href = con.getHeaderField("Location");
			if(this.options.isLean()){
				obj = Json.createObjectBuilder().add("rdf:resource", href).build();
			}else{
				obj = Json.createObjectBuilder().add("href", href).build();
			}
		}else{
			inStream = con.getInputStream();
			JsonReader rdr = Json.createReader(inStream);
			obj = rdr.readObject();
		}
		return obj;
	}

	/**
	 * Create new attachment
	 * @param data
	 * @param name
	 * @param description
	 * @param meta
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	
	public JsonObject createAttachment(String uri,byte[] data, String name,
			String description, String meta) throws IOException, OslcException {
		return this.createAttachment(uri, data, name, description, meta, null);
	}
	
	public JsonObject createAttachment(String uri,byte[] data, String name,
			String description, String meta, Map<String, Object> headers) throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "POST");
		con.setRequestProperty("slug", name);
		con.setRequestProperty("x-document-description", description);
		con.setRequestProperty("x-document-meta", meta);
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		DataOutputStream writer = new DataOutputStream(con.getOutputStream());
		if (data.length == 0) {
			throw new OslcException("data_is_invalid");
		}
		writer.write(data);
		writer.flush();
		writer.close();
		int resCode = con.getResponseCode();
		InputStream inStream;
		if (resCode >= 400) {
			inStream = con.getErrorStream();
			JsonReader rdr = Json.createReader(inStream);
			JsonObject obj = rdr.readObject();
			throw new OslcException(obj);
		}
		JsonObject obj;
		String href = con.getHeaderField("Location");
		if(this.options.isLean()){
			obj = Json.createObjectBuilder().add("rdf:resource", href).build();
		}else{
			obj = Json.createObjectBuilder().add("href", href).build();
		}
		return obj;
	}

	/**
	 * Update the Resource
	 * @param jo
	 * @throws IOException
	 * @throws OslcException
	 */

	public synchronized JsonObject update(String uri, JsonObject jo, String... properties)
			throws IOException, OslcException {
		return this.update(uri, jo, null, properties);
	}
	
	public synchronized JsonObject update(String uri, JsonObject jo, Map<String,Object> headers, String... properties)
			throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "PATCH",properties);
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		OutputStreamWriter writer = new OutputStreamWriter(
				con.getOutputStream());
		if (jo.isEmpty()) {
			throw new OslcException("jo_is_invalid");
		}
		writer.write(jo.toString());
		writer.flush();
		writer.close();
		int resCode = con.getResponseCode();
		InputStream inStream;
		if (resCode >= 400) {
			inStream = con.getErrorStream();
			JsonReader rdr = Json.createReader(inStream);
			JsonObject obj = rdr.readObject();
			throw new OslcException(obj);
		}
		if(resCode == 204){
			return null;
		}
		inStream = con.getInputStream();
		JsonReader rdr = Json.createReader(inStream);
		JsonObject obj = rdr.readObject();
		return obj;
	}

	public synchronized JsonObject merge(String uri, JsonObject jo, String... properties)
			throws IOException, OslcException {
		return this.merge(uri, jo, null, properties);
	}
	
	public synchronized JsonObject merge(String uri, JsonObject jo, Map<String,Object> headers, String... properties)
			throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "MERGE",properties);
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		OutputStreamWriter writer = new OutputStreamWriter(
				con.getOutputStream());
		if (jo.isEmpty()) {
			throw new OslcException("jo_is_invalid");
		}
		writer.write(jo.toString());
		writer.flush();
		writer.close();
		int resCode = con.getResponseCode();
		// String resLine = con.getResponseMessage();
		InputStream inStream;
		if (resCode >= 400) {
			inStream = con.getErrorStream();
			JsonReader rdr = Json.createReader(inStream);
			JsonObject obj = rdr.readObject();
			obj = (JsonObject) obj.get("Error");
			throw new OslcException(resCode, obj.get("message").toString());
		}
		if(resCode == 204){
			return null;
		}
		inStream = con.getInputStream();
		JsonReader rdr = Json.createReader(inStream);
		JsonObject obj = rdr.readObject();
		return obj;
	}
	
	public synchronized JsonArray bulk(String uri, JsonArray ja)
			throws IOException, OslcException {
		return this.bulk(uri, ja, null);
	}
	
	public synchronized JsonArray bulk(String uri, JsonArray ja, Map<String,Object> headers)
			throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "BULK");
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		OutputStreamWriter writer = new OutputStreamWriter(
				con.getOutputStream());
		if (ja.isEmpty()) {
			throw new OslcException("jo_is_invalid");
		}
		writer.write(ja.toString());
		writer.flush();
		writer.close();
		int resCode = con.getResponseCode();
		// String resLine = con.getResponseMessage();
		InputStream inStream;
		if (resCode >= 400) {
			inStream = con.getErrorStream();
			JsonReader rdr = Json.createReader(inStream);
			JsonObject obj = rdr.readObject();
			obj = (JsonObject) obj.get("Error");
			throw new OslcException(resCode, obj.get("message").toString());
		}
		if(resCode == 204){
			return null;
		}
		inStream = con.getInputStream();
		JsonReader rdr = Json.createReader(inStream);
		JsonArray arr = rdr.readArray();
		return arr;
	}
	
	public synchronized JsonObject sync(String uri, JsonObject jo, String... properties)
			throws IOException, OslcException {
		return this.sync(uri, jo, null, properties);
	}
	
	public synchronized JsonObject sync(String uri, JsonObject jo, Map<String,Object> headers, String... properties)
			throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "SYNC",properties);
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		OutputStreamWriter writer = new OutputStreamWriter(
				con.getOutputStream());
		if (jo.isEmpty()) {
			throw new OslcException("jo_is_invalid");
		}
		writer.write(jo.toString());
		writer.flush();
		writer.close();
		int resCode = con.getResponseCode();
		// String resLine = con.getResponseMessage();
		InputStream inStream;
		if (resCode >= 400) {
			inStream = con.getErrorStream();
			JsonReader rdr = Json.createReader(inStream);
			JsonObject obj = rdr.readObject();
			obj = (JsonObject) obj.get("Error");
			throw new OslcException(resCode, obj.get("message").toString());
		}
		if(resCode == 204){
			return null;
		}
		JsonObject obj;
		if(properties.length == 0){
			String href = con.getHeaderField("Location");
			if(this.options.isLean()){
				obj = Json.createObjectBuilder().add("rdf:resource", href).build();
			}else{
				obj = Json.createObjectBuilder().add("href", href).build();
			}
		}else{
			inStream = con.getInputStream();
			JsonReader rdr = Json.createReader(inStream);
			obj = rdr.readObject();
		}
		return obj;
	}
	
	public synchronized JsonObject mergeSync(String uri, JsonObject jo, String... properties)
			throws IOException, OslcException {
		return this.mergeSync(uri, jo, null, properties);
	}
	
	public synchronized JsonObject mergeSync(String uri, JsonObject jo, Map<String,Object> headers, String... properties)
			throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "MERGESYNC",properties);
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		OutputStreamWriter writer = new OutputStreamWriter(
				con.getOutputStream());
		if (jo.isEmpty()) {
			throw new OslcException("jo_is_invalid");
		}
		writer.write(jo.toString());
		writer.flush();
		writer.close();
		int resCode = con.getResponseCode();
		// String resLine = con.getResponseMessage();
		InputStream inStream;
		if (resCode >= 400) {
			inStream = con.getErrorStream();
			JsonReader rdr = Json.createReader(inStream);
			JsonObject obj = rdr.readObject();
			obj = (JsonObject) obj.get("Error");
			throw new OslcException(resCode, obj.get("message").toString());
		}
		if(resCode == 204){
			return null;
		}
		inStream = con.getInputStream();
		JsonReader rdr = Json.createReader(inStream);
		JsonObject obj = rdr.readObject();
		return obj;
	}
	
	/**
	 * Delete the resource/attachment
	 * @throws IOException
	 * @throws OslcException
	 */
	
	public void delete(String uri) throws IOException, OslcException {
		this.delete(uri, null);
	}
	
	public void delete(String uri, Map<String, Object> headers) throws IOException, OslcException {
		if(!isValid()){
			throw new OslcException("The instance of MaximoConnector is not valid.");
		}
		String publicHost = this.options.getHost();
		if(this.options.getPort()!=-1){
			publicHost+= ":" + String.valueOf(this.options.getPort());
		}
		if(!uri.contains(publicHost)){
			URL tempURL = new URL(uri);
			String currentHost = tempURL.getHost();
			if(tempURL.getPort()!=-1){
				currentHost+= ":" + String.valueOf(tempURL.getPort());
			}
			uri = uri.replace(currentHost, publicHost);
		}
		logger.fine(uri);
		URL httpURL = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) httpURL
				.openConnection();
		con = this.setMethod(con, "DELETE");
		if (headers!=null && !headers.isEmpty() ) {
			con = this.setHeaders(con, headers);
		}
		if (cookies == null)
			this.connect();
		this.setCookiesForSession(con);
		int resCode = con.getResponseCode();
		if (resCode >= 400) {
				InputStream inStream = con.getErrorStream();
				JsonReader rdr = Json.createReader(inStream);
				JsonObject obj = rdr.readObject();
				throw new OslcException(obj);
		}
	}

	
	public void deleteResource(String uri) throws IOException, OslcException{
		logger.fine("Delete the resource by MaximoConnector");
		this.delete(uri);	
	}
	
	public void deleteAttachment(String uri) throws IOException, OslcException{
		logger.fine("Delete the attachment by MaximoConnector");
		this.delete(uri);	
	}
	
	
	public static String encode(String userName, String password)
			throws UnsupportedEncodingException {
		return (javax.xml.bind.DatatypeConverter.printBase64Binary((userName
				+ ":" + password).getBytes("UTF-8")));
	}

	protected HttpURLConnection setAuth(String uri) throws IOException {
		if (this.options.getUser() != null
				&& this.options.getPassword() != null) {
			if (options.isBasicAuth()) {
				URL httpURL = new URL(uri);
				HttpURLConnection con = (HttpURLConnection) httpURL
						.openConnection();

				String encodedUserPwd = encode(this.options.getUser(),
						this.options.getPassword());
				con.setRequestProperty("Authorization", "Basic "
						+ encodedUserPwd);
				return con;
			} else if (options.isMaxAuth()) {
				URL httpURL = new URL(uri);

				// long t1 = System.currentTimeMillis();
				HttpURLConnection con = (HttpURLConnection) httpURL
						.openConnection();

				String encodedUserPwd = encode(this.options.getUser(),
						this.options.getPassword());
				con.setRequestProperty("maxauth", encodedUserPwd);
				return con;
			} else if (options.isFormAuth()) {
				String appURI = uri;
				appURI += "/j_security_check";
				URL httpURL = new URL(appURI);
				HttpURLConnection con = (HttpURLConnection) httpURL
						.openConnection();
				con.setInstanceFollowRedirects(false);
				con.setRequestMethod("POST");
				con.setRequestProperty("Accept",
						"text/html,application/xhtml+xml,application/xml");
				con.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				con.setRequestProperty("Connection", "keep-alive");
				con.setRequestProperty("x-public-uri", this.options.getPublicURI());
				con.setDoOutput(true);
				StringBuilder content = new StringBuilder();
				content.append("j_username=").append(this.options.getUser());
				content.append("&j_password=").append(
						this.options.getPassword());
				OutputStream outputStream = con.getOutputStream();
				outputStream.write(content.toString().getBytes());
				outputStream.close();
				return con;
			}
		}
		return null;
	}
	
	protected HttpURLConnection setHeaders(HttpURLConnection con, Map<String, Object> headers)
			throws IOException, OslcException {
		Set<Map.Entry<String, Object>> set = headers.entrySet();
		for (Map.Entry<String, Object> entry : set) {
			con.setRequestProperty(entry.getKey(), entry.getValue().toString());
		}		
		return con;
}

	protected HttpURLConnection setMethod(HttpURLConnection con, String method, String... properties)
			throws IOException, OslcException {
		this.httpMethod = method;		
		if (this.isGET()) {
			con.setRequestMethod(HTTP_METHOD_GET);
			con.setRequestProperty("accept", "application/json");
			con.setUseCaches(false);
			con.setAllowUserInteraction(false);
			con.setRequestProperty("x-public-uri", this.options.getPublicURI());
		} else if (this.isPOST()) {
			con.setRequestMethod(HTTP_METHOD_POST);
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setRequestProperty("x-public-uri", this.options.getPublicURI());
		} else if (this.isPATCH()) {
			con.setRequestMethod(HTTP_METHOD_POST);
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setRequestProperty("x-public-uri", this.options.getPublicURI());
			con.setRequestProperty("x-method-override", HTTP_METHOD_PATCH);
		} else if (this.isMERGE()) {
			con.setRequestMethod(HTTP_METHOD_POST);
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setRequestProperty("x-public-uri", this.options.getPublicURI());
			con.setRequestProperty("x-method-override", HTTP_METHOD_PATCH);
			con.setRequestProperty("patchtype",HTTP_METHOD_MERGE);
		} else if (this.isBULK()) {
			con.setRequestMethod(HTTP_METHOD_POST);
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setRequestProperty("x-public-uri", this.options.getPublicURI());
			con.setRequestProperty("x-method-override", HTTP_METHOD_BULK);
		} else if (this.isSYNC()) {
			con.setRequestMethod(HTTP_METHOD_POST);
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setRequestProperty("x-public-uri", this.options.getPublicURI());
			con.setRequestProperty("x-method-override", HTTP_METHOD_SYNC);
		} else if (this.isMERGESYNC()) {
			con.setRequestMethod(HTTP_METHOD_POST);
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setRequestProperty("x-public-uri", this.options.getPublicURI());
			con.setRequestProperty("x-method-override", HTTP_METHOD_SYNC);
			con.setRequestProperty("patchtype",HTTP_METHOD_MERGE);
		} else if (this.isDELETE()) {
			con.setRequestMethod(HTTP_METHOD_DELETE);
			con.setRequestProperty("accept", "application/json");
			con.setUseCaches(false);
			con.setAllowUserInteraction(false);
			con.setRequestProperty("x-public-uri", this.options.getPublicURI());
		}
		
		for (String property : properties) {
			con.setRequestProperty("Properties", property);
		}
		
		return con;
	}

	private void setCookiesForSession(HttpURLConnection con) {
		for (String cookie : cookies) {
			con.setRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
	}
	
	
//	private MaximoConnector version(JsonObject oslcHome) throws IOException, OslcException{
//		JsonObject versionObj = null;
//		String versionUri = null;
//		if(oslcHome.containsKey("spi:version")){
//			versionObj = oslcHome.getJsonObject("spi:version");
//			if(versionObj.containsKey("rdf:resource")){
//				versionUri = versionObj.getString("rdf:resource");
//			}else if(versionObj.containsKey("href")){
//				versionUri = versionObj.getString("href");
//			}
//		}else if(oslcHome.containsKey("version")){
//			versionObj = oslcHome.getJsonObject("version");
//			if(versionObj.containsKey("href")){
//				versionUri = versionObj.getString("href");
//			}
//		}
//		if(versionUri!=null){
//			this.version = this.get(versionUri);
//		}
//		return this;
//	}
//	
//	private MaximoConnector userInfo(JsonObject oslcHome) throws IOException, OslcException{
//		JsonObject userInfoObj = null;
//		String userInfoUri = null;
//		if(oslcHome.containsKey("spi:whoami")){
//			userInfoObj = oslcHome.getJsonObject("spi:whoami");
//			if(userInfoObj.containsKey("rdf:resource")){
//				userInfoUri = userInfoObj.getString("rdf:resource");
//			}else if(userInfoObj.containsKey("href")){
//				userInfoUri = userInfoObj.getString("href");
//			}
//		}else if(oslcHome.containsKey("whoami")){
//			userInfoObj = oslcHome.getJsonObject("whoami");
//			if(userInfoObj.containsKey("href")){
//				userInfoUri = userInfoObj.getString("href");
//			}
//		}
//		if(userInfoUri != null){
//			this.userInfo = this.get(userInfoUri);
//		}
//		return this;
//	}
//	
//	public JsonObject getVersion(){
//		return this.version;
//	}
//	
//	public JsonObject getUserInfo(){
//		return this.userInfo;
//	}
	
	/**
	 * Disconnect with Maximo Server
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		String logout = this.options.getPublicURI()
				+ "/logout";
		if(this.getOptions().isMultiTenancy()){
			logout += "?&_tenantcode=" + this.getOptions().getTenantCode();
		}
		logger.fine(logout);
		URL httpURL = new URL(logout);

		// long t1 = System.currentTimeMillis();
		HttpURLConnection con = (HttpURLConnection) httpURL.openConnection();
		con.setRequestMethod("GET");
		this.setCookiesForSession(con);
		if (con.getResponseCode() == 401) {
			logger.fine("Logout");
		}
		this.valid = false;
	}
}
