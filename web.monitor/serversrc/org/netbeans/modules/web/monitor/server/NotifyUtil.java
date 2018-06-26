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

package org.netbeans.modules.web.monitor.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

import org.netbeans.modules.web.monitor.data.*;


/**
 * NotifyUtil.java
 *
 *
 * Created: Mon Aug 27 17:21:03 2001
 *
 * @author Ana von Klopp
 * @version
 */

class NotifyUtil  {

    private String ideServer = null; 
    private Vector otherIDEs = null;
    boolean errorPrinted = false;
    private final static String putServlet =
	    "/servlet/org.netbeans.modules.web.monitor.client.PutTransaction?"; //NOI18N  

    private final static String replayServlet = 
	"/servlet/org.netbeans.modules.web.monitor.client.ReplaySendXMLServlet";  //NOI18N


    private static final boolean debug = false;
     
    NotifyUtil() {
	otherIDEs = new Vector(); 
	if(debug) log("NotifyUtil::constructor at end");  //NOI18N
    }

    /** 
     * Sets the URL of any IDEs that should receive monitor data. 
     * We assume that the first IDE set in this way is the primary
     * one. 
     *
     * @param name The host name or IP address of the host on which
     *             the IDE is running. 
     * @param portS The port on which the IDE's internal HTTP server
     *              is running. 
     */
    void setIDE(String name, String portS) throws MalformedURLException { 

	int port = 0; 
	try {
	    port = Integer.parseInt(portS);
	}
	catch(NumberFormatException nfe) {
	    throw new MalformedURLException("Port number is not an integer"); //NOI18N
	}

	try { 
	    if(ideServer == null) 
		ideServer = new URL("http", name, port, putServlet).toString(); //NOI18N
	    else  
		otherIDEs.add(new URL("http", name, port, putServlet).toString()); //NOI18N 
	
	}
	catch(MalformedURLException mux) {
	    throw mux;
	}
    }
		
    /** 
     * Sets the URL of any IDEs that should receive monitor data. 
     * We assume that the first IDE set in this way is the primary
     * one. 
     *
     * @param server A reference to the server on which the IDE is
     *               running in host:port format. 
     */
    void setIDE(String server) throws MalformedURLException { 
	
	String host = server.substring(0, server.indexOf(":"));  //NOI18N
	String port = server.substring(server.indexOf(":") + 1); //NOI18N
	setIDE(host, port); 
	if(debug){ 
	    log("host: " + host); //NOI18N
	    log("port: " + port); //NOI18N
	}
    }
		
    void sendRecord(MonitorData monData, String queryStr) {
	
	if(debug) log("NotifyUtil::notifyServer");  //NOI18N

	if(ideServer != null) { 
	    String urlStr = ideServer.concat(queryStr);
	    if(debug) log("NotifyUtil: url is " + urlStr);  //NOI18N
	    sendRecord(urlStr, monData); 
	} 

	if(otherIDEs.isEmpty()) return; 

	Enumeration ides = otherIDEs.elements();
	while(ides.hasMoreElements()) {

	    String base = (String)ides.nextElement();

	    if(debug) 
		log("NotifyUtil: url is " + base.concat(queryStr)); //NOI18N
	    if(!sendRecord(base.concat(queryStr), monData))
		otherIDEs.remove(base); 
	}
	return; 
    }
    
    private boolean sendRecord(String urlS, MonitorData monData) { 

	boolean status = false; 

	URL url = null;
	try { 
	    url = new URL(urlS); 
	}
	catch(MalformedURLException mux) { 
	    return status; 
	} 
	RecordSender recordSender = new RecordSender(url, monData);
	recordSender.start();
	try { 
	    recordSender.join(3000); 
	    status = recordSender.getStatus(); 
	} 
	catch(InterruptedException ix) {} 
	recordSender = null; 
	return status; 
    } 


    /**
     * 
     * @param id
     * @param status
     * @param host
     */
    
