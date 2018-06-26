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

package org.netbeans.modules.web.monitor.client;

import java.util.*;
import javax.servlet.http.HttpUtils;
import org.netbeans.modules.web.monitor.data.*;


/**
 * Util.java
 *
 * For the next proper release of the monitor module, these methods
 * should move in with their respective data objects. I can't do that
 * for now because I would break compatibility with tomcat-monitor.jar
 * which include copies of the data files.
 *
 * Created: Thu Aug 30 17:43:28 2001
 *
 * @author Ana von Klopp
 * @version
 */

public class Util  {

    private final static boolean debug = false;

    public Util() {}

    /**
     * We use this method to compose a query string from the
     * parameters instead of using the query string we recorded.  This
     * is used by edit/replay, and also as a workaround for regular
     * replay as getParameters() seems to be better implemented to
     * deal with multibyte than getQueryString()...  */

    public static void composeQueryString(RequestData rd) { 
	
	if(debug) System.out.println("Doing query string"); //NOI18N
	
	if(rd.sizeParam() == 0) return;
	
	Param[] params = rd.getParam();
	StringBuffer buf = new StringBuffer(512);
	String name, value;
	 
	for(int i=0; i < params.length; i++) {

	    try {
		name = params[i].getName().trim();
		if(debug) System.out.println("name: " + name); //NOI18N
		value = params[i].getValue(); 
		if(debug) System.out.println("value: " + value); //NOI18N
	    }
	    catch(Exception ex) { 
		continue;
	    }
	    if(name.equals("")) continue;  //NOI18N

	    if (value != null) value = value.trim();
	    else value = ""; //NOI18N
	    
	    if(i>0) buf.append('&'); // NOI18N
	    buf.append(name);
	    buf.append('=');  //NOI18N
	    buf.append(value);
	}
	rd.setAttributeValue("queryString", buf.toString());  //NOI18N
	rd.setParam(new Param[0]);
	
	if (debug) 
	    System.out.println("EditPanel::composedQueryString: [" +  //NOI18N
			       buf.toString() + "]");  //NOI18N
    }

    static boolean removeParametersFromQuery(RequestData rd) {
		 
	// Data wasn't parameterized
	if(rd.sizeParam() == 0) return false;
	
	String queryString =
	    rd.getAttributeValue("queryString");  //NOI18N

	// MULTIBYTE - I think this isn't working... 
	Hashtable ht = null;
	try {
	    ht = javax.servlet.http.HttpUtils.parseQueryString(queryString);
	}
	catch(IllegalArgumentException iae) {
	    // do nothing, that's OK
	    return false;
	}
	if(ht == null || ht.isEmpty()) return false;
	
	Enumeration e = ht.keys();

	while(e.hasMoreElements()) {
	    String name = (String)e.nextElement();
	    try {
		String[] value = (String[])(ht.get(name));
		for(int i=0; i<value.length; ++i) {
		    if(debug) System.out.println("Removing " + //NOI18N
						 name + " " + //NOI18N
						 value);
		    Param p = findParam(rd.getParam(), name, value[i]);
		    rd.removeParam(p);
		}
	    }
	    catch(Exception ex) {
	    }
	}
	return true;
    }

    static void addParametersToQuery(RequestData rd) {

	Hashtable ht = null;
	String queryString = rd.getAttributeValue("queryString");  //NOI18N
	try {
	    ht = javax.servlet.http.HttpUtils.parseQueryString(queryString);
	}
	catch(Exception ex) { }
			    
	if(ht != null && ht.size() > 0) {
	    Enumeration e = ht.keys();
	    while(e.hasMoreElements()) {
		String name = (String)e.nextElement();
		String[] value = (String[])(ht.get(name));
		for(int i=0; i<value.length; ++i) {
		    if(debug) 
			System.out.println("Adding " + name +  //NOI18N
					   " " +  value); //NOI18N
		    Param p = new Param(name, value[i]);
		    rd.addParam(p);
		}
	    }
	}
    }

    /**
     * The session cookie and the actual session that was used might
     * be out of synch. This method makes sure that if the request
     * contained an incoming session cookie and a session was used 
     * then the IDs will match (the session ID will be the one from
     * the session, not the one from the cookie). 
     */
      
    public static void setSessionCookieHeader(MonitorData md) {
	
	// First we check *whether* we have a session cookie at
	// all... 
	Headers headers = md.getRequestData().getHeaders();
	int numParams = headers.sizeParam();
	
	if(numParams == 0) return;

	boolean sessionCookie = false;
	Param[] params = headers.getParam(); 
	StringBuffer cookiesOut = new StringBuffer("");  //NOI18N
	 
	for(int i=0; i<numParams; ++i) {

	    Param p = params[i];
	    
	    if(p.getAttributeValue("name").equals("Cookie")) {  //NOI18N
		
		String cookies = p.getAttributeValue("value");  //NOI18N
		
		StringTokenizer st = new StringTokenizer(cookies, ";");  //NOI18N
		
		while(st.hasMoreTokens()) {
		    String cookie = st.nextToken();
		    if(debug) System.out.println("Now doing "+ //NOI18N
						 cookie);
		    if(cookie.startsWith("JSESSIONID")) {  //NOI18N
			sessionCookie = true;
			if(debug) System.out.println("Found session cookie"); //NOI18N
			if(debug) System.out.println("Getting session ID");  //NOI18N
			String sessionID = null; 
			try {
			    sessionID = 
				md.getSessionData().getAttributeValue("id");  //NOI18N
			    if(debug) System.out.println("..." + sessionID);  //NOI18N
			}
			catch(Exception ex) {}
			if(debug) System.out.println("Setting session cookie");  //NOI18N
			cookiesOut.append("JSESSIONID=");  //NOI18N
			cookiesOut.append(sessionID);
			cookiesOut.append(";");  //NOI18N
		    }
		    else {
			if(debug) System.out.println("Appending " + cookie);   //NOI18N
			cookiesOut.append(cookie);
			cookiesOut.append(";");  //NOI18N
		    }
		}
		if(debug) 
		    System.out.println("Cookie string: " +  //NOI18N
				       cookiesOut.toString()); 
		if(sessionCookie) {
		    if(debug) System.out.println("Found session cookie");  //NOI18N
		    p.setAttributeValue("value",  //NOI18N
					cookiesOut.toString());
		}
	    }
	}
    }

    /**
     * find the param with the given name and value from the list.
     */
    public static Param findParam(Param [] myParams, String name,
				  String value) { 
	for (int i=0; i < myParams.length; i++) {
	    Param param = myParams[i];
	    if (name.equals(param.getName()) &&
		value.equals(param.getValue()) ) {
		return param;
	    }
	}
	return null;
    }
}
