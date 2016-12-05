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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.*;
import javax.xml.datatype.DatatypeConfigurationException;

/**
 * 
 * {@code ResourceSet} implement the operations on {@code ResourceSet}. It
 * provides the set of Resource.
 * 
 * <p>
 * This object can be created by {@code MaximoConnector}. The following code
 * shows how to create {@code MaximoConnector}
 * </p>
 * 
 * <pre>
 * <code>
 * ResourceSet rs = mc.resourceSet(osName);
 * ResourceSet rs = mc.resourceSet(URL);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples demonstrate how to build a new {@code ResourceSet}
 * </p>
 * 
 * <pre>
 * <code>
 * ResourceSet rs = new ResourceSet(osName);
 * ResourceSet rs = new ResourceSet(maximoConnector);
 * ResourceSet rs = new ResourceSet(osName, maximoConnector);
 * ResourceSet rs = new ResourceSet(URL, maximoConnector);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to set {@code ResourceSet} data from
 * {@code ResourceSet}
 * </p>
 * 
 * <pre>
 * <code>
 * rs.where(queryWhere).select(querySelect).hasTerms(terms).pageSize(pageSize)
 * rs.paging();
 * rs.stablePageing();
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to fetch, load, reload, go to next page, go
 * back to previous page, get savedQuery for {@code ResourceSet} data
 * </p>
 * 
 * <pre>
 * <code>
 * rs.fetch(mapOptions);
 * rs.load();
 * rs.reload();
 * rs.nextPage();
 * rs.previousPage();
 * rs.savedQuery(savedQuery);
 * rs.savedQuery(name, paramValues);
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to get {@code ResourceSet} data from
 * {@code ResourceSet}
 * </p>
 * 
 * <pre>
 * <code>
 * JsonObject jo = rs.toJSON(); 
 * byte[] jodata = rs.toJSONBytes();
 * </code>
 * </pre>
 * 
 * <p>
 * The following examples show how to create, get and delete {@code Resource}
 * from {@code ResourceSet}
 * </p>
 * 
 * <pre>
 * <code>
 * Resource rs = fetchMember(uri, properties);
 * Resource rs = member(index);
 * Resource rs = create(jsonObject, properties);
 * </code>
 * </pre>
 * 
 * <p>
 * The following example shows how to get the this page size from
 * {@code ResourceSet}
 * </p>
 * 
 * <pre>
 * <code>
 * int count = rs.count();
 * int totalCount = rs.totalCount();
 * int totalCount = rs.totalCount(true);
 * </code>
 * </pre>
 *
 */
public class ResourceSet {
	private int pageSize = -1;
	private String osName;
	private String whereClause = null;
	private String selectClause = null;
	private String osURI;
	private String publicURI;
	private String appURI;
	private List<String> orderBy = new ArrayList<String>();
	private String savedQuery = null;
	private StringBuffer strbWhere;
	private StringBuffer searchTerms;
	private StringBuffer searchAttributes;
	private JsonObject jsonObject;
	private MaximoConnector mc;
	private boolean paging = false;
	private boolean stablePaging = false;
	private boolean isLoaded = false;
	private JsonArray jsonArray;

	public ResourceSet(String osName) {
		this.osName = osName;
	}

	public ResourceSet(MaximoConnector mc) {
		this.mc = mc;
	}

	public ResourceSet(String osName, MaximoConnector mc) {
		this.osName = osName;
		this.publicURI = mc.getCurrentURI();
		this.mc = mc;
	}

	public ResourceSet(URL publicURI, MaximoConnector mc) {
		this.mc = mc;
		this.publicURI = publicURI.toString();
	}

	/**
	 * Get current URI
	 * 
	 * 
	 */
	public String getAppURI() {
		return this.appURI;
	}

	public String getPublicURI() {
		return this.publicURI;
	}

	public String getOsURI() {
		return this.osURI;
	}

