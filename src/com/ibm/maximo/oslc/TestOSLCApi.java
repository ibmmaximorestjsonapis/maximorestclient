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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.json.*;
import javax.xml.datatype.DatatypeConfigurationException;

import com.ibm.maximo.oslc.Attachment;
import com.ibm.maximo.oslc.AttachmentSet;
import com.ibm.maximo.oslc.MaximoConnector;
import com.ibm.maximo.oslc.Options;
import com.ibm.maximo.oslc.OslcException;
import com.ibm.maximo.oslc.QueryWhere;
import com.ibm.maximo.oslc.Resource;
import com.ibm.maximo.oslc.ResourceSet;
import com.ibm.maximo.oslc.SavedQuery;
import com.ibm.maximo.oslc.Util;

/**
 * Note: the create/update/delete operation will cause the data change.
 * @author zhengy
 *
 */

public class TestOSLCApi{

	public static void main(String[] args) throws OslcException, IOException,
			InterruptedException, DatatypeConfigurationException {
		// connection
		Util.jsonPrettyPrinter("*******************connection******************");
		MaximoConnector mc = new MaximoConnector(new Options().user("murthy")
				.password("murthy").mt(false).lean(true).auth("maxauth")
				.host("localhost").port(7001)).debug(true);
		mc.connect();
		// Example for SELECT
		Util.jsonPrettyPrinter("*******************Example for SELECT******************");
		ResourceSet selectSet = mc.resourceSet("MXWODETAIL")
				.select("spi:wonum", "spi:status", "spi:statusdate")
				.pageSize(2000).fetch(null);
		Util.jsonPrettyPrinter(selectSet.count());
		
		
		// Example for SELECT with Additional Params *NEW
		Util.jsonPrettyPrinter("*******************Example for SELECT with Params******************");
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("oslc.pageSize",50);
		paramsMap.put("oslc.where", "spi:assetnum=\"1001\"");
		paramsMap.put("oslc.select", "spi:assetnum");
		ResourceSet selectWithParamsSet = mc.resourceSet("MXASSET").fetchWithAddtionalParams(paramsMap);
		Util.jsonPrettyPrinter(selectWithParamsSet.toJSON());
		
		// Example for for CREATE and DELETE 
		Util.jsonPrettyPrinter("*******************Example for for CREATE and DELETE******************");
		Util.jsonPrettyPrinter(selectSet.totalCount());
		
		JsonObject jo = null;
		if(mc.isLean()){
			jo = Json.createObjectBuilder()
					.add("siteid", "BEDFORD").add("description", "test")
					.build();
		}else{
			jo = Json.createObjectBuilder()
					.add("spi:siteid", "BEDFORD").add("spi:description", "test")
					.build();
		}
		

		JsonObject jo2 = Json.createObjectBuilder()
				.add("spi:description", "test2").build();

		Resource createRes = selectSet.create(jo, "wonum", "status",
				"statusdate", "description");
		String link = createRes.getURI();
		Resource createRes2 = mc.resource(link);
		createRes.load("wonum", "status", "statusdate", "description");
		Map<String, Object> getParamsMap = new HashMap<String, Object>();
		getParamsMap.put("oslc.properties", "wonum,status,statusdate,description");
		createRes2.loadWithAdditionalParams(getParamsMap);
		createRes.update(jo2);
		
		
		// Example for for CREATE with Additional Headers *NEW
		Util.jsonPrettyPrinter("*******************Example for for CREATE with Additional Headers******************");
		Util.jsonPrettyPrinter(selectSet.totalCount());
		JsonObject addJo = null;
		if(mc.isLean()){
			addJo = Json.createObjectBuilder()
					.add("siteid", "BEDFORD").add("description", "test")
					.build();
		}else{
			addJo = Json.createObjectBuilder()
					.add("spi:siteid", "BEDFORD").add("spi:description", "test")
					.build();
		}
		
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Properties", "wonum");
		Resource createRes3 = selectSet.create(addJo, headers);
		Util.jsonPrettyPrinter(createRes3.toJSON());
		

		//Example for delete
		createRes2.delete();
		Util.jsonPrettyPrinter(selectSet.totalCount());
		
		//Example for update(patch/merge) *UPDATED 
		ResourceSet reSet = mc.resourceSet("MXPO").where(new QueryWhere().and("spi:status").in("WAPPR")).fetch();
		ResourceSet reSetWithoutWhere = mc.resourceSet("MXPO").fetch();
		Resource poRes = null;
		//Patch
		if(reSet.count() > 0){
			poRes = reSet.member(0);
			if(mc.isLean()){
				JsonObject polineObjIn = Json.createObjectBuilder().add("polinenum",1).add("itemnum", "560-00")
						.add("storeloc", "CENTRAL").build();
				JsonArray polineArr = Json.createArrayBuilder().add(polineObjIn).build();
				JsonObject polineObj = Json.createObjectBuilder().add("poline", polineArr).build();
		
				poRes.update(polineObj,"poline");
				Util.jsonPrettyPrinter(poRes.toJSON());
				Util.jsonPrettyPrinter(poRes.toJSON().get("poline"));
			}else{
				JsonObject polineObjIn = Json.createObjectBuilder().add("spi:polinenum",1).add("spi:itemnum", "560-00")
						.add("spi:storeloc", "CENTRAL").build();
				JsonArray polineArr = Json.createArrayBuilder().add(polineObjIn).build();
				JsonObject polineObj = Json.createObjectBuilder().add("spi:poline", polineArr).build();
				
				poRes.update(polineObj,"spi:poline");
				Util.jsonPrettyPrinter(poRes.toJSON());
				Util.jsonPrettyPrinter(poRes.toJSON().get("spi:poline"));
			}
			
			
			if(mc.isLean()){
				JsonObject polineObjIn = Json.createObjectBuilder().add("polinenum",1).add("itemnum", "0-0031")
						.add("storeloc", "CENTRAL").build();
				JsonArray polineArr = Json.createArrayBuilder().add(polineObjIn).build();
				JsonObject polineObj = Json.createObjectBuilder().add("poline", polineArr).build();
				
				poRes.update(polineObj).reload();
				Util.jsonPrettyPrinter(poRes.toJSON());
				Util.jsonPrettyPrinter(poRes.toJSON().get("poline"));
			}else{
				JsonObject polineObjIn = Json.createObjectBuilder().add("spi:polinenum",1).add("spi:itemnum", "0-0031")
						.add("spi:storeloc", "CENTRAL").build();
				JsonArray polineArr = Json.createArrayBuilder().add(polineObjIn).build();
				JsonObject polineObj = Json.createObjectBuilder().add("spi:poline", polineArr).build();
				
				poRes.update(polineObj,"spi:poline");
				Util.jsonPrettyPrinter(poRes.toJSON());
				Util.jsonPrettyPrinter(poRes.toJSON().get("spi:poline"));
			}
			
		}else{
			poRes = reSetWithoutWhere.member(0);
		}

		//Merge
		if(mc.isLean()){
			JsonObject polineObjIn = Json.createObjectBuilder().add("polinenum",1).add("itemnum", "560-00")
					.add("storeloc", "CENTRAL").build();
			JsonArray polineArr = Json.createArrayBuilder().add(polineObjIn).build();
			JsonObject polineObj = Json.createObjectBuilder().add("poline", polineArr).build();
			
			poRes.merge(polineObj,"poline");
			Util.jsonPrettyPrinter(poRes.toJSON());
			Util.jsonPrettyPrinter(poRes.toJSON().get("poline"));
		}else{
			JsonObject polineObjIn = Json.createObjectBuilder().add("spi:polinenum",1).add("spi:itemnum", "560-00")
					.add("spi:storeloc", "CENTRAL").build();
			JsonArray polineArr = Json.createArrayBuilder().add(polineObjIn).build();
			JsonObject polineObj = Json.createObjectBuilder().add("spi:poline", polineArr).build();

			poRes.merge(polineObj,"spi:poline");
			Util.jsonPrettyPrinter(poRes.toJSON());
			Util.jsonPrettyPrinter(poRes.toJSON().get("spi:poline"));
		}
		
			
		// Example for WHERE
		ResourceSet whereSet = mc
				.resourceSet("MXWODETAIL")
				.where((new QueryWhere()).and("spi:status").in("APPR")
				.and("spi:statusdate").lt("2000-07-07T09:50:00-04:00"))
				.select("spi:wonum", "spi:status", "spi:assetnum")
				.fetch(null);
		Util.jsonPrettyPrinter("*******************Example for WHERE******************");
		Util.jsonPrettyPrinter(whereSet.toJSON());
		Util.jsonPrettyPrinter(whereSet.count());
		
		// Example for Action
		whereSet.where(new QueryWhere().and("spi:status").in("WAPPR")).fetch(null);
		Util.jsonPrettyPrinter(whereSet.count());		
		if(whereSet.count() > 0){
			Resource re = whereSet.member(0);
			Util.jsonPrettyPrinter(re.toJSON());
			JsonObject joAction = Json.createObjectBuilder().add("status", "APPR").build();
			re.invokeAction("wsmethod:changeStatus", joAction);
			Util.jsonPrettyPrinter(re.toJSON());
		}
		
		// Example for NEXTPAGE/PREVIOUSPAGE
		ResourceSet nextPageSet = mc.resourceSet("MXSR")
				.select("spi:description", "spi:ticketid").pageSize(5)
				.fetch(null);
		Util.jsonPrettyPrinter("*******************Example for NEXTPAGE/PREVIOUSPAGE******************");
		Util.jsonPrettyPrinter(new String(nextPageSet.toJSONBytes()));
		Util.jsonPrettyPrinter(nextPageSet.count());
		Util.jsonPrettyPrinter(nextPageSet.nextPage().toJSON());
		Util.jsonPrettyPrinter(nextPageSet.previousPage().toJSON());
		Util.jsonPrettyPrinter(whereSet.count());
		
		
		//Example for StablePaging
		ResourceSet spSet = mc.resourceSet("MXSR")
				.select("spi:description", "spi:ticketid").pageSize(2).stablePaging(true)
				.fetch(null);
		Util.jsonPrettyPrinter("*******************Example for NEXTPAGE/PREVIOUSPAGE******************");
		Util.jsonPrettyPrinter(new String(spSet.toJSONBytes()));
		Util.jsonPrettyPrinter(spSet.nextPage().toJSON());
		spSet.nextPage();
		

		// Example for WHERE
		Util.jsonPrettyPrinter("*******************Example for Another WHERE******************");
		ResourceSet anotherWhereSet = mc
				.resourceSet("MXWODETAIL")
				.where("status=\"APPR\"")
				.select("wonum", "status", "statusdate", "$asset.description",
						"assetnum.assettag").pageSize(5).fetch(null);
		Util.jsonPrettyPrinter(anotherWhereSet.totalCount());

		
		// Example for Disconnect
		Util.jsonPrettyPrinter("*******************Example for Disconnect******************");
		mc.disconnect();
		mc.connect();
		
		// Example for ATTACHMENT/ATTACHMENTSET
		// *need DOCLINKS
		Util.jsonPrettyPrinter("*******************Example for ATTACHMENT/ATTACHMENTSET******************");
		Resource res = selectSet.member(0).load();
		AttachmentSet ats = res.attachmentSet();
		
		String str = "hello world @ "
				+ Calendar.getInstance().getTime().toString();
		byte[] data = str.getBytes("utf-8");
		Attachment att = new Attachment().name("attachment.txt")
				.description("test").data(data).meta("FILE", "Attachments");
		att = ats.create(att);
		Util.jsonPrettyPrinter(new String(att.toDoc(), "utf-8"));
		
		String str2 = "hello world @ "
				+ Calendar.getInstance().getTime().toString();
		byte[] data2 = str2.getBytes("utf-8");
		Attachment att2 = new Attachment().name("attachment.txt")
				.description("test").data(data2).meta("FILE", "Attachments");
		att2 = ats.create(att2);
		
		
		String str3 = "hello world @ "
				+ Calendar.getInstance().getTime().toString();
		byte[] data3 = str3.getBytes("utf-8");
		Attachment att3 = new Attachment().name("attachment.txt")
				.description("test").data(data3).meta("FILE", "Attachments");
		att3= ats.create(att3);

		//LoadDocMeta with Attachment/MaximoConnector
		
		Util.jsonPrettyPrinter(att2.toDocMeta());
		Util.jsonPrettyPrinter(mc.attachmentDocMeta(att2.getURI()));
		
		
		//LoadDocContent with Attachment/MaximoConnector
		Util.jsonPrettyPrinter(new String(att2.toDoc(), "utf-8"));
		Util.jsonPrettyPrinter(new String(mc.getAttachmentData(att2.getURI()),"utf-8"));
		

		
		//Delete by MaximoConnector
		mc.deleteAttachment(att2.getURI());
		
		//Delete by id
		
		String id = null;
		if (mc.getOptions().isLean()) {
			id = att3.toDocMeta().getString("identifier");
		} else {
			id = att3.toDocMeta().getString("dcterms:identifier");
		}
		ats.delete(id);

		Attachment att4 = ats.member(id);

		if (att4 == null) {
			Util.jsonPrettyPrinter("No such attachment");
		} else {
			Util.jsonPrettyPrinter(new String(att4.load().toDoc(), "utf-8"));
			Util.jsonPrettyPrinter(att2.loadMeta().toDocMeta());
		}
		
		//Delete by Attachment
		att.delete();
		
		// Example for SAVEDQUERY *need existed savequery
		Util.jsonPrettyPrinter("*******************Example for SAVEDQUERY******************");
		ResourceSet set5 = mc
				.resourceSet("mxpo")
				.savedQuery(
						new SavedQuery().name("poforStatus")
								.addParam("status", "APPR")
								.addParam("like", true)).select("status")
				.fetch(null);
		Util.jsonPrettyPrinter(set5.toJSON());
		Util.jsonPrettyPrinter(set5.totalCount());

		// Example for HASTERMS *need pre-setup for hasterms
		Util.jsonPrettyPrinter("*******************Example for HASTERMS******************");
		ResourceSet set6 = mc.resourceSet("OSLCMXSR")
				.hasTerms("email", "finance")
				.select("spi:description", "spi:ticketid").pageSize(5)
				.fetch(null);
		Util.jsonPrettyPrinter(set6.toJSON());
	
	}
}
