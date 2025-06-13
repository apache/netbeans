/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.monitor.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.netbeans.modules.web.monitor.data.RequestData;
import org.netbeans.modules.web.monitor.data.Param;

/**
 * The MonitorRequestWrapper class is used by the MonitorFilter to
 * wrap the request. It's main function is to ensure that the
 * application receives the data from a replay request.
 */
public class MonitorRequestWrapper extends HttpServletRequestWrapper {
    
    private boolean replay = false;

    // These fields hold local variables during replays. 
    private String localMethod = null;
    private String localProtocol = null;
    private String localScheme = null;
    private String localRemoteAddr = null;
    private String localQueryString = null;
    private Param[] localHeaders = null;
    private Vector localCookies = null;
    private Map oldParams = null; 
    private Map localParams = null;
    private Stack extraParamStack = null;

    // These fields are used to manage session replacement during
    // replay, if the server supports it
    public static final String JSESSIONID = "JSESSIONID"; // NOI18N
    public static final String REPLACED =
	"netbeans.replay.session-replaced"; //NOI18N    

    private static final boolean debug = false;
    
    MonitorRequestWrapper(HttpServletRequest req) {
	super(req);
    }

    /**
     * The filter will only wrap the request if it was a
     * HttpServletRequest. This is a convenience method for 
     * accessing the request variable as such, for those methods that
     * aren't available on the regular servlet request.
     */
    private HttpServletRequest getHRequest() { 
	return (HttpServletRequest)getRequest(); 
    }

    // GETTERS FROM THE HttpServletRequest
    //
    // The getters implement the decorator pattern, except for replays
    // where we use local data. 


    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public String getMethod() {
	if (replay) {
	    return localMethod;
	}
	return getHRequest().getMethod();
    }

    // ********************** PARAMETERS *************************
    // All the getParameter methods must refer to getParameterMap for
    // locally set parameters. See pushExtraParameters and
    // popExtraParameters for additional detail on local parameters. 
    //

    /** getParameterMap returns<br>
     * a) the parameter map from the request, if this is not a replay<br>
     * b) the local parameter map, if no extra parameters have been
     *    set ("extra parameters" are parameters which are set through 
     *    the <jsp:param> tag inside <jsp:forward/include>. <br>
     * c) the local parameter map augmented with parameters so set. <br>
     *
     * @see pushExtraParameters
     * @see popExtraParameters
     */
    public java.util.Map getParameterMap() {

	if(debug) log("getParameterMap()"); //NOI18N

	if (!replay) return getRequest().getParameterMap();

	// Could cache the results of processing the parameters, but
	// it is relatively unusual that this gets expensive. It will
	// only get repeated on a replay where the request processing
	// involves a dispatch made from a JSP using the param tag AND
	// the file that got dispatched to accesses the parameter more
	// than once. 
	if(extraParamStack == null || extraParamStack.empty()) 
	    return (java.util.Map)localParams;

	Map map = (Map)extraParamStack.peek(); 

	if(map.size() == 0) return (java.util.Map)localParams;

	Hashtable ht = new Hashtable(); 

	Iterator keys = localParams.keySet().iterator(); 
	while(keys.hasNext()) { 
	    Object o = keys.next();
	    if(map.containsKey(o)) { 
		String[] vals0 = (String[])localParams.get(o); 
		String[] vals1 = (String[])map.get(o); 
		String[] vals2 = new String[vals0.length + vals1.length]; 
		System.arraycopy(vals0, 0, vals2, 0, vals0.length); 
		System.arraycopy(vals1, 0, vals2, vals0.length, vals1.length); 
		ht.put(o, vals2);
	    } 
	    else 
		ht.put(o, localParams.get(o)); 
	} 

        keys = map.keySet().iterator(); 
	while(keys.hasNext()) { 
	    Object o = keys.next();
	    if(localParams.containsKey(o)) continue;
	    ht.put(o, map.get(o)); 
	} 
	return (Map)ht; 
    }


    /**
    * If this is not a replay, getParameter(key) returns the value of
    * invoking the method on the original request. If it is a replay,
    * it returns the first string of the String array for the key, if
    * such an array exists. Otherwise it returns null.
    */
    public String getParameter(String key) {

	if(debug) log("getParameters()"); //NOI18N

	if (!replay) return getRequest().getParameter(key);

	String [] values = (String[])getParameterMap().get(key);
	if (values != null && values.length > 0) {
	    return values[0];
	}
	return null;
    }

