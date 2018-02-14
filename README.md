# Updated

1. Added support to group by for ResourceSet
2. Added support to sync for ResourceSet
3. Added support to bulk for ResourceSet
4. Added new examples about new API in TestOSLCAPI.java.
5. Fixed bugs
6. Removed references to `javax.xml.bind.DatatypeConverter.printBase64Binary` because that API is not available on Android. It now uses commons-codec to get base64 support.

# Maximo Rest Client 1.0 Released!

1. Added support to arbitrary parameters for Resource/ResourceSet
2. Added support to arbitrary headers for get/post/patch/merge/delete
3. Added support to order by for ResourceSet
4. Added support to invoke action with properties for Resource
5. Added new examples about new API in TestOSLCAPI.java.
6. Fixed bugs

# I. Introduction
-----

The Maximo REST client library provides a set of driver APIs that can be consumed by a Java-based web component that wants to interface with a Maximo instance. The client APIs use the Maximo NextGen REST/JSON APIS, which were originally inspired by Linked Data principles. By using this API, you are able to create, update, delete, and query Maximo business objects by using Maximo integration framework object structures.

The following main components are included in this client library:
	
* [MaximoConnector (com.ibm.maximo.oslc.MaximoConnector)] - The driver API that establishes the authenticated HTTP session with the Maximo server. It is used by the other APIs to create, update, delete, and query Maximo data. The authentication and the other basic information can be configured using an [Options (com.ibm.maximo.oslc.Options)] object.  

* [ResourceSet (com.ibm.maximo.oslc.ResourceSet)] - This API represents a collection of Maximo resources of a given type. The type is determined by the object structure definition that it refers to. In effect, this api is equivalent to the concept of the Maximo MboSet.

* [Resource (com.ibm.maximo.oslc.Resource)] - Each member of a ResourceSet is represented by an instance of this class. This class is equivalent to the concept of a Maximo business object (MBO). 

* [Attachment (com.ibm.maximo.oslc.Attachment)] and [AttachmentSet (com.ibm.maximo.oslc.AttachmentSet)] - These APIs represent the attached documents, or doclinks, in Maximo Asset Management. These APIs are always associated with a Resource object. 

Currently the only supported data format is JSON, and we have two flavors of JSON: the lean and the namespaced. The lean format is supported starting in Maximo Asset Management version 7.6.0.1 and is the recommended format to use, because it uses less bandwidth.

# II. Install
-----
## 2.1 As a Maven dependency
### 2.1.1 Central repository

Maximo REST Client is available in the Maven Central repository as an open source artifact. It can be included in a Maven project easily by adding the dependency to the project. The Maven will automatically handle the maven transitive dependencies.

1. Create a new Maven project.
2. Add following dependency to your pom.xml file.

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

You can use a local repository if the Internet is unavailable or it is difficult to access the central repository for some reason. The client can be installed locally. After the installation, it can be included in a Maven project as well.

1. Run `mvn clean install -Dgpg.skip` at the dictionary of library.
2. Create a new Maven project
3. Add following dependency to your pom.xml file.

```xml
<dependency>
	<groupId>com.ibm.maximo</groupId>
	<artifactId>maximo-restclient</artifactId>
	<version>VERSION</version>
</dependency>
```

Where VERSION is the version you gave this artifact in the `pom.xml`.

## 2.2 As a Java library

If the Maven environment is unavailable, the Maximo REST Client can be used as a regular reference library in the Java project. Because the client depends on javax-json, the javax-json and commons-codec libraries are also needed.

You can get it from http://repo1.maven.org/maven2/org/glassfish/javax.json/1.0.4/ or use the Maven dependency as shown in the following code:

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

When the javax.json-1.0.4.jar, commons-codec-1.1.jar, and maximo-restclient-0.1.jar files are ready, add them to the Java project as common reference libraries.
	
# III. Usage
-----

Maximo Resources, or object structures, represent a graph of related Maximo business objects (Mbos) that provides an atomic view/object to create, update, delete, and query the releated set of Mbos. 

