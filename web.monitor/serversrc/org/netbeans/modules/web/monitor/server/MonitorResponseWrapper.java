/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/**
 * MonitorResponseWrapper.java
 *
 *
 * Created: Tue Feb 27 18:32:32 2001
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;

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
	response.setStatus(code, msg); 
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