    /** 
    * If this is not a replay, getParameterNames returns the value of
    * invoking the method on the original request. If it is a replay,
    * it returns an Enumeration derived from the keyset of the
    * parameter map. 
    */
    public Enumeration getParameterNames() {
	if(debug) log("getParameterNames"); //NOI18N
	if (!replay)
	    return getRequest().getParameterNames();
	if(debug) {
	    Enumeration e = new Vector(getParameterMap().keySet()).elements();
	    while(e.hasMoreElements()) 
		log("\t" + String.valueOf(e.nextElement())); //NOI18N
	}
	return new Vector(getParameterMap().keySet()).elements();
    }

    /** 
    * If this is not a replay, getParameterValues returns the value of
    * invoking the method on the original request. If it is a replay,
    * it returns a String array matching the key in the local parameter
    * map. 
    */
    public String [] getParameterValues(String name) {
	if(debug) log("getParameterValues"); //NOI18N
	if (!replay)
	    return getRequest().getParameterValues(name);
	return (String[])getParameterMap().get(name);
    }


    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public String getQueryString() {
	if (!replay)
	    return getHRequest().getQueryString();
	return localQueryString;
    }

    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public String getProtocol() {
	if (!replay)
	    return getRequest().getProtocol();
	return localProtocol;
    }


    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public String getScheme() {
	if (replay) return localScheme;
	return getRequest().getScheme();
    }

    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     * 
     * According to the Servlet specification, this method must return
     * null if there is no header of the specified name. 
     */
    public String getHeader(String key) {
	
	if (replay) { 
	    int len = localHeaders.length; 
	    for(int i=0; i<len; ++i) { 
		if(localHeaders[i].getName().equalsIgnoreCase(key)) 
		    return localHeaders[i].getValue(); 
	    }
	    if(debug) log("didn't find header"); //NOI18N
	    return null;
	}

	if(debug) { 
	    log("Headers not set locally"); //NOI18N
	    log(key + " " + getHRequest().getHeader(key)); //NOI18N
	}
	return getHRequest().getHeader(key);
    }

    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public Enumeration getHeaderNames() {

	if (replay) { 
	    Vector v = new Vector(); 

	    int len = localHeaders.length; 
	    for(int i=0; i<len; ++i)
		v.add(localHeaders[i].getName()); 
	
	    return v.elements(); //NOI18N
	}
	return getHRequest().getHeaderNames();
    }

    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public Enumeration getHeaders(String name) {

	if (replay) {

	    Vector v = new Vector(); 

	    int len = localHeaders.length; 
	    for(int i=0; i<len; ++i) { 
		if(localHeaders[i].getName().equalsIgnoreCase(name)) 
		    v.add(localHeaders[i].getValue()); 
	    }
	    return v.elements(); //NOI18N
	}
	return getHRequest().getHeaders(name);
    }

    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public int getIntHeader(String name) {
	int headerValue = -1;
	String value = getHeader(name);
	if (value != null)
	    headerValue = Integer.parseInt(value);
	
	return headerValue;
    }

    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public long getDateHeader (String name) {
	
	long dateValue = -1;
	String value = getHeader(name);
	
	if (value != null) {
	    int el = value.indexOf (';');
	    if (el != -1)
		value = value.substring (0, el);
	    
	    try	{
		dateValue = Date.parse (value);
	    } catch (Exception e) {
	    }
	    if (dateValue == -1) {
		// let it throw				
		throw new IllegalArgumentException ();
	    }
	}
	return dateValue;
    }    


    /**
     * During a replay, returns the local value. If not a replay,
     * simply returns the value of invoking the method on the wrapped 
     * request. 
     */
    public Cookie[] getCookies() {
	if(!replay) 
	    return getHRequest().getCookies(); 
	
	if(localCookies == null) 
	    return new Cookie[0];
	
	int numCookies = localCookies.size();
	Cookie[] cookieArray = new Cookie[numCookies];
	Enumeration e = localCookies.elements();
	int index = 0;
	while(e.hasMoreElements()) {
	    cookieArray[index] = (Cookie)e.nextElement(); 
	    ++index;
	}
	return cookieArray;
    }

