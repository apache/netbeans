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

import java.io.*;
import java.text.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.*;

import org.netbeans.modules.web.monitor.data.MonitorData;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

/*
 * Send the xml file for a transaction back to the replay filter/interceptor.
 */

public class ReplaySendXMLServlet extends HttpServlet {


    private static FileObject currDir = null;
    private static FileObject saveDir = null;
    private static FileObject replayDir = null;
    private final static boolean debug = false;
     

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



