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

package org.netbeans.modules.extbrowser.chrome;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extbrowser.ExtBrowserImpl;
import org.netbeans.modules.extbrowser.PrivateBrowserFamilyId;
import org.netbeans.modules.extbrowser.plugins.*;
import org.netbeans.modules.extbrowser.plugins.ExternalBrowserPlugin.BrowserTabDescriptor;
import org.netbeans.modules.extbrowser.plugins.chrome.WebKitDebuggingTransport;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowserFeatures;
import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Browser implementation which uses Chrome plugin together with simple Chrome
 * browser support from extbrowser module. This browser not only implements
 * "Chrome with NetBeans Connector" browser but should be also used instead
 * of plain Chrome browser from extbrowser module in order to persist changes
 * done in Chrome's Developer Tools.
 */
public class ChromeBrowserImpl extends HtmlBrowser.Impl implements EnhancedBrowser {

    private static final Logger LOG = Logger.getLogger(ChromeBrowserImpl.class.getName());

    /** Lookup of this {@code HtmlBrowser.Impl}.  */
    private Lookup lookup;

    /** standart helper variable */
    protected PropertyChangeSupport pcs;

    /** requested URL */
    private URL url;
    protected String title = "";      // NOI18N

    private ExtBrowserImpl delegate;
    
    private BrowserTabDescriptor browserTabDescriptor = null;
    
    private boolean enhancedMode;

    private WebBrowserFeatures browserFeatures;
    private Lookup projectContext;

    private String newURL = null;
    
    /** Default constructor. 
      * <p>Builds PropertyChangeSupport. 
      */
    public ChromeBrowserImpl (ExtBrowserImpl delegate, boolean enhancedMode) {
        pcs = new PropertyChangeSupport (this);
        this.delegate = delegate;
        this.enhancedMode = enhancedMode;
    }

    public boolean hasEnhancedMode() {
        return enhancedMode || temporaryEnhancedMode;
    }

    private boolean temporaryEnhancedMode = false;
    public void setTemporaryEnhancedMode(boolean mode) {
        temporaryEnhancedMode = mode;
        lookup = null;
    }

    public boolean hasTemporaryEnhancedMode() {
        return temporaryEnhancedMode;
    }

    @Override
    public boolean ignoreChange(FileObject fo) {
        if (getBrowserTabDescriptor() != null && getBrowserTabDescriptor().isInitialized()) {
            return BrowserSupport.ignoreChangeDefaultImpl(fo);
        }
        return false;
    }

    @Override
    public void initialize(WebBrowserFeatures browserFeatures) {
        this.browserFeatures = browserFeatures;
    }

    public WebBrowserFeatures getBrowserFeatures() {
        return browserFeatures;
    }

    @Override
    public boolean canReloadPage() {
        return getBrowserTabDescriptor() != null;
    }

    private Lookup createLookup() {
        List<Lookup> lookups = new ArrayList<Lookup>();
        if (hasEnhancedMode()) {
            lookups.add(Lookups.fixed(
                        new MessageDispatcherImpl(),
                        new RemoteScriptExecutor(this),
                        new PageInspectionHandleImpl(this)
                    ));
            WebKitDebuggingTransport transport = new WebKitDebuggingTransport(
                    this);
            lookups.add(Lookups.fixed(transport,
                    Factory.createWebKitDebugging(transport)));
        }
        return new ProxyLookup(lookups.toArray(new Lookup[0]));
    }
    
    /** Dummy implementations */
    @Override
    public boolean isBackward() { return false; }
    @Override
    public boolean isForward() { return false; }
    @Override
    public void backward() { }
    @Override
    public void forward() { }
    @Override
    public boolean isHistory() { return false; }
    @Override
    public void showHistory() {}
    @Override
    public void stopLoading() { }
    
    protected void setTitle (String title) {
    }
    
    @Override
    public String getTitle() {
        return "";
    }

    
    /** Returns status message representing status of html browser.
     *
     * @return status message.
     */
    @Override
    public String getStatusMessage() {
        return "";
    }
        
