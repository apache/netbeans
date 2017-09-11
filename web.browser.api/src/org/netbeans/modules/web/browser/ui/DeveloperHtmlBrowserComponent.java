/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.browser.ui;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.HtmlBrowserComponent;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * HTML browser window with developer support tools.
 *
 * @author S. Aubrecht
 */
public class DeveloperHtmlBrowserComponent extends HtmlBrowserComponent {

    private final DeveloperToolbar devToolbar = DeveloperToolbar.create();

    public DeveloperHtmlBrowserComponent() {
        super( false, false );
    }

    public DeveloperHtmlBrowserComponent( HtmlBrowser.Factory factory ) {
        super( factory, false, false );
    }

    @Override
    public void open() {
        WindowManager wm = WindowManager.getDefault();
        Mode mode = wm.findMode( this );
        if( null == mode && !Boolean.getBoolean("webpreview.document") ) { //NOI18N
            mode = wm.findMode("webpreview"); //NOI18N
            if( null != mode ) {
                mode.dockInto( this );
            }
        }
        super.open();
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        devToolbar.intialize( getLookup() );
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    @Override
    protected HtmlBrowser createBrowser( Factory factory, boolean showToolbar, boolean showStatus ) {
        return new HtmlBrowser( factory, showToolbar, showStatus, devToolbar.getComponent() );
    }

    @Override
    protected String preferredID() {
        return super.preferredID() + "_dev"; //NOI18N
    }

    /** Serializes browser component -> writes Replacer object which
    * holds browser content and look. */
    @Override
    protected Object writeReplace () throws java.io.ObjectStreamException {
        return new BrowserReplacer (this);
    }

    /* Deserialize this top component. Now it is here for backward compatibility
    * @param in the stream to deserialize from
    */
    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal (in);
    }

    public static final class BrowserReplacer implements java.io.Externalizable {

        /** serial version UID */
        static final long serialVersionUID = 3215713034827048413L;


        /** browser window to be serialized */
        private transient DeveloperHtmlBrowserComponent bComp = null;
        transient boolean statLine;
        transient boolean toolbar;
        transient URL url;

        public BrowserReplacer () {
        }

        public BrowserReplacer (DeveloperHtmlBrowserComponent comp) {
            bComp = comp;
        }


        /* Serialize this top component.
        * @param out the stream to serialize to
        */
        @Override
        public void writeExternal (ObjectOutput out)
        throws IOException {
            out.writeBoolean (bComp.isStatusLineVisible ());
            out.writeBoolean (bComp.isToolbarVisible ());
            out.writeObject (bComp.getDocumentURL ());
        }

        /* Deserialize this top component.
          * @param in the stream to deserialize from
          */
        @Override
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            statLine = in.readBoolean ();
            toolbar = in.readBoolean ();
            url = (URL) in.readObject ();

        }


        private Object readResolve ()
        throws java.io.ObjectStreamException {
            // return singleton instance
            try {
                if ("http".equals(url.getProtocol())    // NOI18N
                &&  InetAddress.getByName (url.getHost ()).equals (InetAddress.getLocalHost ())) {
                    url.openStream ();
                }
            }
            // ignore exceptions thrown during our test of accessibility and restore browser
            catch (java.net.UnknownHostException exc) {}
            catch (java.lang.SecurityException exc) {}
            catch (java.lang.NullPointerException exc) {}

            catch (java.io.IOException exc) {
                // do not restore JSP/servlet pages - covers FileNotFoundException, ConnectException
                return null;
            }
            catch (java.lang.Exception exc) {
                // unknown exception - write log message & restore browser
                Logger.getLogger(HtmlBrowserComponent.class.getName()).log(Level.WARNING, null, exc);
            }
            WebBrowser browser = WebBrowsers.getInstance().getPreferred();
            if( null == browser || !browser.isEmbedded() ) {
                browser = WebBrowsers.getInstance().getEmbedded();
            }
            if( null != browser && browser.isEmbedded() ) {
                HtmlBrowser.Factory factory = browser.getHtmlBrowserFactory();
                bComp = new DeveloperHtmlBrowserComponent( factory );
                bComp.setURL( url );
            }
            return bComp;
        }

    } // end of BrowserReplacer inner class
}
