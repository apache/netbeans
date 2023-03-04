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

package org.netbeans.core.browser.api;

import java.beans.PropertyChangeListener;
import java.util.Map;
import org.openide.util.Lookup;
import org.w3c.dom.Document;

/**
 * Embedded browser
 *
 * @author S. Aubrecht
 */
public abstract class WebBrowser {
    
    /** The name of property representing status of html browser. */
    public static final String PROP_STATUS_MESSAGE = "statusMessage"; // NOI18N

    /** The name of property representing current URL. */
    public static final String PROP_URL = "url"; // NOI18N

    /** Title property */
    public static final String PROP_TITLE = "title"; // NOI18N

    /** forward property */
    public static final String PROP_FORWARD = "forward"; // NOI18N

    /** backward property name */
    public static final String PROP_BACKWARD = "backward"; // NOI18N

    /** history property name */
    public static final String PROP_HISTORY = "history"; // NOI18N

    /** loading property name */
    public static final String PROP_LOADING = "loading"; // NOI18N

    /**
     * Returns visual component of html browser, it doesn't include any toolbars
     * nor status bars.
     *
     * @return visual component of html browser.
     */
    public abstract java.awt.Component getComponent();

    /**
    * Reloads current html page.
    */
    public abstract void reloadDocument();

    /**
    * Stops loading of current html page.
    */
    public abstract void stopLoading();

    /**
     * Load given url which doesn't have to be a valid URL, e.g. "about:config"
     * @param url Url to load
     */
    public abstract void setURL(String url);

    /**
    * Returns current URL.
    *
    * @return current URL.
    */
    public abstract String getURL();

    /**
    * Returns status message representing status of html browser.
    *
    * @return status message.
    */
    public abstract String getStatusMessage();

    /** Returns title of the displayed page.
    * @return title
    */
    public abstract String getTitle();

    /** Is forward button enabled?
    * @return true if it is
    */
    public abstract boolean isForward();

    /** Moves the browser forward. Failure is ignored.
    */
    public abstract void forward();

    /** Is backward button enabled?
    * @return true if it is
    */
    public abstract boolean isBackward();

    /** Moves the browser forward. Failure is ignored.
    */
    public abstract void backward();

    /** Is history button enabled?
    * @return true if it is
    */
    public abstract boolean isHistory();

    /** Invoked when the history button is pressed.
    */
    public abstract void showHistory();

    /**
    * Adds PropertyChangeListener to this browser.
    *
    * @param l Listener to add.
    */
    public abstract void addPropertyChangeListener(PropertyChangeListener l);

    /**
    * Removes PropertyChangeListener from this browser.
    *
    * @param l Listener to remove.
    */
    public abstract void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Show given content in the browser
     * @param content Content to show
     */
    public abstract void setContent( String content );

    /**
     *
     * @return Current browser content. It's a live document, any modifications
     * to the document are reflected in browser window.
     */
    public abstract Document getDocument();

    /**
     * Invoke this method when the browser component has been removed from Swing
     * hierarchy and will be no longer needed.
     */
    public abstract void dispose();

    //advanced

    /**
     * Listen to various browser events
     * @param l
     */
    public abstract void addWebBrowserListener( WebBrowserListener l );

    /**
     * Remove listener
     * @param l
     */
    public abstract void removeWebBrowserListener( WebBrowserListener l );

    /**
     *
     * @param domain
     * @param name
     * @param path
     * @return Cookie with given name and domain or null.
     */
    public abstract Map<String,String> getCookie( String domain, String name, String path );

    /**
     * Deletes a cookie
     * @param domain
     * @param name
     * @param path
     */
    public abstract void deleteCookie( String domain, String name, String path );

    /**
     * Add a cookie
     * @param cookie
     */
    public abstract void addCookie( Map<String,String> cookie );

    /**
     * Execute JavaScript
     * @param script
     */
    public abstract Object executeJavaScript( String script );

    public abstract Lookup getLookup();
    
}
