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

package org.netbeans.modules.httpserver;

import java.awt.Dialog;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import javax.swing.event.EventListenerList;
import org.openide.DialogDescriptor;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbPreferences;

/**
 * Options for http server
 *
 * @author Ales Novak, Petr Jiricka
 */
public final class HttpServerSettings {
    private static final HttpServerSettings INSTANCE = new HttpServerSettings();

    private static final int MAX_START_RETRIES = 20;
    private static int currentRetries = 0;

    protected static final EventListenerList listenerList = new EventListenerList();

    /** Has this been initialized ?
    *  Becomes true if a "running" getter or setter is called
    */
    @SuppressWarnings("PackageVisibleField")
    static boolean inited = false;

    /** Contains threads which are or will be asking for access for the given IP address. */
    private static final ConcurrentHashMap<InetAddress,Thread> whoAsking = new ConcurrentHashMap<>();

    public static final int SERVER_STARTUP_TIMEOUT = 3000;

    /** constant for local host */
    public static final String LOCALHOST = "local"; // NOI18N
    /** constant for any host */
    public static final String ANYHOST = "any"; // NOI18N

    @SuppressWarnings("PublicField")
    public static HostProperty hostProperty = null;

    public static final String PROP_PORT = "port"; // NOI18N
    public static final String PROP_HOST_PROPERTY = "hostProperty"; // NOI18N
    public static final String PROP_RUNNING = "running"; // NOI18N

    private static final String PROP_SHOW_GRANT_ACCESS = "showGrantAccess"; // NOI18N

    /**
     * port
     */
    private static final int DEFAULT_PORT = 8082;

    /**
     * mapping of wrapper to URL
     */
    @SuppressWarnings("FieldMayBeFinal")
    private static String wrapperBaseURL = "/resource/"; // NOI18N
    
    /** Reflects whether the server is actually running, not the running property */
    @SuppressWarnings("PackageVisibleField")
    static boolean running = false;

    private static boolean startStopMessages = true;

    /** Lock for the httpserver operations */
    private static Object httpLock;
    
    private  static Preferences getPreferences() {
        return NbPreferences.forModule(HttpServerSettings.class);
    }    
    
    /**
     * Obtains lock for httpserver synchronization
     */
    static final Object httpLock () {
        if (httpLock == null) {
            httpLock = new Object ();
        }
        return httpLock;
    }

    private HttpServerSettings() {
    }

    public static HttpServerSettings getDefault() {
        return INSTANCE;
    }
    
    /** getter for running status */
    public boolean isRunning() {
        if (inited) {
            return running;
        }
        else {
            // this used to be true, but it seems more reasonable not to start the server by default
            // Fixes bug 11347
            setRunning(false);
            return running;
        }
    }

    /** Intended to be called by the thread which succeeded to start the server */
    void runSuccess() {
        synchronized (httpLock ()) {
            currentRetries = 0;
            running = true;
            ServerControlNode.getInstance().updateNodeState();
            httpLock ().notifyAll();
        }
    }

    /** Intended to be called by the thread which failed to start the server. 
     * It decides whether try to start server on next port or show appropriate
     * error message.
     */
    void runFailure(Throwable t) {
        running = false;
        ServerControlNode.getInstance().updateNodeState();
        if (t instanceof IncompatibleClassChangeError) {
            // likely there is a wrong servlet API version on CLASSPATH
            DialogDisplayer.getDefault ().notify(new NotifyDescriptor.Message(
               NbBundle.getMessage (HttpServerSettings.class, "MSG_HTTP_SERVER_incompatbleClasses"),
               NotifyDescriptor.Message.WARNING_MESSAGE));
        }
        else if (t instanceof java.net.BindException) {
            // can't open socket - we can retry
            currentRetries ++;
            if (currentRetries <= MAX_START_RETRIES) {
                setPort(getPort() + 1);
                setRunning(true);
            }
            else {
                currentRetries = 0;
                DialogDisplayer.getDefault ().notify(new NotifyDescriptor.Message(
                                               NbBundle.getMessage (HttpServerSettings.class, "MSG_HTTP_SERVER_START_FAIL"),
                                               NotifyDescriptor.Message.WARNING_MESSAGE));
                int p = getPort ();
                if (p < 1024 && inited && Utilities.isUnix()) {
                    DialogDisplayer.getDefault ().notify(new NotifyDescriptor.Message(
                                               NbBundle.getMessage (HttpServerSettings.class, "MSG_onlyRootOnUnix"),
                                               NotifyDescriptor.WARNING_MESSAGE));
                }

            }
        }
        else {
            // unknown problem
            DialogDisplayer.getDefault ().notify(new NotifyDescriptor.Message(
               NbBundle.getMessage (HttpServerSettings.class, "MSG_HTTP_SERVER_START_FAIL_unknown"),
               NotifyDescriptor.Message.WARNING_MESSAGE));
        }
    }