    /**
     * This always returns the attributes that are set on the wrapped
     * request, regardless of whether this is a replay or
     * not. (Attributes are set by the web components, not as a result
     * of parsing the request). This method strips off any attributes
     * names which are used by the HTTP Monitor itself (these start
     * with "netbeans.monitor." so it's unlikely that they'll be used
     * in a regular app. 
     */
    public Enumeration getAttributeNames() {
		 
	if(debug) log("getAttributeNames()"); //NOI18N
	
	Enumeration e = getRequest().getAttributeNames();

	// If we're debugging, check the monitor attributes while
	// we're at it... 
	if(debug) return e; 

	Vector v = new Vector();
	while (e.hasMoreElements()) {
	    String name = (String)e.nextElement();
 	    // We don't record the request or the servlet attributes
	    // because we made those oursevles.
	    if(name.startsWith(MonitorFilter.PREFIX)) { 
		if(debug) log("discarded " + name); //NOI18N
		continue;
	    }
	    if(debug) log("keeping " + name); //NOI18N
	    v.add(name);
	}
	return v.elements();
    }

    // Pending - not held as local data though it should be 
    public String getRemoteAddr() {
	return getRequest().getRemoteAddr();
	/*
	if (!replay)
	    //return null; // not yet: getRequest().getRemoteAddr();
	    return getRequest().getRemoteAddr();
	return localRemoteAddr;
	*/
    }

    //*********************** END GETTERS  **********************