	/**
	 * Get ResourceSet data in JSON
	 * 
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public JsonObject toJSON() throws IOException, OslcException {
		// this.load();
		return this.jsonObject;
	}

	/**
	 * Get ResourceSet data in JSONBytes
	 * 
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public byte[] toJSONBytes() throws OslcException, IOException {
		// this.load();
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		Json.createWriter(bo).writeObject(this.jsonObject);
		bo.close();
		return bo.toByteArray();
	}

	// Set whereClause
	public ResourceSet where(String whereClause) {
		this.whereClause = whereClause;
		return this;
	}

	public ResourceSet where(QueryWhere where) {
		this.whereClause = where.whereClause();
		return this;
	}
	
	public ResourceSet searchAttributes(String... attributes) {
		this.searchAttributes = new StringBuffer();
		for (String attribute : attributes) {
			searchAttributes.append("" + attribute + ",");
		}
		return this;
	}

	public ResourceSet hasTerms(String... terms) {
		searchTerms = new StringBuffer();
		for (String term : terms) {
			searchTerms.append("\"" + term + "\",");
		}
		return this;
	}

	// Set selectClause
	public ResourceSet select(String... selectClause) {
		this.selectClause = (new QuerySelect()).select(selectClause);
		return this;
	}

	public ResourceSet pageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	// &oslc.paging=true - if paging is false - do not add the query parameter
	public ResourceSet paging(boolean type) {
		this.paging = type;
		return this;
	}

	public ResourceSet stablePaging(boolean type) {
		this.stablePaging = type;
		return this;
	}
	
	public ResourceSet orderBy(String... orderByProperties){
		for(String property: orderByProperties){
			this.orderBy.add(property);
		}
		return this;
	}

	/**
	 * Fetching the data for ResourceSet
	 * 
	 * 
	 * @throws OslcException
	 * @throws IOException
	 */
	
	public ResourceSet fetch() throws OslcException, IOException {
		this.fetch(null);
		return this;
	}
	
	/**
	 * Fetching the data for ResourceSet with arbitrary parameters
	 * 
	 * 
	 * @param additionalParams
	 * 
	 * @throws OslcException
	 * @throws IOException
	 */
	
	public ResourceSet fetchWithAddtionalParams(Map<String, Object> additionalParams) throws OslcException, IOException {
		return this.fetchWithAddtionalHeadersAndParams(additionalParams, null);
	}
	
	/**
	 * Fetching the data for ResourceSet with arbitrary headers
	 * 
	 * 
	 * @param additionalHeaders
	 * 
	 * @throws OslcException
	 * @throws IOException
	 */
	
	public ResourceSet fetchWithAddtionalHeaders(Map<String, Object> additionalHeaders) throws OslcException, IOException {
		return this.fetchWithAddtionalHeadersAndParams(null, additionalHeaders);
	}
	
	/**
	 * Fetching the data for ResourceSet with arbitrary parameters and headers
	 * 
	 * 
	 * @param additionalParams
	 * @param additionalHeaders
	 * 
	 * @throws OslcException
	 * @throws IOException
	 */
	
	public ResourceSet fetchWithAddtionalHeadersAndParams(Map<String, Object> additionalParams, Map<String, Object> additionalHeaders) throws OslcException, IOException {
		this.buildURI();
		StringBuilder strb = new StringBuilder();
		strb.append(this.appURI);
		if(!this.appURI.contains("?")){
			strb.append("?");
		}
		if(additionalParams != null && !additionalParams.isEmpty()){
			Set<Map.Entry<String, Object>> entrySet = additionalParams.entrySet();
			for(Map.Entry<String, Object> entry: entrySet){
				StringBuilder singleParam = new StringBuilder();
				singleParam.append("&").append(entry.getKey()).append("=");
				singleParam.append(Util.urlEncode(entry.getValue().toString()));
				strb.append(singleParam.toString());
			}
		}
		this.appURI = strb.toString();
		if(additionalHeaders !=null && !additionalHeaders.isEmpty()){
			this.jsonObject = this.mc.get(this.appURI, additionalHeaders);
		}else{
			this.jsonObject = this.mc.get(this.appURI);
		}
		if (this.jsonObject.containsKey("rdfs:member")) {
			this.jsonArray = (JsonArray) this.jsonObject.get("rdfs:member");
		} else {
			this.jsonArray = (JsonArray) this.jsonObject.get("member");
		}
		isLoaded = true;
		return this;
	}
	
	public ResourceSet fetch(Map options) throws OslcException, IOException {
		try {
			this.buildURI();
		} catch (OslcException e) {
			e.printStackTrace();
		}
		this.jsonObject = this.mc.get(this.appURI);
		if (this.jsonObject.containsKey("rdfs:member")) {
			this.jsonArray = (JsonArray) this.jsonObject.get("rdfs:member");
		} else {
			this.jsonArray = (JsonArray) this.jsonObject.get("member");
		}
		isLoaded = true;
		return this;
	}