    /** Restarts the server if it is running - must be called in a synchronized block 
     *  No need to restart if it is called during deserialization.
     */
    private void restartIfNecessary(boolean printMessages) {
        if (running) {
            if (!printMessages)
                setStartStopMessages(false);
            HttpServerModule.stopHTTPServer();
            HttpServerModule.initHTTPServer();
            // messages will be enabled by the server thread
        }
    }

    /** setter for running status */
    public void setRunning(boolean running) {
        inited = true;
        if (HttpServerSettings.running == running)
            return;

        synchronized (httpLock ()) {
            if (running) {
                // running status is set by another thread
                HttpServerModule.initHTTPServer();
            }
            else {
                HttpServerSettings.running = false;
                ServerControlNode.getInstance().updateNodeState();
                HttpServerModule.stopHTTPServer();
            }
        }
    }

    // NOT publicly available
    
    /** getter for classpath base */
    String getWrapperBaseURL() {
        return wrapperBaseURL;
    }

    /** setter for port */
    public void setPort(int p) {
        if (p <= 0 || p >65535) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    NbBundle.getMessage(HttpServerSettings.class, "ERR_PortNumberOutOfRange", p),
                    NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        
        synchronized (httpLock ()) {
            getPreferences().putInt(PROP_PORT,p);
            restartIfNecessary(true);
        }
    }

    /** getter for port */
    public int getPort() {
        return getPreferences().getInt(PROP_PORT, DEFAULT_PORT);
    }

    public void setStartStopMessages(boolean ssm) {
        startStopMessages = ssm;
    }

    public boolean isStartStopMessages() {
        return startStopMessages;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.httpserver.HttpServerSettings");
    }

    public void addGrantAccessListener(GrantAccessListener l) {
        listenerList.add(GrantAccessListener.class, l);
    }

    public void removeGrantAccessListener(GrantAccessListener l) {
        listenerList.remove(GrantAccessListener.class, l);
    }

