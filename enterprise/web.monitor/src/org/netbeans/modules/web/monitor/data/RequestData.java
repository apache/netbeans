/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.monitor.data;

import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Vector;

import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BeanComparator;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.GraphManager;
import org.netbeans.modules.schema2beans.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RequestData extends BaseBean {

    static Vector<BeanComparator> comparators = new Vector<>();

    public static final String PARAM = "Param"; //NOI18N
    public static final String HEADERS = "Headers"; //NOI18N
    public static final String REQUESTATTRIBUTESIN =
	"RequestAttributesIn"; //NOI18N
    public static final String REQUESTATTRIBUTESOUT =
	"RequestAttributesOut"; //NOI18N
    public static final String REQUESTDATA = "RequestData"; //NOI18N

    public static final String JSESSIONID = "JSESSIONID"; // NOI18N
    public static final String COOKIE = "cookie"; // NOI18N
    private static final boolean debug = false;
    

    public RequestData() {
	this(Common.USE_DEFAULT_VALUES);
    }


    public RequestData(Node doc, int options) {
	super(RequestData.comparators, new Version(1, 0, 6));
	if (doc == null) {
	    doc = GraphManager.createRootElementNode(REQUESTDATA); 
		
	    if (doc == null)
		throw new RuntimeException("failed to create a new DOM root!");  //NOI18N
	}
	Node n = GraphManager.getElementNode(REQUESTDATA, doc); 
	if (n == null)
	    throw new RuntimeException("doc root not found in the DOM graph!"); //NOI18N

	this.graphManager.setXmlDocument(doc);

	// Entry point of the createBeans() recursive calls
	this.createBean(n, this.graphManager());
	this.initialize(options);
    }

    public RequestData(int options)	{
	super(RequestData.comparators, new Version(1, 0, 6));
	// Properties (see root bean comments for the bean graph)

	this.createProperty("Headers", HEADERS, //NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    Headers.class);


	this.createProperty("RequestAttributesIn", REQUESTATTRIBUTESIN, //NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    RequestAttributesIn.class);


	this.createProperty("RequestAttributesOut", REQUESTATTRIBUTESOUT, //NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    RequestAttributesOut.class);


	this.createProperty("Param", PARAM, //NOI18N
			    Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    Param.class);
	this.createAttribute(PARAM, "name", "Name", //NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);

	this.createAttribute(PARAM, "value", "Value", //NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {

    }

    // This attribute is mandatory
    public void setHeaders(Headers value) {
	this.setValue(HEADERS, value);
    }

    //
    public Headers getHeaders() {
	return (Headers)this.getValue(HEADERS);
    }


    // This attribute is mandatory
    public void setRequestAttributesIn(RequestAttributesIn value) {
	this.setValue(REQUESTATTRIBUTESIN, value);
    }

    //
    public RequestAttributesIn getRequestAttributesIn() {
	return (RequestAttributesIn)this.getValue(REQUESTATTRIBUTESIN);
    }


    // This attribute is mandatory
    public void setRequestAttributesOut(RequestAttributesOut value) {
	this.setValue(REQUESTATTRIBUTESOUT, value);
    }

    //
    public RequestAttributesOut getRequestAttributesOut() {
	return (RequestAttributesOut)this.getValue(REQUESTATTRIBUTESOUT);
    }


    // This attribute is an array, possibly empty
    public void setParam(int index, Param value)
    {
	this.setValue(PARAM, index, value);
    }

    //
    public Param getParam(int index)
    {
	return (Param)this.getValue(PARAM, index);
    }

    // This attribute is an array, possibly empty
    public void setParam(Param[] value)
    {
	if(debug) log("setParam(Param[] value)"); //NOI18N
	try {
	    this.setValue(PARAM, value);
	}
	catch(Exception ex) {
	    ex.printStackTrace();
	}
    }

    //
    public Param[] getParam()
    {
	return (Param[])this.getValues(PARAM);
    }

    // Return the number of properties
    public int sizeParam()
    {
	return this.size(PARAM);
    }

    // Add a new element returning its index in the list
    public int addParam(Param value) {
	return this.addValue(PARAM, value);
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeParam(Param value)
    {
	return this.removeValue(PARAM, value);
    }


    /* Methods for manipulating the session cookie */ 

    public void setReplaceSessionCookie(boolean value) { 
	this.setAttributeValue("replace", String.valueOf(value)); // NOI18N
    }

    public boolean getReplaceSessionCookie() {
	try {
	    if(this.getAttributeValue("replace").equals("true")) // NOI18N
		return true;
	}
	catch(NullPointerException npe) {
	    // do nothing
	}
	return false;
    }


    public String getSessionID() {
	return findSessionID(getCookieString());
    }
    

    public String getCookieString() {
	Param[] headers = getHeaders().getParam();
	StringBuffer cookieStr = new StringBuffer(); 
	int len = headers.length;
	for(int j=0; j<len; ++j) {
	    if(headers[j].getName().equalsIgnoreCase(COOKIE)) { 
		cookieStr.append(headers[j].getValue());
		cookieStr.append(";"); 
	    }
	}
	return cookieStr.toString();
    }
    
	    

    public static String findSessionID(String cookieStr) {

	if(cookieStr == null || cookieStr.equals("")) //NOI18N
	    return ""; //NOI18N
	
	StringTokenizer tok = new StringTokenizer(cookieStr,
						  ";", false); // NOI18N
	    
	while (tok.hasMoreTokens()) {
		
	    String token = tok.nextToken();
	    int i = token.indexOf("="); // NOI18N
	    if (i > -1) {
			
		String name = token.substring(0, i).trim();
		if(name.equals(JSESSIONID)) {
		    String value = token.substring(i+1).trim();
		    return value=stripQuote(value);
		}
	    }
	}
	return ""; //NOI18N
    }
    
    /** 
     * Gets the cookies as an array of Param from the cookie string in 
     * the header. If there is no cookie header we return an empty
     * array. 
     */
    public Param[] getCookiesAsParams() {

	String cookieStr = getCookieString();
	if(debug) log("cookie string is " + cookieStr); 

	if(cookieStr == null || cookieStr.equals(""))  //NOI18N
	    return new Param[0];
		
	Vector<Param> cookies = new Vector<>();
	    
	StringTokenizer tok = new StringTokenizer(cookieStr,
						  ";", false); // NOI18N
	    
	while (tok.hasMoreTokens()) {
		
	    String token = tok.nextToken();
	    if(debug) log("token is " + token); 
	    int i = token.indexOf("="); // NOI18N
	    if (i > -1) {

		String name = token.substring(0, i).trim();
		String value = token.substring(i+1).trim();
		value=stripQuote(value);
		cookies.addElement(new Param(name, value));
		if(debug) log(name + "=" + value); 
	    }
	}
	int numCookies = cookies.size();
	Param[] params = new Param[numCookies]; 
	for(int k=0; k<numCookies; ++k) 
	    params[k] = cookies.elementAt(k);
	
	return params;
    }

    /**
     * This method is used by EditPanelCookies to add cookies to the
     * request data prior to a replay. We add the cookie to the 
     * CookiesIn array, as well as adding the corresponding string to
     * the header. It is the latter that is used for the replay - the
     * former is only for display purposes while the user is modifying 
     * the request. 
     */
     
    public int addCookie(String ckname, String ckvalue) {

        // Do we have to check for duplicates? 
	if(debug) 
	    log("Adding cookie: " + ckname + " " + ckvalue); //NOI18N

	// Holds the cookie header
	StringBuffer buf = new StringBuffer();
	
	Param[] headers = getHeaders().getParam();
	if(headers == null) headers = new Param[0]; 

	int len = headers.length;

	// No headers (this should not happen)
	// Create a set of headers and add a cookie header
	if(len == 0) { 
	    if(debug) log("We had a cookie header with no value");
	    buf.append(ckname);
	    buf.append("=");  //NOI18N
	    buf.append(ckvalue); 
	    if(debug) log("New cookie string is " + buf.toString()); //NOI18N
	    setCookieHeader(buf.toString()); 
	    return 1;
	}

	for(int i=0; i<len; ++i) {
	    if(!headers[i].getName().equalsIgnoreCase(COOKIE)) 
		continue; 

	    if(debug) log("Found cookie header");
	    String oldCookies = headers[i].getValue(); 

	    if(oldCookies != null && !oldCookies.trim().equals("")) { //NOI18N
		buf.append(oldCookies.trim());
		buf.append(";"); //NOI18N
	    } 
		
	    if(debug) log("appended ; to cookie string");
	    buf.append(ckname);
	    buf.append("=");//NOI18N
	    buf.append(ckvalue);
	    headers[i].setValue(buf.toString());
	    if(debug) log("New cookie string is " + buf.toString()); //NOI18N
	    return 1; 
	}
	 
	// There were no cookies, create a new header
	if(debug) log("We had no cookie header");
	buf.append(ckname);
	buf.append("=");  //NOI18N
	buf.append(ckvalue); 
	if(debug) log("New cookie string is " + buf.toString()); //NOI18N
	setCookieHeader(buf.toString()); 
	return 0;
    }


    /**
     * This method is used by EditPanelHeader in case the added header
     * turns out to be a cookie. 
     */
    public int addCookie(String ckstr) { 
	int index = ckstr.indexOf("="); 
	if(index == -1) return addCookie(ckstr, ""); 
	else if(index == ckstr.length()-1) return addCookie(ckstr, ""); 
	return addCookie(ckstr.substring(0,index), ckstr.substring(index+1)); 
    }

    public void setCookieHeader(String cookies) { 


    	Param[] headers = getHeaders().getParam();
	if(headers == null) headers = new Param[0];

	int len = headers.length;
	for(int i=0; i<len; ++i) {
	    if(!headers[i].getName().equalsIgnoreCase(COOKIE)) 
		continue; 

	    headers[i].setValue(cookies);
	    return;
	} 

	// We didn't find a cookie header - create one
	Param p = new Param(COOKIE, cookies);
	getHeaders().addParam(p); 
    } 


    public void deleteCookie(String ckname, String ckvalue) {

	if(debug) log("Deleting cookie: " + ckname + " " + ckvalue); //NOI18N
	
	Param[] headers = getHeaders().getParam();
	boolean noCookie = false; 

	// No headers (this should not happen) 
	if(headers == null || headers.length == 0) return;
	 
	int len = headers.length;
	for(int i=0; i<len; ++i) {

	    if(!headers[i].getName().equalsIgnoreCase(COOKIE)) 
		continue; 

	    if(debug) log(" found cookie header");//NOI18N

	    String oldCookies = headers[i].getValue(); 
	    
	    if(oldCookies == null || oldCookies.trim().equals("")) { //NOI18N
		if(debug) log(" no cookies!");//NOI18N
		return;
	    } 

	    if(debug) log(" old cookie string is " + oldCookies);//NOI18N
		
	    StringBuffer buf = new StringBuffer(); 
	    StringTokenizer tok = 
		new StringTokenizer(headers[i].getValue(),
				    ";", false); // NOI18N
	    
	    while (tok.hasMoreTokens()) {
		    
		String token = tok.nextToken();
		int j = token.indexOf("="); // NOI18N
		if (j > -1) {

		    String name = token.substring(0, j).trim();
		    String value = token.substring(j+1).trim();
		    value=stripQuote(value);

		    if(debug) log("Processing cookie: " + //NOI18N
				  name + " " + value); //NOI18N
			
		    if(name.equalsIgnoreCase(ckname) && value.equalsIgnoreCase(ckvalue)) 
			continue;
		    else {
			if(debug) log("Keep this cookie"); //NOI18N
			buf.append(name);
			buf.append("=");//NOI18N
			buf.append(value);
			buf.append(";"); //NOI18N
		    }
		}
		    
		if(debug) log("New cookie string is: " + //NOI18N
			      buf.toString());
	    }
	    if(buf.toString().equals("")) getHeaders().removeParam(headers[i]);
	    else headers[i].setValue(buf.toString());
	    break;
	}
	// In this case if we don't find the cookie string, we don't
	// need to do anything
    }

    public void deleteCookie(String ckname) {

	if(debug) log("Deleting cookie: " + ckname); //NOI18N
				     
	Param[] headers = getHeaders().getParam();
	// No headers (this should not happen) 
	if(headers == null || headers.length == 0) { 
	    if(debug) log("No headers"); //NOI18N
	    return;
	} 
	int len = headers.length;
	for(int i=0; i<len; ++i) {

	    if(debug) log("Examining header " + headers[i].getName()); //NOI18N

	    if(!headers[i].getName().equalsIgnoreCase(COOKIE)) 
		continue; 

	    String oldCookies = headers[i].getValue(); 
	    
	    if(oldCookies == null || oldCookies.trim().equals("")) { //NOI18N
		if(debug) log("No cookies"); //
		return;
	    } 
		
	    StringBuffer buf = new StringBuffer();
	    StringTokenizer tok = new StringTokenizer(headers[i].getValue(),
						      ";", false); // NOI18N
	    
	    while (tok.hasMoreTokens()) {
		    
		String token = tok.nextToken();
		int j = token.indexOf("="); // NOI18N
		if (j > -1) {

		    String name = token.substring(0, j).trim();
		    if(name.equalsIgnoreCase(ckname)) continue;
		    else {
			if(debug) log("Keep this cookie");//NOI18N
			String value = 
			    token.substring(j+1).trim();
			value=stripQuote(value);
			buf.append(name);
			buf.append("=");//NOI18N
			buf.append(value);
			buf.append(";"); //NOI18N
		    }
		}
	    }

	    if(debug) 
		log("New cookie string is: " + buf.toString()); //NOI18N

	    if(buf.toString().equals("")) getHeaders().removeParam(headers[i]);
	    else headers[i].setValue(buf.toString());
	    return;
	}
	// If we never find a cookie header we don't need to do
	// anything
    }
    

    /**
     * @param value a <code>String</code> specifying the cookie value
     * (possibly quoted). 
     */
    public static String stripQuote( String value )  {
	
	if (((value.startsWith("\"")) && (value.endsWith("\""))) || // NOI18N
	    ((value.startsWith("'") && (value.endsWith("'"))))) { // NOI18N
	    try {
		return value.substring(1,value.length()-1);
	    } catch (Exception ex) { 
	    }
	}
	return value;
    }  

    // This method verifies that the mandatory properties are set
    public boolean verify()
    {
	return true;
    }

    //
    public static void addComparator(BeanComparator c)
    {
	RequestData.comparators.add(c);
    }

    //
    public static void removeComparator(BeanComparator c)
    {
	RequestData.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n, PropertyChangeListener l)
    {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent)
    {
	String s;
	BaseBean n;

	str.append(indent);
	str.append("Headers"); //NOI18N
	n = this.getHeaders();
	if (n != null)
	    n.dump(str, indent + "\t"); //NOI18N
	else
	    str.append(indent+"\tnull"); //NOI18N
	this.dumpAttributes(HEADERS, 0, str, indent);

	str.append(indent);
	str.append("Param["+this.sizeParam()+"]");  //NOI18N
	for(int i=0; i<this.sizeParam(); i++)
	    {
		str.append(indent+"\t"); //NOI18N
		str.append("#"+i+":"); //NOI18N
		n = this.getParam(i);
		if (n != null)
		    n.dump(str, indent + "\t"); //NOI18N
		else
		    str.append(indent+"\tnull"); //NOI18N
		this.dumpAttributes(PARAM, i, str, indent);
	    }

    }

    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("RequestData\n");  //NOI18N
	this.dump(str, "\n  "); //NOI18N
	return str.toString();
    }
    
    //
    // This method returns the root of the bean graph
    // Each call creates a new bean graph from the specified DOM graph
    //
    public static RequestData createGraph(Node doc) {
	return new RequestData(doc, Common.NO_DEFAULT_VALUES);
    }

    public static RequestData createGraph(InputStream in) {
	return RequestData.createGraph(in, false);
    }

    public static RequestData createGraph(InputStream in, boolean validate) {
	try {
	    Document doc = GraphManager.createXmlDocument(in, validate);
	    return RequestData.createGraph(doc);
	}
	catch (Throwable t) {
	    throw new RuntimeException("DOM graph creation failed: "+  //NOI18N
				       t.getMessage()); 
	}
    }

    //
    // This method returns the root for a new empty bean graph
    //
    public static RequestData createGraph() {
	return new RequestData();
    }

    public void log(String s) { 
	System.out.println("RequestData::" + s); //NOI18N
    }

}

