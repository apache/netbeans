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

package org.netbeans.modules.extbrowser;

import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Utilities;

/** Factory and descriptions for external browser
 */

public class ExtWebBrowser implements HtmlBrowser.Factory, java.io.Serializable, PropertyChangeListener {

    private static final long serialVersionUID = -3021027901671504127L;

    public static final String PROP_NAME = "name"; // NOI18N
    
    /** Browser executable property name */
    public static final String PROP_BROWSER_EXECUTABLE = "browserExecutable"; // NOI18N
    
    /** DDE server property name */
    public static final String PROP_DDESERVER = "dDEServer";   // NOI18N
    
    /** DDE activate timeout property name */
    public static final String PROP_DDE_ACTIVATE_TIMEOUT = "activateTimeout";   // NOI18N
    
    /** DDE openURL timeout property name */
    public static final String PROP_DDE_OPENURL_TIMEOUT = "openurlTimeout";     // NOI18N

    /** Fired when Browser Family ID has changed. Only System Default browser are
        expected to change Browser Family ID. */
    public static final String PROP_PRIVATE_BROWSER_FAMILY = "privateBrowserFamilyId";   // NOI18N

    /** Name of DDE server corresponding to Google Chrome */
    public static final String CHROME = "CHROME";   // NOI18N
    /** Name of DDE server corresponding to Google Chromium */
    public static final String CHROMIUM = "CHROMIUM";   // NOI18N
    /** Name of DDE server corresponding to Internet Explorer */
    public static final String IEXPLORE = "IEXPLORE";   // NOI18N
    /** Name of DDE server corresponding to Mozilla */
    public static final String MOZILLA  = "MOZILLA";    // NOI18N
    /** Name of DDE server corresponding to Firefox */
    public static final String FIREFOX  = "FIREFOX";    // NOI18N
    
    /** Default for DDE activate timeout property */
    protected static final int DEFAULT_ACTIVATE_TIMEOUT = 2000;

    /** Default for DDE openURL timeout property */
    protected static final int DEFAULT_OPENURL_TIMEOUT = 3000;
    
    /** storage for DDE server property */
    protected String ddeServer;
    
    /** storage for DDE activate timeout property */
    protected int activateTimeout = DEFAULT_ACTIVATE_TIMEOUT;

    /** storage for DDE openURL timeout property */
    protected int openurlTimeout = DEFAULT_OPENURL_TIMEOUT;

    /** Logger for extbrowser module. */
    private static final Logger err = Logger.getLogger("org.netbeans.modules.extbrowser");   // NOI18N
    
    protected String name;

    private PrivateBrowserFamilyId family;

    public static Logger getEM() {
        return err;
    }
        
    /** Holds value of property browserExecutable. */
    protected NbProcessDescriptor browserExecutable;
    
    protected transient PropertyChangeSupport pcs;

    public ExtWebBrowser () {
        this(PrivateBrowserFamilyId.UNKNOWN);
    }

    /** Creates new Browser */
    public ExtWebBrowser (PrivateBrowserFamilyId family) {
        this.family = family;
        init();
    }

    /** initialize object */
    private void init () {
        if (err.isLoggable(Level.FINE)) {
            err.log(Level.FINE, getClass().getName() + " " + System.currentTimeMillis() + "> init");
        }
        pcs = new PropertyChangeSupport(this);
        if (Utilities.isWindows()) {
            pcs.addPropertyChangeListener(this);
        }
    }

    private ExtWebBrowser browserExecutableDelegate = null;

    /**
     * This method allows browser factory to delegate its browser executable
     * to some other browser factory. For example "Chrome with NB integration"
     * should be executed just like regular "Chrome" browser.
     */
    public void useBrowserExecutableDelegate(ExtWebBrowser otherBrowser) {
        this.browserExecutableDelegate = otherBrowser;
    }


    /**
     * Gets DDE server name
     * @return server name of DDEserver.
     *         <CODE>null</CODE> when no server is selected (means default web browser).
     */
    public String getDDEServer () {
        return ddeServer;
    }
    