    /** Call setURL again to force reloading.
     * Browser must be set to reload document and do not cache them.
     */
    @Override
    public void reloadDocument() {
        if (url == null) {
            return;
        }
        BrowserTabDescriptor tab = getBrowserTabDescriptor();
        if (tab != null) {
            URL u = url;
            // if user navigated to a different URL then reload the new URL
            // for example going from .../index.html to .../index.html#page2
            // must reload .../index.html#page2
            if (newURL != null) {
                try {
                    URL u2 = new URL(newURL);
                    // use new URL only if the hostname and port are the same
                    if (u2.getAuthority() != null && u2.getAuthority().equals(u.getAuthority())) {
                        u = u2;
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            ExternalBrowserPlugin.getInstance().showURLInTab(tab, u);
        } else if (!hasEnhancedMode()) {
            setURL(url);
        }
    }
        
    
    /** Returns current URL.
     *
     * @return current URL.
     */
    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public void close(boolean closeTab) {
        BrowserTabDescriptor tab = getBrowserTabDescriptor();
        if (tab != null) {
            ExternalBrowserPlugin.getInstance().close(tab, closeTab);
        }
    }

    @Override
    public void setProjectContext(Lookup projectContext) {
        this.projectContext = projectContext;
    }

    public Lookup getProjectContext() {
        return projectContext;
    }
    
    /** 
     *  Sets current URL. Descendants of this class will implement it and they can call this
     *  to display internal resources.
     *
     * @param url URL to show in the browser.
     */
    @Override
    public void setURL(final URL url) {
        newURL = null;
        assert delegate.getPrivateBrowserFamilyId() == PrivateBrowserFamilyId.CHROMIUM ||
                delegate.getPrivateBrowserFamilyId() == PrivateBrowserFamilyId.CHROME :
                "wrong browser: "+delegate+" "+delegate.getPrivateBrowserFamilyId();
        BrowserFamilyId pluginId = delegate.getPrivateBrowserFamilyId() == PrivateBrowserFamilyId.CHROMIUM ? BrowserFamilyId.CHROMIUM : BrowserFamilyId.CHROME;
        ExtensionManager.ExtensitionStatus status = ExtensionManager.isInstalled(pluginId);
        BrowserTabDescriptor tab = getBrowserTabDescriptor();
        if (hasEnhancedMode()) {
            if (tab == null) {
                boolean browserPluginAvailable = true;
                if (status == ExtensionManager.ExtensitionStatus.DISABLED) {
                    browserPluginAvailable = false;
                } else if (status == ExtensionManager.ExtensitionStatus.MISSING || 
                        status == ExtensionManager.ExtensitionStatus.NEEDS_UPGRADE) {
                    browserPluginAvailable = ExtensionManager.installExtension(pluginId,
                             status);
                }
                if (browserPluginAvailable) {
                    if (ExternalBrowserPlugin.getInstance().isServerRunning()) {
                        // instead of using real URL to open a new tab in the browser
                        // (and possible start browser process itself) I'm going to use
                        // a temp file which I will refresh with the real URL once the
                        // link between browser and IDE was established. The reason is
                        // that I would like to be able to set breakpoints to the browser
                        // tab before the URL is loaded so that breakpoints get hit
                        // even when the URL is loaded for the first time.
                        URL tempUrl = createBlankHTMLPage();
                        assert tempUrl != null;
                        ExternalBrowserPlugin.getInstance().register(tempUrl, url, this);
                        delegate.setURL(tempUrl);
                    } else {
                        delegate.setURL(url);
                    }
                }
            }
            else {
                tab.reEnableReInitialization();
                ExternalBrowserPlugin.getInstance().showURLInTab(tab, url);
            }
        }
        else {
            
            // enforce ExternalBrowserPlugin server initialization even when page
            // is opened without NB integration:
            ExternalBrowserPlugin.getInstance();

            if (status == ExtensionManager.ExtensitionStatus.INSTALLED) {
                if (tab == null) {
                    ExternalBrowserPlugin.getInstance().register(url, url, this);
                    delegate.setURL(url);
                } else {
                    ExternalBrowserPlugin.getInstance().showURLInTab(tab, url);
                }
            } else {
                delegate.setURL(url);
            }
        }
        this.url = url;
    }

    private URL createBlankHTMLPage() {
        try {
            Path tempPath = Files.createTempFile("blank", ".html");
            try(OutputStream os = Files.newOutputStream(tempPath);
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                osw.write("<html :netbeans_temporary=\"true\"></html>");
            }
            // The call to toRealPath ensures, that symlinks are resolved. It
            // was observed, that chrome on macOS reports the file url with
            // symlinks resolved, so align with that.
            return tempPath.toRealPath().toUri().toURL();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Failed to create blank page for chrome", ex);
        }
        return null;
    }
    
    /** Returns visual component of html browser.
     *
     * @return visual component of html browser.
     */
    @Override
    public final java.awt.Component getComponent() {
        return null;
    }

    /** Adds PropertyChangeListener to this browser.
     *
     * @param l Listener to add.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /** Removes PropertyChangeListener from this browser.
     *
     * @param l Listener to remove.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    public final Lookup getLookup() {
        if (lookup == null) {
            lookup = createLookup();
        }
        return lookup;
    }

    public void wasClosed() {
        setBrowserTabDescriptor(null);
        url = null;
        pcs.firePropertyChange(HtmlBrowser.Impl.PROP_BROWSER_WAS_CLOSED, null, null);
    }

    public synchronized BrowserTabDescriptor getBrowserTabDescriptor() {
        return browserTabDescriptor;
    }

    public synchronized void setBrowserTabDescriptor(BrowserTabDescriptor browserTabDescriptor) {
        this.browserTabDescriptor = browserTabDescriptor;
    }

    public void urlHasChanged(String newURL) {
        this.newURL = newURL;
        pcs.firePropertyChange(HtmlBrowser.Impl.PROP_URL, null, null);
    }

}