We will use the Work Order, Purchase Order and Companies Resources as examples to show you how to use the Maximo REST Client.

>**Note**: The use cases can be found at TestOSLCApi.java.

## 3.1 Query a work order for work order set (mxwodetail)

The following instruction shows how to query a work order from Maximo Asset Management by using the Maximo RET Client Library.

### 3.1.1 Connect to Maximo Asset Management

Before you connect, it is necessary to set up the authentication and environment information in Options.

* For authentication, the username, password and authentication method are required. The value for authentication method can be "maxauth", "basic" or "form". The sample code is shown in the following code:

```java
Options option = new Options().user("maxadmin").password("maxadmin").auth("maxauth");
```

> **Note**: For Maximo Asset Management Multitenancy, take the tenant code = "00", as an example, in the following Options.

```java
Options option = new Options().user("maxadmin").password("maxadmin").auth("maxauth").mt(true).tenantCode("00");
```

* For environment, it needs the data mode setting, host, port, and if  the debug is enabled. The sample code is shown in the following code:

```java
option.host("host").port(7001).lean(true);
```

* Based on this configuration, connect to the Maximo Asset Management by using MaximoConnector:

```java
MaximoConnector mc=new MaximoConnector(option).debug(true);
mc.connect();
```

* Or directly by using the following code:

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

### 3.1.2 Query the work orders

* Create a ResourceSet, which is a query for the Approved Work Order Set. The selected items are wonum, status.

By object structure name:
  
```java
ResourceSet rs = mc.resourceSet("mxwodetail").select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).fetch();
```

By RESTful URI :
  
```java
ResourceSet rs = mc.resourceSet(new URL("http://host:port/maximo/oslc/os/mxwodetail")).select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).fetch();
```

* There is a paging API for thw Maximo REST Client that allows forward and backward paging of data by the client.
  * For the page size = 10: 
  
```java
ResourceSet rs = mc.resourceSet("mxwodetail").select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).pageSize(10).fetch();
```

* For the default paging, which assumes that a default page size is configured on the the Resource's object structure. If no page size is configured, this directive is ignored, and all records matching the query filter is returned: 

```java
ResourceSet rs = mc.resourceSet("mxwodetail").select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).paging(true).fetch();
```

* For the stablepaging:

```java
ResourceSet rs = mc.resourceSet("mxwodetail").select("wonum","status").where((new QueryWhere()).where("status").equalTo("APPR")).stablePaging(true).fetch();
```

* Turn to next or previous page:

```java 
rs.nextPage();
rs.previousPage();
```

For stable paging where currently only scrolling forward is supported, a call to previousPage() results in an error.

* Get the ResourceSet in JSON:

```java
JsonObject jo = rs.toJSON(); 
```

> **Note**:  we support JSON output in byte array. Try the following code:

```java
byte[] jodata = rs.toJSONBytes();
```

* Each Resource has a unique ID. It is easy to get the specific work order by it. In the following example, you can see how to get the Work Order (_QkVERk9SRC8xMDAw) directly.


By specific URI:
  
```java
String woUri = "http://host:port/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw";
```

By using the ResourceSet
  
```java
Resource re = rs.fetchMember(woUri);
```

Or by using MaximoConnector:
  
```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
Resource re = mc.resource(woUri);
```

By index, which will query the member resource from the resourceset collection and will not make a trip to the server:
  
```java
Resource re = rs.member(0);
```

* To query more data from the server for this resource, consider using the load() and reload() APIs on the Resource.

```java
re.reload("wonum","status","assetnum","location","wplabor.craft");
```

OR simply
  
```java
re.reload("*");
```

* Get the work order in JSON or byte array:

```java
JsonObject jo = re.toJSON();
byte[] joBytes = re.toJSONBytes();
```

### 3.1.3 Traverse the Work Orders 
In some case, you might need to traverse some or all work orders. There are some helpful API in the Maximo REST Client.

* Connect to the Maximo Asset Management: 

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get a work order set from the Maximo server:

