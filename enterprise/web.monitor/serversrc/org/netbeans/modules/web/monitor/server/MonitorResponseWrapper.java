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
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * MonitorResponseWrapper.java
 *
 *
 * Created: Tue Feb 27 18:32:32 2001
 *
 * @author Ana von Klopp
 * @version
 */
public class MonitorResponseWrapper extends HttpServletResponseWrapper {

    private Vector cookies = null;
    private int status;
    private boolean cookieSetOnInclude = false;

    private HttpServletResponse response = null;
    private HttpServletRequest request = null;

    private static boolean debug = false;

    /**
     * The constructor needs to have access both to the response
     * object that it wraps and to the request. The latter is needed
     * to determine whether the resource which is currently accessing
     * the response was dispatched to as an include or not
     *
     * @param response The HTTPServletResponse object that this object
     * wraps. 
     * $param request The HttpServletRequest object that is processed
     * in synch with the response. 
     *
     */
    public MonitorResponseWrapper(HttpServletResponse response, 
				  HttpServletRequest request) {
	super(response); 
	this.response = (HttpServletResponse)getResponse();
	this.request = request;
	cookies = new Vector();
    }

    /**
     * Returns the outgoing cookies that were added to the response as
     * it was processed by the servlets and JSPs. 
     * 
     * @return An enumeration of cookies
     *
     */
    public Enumeration getCookies() { 
	return cookies.elements();
    }

    /**
     * Returns the HTTP status of the response 
     * 
     * @return An integer representing the HTTP status
     *
     */
    public int getStatus() { 
	return status; 
    }


    /**
     * Wraps the addCookie method on the response. Since the Servlet
     * APIs does not allow us to retrieve the outgoing cookies, the
     * wrapper itself maintains a duplicate list which can be accessed
     * with the getCookies method. 
     * 
     * @param A cookie
     *
     */
    public void addCookie (Cookie cookie) {
	String str = (String)request.getAttribute("javax.servlet.include.request_uri"); //NOI18N
	if(str == null) { 
	    cookies.add(cookie);
	    response.addCookie(cookie); 
	}
	else { 
	    // An included resource tried to set a cookie, which is
	    // illegal but swallowed by the reference implementation
	    cookieSetOnInclude = true;
	}
    }
    
    /**
     * Wraps the sendError method on the response. The HTTP status is
     * not accessible through the Servlet APIs, so the wrapper
     * maintains a copy of the value which can be accessed with the
     * getStatus method.
     * 
     * @param status an integer representing the HTTP status
     * @param detail a message explaining the error
     *
     */
    public void sendError (int status, String detail) throws
	IOException, IllegalStateException {
	this.status = status;
	response.sendError (status, detail); 
    }

    /**
     * Wraps the sendError method on the response. The HTTP status is
     * not accessible through the Servlet APIs, so the wrapper
     * maintains a copy of the value which can be accessed with the
     * getStatus method.
     * 
     * @param status an integer representing the HTTP status
     *
     */
    public void sendError (int status)  throws IOException,
	IllegalStateException {
	this.status = status;
	response.sendError (status, null); 
    }

    /**
     * Wraps the setStatus method on the response. The HTTP status is
     * not accessible through the Servlet APIs, so the wrapper
     * maintains a copy of the value which can be accessed with the
     * getStatus method.
     * 
     * @param status an integer representing the HTTP status
     *
     */
    public void setStatus(int code) {
	this.status = code;
	response.setStatus(code); 
    }

    /**
     * Wraps the setStatus method on the response. The HTTP status is
     * not accessible through the Servlet APIs, so the wrapper
     * maintains a copy of the value which can be accessed with the
     * getStatus method.
     * 
     * @param status an integer representing the HTTP status
     * @param msg a message explaining the status
     *
     */
    public void setStatus (int code, String msg) {
        this.status = code;
        try {
            Method m = HttpServletResponse.class.getMethod("setStatus", new Class<?>[]{int.class, String.class});
            m.invoke(this, code, msg);
        } catch (ReflectiveOperationException ex) {
            // The reflective call is needed to be able to recompile this class
            // with modified imports to work in jakarta servlet. When this
            // method is invoked, it must me resolveable, else the Caller would
            // not be able to be loaded. The method is public, so permissions
            // should not be a problem.
        }
    }

    /**
     * Logger method so that this object can log messages to the log
     * file for the servlet context. Note that this will fail if there
     * is no HTTP session. 
     * 
     * @param msg The message to log. 
     */
    private void log(String msg) { 
	try { 
	    request.getSession(false).getServletContext().log("MonitorResponseWrapper::" + msg); //NOI18N
	}
	catch(Throwable t) {
	    if(debug) t.printStackTrace();
	}
    }

    /**
     * This method can be used by a tool that warns a developer that
     * an included resource tried to do something it is not allowed to
     * do. The Monitor client could show this.
     *
     * @return true if an included resource attempted to set a cookie
     *
     */
    public boolean cookieSetOnInclude() { 
	return cookieSetOnInclude;
    }
} // MonitorResponseWrapper
