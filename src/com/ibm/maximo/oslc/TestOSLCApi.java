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
import java.util.Calendar;
import java.util.Date;
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

public class TestOSLCApi{

	// MultiThread
	private static class ClientThread extends Thread {
		MaximoConnector mc = null;

		public ClientThread(String str, MaximoConnector mc) {
			super(str);
			this.mc = mc;
		}

		@Override
		public void run() {
			try {

				ResourceSet set2 = mc.resourceSet("MXWODETAIL")
						.select("wonum", "status", "statusdate")
						.pageSize(50).fetch(null);
				Util.jsonPrettyPrinter(set2.totalCount());
				JsonObject jo = Json.createObjectBuilder()
						.add("siteid", "BEDFORD").add("description", "test")
						.build();

				JsonObject jo2 = Json.createObjectBuilder()
						.add("spi:escription", "test2").build();

				Resource res3 = set2.create(jo, "wonum", "status",
						"statusdate", "description");
				String link = res3.getURI();
				Resource res4 = mc.resource(link);
				// Util.jsonPrettyPrinter(res3.toJSON());
				res3.load("wonum", "status", "statusdate", "description");
				res4.load("wonum", "status", "statusdate", "description");
				res3.update(jo2);
				Util.jsonPrettyPrinter(res3.reload().toJSON());
				Util.jsonPrettyPrinter(res4.reload().toJSON());

				res3.delete();
				Util.jsonPrettyPrinter(set2.totalCount());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class UpdateThread extends Thread {
		MaximoConnector mc = null;

		public UpdateThread(String str, MaximoConnector mc) {
			super(str);
			this.mc = mc;
		}

		@Override
		public void run() {
			try {

				ResourceSet set2 = mc
						.resourceSet("MXWODETAIL")
						.select("spi:wonum", "spi:status", "spi:statusdate")
						.where(new QueryWhere().where("spi:status").equalTo(
								"WAPPR")).pageSize(50).fetch(null);
				Util.jsonPrettyPrinter(set2.count());
				JsonObject jo2 = Json.createObjectBuilder()
						.add("spi:description", "test2").build();
				JsonObject jo3 = Json.createObjectBuilder()
						.add("spi:description", "test3").build();

				Resource res3 = set2.member(0);
				String link = res3.getURI();
				Resource res4 = mc.resource(link);
				// Util.jsonPrettyPrinter(res3.toJSON());
				res3.load("spi:wonum", "spi:status", "spi:statusdate",
						"spi:description");
				res4.load("spi:wonum", "spi:status", "spi:statusdate",
						"spi:description");
				res3.update(jo2);
				res4.update(jo3);
				Util.jsonPrettyPrinter(res3.reload("spi:description").toJSON());
				Util.jsonPrettyPrinter(res4.reload("spi:description").toJSON());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws OslcException, IOException,
			InterruptedException, DatatypeConfigurationException {
		// connection
		Util.jsonPrettyPrinter("*******************connection******************");
		MaximoConnector mc = new MaximoConnector(new Options().user("wilson")
				.password("wilson").mt(false).lean(true).auth("maxauth")
				.host("localhost").port(7001)).debug(true);
		mc.connect();
		// Example for SELECT
		Util.jsonPrettyPrinter("*******************Example for SELECT******************");
		ResourceSet set2 = mc.resourceSet("MXWODETAIL")
				.select("spi:wonum", "spi:status", "spi:statusdate")
				.pageSize(2000).fetch(null);
		Util.jsonPrettyPrinter(set2.count());
		// Example for for CREATE and DELETE
//		Util.jsonPrettyPrinter("*******************Example for for CREATE and DELETE******************");
//		if(mc.getOptions().isLean()){
//			java.util.List<ClientThread> list = new java.util.ArrayList<ClientThread>();
//
//			for (int i = 1; i < 20; i++) {
//				ClientThread c = new ClientThread(String.valueOf(i), mc);
//				list.add(c);
//				c.start();
//			}
//
//			for (ClientThread c : list) {
//				c.join();
//			}
//		}else{
//			java.util.List<UpdateThread> list = new java.util.ArrayList<UpdateThread>();
//
//			for (int i = 1; i < 20; i++) {
//				UpdateThread c = new UpdateThread(String.valueOf(i), mc);
//				list.add(c);
//				c.start();
//			}
//
//			for (UpdateThread c : list) {
//				c.join();
//			}
//		}
		
		//Example for update(patch/merge);
		ResourceSet reSet = mc.resourceSet("MXPO").fetch();
		Resource poRes  = reSet.member(0);
		JsonObject polineObjIn = Json.createObjectBuilder().add("polinenum",1).add("itemnum", "560-00")
				.add("storeloc", "CENTRAL").build();
		JsonArray polineArr = Json.createArrayBuilder().add(polineObjIn).build();
		JsonObject polineObj = Json.createObjectBuilder().add("poline", polineArr).build();
		
		//patch
		poRes.update(polineObj);
		Util.jsonPrettyPrinter(poRes.toJSON().get("poline"));
		
		JsonObject polineObjIn2 = Json.createObjectBuilder().add("polinenum",2).add("itemnum", "0-0031")
				.add("storeloc", "CENTRAL").build();
		JsonArray polineArr2 = Json.createArrayBuilder().add(polineObjIn2).build();
		JsonObject polineObj2 = Json.createObjectBuilder().add("poline", polineArr2).build();
		poRes.update(polineObj2);
		Util.jsonPrettyPrinter(poRes.toJSON().get("poline"));
		
		//Merge
		JsonObject polineObjIn3 = Json.createObjectBuilder().add("polinenum",1).add("itemnum", "0-0031")
				.add("storeloc", "CENTRAL").build();
		JsonArray polineArr3 = Json.createArrayBuilder().add(polineObjIn3).build();
		JsonObject polineObj3 = Json.createObjectBuilder().add("poline", polineArr3).build();
		poRes.merge(polineObj3);
		Util.jsonPrettyPrinter(poRes.toJSON().get("poline"));
		
		//Example for DELETE
		
		ResourceSet vendorSet = mc.resourceSet("mxsr").fetch();
		Resource vendor = vendorSet.member(vendorSet.count()-1);
		vendor.delete();
		
		
		// Example for WHERE
		ResourceSet set3 = mc
				.resourceSet("MXWODETAIL")
				.where((new QueryWhere()).and("spi:status").in("APPR")
						.and("spi:statusdate").lt("2000-07-07T09:50:00-04:00"))
						//.and("spi:assetnum").equalTo("11450"))
				.select("spi:wonum", "spi:status", "spi:assetnum")
				.fetch(null);
		Util.jsonPrettyPrinter("*******************Example for WHERE******************");
		Util.jsonPrettyPrinter(set3.toJSON());
		Util.jsonPrettyPrinter(set3.count());
		
		// Example for Action
		set3.where(new QueryWhere().and("spi:status").in("WAPPR")).fetch(null);
		Util.jsonPrettyPrinter(set3.count());		
		Resource re = set3.member(0);
		Util.jsonPrettyPrinter(re.toJSON());
		JsonObject jo = Json.createObjectBuilder()
				.add("status", "APPR").build();
		re.invokeAction("wsmethod:changeStatus", jo);
		Util.jsonPrettyPrinter(re.toJSON());
		
		
		// Example for NEXTPAGE/PREVIOUSPAGE
		ResourceSet set = mc.resourceSet("MXSR")
				.select("spi:description", "spi:ticketid").pageSize(5)
				.fetch(null);
		Util.jsonPrettyPrinter("*******************Example for NEXTPAGE/PREVIOUSPAGE******************");
		Util.jsonPrettyPrinter(new String(set.toJSONBytes()));
		Util.jsonPrettyPrinter(set.count());
		Util.jsonPrettyPrinter(set.nextPage().toJSON());
		Util.jsonPrettyPrinter(set.previousPage().toJSON());
		Util.jsonPrettyPrinter(set3.count());

		// Example for ATTACHMENT/ATTACHMENTSET
		Util.jsonPrettyPrinter("*******************Example for ATTACHMENT/ATTACHMENTSET******************");
		Resource res = set2.member(0).load();
		AttachmentSet ats = res.attachmentSet();
		
		String str = "hello world @ "
				+ Calendar.getInstance().getTime().toString();
		byte[] data = str.getBytes("utf-8");
		Attachment att = new Attachment().name("attachment.txt")
				.description("test").data(data).meta("FILE", "Attachments");
		att = ats.create("DOCLINKS", att);
		Util.jsonPrettyPrinter(new String(att.toDoc(), "utf-8"));
		
		String str2 = "hello world @ "
				+ Calendar.getInstance().getTime().toString();
		byte[] data2 = str2.getBytes("utf-8");
		Attachment att2 = new Attachment().name("attachment.txt")
				.description("test").data(data2).meta("FILE", "Attachments");
		att2 = ats.create(att2);
		
		
		String str3 = "hello world @ "
				+ Calendar.getInstance().getTime().toString();
		byte[] data3 = str2.getBytes("utf-8");
		Attachment att3 = new Attachment().name("attachment.txt")
				.description("test").data(data3).meta("FILE", "Attachments");
		att3= ats.create(att3);

		//LoadDocMeta with Attachment/MaximoConnector
		
		Util.jsonPrettyPrinter(att2.toDocMeta());
		Util.jsonPrettyPrinter(mc.attachmentDocMeta(att2.getURI()));
		
		
		//LoadDocContent with Attachment/MaximoConnector
		Util.jsonPrettyPrinter(new String(att2.toDoc(), "utf-8"));
		Util.jsonPrettyPrinter(new String(mc.getAttachmentData(att2.getURI()),"utf-8"));
		
		// *need DOCLINKS
		//Delete by Attachment
		att.delete();
		
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

		// Example for WHERE
		Util.jsonPrettyPrinter("*******************Example for Another WHERE******************");
		ResourceSet set4 = mc
				.resourceSet("MXWODETAIL")
				.where("status=\"APPR\"")
				.select("wonum", "status", "statusdate", "$asset.description",
						"assetnum.assettag").pageSize(5).fetch(null);
		Util.jsonPrettyPrinter(set4.totalCount());

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

		// Example for Disconnect
		Util.jsonPrettyPrinter("*******************Example for Disconnect******************");
		mc.disconnect();
		mc.connect();
	}
}