```java
ResourceSet rs = mc.resourceSet("mxwodetail").pageSize(10).
```
 
* Travel through the work orders that are on the current page:

```java
for(int i=0;i<rs.count();i++){
	Resource re = rs.member(i);
	...//other operations
}
```

* Travel through all of the workorders. 
* Only available in github. Issue is encountered when pushing code to Maven.

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

### 3.1.4 Disconnect from Maximo Asset Management

* End the session with Maximo Asset Management after you finish the work:

```java
mc.disconnect();
``` 

## 3.2 Create a new work order (mxwodetail)
The following instruction shows how to create a new work order by Maximo Rest Client.
### 3.2.1 Get the work order set

* Connect to Maximo Asset Management  

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get the existing work order list from the Maximo server to support the creation.

```java
ResourceSet rs = mc.resourceSet("mxwodetail");
```

### 3.2.2 Create a new work order
* Create a valid JSON object that has the essential information, such as siteid for BEDFORD.

For non-lean, add the prefix before the attribute:
  
```java
JsonObject jo = Json.createObjectBuilder().add("spi:siteid", "BEDFORD").add("spi:description", "test").build();
Resource re = rs.create(jo);
```

For lean, skip the prefix, and use the attribute directly:
  
```java
JsonObject jo = Json.createObjectBuilder().add("siteid", "BEDFORD").add("description", "test").build();
Resource re = rs.create(jo);
```

* To work with child objects, as per the Object Structure definition, you can just make it part of the work order json. The following code shows the creation of a planned labor record or object that is a child of  the work order:

```java
JsonObject wplJo = Json.createObjectBuilder().add("skilllevel", "FIRSTCLASS").add("craft", "ELECT").build();
JsonArray wpLaborArray = Json.createArrayBuilder().add(wplJo).build();
jo.add("wplabor",wpLaborArray );
```

> **Note**: The sample uses the lean format.

### 3.2.3 Return with the properties
By default, the create operation will not return any content of the newly created work order. Because many attributes use default values or calues that auto-generated at the server based on Maximo business logic, it often makes sense to get the final representation of the newly created resource.

Instead of reselecting the work order, which makes a round-trip to the server, it is easy to get the resource content in response information when a new work order is created by the Maximo REST Client.

For non-lean:

```java
Resource re = rs.create(jo,"spi:wonum", "spi:status","spi:statusdate","spi:description");
```

or simply:
		
```java
Resource re = rs.create(jo,"*");
```

For lean:

```java
Resource re = rs.create(jo,"wonum", "status","statusdate", "description");
```
 
or simply:
  
```java
Resource re = rs.create(jo,"*");
```

## 3.3 Update a purchase order (mxpo)

To update a resource, you can use either the update() or the merge() APIs. The difference between these APIs how they handle the related child objects in the Resource. An example that uses the PO Resource (mxpo) best illustrates this difference. This example will reference two of the Maximo business object that is contained in the Resource: the PO(parent) and POLINE(child).

In this scenario, you have an existing purchase order that has one PO Line child objects. If you wanted to update the PO with a new PO Line entry, which is the second line, you use the merge() api. The merge process goes through the request "poline" array of objects and matches them up with the existing set of polines, which is the current one, and it will compare the rdf:about, if  it is present in the new poline,  to determine if this line is an existing poline or a new one. If it determines that it is a new one, it will proceed with creating this new poline, and you have two polines. If it finds a match, it will proceed with updating the matched one with the requested poline content, and you have one updated poline. If there are other existing polines, they won't be updated by the process. 

If you use the update() API instead of the merge() API for this scenario, have only one PO Line. If there are any other PO Lines, they will be deleted. This deletion is because the update process treats the "poline" element as an atomic object and will update it as a complete replacement. Processing will insert the new PO Line or update the matching PO Line and delete all the other existing PO Lines for that PO.

The update() and merge() behavior applies only for child objects and not for the root object. The root object is always updated by using either API, assuming some attributes of that object have been changed. 

In another scenario, you have an exsiting PO that has three polines(1,2,3) and you want to complete the following actions:

1. delete poline#1
2. update poline#3 
3. create a new poline#N

You need to do the following tasks:

Use the update() API and send three polines (2,3,4). 

The update API will see that the request does not contain PO Line 1 and so it will delete it. It will skip the update of PO Line 2 because  no attributes were change, update PO Line 3, and add a new line, PO Line 4. 

After the update, the PO has lines 2,3 and 4.

So if you used the merge() API instead, the only difference is that PO Line 1 is not deleted. The PO has lines 1,2,3 and 4.

### 3.3.1 Update the poline in the purchase order


You can create a new PO Line on a purchase order and then update this purchase order by using the update() API to update the existing PO Line or replace the existing one by the a new PO Line.

If the polinenum(s) is matched, Maximo Asset Management will update the existing poline with a new array.

If the polinenum(s) is not matched, Maximo Asset Management will delete the existing poline array and create a new one with the new array. The array size will be equal to the new array.

* Connect to Maximo Asset Management:

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get a Resource from ResourceSet:

```java
ResourceSet reSet = mc.resourceSet("MXPO").fetch();
Resource poRes  = reSet.member(0);
```

* Build a valid JsonObject for adding a new Child Object for Resource:

```java
JsonObject polineObjIn = Json.createObjectBuilder().add("polinenum", 1).add("itemnum", "560-00").add("storeloc", "CENTRAL").build();
JsonArray polineArray = Json.createArrayBuilder().add(polineObjIn).build();
JsonObject poObj = Json.createObjectBuilder().add("poline", polineArray).build();
```
* Create a new poline:

```java
poRes.update(poObj);
```

* Build a valid JsonObject for updating that Child Object in the Resource:

```java
JsonObject polineObjIn2 = Json.createObjectBuilder().add("polinenum", 2).add("itemnum", "0-0031").add("storeloc", "CENTRAL").build();
JsonArray polineArray2 = Json.createArrayBuilder().add(polineObjIn2).build();
JsonObject poObj2 = Json.createObjectBuilder().add("poline", polineArray2).build();
```

* Update the Resource:

```java
poRes.update(polineObj2);
```

At the end of it, you have a PO with 1 POLINE. The steps below explains how it happens:

The server-side framework will attempt to locate a POLINE that has the polinenum 2 and will not find any, because there is only a POLINE with polinenum 1). 

- It will add a new POLINE that has polinenum 2.

- It will delete all the remaining POLINEs that are not present in the JSON object, which will result in PO Line 1 being deleted.

### 3.3.2 Merge the poline in the purchase order

You can create a new poline on a purchase order and then merge this purchase order by using another poline object. 
You can create a new PO Line on a purchase order and then merge this purchase order  by using the merge() API to update the existing line or add an additional line.

If the poline(s) is matched, Maximo Asset Management will update the existing poline with new array, which is similar to using the update() method.

If the poline(s) is not matched, Maximo Asset Management will add the new poline array to the existing poline array.

* Connect to Maximo Asset Management:

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get a Resource from ResourceSet:

```java
ResourceSet reSet = mc.resourceSet("MXPO").fetch();
Resource poRes  = reSet.member(1);
```

* Build a valid JsonObject for adding a new Child Object for the Resource:

```java
JsonObject polineObjIn = Json.createObjectBuilder().add("polinenum", 1).add("itemnum", "560-00").add("storeloc", "CENTRAL").build();
JsonArray polineArray = Json.createArrayBuilder().add(polineObjIn).build();
JsonObject poObj = Json.createObjectBuilder().add("poline", polineArray ).build();
```

* Update the Resource:

```java
poRes.update(poObj);//this creates a POLINE with polinenum 1.
```

* Build a valid JsonObject for merging the Child Object in the Resource:

```java
JsonObject polineObjIn3 = Json.createObjectBuilder().add("polinenum", 2).add("itemnum", "0-0031").add("storeloc", "CENTRAL").build();
JsonArray polineArray3 = Json.createArrayBuilder().add(polineObjIn3).build();
JsonObject polineObj3 = Json.createObjectBuilder().add("poline", polineObjIn3).build();
```

