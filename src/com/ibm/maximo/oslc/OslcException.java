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

import javax.json.JsonObject;

public class OslcException extends Exception {
	/**
	 * 
	 */
	private int errorCode = 400;

	public OslcException(int errorCode, String message, Throwable t) {
		super(message, t);

	}

	public OslcException(int errorCode, String message) {
		super(message);
	}

	public int getErrorCode() {
		return this.errorCode;
	}

	public OslcException(String message) {
		super(message);
	}

	public OslcException(JsonObject jo) {
		super(jo.containsKey("Error") ? jo.getJsonObject("Error").getString(
				"message") : jo.getJsonObject("oslc:Error").getString(
				"oslc:message"));
	}
}
