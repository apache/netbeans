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
package org.netbeans.core.browser.webview;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Session;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.WebKitUIManager;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * HTML browser implementation which uses embedded native browser component.
 *
 * @author S. Aubrecht
 */
public class HtmlBrowserImpl extends HtmlBrowser.Impl implements EnhancedBrowser {
    private static RequestProcessor RP = new RequestProcessor(HtmlBrowserImpl.class);

    private WebBrowser browser;
    private final Object LOCK = new Object();
    private Session session;
    private Lookup consoleLogger;
    private Lookup networkMonitor;
    private Lookup projectContext;
    private WebBrowserFeatures browserFeatures;
    
    public HtmlBrowserImpl() {
        super();
    }

    public HtmlBrowserImpl( WebBrowser browser ) {
        this.browser = browser;
    }

    @Override
    public Component getComponent() {
        return getBrowser().getComponent();
    }

    private WebBrowser getBrowser() {
        synchronized( LOCK ) {
            if( null == browser ) {
                browser = WebBrowserImplProvider.createBrowser();
                try {
                    browser.getComponent(); //#219304 - try to create the WebView component to check if binaries load fine
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable ex) {
                    Logger.getLogger( HtmlBrowserImpl.class.getName() ).log(Level.INFO, ex.getMessage(), ex);
                    browser = new NoWebBrowserImpl( ex.getMessage() );
                }
            }
            return browser;
        }
    }

    private EnhancedBrowser getEnhancedBrowser() {
        WebBrowser wb = getBrowser();
        if (wb instanceof EnhancedBrowser) {
            return (EnhancedBrowser)wb;
        }
        return null;
    }
    
    @Override
    public void reloadDocument() {
        init(null);
    }

    @Override
    public void stopLoading() {
        getBrowser().stopLoading();
    }

    @Override
    public Lookup getLookup() {
        return getBrowser().getLookup();
    }
        
    @Override
    public boolean ignoreChange(FileObject fo) {
        if (getEnhancedBrowser() != null) {
            return getEnhancedBrowser().ignoreChange(fo);
        }
        return BrowserSupport.ignoreChangeDefaultImpl(fo);
    }

    private boolean initialized = false;

