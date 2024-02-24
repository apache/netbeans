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

package org.netbeans.modules.tomcat5.deploy;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.tomcat5.config.gen.Context;
import org.netbeans.modules.tomcat5.config.gen.Engine;
import org.netbeans.modules.tomcat5.config.gen.Host;
import org.netbeans.modules.tomcat5.config.gen.SContext;
import org.netbeans.modules.tomcat5.config.gen.Server;
import org.netbeans.modules.tomcat5.config.gen.Service;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import java.io.*;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.netbeans.modules.tomcat5.AuthorizationException;
import org.netbeans.modules.tomcat5.util.TomcatProperties;

/** Implemtation of management task that provides info about progress
 *
 * @author  Radim Kubacki
 */
public class TomcatManagerImpl implements ProgressObject, Runnable {
    
    /** RequestProcessor processor that serializes management tasks. */
    private static RequestProcessor rp;
    
    /** Returns shared RequestProcessor. */
    private static synchronized RequestProcessor rp () {
        if (rp == null) {
            rp = new RequestProcessor ("Tomcat management", 1); // NOI18N
        }
        return rp;
    }

    /** Support for progress notifications. */
    private ProgressEventSupport pes;
    
    /** Command that is executed on running server. */
    private String command;
    
    /** Output of executed command (parsed for list commands). */
    private String output;
    
    /** Command type used for events. */
    private CommandType cmdType;
    
    /** InputStream of application data. */
    private InputStream istream;
    
    private TomcatManager tm;
    
    /** Has been the last access to tomcat manager web app authorized? */
    private boolean authorized;
    
    /** TargetModuleID of module that is managed. */
    private TomcatModule tmId;
    
    private static final Logger LOGGER = Logger.getLogger(TomcatManagerImpl.class.getName());
    
    public TomcatManagerImpl (TomcatManager tm) {
        this.tm = tm;
        pes = new ProgressEventSupport (this);
    }

