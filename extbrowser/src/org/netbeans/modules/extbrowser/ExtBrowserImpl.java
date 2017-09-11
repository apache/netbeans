/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    /** Lookup of this {@code HtmlBrowser.Impl}.  */
    private Lookup lookup;

    /** standart helper variable */
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
    
    protected void setTitle (String title) {
        return;
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
        RP.post(new Runnable() {
            @Override
            public void run() {
                loadURLInBrowserInternal(url);
            }
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

    public final Lookup getLookup() {
        return Lookup.EMPTY;
    }

}