    /**
     * Sets DDE server name
     * @param ddeServer name of DDE server or <CODE>null</CODE>
     */
    public void setDDEServer (String ddeServer) {
        if ((ddeServer != null) && !ddeServer.equals(this.ddeServer)) {
            String old = this.ddeServer;
            this.ddeServer = ddeServer;
            pcs.firePropertyChange (PROP_DDESERVER, old, ddeServer);
            err.log(Level.INFO, "DDEServer changed to: " + ddeServer);                  // NOI18N
        }
    }

    /** Getter for property openurlTimeout.
     * @return Value of property openurlTimeout.
     *
     */
    public int getOpenurlTimeout() {
        return openurlTimeout;
    }
    
    /** Setter for property openurlTimeout.
     * @param openurlTimeout New value of property openurlTimeout.
     *
     */
    public void setOpenurlTimeout(int openurlTimeout) {
        if (openurlTimeout != this.openurlTimeout) {
            int oldVal = this.openurlTimeout;
            this.openurlTimeout = openurlTimeout;
            pcs.firePropertyChange(PROP_DDE_OPENURL_TIMEOUT, oldVal, openurlTimeout);
        }
    }
    
    /** Getter for property activeTimeout.
     * @return Value of property activeTimeout.
     *
     */
    public int getActivateTimeout() {
        return activateTimeout;
    }
    
    /** Setter for property activeTimeout.
     * @param activateTimeout New value of property activeTimeout.
     */
    public void setActivateTimeout(int activateTimeout) {
        if (activateTimeout != this.activateTimeout) {
            int oldVal = this.activateTimeout;
            this.activateTimeout = activateTimeout;
            pcs.firePropertyChange(PROP_DDE_ACTIVATE_TIMEOUT, oldVal, activateTimeout);
        }
    }

    // getter for browser name - should be overriden in subclasses
    public String getName() {
        return name;
    }
    
    /** Setter for browser name
     * @param name browser name
     */
    public void setName(String name) {
        if ((name != null) && (!name.equals(this.name))) {
            String oldVal = this.name;
            this.name = name;
            pcs.firePropertyChange(PROP_NAME, oldVal, name);
        }
    }
    
    /** Getter for property browserExecutable.
     * @return Value of property browserExecutable.
     */
    public NbProcessDescriptor getBrowserExecutable () {
        if (browserExecutableDelegate != null) {
            return browserExecutableDelegate.getBrowserExecutable();
        }
        NbProcessDescriptor result = browserExecutable;
        if (browserExecutable == null || "".equals(browserExecutable.getProcessName())) { // NOI18N
            result = defaultBrowserExecutable();
        }
        if (err.isLoggable(Level.FINE)) {
            err.log(Level.FINE, "" + System.currentTimeMillis() + " getBrowserExecutable: " + // NOI18N
                   (result == null ? "null" : result.getProcessName() + " " + result.getArguments())); // NOI18N
        }
        return result;
    }
    
