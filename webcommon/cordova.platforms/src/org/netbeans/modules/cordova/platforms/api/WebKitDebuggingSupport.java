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
package org.netbeans.modules.cordova.platforms.api;

import java.awt.Image;
import java.net.URL;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.browser.spi.MessageDispatcher;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebServer;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.WebKitUIManager;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.api.progress.*;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public final class WebKitDebuggingSupport {

    private static WebKitDebuggingSupport instance;
    
    private Session debuggerSession;
    private Lookup consoleLogger;
    private Lookup networkMonitor;
    private WebKitDebugging webKitDebugging;
    private MobileDebugTransport transport;
    private MessageDispatcherImpl dispatcher;
    private final RequestProcessor RP = new RequestProcessor(WebKitDebuggingSupport.class.getName(), 10);
    private volatile boolean startDebuggingInProgress = true;
    
    public static synchronized WebKitDebuggingSupport getDefault() {
        if (instance == null) {
            instance = new WebKitDebuggingSupport();
        }
        return instance;
    }
    
    @NbBundle.Messages({
        "LBL_ConnectingIOSDevice=Connecting to iOS Device...",
        "LBL_ConnectionIOSSimulator=Connecting to iOS Simulator...",
        "LBL_ConnectionAndroidDevice=Connecting to Android Device...",
        "LBL_ConnectionAndroidEmulator=Connecting to Android Emulator...",})
    private static String getProgressLabel(Device dev) {
        if (PlatformManager.IOS_TYPE.equals(dev.getPlatform().getType())) {
            return dev.isEmulator() ? Bundle.LBL_ConnectionIOSSimulator() : Bundle.LBL_ConnectingIOSDevice();
        } else {
            return dev.isEmulator() ? Bundle.LBL_ConnectionAndroidEmulator() : Bundle.LBL_ConnectionAndroidDevice();
        }

    }

    public synchronized void startDebugging(Device device, Project p, Lookup context, boolean navigateToUrl) {
        if (transport != null || webKitDebugging != null) {
            //stop old session
            stopDebuggingNow(false);
        }
        ProgressHandle handle = ProgressHandle.createHandle(getProgressLabel(device), new Cancellable() {
            @Override
            public boolean cancel() {
                stopDebugging(true);
                return true;
            }
        });
        handle.start();
        startDebuggingInProgress = true;
        try {
            transport = device.getDebugTransport();
            final String url = getUrl(p, context);
            transport.setBaseUrl(url);
            if (url == null) {
                //phonegap
                String id = context.lookup(String.class);
                transport.setBundleIdentifier(id);
                BrowserURLMapperImplementation.BrowserURLMapper mapper = context.lookup(BrowserURLMapperImplementation.BrowserURLMapper.class);
                transport.setBrowserURLMapper(mapper);
            }
            boolean attached = transport.attach();
            if (!attached) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (transport == null) {
                return;
            }
            webKitDebugging = Factory.createWebKitDebugging(transport);
            if (navigateToUrl) {
                webKitDebugging.getPage().navigate(url);
            }
            webKitDebugging.getDebugger().enable();
            Lookup projectContext = Lookups.singleton(p);
            debuggerSession = WebKitUIManager.getDefault().createDebuggingSession(webKitDebugging, projectContext);
            consoleLogger = WebKitUIManager.getDefault().createBrowserConsoleLogger(webKitDebugging, projectContext);
            networkMonitor = WebKitUIManager.getDefault().createNetworkMonitor(webKitDebugging, projectContext);
            dispatcher = new MessageDispatcherImpl();
            BrowserFamilyId browserFamilyId = context.lookup(BrowserFamilyId.class);
            if (browserFamilyId == null) {
                browserFamilyId = BrowserFamilyId.UNKNOWN;
            }
            PageInspector.getDefault().inspectPage(Lookups.fixed(
                    webKitDebugging,
                    p,
                    context.lookup(Image.class),
                    browserFamilyId,
                    dispatcher));
        } finally {
            startDebuggingInProgress = false;
            handle.finish();
        }
    }
    
    public void stopDebugging(final boolean fullCleanup) {
        if (transport != null && fullCleanup) {
            transport.flush();
            if (webKitDebugging==null) {
                transport.detach();
                transport = null;
            } 
        }
        
        if (startDebuggingInProgress && !fullCleanup)
            return;
        RP.post(new Runnable() {
            @Override
            public void run() {
                stopDebuggingNow(fullCleanup);
            }
        });
    }
    
    private synchronized void stopDebuggingNow(boolean fullCleanup) {
        if (webKitDebugging == null || webKitDebugging == null) {
            return;
        }
            if (debuggerSession != null) {
                WebKitUIManager.getDefault().stopDebuggingSession(debuggerSession);
            }
            debuggerSession = null;
            if (consoleLogger != null) {
                WebKitUIManager.getDefault().stopBrowserConsoleLogger(consoleLogger);
            }
            consoleLogger = null;
            if (networkMonitor != null) {
                WebKitUIManager.getDefault().stopNetworkMonitor(networkMonitor);
            }
            networkMonitor = null;
            if (webKitDebugging.getDebugger().isEnabled()) {
                webKitDebugging.getDebugger().disable();
            }
            dispatcher.dispose();
            if (fullCleanup) {
                // Perform a dummy blocking call that ensures that the previous
                // calls are processed before we continue and detach the transport.
                webKitDebugging.getRuntime().evaluate("0"); // NOI18N
            }
            webKitDebugging.reset();
            if (transport!=null) {
                transport.detach();
                transport = null;
            }
        webKitDebugging = null;
    }
    

    public void reload() {
        RP.post(new Runnable() {

            @Override
            public void run() {
                if (webKitDebugging != null) {
                    webKitDebugging.getPage().reload(true, null);
                }
            }
            
        });
    }
    
    public FileObject getFile(Project p, Lookup context) {
        DataObject dObject = context.lookup(DataObject.class);
        FileObject fileObject = dObject==null?ClientProjectUtilities.getStartFile(p):dObject.getPrimaryFile();
        return fileObject;
    }
    
    
    private String getUrl(Project p) {
        return getUrl(p, Lookup.EMPTY);
    }
    
    public String getUrl(Project p, Lookup context) {
        if (ClientProjectUtilities.isUsingEmbeddedServer(p)) {
            WebServer.getWebserver().start(p, ClientProjectUtilities.getSiteRoot(p), ClientProjectUtilities.getWebContextRoot(p));
        } else {
            WebServer.getWebserver().stop(p);
        }

        if (context == null || context.lookup(BrowserURLMapperImplementation.BrowserURLMapper.class)!=null) {
            return null;
        }
        URL url = context.lookup(URL.class);
        if (url!=null) {
            //TODO: hack to workaround #221791
            return url.toExternalForm().replace("localhost", WebUtils.getLocalhostInetAddress().getHostAddress()); // NOI18N
        }

        DataObject dObject = context.lookup(DataObject.class);
        FileObject fileObject = dObject == null ? ClientProjectUtilities.getStartFile(p) : dObject.getPrimaryFile();
        if (fileObject == null) {
            return null;
        }
        //TODO: hack to workaround #221791
        return ServerURLMapping.toServer(p, fileObject).toExternalForm().replace("localhost", WebUtils.getLocalhostInetAddress().getHostAddress()); // NOI18N
    }

    static class MessageDispatcherImpl extends MessageDispatcher {
        void dispose() {
            dispatchMessage(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, null);
        }
    }
    
    
}