    public void deploy (Target t, InputStream is, InputStream deplPlan) {
        Context ctx;
        try {
            ctx = Context.createGraph(deplPlan);
        } catch (RuntimeException e) {
            String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_DeployBrokenContextXml");
            pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, cmdType, msg, StateType.FAILED));
            return;
        }
        String ctxPath = ctx.getAttributeValue ("path");   // NOI18N
        tmId = new TomcatModule (t, ctxPath);
        command = "deploy?path=" + encodePath(tmId.getPath()); // NOI18N
        cmdType = CommandType.DISTRIBUTE;
        String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_DeploymentInProgress");
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, msg, StateType.RUNNING));
        istream = is;
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Deploys WAR file or directory to Tomcat using deplPlan as source 
     * of conetx configuration data.
     */
    public void install (Target t, File wmfile, File deplPlan) {
        // WAR file
        String docBase = wmfile.toURI ().toASCIIString ();
        if (docBase.endsWith ("/")) { // NOI18N
            docBase = docBase.substring (0, docBase.length ()-1);
        }
        if (wmfile.isFile ()) {
            // WAR file
            docBase = "jar:"+docBase+"!/"; // NOI18N
        }
        // config or path
        String ctxPath = null;
        try {
            if (!deplPlan.exists ()) {
                if (wmfile.isDirectory ()) {
                    ctxPath = "/"+wmfile.getName ();    // NOI18N
                }
                else {
                    ctxPath = "/"+wmfile.getName ().substring (0, wmfile.getName ().lastIndexOf ("."));    // NOI18N
                }
                tmId = new TomcatModule (t, ctxPath); // NOI18N
                command = "deploy?update=true&path="+encodePath(ctxPath)+"&war="+docBase; // NOI18N
            }
            else {
                FileInputStream in = new FileInputStream (deplPlan);
                Context ctx;
                try {
                    ctx = Context.createGraph(in);
                } catch (RuntimeException e) {
                    String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_DeployBrokenContextXml");
                    pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, cmdType, msg, StateType.FAILED));
                    return;
                }
                //PENDING #37763
//                tmId = new TomcatModule (t, ctx.getAttributeValue ("path")); // NOI18N
//                command = "install?update=true&config="+deplPlan.toURI ()+ // NOI18N
//                    "&war="+docBase; // NOI18N
                if (wmfile.isDirectory ()) {
                    ctxPath = "/"+wmfile.getName ();    // NOI18N
                }
                else {
                    ctxPath = "/"+wmfile.getName ().substring (0, wmfile.getName ().lastIndexOf ("."));    // NOI18N
                }
                ctxPath = ctx.getAttributeValue ("path");
                tmId = new TomcatModule (t, ctxPath); // NOI18N
                command = "deploy?update=true&path="+encodePath(tmId.getPath())+"&war="+docBase; // NOI18N
            }
            
            // call the command
            cmdType = CommandType.DISTRIBUTE;
            String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_DeploymentInProgress");
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, msg, StateType.RUNNING));
            
            rp ().post (this, 0, Thread.NORM_PRIORITY);
        }
        catch (java.io.FileNotFoundException fnfe) {
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, fnfe.getLocalizedMessage (), StateType.FAILED));
        }
        
    }
    
    public void initialDeploy (Target t, File contextXml, File dir) {
        try {
            FileInputStream in = new FileInputStream (contextXml);
            Context ctx = Context.createGraph (in);
            String docBaseURI = dir.getAbsoluteFile().toURI().toASCIIString();
            String docBase = dir.getAbsolutePath ();
            String ctxPath = ctx.getAttributeValue ("path");
            this.tmId = new TomcatModule (t, ctxPath, docBase); //NOI18N
            String tmpContextXml = createTempContextXml(docBase, ctx);
            if (tm.isTomcat50()) {
                command = "deploy?config=" + tmpContextXml + "&war=" + docBaseURI; // NOI18N
            } else {
                command = "deploy?config=" + tmpContextXml + "&path=" + encodePath(tmId.getPath()); // NOI18N
            }
            cmdType = CommandType.DISTRIBUTE;
            String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_DeploymentInProgress");
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, msg, StateType.RUNNING));
            rp ().post (this, 0, Thread.NORM_PRIORITY);
        } catch (java.io.IOException ioex) {
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, ioex.getLocalizedMessage (), StateType.FAILED));
        } catch (MissingResourceException e) {
            String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_DeployBrokenContextXml");
            pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, cmdType, msg, StateType.FAILED));
        }
    }

    public void remove(TomcatModule tmId) {
        // remove context from server.xml
        Server server = tm.getRoot();
        if (server != null && removeContextFromServer(server, tmId.getPath())) {
            File f = null;
            try {
                f = tm.getTomcatProperties().getServerXml();
                server.write(f);
            } catch (IOException | Schema2BeansRuntimeException e) {
                // cannot save changes
                pes.fireHandleProgressEvent(tmId, new Status (ActionType.EXECUTE, 
                        CommandType.UNDEPLOY, 
                        NbBundle.getMessage(TomcatManagerImpl.class, "MSG_ServerXml_RO", f.getAbsolutePath()),
                        StateType.FAILED));                
                return;
            }
        }
        this.tmId = tmId;
        command = "undeploy?path="+encodePath(tmId.getPath()); // NOI18N
        cmdType = CommandType.UNDEPLOY;
        String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_UndeploymentInProgress");
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, msg, StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);        
    }

    /**
     * Remove context with the specified path from the Server tree.
     * Look for the first appearance of the service and host element.
     * (ide currently does not support multiple service and host elements).
     */
    private boolean removeContextFromServer(Server server, String path) {
        // root web application is specified as an empty string
        if (path.equals("/")) {
            path = ""; // NOI18N
        }
        Service[] service = server.getService();
        if (service.length > 0) {
            Engine engine = service[0].getEngine();
            if (engine != null) {
                Host[] host = engine.getHost();
                if (host.length > 0) {                    
                    SContext[] sContext = host[0].getSContext();
                    for (int i = 0; i < sContext.length; i++) {
                        if (sContext[i].getAttributeValue("path").equals(path)) { // NOI18N
                            host[0].removeSContext(sContext[i]);
                            return true;
                        }                        
                    }
                }
            }
        }
        return false;
    }
    
    /** Starts web module. */
    public void start (TomcatModule tmId) {
        this.tmId = tmId;
        command = "start?path="+encodePath(tmId.getPath()); // NOI18N
        cmdType = CommandType.START;
        String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_StartInProgress");
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, msg, StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Stops web module. */
    public void stop (TomcatModule tmId) {
        this.tmId = tmId;
        command = "stop?path="+encodePath(tmId.getPath()); // NOI18N
        cmdType = CommandType.STOP;
        String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_StopInProgress");
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, msg, StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Reloads web module. */
    public void reload (TomcatModule tmId) {
        this.tmId = tmId;
        command = "reload?path="+encodePath(tmId.getPath()); // NOI18N
        cmdType = CommandType.REDEPLOY;
        String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_ReloadInProgress");
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, msg, StateType.RUNNING));
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    public void incrementalRedeploy (TomcatModule tmId) {
        try {
            this.tmId = tmId;
            String docBase = tmId.getDocRoot ();
            assert docBase != null;
            String docBaseURI = new File (docBase).toURI().toASCIIString();
            File contextXml = new File (docBase + "/META-INF/context.xml"); // NO18N
            FileInputStream in = new FileInputStream (contextXml);
            Context ctx = Context.createGraph (in);
            String tmpContextXml = createTempContextXml(docBase, ctx);
            if (tm.isTomcat50()) {
                command = "deploy?config=" + tmpContextXml + "&war=" + docBaseURI; // NOI18N
            } else {
                command = "deploy?config=" + tmpContextXml + "&path=" + encodePath(tmId.getPath()); // NOI18N
            }
            cmdType = CommandType.DISTRIBUTE;
            String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_DeployInProgress");
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, msg, StateType.RUNNING));
            rp ().post (this, 0, Thread.NORM_PRIORITY);
        } catch (java.io.IOException ioex) {
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, ioex.getLocalizedMessage (), StateType.FAILED));
        } catch (MissingResourceException e) {
            String msg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_DeployBrokenContextXml");
            pes.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, cmdType, msg, StateType.FAILED));
        }
    }
    
    /**
     * Translates a context path string into <code>application/x-www-form-urlencoded</code> format.
     */
    private static String encodePath(String str) {
        try {
            StringTokenizer st = new StringTokenizer(str, "/"); // NOI18N
            if (!st.hasMoreTokens()) {
                return str;
            }
            StringBuilder result = new StringBuilder();
            while (st.hasMoreTokens()) {
                result.append("/").append(URLEncoder.encode(st.nextToken(), StandardCharsets.UTF_8.name()));
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // this should never happen
        }
    }
    
    /**
     * Create a temporary copy of context.xml and set a docBase attribute 
     * in it. This does not modify the existing context.xml.
     *
     * @return properly escaped URL (<code>application/x-www-form-urlencoded</code>) in string form
     */
    private String createTempContextXml(String docBase, Context ctx) throws IOException {
        File tmpContextXml = Files.createTempFile("context", ".xml").toFile(); // NOI18N
        tmpContextXml.deleteOnExit();
        if (!docBase.equals (ctx.getAttributeValue ("docBase"))) { //NOI18N
            ctx.setAttributeValue ("docBase", docBase); //NOI18N
            try (FileOutputStream fos = new FileOutputStream (tmpContextXml)) {
                ctx.write (fos);
            }
        }
        // http://www.netbeans.org/issues/show_bug.cgi?id=167139
        URL url = tmpContextXml.toURI().toURL();
        String ret = URLEncoder.encode(url.toString(), StandardCharsets.UTF_8.name());
        return ret;
    }
    
    /** Lists web modules.
     * This method runs synchronously.
     * @param target server target
     * @param state one of ENUM_ constants.
     *
     * @throws IllegalStateException when access to tomcat manager has not been
     * authorized and therefore list of target modules could not been retrieved
     */
    TargetModuleID[] list (Target t, int state) throws IllegalStateException {
        command = "list"; // NOI18N
        run ();
        if (!authorized) {
            // connection to tomcat manager has not been authorized
            String errMsg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_AuthorizationFailed",
                    tm.isAboveTomcat70() ? "manager-script" : "manager");
            IllegalStateException ise = new IllegalStateException(errMsg);
            throw (IllegalStateException)ise.initCause(new AuthorizationException());
        }
        // PENDING : error check
        java.util.List modules = new java.util.ArrayList ();
        boolean first = true;
        StringTokenizer stok = new StringTokenizer (output, "\r\n");    // NOI18N
        while (stok.hasMoreTokens ()) {
            String line = stok.nextToken ();
            if (first) {
                first = false;
            }
            else {
                StringTokenizer ltok = new StringTokenizer (line, ":"); // NOI18N
                try {
                    String ctx = ltok.nextToken ();
                    String s = ltok.nextToken ();
                    String tag = ltok.nextToken ();
                    String path = null;
                    //take the rest of line as path (it can contain ':')
                    // #50410 - path information is missing in the Japanese localization of Tomcat Manager
                    if (ltok.hasMoreTokens()) {
                        path = line.substring (ctx.length () + s.length () + tag.length () + 3);
                    }
                    if ("running".equals (s)
                    &&  (state == TomcatManager.ENUM_AVAILABLE || state == TomcatManager.ENUM_RUNNING)) {
                        modules.add (new TomcatModule (t, ctx, path));
                    }
                    if ("stopped".equals (s)
                    &&  (state == TomcatManager.ENUM_AVAILABLE || state == TomcatManager.ENUM_NONRUNNING)) {
                        modules.add (new TomcatModule (t, ctx, path));
                    }
                } catch (java.util.NoSuchElementException e) {
                    // invalid value
                    LOGGER.log(Level.FINE, line, e);
                    System.err.println(line);
                    e.printStackTrace();
                }
            }
        }
        return (TargetModuleID []) modules.toArray (new TargetModuleID[0]);
    }
    
    /** Queries Tomcat server to get JMX beans containing management information
     * @param param encoded parameter(s) for query
     * @return server output
     */
    public String jmxProxy (String query) {
        command = "jmxproxy/"+query; // NOI18N
        run ();
        // PENDING : error check
        return output;
    }
    
    /** JSR88 method. */
    @Override
    public ClientConfiguration getClientConfiguration (TargetModuleID targetModuleID) {
        return null; // PENDING
    }
    
    /** JSR88 method. */
    @Override
    public DeploymentStatus getDeploymentStatus () {
        return pes.getDeploymentStatus ();
    }
    
    /** JSR88 method. */
    @Override
    public TargetModuleID[] getResultTargetModuleIDs () {
        return new TargetModuleID [] { tmId };
    }
    
    /** JSR88 method. */
    @Override
    public boolean isCancelSupported () {
        return false;
    }
    
    /** JSR88 method. */
    @Override
    public void cancel () 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("cancel not supported in Tomcat deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    @Override
    public boolean isStopSupported () {
        return false;
    }
    
    /** JSR88 method. */
    @Override
    public void stop () throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("stop not supported in Tomcat deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    @Override
    public void addProgressListener (ProgressListener l) {
        pes.addProgressListener (l);
    }
    
    /** JSR88 method. */
    @Override
    public void removeProgressListener (ProgressListener l) {
        pes.removeProgressListener (l);
    }
    
    /** Executes one management task. */
    @Override
    public synchronized void run () {
        LOGGER.log(Level.FINE, command);
        pes.fireHandleProgressEvent (tmId, new Status (ActionType.EXECUTE, cmdType, command /* message */, StateType.RUNNING));
        
        output = "";
        authorized = true;
        
        int retries = 4;
        
        // similar to Tomcat's Ant task
        URLConnection conn = null;
        
        URL urlToConnectTo = null;

        boolean failed = false;
        String msg = null;
        while (retries >= 0) {
            retries = retries - 1;
            try {

                // Create a connection for this command
                String uri = tm.getPlainUri ();
                String withoutSpaces = (uri + command).replace(" ", "%20");  //NOI18N
                urlToConnectTo = new URL(withoutSpaces);
                
                if (Boolean.getBoolean("org.netbeans.modules.tomcat5.LogManagerCommands")) { // NOI18N
                    String message = "Tomcat 5 sending manager command: " + urlToConnectTo;
                    Logger.getLogger(TomcatManagerImpl.class.getName()).log(Level.FINE, null, new Exception(message));
                }

                if (tm.isMisconfiguredProxy()) {
                    conn = urlToConnectTo.openConnection(Proxy.NO_PROXY);
                } else {
                    conn = urlToConnectTo.openConnection();
                }
                HttpURLConnection hconn = (HttpURLConnection) conn;

                // Set up standard connection characteristics
                hconn.setAllowUserInteraction(false);
                hconn.setDoInput(true);
                hconn.setUseCaches(false);
                if (istream != null) {
                    hconn.setDoOutput(true);
                    hconn.setRequestMethod("PUT");   // NOI18N
                    hconn.setRequestProperty("Content-Type", "application/octet-stream");   // NOI18N
                } else {
                    hconn.setDoOutput(false);
                    hconn.setRequestMethod("GET"); // NOI18N
                }
                hconn.setRequestProperty("User-Agent", // NOI18N
                                         "NetBeansIDE-Tomcat-Manager/1.0"); // NOI18N
                // Set up an authorization header with our credentials
                TomcatProperties tp = tm.getTomcatProperties();
                String input = tp.getUsername () + ":" + tp.getPassword ();
                String auth = Base64.getEncoder().encodeToString(input.getBytes());
                hconn.setRequestProperty("Authorization", // NOI18N
                                         "Basic " + auth); // NOI18N

                // Establish the connection with the server
                hconn.connect();
                int respCode = hconn.getResponseCode();
                if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED 
                    || respCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    // connection to tomcat manager has not been allowed
                    authorized = false;
                    String errMsg = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_AuthorizationFailed");
                    pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, cmdType, errMsg, StateType.FAILED));
                    return;
                } else if (respCode == HttpURLConnection.HTTP_BAD_GATEWAY) {
                    throw new IOException(Integer.toString(respCode));
                }
                if (Boolean.getBoolean("org.netbeans.modules.tomcat5.LogManagerCommands")) { // NOI18N
                    int code = hconn.getResponseCode();
                    String message = "Tomcat 5 receiving response, code: " + code;
                    System.out.println(message);
                    Logger.getLogger(TomcatManagerImpl.class.getName()).log(Level.INFO, null, new Exception(message));
                }
                // Send the request data (if any)
                if (istream != null) {
                    try (BufferedOutputStream ostream = new BufferedOutputStream(hconn.getOutputStream(), 1024)) {
                        byte buffer[] = new byte[1024];
                        while (true) {
                            int n = istream.read(buffer);
                            if (n < 0) {
                                break;
                            }
                            ostream.write(buffer, 0, n);
                        }
                        ostream.flush();
                    }
                    istream.close();
                }

                // Process the response message
                try (InputStreamReader reader = new InputStreamReader(hconn.getInputStream(), StandardCharsets.UTF_8)) {
                    retries = -1;
                    StringBuffer buff = new StringBuffer();
                    String error = null;
                    msg = null;
                    boolean first = !command.startsWith ("jmxproxy");   // NOI18N
                    while (true) {
                        int ch = reader.read();
                        if (ch < 0) {
                            output += buff.toString ()+"\n";    // NOI18N
                            break;
                        } else if ((ch == '\r') || (ch == '\n')) {
                            String line = buff.toString();
                            buff.setLength(0);
                            LOGGER.log(Level.FINE, line);
                            if (first) {
                                // hard fix to accept the japanese localization of manager app
                                String japaneseOK="\u6210\u529f"; //NOI18N
                                msg = line;
                                // see issue #62529
                                if (line.indexOf("java.lang.ThreadDeath") != -1) { // NOI18N
                                    String warning = NbBundle.getMessage(TomcatManagerImpl.class, "MSG_ThreadDeathWarning");
                                    pes.fireHandleProgressEvent(
                                        tmId, 
                                        new Status(ActionType.EXECUTE, cmdType, warning, StateType.RUNNING)
                                    );
                                } else if (!(line.startsWith("OK -") || line.startsWith(japaneseOK))) { // NOI18N
                                    error = line;
                                }
                                first = false;
                            }
                            output += line+"\n";    // NOI18N
                        } else {
                            buff.append((char) ch);
                        }
                    }
                    if (buff.length() > 0) {
                        LOGGER.log(Level.FINE, buff.toString());
                    }
                    if (error != null) {
                        LOGGER.log (Level.INFO, "TomcatManagerImpl connecting to: " + urlToConnectTo, error); // NOI18N
                        pes.fireHandleProgressEvent (tmId, new Status (ActionType.EXECUTE, cmdType, error, StateType.FAILED));
                        failed = true;
                    }
                    if (msg == null) {
                        msg = buff.toString();
                    }
                }
            } catch (IOException | MissingResourceException e) {
                if (e instanceof IOException && e.getMessage() != null
                        && e.getMessage().contains(Integer.toString(HttpURLConnection.HTTP_BAD_GATEWAY))) {
                    tm.setMisconfiguredProxy(true);
                    LOGGER.log(Level.INFO, "Proxy is misconfigured for localhost");
                } else if (tm.isMisconfiguredProxy()) {
                    tm.setMisconfiguredProxy(false);
                }
                if (retries < 0) {
                    LOGGER.log(Level.INFO, "TomcatManagerImpl connecting to: " + urlToConnectTo, e); // NOI18N
                    pes.fireHandleProgressEvent (tmId, new Status (ActionType.EXECUTE, cmdType, e.getLocalizedMessage (), StateType.FAILED));
                    failed = true;
                }
                // throw t;
            } finally {
                if (istream != null) {
                    try {
                        istream.close();
                    } catch (java.io.IOException ioe) { // ignore this
                    }
                    istream = null;
                }                
            }
            if (retries >=0) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {}
            }
        } // while
        if (!failed) {
            pes.fireHandleProgressEvent (tmId, new Status (ActionType.EXECUTE, cmdType, msg, StateType.COMPLETED));
        }
    }
}
