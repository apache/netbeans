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
package org.netbeans.modules.web.browser.api;

import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.core.HtmlBrowserComponent;
import org.netbeans.modules.web.browser.ui.DeveloperHtmlBrowserComponent;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Single opened browser tab. Methods on this class can be called from any thread
 * and opening browser URL or reloading a document will be re-posted into AWT
 * thread as necessary.
 */
public final class WebBrowserPane {

    private HtmlBrowser.Impl impl;
    private WebBrowserFactoryDescriptor descriptor;
    private final List<WebBrowserPaneListener> listeners = 
        new CopyOnWriteArrayList<WebBrowserPaneListener>();
    private PropertyChangeListener listener;
    private HtmlBrowserComponent topComponent;
    private boolean wrapEmbeddedBrowserInTopComponent;
    private boolean createTopComponent = false;
    private Lookup lastProjectContext = null;
    private WebBrowserFeatures features;
    private boolean open = false;
    
    WebBrowserPane(WebBrowserFeatures features, WebBrowserFactoryDescriptor desc,
            boolean wrapEmbeddedBrowserInTopComponent) 
    {
        this(features, desc, wrapEmbeddedBrowserInTopComponent, null);
    }
    
    private WebBrowserPane(WebBrowserFeatures features, WebBrowserFactoryDescriptor descriptor,
            boolean wrapEmbeddedBrowserInTopComponent, HtmlBrowserComponent comp) 
    {
        this.descriptor = descriptor;
        this.features = features;
        this.wrapEmbeddedBrowserInTopComponent = wrapEmbeddedBrowserInTopComponent;
        listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (HtmlBrowser.Impl.PROP_BROWSER_WAS_CLOSED.equals(
                        evt.getPropertyName())) 
                {
                    open = false;
                    firePaneClosed();
                }
                if (HtmlBrowser.Impl.PROP_URL.equals(evt.getPropertyName())) {
                    fireUrlChange();
                }
            }
        };
        /*
         * That's bad practice: HtmlBrowserComponent will create other impl 
         * to work with it internally. So there will be two different
         * not related impls with different Swing Components.
         * embedded = impl.getComponent() != null;
         */
        
        if (comp != null) {
            topComponent = comp;
        } 
        else {
            if (isEmbedded() && wrapEmbeddedBrowserInTopComponent) {
                // this needs to happen in AWT thread so let's do it later
                createTopComponent = true;
            }
            else {
                impl = descriptor.getFactory().createHtmlBrowserImpl();
                impl.addPropertyChangeListener(listener);
                if ( impl instanceof EnhancedBrowser ){
                    ((EnhancedBrowser) impl).initialize(features);
                }
            }
        }
    }
    
    /**
     * Close communication chanel (if there is any) between IDE and browser.
     * Closing browser window is an optional feature and it may or may not be
     * supported by current browser.
     * @param closeTab should the browser window be closed as well?
     */
    public void close(boolean closeTab) {
        if (impl != null) {
            if ( impl instanceof EnhancedBrowser ){
                ((EnhancedBrowser) impl).close(closeTab);
            }
        }
        open = false;
    }

    boolean isOpen() {
        return open;
    }
    
    private synchronized HtmlBrowserComponent getTopComponent() {
        if (topComponent == null && createTopComponent) {

            // below constructor sets some TopComponent properties and needs
            // to be therefore called in AWT thread:
            topComponent = new DeveloperHtmlBrowserComponent(descriptor.getFactory());
            topComponent.putClientProperty( "web.browser.pane", this ); //NOI18N
        }
        return topComponent;
    }

    /**
     * Is this embedded or external browser.
     */
    public boolean isEmbedded() {
        return descriptor.getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW;  // NOI18N
    }

    boolean canReloadPage() {
        if ( impl instanceof EnhancedBrowser ){
            return ((EnhancedBrowser) impl).canReloadPage();
        }
        return false;
    }

    boolean hasNetBeansIntegration() {
        return descriptor.hasNetBeansIntegration();
    }

    /**
     * Returns bare browser component. This method returns valid value only
     * when browser is embedded one and only if this pane was created via
     * WebBrowser.createNewBrowserPane(false). In all other cases it will 
     * always return null.
     */
    public Component getBrowserComponent() {
        if (isEmbedded() && !wrapEmbeddedBrowserInTopComponent) {
            return impl.getComponent();
        }
        return null;
    }
    
    /*
     * Access to the same browser pane as used by HtmlBrowser.UrlDisplayer class.
     */