	/**
	 * Go to nextPage
	 * 
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */

	public ResourceSet nextPage() throws IOException, OslcException {
		if (this.jsonObject.containsKey("responseInfo")) {
			this.appURI = this.jsonObject.getJsonObject("responseInfo")
					.getJsonObject("nextPage").getString("href");
		} else if (this.jsonObject.containsKey("oslc:responseInfo")) {
			this.appURI = this.jsonObject.getJsonObject("oslc:responseInfo")
					.getJsonObject("oslc:nextPage").getString("rdf:resource");
		}
		this.jsonObject = this.mc.get(this.appURI);
		if (this.jsonObject.containsKey("rdfs:member")) {
			this.jsonArray = (JsonArray) this.jsonObject.get("rdfs:member");
		} else {
			this.jsonArray = (JsonArray) this.jsonObject.get("member");
		}
		return this;
	}

	/**
	 * Go back to previous page
	 * 
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */

	public ResourceSet previousPage() throws IOException, OslcException {
		
		if(this.jsonObject.containsKey("responseInfo") && this.jsonObject.getJsonObject("responseInfo").containsKey("previousPage")){
			this.appURI = this.jsonObject.getJsonObject("responseInfo")
					.getJsonObject("previousPage").getString("href");
		}
		else if(this.jsonObject.containsKey("oslc:responseInfo") && this.jsonObject.getJsonObject("oslc:responseInfo").containsKey("oslc:previousPage")){
			this.appURI = this.jsonObject.getJsonObject("oslc:responseInfo")
					.getJsonObject("oslc:previousPage")
					.getString("rdf:resource");
		}
		else{
			String[] strs = this.appURI.split("\\=|\\&|\\?");
			boolean isPageNo = false;
			int pageno = 0;
			for (String str : strs) {
				if (str.equals("pageno")) {
					isPageNo = true;
				} else if (isPageNo) {
					pageno = Integer.valueOf(str);
					break;
				}
			}
			if (pageno == 2) {
				this.appURI = this.appURI.replace(
						"pageno=" + String.valueOf(pageno), "");
			} else {
				this.appURI = this.appURI.replace(
						"pageno=" + String.valueOf(pageno),
						"pageno=" + String.valueOf(pageno - 1));
			}
		}
		this.jsonObject = this.mc.get(this.appURI);
		if (this.jsonObject.containsKey("rdfs:member")) {
			this.jsonArray = (JsonArray) this.jsonObject.get("rdfs:member");
		} else {
			this.jsonArray = (JsonArray) this.jsonObject.get("member");
		}
		return this;
	}

	/**
	 * Load the current data
	 * 
	 * 
	 * @throws OslcException
	 * @throws IOException
	 */
	public ResourceSet load() throws OslcException, IOException {
		if (isLoaded) {
			return this;
		}
		this.jsonObject = this.mc.get(this.appURI);
		if (this.jsonObject.containsKey("rdfs:member")) {
			this.jsonArray = (JsonArray) this.jsonObject.get("rdfs:member");
		} else {
			this.jsonArray = (JsonArray) this.jsonObject.get("member");
		}
		isLoaded = true;
		return this;
	}

	public ResourceSet reload() throws OslcException, IOException {
		isLoaded = false;
		load();
		return this;
	}

	/**
	 * 
	 * 
	 * @throws DatatypeConfigurationException
	 * @throws UnsupportedEncodingException
	 */
	public ResourceSet savedQuery(String name, Map<String, Object> paramValues) {
		this.savedQuery = new SavedQuery(name, paramValues).savedQueryClause();
		return this;
	}

	public ResourceSet savedQuery(SavedQuery qsaved) {
		this.savedQuery = qsaved.savedQueryClause();
		return this;
	}

	/**
	 * URI Builder
	 * 
	 * @throws OslcException
	 */

