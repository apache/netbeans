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

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.SwingUtilities;

import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.web.monitor.data.Constants;

/*
 * Put a transaction
 */
  
public class PutTransaction extends HttpServlet {

    private static FileObject currDir = null;
    private static boolean debug = false;
     
    private ServletConfig servletConfig = null;

    public void doPost(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {
	 
	if(debug) log("doPost"); //NOI18N
	if(currDir == null) {
	    try { 
		currDir = Controller.getCurrDir();
	    }
	    catch(FileNotFoundException ex) {
		// PENDING report this error properly
		if(debug) log("Couldn't write the transaction data");  //NOI18N
		return;
	    }
	}

	// As soon as you get the parameters, you've gotten an input
	// string for this. Don't do that. 

	String id = req.getQueryString(); 
	if(id == null || id.length() == 0) { 
	    if(debug) log("Bad request, exiting..."); //NOI18N
	    return; 
	}

	id = id.substring(0, id.indexOf(Constants.Punctuation.itemSep));

	if(debug) log(" Trying to add the transaction"); //NOI18N
	FileObject fo = null;
	 
	try {
	    if(debug) log(" Before creating the file"); //NOI18N
	    fo = currDir.createData(id, "xml"); //NOI18N
	    if(debug) log(" After creating the file"); //NOI18N
	}
	catch(IOException ioex) { 
	    if(debug) log(" Could not create the file, exiting..."); 
	    return;
	} 
	FileLock lock = null;
	try { 
	    lock = fo.lock();
	    if(debug) log(" Got the lock"); //NOI18N
	} 
	catch(FileAlreadyLockedException falex) { 
	    if(debug) log(" Couldn't get a file lock, exiting..."); //NOI18N
	    return; 
	} 

	boolean success = false;
	try {
	    PrintWriter fout = new PrintWriter(fo.getOutputStream(lock));
        try {
            InputStreamReader isr = new InputStreamReader(req.getInputStream());
            try {
                char[] charBuf = new char[4096];
                int numChars;

                while((numChars = isr.read(charBuf, 0, 4096)) != -1) {
                    fout.write(charBuf, 0, numChars);
                }
            } finally {
                isr.close();
            }
        } finally {
            fout.close();
        }
	    success = true;
 	    if(debug) log("...success"); //NOI18N
	}
	catch(IOException ioex) {
	    if (debug) { 
		log("Failed to read/write the record:"); 
		log(ioex);
	    }
	}
	finally {
	    lock.releaseLock(); 

	    try { 
            res.setContentType("text/plain");  //NOI18N	    
            PrintWriter out = res.getWriter();
            try {
                out.println(Constants.Comm.ACK); 
            } finally {
                out.close();
            }
	    } catch(Exception ex) {
            // It doesn't actually matter if this goes wrong
	    }
	}
        final boolean success2 = success;
        final String id2 = id;
        // window system code must be run in AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                if(success2) {
		    MonitorAction.addTransaction(id2); 
		}
	    }});
    }

    // PENDING - deal better with this
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	if(debug) log("doGet");  //NOI18N

	PrintWriter out = res.getWriter();
	try { 
	    //out.println(id); 
	    out.println("Shouldn't use GET for this!");  //NOI18N
	}
	catch (Exception e) { 
	    if(debug) log(e.getMessage());
	}
	try { out.close(); } catch(Exception ex) {}
    }


    /**
     * Init method for this filter 
     *
     */
    public void init(ServletConfig servletConfig) { 

	this.servletConfig = servletConfig;
	if(debug) log("init");  //NOI18N
    }
    
    public void log(String msg) {
	System.out.println("PutTransaction::" + msg); //NOI18N
	
    }

    public void log(Throwable t) {
	log(getStackTrace(t));
    }


    public static String getStackTrace(Throwable t) {

	String stackTrace = null;
	    
	try {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    t.printStackTrace(pw);
	    pw.close();
	    sw.close();
	    stackTrace = sw.getBuffer().toString();
	}
	catch(Exception ex) {}
	return stackTrace;
    }

} //PutTransaction.java