    /** Setter for property browserExecutable.
     * @param browserExecutable New value of property browserExecutable.
     */
    public void setBrowserExecutable (NbProcessDescriptor browserExecutable) {
        if ((browserExecutable != null) && (!browserExecutable.equals(this.browserExecutable))) {
            NbProcessDescriptor oldVal = this.browserExecutable;
            this.browserExecutable = browserExecutable;
            pcs.firePropertyChange(PROP_BROWSER_EXECUTABLE, oldVal, browserExecutable);
        }
        if (browserExecutable == null) {
            NbProcessDescriptor oldVal = this.browserExecutable;
            this.browserExecutable = defaultBrowserExecutable();
            pcs.firePropertyChange(PROP_BROWSER_EXECUTABLE, oldVal, browserExecutable);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExtWebBrowser.PROP_BROWSER_EXECUTABLE)) {
            Object np = evt.getNewValue();
            if (np instanceof NbProcessDescriptor) {
                String processName = ((NbProcessDescriptor)np).getProcessName();
                if (err.isLoggable(Level.FINE)) {
                    err.log(Level.FINE, "" + System.currentTimeMillis() + "> propertychange: " + processName);
                }
                if ((processName != null) && (processName.trim().length() > 0)) {
                    if (processName.toUpperCase().indexOf("IEXPLORE.EXE") > -1) {       // NOI18N
                        setDDEServer(IEXPLORE);
                    } else if (processName.toUpperCase().indexOf("MOZILLA.EXE") > -1) { // NOI18N
                        setDDEServer(MOZILLA);
                    } else if (processName.toUpperCase().indexOf("FIREFOX.EXE") > -1) { // NOI18N
                        setDDEServer(FIREFOX);
                    } else if (processName.toUpperCase().indexOf("CHROME.EXE") > -1) { // NOI18N
                        setDDEServer(CHROME);
                    } else {
                        setDDEServer(null);
                    }
                }
            }
        }
    }

    /** Default command for browser execution.
     *  Can be overriden to return browser that suits to platform and settings.
     *
     * @return netscape without any argument.
     */
    protected NbProcessDescriptor defaultBrowserExecutable () {
        String b = "mozilla";  // NOI18N
        if (err.isLoggable(Level.FINE)) {
            err.log(Level.FINE, "" + System.currentTimeMillis() + "> ExtBrowser: defaultBrowserExecutable: ");
        }
        if (Utilities.isWindows()) {
            String params = "";                                         // NOI18N
            try {
                // finds HKEY_CLASSES_ROOT\\".html" and respective HKEY_CLASSES_ROOT\\<value>\\shell\\open\\command
                // we will ignore all params here
                b = NbDdeBrowserImpl.getDefaultOpenCommand ();
                String [] args = Utilities.parseParameters(b);

                if (args == null || args.length == 0) {
                    throw new NbBrowserException ();
                }
                b = args[0];
                if (args[0].toUpperCase().indexOf("IEXPLORE.EXE") > -1) {       // NOI18N
                    setDDEServer(IEXPLORE);
                    params = "-nohome ";                                         // NOI18N
                } else if (args[0].toUpperCase().indexOf("MOZILLA.EXE") > -1) { // NOI18N
                    setDDEServer(MOZILLA);
                } else if (args[0].toUpperCase().indexOf("FIREFOX.EXE") > -1) { // NOI18N
                    setDDEServer(FIREFOX);
                } else if (args[0].toUpperCase().indexOf("CHROME.EXE") > -1) { // NOI18N
                    setDDEServer(CHROME);
                }
                params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                return new NbProcessDescriptor(b, params);
                
            } catch (NbBrowserException e) {
                try {
                    b = NbDdeBrowserImpl.getBrowserPath("IEXPLORE"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(IEXPLORE);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }

                    b = NbDdeBrowserImpl.getBrowserPath("MOZILLA"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(MOZILLA);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }

                    b = NbDdeBrowserImpl.getBrowserPath("FIREFOX"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(FIREFOX);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }

                    b = NbDdeBrowserImpl.getBrowserPath("chrome"); // NOI18N
                    if ((b != null) && (b.trim().length() > 0)) {
                        setDDEServer(CHROME);
                        params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
                        return new NbProcessDescriptor(b, params);
                    }
                } catch (NbBrowserException e2) {
                    setDDEServer(IEXPLORE);
                    b = "C:\\Program Files\\Internet Explorer\\iexplore.exe";     // NOI18N            
                }
            } catch (UnsatisfiedLinkError e) {
                // someone is customizing this on non-Win platform
                b = "iexplore";     // NOI18N
            }
            params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
            return new NbProcessDescriptor (b, params);

        // Unix but not MacOSX
        } else if (Utilities.isUnix() && !Utilities.isMac()) {
            
            // Linux -> Mozilla should be default
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                b = "mozilla";                                                    // NOI18N
                java.io.File f = new java.io.File ("/usr/local/mozilla/mozilla"); // NOI18N
                if (f.exists()) {
                    b = f.getAbsolutePath();
                } else {
                    f = new java.io.File ("/usr/bin/firefox"); // NOI18N
                    if (f.exists()) {
                        b = f.getAbsolutePath();
                    }
                }
            // Solaris -> Netscape should be default
            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                b = "mozilla";                                                 // NOI18N
                java.io.File f = new java.io.File ("/usr/sfw/lib/mozilla"); // NOI18N
                if (f.exists()) {
                    b = f.getAbsolutePath();
                }
            }

            return new NbProcessDescriptor( b,
                "-remote \"openURL({" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "})\"", // NOI18N
                ExtWebBrowser.UnixBrowserFormat.getHint()
            );
            
        // OS/2
        } else if (Utilities.getOperatingSystem () == Utilities.OS_OS2) {
            return new NbProcessDescriptor(
                "Netscape.exe", // NOI18N
                // {URL}
                " {" + UnixBrowserFormat.TAG_URL + "}", // NOI18N
                ExtWebBrowser.UnixBrowserFormat.getHint()
            );
            
        // Mac OS
        } else if (Utilities.isMac()) {
            return new NbProcessDescriptor(
                "/usr/bin/open", // NOI18N
                // {URL}
                " {" + UnixBrowserFormat.TAG_URL + "}", // NOI18N
                ExtWebBrowser.UnixBrowserFormat.getHint()
            );
            
        // Other
        } else {
            return new NbProcessDescriptor(
                // empty string for process
                "", // NOI18N
                // {URL}
                " {" + UnixBrowserFormat.TAG_URL + "}", // NOI18N
                ExtWebBrowser.UnixBrowserFormat.getHint()
            );
        }
    }

    /**
     * Returns a new instance of BrowserImpl implementation.
     */
    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        return new DelegatingWebBrowserImpl(this);
    }

    /**
     * @param l new PropertyChangeListener */    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * @param l PropertyChangeListener to be removed */    
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener (l);
        }
    }
    
    private void readObject (java.io.ObjectInputStream ois) 
    throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        if (browserExecutable != null && browserExecutable.getArguments() != null) {
            // replace old {params} with {URL}
            String args = browserExecutable.getArguments();
            int idx = args.indexOf("{params}"); // NOI18N
            if (idx >= 0) {
                browserExecutable = new NbProcessDescriptor (
                    browserExecutable.getProcessName(),
                    args.substring(0, idx)+"-remote \"openURL({URL})"+args.substring(idx+8), // NOI18N
                    ExtWebBrowser.UnixBrowserFormat.getHint()
                );
            }
        }
        if (family == null) {
            if (this instanceof ChromeBrowser) {
                family = PrivateBrowserFamilyId.CHROME;
            } else if (this instanceof ChromiumBrowser) {
                family = PrivateBrowserFamilyId.CHROMIUM;
            } else if (this instanceof FirefoxBrowser) {
                family = PrivateBrowserFamilyId.FIREFOX;
            } else if (this instanceof IExplorerBrowser) {
                family = PrivateBrowserFamilyId.IE;
            } else if (this instanceof MozillaBrowser) {
                family = PrivateBrowserFamilyId.MOZILLA;
            } else if (this instanceof SafariBrowser) {
                family = PrivateBrowserFamilyId.SAFARI;
//            } else if (this instanceof OperaBrowser) {
//                family = PrivateBrowserFamilyId.OPERA;
            } else {
                family = PrivateBrowserFamilyId.UNKNOWN;
            }
        }
        init();
    }

    public PrivateBrowserFamilyId getPrivateBrowserFamilyId() {
        return family;
    }

    void setPrivateBrowserFamilyId(PrivateBrowserFamilyId family) {
        if (!this.family.equals(family)) {
            PrivateBrowserFamilyId oldVal = this.family;
            this.family = family;
            pcs.firePropertyChange(PROP_PRIVATE_BROWSER_FAMILY, oldVal, family);
        }
    }


    /** Default format that can format tags related to execution. 
     * Currently this is only the URL.
     */
    public static class UnixBrowserFormat extends org.openide.util.MapFormat {
        
        /** SVUID for serialization. */
        private static final long serialVersionUID = -699340388834127437L;
        
        /** Tag used to pass URL */
        public static final String TAG_URL = "URL"; // NOI18N

        /** Creates UnixBrowserFormat for URL.
         * @param url to specify URL
         */
        public UnixBrowserFormat (String url) {
            super(new java.util.HashMap ());
            java.util.Map map = getMap ();        
            map.put (TAG_URL, url);
        }
        
        public static String getHint() {
            return NbBundle.getMessage(ExtWebBrowser.class, "MSG_BrowserExecutorHint"); // NOI18N
        }

    }
    
}
