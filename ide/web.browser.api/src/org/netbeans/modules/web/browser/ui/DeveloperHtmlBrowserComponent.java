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