    /**
     * Populates the request wrapper with local data during a replay. 
     * 
     * @param rd The RequestData object that we use to repopulate this 
     * request
     * @param replaceSessionID true if the user requested for the
     * session ID to be replaced
     * @param canplaceSessionID true if the server can replace the
     * session ID 
     **/
    public void populate(RequestData rd, 
			 boolean replaceSessionID) { 

	// Replay is true, values are local 
	replay = true; 

	// Set the request method
	localMethod = rd.getAttributeValue("method");  // NOI18N

	// Set the protocol
	localProtocol = rd.getAttributeValue("protocol"); // NOI18N

	// Set the scheme 
	localScheme = rd.getAttributeValue("scheme"); // NOI18N

	// PENDING: Set the remote address
	// localRemoteAddr =  
	// rd.getClientData().getAttributeValue("remoteAddress"); // NOI18N

	// Set the query string 
	localQueryString = rd.getAttributeValue("queryString"); // NOI18N
	
	oldParams = getRequest().getParameterMap(); 

	// Do the parameters
	if(localMethod.equals("GET")) { //NOI18N

	    try {
		localParams = parseQueryString(localQueryString);
	    }
	    catch(Exception ex) {
		// This utility doesn't like query strings that aren't 
		// in parameter format
		localParams = new Hashtable();
	    }
	}

	else if(localMethod.equals("POST")) { // NOI18N

	    try {
		localParams = parseQueryString(localQueryString);
	    }
	    catch(Exception ex) {
		// This utility doesn't like query strings that aren't 
		// in parameter format
		localParams = new Hashtable();
	    }

	    // PENDING - this assumes the stuff comes in as parameters,
	    // but it could come in as non-parameterized data and
	    // ideally we should deal with this case also (and
	    // multiforms). Right now we just make sure we don't break
	    // everything if there is a non-parametrized request. 

	    Param[] params = rd.getParam();
	    int numParams = params.length;
	
	    for(int i=0; i<numParams; ++i) {
		String name = params[i].getAttributeValue("name"); // NOI18N
		String[] values = null;
		if(localParams.containsKey(name)) {
		    values = (String[])localParams.get(name);
		    String[] newvals = new String[values.length+1];
		    int j;
		    for(j=0; j<values.length; ++j)
			newvals[j] = values[j];
		    newvals[j] = params[i].getAttributeValue("value"); // NOI18N
		    localParams.put(name, newvals);  
		}
		else {
		    values = new String[1]; 
		    values[0] = params[i].getAttributeValue("value"); // NOI18N
		    localParams.put(name, values);  
		}
	    }
	}
	else if(localMethod.equals("PUT")) { // NOI18N

	    localParams = new Hashtable(); 

	    // PENDING
	    // This method would normally come with a file passed in
	    // through the inputstream. I am ignoring that. 
	}
	
	else {
	    // The user shouldn't be able to set any parameters for
	    // other HTTP methods. 
	    localParams = new Hashtable(); 
	}

	// Set the headers and cookies
	if(debug) {
	    log("CookieString from real req: " + //NOI18N
		String.valueOf(getHRequest().getHeader("cookie"))); //NOI18N
	    log("CookieString from rd: " + //NOI18N
		String.valueOf(rd.getCookieString())); 

	} 

	// Used to create the cookie header
	StringBuffer cookieBuf = new StringBuffer();

	// Set the headers in the wrapper to what they were set to in
	// the data record. It will have to be modified in case either 
	// a) The user wanted to use the browser's cookie
	// b) The server can't replace the session cookie
	int numHeaders = rd.getHeaders().sizeParam();
	localHeaders = new Param[numHeaders]; 
	for(int i=0; i<numHeaders; ++i) 
	    localHeaders[i] = rd.getHeaders().getParam(i); 

	if(debug) { 
	    log("How many parameters do we have?"); //NOI18N
	    log("param length is " + String.valueOf(localHeaders.length)); //NOI18N
	    for(int i=0; i<localHeaders.length; ++i) 
		log(localHeaders[i].getName() + " " + //NOI18N
		    localHeaders[i].getValue()); 
	}

	// We're going to parse the cookies again so we'll start with
	// an empty vector
	localCookies = new Vector();

	// Holds the session id from the data record
	String idFromRequest = null;

	// Cookies from the data record
	Param[] myCookies = rd.getCookiesAsParams();

	// Start by adding the recorded cookies, if there were any
	if(myCookies != null &&  myCookies.length > 0) {

	    if(debug) log("Now adding cookies"); //NOI18N

	    String ckname = null, ckvalue=null; 
	    for(int i=0; i<myCookies.length; ++i) {
		
		ckname = myCookies[i].getAttributeValue("name"); //NOI18N
		ckvalue = myCookies[i].getAttributeValue("value"); //NOI18N

		// We don't add the session cookie yet, but we keep
		// the session id from the record in case
		if(ckname.equalsIgnoreCase(JSESSIONID)) { 
		    idFromRequest = ckvalue;
		    continue; 
		}
		localCookies.add(new Cookie(ckname, ckvalue)); 
		if(debug) log("Added " + ckname + "=" + ckvalue); //NOI18N

		if(cookieBuf.length() > 0) cookieBuf.append("; "); //NOI18N
		cookieBuf.append(ckname);
		cookieBuf.append("="); //NOI18N
		cookieBuf.append(ckvalue);
	    }
	}

	// Find out if the session id was replaced by checking whether
	// the replaced attribute was set (by the MonitorValve, on
	// Tomcat). 

	boolean sessionReplaced = false; 
	try { 
	    String value = (String)getHRequest().getAttribute(REPLACED); 
	    if(value.equals("true")) sessionReplaced = true; //NOI18N
	    if(debug) log("replaced the session");  //NOI18N
	} 
	catch(Exception ex) { 
	    if(debug) log("didn't replace the session");  //NOI18N
	} 

	if(sessionReplaced) { 

	    // If the session ID was replaced, this could mean either 
	    // that we request a different session or that the
	    // reference to the session was removed. In the former
	    // case, we add the session cookie to the vector and to
	    // the cookie string, using the ID from the data record. 

	    if(idFromRequest != null) { 
		
		localCookies.add(new Cookie(JSESSIONID, idFromRequest)); 
		if(cookieBuf.length() > 0) cookieBuf.append("; "); //NOI18N
		    cookieBuf.append(JSESSIONID);
		    cookieBuf.append("="); //NOI18N
		    cookieBuf.append(idFromRequest);
	    }
	    // In the latter case we do nothing. 
	} 
	else { 

	    // The session ID was not replaced, and to adjust for this
	    // we add the session cookie from the incoming request, if
	    // there is one. 

	    // PENDING! 
	    // If the user wanted to replace the ID and it failed,
	    // this should be flagged here. 

	    if(debug) log("Old request is " + getHRequest().toString()); //NOI18N

	    Cookie[] ck = getHRequest().getCookies(); 

	    if(debug) log("Got the incoming cookies");  //NOI18N

	    if(ck != null && ck.length > 0) { 
		for(int i=0; i<ck.length; ++i) { 
		    if(ck[i].getName().equals(JSESSIONID)) { 
			localCookies.add(ck[i]);
			if(cookieBuf.length() > 0) cookieBuf.append("; "); //NOI18N
			cookieBuf.append(JSESSIONID);
			cookieBuf.append("="); //NOI18N
			cookieBuf.append(ck[i].getValue());
		    }
		}
	    }
	    // the entity that sent the request did not send any
	    // cookies, do nothing 
	}
	String cookieStr = cookieBuf.toString(); 
	if(cookieStr.equals("")) { //NOI18N
	    if(debug) log("No cookies, deleting cookie header"); //NOI18N
	    removeHeader("cookie"); //NOI18N
	}
	else { 
	    if(debug) log("Setting cookie header to " + //NOI18N
			  cookieBuf.toString()); 
	    setHeader("cookie", cookieBuf.toString()); //NOI18N
	}
    }


