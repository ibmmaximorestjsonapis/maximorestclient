# Updated

1. Add support to group by for ResourceSet
2. Add support to sync for ResourceSet
3. Add support to bulk for ResourceSet
4. Add new examples about new API in TestOSLCAPI.java.
5. Fix bugs
6. Removed references to `javax.xml.bind.DatatypeConverter.printBase64Binary` since that API is not available on Android.  It now uses commons-codec to get base64 support.

# Maximo Rest Client 1.0 Released!

1. Add support to arbitrary parameters for Resource/ResourceSet
2. Add support to arbitrary headers for get/post/patch/merge/delete
3. Add support to order by for ResourceSet
4. Add support to invoke action with properties for Resource
5. Add new examples about new API in TestOSLCAPI.java.
6. Fix bugs

# I. Introduction
-----

The Maximo REST client library provides a set of driver API's which can be consumed by an JAVA based web component that would like to interface with a Maximo instance. The client api's use the Maximo NextGen REST/JSON apis which were originally inspired by Linked Data principles. Using this api you would be able to create,update,delete and query Maximo business objects (Using Maximo Integration Framework Object Structures).

The main components of this client library include:
	
* [MaximoConnector (com.ibm.maximo.oslc.MaximoConnector)] - The driver api that establishes the authenticated HTTP session with the Maximo server. It is used by the other apis to create,update,delete and query Maximo data. The authentication and the other basic information can be configured using an [Options (com.ibm.maximo.oslc.Options)] object.  

* [ResourceSet (com.ibm.maximo.oslc.ResourceSet)] - This api represents a collection of Maximo resources of a given type. The type is determined by the Object Structure definition it refers to. In effect this api is equivalent to the concept of Maximo MboSet's

* [Resource (com.ibm.maximo.oslc.Resource)] - Each member of a ResourceSet is represented by an instance of this class. This is equivalent to to the concept of Mbo in Maximo 

* [Attachment (com.ibm.maximo.oslc.Attachment)] and [AttachmentSet (com.ibm.maximo.oslc.AttachmentSet)] - These apis represent the attached docs (doclinks) in Maximo. These are always associated with some Resource. 

Currently the only supported data format is JSON and we have 2 flavors of JSON – the lean and the namespaced. The lean format is supported starting with the version 7.6.0.1 Maximo and is the recommended format to use (as it uses less bandwidth).

# II. Install
-----
## 2.1 As a Maven dependency
### 2.1.1 Central repository

Maximo Rest Client is available in Maven Central repository as an open source artifact. It can be included in a Maven project easily by adding the dependency to the project. The Maven will deal with the maven transitive dependencies automatically.

1. Create a new Maven project
2. Add following dependency to your pom.xml file

Latest Release 
```xml
<dependency>
    <groupId>com.ibm.maximo</groupId>
    <artifactId>maximo-restclient</artifactId>
    <version>1.0</version>
</dependency>
```

Last Release
```xml
<dependency>
    <groupId>com.ibm.maximo</groupId>
    <artifactId>maximo-restclient</artifactId>
    <version>0.1</version>
</dependency>
```

### 2.1.2  Local repository

If the Internet is unavailable or it is difficult to access the central repository for some reason. The client can be installed locally. After the installation, it can be included in a Maven project as well.

1. Run `mvn clean install -Dgpg.skip` at the dictionary of library.
2. Create a new Maven project
3. Add following dependency to your pom.xml file

```xml
<dependency>
	<groupId>com.ibm.maximo</groupId>
	<artifactId>maximo-restclient</artifactId>
	<version>VERSION</version>
</dependency>
```

Where VERSION is the version you gave this artifact in the `pom.xml`

## 2.2 As a Java library

If the Maven environment is unavailable. The Maximo Rest Client can be used as a regular reference library in Java project. As the client depends on javax-json, the javax-json and commons-codec libraries is needed as well.

You can get it from http://repo1.maven.org/maven2/org/glassfish/javax.json/1.0.4/ or use the Maven dependency as,

