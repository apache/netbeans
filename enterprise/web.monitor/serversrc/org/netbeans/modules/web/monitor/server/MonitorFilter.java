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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.net.MalformedURLException;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import java.text.DateFormat;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.netbeans.modules.web.monitor.data.*;

public class MonitorFilter extends Logger implements Filter {

    // REPLAY strings - must be coordinated with client.Controller
    public static final String REPLAY = "netbeans.replay"; //NOI18N
    public static final String PORT = "netbeans.replay.port"; //NOI18N
    public static final String REPLAYSTATUS = "netbeans.replay.status"; //NOI18N
    public static final String REPLAYSESSION = "netbeans.replay.session"; //NOI18N

    // The request attribute name under which we store a reference to
    // ourself. 
    private String attribute = null;
    public static final String PREFIX = "netbeans.monitor"; //NOI18N
    private static final String attNameRequest =
	"netbeans.monitor.request"; //NOI18N
    private static final String attNameResponse =
	"netbeans.monitor.response"; //NOI18N
    private static final String attNameFilter =
	"netbeans.monitor.filter"; //NOI18N
    private static final String attNameMonData =
	"netbeans.monitor.monData"; //NOI18N
    //private final static String attNameExecTime =
    //"netbeans.monitor.execTime"; //NOI18N
    public static final String IDE = "netbeans.monitor.ide"; //NOI18N
    public static final String IDES = "netbeans.monitor.register"; //NOI18N

    // Are we supposed to run?
    private static boolean collectData = true;
    
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    private static final String className = 
	"org.netbeans.modules.web.monitor.server.Monitor"; //NOI18N
    
    private static ResourceBundle statusmsgs = 
	ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.MonitorBundle"); //NOI18N 

    private static NotifyUtil notifyUtil = null;

    /**
     * List of AppServer system web modules whose requests will be filtered out
     */
    private static final String APPSERVER_SYSTEM_WEB_MODULES[] = {
                "/com_sun_web_ui", //NOI18N
                "/asadmin", //NOI18N
                "/web1"}; //NOI18N

    /**
     * Netbeans internal request URI to find out the Tomcat running status.
     *
     * Issue #47048 - the HttpURLConnection is now used to determine whether Tomcat
     * is running. The request URI equals "/netbeans-tomcat-status-test" to make
     * it possible for the monitor to filter it out.
     */
    private static final String NETBEANS_INTERNAL_REQUEST_URI = 
                "/netbeans-tomcat-status-test"; //NOI18N

    // debugging 
    private static final boolean debug = false;

    public MonitorFilter() { 
    } 