	private ResourceSet buildURI() throws OslcException {
		try {
			StringBuilder strb = new StringBuilder();
			// strb.append(this.URI);
			if (this.publicURI != null) {
				strb.append(this.publicURI);//
			} else {
				throw new OslcException("URI_is_invalid");
			}
			if (this.osName != null) {
				strb.append("/os/" + this.osName.toLowerCase());
			} else {
				//throw new OslcException("osName_is_invalid");
			}

			strb.append(this.publicURI.contains("?") ? "" : "?").append(
					"&collectioncount=1");

			this.osURI = strb.toString();

			if (this.selectClause != null) {
				strb.append("&oslc.select="
						+ URLEncoder.encode(this.selectClause, "utf-8"));
			}
			if (this.whereClause != null) {
				strb.append("&oslc.where="
						+ URLEncoder.encode(this.whereClause, "utf-8"));
			} else if (strbWhere != null) {
				strb.append("&oslc.where=" + strbWhere.toString());
			}
			if (this.pageSize != -1) {
				strb.append("&oslc.pageSize=").append(String.valueOf(pageSize));
			}
			if (this.paging == true) {
				strb.append("&oslc.paging=true");
			}
			if (this.searchAttributes != null) {
				strb.append("&searchAttributes="
						+ URLEncoder.encode(this.searchAttributes.substring(0,
								this.searchAttributes.toString().length() - 1),
								"utf-8"));
			}
			if (this.searchTerms != null) {
				strb.append("&oslc.searchTerms="
						+ URLEncoder.encode(this.searchTerms.substring(0,
								this.searchTerms.toString().length() - 1),
								"utf-8"));
			}
			if (this.stablePaging == true) {
				strb.append("&stablepaging=true");
			}
			if (this.savedQuery != null) {
				strb.append("&savedQuery=" + this.savedQuery);
			}
			if (this.orderBy.size()>0){
				strb.append("&oslc.orderBy=");
				for(String property: this.orderBy){
					strb.append("-" + property + ",");
				}
				if(strb.toString().endsWith(",")){
					strb = strb.deleteCharAt(strb.length() - 1);
				}
			}
			this.appURI = strb.toString();
			return this;
		} catch (Exception e) {
			throw new OslcException(500, "error building url", e);
		}
	}

	public Resource fetchMember(String uri, String... properties)
			throws IOException, OslcException {
		StringBuilder strb = new StringBuilder().append(uri);
		if (properties.length > 0) {
			strb.append(uri.contains("?") ? "" : "?").append(
					"&oslc.properties=");
			StringBuilder paramsStrb = new StringBuilder();
			for (String property : properties) {
				paramsStrb.append(property).append(",");
			}
			if(paramsStrb.toString().endsWith(",")){
				paramsStrb = paramsStrb.deleteCharAt(paramsStrb.length() - 1);
			}
			strb.append(Util.urlEncode(paramsStrb.toString()));
		}
		JsonObject jo = this.mc.get(strb.toString());
		return new Resource(jo, this.mc);
	}

	/**
	 * get the member in ResourceSet
	 * 
	 * @param index
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */

	public Resource member(int index) throws OslcException, IOException {
		if (!isLoaded) {
			load();
		}
		if(index >= this.jsonArray.size()){
			return null;
		}
		JsonObject jo = (JsonObject) this.jsonArray.get(index);
		return new Resource(jo, this.mc);
	}

	/**
	 * Create a new Resource with the properties in hearder
	 * 
	 * @param jo
	 * @param properties
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */

	public Resource create(JsonObject jo, String... properties)
			throws IOException, OslcException {
		if (this.osURI == null) {

			try {
				this.buildURI();
			} catch (OslcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JsonObject rjo = this.mc.create(this.osURI, jo, properties);
		this.reload();
		// use the maximo connector to connect to oslc server and then POST data
		// to it
		return new Resource(rjo, this.mc);
		// use the maximo connector to connect to oslc server and then load data
		// from it
	}
	
	public Resource create(JsonObject jo, Map<String, Object> headers, String... properties)
			throws IOException, OslcException {
		if (this.osURI == null) {

			try {
				this.buildURI();
			} catch (OslcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JsonObject rjo = this.mc.create(this.osURI, jo, headers, properties);
		this.reload();
		// use the maximo connector to connect to oslc server and then POST data
		// to it
		return new Resource(rjo, this.mc);
		// use the maximo connector to connect to oslc server and then load data
		// from it
	}
	
	public Resource sync(JsonObject jo, String... properties)
			throws IOException, OslcException {
		if (this.osURI == null) {

			try {
				this.buildURI();
			} catch (OslcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JsonObject rjo = this.mc.sync(this.osURI, jo, properties);
		this.reload();
		// use the maximo connector to connect to oslc server and then POST data
		// to it
		return new Resource(rjo, this.mc);
		// use the maximo connector to connect to oslc server and then load data
		// from it
	}
	
	public Resource sync(JsonObject jo, Map<String, Object> headers, String... properties)
			throws IOException, OslcException {
		if (this.osURI == null) {

			try {
				this.buildURI();
			} catch (OslcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JsonObject rjo = this.mc.sync(this.osURI, jo, headers, properties);
		this.reload();
		// use the maximo connector to connect to oslc server and then POST data
		// to it
		return new Resource(rjo, this.mc);
		// use the maximo connector to connect to oslc server and then load data
		// from it
	}
	
	public Resource mergeSync(JsonObject jo, String... properties)
			throws IOException, OslcException {
		if (this.osURI == null) {

			try {
				this.buildURI();
			} catch (OslcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JsonObject rjo = this.mc.mergeSync(this.osURI, jo, properties);
		this.reload();
		// use the maximo connector to connect to oslc server and then POST data
		// to it
		return new Resource(rjo, this.mc);
		// use the maximo connector to connect to oslc server and then load data
		// from it
	}
	
	public Resource mergeSync(JsonObject jo, Map<String, Object> headers, String... properties)
			throws IOException, OslcException {
		if (this.osURI == null) {
			try {
				this.buildURI();
			} catch (OslcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JsonObject rjo = this.mc.mergeSync(this.osURI, jo, headers, properties);
		this.reload();
		// use the maximo connector to connect to oslc server and then POST data
		// to it
		return new Resource(rjo, this.mc);
		// use the maximo connector to connect to oslc server and then load data
		// from it
	}

	public int configuredPageSize() {
		return this.pageSize;
	}

	/**
	 * Count the total number of Resources by calling RESTful API
	 * 
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	public int totalCount() throws IOException, OslcException {
		if (!isLoaded) {
			load();
		}
		JsonObject jo = null;
		int total = -1;
		if (this.jsonObject.containsKey("oslc:responseInfo")) {
			jo = this.jsonObject.getJsonObject("oslc:responseInfo");
			if (jo.containsKey("oslc:totalCount")) {
				total = jo.getInt("oslc:totalCount");
			} else if (!jo.containsKey("oslc:nextPage")) {
				return this.count();
			}
		} else if (this.jsonObject.containsKey("responseInfo")) {
			jo = this.jsonObject.getJsonObject("responseInfo");
			if (jo.containsKey("totalCount")) {
				total = jo.getInt("totalCount");
			} else if (!jo.containsKey("nextPage")) {
				return this.count();
			}
		}
		total = this.totalCount(true);
		return total;
	}

	/**
	 * Count the total number of Resources.
	 * When fromServer=true, it calls the totalCount API.
	 * When fromServer=false, it calls the RESTful API.
	 * 
	 * @throws IOException
	 * @throws OslcException
	 */
	
	public int totalCount(boolean fromServer) throws IOException, OslcException {
		if (!fromServer) {
			return this.totalCount();
		}
		String appURI = this.appURI + (this.appURI.contains("?") ? "" : "?");
		JsonObject jo = this.mc.get(appURI + "&count=1");
		if (jo.containsKey("totalCount")) {
			return jo.getInt("totalCount");
		} else {
			int tempPageSize = this.configuredPageSize();
			this.pageSize = -1;
			appURI = this.buildURI().appURI;
			jo = this.mc.get(appURI);
			int size = -1;
			if (jo.containsKey("member")) {
				size = jo.getJsonArray("member").size();
			} else if (jo.containsKey("rdfs:member")) {
				size = jo.getJsonArray("rdfs:member").size();
			}
			this.pageSize = tempPageSize;
			this.buildURI();
			return size;
		}
	}

	/**
	 * get current number of Resource by calling RESTful API
	 * 
	 * 
	 * @throws OslcException
	 * @throws IOException
	 */
	public int count() throws OslcException, IOException {
		if (!isLoaded) {
			load();
		}
		int size = -1;
		if (this.jsonObject.containsKey("member")) {
			size = this.jsonObject.getJsonArray("member").size();
		} else if (this.jsonObject.containsKey("rdfs:member")) {
			size = this.jsonObject.getJsonArray("rdfs:member").size();
		}
		return size;
	}
	
	public BulkProcessor bulk(){
		return new BulkProcessor(this.mc,this.osURI);
	}
	
	public Aggregation groupBy(){
		try {
			return new Aggregation(this.mc,this.buildURI().appURI);
		} catch (OslcException e) {
			e.printStackTrace();
		}
		return null;
	}
}