    /**
     * Initialize and set browser to the url, or reload the page.
     * Called in event dispatch thread.
     * @param url The URL to set the browser to or <code>null</code> to reload
     * the page.
     */
    private void init(final String url) {
        if (initialized) {
            setBrowserTo(url);
            return;
        }
        if (browserFeatures == null || !browserFeatures.isNetBeansIntegrationEnabled()) {
            setBrowserTo(url);
            return;
        }
        initialized = true;

        // projectContext lookup contains Project instance if URL being opened is from a project

        getBrowser();
        final TransportImplementation transport = getLookup().lookup(TransportImplementation.class);
        if (url != null && transport instanceof TransportImplementationWithURLToLoad) {
            ((TransportImplementationWithURLToLoad) transport).setURLToLoad(url);
        }
        final WebKitDebugging webkitDebugger = getLookup().lookup(WebKitDebugging.class);

        if (webkitDebugger == null || projectContext == null) {
            setBrowserTo(url);
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                transport.attach();
                webkitDebugger.getDebugger().enable();
                if (browserFeatures.isJsDebuggerEnabled()) {
                    session = WebKitUIManager.getDefault().createDebuggingSession(webkitDebugger, projectContext);
                }
                if (browserFeatures.isConsoleLoggerEnabled()) {
                    consoleLogger = WebKitUIManager.getDefault().createBrowserConsoleLogger(webkitDebugger, projectContext);
                }
                if (browserFeatures.isNetworkMonitorEnabled()) {
                    networkMonitor = WebKitUIManager.getDefault().createNetworkMonitor(webkitDebugger, projectContext);
                }

                PageInspector inspector = PageInspector.getDefault();
                if (inspector != null && browserFeatures.isPageInspectorEnabled()) {
                    inspector.inspectPage(new ProxyLookup(getLookup(), projectContext, Lookups.fixed(HtmlBrowserImpl.this)));
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setBrowserTo(url);
                        if (url != null && transport instanceof TransportImplementationWithURLToLoad) {
                            // reset the URL to load, so that the URL is taken from the browser:
                            ((TransportImplementationWithURLToLoad) transport).setURLToLoad(null);
                        }
                    }
                });
            }
        });
    }
    
    private void setBrowserTo(String url) {
        if (url != null) {
            getBrowser().setURL(url);
        } else {
            getBrowser().reloadDocument();
        }
    }

    private void destroy() {
        if (!initialized) {
            return;
        }
        initialized = false;
        final TransportImplementation transport = getLookup().lookup(TransportImplementation.class);
        final WebKitDebugging webkitDebugger = getLookup().lookup(WebKitDebugging.class);
        final MessageDispatcherImpl dispatcher = getLookup().lookup(MessageDispatcherImpl.class);
        if (webkitDebugger == null) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (session != null) {
                    WebKitUIManager.getDefault().stopDebuggingSession(session);
                }
                session = null;
                if (consoleLogger != null) {
                    WebKitUIManager.getDefault().stopBrowserConsoleLogger(consoleLogger);
                }
                consoleLogger = null;
                if (networkMonitor != null) {
                    WebKitUIManager.getDefault().stopNetworkMonitor(networkMonitor);
                }
                networkMonitor = null;
                if (dispatcher != null) {
                    dispatcher.dispatchMessage(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, null);
                }
                if (webkitDebugger.getDebugger().isEnabled()) {
                    webkitDebugger.getDebugger().disable();
                }
                webkitDebugger.reset();
                transport.detach();
            }
        });
    }
    
    @Override
    public void setURL(final URL url) {
        init(url.toString());
    }

    @Override
    public URL getURL() {
        String strUrl = getBrowser().getURL();
        if( null == strUrl )
            return null;
        try {
            return new URL(strUrl);
        } catch( MalformedURLException ex ) {
            Logger.getLogger(HtmlBrowserImpl.class.getName()).log(Level.FINE, null, ex);
        }
        return null;
    }

    @Override
    public String getLocation() {
        return getBrowser().getURL();
    }

    @Override
    public void setLocation(String str) {
        getBrowser().setURL(str);
    }

    //see org.netbeans.modules.web.browser.ui.HtmlPreviewElement
    public void setContent( String htmlContent ) {
        getBrowser().setContent( htmlContent );
    }

    @Override
    public String getStatusMessage() {
        return getBrowser().getStatusMessage();
    }

    @Override
    public String getTitle() {
        return getBrowser().getTitle();
    }

    @Override
    public boolean isForward() {
        return getBrowser().isForward();
    }

    @Override
    public void forward() {
        getBrowser().forward();
    }

    @Override
    public boolean isBackward() {
        return getBrowser().isBackward();
    }

    @Override
    public void backward() {
        getBrowser().backward();
    }

    @Override
    public boolean isHistory() {
        return getBrowser().isHistory();
    }

    @Override
    public void showHistory() {
        getBrowser().showHistory();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getBrowser().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getBrowser().removePropertyChangeListener(l);
    }

    @Override
    public void dispose() {
        destroy();
        synchronized( LOCK ) {
            if( null != browser ) {
                browser.dispose();
            }
            browser = null;
        }
    }

    @Override
    public void initialize(WebBrowserFeatures browserFeatures) {
        this.browserFeatures = browserFeatures;
        if (getEnhancedBrowser() != null) {
            getEnhancedBrowser().initialize(browserFeatures);
        }
    }

    @Override
    public void close(boolean closeTab) {
        if (getEnhancedBrowser() != null) {
            getEnhancedBrowser().close(closeTab);
        }
        destroy();
        if (closeTab) {
            // TBD
        }
    }

    @Override
    public void setProjectContext(Lookup projectContext) {
        this.projectContext = projectContext;
        if (getEnhancedBrowser() != null) {
            getEnhancedBrowser().setProjectContext(projectContext);
        }
    }

    @Override
    public boolean canReloadPage() {
        return true;
    }
    
}
