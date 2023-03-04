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

package org.netbeans.modules.web.monitor.data;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Vector;

import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BeanComparator;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.GraphManager;
import org.netbeans.modules.schema2beans.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


public class MonitorData extends BaseBean implements DataRecord {

    private static final boolean debug = false;

    static Vector<BeanComparator> comparators = new Vector<>();

    public static final String CLIENTDATA = "ClientData";   // NOI18N
    public static final String SESSIONDATA = "SessionData"; // NOI18N
    public static final String COOKIESDATA = "CookiesData"; // NOI18N
    public static final String REQUESTDATA = "RequestData"; // NOI18N
    public static final String SERVLETDATA = "ServletData"; // NOI18N
    public static final String CONTEXTDATA = "ContextData"; // NOI18N
    public static final String ENGINEDATA = "EngineData"; // NOI18N
    public static final String MONITORDATA = "MonitorData"; // NOI18N
    public static final String DISPATCHES = "Dispatches"; // NOI18N

    public MonitorData() {
	this(null, Common.USE_DEFAULT_VALUES);
    }

    public MonitorData(Node doc, int options) {
	this(Common.NO_DEFAULT_VALUES);
	if (doc == null)
	    {
		doc = GraphManager.createRootElementNode(MONITORDATA);
		if (doc == null)
		    throw new RuntimeException("failed to create a new DOM root!"); // NOI18N
	    }
	Node n = GraphManager.getElementNode(MONITORDATA, doc);
	if (n == null)
	    throw new RuntimeException("doc root not found in the DOM graph!"); // NOI18N

	this.graphManager.setXmlDocument(doc);

	// Entry point of the createBeans() recursive calls
	this.createBean(n, this.graphManager());
	this.initialize(options);
    }
    