```xml
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>javax.json</artifactId>
    <version>1.0.4</version>
</dependency>
<dependency>
	<groupId>commons-codec</groupId>
	<artifactId>commons-codec</artifactId>
	<version>1.1</version>
</dependency>

```

When the javax.json-1.0.4.jar, commons-codec-1.1.jar and maximo-restclient-0.1.jar is ready, add them to the java project as common reference libraries.
	
# III. Usage
-----

Maximo Resources (Object Structures) represent a graph of related Maximo objects (Mbo's) that provides an atomic view/object to create/update/delete/query the releated set of Mbos. 

We will use Work Order, Purchase Order and Companies Resource as examples to show you how to use the Maximo Rest Client.

>**note**: The use cases can be find at TestOSLCApi.java

## 3.1 Query a Work Order  for Work Order Set (mxwodetail)

The following instruction shows how to query a work order from Maximo by using Maximo Rest Client Library.

### 3.1.1 Connect to Maximo

Before we connect to Maximo, it is necessary to set up authentication and environment information in Options;

* For authentication, it needs username, password and authentication way among "maxauth", "basic" and "form". The sample code is as following,

```java
Options option = new Options().user("maxadmin").password("maxadmin").auth("maxauth");
```

> **note**: For Maximo Multi-Tenancy, take the tenant code = "00" as an example, using the following Options.

```java
Options option = new Options().user("maxadmin").password("maxadmin").auth("maxauth").mt(true).tenantCode("00");
```

* For environment, it needs the data mode setting, host, port and if it the debug is enabled. The sample code is as following,

```java
option.host("host").port(7001).lean(true);
```

* Based on this configuration, connect to the Maximo using MaximoConnector.

```java
MaximoConnector mc=new MaximoConnector(option).debug(true);
mc.connect();
```

* Or directly use the following code,

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

### 3.1.2 Query the Work Orders

* Create a ResourceSet which is a query for Approved Work Order Set. The selected items is wonum, status.

By object structure name:
  
```java
ResourceSet rs = mc.resourceSet("mxwodetail").select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).fetch();
```

By RESTful URI :
  
```java
ResourceSet rs = mc.resourceSet(new URL("http://host:port/maximo/oslc/os/mxwodetail")).select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).fetch();
```

* There is a paging API for Maximo rest client, that allows forward and backward paging of data by the client.
  * For the page size = 10: 
  
```java
ResourceSet rs = mc.resourceSet("mxwodetail").select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).pageSize(10).fetch();
```

* For the default paging (assumes a default page size is configured on the Resource's Object Structure. If no page size is configured, this directive is ignored and all records matching the query filter is returned): 

```java
ResourceSet rs = mc.resourceSet("mxwodetail").select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).paging(true).fetch();
```

* For the stablepaging:

```java
ResourceSet rs = mc.resourceSet("mxwodetail").select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).stablePaging(true).fetch();
```

* Turn to next or previous page.

```java 
rs.nextPage();
rs.previousPage();
```

For stable paging where currently only scrolling forward is supported, a call to previousPage() would result in an error.

* Get the ResourceSet in JSON:

```java
JsonObject jo = rs.toJSON(); 
```

> **note**:  we support JSON output in byte array. Try the following code,

```java
byte[] jodata = rs.toJSONBytes();
```

* Each Resource has a unique ID. It is easy to get the specific work order by it. In the following example, we try to get the Work Order (_QkVERk9SRC8xMDAw) directly.


By specific URI:
  
```java
String woUri = "http://host:port/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw";
```

Using ResourceSet
  
```java
Resource re = rs.fetchMember(woUri);
```

Or using MaximoConnector
  
```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
Resource re = mc.resource(woUri);
```

By index (this will query the member resource from the resourceset collection and will not make a trip to the server):
  
```java
Resource re = rs.member(0);
```

* In order to query more data from the server for this resource, consider using the load() and reload() apis on the Resource.

```java
re.reload("wonum","status","assetnum","location","wplabor.craft");
```

OR simply
  
```java
re.reload("*");
```

* Get the work order in JSON or Byte Array:

```java
JsonObject jo = re.toJSON();
byte[] joBytes = re.toJSONBytes();
```

### 3.1.3 Traverse the Work Orders 
In some case, there is a need to traverse the some or all work orders. There are some helpful API in  Maximo Rest Client.

* Connect to the Maximo, 

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get a work order set from Maximo server.

```java
ResourceSet rs = mc.resourceSet("mxwodetail").pageSize(10).
```
 
* Travel through the workorders in current page.

```java
for(int i=0;i<rs.count();i++){
	Resource re = rs.member(i);
	...//other operations
}
```

* Travel through all of the workorders. 
* Only available in github. Having issue when pushing code to maven.

```java
for(int i=0;i<rs.totalCount();)
{	
	for(int j=0;j<rs.count();j++)
	{
		Resource re = rs.member(j);
	}
	i+=rs2.count();
	if(!rs2.hasNextPage())
	{
		break;
	}
	rs2.nextPage();
}
```

### 3.1.4 Disconnect from the Maximo

* End the session with Maximo after you finish the work

```java
mc.disconnect();
``` 

## 3.2 Create a new Work Order (mxwodetail)
The following instruction shows how to create a new work order by Maximo Rest Client.
### 3.2.1 Get the Work Order Set

* Connect to the Maximo, 

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get the existing work order list from Maximo server to support the creation.

```java
ResourceSet rs = mc.resourceSet("mxwodetail");
```

### 3.2.2 Create a new Work Order
* Create a valid JSON object with the essential information like siteid for BEDFORD

For non-lean, add the prefix before the attribute:
  
```java
JsonObject jo = Json.createObjectBuilder().add("spi:siteid", "BEDFORD").add("spi:description", "test").build();
Resource re = rs.create(jo);
```

For lean, skip the prefix, using the attribute directly:
  
```java
JsonObject jo = Json.createObjectBuilder().add("siteid", "BEDFORD").add("description", "test").build();
Resource re = rs.create(jo);
```

* To work with child objects (as per the Object Structure definition) you can just make it part of the workorder json. Below shows the creation of a Plaaned Labor record/object that is child to the work order.

```java
JsonObject wplJo = Json.createObjectBuilder().add("skilllevel", "FIRSTCLASS").add("craft", "ELECT").build();
JsonArray wpLaborArray = Json.createArrayBuilder().add(wplJo).build();
jo.add("wplabor",wpLaborArray );
```

> **note**: the sample uses the lean format.

### 3.2.3 Return with the Properties
By default, the create operation will not return any content of the new created work order. Since many attribute values get defaulted or auto-generated at the server side based on Maximo business logic, it often makes sense to get the final representation of the newly created resource.

Instead of re-selecting the work order again (which makes a round-trip to the server), it is easy to get the resource content in response information when we create a new work order by Maximo Rest Client.

For non-lean ,

```java
Resource re = rs.create(jo,"spi:wonum", "spi:status","spi:statusdate","spi:description");
```

or simply
		
```java
Resource re = rs.create(jo,"*");
```

For lean,

```java
Resource re = rs.create(jo,"wonum", "status","statusdate", "description");
```
 
or simply
  
```java
Resource re = rs.create(jo,"*");
```

## 3.3 Update a Purchase Order (mxpo)

To update a resource, we can use either the update() or the merge() apis. The difference between the 2 apis is about how they handle the related child objects in the Resource. An example using the PO  Resource (mxpo) will best illustrate the difference. This example will reference two of the Maximo Business Object contained in the resource, the PO(parent) and POLINE(child).

Say you have an existing purchase order with 1 PO Line child objects. If you wanted to update the PO with a new PO Line entry (ie a 2nd one), you would want to use the merge() api. The merge process goes through the request "poline" array of objects and matches them up with the existing set of poline's (which is the current 1) and it will figure out by comparing the rdf:about (if present in the new poline) to determine if this an exiting poline or a new one. If it determines that it is a new one  it will proceed with creating this new poline and we will end up with 2 polines. If it finds a match, it will proceed with updating the matched one with the requested poline content and we will end up with 1 updated poline. If there are other existing polines, they won't be updated by the process. 

If we use the update() api instead of merge() api, for this scenario, we will end up with only 1 PO Line. If there are any other PO Lines, they will be deleted. This is because update process treats the "poline" element as an atomic object and will update it as a complete replacement.Processing will insert the new PO Line or update the matching PO Line and delete all the other existing PO Lines for that PO.

As apparent from this discussion this update() vs merge() behavior applies only for child objects and not for the root object. The root object is always updated using either API(assuming some attributes of that object have been change)

In another scenario, suppose we have an exsiting PO with 3 polines(1,2,3) and we wanted to

1. delete poline#1
2. update poline#3 
3. create a new poline#N

We would need to 

Use the update() api and send 3 polines (2,3,4). 

The update api will see that the request does not contain PO Line 1 and hence it will delete it, it will skip the update of PO Line 2 (as there is no attributes changed), update PO Line 3 and add a new one PO Line 4. 

After the update the PO would have lines 2,3 and 4

So if we used the merge() instead - the only difference would be that PO Line 1 would have not been deleted. The PO would have lines 1,2,3 and 4

### 3.3.1 Update the poline in Purchase Order


We will create a new PO Line to a purchase order, and then update this purchase order using update() api to update the existing PO Line or replace the existing one by the a new PO Line.

If the polinenum(s) is matched, Maximo will update the existing poline with new array.

If the polinenum(s) is not matched, Maximo will delete the existing poline array and create a new one with the new array. The array size will equal to the new array.

* Connect to the Maximo 

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get a Resource from ResourceSet

```java
ResourceSet reSet = mc.resourceSet("MXPO").fetch();
Resource poRes  = reSet.member(0);
```

* Build a valid JsonObject for adding a new Child Object for Resource

```java
JsonObject polineObjIn = Json.createObjectBuilder().add("polinenum", 1).add("itemnum", "560-00").add("storeloc", "CENTRAL").build();
JsonArray polineArray = Json.createArrayBuilder().add(polineObjIn).build();
JsonObject poObj = Json.createObjectBuilder().add("poline", polineArray).build();
```
* Create a new poline

```java
poRes.update(poObj);
```

* Build a valid JsonObject for updating that Child Object in Resource

```java
JsonObject polineObjIn2 = Json.createObjectBuilder().add("polinenum", 2).add("itemnum", "0-0031").add("storeloc", "CENTRAL").build();
JsonArray polineArray2 = Json.createArrayBuilder().add(polineObjIn2).build();
JsonObject poObj2 = Json.createObjectBuilder().add("poline", polineArray2).build();
```

* Update the Resource

```java
poRes.update(polineObj2);
```

At the end of it, we will have a PO with 1 POLINE. The steps below explains how it happens:

The server side framework will attempt to locate a POLINE with the polinenum 2 and will not find any (as there is only a POLINE with polinenum 1). 

- It will add a new POLINE with polinenum 2.

- It will delete all the remaining POLINE's that are not present in the json object, that will result in PO Line 1 being deleted

### 3.3.2 Merge the poline in Purchase Order

We create a new poline to a purchase order, and then merge this purchase order using another poline object. 
We will create a new PO Line to a purchase order, and then merge this purchase order using merge() api to update the existing line or add an additional line.

If the poline(s) is matched, Maximo will update the existing poline with new array as we use the update() method.

If the poline(s) is not matched, Maximo will add the new poline array to the existing poline array.

* Connect to the Maximo 

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get a Resource from ResourceSet

```java
ResourceSet reSet = mc.resourceSet("MXPO").fetch();
Resource poRes  = reSet.member(1);
```

* Build a valid JsonObject for adding a new Child Object for Resource

```java
JsonObject polineObjIn = Json.createObjectBuilder().add("polinenum", 1).add("itemnum", "560-00").add("storeloc", "CENTRAL").build();
JsonArray polineArray = Json.createArrayBuilder().add(polineObjIn).build();
JsonObject poObj = Json.createObjectBuilder().add("poline", polineArray ).build();
```

* Update the Resource	

```java
poRes.update(poObj);//this creates a POLINE with polinenum 1.
```

* Build a valid JsonObject for merging the Child Object in Resource

```java
JsonObject polineObjIn3 = Json.createObjectBuilder().add("polinenum", 2).add("itemnum", "0-0031").add("storeloc", "CENTRAL").build();
JsonArray polineArray3 = Json.createArrayBuilder().add(polineObjIn3).build();
JsonObject polineObj3 = Json.createObjectBuilder().add("poline", polineObjIn3).build();
```

* Merge the Resource

```java
poRes.merge(polineObj3);//this will create a POLINE with polinenum 2.
```

At the end of it, we will have a PO with 2 POLINEs. The steps below explains how it happens:

The server side framework will attempt to locate a POLINE with the polinenum 2 and will not find any (as there is only a POLINE with polinenum 1). 

- It will add a new POLINE with polinenum 2.

- It will keep the remaining lines (ie in this case POLINE with polinenum 1) as is.

## 3.4 Delete a Service Request (mxsr)

Delete an existing work order by Maximo Rest Client

### 3.4.1 Get an existing Service Request
* Connect to the Maximo 

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get the basic Service Request set from Maximo server.

```java
ResourceSet rs = mc.resourceSet("mxsr").
```

* Get an existing Service Request

By specific URI:
  
```java
String venUri = "http://localhost:7001/maximo/oslc/os/mxsr/_U1IvMTE3Mw--";
```

Using ResourceSet
  
```java
Resource re = rs.fetchMember(srUri);
```

Or using MaximoConnector
  
```java
Resource re = mc.resource(srUri);
```

By index:
  
```java
Resource re = rs.member(0);
```

### 3.4.2 Delete the Service Request

* Call the deleteResource method of MaximoConnector

```java
mc.deleteResource(srUri);
```

* Call the delete method of Resource.

```java
re.delete();
```

## 3.5 Attachments
Attachments in Maximo are ducuments, fileds or images that are attached to a resource such as a Work Order or Service Request. The following example shows how to add and delete an attachment to work order. Within the resource definition, the DOCLINKS object (child to Work Order object) supports the attachment data.
### 3.5.1 Create an attachment for an existing work order.
* Connect to the Maximo 

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get the basic work order set from Maximo server.

```java
ResourceSet rs = mc.resourceSet("mxwodetail").
```

* Get an existing work order form work order set.

By specific URI:
  
```java
String woUri = "http://host:port/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw";
```

Using ResourceSet
  
```java 
Resource re = rs.fetchMember(woUri);
```

using MaximoConnector
  
```java
Resource re = mc.resource(woUri);
```

By index (be careful with the range):
  
```java 	
Resource re = rs.member(0);
```

* Get the attachment set of this work order

```java
AttachmentSet ats = re.attachmentSet();
```

* Initial the document data

```java
String str = "hello world @ "+ Calendar.getInstance().getTime().toString();
byte[] data = str.getBytes("utf-8");
```

* Initial the attachment files

```java
Attachment att = new Attachment().name("attachment.txt").description("test").data(data).meta("FILE", "Attachments");
```

* Attach the file to the work order

By default,
  
```java
att = ats.create(att);
```

>**note**: For the custom property name for doclinks such as "customdoclink",
  
```java
att = ats.create("customdoclink",att);
```

### 3.5.2 Get the data from the attachment
* Get attachment from AttachmentSet
* Get an existing attachment from maximo:

```java
Attachment att = ats.member(0);
```

* Get attachement doc :

```java
byte[] data = att.toDoc();
```

* Get attachment meta:

```java
JsonObject attMeta = att.toDocMeta();
JsonObject attMeta = att.fetchDocMeta();
```

* Get Attachment using MaximoConnector directly by calling the attachment URI. As the Resource has unique ID, the attachment has ID as well. In the following example, we will get attachment (28) which is attached in work order (_QkVERk9SRC8xMDAw):

```java
String attUri = "http://host/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw/DOCLINKS/28";
Attachment att = mc.attachment(attUri);
byte[] data = mc.attachedDoc(attUri);
JsonObject attMeta = mc.attachmentDocMeta(attUri);
```

### 3.5.3 Delete an attachment
* Get an existing work order form work order set.

By specific URI:
  
```java
String woUri = "http://host:port/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw";
```

Using ResourceSet
  
```java
Resource re = rs.fetchMember(woUri);
```

Or using MaximoConnector

```java
Resource re = mc.resource(woUri);
```

By index (be careful with the range):
  
```java
Resource re = rs.member(0);
```

* Get the AttachmentSet from work order 

```java
AttachmentSet ats = re.attachmentSet();
```

* Get the Attachment from AttachmentSet

By index
  
```java
Attachment att = ats.member(0);
```

By specific URI:
  
```java
String attUri = "http://host/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw/DOCLINKS/28";
```

Using AttachmentSet
  
```java  
Attachment att = ats.fetchMember(attUri);
```

Or using MaximoConnector
  
```java 	
Attachment att  = mc.attachment(attUri);
```
* Delete the Attachment

Using Attachment (useful when you already have the attachment)
  
```java
att.delete();
```

Using MaximoConnector (useful when you just have the uri)
  
```java
mc.deleteAttachment(attUri);
```

## 3.6 Saved Query
Maximo supports a feature called a Saved Query where a pre-built query for an application, such as Work Order Tracking, which allows a user to easily retrieve a common set of data (example: a list of Approved Work Order). Assuming public saved queries are present for  an application (like WOTRACK), you can use the savedQuery() api to query records based on defined filter criterion. We need also make sure that the authorized application is set accordingly for the Object Structure we are planning to use.

Taking the "OWNER IS ME" query for WOTRACK (workorder) application as an example. Assuming MXWODETAIL Object Structure is setup with the "WOTRACK" as authorized application.

* Query the data

```java
ResourceSet rs = mc.resourceSet("mxwodetail").savedQuery(new SavedQuery().name("WOTRACK:OWNER IS ME")).select("*").fetch();
```

The select("*") queryes all attributes for the filtered set of mxwodetail. As mentioned earlier, we can do a partial resource selection like select("wonum","status").

We can also do further filtering along with the saved query.

* Query the data

```java
ResourceSet rs = mc.resourceSet("mxwodetail").savedQuery(new SavedQuery().name("WOTRACK:OWNER IS ME")).where(new QueryWhere().where("status").in("APPR","WAPPR")).select("wonum","status","statusdate").fetch();
```

## 3.7 Terms Search

This is used mostly for text search. This needs the server side Object Structure setup with the searchable attributes. Assuming its setup with say "description" we can use the hasTerms() api to set the searchable terms.

It will select all the resources from oslcmxsr whose description contains email or finance.

* Connect to the Maximo

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").auth("maxauth").host("host").port(7001));
mc.connect();
```

* Fetch the ResourceSet

```java
ResourceSet res = mc.resourceSet("OSLCMXSR").hasTerms("email", "finance").select("spi:description", "spi:ticketid").pageSize(5).fetch();
```

## 3.8 Action

Actions are functional components of a resource that perform specific tasks such as changing the status of a resource or moving a resource. This example uses the changeStatus action as an example (the changeStatus method is annotated  marked as a WebMethod in the WorkOrder Service), 

* Connect to the Maximo

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get the ResourceSet where status  = WAPPR

```java
ResourceSet reSet = mc.resourceSet("MXWODETAIL").where((new QueryWhere()).where("status").equalTo("WAPPR")).fetch();
```

* Get the first member of ResourceSet,

```java
Resource re = reSet.member(0);
```

* Build JsonObject for changing 

```java
JsonObject jo = Json.createObjectBuilder().add("status","APPR").add("memo","approval").build();
```

* Invoke the Action

```java
re.invokeAction("wsmethod:changeStatus", jo);
```

# References

[Java API](https://maximo-java-rest-client.github.io/maximo-java-rest-client/index.html)