    // UTILITY METHODS 
    // These methods set local data. They are used by
    // populate to set data during replays. 


    /** 
     * This method MUST be invoked by the  monitor filter whenever it
     * receives a request to process a dispatched request, BEFORE it
     * collects any data. 
     * We need to do this because the specs allow web components to
     * add extra parameters on dispatch, e.g.  
     * <jsp:forward page="page.jsp">
     * <jsp:param name="name" value="value"/>
     * </jsp:forward>
     * and these are only available to the dispatched-to resources
     * (at least some servers remove them after the request dispatcher
     * has terminated). So we must have a mechanism which guarantees
     * that the resources in the web app (and the monitor itself) only
     * sees the parameters when they would have been visible without
     * the monitor in place. 
     * To deal with such parameters on a replay (where the request's
     * "real" parameters have been replaced with the ones from the
     * original request) I have to add them to the ones that the
     * wrapper already knows about.
     */
    void pushExtraParameters() {
	
	if(!replay) return; 

	if(debug) log("pushExtraParameters"); //NOI18N

	// If this is a replay and the request was dispatched using
	// the following type of syntax
	// <jsp:forward page="page.jsp">
	// <jsp:param name="name" value="value"/>
	// </jsp:forward>
	// then the server will add a parameter to the request in the
	// background (there are no API methods to do this). So we
	// have to check if the parameters that the original request
	// is aware of have grown. If so, we create an additional map
	// with the extra parameters. 

	Map extraParams = new Hashtable(); 

	Map currentMap = getRequest().getParameterMap(); 
	Iterator keys = currentMap.keySet().iterator(); 

	while(keys.hasNext()) { 
	    Object o = keys.next(); 
	    if(debug) { 
		//log("Key: " + (String)o); //NOI18N
		String[] value = (String[])currentMap.get(o); 
		StringBuffer buf = new StringBuffer();
		for(int k=0; k<value.length; ++k) { 
		    buf.append(value[k]); 
		    buf.append(" "); //NOI18N
		}
		log("Value: " + buf.toString()); //NOI18N
	    }

	    if(!oldParams.containsKey(o)) { 
		extraParams.put(o, currentMap.get(o)); 
		continue; 
	    }
	    
	    if(oldParams.get(o).equals(currentMap.get(o))) { 
		continue;
	    }
	    else { 
		extraParams.put(o, currentMap.get(o)); 
	    } 
	}
	if(extraParamStack == null) 
	    extraParamStack = new Stack(); 

	extraParamStack.push(extraParams); 
	if(debug) 
	    log("Param stack size: " + String.valueOf(extraParamStack.size())); //NOI18N
    }

    /** 
     * This method MUST be invoked by the  monitor filter whenever it
     * has finished processing a dispatched request, AFTER it has
     * collected the data. 
     * 
     * @see pushExtraParameters
     */
    void popExtraParameters() { 

	if(!replay) return; 

	if(debug) log("popExtraParameters"); //NOI18N
	if(extraParamStack == null || extraParamStack.empty()) { 
	    log("ERROR - MonitorRequestWrapper empty param stack!"); //NOI18N
	    return; 
	}
	extraParamStack.pop(); 
    } 


