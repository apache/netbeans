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
package org.netbeans.modules.web.browser.api;

import org.netbeans.modules.web.common.api.DependentFileQuery;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.Helper;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Helper class to be added to project's lookup and to be used to open URLs from
 * project. It keeps association between URL opened in browser and project's file.
 * According to results of DependentFileQuery it can answer whether project's file
 * change should result into refresh of URL in the browser. It also keeps 
 * WebBrowserPane instance to open URLs in. Such browser pane can be single global one
 * shared with HtmlBrowser.URLDisaplyer (ie. getGlobalSharedOne method) or single
 * global one owned by BrowserSupport (ie. getDefault method) or per project
 * pane (ie. getProjectScoped method).
 */
public final class BrowserSupport {
    
    private WebBrowserPane pane;
    
    private URL currentURL;
    private WebBrowser browser;
    private PropertyChangeListener listener;
    private FileObject file;

    private static BrowserSupport INSTANCE = create();
    
    private static BrowserSupport INSTANCE_EMBEDDED;
    
    /**
     * Returns instance of BrowserSupport which shares WebBrowserPane
     * with HtmlSupport.URLDisplayer. That means that opening a URL via
     * BrowserSupport.load() or via HtmlSupport.URLDisaplayer.show() will
     * results into URL being opened in the same browser pane.
     */
    public static synchronized BrowserSupport getGlobalSharedOne() {
        // XXX: to implement this I need to hack in NbDisplayerURL
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    /**
     * Returns singleton instance of BrowserSupport with its own WebBrowserPane.
     * Using this instance means that all URLs opened with BrowserSupport will 
     * have its own browser pane and all URLs opened via HtmlSupport.URLDisplayer
     * will have its own browser pane as well. The browser used to open URLs is
     * always the one configured in IDE Options.
     */
    public static BrowserSupport getDefault() {
        return INSTANCE;
    }

    /**
     * Creates a new instance of BrowserSupport which will always use browser
     * according to IDE Options.
     */
    public static BrowserSupport create() {
        return new BrowserSupport();
    }
    
    /**
     * Creates a new instance of BrowserSupport for given browser.
     */
    public static BrowserSupport create(WebBrowser browser) {
        return new BrowserSupport(browser);
    }
    
    public static BrowserSupport getDefaultEmbedded() {
        if (INSTANCE_EMBEDDED == null) {
            WebBrowser browser = WebBrowsers.getInstance().getEmbedded();
            if (browser != null) {
                INSTANCE_EMBEDDED = create(browser);
            }
        }
        return INSTANCE_EMBEDDED;
    }
    
    /**
     * Use browser from IDE settings and change browser pane whenever default
     * browser changes in IDE options.
     */
    private BrowserSupport() {
        this(null);
    }
    
    private BrowserSupport(WebBrowser browser) {
        this.browser = browser;
    }
    
    private synchronized WebBrowserPane getWebBrowserPane() {
        if (pane == null) {
            if (browser == null) {
                browser = WebBrowsers.getInstance().getPreferred();
                listener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        synchronized ( BrowserSupport.this ) {
                            if (WebBrowsers.PROP_DEFAULT_BROWSER.equals(evt
                                    .getPropertyName()))
                            {
                                if (!WebBrowsers.getInstance().getPreferred()
                                        .getId().equals(browser.getId()))
                                {
                                    // update browser pane
                                    browser = WebBrowsers.getInstance()
                                            .getPreferred();
                                    if ( pane!= null ){
                                        // pane could be null because of the following code
                                        pane = null;
                                    }
                                }
                            }
                        }
                    }
                };
                WebBrowsers.getInstance().addPropertyChangeListener(listener);
            }
            pane = browser.createNewBrowserPane(true);
        }
        return pane;
    }
    
    /**
     * Opens URL in a browser pane associated with this BrowserSupport.
     * FileObject param is "context" associated with URL being opened. It can be 
     * file which is being opened in the browser or  project folder in case of URL being result
     * of project execution. If browser pane does not support concept of reloading it will simply 
     * open a tab with this URL on each execution.
     * 
     */
    public void load(URL url, FileObject context) {
        WebBrowserPane wbp = getWebBrowserPane();
        file = context;
        currentURL = url;
        Project project = FileOwnerQuery.getOwner(context);
        Lookup lkp = Lookup.EMPTY;
        if( null != project ) {
            DataObject dob = null;
            try {
                dob = DataObject.find( file );
            } catch( DataObjectNotFoundException ex ) {
                //ignore
            }
            lkp = null == dob ? Lookups.fixed( project, context, browser.getBrowserFamily() ) :
                    Lookups.fixed( project, context, dob, browser.getBrowserFamily() );
        }
        wbp.setProjectContext(lkp);
        wbp.showURL(url);
    }
    
    /**
     * The same behaviour as load() method but file object context is not necessary
     * to be passed again.
     */
    public boolean reload(URL url) {
        if (!canReload(url)) {
            return false;
        }
        getWebBrowserPane().reload();
        return true;
    }
    
    /**
     * Reloads the current URL if it is set.
     * @return {@code true} if the browser is reloaded, {@code false} otherwise
     * @since 1.10
     */
    public boolean reload() {
        if (currentURL == null) {
            return false;
        }
        getWebBrowserPane().reload();
        return true;
    }

    /**
     * Does this browser supports page reload?
     */
    public boolean canReload() {
        return getWebBrowserPane().canReloadPage();
    }

    /**
     * Has this URL being previous opened via load() method or not? BrowserSupport
     * remember last URL opened.
     */
    public boolean canReload(URL url) {
        return currentURL != null && currentURL.equals(url) && !url.toExternalForm().equals(Helper.urlBeingRefreshedFromBrowser.get()) &&
                getWebBrowserPane().canReloadPage();
    }

    /**
     * Some file types should not be refresh upon save in case of some browsers.
     * For example CSS are handled directly by CSS support in case of "Chrome with
     * NetBeans Connector" browser.
     */
    public boolean ignoreChange(FileObject fo) {
        return getWebBrowserPane().ignoreChange(fo);
    }

    public static boolean ignoreChangeDefaultImpl(FileObject fo) {
        // #217284 - ignore changes in CSS
        return fo.hasExt("css");
    }

    /**
     * Returns URL which was opened in the browser and which was associated with
     * given FileObject. That is calling load(URL, FileObject) creates mapping 
     * between FileObject in IDE side and URL in browser side and this method 
     * allows to use the mapping to retrieve URL.
     * 
     * If checkDependentFiles parameter is set to true then 
     * DependentFileQuery.isDependent will be consulted to check whether URL opened
     * in the browser does not depend on given FileObject. If answer is yes than
     * any change in this FileObject should be reflected in browser and URL
     * should be refreshed in browser.
     */
    public URL getBrowserURL(FileObject fo, boolean checkDependentFiles) {
        Project project = FileOwnerQuery.getOwner(fo);
        if (file == null || currentURL == null) {
            return null;
        }
        if (checkDependentFiles) {
            if ( file.equals( project.getProjectDirectory() ) || DependentFileQuery.isDependent(file, fo)) {
                // Two cases :
                // - a project was "Run" and we have no idea which exact project's 
                //   file was opened in browser;
                //   because "fo" belongs to the project we could say
                //   that URL corresponding for this fo is project's URL;
                //   let's first check other opened browsers for better match and
                //   if nothing better is found we can return project's URL
                // - <code>file</code> depends on <code>fo</code>
                return currentURL;
            }
        } 
        if (fo.equals(file)) {
            return currentURL;
        }
        return null;
    }

    /**
     * Close communication chanel (if there is any) between IDE and browser.
     * Closing browser window is an optional feature and it may or may not be
     * supported by current browser.
     * @param closeTab should the browser window be closed as well?
     */
    public void close(boolean closeTab) {
        getWebBrowserPane().close(closeTab);
    }

    /**
     * Returns true after browser pane was opened and until it is closed by user.
     * @since 1.34
     */
    public synchronized boolean isWebBrowserPaneOpen() {
        WebBrowserPane p = pane;
        return p != null && p.isOpen();
    }
}
