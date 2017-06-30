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

/**
 * 
 * {@code Options} is served for {@code MaximoConnector}.
 * 
 * <p>
 * The following code shows how to initial {@code MaximoConnector} using {@code MaximoConnector} and {@code Options}Constructor</p>
 * <pre>
 * <code>
 * MaximoConnector mc = new MaximoConnector(new Options().user(userName)
 * .password(password).mt(true).lean(false).auth(authMethod)
 * .host(hostAddress).port(portNum));
 * </code>
 * </pre>
 * 
 */


public class Options {

	private String host;
	private Integer port = null;
	private String authMode = null;
	public static final String AUTH_BASIC = "basic";
	public static final String AUTH_MAXAUTH = "maxauth";
	public static final String AUTH_FORM = "form";
	private String user;
	private String password;
	private boolean ssl = false;
	private boolean mt = false;
	private boolean lean = false;
	private String publicURI = null;
	private String appContext = "maximo";
	private String apiContext = "oslc";
	private String appURI = null;
	private String tenantcode = "00";
	
	public Options host(String host)
	{
		this.host = host;
		return this;
	}
	
	public Options maxrest()
	{
		appContext = "maxrest";
		return this;
	}
	
	public Options appContext(String context)
	{
		appContext = context;
		return this;
	}
	
	public Options apiContext(String apiContext)
	{
		this.apiContext = apiContext;
		return this;
	}
	
	public Options https()
	{
		this.ssl = true;
		return this;
	}
	
	public Options http()
	{
		this.ssl = false;
		return this;
	}
	
	public Options port(int port)
	{
		this.port = port;
		return this;
	}
	
	public Options auth(String authMode)
	{
		this.authMode = authMode;
		return this;
	}
	
	public Options mt(boolean mtMode){
		this.mt = mtMode;
		return this;
	}
	
	public Options user(String user)
	{
		this.user = user;
		return this;
	}

	public Options password(String password)
	{
		this.password = password;
		return this;
	}
	
	public Options AppURI(String appURI)
	{
		this.appURI = appURI;
		return this;
	}
	
	public Options lean(boolean lean){
		this.lean = lean;
		return this;
	}
	
	public Options tenantCode(String tenantCode){
		this.tenantcode = tenantCode;
		return this;
	}
	
	String getPassword()
	{
		return this.password;
	}
	
	String getUser()
	{
		return this.user;
	}
	
	public boolean isBasicAuth()
	{
		return this.authMode.equals(AUTH_BASIC);
	}
	
	public boolean isFormAuth()
	{
		return this.authMode.equals(AUTH_FORM);
	}

	public boolean isMaxAuth()
	{
		return this.authMode.equals(AUTH_MAXAUTH);
	}
	
	public boolean isMultiTenancy()
	{
		return this.mt;
	}
	
	public boolean isLean(){
		return this.lean;
	}
	
	public String getHost(){
		return this.host;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public String getTenantCode(){
		return this.tenantcode;
	}
	//Get app URI
	
	String getAppURI()
	{
		if(appURI != null){
			if(mt == true && !appURI.contains("tenantcode")) appURI+=(appURI.contains("?")?"":"?")+"&_tenantcode="+tenantcode;
			if(this.isLean()){
				if(this.appURI.contains("&lean=0")){
					this.appURI = this.appURI.replace("&lean=0", "&lean=1");
				}else if(!this.appURI.contains("&lean=1")){
					this.appURI+=(appURI.contains("?")?"":"?")+"&lean=1";
				}
				return this.appURI;
			}else{
				if(this.appURI.contains("&lean=1")){
					this.appURI = this.appURI.replace("&lean=1", "&lean=0");
				}else if(!this.appURI.contains("&lean=0")){
					this.appURI+=(appURI.contains("?")?"":"?")+"&lean=0";
				}
				return this.appURI;	
			}		
		}
		StringBuffer strb = new StringBuffer(ssl?"https://":"http://");
		strb.append(host);
		if(this.port != null){
			strb.append(":"+String.valueOf(port));
		}
		strb.append("/"+appContext).append("/"+this.apiContext);
		if(mt == true){
			strb.append(strb.toString().contains("?")?"":"?").append("&_tenantcode="+tenantcode);
		}
		if(lean == true){
			strb.append(strb.toString().contains("?")?"":"?").append("&lean=1");
		}
		this.appURI = strb.toString();
		return this.appURI;
	}

	//Get public URI
	
	String getPublicURI()
	{
		if(publicURI != null) return this.publicURI;
		else if(appURI != null){
			String[] strs = appURI.split("/")[2].split(":");
			this.host = strs[0];
			if(strs.length>1)
				this.port = Integer.valueOf(strs[1]);
		}
		StringBuffer strb = new StringBuffer(ssl?"https://":"http://");
		strb.append(host).append(":"+String.valueOf(port)).append("/"+appContext).append("/"+this.apiContext);
		//if(mt == true) strb.append("?&_tenantcode="+tenantcode);
		this.publicURI = strb.toString();
		return this.publicURI;
	}
}