    RequestData getRecord(String id, String status, String host, int port) {

	RequestData rd = null;

	// This is the default case - the server port was already known

	StringBuffer uriBuf = new StringBuffer(replayServlet); //NOI18N
	uriBuf.append("?status=");  //NOI18N
	uriBuf.append(status);
	uriBuf.append("&id=");  //NOI18N
	uriBuf.append(id);

	URL url = null;
	try { 
	    url = new URL("http", host, port, uriBuf.toString()); //NOI18N
	}
	catch(MalformedURLException mux) {
	    // This should not happen
	}

	if(debug) log(url.toString());

	RecordFetcher recordFetcher = new RecordFetcher(url); 
	recordFetcher.start();
	try { 
	    recordFetcher.join(3000); 
	    if(recordFetcher.getStatus()) 
		return recordFetcher.getMonitorData().getRequestData(); 
	    else
		return null; 
	} 
	catch(InterruptedException ix) {} 
	recordFetcher = null; 
	return null;
    } 
    
    /**
     * This thread is used to send records to the IDE's internal
     * server. 
     */
    class RecordSender extends Thread {

	URL url = null;
	boolean gotAck = false;
	boolean triedToRestart = false;
	URLConnection conn = null;
	MonitorData monData = null;

	RecordSender(URL url, MonitorData monData) {
	    super("HTTP Monitor, sends data to IDE"); //NOI18N
	    this.monData = monData;
	    this.url = url;
	}

	public void run() {
	    if(debug) 
		log("NotifyUtil: connecting to " +  //NOI18N
			   url.toString()); 

	    PrintWriter out = null;
	    BufferedReader in = null;
	    
	    try {
		if(debug) log("\tOpening connection");  //NOI18N
		conn = url.openConnection();
		conn.setRequestProperty("\tContent-type","text/xml");  //NOI18N
		conn.setDoOutput(true);
                try {
                    out = new PrintWriter(conn.getOutputStream());
                } catch (IOException e) {
                    if (!NotifyUtil.this.errorPrinted) {
                        String msg = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_httpserver_problem"); // NOI18N
                        System.out.println(msg);
                        NotifyUtil.this.errorPrinted = true;
                    }
                    return;
                }
		if(debug) log("\tGot output stream");  //NOI18N
		monData.write(out);
		out.flush();
		
		if(debug) {
		    String file = 
			monData.createTempFile("notifyutil.xml"); // NOI18N
		    log("Wrote replay data to " + file); // NOI18N
		}

                try {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } catch (IOException e) {
                    if (!NotifyUtil.this.errorPrinted) {
                        String msg = ResourceBundle.getBundle("org.netbeans.modules.web.monitor.server.Bundle").getString("MON_Warning_disabled_monitor"); // NOI18N
                        System.out.println(msg);
                        NotifyUtil.this.errorPrinted = true;
                    }
                    return;
                }

		String inputLine = null;

		while ((inputLine = in.readLine()) != null) {

		    if(inputLine.equals(Constants.Comm.ACK)) {
			if(debug) log("\tGot ack"); //NOI18N
			gotAck = true;
			break;
		    }
		}
	    }
	    catch(IOException ioe) {
		log(ioe);
		
	    }
	    catch(NullPointerException npe) {
		log(npe);
	    }
	    catch(Throwable t) {
		log(t); 
	    }
	    finally {
		try {
		    in.close();
		}
		catch(Throwable t) {
		}
		
		try {
		    out.close();
		}
		catch(Throwable t) {
		}
	    }	    
	}

	boolean getStatus() {
	    return gotAck;
	}
    }


    class RecordFetcher extends Thread {

	URL url = null;
	boolean gotAck = false;
	String trace = null;

	MonitorData monData = null;

	RecordFetcher(URL url) {
	    super("HTTP Monitor, retrieves data from IDE"); //NOI18N
	    this.url = url;
	}

	MonitorData getMonitorData() {
	    return monData;
	}

	public void run() {

	    InputStream in = null;
	    InputStreamReader urlIn = null;

	    try {
		in = url.openStream();
		urlIn = new InputStreamReader(in); 
		monData = MonitorData.createGraph(urlIn);
		gotAck = true;
	    }
	    catch(Throwable t) { 
		gotAck = false;
		trace = Logger.getStackTrace(t);
	    }
	    finally {
		try {
		    urlIn.close();
		} 
		catch (Exception ex) {}
		try {
		    in.close();
		} 
		catch (Exception ex) {}
	    }
	}
	
	boolean getStatus() {
	    return gotAck;
	}

    }

    void log(Throwable t) { 
	log(Logger.getStackTrace(t));
    }

    void log(String msg) { 
	System.out.println("NotifyUtil::" + msg); //NOI18N
    }
} // NotifyUtil