* Merge the Resource:

```java
poRes.merge(polineObj3);//this will create a POLINE with polinenum 2.
```

At the end of it, you have a PO that has 2 POLINEs. The steps below explains how it happens:

The server-side framework will attempt to locate a POLINE that has the polinenum 2 and will not find any, because there is only a POLINE that has polinenum 1. 

- It will add a new POLINE with polinenum 2.

- It will keep the remaining lines, in this case POLINE with polinenum 1, as is.

## 3.4 Delete a service request (mxsr)

You can delete an existing work order by using the Maximo REST Client.

### 3.4.1 Get an existing service request
* Connect to Maximo Asset Management:

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get the basic service request set from the Maximo server:

```java
ResourceSet rs = mc.resourceSet("mxsr").
```

* Get an existing service request:

By using a specific URI:
  
```java
String venUri = "http://localhost:7001/maximo/oslc/os/mxsr/_U1IvMTE3Mw--";
```

By using ResourceSet:
  
```java
Resource re = rs.fetchMember(srUri);
```

Or byusing MaximoConnector:
  
```java
Resource re = mc.resource(srUri);
```

By index:
  
```java
Resource re = rs.member(0);
```

### 3.4.2 Delete the service request

* Call the deleteResource method of MaximoConnector:

```java
mc.deleteResource(srUri);
```

* Call the delete method of Resource:

```java
re.delete();
```

## 3.5 Attachments
Attachments in Maximo Asset Management are documents, files, or images that are attached to a resource, such as a work order or service request. The following example shows how to add and delete an attachment on a work order. In the resource definition, the DOCLINKS object, which is a child of the work order object, supports the attachment data.
### 3.5.1 Create an attachment for an existing work order.
* Connect to Maximo Asset Management:

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get the basic work order set from the Maximo server:

```java
ResourceSet rs = mc.resourceSet("mxwodetail").
```

* Get an existing work order from the work order set:

By specific URI:
  
```java
String woUri = "http://host:port/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw";
```

By using ResourceSet:
  
```java 
Resource re = rs.fetchMember(woUri);
```

By using MaximoConnector:
  
```java
Resource re = mc.resource(woUri);
```

By index (be careful with the range):
  
```java 	
Resource re = rs.member(0);
```

* Get the attachment set of this work order:

```java
AttachmentSet ats = re.attachmentSet();
```

* Initial the document data:

```java
String str = "hello world @ "+ Calendar.getInstance().getTime().toString();
byte[] data = str.getBytes("utf-8");
```

* Initial the attachment files:

```java
Attachment att = new Attachment().name("attachment.txt").description("test").data(data).meta("FILE", "Attachments");
```

* Attach the file to the work order:

By default,
  
```java
att = ats.create(att);
```

>**Note**: For the custom property name for doclinks such as "customdoclink",
  
```java
att = ats.create("customdoclink",att);
```

### 3.5.2 Get the data from the attachment
* Get attachment from AttachmentSet
* Get an existing attachment from Maximo Asset Management:

```java
Attachment att = ats.member(0);
```

* Get the attachment doc:

```java
byte[] data = att.toDoc();
```

* Get the attachment meta:

```java
JsonObject attMeta = att.toDocMeta();
JsonObject attMeta = att.fetchDocMeta();
```

* Get the attachment by using MaximoConnector directly by calling the attachment URI. Because the Resource has a unique ID, the attachment has an ID as well. In the following example, you can see how to get an attachment that has the ID 28 and that is attached to a work order (_QkVERk9SRC8xMDAw):

```java
String attUri = "http://host/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw/DOCLINKS/28";
Attachment att = mc.attachment(attUri);
byte[] data = mc.attachedDoc(attUri);
JsonObject attMeta = mc.attachmentDocMeta(attUri);
```

### 3.5.3 Delete an attachment
* Get an existing work order from a work order set:

By specific URI:
  
```java
String woUri = "http://host:port/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw";
```

By using ResourceSet:
  