    /**
     * Collects data of the HTTP Transaction
     *
     * @param request The servlet request we are processing
     * @param result The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
	throws IOException, ServletException {

	// Time stamp for when Monitor filter is invoked
	//long entryTime = System.currentTimeMillis(); 

	if(debug) log("doFilter()");  //NOI18N

	// PENDING - checking with Tomcat developers to find out if
	// there is some other way to reliably determine that a
	// request is for the manager application.
	Object obj = getFilterConfig().getServletContext().getAttribute("org.apache.catalina.MBeanServer"); 
        
        boolean internalAppServerRequest = false;
        boolean internalIDERequest = false;
        if (request instanceof HttpServletRequest) {
            // PENDING - a saffer way how to filter out internal AppServer requests
            // should be used, system web modules URIs can be changed in the next 
            // AppServer release
            String requestURI = ((HttpServletRequest)request).getRequestURI();
            for (int i = 0; i < APPSERVER_SYSTEM_WEB_MODULES.length; i++) {
                if (requestURI.startsWith(APPSERVER_SYSTEM_WEB_MODULES[i])) {
                    internalAppServerRequest = true;
                    break;
                }
            }
            
            // Issue #47048 - the HttpURLConnection is now used to determine whether Tomcat
            // is running. The requestURI is "/netbeans-tomcat-status-test" to make 
            // it possible for the monitor to filter it out.
            if (requestURI.startsWith(NETBEANS_INTERNAL_REQUEST_URI)) {
                internalIDERequest = true;
            }
        }
        
	if(!collectData || !(request instanceof HttpServletRequest) ||
	   obj != null || internalAppServerRequest || internalIDERequest)  {

	    if(debug) log("not collecting data"); //NOI18N
	    // Do not be tempted to factor this into its own methods
	    // - gotta retrow those exceptions remember... 
	    try {
		chain.doFilter(request, response);
	    }
	    catch(Throwable t) {
		rethrow(t);
	    }
	    return;
	}
	
	HttpServletRequest req = (HttpServletRequest)request; 
	if(debug) log("Request for: " + req.getRequestURI()); //NOI18N

	// On servlet 2.4 containers, the filter processes dispatched
	// requests as well. We need to know whether this invocation
	// is the outermost invocation or not. The outermost
	// invocation sets up the MonitorData object and replaces the
	// request and response with a wrappers, and is responsible
	// for sending data back. Inner invocations create another
	// type of data object and link them to the one for the parent
	// invocation. 
	boolean outermost = true; 

	HttpServletRequestWrapper requestWrapper = null; 
	HttpServletResponseWrapper responseWrapper = null; 

	// We collect the same type of data for original and
	// dispatched requests. The latter is nested inside the
	// former. The schema2beans library does (did?) not allow an
	// XML element to have itself as a member, so we use an
	// interface DataRecord to represent either a MonitorData
	// record (outer) or DispatchData (inner).
	DataRecord dr = null; 

	if(request instanceof HttpServletRequestWrapper &&
	   response instanceof HttpServletResponseWrapper) { 
	    
	    Object o = req.getAttribute(attNameRequest); 
	    if(o instanceof MonitorRequestWrapper) { 
		
		if(debug) 
		    log("Request previously processed by the monitor"); //NOI18N
		outermost = false; 

		// The response has been wrapped by the Monitor and
		// possibly by another entity as well (meaning that it
		// was dispatched). We use the outermost wrappers.
		requestWrapper = (HttpServletRequestWrapper)req; 
		responseWrapper = (HttpServletResponseWrapper)response;
		
		// Create the data record for the dispatched request
		// and link it into the parent data record. 
		dr = setupDispatchDataRecord((MonitorRequestWrapper)o); 

		// This will also add the time since the monitor last
		// left off to the execution time the resourced that
		// caused the current dispatch.
		//dr = setupDispatchDataRecord((MonitorRequestWrapper)o, 
		// entryTime); 
		if(dr == null) { 
		    if(debug) 
			log("failed to link to parent data record"); //NOI18N
		    try { chain.doFilter(request, response); }
		    catch(Throwable t) { rethrow (t); }
		    return;
		} 
	    }
			    
	    // If we can't get the MonitorRequestWrapper from the
	    // attribute, the request was wrapped, but not by the
	    // Monitor. This means that we're processing an incoming
	    // request (as opposed to a dispatched request, which
	    // would have been wrapped by the monitor already) and
	    // that another Filter has been deployed before the
	    // MonitorFilter. We log a warning to the log file to
	    // inform the user of this. (Unless the other filter
	    // modifies the request, this is probably harmless, though
	    // the other filter might give the "wrong" info in case of
	    // a replay for example).

	    if(requestWrapper == null) log(ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Deploy_first")); //NOI18N
	} 

	if(requestWrapper == null) { 

	    if(debug) log("request just entered container"); //NOI18N
	    // The request was not wrapped, signalling that it is an
	    // original (non-dispatched) request. 
	
	    // The first step is to check whether it was a replay or
	    // not. If it is a replay, the query string will have a
	    // replay parameter. Note that we must *not* use
	    // getParameter() because this will cause the query string
	    // and data to be parsed once and for all, and it is
	    // possible that the application object expects to parse
	    // the inputstream directly. 
	    String query = req.getQueryString();

	    if(query != null && query.startsWith(REPLAY)) { 

		if(debug) log("received replay request"); //NOI18N

		try { 
		    requestWrapper = getReplayRequest(req); 
		} 
		catch(IOException ioex) { 
		    // We received a request for a replay, but failed
		    // to retrive replay data. We process this as a
		    // normal request, as the request will go to the
		    // same resource, though perhaps the behaviour
		    // should be modified to show an error msg
		    // instead).  
		    try { chain.doFilter(request, response);	}
		    catch(Throwable t) { rethrow (t); }
		    return;
		}
	    }
	    else {
		if(debug) log("wrapping the request"); //NOI18N
		requestWrapper = new MonitorRequestWrapper(req);  
	    }
		
	    // Set the wrapper as a request attribute. Because
	    // developers can use as many filters that they like,
	    // and because these filter must use a wrapped request
	    // (since Servlet 2.4), there's no guarantee that the
	    // request object this filter receives on dispatch is
	    // a MonitorRequestWrapper. Setting it as an attribute
	    // saves us from walking the wrapper chain to find the
	    // wrapper, and also to find out if it this was a
	    // dispatched request (see above). 
	    requestWrapper.setAttribute(attNameRequest, requestWrapper); 

	    // Create the data record, using the wrapped request. 
	    dr = setupDataRecord(requestWrapper); 

	    // Create the response wrapper
	    if(debug) log(" Replace Response"); //NOI18N
	    HttpServletResponse res = (HttpServletResponse)response;

	    // The responseWrapper has a handle on the response
	    // (obviously) and also on the request. The latter is
	    // used to determine whether the request is currently
	    // dispatched as an include, for the purposes of knowing
	    // whether cookies can be added or not. 
	    responseWrapper = 
		new MonitorResponseWrapper(res, requestWrapper); 
	    
	    // We need the response wrapper to collect data after the
	    // request has processed, this saves us from walking the
	    // wrapper chain. 
	    requestWrapper.setAttribute(attNameResponse, responseWrapper); 
	    
	    // This attribute allows a Servlet 2.3 container to locate
	    // the filter and invoke the methods deal with dispatched
	    // requests if they have the capability of listening to
	    // such events. 
	    requestWrapper.setAttribute(attNameFilter, this); 
	} 

	Throwable processingError = null;
	boolean cntnue = true; 

	// Collect data about the request before it is processed
	if(debug) log("doFilter(): Collect data before"); //NOI18N

	try { 
	    getDataBefore(dr, requestWrapper); 

	}
	catch(StackOverflowError soe) { 
	    // The developer has done something that causes an
	    // infinite loop. We will not go through with running the
	    // filter and catching more data. Technically we should
	    // delete the last DispatchData record from the monitor
	    // stack, but I will just push on an incomplete one. The
	    // user would have to open about a 100 nested records to
	    // see an error. 

	    if(debug) 
		log(" StackOverflow before processing the request"); //NOI18N
	    processingError = soe; 
	    cntnue = false; 
	}

	catch(Throwable t) { 
	    // There was a problem in the monitor code, ignore and continue
	    if(debug) log(getStackTrace(t)); 
	} 

	if(cntnue) { 

	    //if(debug) log("Setting time in request attribute"); 
	    //requestWrapper.setAttribute(attNameExecTime, 
	    //new Long(System.currentTimeMillis())); 
	    try { 
		chain.doFilter(requestWrapper, responseWrapper);
	    }

	    catch(StackOverflowError soe) { 
		// The developer has done something that causes an
		// infinite loop. We will not go through with running the
		// filter and catching more data. Technically we should
		// delete the last DispatchData record from the monitor
		// stack, but I will just push on an incomplete one. The
		// user would have to open about a 100 nested records to
		// see an error. 

		if(debug) 
		    log(" StackOverflow while processing the request"); //NOI18N
		processingError = soe; 
		cntnue = false; 
	    }

	    catch(Throwable t) {
		processingError = t;
	    }
	    /*
	      try { 
	      long exit = System.currentTimeMillis();
	      if(debug) log("Setting execution time on last known data record");
	      long entry = ((Long)requestWrapper.getAttribute(attNameExecTime)).
	      longValue(); 
	      dr.addExecTime(exit-entry); 
	      }
	      catch(NullPointerException npe) { 
	      // Couldn't get the attribute - perhaps the request got
	      // mangled by an intrusive user request wrapper
	      if(debug) npe.printStackTrace();
	      }
	      catch(ClassCastException cce) { 
	      // The attribute did not contain a long. Shouldn't
	      // happen. 
	      if(debug) cce.printStackTrace();
	      }
	      catch(Throwable t) { 
	      // The attribute did not contain a long. Shouldn't
	      // happen. 
	      if(debug) t.printStackTrace();
	      }
	    */
	}

	if(cntnue) { 

	    // Collect data after the request is processed. (This method
	    // gets the MonitorResponseWrapper from the request
	    // attribute. As for the request, we can get it from any
	    // request object, we need the wrapper only for the replay
	    // setup.) 
	    if(debug) log("doFilter(): Collect data after"); //NOI18N

	    try { 
		getDataAfter(dr, requestWrapper); 
	    }
	    catch(Throwable t) { 
		if(debug) log(getStackTrace(t)); 
	    } 
	} 

	// Finish the record
	if(outermost) { 
	    if(debug) log("Final, send data to server"); //NOI18N
	    disposeDataRecord(requestWrapper); 
	} 
	else { 
	    if(debug) log("Non-final, continue processing"); //NOI18N
	    disposeDispatchedDataRecord(requestWrapper); 
	    // Pop the RequestWrapper's parameter stack	
	    ((MonitorRequestWrapper)(requestWrapper.getAttribute(attNameRequest))).popExtraParameters(); 
	} 
       
	if(processingError != null) {
	    // user will get this from the log 
	    // log("A web application object caused an exception"); //NOI18N
	    // log(getStackTrace(processingError)); 
	    rethrow(processingError); 
	}
    
	// Set the time stamp
	// req.setAttribute(attNameExecTime, 
	//	new Long(System.currentTimeMillis())); 
    }

    private DataRecord setupDataRecord(ServletRequest req) { 

	if(debug) log("setupDataRecord()"); //NOI18N
	MonitorData md = new MonitorData(); 
	Stack dataStack = new Stack(); 
	dataStack.push(md);
	if(debug) log(" created MonData stack & set attribute");  //NOI18N
	req.setAttribute(attNameMonData, dataStack); 
	return md; 
    }

    /**
     * setupDispatchDataRecord creates a new DispatchDataRecord to
     * record data for the request we are about to process and links
     * it to the Dispatches category of the data record associated 
     * with the resource which dispatched the request. If that
     * resource has not dispatched before (true if forward, or if
     * first include), then we create a Dispatches object for this
     * data record.  
     * @param req The MonitorRequestWrapper associated with the
     * request.
     * @return a fresh DataRecord
     */
    private DataRecord setupDispatchDataRecord(MonitorRequestWrapper req) { 

	/* @param entryTime The value returned by
	 * System.currentTimeMillis() when the MonitorFilter kicked in for
	 * this request. Gotten by doFilter in a Servlet 2.4 environment,
	 * by handleDispatchedBefore pre Servlet 2.4. */
	// long entryTime) 

	req.pushExtraParameters(); 
	if(debug) log("Pushed the wrapper parameters"); //NOI18N

	Stack dataStack = null;
	try {
	    dataStack = (Stack)(req.getAttribute(attNameMonData)); 
	}
	catch(Throwable t){
	    // This should not fail 
	    if(debug) log("MonitorFilter - this request had no stack");//NOI18N
	    return null;
	}
	 
	if(dataStack.empty()) {
	    if(debug) log("process dispatched ERROR - stack is empty");//NOI18N
	    return null;
	}

	Object obj = dataStack.peek(); 
	Dispatches disp = null;

	if(!(obj instanceof DataRecord)) {
	    if(debug) log("ERROR - obj on stack is not DataRecord"); //NOI18N
	    return null; 
	} 

	DataRecord dr = (DataRecord)obj;

	// Add the execution time to the record
	/*
	  try { 
	  if(debug) log("Setting execution time on last known data record");
	  long lastExit = ((Long)req.getAttribute(attNameExecTime)).longValue(); 
	  dr.addExecTime(entryTime-lastExit); 
	  }
	  catch(NullPointerException npe) { 
	  // Couldn't get the attribute - perhaps the request got
	  // mangled by an intrusive user request wrapper
	  if(debug) npe.printStackTrace();
	  }
	  catch(ClassCastException cce) { 
	  // The attribute did not contain a long. Shouldn't
	  // happen. 
	  if(debug) cce.printStackTrace();
	  }
	  catch(Throwable t) { 
	  // Something went wrong with schema2beans
	  if(debug) t.printStackTrace();
	  }
	*/

	disp = dr.getDispatches();
	if(disp == null) { 
	    if(debug) log("Data record had no dispatches yet"); //NOI18N
	    disp = new Dispatches();
	    dr.setDispatches(disp);
	}

	DispatchData disData = new DispatchData();
	disp.addDispatchData(disData);
	if(debug) log("Added new data record to existing one"); //NOI18N

	dataStack.push(disData);
	if(debug) log("pushed the data record onto the stack"); //NOI18N
	return disData;
    } 

    private void disposeDataRecord(ServletRequest req) { 

	if(debug) log("disposeDataRecord()"); //NOI18N

	// Remove the attributes used by the monitor - we need to do
	// this in case there is an error...
	req.removeAttribute(attNameRequest); 
	req.removeAttribute(attNameResponse); 
	req.removeAttribute(attNameFilter); 

	MonitorData monData = null;

	Stack stack = (Stack)(req.getAttribute(attNameMonData)); 
	req.removeAttribute(attNameMonData); 

	if(stack != null && !stack.empty()) { 
	    if(debug) { 
		log("found mondata stack"); //NOI18N	
		log("stack size=" + stack.size()); //NOI18N	
	    } 
	    Object o = stack.pop(); 
	    if(o instanceof MonitorData) 
		monData = (MonitorData)o;
	    else if(debug) { 
		log(o.toString()); 
		log("ERROR - wrong type object on stack"); //NOI18N
	    } 
	}
	else if(debug) { 
	    log("ERROR - mondata stack empty"); //NOI18N
	} 

	if(monData == null) { 
	    return; 
	} 

	StringBuffer buf = 
	    new StringBuffer(monData.getAttributeValue("id")); //NOI18N
	buf.append(Constants.Punctuation.itemSep);
	buf.append(monData.getAttributeValue("method")); //NOI18N
	buf.append(Constants.Punctuation.itemSep);
	buf.append(monData.getAttributeValue("resource")); //NOI18N
	
	if(debug) { 
	    log(" Notify client"); //NOI18N	
	    log(" Query string is "  + //NOI18N
		buf.toString());
	    log("Notify util is " + notifyUtil.toString()); //NOI18N

	    String file = 
		monData.createTempFile("filter-send.xml"); // NOI18N
	    log("Wrote data to " + file); // NOI18N
	}

	notifyUtil.sendRecord(monData, buf.toString()); 
	if(debug) log("Notify util has terminated"); //NOI18N

    }

    private DataRecord disposeDispatchedDataRecord(ServletRequest req) { 

	Stack stack = (Stack)(req.getAttribute(attNameMonData)); 
	Object o = null; 
	if(stack != null && !stack.empty())
	    o = stack.pop(); 
	if(o instanceof DataRecord) return (DataRecord)o; 
	return null; 
    } 

    /**
     * This is a utility method for Servlet 2.3 containers. Configure
     * the container to access the servlet filter from the request
     * attribute and invoke this method in order to gather data about
     * dispatched requests. 
     */
    public void handleDispatchedBefore(ServletRequest req) { 

	// Time stamp for when Monitor filter is invoked
	// long entryTime = System.currentTimeMillis(); 

	if(debug) log ("handleDispatchBefore: start");//NOI18N

	Object w  = req.getAttribute(attNameRequest); 
	if(!(w instanceof MonitorRequestWrapper)) {
	    return; 
	} 
	// get the dispatch data 
	DataRecord dr = setupDispatchDataRecord((MonitorRequestWrapper)w); 
	//entryTime); 

	if(dr == null) return; 

	// collect data 
	getDataBefore(dr, (HttpServletRequest)req); 

	/*
	  if(debug) log("Setting time attribute on request wrapper");
	  req.setAttribute(attNameExecTime, 
	  new Long(System.currentTimeMillis())); 
	*/
	return; 
    } 
				       

    /**
     * This is a utility method for Servlet 2.3 containers. Configure
     * the container to access the servlet filter from the request
     * attribute and invoke this method in order to gather data about
     * dispatched requests. 
     */
    public void handleDispatchedAfter(ServletRequest req) { 

	//long entry = System.currentTimeMillis(); 
	
	if(debug) log ("handleDispatchedAfter()");//NOI18N

	Object w  = req.getAttribute(attNameRequest); 
	if(!(w instanceof MonitorRequestWrapper)) {
	    return; 
	} 

	DataRecord dr = disposeDispatchedDataRecord((MonitorRequestWrapper)w);
	if(dr == null) return; 

	/*
	  try { 
	  if(debug) log("Setting execution time on last known data record");
	  long exit = 
	  ((Long)req.getAttribute(attNameExecTime)).longValue(); 
	  dr.addExecTime(entry-exit); 
	  }
	  catch(NullPointerException npe) { 
	  // Couldn't get the attribute - perhaps the request got
	  // mangled by an intrusive user request wrapper
	  if(debug) npe.printStackTrace();
	  }
	  catch(ClassCastException cce) { 
	  // The attribute did not contain a long. Shouldn't
	  // happen. 
	  if(debug) cce.printStackTrace();
	  }
	  catch(Throwable t) { 
	  // The attribute did not contain a long. Shouldn't
	  // happen. 
	  if(debug) t.printStackTrace();
	  }
	*/

	// collect data 
	getDataAfter(dr, (HttpServletRequest)req); 
	if(debug) log ("collected data");//NOI18N

	// Pop the RequestWrapper's parameter stack	
	((MonitorRequestWrapper)w).popExtraParameters(); 

	/*
	  if(debug) log("Setting time attribute on request wrapper");
	  req.setAttribute(attNameExecTime, 
	  new Long(System.currentTimeMillis())); 
	*/
	return; 
    } 
    
    /**
     * Collects data from the HttpServletRequest before the servlet
     * processes it 
     */
    private void getDataBefore(DataRecord dataRecord, 
			       HttpServletRequest request) {

	if(dataRecord instanceof MonitorData) {
	    String timestamp = String.valueOf(System.currentTimeMillis()); 
	    String method = request.getMethod();
	    String uri = request.getRequestURI();

	    // PENDING - this is used for the label and should refer to
	    // the resource, not to the URI that was used to access it. 
	    String resource = new String(uri);

	    // PENDING - don't use the timestamp as the ID
	    String id = new String(timestamp);
	    
	    if(debug) { 
		log("            id: " + id); //NOI18N
		log("           uri: " + request.getRequestURI()); //NOI18N
	    }
	    dataRecord.setAttributeValue("id", id); //NOI18N
	    dataRecord.setAttributeValue("timestamp", timestamp); //NOI18N
	    dataRecord.setAttributeValue("resource", resource); //NOI18N
	    dataRecord.setAttributeValue("method", method); //NOI18N
	}
	else if(dataRecord instanceof DispatchData) {
	    String resource =
		(String)request.getAttribute("javax.servlet.include.request_uri"); //NOI18N
	    if(resource == null || resource.equals("")) //NOI18N
		resource = request.getRequestURI();
	    
	    dataRecord.setAttributeValue("resource", resource);//NOI18N
	}
	
	// PENDING: 
	// The following three only need to be recorded once per
	// request. Need to modify the client first however. 
	// ---------------------- FROM HERE -------------------
	if(debug) log(" Client Data"); //NOI18N
	ClientData cd = new ClientData();
	recordClientData(cd, request);
	dataRecord.setClientData(cd);
	
	if(debug) log(" Context data"); //NOI18N
	ContextData cond = new ContextData();
	recordContextData(cond, request);
	dataRecord.setContextData(cond);
	
	if(debug) log(" Servlet engine data"); //NOI18N
	EngineData ed = new EngineData();
	recordEngineData(ed, request);
	dataRecord.setEngineData(ed);
	// ---------------------- TO HERE ---------------------
	 
	if(debug) log(" Session Data"); //NOI18N
	SessionData sd = new SessionData();
	getSessionIn(sd, request);
	dataRecord.setSessionData(sd);
	
	if(debug) log(" Request Data"); //NOI18N
	RequestData rd = new RequestData(); 
	recordRequestData(rd, request);
	if(debug) log(" Set Request Data"); //NOI18N
	dataRecord.setRequestData(rd); 

	// We must do this after we have processed the headers
	if(debug) log(" Cookie Data"); //NOI18N
	CookiesData cookiesData = new CookiesData();
	recordCookiesIn(cookiesData, request); 
	dataRecord.setCookiesData(cookiesData); 

	if(debug) log("getDataBefore(): done"); //NOI18N
    }

    
    private void getDataAfter(DataRecord dataRecord, 
			      HttpServletRequest request) { 

	if(debug) log("getDataAfter(DataRecord, HttpServletRequest)"); //NOI18N
	
	MonitorResponseWrapper monResponse = 
	    (MonitorResponseWrapper)request.getAttribute(attNameResponse); 
	
	if(debug) log(" Get status");	//NOI18N
	int status = monResponse.getStatus();
        RequestData rd = dataRecord.getRequestData();
        // if the exit status is unknown, display appropriate msg instead
        if (status == 0) {
            if(debug) log(ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Unknown_exit_status"));
            rd.setAttributeValue("status", //NOI18N
                    ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Unknown_exit_status"));
        } else {
            String statusStr = "sc".concat(String.valueOf(status)); //NOI18N
            if(debug) log("Status string is " + statusStr); 
	
            if(debug) log(String.valueOf(statusmsgs.getString(statusStr))); 

            rd.setAttributeValue("status", //NOI18N
			     statusmsgs.getString(statusStr)); //NOI18N
        }

	if(debug) log(" request attributes out"); //NOI18N
	RequestAttributesOut reqattrout = new RequestAttributesOut();
	reqattrout.setParam(recordRequestAttributes(request));
	
	rd.setRequestAttributesOut(reqattrout);

	if(debug)  log(" add request parameter"); //NOI18N
	addRequestParameters(rd, request); 

	if(debug) log(" Cookies out"); //NOI18N
	recordCookiesOut(dataRecord.getCookiesData(), monResponse); 

	if(debug) log(" Session out"); //NOI18N
	addSessionOut(dataRecord.getSessionData(), request);
    }
    

    /**
     * Creates an instance of ClientData based on the request
     */
    private void recordClientData(ClientData cd, 
				  HttpServletRequest request) {

	String protocol = request.getProtocol();
	while(protocol.endsWith("\n")) //NOI18N
	    protocol = protocol.substring(0, protocol.length()-2);
	 
	cd.setAttributeValue("protocol", protocol); //NOI18N
	cd.setAttributeValue("remoteAddress", //NOI18N
			     request.getRemoteAddr()); 

	Enumeration hvals; 
	StringBuffer valueBuf; 
	int counter; 

	// Software used
	valueBuf = new StringBuffer(128);
	counter = 0;
	hvals = request.getHeaders(Constants.Http.userAgent);
	if(hvals != null) { 
	    while(hvals.hasMoreElements()) { 
		if(counter > 0) valueBuf.append(", "); // NOI18N
		valueBuf.append((String)hvals.nextElement());
		++counter;
	    }
	}
	cd.setAttributeValue("software", valueBuf.toString()); //NOI18N
	 
	//Languages
	valueBuf = new StringBuffer(128);
	counter = 0;
	hvals = request.getHeaders(Constants.Http.acceptLang);
	if(hvals != null) { 
	    while(hvals.hasMoreElements()) { 
		if(counter > 0) valueBuf.append(", "); // NOI18N
		valueBuf.append((String)hvals.nextElement());
		++counter;
	    }
	}
	cd.setAttributeValue("locale", valueBuf.toString()); //NOI18N      

	// File formats
	valueBuf = new StringBuffer(128);
	counter = 0;
	hvals = request.getHeaders(Constants.Http.accept);
	if(hvals != null) { 
	    while(hvals.hasMoreElements()) { 
		if(counter > 0) valueBuf.append(", "); // NOI18N
		valueBuf.append((String)hvals.nextElement());
		++counter;
	    }
	}
	cd.setAttributeValue("formatsAccepted", valueBuf.toString()); //NOI18N
			   
	// Encoding
	valueBuf = new StringBuffer(128);
	counter = 0;
	hvals = request.getHeaders(Constants.Http.acceptEncoding);
	if(hvals != null) { 
	    while(hvals.hasMoreElements()) { 
		if(counter > 0) valueBuf.append(", "); // NOI18N
		valueBuf.append((String)hvals.nextElement());
		++counter;
	    }
	}
	cd.setAttributeValue("encodingsAccepted", valueBuf.toString()); //NOI18N
	//Char sets
	valueBuf = new StringBuffer(128);
	counter = 0;
	hvals = request.getHeaders(Constants.Http.acceptCharset);
	if(hvals != null) { 
	    while(hvals.hasMoreElements()) { 
		if(counter > 0) valueBuf.append(", "); // NOI18N
		valueBuf.append((String)hvals.nextElement());
		++counter;
	    }
	}
	cd.setAttributeValue("charsetsAccepted", valueBuf.toString()); //NOI18N    
	
    }
    
    private void recordCookiesIn(CookiesData cd, 
				 HttpServletRequest request) {  

	Cookie cks[] = null; 

	try { 
	    cks = request.getCookies();
	}
	catch(Exception ex) { 
	    // Do nothing, there were no cookies
	}
	
	if(cks == null || cks.length == 0) { 
	    if(debug) log(" no incoming cookies"); //NOI18N
	    cd.setCookieIn(new CookieIn[0]);
	    return;
	}

	if(debug) log(" found incoming cookies"); //NOI18N
	CookieIn[] theCookies = new CookieIn[cks.length];
	for (int i = 0; i < theCookies.length; i++) {
	    theCookies[i] = new CookieIn(cks[i]);
	    if(debug) log("cookie: " + //NOI18N
			  theCookies[i].toString());
	}
	cd.setCookieIn(theCookies);
    }
    
    private void recordCookiesOut(CookiesData cd, 
				  MonitorResponseWrapper response) {  

	if(debug) log(" Cookies out"); //NOI18N

	Enumeration e = response.getCookies();
	int numCookies = 0;
	while(e.hasMoreElements()) {
	    e.nextElement();
	    ++numCookies;
	}
	
	if(numCookies == 0) {
	    if(debug) log(" no cookies"); //NOI18N
	    cd.setCookieOut(new CookieOut[0]);
	    return;
	}

	if(debug) log(" number of cookies is " + //NOI18N
		      String.valueOf(numCookies)); //NOI18N
	e = response.getCookies();
	CookieOut[] theCookies = null;
	try {
	    theCookies = new CookieOut[numCookies];
	    for (int i = 0; i < theCookies.length; i++) {
		theCookies[i] = new CookieOut((Cookie)e.nextElement()); 
		if(debug) log("cookie: " + //NOI18N
			      theCookies[i].toString());
	    }
	}
	catch(NullPointerException ne) {
	    theCookies = new CookieOut[0];
	}
	cd.setCookieOut(theCookies);
    }


    private void getSessionIn(SessionData sess, 
			      HttpServletRequest request) {  

	HttpSession sessIn = null; 
	try { 
	    sessIn = request.getSession(false); 
	}
	catch(Exception ne) {}

	if(sessIn == null) {
	    sess.setAttributeValue("before", "false"); //NOI18N
	    return;
	}

	sess.setAttributeValue("before", "true"); //NOI18N

	sess.setAttributeValue("id", sessIn.getId()); //NOI18N
	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,
						       DateFormat.SHORT);  
	Date date = new Date(sessIn.getCreationTime()); 
	sess.setAttributeValue("created", df.format(date)); //NOI18N

	SessionIn si = new SessionIn();
	int maxint = 0; 
	try { 
	    maxint = sessIn.getMaxInactiveInterval(); 
	    if(maxint != 0) 
		// Note that XMLBeans library treats NMTOKENS as Strings
		si.setAttributeValue("inactiveInterval", //NOI18N
				     String.valueOf(maxint) );
	} 
	catch(NumberFormatException ne) {} 
	     
	try {
	    date = new Date(sessIn.getLastAccessedTime()); 
	    si.setAttributeValue("lastAccessed", df.format(date)); //NOI18N
	}
	catch(Exception ex) {}

	si.setParam(getSessionAttributes(sessIn)); 
	sess.setSessionIn(si);
    }
    
    private void addSessionOut(SessionData sess, HttpServletRequest request) {  
	
	HttpSession sessOut = null; 
	try { 
	    sessOut = request.getSession(false); 
	}
	catch(Exception ne) {}

	if(sessOut == null) {
	    sess.setAttributeValue("after", "false"); //NOI18N
	    return;
	}

	DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL,
						       DateFormat.SHORT);  

	sess.setAttributeValue("after", "true"); //NOI18N
	Date date = null;
	
	if(sess.getAttributeValue("before").equals("false")) { //NOI18N
	    sess.setAttributeValue("id", sessOut.getId());  //NOI18N
	    date = new Date(sessOut.getCreationTime()); 
	    sess.setAttributeValue("created", df.format(date));  //NOI18N
	}
	
	SessionOut so = new SessionOut();
	int maxint = 0; 
	try { 
	    maxint = sessOut.getMaxInactiveInterval(); 
	    if(maxint != 0) 
		so.setAttributeValue("inactiveInterval", //NOI18N
				     String.valueOf(maxint)); 
	} 
	catch(NumberFormatException ne) {} 
	try {
	    date = new Date(sessOut.getLastAccessedTime()); 
	    so.setAttributeValue("lastAccessed", df.format(date)); //NOI18N
	}
	catch(Exception ex) {}

	Param[] params = getSessionAttributes(sessOut);
	so.setParam(params);
	sess.setSessionOut(so);
    }
    

    private Param[] getSessionAttributes(HttpSession session) {

	Enumeration names = null;
	try {
	    names = session.getAttributeNames(); 
	}
	catch(Exception e) {}
	
	if(names == null || !names.hasMoreElements()) 
	    return new Param[0];

	Vector v = new Vector();
	while (names.hasMoreElements()) {
	    String name = (String)names.nextElement();
	    Object value = session.getAttribute(name);
	    String valueRep = null;            
            try {
                if(value == null) {
                    valueRep = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_attributes"); //NOI18N
                } else {
                    valueRep = value.toString();                    
                    if (valueRep == null) {
                        valueRep = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_toString_null"); //NOI18N
                    }
                }
            } catch (Throwable t) {
                // Ensure that the monitor can continue to run even if there is a
                // serious problem in the application code that it is monitoring
                valueRep = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_toString_exception"); //NOI18N
            }            
	    Param p = new Param();
	    p.setAttributeValue("name", name); //NOI18N
	    p.setAttributeValue("value", valueRep); //NOI18N
	    v.add(p);
	}
	int size = v.size();
	Param[] params = new Param[size]; 
	for(int i=0; i<size; ++i) 
	    params[i] = (Param)v.elementAt(i);
	return params;
    }


    /**
     * Creates an instance of RequestData based on the request
     */
    private void recordRequestData(RequestData rd, 
				   HttpServletRequest request) {

	if(debug)  log(" recordRequestData()"); //NOI18N
	
	// The method variable is used again below
	String method = request.getMethod();
	
	rd.setAttributeValue("uri", request.getRequestURI()); //NOI18N
	rd.setAttributeValue("method", method); //NOI18N

	String protocol = request.getProtocol();
	while(protocol.endsWith("\n")) //NOI18N
	    protocol = protocol.substring(0, protocol.length()-2);
	rd.setAttributeValue("protocol", protocol); //NOI18N

	rd.setAttributeValue("ipaddress", request.getRemoteAddr());//NOI18N

	if(debug)  log("               doing query string"); //NOI18N

	String queryString = request.getQueryString();
	if(queryString == null || queryString.trim().equals("")) { //NOI18N
	    queryString = ""; //NOI18N
	}

	if(debug)  log("Query string is: " + queryString); // NOI18N

	// Parse it the way we do with the errors... 
	rd.setAttributeValue("queryString", queryString); //NOI18N

	//NOI18N
	rd.setAttributeValue("scheme", request.getScheme()); //NOI18N

	if(debug)  log("               doing headers"); //NOI18N
	Headers headers = new Headers();
	headers.setParam(recordHeaders(request));
	rd.setHeaders(headers);

	if(debug)  log("               doing request attributes...in"); //NOI18N
	RequestAttributesIn reqattrin = new RequestAttributesIn();
	reqattrin.setParam(recordRequestAttributes(request));
	rd.setRequestAttributesIn(reqattrin);
    }

    /**
     * Creates an instance of ContextData based on the request
     */
    private void recordContextData(ContextData cd, 
				   HttpServletRequest request) 
    {
	ServletContext context = filterConfig.getServletContext();
	
	if(debug) log(" Getting servlet context props"); //NOI18N
	cd.setAttributeValue("absPath", context.getRealPath("/")); //NOI18N
	cd.setAttributeValue("contextName", //NOI18N
			     context.getServletContextName()); //NOI18N

	if(debug)  log(" context attributes"); //NOI18N
	ContextAttributes ctxtattr = new ContextAttributes();
	ctxtattr.setParam(recordContextAttributes(context));
	cd.setContextAttributes(ctxtattr);

	if(debug) log(" Getting context init parameters"); //NOI18N
	Enumeration e = context.getInitParameterNames(); 
	Vector v = new Vector();

	while (e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    String value = context.getInitParameter(name);
	    Param p = new Param();
	    p.setAttributeValue("name", name); //NOI18N
	    p.setAttributeValue("value", value); //NOI18N
	    v.add(p);
	}

	int size = v.size();
	Param[] params = new Param[size];
	for(int i=0; i< size; ++i) 
	    params[i] = (Param)v.elementAt(i);
	cd.setParam(params);
    }
    


    /**
     * Creates an instance of EngineData based on the request
     */
    private void recordEngineData(EngineData ed, 
				  HttpServletRequest request) 
    {
	ServletContext context = filterConfig.getServletContext();
	ed.setAttributeValue("serverName", request.getServerName()); //NOI18N
	ed.setAttributeValue("serverPort", //NOI18N
			     String.valueOf(request.getServerPort()));
	ed.setAttributeValue("jre", //NOI18N
			     System.getProperty("java.version"));
	ed.setAttributeValue("platform", context.getServerInfo()); //NOI18N
    }
         
    /**
     * Creates an instance of Headers based on the request
     */
    private Param[] recordHeaders(HttpServletRequest request) {

	if(debug) log(" Doing headers");  //NOI18N
	
	Vector v = new Vector(); 
	Vector names = new Vector(); 
	 
	Enumeration e = request.getHeaderNames();
	 
	while (e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    if(debug) log(" Header name: " + name);  //NOI18N
	    if(names.contains(name)) continue;
	    if(debug) log(" Get enumeration of header values");  //NOI18N
	    Enumeration value = request.getHeaders(name);
	    int counter = 0; 
	    while(value.hasMoreElements()) { 
		if(debug) log(" Adding new parameter");  //NOI18N
		v.add(new Param(name, (String)value.nextElement())); 
		++counter;
	    } 
	    if(counter > 1) names.add(name); 
	}
	int size = v.size();
	Param[] params = new Param[size];
	for(int i=0; i< size; ++i) 
	    params[i] = (Param)v.elementAt(i);

	return params;
    }

    /**
     * Adds parameters to the RequestData
     */
    private void addRequestParameters(RequestData rd, 
				      HttpServletRequest request) {
	
	String method = rd.getAttributeValue("method"); //NOI18N

	// If it is a POST request we check if it was URL encoded 
	// Not sure this matters if we record the parameters after the 
	// request has been processed 

	boolean urlencoded = true;
	if(debug)  log("               doing parameters"); //NOI18N
	if(method.equals("POST")) { //NOI18N

	    Headers headers = rd.getHeaders();
	    String urlencodedS = "application/x-www-form-urlencoded"; //NOI18N
	    String typeS = "Content-type"; //NOI18N
 
	    if(headers.containsHeader(typeS) && 
	       !(headers.getHeader(typeS).equalsIgnoreCase(urlencodedS)))
		urlencoded = false; 
	}
	rd.setAttributeValue("urlencoded", //NOI18N
			     String.valueOf(urlencoded)); 


	if(method.equals("GET")) { //NOI18N
	
	    if(debug)  log("GET"); //NOI18N

	    try {
		Enumeration e = request.getParameterNames();
		while(e.hasMoreElements()) {
		    String name = (String)e.nextElement();	    
		    if(debug) log("Parameter name: " + //NOI18N
				  name); 
		    String[] vals = request.getParameterValues(name);
		    for(int i=0; i<vals.length; ++i) 
			rd.addParam(new Param(name, vals[i]));
		}
	    }
	    catch(Exception ex) {
		// The query string was not parameterized. This is
		// legal. If this happens we simply don't record
		// anything here, since the query string is recorded
		// separately. 
		if(debug) log("Non parameterized query string"); //NOI18N
	    }
	    
 
	    if(debug)  log("GET end"); //NOI18N
	}

	else if (method.equals("POST") && urlencoded) { //NOI18N
	    
 	    if(debug)  log("POST"); //NOI18N
	    Enumeration e = null;
	     
	    try {
		e = request.getParameterNames();
		while(e.hasMoreElements()) {
		    String name = (String)e.nextElement();	    
		    if(debug) log("Parameter name: " +  //NOI18N
				  name);
		    String[] vals = request.getParameterValues(name);
		    for(int i=0; i<vals.length; ++i) 
			rd.addParam(new Param(name, vals[i]));
		}
	    }
	    catch(Exception ex) {
		// PENDING: this could also be because the user choose 
		// to parse the parameters themselves. Need to fix
		// this message.
		rd.setAttributeValue("urlencoded", "bad"); //NOI18N
	    }

 	    if(debug)  log("POST"); //NOI18N
	}
    }

    /**
     * Creates a Param[] from a HttpServletRequest
     */
    private Param[] recordRequestAttributes(HttpServletRequest request) {

	if(debug) log("recordRequestAttributes(): start"); //NOI18N
	
	Vector v = new Vector();
	 
	Enumeration e = request.getAttributeNames();
	 
	while (e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    if(debug) log(" name: " + name); //NOI18N
	    Object value = request.getAttribute(name);
            String valueRep = null;
            try {
                valueRep = value.toString();
                if (valueRep == null) {
                    valueRep = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_toString_null"); //NOI18N
                }
            } catch (Throwable t) {
                // Ensure that the monitor can continue to run even if there is a
                // serious problem in the application code that it is monitoring
                valueRep = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_toString_exception"); //NOI18N
            }
	    Param p = new Param();
	    p.setAttributeValue("name", name);  //NOI18N
	    p.setAttributeValue("value", valueRep); //NOI18N
	    v.add(p);
	}
	if(debug) log("Got all request attributes"); //NOI18N

	int size = v.size();

	Param[] params = new Param[size];
	for(int i=0; i< size; ++i) 
	    params[i] = (Param)v.elementAt(i);

	if(debug) log("recordRequestAttributes(): end"); //NOI18N
	return params;
    }


    /**
     * Creates a Param[] of attributes from a Context
     */
    private Param[] recordContextAttributes(ServletContext context) {

	if(debug) log("recordContextAttributes"); //NOI18N
	
	Vector v = new Vector();
	 
	Enumeration e = context.getAttributeNames();
	 
	while (e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    if(debug) log(" name: " + name);  //NOI18N
	    Object value = context.getAttribute(name);
            String valueRep = null;
            try {
                if(value == null) {
                    valueRep = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_attributes"); //NOI18N
                } else if (value.getClass().isArray()) {
                    Object[] valueItems = (Object[])value;
                    StringBuffer sb = new StringBuffer(valueItems.length * 16);
                    if (valueItems.length > 0) sb.append(valueItems[0]);
                    for(int i=1; i < valueItems.length; i++) {
                        sb.append(", "); // NOI18N
                        sb.append(valueItems[i]);
                    }
                    valueRep = sb.toString();
                } else {
                    valueRep = value.toString();
                    if (valueRep == null) {
                        valueRep = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_toString_null"); //NOI18N
                    }
                }
            } catch (Throwable t) {
                // Ensure that the monitor can continue to run even if there is a
                // serious problem in the application code that it is monitoring
                valueRep = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_toString_exception"); //NOI18N
            }
	    Param p = new Param();
	    p.setAttributeValue("name", name);  //NOI18N
	    p.setAttributeValue("value", valueRep);  //NOI18N
	    v.add(p);
	}
	int size = v.size();
	Param[] params = new Param[size];
	for(int i=0; i< size; ++i) 
	    params[i] = (Param)v.elementAt(i);

	return params;
    }


    // PENDING - add own exception 
    private MonitorRequestWrapper getReplayRequest(HttpServletRequest req)
	throws IOException {  

	// Fail if we don't identify the old request
	String status = req.getParameter(REPLAYSTATUS); 
	if (status == null) {
	    String msg = " replay request corrupted"; //NOI18N
	    if(debug) log(msg); 
	    throw new IOException(msg); 
	}


	String id = req.getParameter(REPLAY);
	String portS = req.getParameter(PORT);
	int port = 0; 
	try { 
	    port = Integer.parseInt(portS);
	}
	catch(NumberFormatException nfe) {
	    // We have no port to get the request from, so we return
	    String msg = " Request did not provide a port number"; //NOI18N
	    if(debug) log(msg); 
	    throw new IOException(msg); 
	} 
	
	String ipaddress = req.getRemoteAddr(); 
	RequestData rd = notifyUtil.getRecord(id, status, ipaddress, port);

	if(rd == null) { 
	    String msg = "Failed to get the request";  //NOI18N
	    if(debug) log(msg); 
	    throw new IOException(msg); 
	}

	if(debug) log("Got requestdata as we should");  //NOI18N

	boolean replaceSessionID = false;
	try {
	    String sessionID = req.getParameter(REPLAYSESSION);
	    if(sessionID != null) {
		if(debug) log("User asked for new session " + //NOI18N
			      sessionID);
		replaceSessionID = true;
	    }
	    else if(debug) log("User wants browser's session"); //NOI18N
	}
	catch(NullPointerException npe) {
	    log("NPE when getting " + REPLAYSESSION); //NOI18N
	}


	MonitorRequestWrapper requestWrapper = 
	    new  MonitorRequestWrapper(req);
	if(debug) log("Created wrapper");  //NOI18N
	requestWrapper.populate(rd, replaceSessionID); 
	if(debug) log("Populated wrapper");  //NOI18N
	//requestWrapper.setAttribute(attNameRequest, requestWrapper);
	return requestWrapper; 
    }
    
    
    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
	return (this.filterConfig);
    }


    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {

	this.filterConfig = filterConfig;
	if (filterConfig != null)
	    this.attribute = filterConfig.getInitParameter("attribute"); //NOI18N
	else
	    this.attribute = null;
    }

    /**
     * Destroy method for this filter 
     *
     */
    public void destroy() { 
    }


    /**
     * Init method for this filter 
     *
     */
    public void init(FilterConfig filterConfig) { 

	if(debug) System.out.println("init()");//NOI18N
	
	this.filterConfig = filterConfig;

	notifyUtil = new NotifyUtil();

	boolean noIDE = true;
	String ide = filterConfig.getInitParameter(IDE); 
	if(ide != null && !ide.equals("")) { //NOI18N

	    if(debug) log("trying to start the IDE with " + ide);//NOI18N
	    try { 
		notifyUtil.setIDE(ide); 
		collectData = true;
		if(debug) log("Starting server with ide " + ide); //NOI18N
	    } 
	    catch(MalformedURLException mux) { 
		log("IDE init parameter has an invalid value:"); //NOI18N
		log(ide);
		log("starting anyway"); //NOI18N
	    }
	}
	else { 
	    log("IDE init parameter has an invalid value:"); //NOI18N
	    log(ide);
	    log("starting anyway"); //NOI18N
	}

	String ides = filterConfig.getInitParameter(IDES); 
	if(ides != null && !ides.trim().equals("")) { //NOI18N

	    StringTokenizer st = new StringTokenizer(ides, ","); //NOI18N
	    String name; 
	    while(st.hasMoreTokens()) { 

		name = (String)(st.nextToken());
		try { 
		    notifyUtil.setIDE(name.trim()); 
		    collectData = true;
		    if(debug) log("Starting server with name " + name); //NOI18N
		} 
		catch(MalformedURLException mux) { 
		    log("additional IDE includes an invalid server declaration:"); //NOI18N
		    log(name);
		    log("starting anyway"); //NOI18N
		}
	    }
	}
	if(debug) 
	    log("We're collecting data " + //NOI18N
		String.valueOf(collectData)); 
    }
    
    /**
     * Return a String representation of this object.
     */
    public String toString() {

	if (filterConfig == null) return ("MonitorFilter()"); //NOI18N
	StringBuffer sb = new StringBuffer("MonitorFilter("); //NOI18N
	sb.append(filterConfig);
	sb.append(")"); //NOI18N
	return (sb.toString());

    }

    /**
     * Get the value of collectData.
     * @return Value of collectData.
     */
    public static boolean getCollectData() {
	return collectData;
    }
    
    /**
     * Set the value of collectData.
     * @param v  Value to assign to collectData.
     */
    public static void setCollectData(boolean  v) {
	collectData = v;
    }

    private void rethrow(Throwable t) throws IOException,
					     ServletException { 

	if(debug) log(" rethrow(" + t.getMessage() + ")"); //NOI18N
	if(t instanceof StackOverflowError) { 
	    String message =
		ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_overflow"); 
	    filterConfig.getServletContext().log(message); 
	    System.out.println(message); 
	    throw new ServletException(message, t);
	}
	if(t instanceof RuntimeException) throw (RuntimeException)t;
	if(t instanceof ServletException) throw (ServletException)t;
	if(t instanceof IOException) throw (IOException)t;
	else { 
	    String message =
		ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Rethrow"); 
	    throw new ServletException(message, t);
	}
    }

    public void log(String msg) {
	//filterConfig.getServletContext().log("MonitorFilter::" + msg); //NOI18N
	System.out.println("MonitorFilter::" + msg);//NOI18N
    }

    public void log(Throwable t) {
	log(getStackTrace(t)); 
    }
}


