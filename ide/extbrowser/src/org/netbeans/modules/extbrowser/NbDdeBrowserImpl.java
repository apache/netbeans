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

import java.awt.EventQueue;
import java.net.*;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import org.openide.util.Exceptions;


/**
 * Class that uses DDE to communicate with web browser through DDE.
 * Currently three browsers are supported:
 * <UL>
 * <LI>Netscape Navigator</LI>
 * <LI>Internet Explorer</LI>
 * <LI>Mozilla</LI>
 * </UL>
 *
 * <P>Limitations: Mozilla doesn't support WWW_Activate now
 * IE has different implementation on Win9x and on WinNT/Win2000. 
 * WWW_Activate creates always new window on Win9x so we don't use it.
 * Also it accepts only "0xFFFFFFFF" for WWW_Activate on WinNT/Win2K.
 *
 * <P>Documentation can be found 
 * <a href="http://developer.netscape.com/docs/manuals/communicator/DDE/ddevb.htm">
 * here</a>.
 *
 * @author  Radim Kubacki
 */
public class NbDdeBrowserImpl extends ExtBrowserImpl {

    /** DDE topic names */
    private static final String WWW_ACTIVATE      = "WWW_Activate";          // NOI18N
    private static final String WWW_OPEN_URL      = "WWW_OpenURL";           // NOI18N
    
    private static final String EXTBROWSER_DLL = "extbrowser"; //NOI18N
    private static final String EXTBROWSER_DLL_64BIT = "extbrowser64"; //NOI18N

    static {
        logFine("NbDdeBrowser: static initializer: "); // NOI18N
        try {
            if (org.openide.util.Utilities.isWindows()) {

                // should be 32 or 64 bit, but it may not be present on some jdks
                String sunDataModel = System.getProperty("sun.arch.data.model"); //NOI18N
                if (sunDataModel != null) {
                    if ("64".equals(sunDataModel)) { //NOI18N
                        System.loadLibrary(EXTBROWSER_DLL_64BIT);
                    } else {
                        System.loadLibrary(EXTBROWSER_DLL);
                    }
                } else {
                    String javaVMName = System.getProperty("java.vm.name"); //NOI18N
                    if ((javaVMName != null) && (javaVMName.contains("64"))) { //NOI18N
                        System.loadLibrary(EXTBROWSER_DLL_64BIT);
                    } else {
                        System.loadLibrary(EXTBROWSER_DLL);
                    }
                }

            }
        } catch (Exception e) {
            DialogDisplayer.getDefault ().notify (
                new NotifyDescriptor.Message(NbBundle.getMessage(NbDdeBrowserImpl.class, "ERR_cant_locate_dll"),
                NotifyDescriptor.INFORMATION_MESSAGE)
            );
        }
    }
            
    /** native thread that displays URLs */
    private static Thread nativeThread = null;
    
    /** runnable class that implements the work of nativeThread */
    private static NbDdeBrowserImpl.URLDisplayer nativeRunnable = null;
    
    /** Creates new NbDdeBrowserImpl
     * @param extBrowserFactory factory to use
     */
    public NbDdeBrowserImpl (ExtWebBrowser extBrowserFactory) {
        super ();
        this.extBrowserFactory = extBrowserFactory;
        logFine("NbDdeBrowserImpl created with factory:", extBrowserFactory); // NOI18N
    }
    
    private native byte [] reqDdeMessage (String srv, String topic, String item, int timeout) throws NbBrowserException;
    
    /** finds registry entry for browser opening */
    public static native String getBrowserPath (String browser) throws NbBrowserException;
    
    /** returns the command that executes default application for opening of 
     *  .html files
     */
    public static native String getDefaultOpenCommand() throws NbBrowserException;
    
    /** Sets current URL.
     *
     * @param url URL to show in the browser.
     */
    @Override
    protected void loadURLInBrowserInternal(URL url) {
        assert !EventQueue.isDispatchThread();
        logFine("NbDdeBrowserImpl.setUrl:", url); // NOI18N
        if (url == null) {
            return;
        }
        if (isInternetExplorer()) {
            if (nativeThread == null) {
                nativeRunnable = new NbDdeBrowserImpl.URLDisplayer ();
                nativeThread = new Thread(nativeRunnable, "URLdisplayer");   // NOI18N
                nativeThread.start ();
            }
            nativeRunnable.postTask (new DisplayTask (url, this));
        }
        else {
            NbProcessDescriptor np = extBrowserFactory.getBrowserExecutable();
            try {
                url = URLUtil.createExternalURL(url, false);
                String urlTxt;
                if (url == null) {
                    urlTxt = ""; // NOI18N;
                } else {
                    try {
                        urlTxt = url.toURI().toASCIIString();
                    } catch (URISyntaxException ex) {
                        logInfo(ex);
                        urlTxt = url.toString();
                    }
                }
                if (np != null) {
                    np.exec(new SimpleExtBrowser.BrowserFormat(urlTxt));
                }
            } catch (IOException ex) {
                logInfo(ex);
                BrowserUtils.notifyMissingBrowser(np.getProcessName());
            }
        }
    }
    
