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

package org.netbeans.modules.web.monitor.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

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
    private static final String putServlet =
	    "/servlet/org.netbeans.modules.web.monitor.client.PutTransaction?"; //NOI18N  

    private static final String replayServlet = 
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
        try {
            if (debug) {
                log("NotifyUtil::notifyServer");  //NOI18N
            }

            if (ideServer != null) {
                String urlStr = ideServer.concat(URLEncoder.encode(queryStr, "UTF-8")); //NOI18N
                if (debug) {
                    log("NotifyUtil: url is " + urlStr);  //NOI18N
                }
                sendRecord(urlStr, monData);
            }

            if (otherIDEs.isEmpty()) {
                return;
            }

            Enumeration ides = otherIDEs.elements();
            while (ides.hasMoreElements()) {

                String base = (String) ides.nextElement();
                String urlStr = base.concat(URLEncoder.encode(queryStr, "UTF-8")); //NOI18N

                if (debug) {
                    log("NotifyUtil: url is " + urlStr); //NOI18N
                }
                if (!sendRecord(urlStr, monData)) {
                    otherIDEs.remove(base);
                }
            }
            return;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex); // If UTF-8 is not supported we are screwed
        }
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


	URL url = null;
	try {
            StringBuilder uriBuf = new StringBuilder(replayServlet); //NOI18N
            uriBuf.append("?status=");  //NOI18N
            uriBuf.append(URLEncoder.encode(status, "UTF-8")); //NOI18N
            uriBuf.append("&id=");  //NOI18N
            uriBuf.append(URLEncoder.encode(id, "UTF-8")); //NOI18N
	    url = new URL("http", host, port, uriBuf.toString()); //NOI18N
	}
	catch(MalformedURLException | UnsupportedEncodingException mux) {
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
		conn.setRequestProperty("Content-type","text/xml");  //NOI18N
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