    /** Returns true if oneof the listeners allowed access */
    protected boolean fireGrantAccessEvent(InetAddress clientAddress, String resource) {
        Object[] listeners = listenerList.getListenerList();
        GrantAccessEvent grantAccessEvent = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==GrantAccessListener.class) {
                if (grantAccessEvent == null)
                    grantAccessEvent = new GrantAccessEvent(this, clientAddress, resource);
                ((GrantAccessListener)listeners[i+1]).grantAccess(grantAccessEvent);
            }
        }
        return (grantAccessEvent == null) ? false : grantAccessEvent.isGranted();
    }

    /**
     * Requests access for address addr. If necessary asks the user. Returns
     * true it the access
     * has been granted.
     */
    boolean allowAccess(InetAddress addr, String requestPath) {
        if (accessAllowedNow(addr, requestPath))
            return true;

        Thread askThread;
        synchronized (whoAsking) {
            // one more test in the synchronized block
            if (accessAllowedNow(addr, requestPath))
                return true;

            askThread = (Thread) whoAsking.get(addr);
            if (askThread == null) {
                askThread = Thread.currentThread();
                whoAsking.put(addr, askThread);
            }
        }

        // now ask the user
        synchronized (HttpServerSettings.class) {
            if (askThread != Thread.currentThread()) {
                return accessAllowedNow(addr, requestPath);
            }

            try {
                if (!isShowGrantAccessDialog ())
                    return false;
                
                String msg = NbBundle.getMessage (HttpServerSettings.class, "MSG_AddAddress", addr.getHostAddress ());
                
                final GrantAccessPanel panel = new GrantAccessPanel (msg);
                DialogDescriptor descriptor = new DialogDescriptor (
                    panel,
                    NbBundle.getMessage (HttpServerSettings.class, "CTL_GrantAccessTitle"),
                    true,
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.NO_OPTION,
                    null
                );
                descriptor.setMessageType (NotifyDescriptor.QUESTION_MESSAGE);
                // descriptor.setOptionsAlign (DialogDescriptor.BOTTOM_ALIGN);
                final Dialog d  = DialogDisplayer.getDefault ().createDialog (descriptor);
                d.setSize (580, 180);
                d.setVisible(true);

                setShowGrantAccessDialog (panel.getShowDialog ());
                if (NotifyDescriptor.YES_OPTION.equals(descriptor.getValue ())) {
                    appendAddressToGranted(addr.getHostAddress());
                    return true;
                }
                else
                    return false;
            }
            finally {
                whoAsking.remove(addr);
            }
        } // end synchronized
    }

    /** Checks whether access to the server is now allowed. */
    private boolean accessAllowedNow(InetAddress addr, String resource) {
        if (hostProperty.getHost().equals(HttpServerSettings.ANYHOST))
            return true;

        Set hs = getGrantedAddressesSet();
        if (hs.contains(addr.getHostAddress()))
            return true;

        return fireGrantAccessEvent(addr, resource);
    }

    /** Appends the address to the list of addresses which have been granted access. */
    private void appendAddressToGranted(String addr) {
        synchronized (httpLock ()) {
            String granted = hostProperty.getGrantedAddresses().trim();
            if ((granted.length() > 0) &&
                    (granted.charAt(granted.length() - 1) != ';') &&
                    (granted.charAt(granted.length() - 1) != ','))
                granted += ',';
            granted += addr;
            hostProperty.setGrantedAddresses(granted);
        }
    }

    /** Returns a list of addresses which have been granted access to the web server,
    * including the localhost. Addresses are represented as strings. */
    Set<String> getGrantedAddressesSet() {
        HashSet<String> addr = new HashSet<>();
        try {
            addr.add(InetAddress.getByName("localhost").getHostAddress()); // NOI18N
            addr.add(InetAddress.getLocalHost().getHostAddress());
        }
        catch (UnknownHostException e) {}
        StringTokenizer st = new StringTokenizer(hostProperty.getGrantedAddresses(), ",;"); // NOI18N
        while (st.hasMoreTokens()) {
            String ipa = st.nextToken();
            ipa = ipa.trim();
            try {
                addr.add(InetAddress.getByName(ipa).getHostAddress());
            }
            catch (UnknownHostException e) {}
        }
        return addr;
    }

    /** Getter for property hostProperty.
     * @return Value of property hostProperty.
     */
    public HttpServerSettings.HostProperty getHostProperty () {
        if (hostProperty == null) {
            hostProperty = new HostProperty(getPreferences().get("grantedAddresses",""), 
                    getPreferences().get("host",LOCALHOST));
        }
        return hostProperty;
    }
    
    /** Setter for property hostProperty.
     * @param hostProperty New value of property hostProperty.
     */
    public void setHostProperty (HttpServerSettings.HostProperty hostProperty) {
        if (ANYHOST.equals(hostProperty.getHost ()) || LOCALHOST.equals(hostProperty.getHost ())) {
            HttpServerSettings.hostProperty.setHost(hostProperty.getHost());
            HttpServerSettings.hostProperty.setGrantedAddresses(hostProperty.getGrantedAddresses());
            getPreferences().put("host", hostProperty.getHost());//NOI18N
            getPreferences().put("grantedAddresses", hostProperty.getGrantedAddresses());//NOI18N
        }
    }
    
    public boolean isShowGrantAccessDialog () {
        return getPreferences().getBoolean(PROP_SHOW_GRANT_ACCESS, true);
    }
    
    public void setShowGrantAccessDialog (boolean show) {
        getPreferences().putBoolean(PROP_SHOW_GRANT_ACCESS,show);
    }
    
    /** Property value that describes set of host with granted access
     */
    public static class HostProperty implements java.io.Serializable {
        
        private String grantedAddresses;
        
        private String host;
        
        private static final long serialVersionUID = 1927848926692414249L;
        
        HostProperty (String grantedAddresses, String host) {
            this.grantedAddresses = grantedAddresses;
            this.host = host;
        }
        
        /** Getter for property host.
         * @return Value of property host.
         */
        public String getHost () {
            return host;
        }
        
        /** Setter for property host.
         * @param host New value of property host.
         */
        public void setHost (String host) {
            this.host = host;                        
        }
        
        /** Getter for property grantedAddresses.
         * @return Value of property grantedAddresses.
         */
        public String getGrantedAddresses () {
            return grantedAddresses;
        }
        
        /** Setter for property grantedAddresses.
         * @param grantedAddresses New value of property grantedAddresses.
         */
        public void setGrantedAddresses (String grantedAddresses) {
            this.grantedAddresses = grantedAddresses;            
        }
        
    }
}
