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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * The ExtBrowserImpl is generalized external browser.
 *
 * @author Radim Kubacki
 */
public abstract class ExtBrowserImpl extends HtmlBrowser.Impl {

    private static final RequestProcessor RP = new RequestProcessor(ExtBrowserImpl.class);

    /** standard helper variable */
    protected PropertyChangeSupport pcs;

    /** requested URL */
    private URL url;
    protected String title = "";      // NOI18N

    /** reference to a factory to get settings */
    protected ExtWebBrowser extBrowserFactory;
    
    /** Default constructor. 
      * <p>Builds PropertyChangeSupport. 
      */
    public ExtBrowserImpl () {
        pcs = new PropertyChangeSupport (this);
    }
    
    public PrivateBrowserFamilyId getPrivateBrowserFamilyId() {
        return extBrowserFactory.getPrivateBrowserFamilyId();
    }
    

    /** This method will be always run on background thread as detection can
         take a while for System Default browser. The method will be called only once.*/
    protected PrivateBrowserFamilyId detectPrivateBrowserFamilyId(){
        return PrivateBrowserFamilyId.UNKNOWN;
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
    
    protected void setTitle (String title) { }
    
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
        setURL(url);
    }
        
    
    /** Returns current URL.
     *
     * @return current URL.
     */
    @Override
    public URL getURL() {
        return url;
    }

    /** 
     *  Sets current URL. Descendants of this class will implement it and they can call this
     *  to display internal resources.
     *
     * @param url URL to show in the browser.
     */
    @Override
    public void setURL(final URL url) {
        loadURLInBrowser(url);
        this.url = url;
    }

    protected final void loadURLInBrowser(final URL url) {
        RP.post(() -> {
            loadURLInBrowserInternal(url);
        });
    }

    /**
     * Loads given URL in the browser.
     * <p>
     * This method is always called in a background thread.
     * @param url URL to be loaded
     * @since 1.46
     */
    protected abstract void loadURLInBrowserInternal(URL url);

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

    @Override
    public final Lookup getLookup() {
        return Lookup.EMPTY;
    }

}