//    public synchronized static WebBrowserPane getDefault() {
//        URLDisplayer u = URLDisplayer.getDefault();
//        if (!(u instanceof NbURLDisplayer)) {
//            return null;
//        }
//        NbURLDisplayer uu = (NbURLDisplayer)u;
//        HtmlBrowserComponent comp = uu.getInternalBrowserTopComponent();
//        if (comp == null) {
//            return null;
//        }
//        return new WebBrowserPane(comp);
//    }
    
    
    /**
     * This method shows given URL in *this* pane and should never result into
     * opening an additional browser window. Can be guaranteed only when NetBeans
     * plugins for external browsers are used or in case of embedded browser.
     */
    public void showURL(final URL u) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                HtmlBrowserComponent comp = getTopComponent();
                if (comp != null) {
                    comp.setURLAndOpen(u);
                    impl = topComponent.getBrowserImpl();
                    // initialize component with project context because 
                    // comp.setURLAndOpen() may have created a new browser instance
                    if ( impl instanceof EnhancedBrowser ){
                        ((EnhancedBrowser) impl).initialize(features);
                        ((EnhancedBrowser) impl).setProjectContext(lastProjectContext);
                    }
                    if ( impl!= null){
                        impl.addPropertyChangeListener(listener);
                    }
                } else {
                    impl.setURL(u);
                }
            }
        };
        open = true;
        if (SwingUtilities.isEventDispatchThread() || !isEmbedded()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    public void setProjectContext(Lookup projectContext) {
        lastProjectContext = projectContext;
        if ( impl instanceof EnhancedBrowser ){
            ((EnhancedBrowser) impl).setProjectContext(projectContext);
        }
    }

    public boolean ignoreChange(FileObject fo) {
        if (impl instanceof EnhancedBrowser) {
            return ((EnhancedBrowser)impl).ignoreChange(fo);
        }
        return false;
    }

    /**
     * Reload whatever is in this browser pane. Again can work reliable only
     * when NetBeans plugins for external browsers are used or in case of embedded browser.
     */
    public void reload() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (impl != null ) {
                    impl.reloadDocument();
                }
                else {
                    HtmlBrowserComponent comp = getTopComponent();
                    if (comp != null) {
                        impl = comp.getBrowserImpl();
                        if ( impl == null ){
                            comp.setURLAndOpen( comp.getDocumentURL());
                        }
                        else {
                            impl.reloadDocument();
                        }
                    }
                }
            }
        };
        open = true;
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    /**
     * Lookup associated with this browser pane.
     */
    public Lookup getLookup() {
        if ( impl == null ){
            return Lookup.EMPTY;
        }
        return impl.getLookup();
    }

    public void addListener(WebBrowserPaneListener l) {
        listeners.add(l);
    }

    public void removeListener(WebBrowserPaneListener l) {
        listeners.remove(l);
    }
    
    private void firePaneClosed() {
        for (WebBrowserPaneListener listener : listeners) {
            listener.browserEvent(new WebBrowserPaneWasClosedEvent(this));
        }
    }
    
    private void fireUrlChange() {
        for (WebBrowserPaneListener listener : listeners) {
            listener.browserEvent(new WebBrowserPaneURLChangedEvent(this));
        }
    }
    
    /**
     * Listener to browser pane events, eg. pane was closed.
     */
    public static interface WebBrowserPaneListener {

        void browserEvent(WebBrowserPaneEvent event);
    }

    /**
     * Marker interface for all browser events.
     */
    public abstract static class WebBrowserPaneEvent {

        private WebBrowserPane pane;

        private WebBrowserPaneEvent(WebBrowserPane pane) {
            this.pane = pane;
        }
        
        /**
         * Which pane was closed.
         */
        public WebBrowserPane getWebBrowserPane() {
            return pane;
        }
    }

    /**
     * Event notifying listeners that the pane was closed.
     */
    public static final class WebBrowserPaneWasClosedEvent extends WebBrowserPaneEvent {

        private WebBrowserPaneWasClosedEvent(WebBrowserPane pane) {
            super(pane);
        }

    }
    
    /**
     * Event notifying listeners that the pane URL has changed.
     */
    public static final class WebBrowserPaneURLChangedEvent extends WebBrowserPaneEvent {

        private WebBrowserPaneURLChangedEvent(WebBrowserPane pane) {
            super(pane);
        }

    }
    
}