```java
Resource re = rs.fetchMember(woUri);
```

Or by using MaximoConnector:

```java
Resource re = mc.resource(woUri);
```

By index (be careful with the range):
  
```java
Resource re = rs.member(0);
```

* Get the AttachmentSet from the work order:

```java
AttachmentSet ats = re.attachmentSet();
```

* Get the attachment from the AttachmentSet:

By index:
  
```java
Attachment att = ats.member(0);
```

By using a specific URI:
  
```java
String attUri = "http://host/maximo/oslc/os/mxwodetail/_QkVERk9SRC8xMDAw/DOCLINKS/28";
```

By using AttachmentSet:
  
```java  
Attachment att = ats.fetchMember(attUri);
```

Or by using MaximoConnector:
  
```java 	
Attachment att  = mc.attachment(attUri);
```
* Delete the attachment:

By using the attachment, which is useful when you already have the attachment:
  
```java
att.delete();
```

By using MaximoConnector, which is useful when you have only the URI:
  
```java
mc.deleteAttachment(attUri);
```

## 3.6 Saved query
Maximo Asset Management supports a feature that is called a Saved Query that is a pre-built query for an application, such as the Work Order Tracking application, which allows a user to easily retrieve a common set of data, for example, a list of approved work orders. Assuming public saved queries are present for an application, you can use the savedQuery() API to query records based on defined filter criterion. You also must ensure that the authorized application is set accordingly for the object structure you plan to use.

Take the "OWNER IS ME" query for the Work Order Tracking (WOTRACK) application as an example, which assumes that the MXWODETAIL object structure is setup with "WOTRACK" as the authorized application.

* Query the data:

```java
ResourceSet rs = mc.resourceSet("mxwodetail").savedQuery(new SavedQuery().name("WOTRACK:OWNER IS ME")).select("*").fetch();
```

The select("*") statement queries all attributes for the filtered set of mxwodetail. As mentioned earlier, you can do a partial resource selection, such as select("wonum","status").

You can also do further filtering with the saved query.

* Query the data

```java
ResourceSet rs = mc.resourceSet("mxwodetail").savedQuery(new SavedQuery().name("WOTRACK:OWNER IS ME")).where(new QueryWhere().where("status").in("APPR","WAPPR")).select("wonum","status","statusdate").fetch();
```

## 3.7 Terms search

This feature is used mostly for text search. This feature needs the server-side object structure to be set up with the searchable attributes. For example, if it is setup with "description", you can use the hasTerms() API to set the searchable terms.

For example, you can select all the resources from oslcmxsr whose description contains email or finance.

* Connect to Maximo Asset Management:

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").auth("maxauth").host("host").port(7001));
mc.connect();
```

* Fetch the ResourceSet:

```java
ResourceSet res = mc.resourceSet("OSLCMXSR").hasTerms("email", "finance").select("spi:description", "spi:ticketid").pageSize(5).fetch();
```

## 3.8 Action

Actions are functional components of a resource that perform specific tasks, such as changing the status of a resource or moving a resource. This example uses the changeStatus action as an example (the changeStatus method is annotated  marked as a WebMethod in the WorkOrder Service), 

* Connect to Maximo Asset Management:

```java
MaximoConnector mc = new MaximoConnector(new Options().user("maxadmin").password("maxadmin").lean(true).auth("maxauth").host("host").port(7001));
mc.connect();
```

* Get the ResourceSet where status is equal to WAPPR:

```java
ResourceSet reSet = mc.resourceSet("MXWODETAIL").where((new QueryWhere()).where("status").equalTo("WAPPR")).fetch();
```

* Get the first member ofthe ResourceSet:

```java
Resource re = reSet.member(0);
```

* Build the JsonObject for changing:

```java
JsonObject jo = Json.createObjectBuilder().add("status","APPR").add("memo","approval").build();
```

* Invoke the action:

```java
re.invokeAction("wsmethod:changeStatus", jo);
```

# References

[Java API](https://ibm-maximo-dev.github.io/maximo-java-rest-client/index.html)
