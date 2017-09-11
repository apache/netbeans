/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