    private static void logInfo(Exception ex) {
        ExtWebBrowser.getEM().log(Level.INFO, null, ex);
    }

    private static void logFine(String message) {
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "{0} {1}", new Object[]{System.currentTimeMillis(), message});
        }
    }

    private static void logFine(String message, Object object) {
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "{0} {1} {2}", new Object[]{System.currentTimeMillis(), message, object});
        }
    }

    @Override
    protected PrivateBrowserFamilyId detectPrivateBrowserFamilyId(){
        PrivateBrowserFamilyId id = super.detectPrivateBrowserFamilyId();
        if (id == PrivateBrowserFamilyId.UNKNOWN){
            switch (realDDEServer()) {
                case ExtWebBrowser.FIREFOX: id = PrivateBrowserFamilyId.FIREFOX; break;
                case ExtWebBrowser.CHROME: id = PrivateBrowserFamilyId.CHROME; break;
                default: id = PrivateBrowserFamilyId.UNKNOWN;
            }
        }
        return id;
    }

    /** Finds the name of DDE server. 
     *  If <Default system browser> is set then it resolves it into either 
     *  Netscape or IExplore
     */
    private String realDDEServer () {
        logFine("NbDdeBrowserImpl.realDDEServer"); // NOI18N
        String srv = extBrowserFactory.getDDEServer ();
        if (srv != null) {
            return srv;
        }
        
        try {
            String cmd = getDefaultOpenCommand ();
            if (cmd != null) {
                cmd = cmd.toUpperCase();
                if (cmd.contains(ExtWebBrowser.IEXPLORE)) {
                    return ExtWebBrowser.IEXPLORE;
                }

                if (cmd.contains("CHROME")) { // NOI18N
                    return ExtWebBrowser.CHROME;
                }

                // check FF before Mozilla becuase FF is Mozilla but Mozills itself is old one
                if (cmd.contains(ExtWebBrowser.FIREFOX)) {
                    return ExtWebBrowser.FIREFOX;
                }
                
                if (cmd.contains(ExtWebBrowser.MOZILLA)) {
                    return ExtWebBrowser.MOZILLA;
                }
            }
        } catch (Exception ex) {
            // some problem in native code likely
            logInfo(ex);
        }
        // guess IE
        return ExtWebBrowser.IEXPLORE;
    }
    
    /** Getter for property activateTimeout.
     * @return Value of property activateTimeout.
     *
     */
    public int getActivateTimeout() {
        return extBrowserFactory.getActivateTimeout();
    }
        
    /** Getter for property openUrlTimeout.
     * @return Value of property openUrlTimeout.
     *
     */
    public int getOpenUrlTimeout() {
        return extBrowserFactory.getOpenurlTimeout();
    }

    private boolean isInternetExplorer() {
        return realDDEServer().equals(ExtWebBrowser.IEXPLORE);
    }
        
    /**
     * Singleton for doing all DDE operations.
     */
    static class URLDisplayer implements Runnable { // NOI18N

        private static final int ADDITIONAL_WAIT_TIMEOUT = 6000;
    
        /** FIFO of urls that should be displayed */
        Vector tasks;
        
        /** This is set to true during displaying of URL. 
         *  Used by Timer to interrupt displaying and print error message 
         */
        boolean isDisplaying = false;

        private URLDisplayer () {
            tasks = new Vector ();
        }
        
        private void postTask (DisplayTask task) {
            synchronized (this) {
                boolean shouldNotify = tasks.isEmpty ();
                tasks.add (task);
                if (shouldNotify) {
                    notifyAll();
                }
            }
        }
        
        /**
         * Returns next URL from queue that was posted for displaying.
         * This method blocks other processing until there is an request
         */
        private synchronized DisplayTask getNextTask() throws InterruptedException {
            do {
                
                if (!tasks.isEmpty ()) {
                    return (DisplayTask)tasks.remove(0);
                }
                wait();
                
            } while (true);
        }

        @NbBundle.Messages("NbDdeBrowserImpl.browser.external=external browser")
        @Override
        public void run() {
            logFine("NbDdeBrowserImpl.run"); // NOI18N
            while (true) {
                try {
                    /** url to be displayed */
                    DisplayTask task = getNextTask();
                    
                    isDisplaying = true;
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (isDisplaying) {
                                NbDdeBrowserImpl.nativeThread.interrupt();
                                logFine("interrupted in URLDisplayer.run.TimerTask.run()"); // NOI18N
                                BrowserUtils.notifyMissingBrowser(Bundle.NbDdeBrowserImpl_browser_external());
                            }
                        }
                    }, /*task.browser.extBrowserFactory.getBrowserStartTimeout() + */ADDITIONAL_WAIT_TIMEOUT);
                    dispatchURL (task);
                    timer.cancel();
                } catch (InterruptedException ex) {
                    ExtWebBrowser.getEM().log(Level.INFO, "interrupted in run(): " + ex);     // NOI18N
                    // do nothing
                } finally {
                    isDisplaying = false;
                }
            }
        }

        public void dispatchURL (DisplayTask task) {
            logFine("NbDdeBrowserImpl.dispatchURL:", task); // NOI18N
            try {                
                URL url = task.url;
                logFine("URLDispatcher.url:", url); // NOI18N
                
                // internal protocols cannot be displayed in external viewer
                url = URLUtil.createExternalURL(url, URLUtil.browserHandlesJarURLs(task.browser.realDDEServer()));   // XXX support Netscape too?

                logFine("url:", url); // NOI18N

                String urlStr = url.toString();
                
                boolean triedStart = false;
                final int MAX_URL_LENGTH = 199;
                
                if ((urlStr != null) && (urlStr.length() > MAX_URL_LENGTH)) {
                     urlStr = getFileUrl(urlStr);
                }

                logFine("urlstr:", urlStr); // NOI18N
                if (!win9xHack(task.browser.realDDEServer())) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage (NbDdeBrowserImpl.class, "MSG_activatingBrowser"));
                    try {
                        task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_ACTIVATE,"-1,0x0",task.browser.getActivateTimeout());
                    } catch (NbBrowserException ex) {
                        logFine("Exception, gonna start browser:", ex);
                        triedStart = true;
                        startBrowser(task.browser.extBrowserFactory.getBrowserExecutable(), urlStr);
                    }  
                }
                logFine("firstpart"); // NOI18N

                if (!triedStart) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_openingURLInBrowser", urlStr));
                    String args1 = "\""+urlStr+"\",,-1,0x1,,,";  // NOI18N

                    try {
                        Thread.sleep(500); // trying hack for bug #42438 - Browser executes twice which is a Mozilla bug
                        task.browser.reqDdeMessage(task.browser.realDDEServer(),WWW_OPEN_URL,args1,task.browser.getOpenUrlTimeout());
                    } catch (NbBrowserException ex) {
                        logFine("Restarting browser."); // NOI18N
                        startBrowser(task.browser.extBrowserFactory.getBrowserExecutable(), urlStr);
                    } catch (InterruptedException ex) {
                        // just ignore
                    }
                }
                logFine("secondpart"); // NOI18N

                task.browser.pcs.firePropertyChange(PROP_URL, task.browser.getURL(), url);

            } catch (final Exception ex) {
                logFine("Interrupted in URLDisplayer.dispatchURL.end"); // NOI18N
                Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_win_browser_invocation_failed"));
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        }
        
        
        /** 
         *
         */
        private String getFileUrl(String url) {
            logFine("Gonna get redirect file for long url:", url); // NOI18N
            String newurl = null;
            FileWriter fw = null;
            File f = null;
            
            int retries = 10;
            
            while ((f == null) && (retries > 0)) {
                retries--;
                try {
                    f = Files.createTempFile("extbrowser", ".html").toFile();             // NOI18N
                    logFine("file:", f); // NOI18N
                    if (f != null) { 
                        fw = new FileWriter(f);
                        if (f.canWrite()) {
                            String s1 = org.openide.util.NbBundle.getMessage(NbDdeBrowserImpl.class, "TXT_RedirectURL1");   //NOI18N
                            String s2 = org.openide.util.NbBundle.getMessage(NbDdeBrowserImpl.class, "TXT_RedirectURL2");   //NOI18N
                            String s = s1.concat(url).concat(s2);
                            fw.write(s);
                            fw.flush();
                        }
                        newurl = "file:/" + f.getAbsolutePath();                // NOI18N
                    }
                } catch (IOException ioe) {
                    logInfo(ioe);
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException ioe) {
                            logInfo(ioe);
                        }
                    }
                }                
            }
            if (newurl != null) { 
                logFine("New URL:", newurl); // NOI18N
                return newurl;
            }
            return url;
        }
        
        /**
         * Checks for IExplorer & Win9x combination.
         */
        private boolean win9xHack (String browser) {
            return browser.equals(ExtWebBrowser.IEXPLORE)
                   && (Utilities.getOperatingSystem() == Utilities.OS_WIN98 
                      ||  Utilities.getOperatingSystem() == Utilities.OS_WIN95);
        }

        /** 
         * Utility function that tries to start new browser process.
         *
         * It is used when WWW_Activate or WWW_OpenURL fail
         */
        private void startBrowser(NbProcessDescriptor cmd, String url) throws java.io.IOException {
            StatusDisplayer.getDefault ().setStatusText (NbBundle.getMessage(NbDdeBrowserImpl.class, "MSG_startingBrowser", url));
            cmd.exec(new ExtWebBrowser.UnixBrowserFormat(url));
        }
    }

    /** Encapsulating class for URL and browser that asks for its displaying */
    private static class DisplayTask {
        URL url;
        NbDdeBrowserImpl browser;
        
        DisplayTask (URL url, NbDdeBrowserImpl browser) {
            this.url = url;
            this.browser = browser;
        }
    }
}