    public MonitorData(int options) {
	super(MonitorData.comparators, new Version(1, 0, 6));
	// The graph manager is allocated in the bean root
	this.graphManager = new GraphManager(this);

	this.createRoot(MONITORDATA, MONITORDATA,
			Common.TYPE_1 | Common.TYPE_BEAN, MonitorData.class);

	// Properties (see root bean comments for the bean graph)
	this.createProperty("ClientData", CLIENTDATA,  // NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    ClientData.class);
	this.createAttribute(CLIENTDATA, "protocol", "Protocol", // NOI18N 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(CLIENTDATA, "remoteAddress", "RemoteAddress", // NOI18N 
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(CLIENTDATA, "software", "Software",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "locale", "Locale",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "formatsAccepted", "FormatsAccepted",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "encodingsAccepted", "EncodingsAccepted",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CLIENTDATA, "charsetsAccepted", "CharsetsAccepted",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("SessionData", SESSIONDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    SessionData.class);
	this.createAttribute(SESSIONDATA, "before", "Before",  // NOI18N
			     AttrProp.ENUM | AttrProp.REQUIRED,
			     new String[] {
				 "false", // NOI18N
				 "true" // NOI18N
			     }, "false"); // NOI18N
	this.createAttribute(SESSIONDATA, "after", "After",  // NOI18N
			     AttrProp.ENUM | AttrProp.REQUIRED,
			     new String[] {
				 "false", // NOI18N
				 "true" // NOI18N
			     }, "false"); // NOI18N
	this.createAttribute(SESSIONDATA, "id", "Id",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SESSIONDATA, "created", "Created",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createProperty("CookiesData", COOKIESDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    CookiesData.class);

	// PENDING - I think I need to move this to the request data
	// class to make it an independent class that can be handled
	// on its own (if I want to pass less data about). 
	this.createProperty("RequestData", REQUESTDATA,  // NOI18N
			    Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    RequestData.class);
	this.createAttribute(REQUESTDATA, "uri", "Uri",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(REQUESTDATA, "method", "Method",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute(REQUESTDATA, "urlencoded", "Urlencoded",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "queryString", "QueryString",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "replace", "Replace",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "protocol", "Protocol",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "ipaddress", "Ipaddress",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "scheme", "Scheme",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(REQUESTDATA, "status", "Status",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createProperty("ServletData", SERVLETDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    ServletData.class);
	this.createAttribute(SERVLETDATA, "name", "Name",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "className", "ClassName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "packageName", "PackageName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "servletInfo", "ServletInfo",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "relPath", "RelPath",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "transPath", "TransPath",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "contextName", "ContextName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "absPath", "AbsPath",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "jre", "Jre",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "platform", "Platform",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "serverPort", "ServerPort",  // NOI18N
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(SERVLETDATA, "serverName", "ServerName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createAttribute(SERVLETDATA, "collected", "Collected",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createProperty("ContextData", CONTEXTDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    ContextData.class);
	this.createAttribute(CONTEXTDATA, "contextName", "ContextName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(CONTEXTDATA, "absPath", "AbsPath",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createProperty("EngineData", ENGINEDATA,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    EngineData.class);

	this.createAttribute(ENGINEDATA, "jre", "Jre",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(ENGINEDATA, "platform", "Platform",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(ENGINEDATA, "serverPort", "ServerPort",  // NOI18N
			     AttrProp.NMTOKEN | AttrProp.IMPLIED,
			     null, null);
	this.createAttribute(ENGINEDATA, "serverName", "ServerName",  // NOI18N
			     AttrProp.CDATA | AttrProp.IMPLIED,
			     null, null);

	this.createAttribute("resource", "Resource",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute("timestamp", "Timestamp",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute("id", "Id",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);
	this.createAttribute("method", "Metod",  // NOI18N
			     AttrProp.CDATA | AttrProp.REQUIRED,
			     null, null);

	this.createProperty("Dispatches", DISPATCHES,  // NOI18N
			    Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			    Dispatches.class);


	this.initialize(options);
    }

    // Setting the default values of the properties
    void initialize(int options) {
    }

    // This attribute is mandatory
    public void setClientData(ClientData value) {
	this.setValue(CLIENTDATA, value);
    }

    //
    public ClientData getClientData() {
	return (ClientData)this.getValue(CLIENTDATA);
    }

    // This attribute is optional
    public void setSessionData(SessionData value) {
	this.setValue(SESSIONDATA, value);
    }

    //
    public SessionData getSessionData() {
	return (SessionData)this.getValue(SESSIONDATA);
    }

    // This attribute is optional
    public void setCookiesData(CookiesData value) {
	this.setValue(COOKIESDATA, value);
    }

    //
    public CookiesData getCookiesData() {
	return (CookiesData)this.getValue(COOKIESDATA);
    }

    // This attribute is optional
    public void setDispatches(Dispatches value) {
	this.setValue(DISPATCHES, value);
    }

    //
    public Dispatches getDispatches() {
	if(debug) System.out.println("Running getDispatches"); //NOI18N
	return (Dispatches)this.getValue(DISPATCHES);
    }

    // This attribute is mandatory
    public void setRequestData(RequestData value) {
	this.setValue(REQUESTDATA, value);
    }

    //
    public RequestData getRequestData() {
	return (RequestData)this.getValue(REQUESTDATA);
    }

    // This attribute is optional
    public void setServletData(ServletData value) {
	this.setValue(SERVLETDATA, value);
    }

    //
    public ServletData getServletData() {
	return (ServletData)this.getValue(SERVLETDATA);
    }

    // This attribute is optional
    public void setEngineData(EngineData value) {
	this.setValue(ENGINEDATA, value);
    }

    //
    public EngineData getEngineData() {
	return (EngineData)this.getValue(ENGINEDATA);
    }

    // This attribute is optional
    public void setContextData(ContextData value) {
	this.setValue(CONTEXTDATA, value);
    }

    //
    public ContextData getContextData() {
	return (ContextData)this.getValue(CONTEXTDATA);
    }

    // This method verifies that the mandatory properties are set
    public boolean verify() {
	return true;
    }

    public String getServerAndPort() 
    {
	String server = null;
	String port = null;
		
	try {
	    server = getEngineData().getAttributeValue("serverName"); // NOI18N
	    port = getEngineData().getAttributeValue("serverPort"); // NOI18N
	    return server.concat(":").concat(port); //NOI18N
	}
	catch(NullPointerException npe) {
	}
	    
	// Backwards compatibility
	server = getServletData().getAttributeValue("serverName"); // NOI18N
	port = getServletData().getAttributeValue("serverPort"); // NOI18N
	return server.concat(":").concat(port); //NOI18N
    }


    public String getServerName() {
	
	try {
	    return getEngineData().getAttributeValue("serverName"); // NOI18N
	}
	catch(NullPointerException npe) {
	}
	    
	// Backwards compatibility
	return getServletData().getAttributeValue("serverName"); // NOI18N
    }

    public int getServerPort() {
	
	String portS = null;
	try {
	    portS = getEngineData().getAttributeValue("serverPort"); // NOI18N
	}
	catch(NullPointerException npe) {
	}
	if(portS == null) 
	    portS = getServletData().getAttributeValue("serverPort"); // NOI18N 
	return Integer.parseInt(portS);
    }


    public String getServerPortAsString() {
	
	String portS = null;
	try {
	    portS = getEngineData().getAttributeValue("serverPort"); // NOI18N
	}
	catch(NullPointerException npe) {
	}
	if(portS == null) 
	    portS = getServletData().getAttributeValue("serverPort"); // NOI18N 
	return portS;
	
	
    }


    public void setServerName(String server) {
	
	try {
	    getEngineData().setAttributeValue("serverName", server); // NOI18N
	    return;
	}
	catch(NullPointerException npe) {
	}
	    
	try {
	    getServletData().getAttributeValue("serverName, server"); // NOI18N
	    return;
	}
	catch(NullPointerException npe) {
	}
    }

    public void setServerPort(int port) {
	
	try {
	    getEngineData().setAttributeValue("serverPort",  // NOI18N
					      String.valueOf(port));
	    return;
	}
	catch(NullPointerException npe) {
	}
	

	try {
	    getServletData().setAttributeValue("serverPort",  // NOI18N
					      String.valueOf(port));
	    return;
	}
	catch(NullPointerException npe) {
	}
    }

    public void setServerPort(String port) {
	
	try {
	    getEngineData().setAttributeValue("serverPort",  // NOI18N
					      port); 
	    return;
	}
	catch(NullPointerException npe) {
	}
	

	try {
	    getServletData().setAttributeValue("serverPort",  // NOI18N
					       port);
	    return;
	}
	catch(NullPointerException npe) {
	}
    }
    

    //
    public static void addComparator(BeanComparator c) {
	MonitorData.comparators.add(c);
    }

    //
    public static void removeComparator(BeanComparator c) {
	MonitorData.comparators.remove(c);
    }
    //
    public void addPropertyChangeListener(PropertyChangeListener l) {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(PropertyChangeListener l) {
	BeanProp p = this.beanProp();
	if (p != null)
	    p.removePCListener(l);
    }

    //
    public void addPropertyChangeListener(String n, PropertyChangeListener l){
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.addPCListener(l);
    }

    //
    public void removePropertyChangeListener(String n,
					     PropertyChangeListener l) {
	BeanProp p = this.beanProp(n);
	if (p != null)
	    p.removePCListener(l);
    }

    //
    // This method returns the root of the bean graph
    // Each call creates a new bean graph from the specified DOM graph
    //
    public static MonitorData createGraph(Node doc) {
	return new MonitorData(doc, Common.NO_DEFAULT_VALUES);
    }

    public static MonitorData createGraph(java.io.Reader reader)
	throws IOException {
	try {
	    return MonitorData.createGraph(reader, false);
	}
	catch(IOException ioe) {
	    throw ioe;
	}
    }

    public static MonitorData createGraph(java.io.Reader reader, 
					  boolean validate) throws IOException {
	try {
	    InputSource insource = new InputSource(reader);
	    insource.setEncoding("UTF-8"); // NOI18N
	    Document doc = 
		GraphManager.createXmlDocument(insource, validate);
	    return MonitorData.createGraph(doc);
	}
	catch (Throwable t) {
	    throw new IOException();
	}
    }

    //
    // This method returns the root for a new empty bean graph
    //
    public static MonitorData createGraph() {
	return new MonitorData();
    }

    public void write(OutputStream out) throws IOException {
	throw new RuntimeException("Don't do this!");  // NOI18N
    }
    
    public void write(Writer writer) throws IOException {
        try {
            this.write(writer, "UTF-8"); // NOI18N
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
	String s;
	BaseBean n;
	str.append(indent);
	str.append("ClientData"); // NOI18N
	n = this.getClientData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(CLIENTDATA, 0, str, indent);

	str.append(indent);
	str.append("SessionData"); // NOI18N
	n = this.getSessionData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(SESSIONDATA, 0, str, indent);

	str.append(indent);
	str.append("CookiesData"); // NOI18N
	n = this.getCookiesData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(COOKIESDATA, 0, str, indent);

	str.append(indent);
	str.append("RequestData"); // NOI18N
	n = this.getRequestData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(REQUESTDATA, 0, str, indent);

	str.append(indent);
	str.append("ServletData"); // NOI18N
	n = this.getServletData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(SERVLETDATA, 0, str, indent);

	str.append("ContextData"); // NOI18N
	n = this.getContextData();
	if (n != null)
	    n.dump(str, indent + "\t"); // NOI18N
	else
	    str.append(indent+"\tnull"); // NOI18N
	this.dumpAttributes(CONTEXTDATA, 0, str, indent);

	str.append(indent);
	str.append("Dispatches");	// NOI18N
	n = this.getDispatches();
	if (n != null)
	    n.dump(str, indent + "\t");	// NOI18N
	else
	    str.append(indent+"\tnull");	// NOI18N
	this.dumpAttributes(DISPATCHES, 0, str, indent);
    }

    public String dumpBeanNode() {
	StringBuffer str = new StringBuffer();
	str.append("MonitorData\n"); // NOI18N
	this.dump(str, "\n  "); // NOI18N
	return str.toString();
    }

    /** Writes the data to a temp file. 
     * @param filename The name of the file to which the data is
     * written
     * @return the absolute path to the file as a String
     *
     */
    public String createTempFile(String filename) { 

	StringBuffer buf = new StringBuffer
	    (System.getProperty("java.io.tmpdir")); // NOI18N
	buf.append(System.getProperty("file.separator")); // NOI18N
	buf.append(filename); 

	try {
	    File file = new File(buf.toString()); 
	    FileOutputStream fout = new FileOutputStream(file);
	    PrintWriter pw = new PrintWriter(fout);
	    this.write(pw);
	    pw.close();
	    fout.close();
	    return file.getAbsolutePath(); 
	}
	catch(Throwable throwable) {
	    return "null"; //NOI18N
	}     
    }

    /*
    public TransactionNode createTransactionNode(boolean current) {
	TransactionNode node = 
	    new TransactionNode(this.getAttributeValue("id"), // NOI18N
				this.getAttributeValue("method"), // NOI18N
				this.getAttributeValue("resource")); // NOI18N
	node.setCurrent(current);
	return node;
    }
    */
}
