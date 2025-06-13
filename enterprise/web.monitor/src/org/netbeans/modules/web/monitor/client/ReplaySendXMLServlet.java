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

package org.netbeans.modules.web.monitor.client;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.modules.web.monitor.data.MonitorData;
import org.openide.filesystems.FileObject;

/*
 * Send the xml file for a transaction back to the replay filter/interceptor.
 */

public class ReplaySendXMLServlet extends HttpServlet {


    private static FileObject currDir = null;
    private static FileObject saveDir = null;
    private static FileObject replayDir = null;
    private static final boolean debug = false;
     

    //
    // The action is really going to happen in the GET??
    //
    public void doPost(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	if(debug) 
	    System.out.println("\n\nReplaySendXMLServlet:  DoPost.\n\n"); //NOI18N
	PrintWriter out = res.getWriter();
	try { 
	    out.println("Shouldn't use POST for this!");  //NOI18N
	}
	catch (Exception e) { 
	}
	try { out.close(); } catch(Exception ex) {}
    }

    // Return the desired transaction file in the response.
    //
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	if(debug) System.out.println("\n\nReplaySendXMLServlet:  DoGet.\n\n"); //NOI18N

	String status = null;
	String id = null;
	
	try {
	    status = req.getParameter("status");  //NOI18N
	    id = req.getParameter("id");  //NOI18N
	    if(debug) 
		System.out.println("\n\nReplaySendXMLServlet: id=" +  //NOI18N
				   id + " ,status=" + status);  //NOI18N
	}
	catch(Exception ex) {
	    // PENDING - deal 
	    return;
	}

	Controller controller = MonitorAction.getController();
	MonitorData md = controller.retrieveMonitorData(id, status);
	if(md != null) {

	    Util.setSessionCookieHeader(md);
	    String method =
		md.getRequestData().getAttributeValue("method");  //NOI18N
	    
	    if(method.equals("POST")) {  //NOI18N
		Util.removeParametersFromQuery(md.getRequestData());
	    }
	    else if(method.equals("GET")) {  //NOI18N
		Util.composeQueryString(md.getRequestData());
	    }

	    res.addHeader("Content-type", //NOI18N
			  "text/plain;charset=\"UTF-8\"");  //NOI18N

	    PrintWriter out = res.getWriter();
	    try {
		md.write(out);
	    }
	    catch(NullPointerException npe) {
		if(debug) npe.printStackTrace();
	    }
	    catch(IOException ioe) {
		if(debug) ioe.printStackTrace();
	    }
	    catch(Throwable t) {
		if(debug) t.printStackTrace();
	    }
	    finally {
		// Do we need to close out? 
		try {
		    out.close();
		}
		catch(Exception ex) {
		}
	    }
	}
	if(debug) {
	    try {
		StringBuffer buf = new StringBuffer
		    (System.getProperty("java.io.tmpdir")); // NOI18N
		buf.append(System.getProperty("file.separator")); // NOI18N
		buf.append("replay-servlet.xml"); // NOI18N
		File file = new File(buf.toString()); 
		log("Writing replay data to " // NOI18N
		    + file.getAbsolutePath()); 		
		FileOutputStream fout = new FileOutputStream(file);
		PrintWriter pw2 = new PrintWriter(fout);
		md.write(pw2);
		pw2.close();
		fout.close();

	    }
	    catch(Throwable t) {
	    }   
	}
	
	if(debug) 
	    System.out.println("ReplaySendXMLServlet doGet exiting...");  //NOI18N
    }
} //ReplaySendXMLServlet.java