    /**
     * The setHeader method allows the request wrapper to modify
     * the value of a header. This method is only used during replay,
     * by the populate method. 
     */
    private void setHeader(String headerName, String headerValue) {
	
	if(!replay) { 
	    log("setHeader() must only be used from replay"); //NOI18N
	    return; 
	} 

	boolean addedHeader = false;

	if(debug) log("Headers were set locally"); //NOI18N
	for(int i=0; i<localHeaders.length; ++i) { 
	    if(localHeaders[i].getName().equalsIgnoreCase(headerName)) {
		localHeaders[i].setValue(headerValue); 
		addedHeader = true;
		if(debug) log("Replaced existing header"); //NOI18N
		break;
	    }
	}
	if(addedHeader) return; 
	Param[] p = new Param[localHeaders.length + 1]; 
	int numHeaders = 0; 
	while(numHeaders < localHeaders.length) { 
	    p[numHeaders] = localHeaders[numHeaders]; 
	    ++numHeaders;
	} 
	p[numHeaders] = new Param(headerName, headerValue); 
	localHeaders = p;
	if(debug) log("Added new header"); //NOI18N
	return; 
    } 

    /**
     * The removeHeader method allows the request wrapper to delete 
     * a header. This may be necessary during a replay, to remove the
     * session cookie if the browser didn't send one. Note that using
     * this method will result in the headers being local to the
     * RequestWrapper, since we can't manipulate the headers on the
     * actual request. 
     */
    private void removeHeader(String headerName) { 

	if(!replay) { 
	    log("removeHeader() must only be used from replay"); //NOI18N
	    return; 
	} 

	if(debug) log("removeHeader()"); //NOI18N

	Vector v = new Vector(); 
	
	for(int i=0; i<localHeaders.length; ++i) { 
	    if(localHeaders[i].getName().equalsIgnoreCase(headerName))
		continue; 
	    v.add(localHeaders[i]); 
	} 
	
	int size = v.size();
	localHeaders = new Param[size];
	for(int i=0; i< size; ++i) 
	    localHeaders[i] = (Param)v.elementAt(i);
	return; 
    }
	


    /**
     * toString prints out the main values of the request. 
     */ 
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("uri: "); // NOI18N
	buf.append(getRequestURI());
	buf.append("\n"); // NOI18N
	buf.append("method: "); // NOI18N
	buf.append(getMethod());
	buf.append("\n"); // NOI18N
	buf.append("QueryString: "); // NOI18N
	buf.append(getQueryString());
	buf.append("\n"); // NOI18N
	buf.append("Parameters:\n"); // NOI18N
	Enumeration e = getParameterNames();
	while(e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    String value = getParameter(name);
	    buf.append("\tName: "); // NOI18N
	    buf.append(name);
	    buf.append("\tValue: "); // NOI18N
	    buf.append(value);
	    buf.append("\n"); // NOI18N
	}

	buf.append("Headers:\n"); // NOI18N
	e = getHeaderNames();
	while(e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    String value = getHeader(name);
	    buf.append("\tName: "); // NOI18N
	    buf.append(name);
	    buf.append("\tValue: "); // NOI18N
	    buf.append(value);
	    buf.append("\n"); // NOI18N
	}
	return buf.toString();
    }
    
    /* 
     * log, used for debugging. 
     */
    private void log(String s) {
	System.out.println("MonitorRequestWrapper::" + s); // NOI18N
    }

    private Hashtable<String, String[]> parseQueryString(String queryString) {
        Map<String, List<String>> resultPrep = new HashMap<>();
        StringTokenizer st = new StringTokenizer(queryString, "&");
        while (st.hasMoreTokens()) {
            try {
                String pair = st.nextToken();
                int pos = pair.indexOf('=');

                String key;
                String val;
                if (pos >= 0) {
                    key = URLDecoder.decode(pair.substring(0, pos), "UTF-8");
                    val = URLDecoder.decode(pair.substring(pos + 1, pair.length()), "UTF-8");
                } else {
                    key = URLDecoder.decode(pair, "UTF-8");
                    val = "";
                }
                List<String> valueList = resultPrep.get(key);
                if (valueList == null) {
                    valueList = new ArrayList<>();
                    resultPrep.put(key, valueList);
                }
                valueList.add(val);
            } catch (UnsupportedEncodingException ex) {
                // Ignore - lets assume UTF-8 is supported everywhere
            }
        }
        Hashtable<String, String[]> result = new Hashtable<>();
        for (String key : resultPrep.keySet()) {
            result.put(key, (String[]) resultPrep.get(key).toArray());
        }
        return result;
    }
} // MonitorRequestWrapper
