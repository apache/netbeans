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

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Browser factory for System default Win browser.
 *
 * @author Martin Grebac
 */
public class SystemDefaultBrowser extends ExtWebBrowser {

    private static final long serialVersionUID = -7317179197254112564L;
    private static final Logger logger = Logger.getLogger(SystemDefaultBrowser.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(SystemDefaultBrowser.class.getName(), 3);
    private transient AtomicBoolean detected = new AtomicBoolean(false);

    private static final boolean USE_JDK_BROWSER = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            && Boolean.getBoolean("org.netbeans.modules.extbrowser.UseDesktopBrowse");

    /** Determines whether the browser should be visible or not
     *  @return true when OS is not Windows and is not Unix with Default Browser capability and Desktop is inactive.
     *          false in all other cases.
     */
    public static Boolean isHidden() {
        return !Utilities.isWindows() && !defaultBrowserUnixReady() && !Utilities.isMac() && !USE_JDK_BROWSER;
    }

    private static boolean defaultBrowserUnixReady() {
        return Utilities.isUnix() && NbDefaultUnixBrowserImpl.isAvailable();
    }
    
    /** Creates new ExtWebBrowser */
    public SystemDefaultBrowser() {
        super(PrivateBrowserFamilyId.UNKNOWN);
    }

    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not Windows.
     * @return browserImpl implementation of browser.
     */
    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        if (USE_JDK_BROWSER) {
            return new JdkBrowserImpl();
        } else if (Utilities.isWindows()) {
            return new NbDdeBrowserImpl(this);
        } else if (Utilities.isMac()) {
            return new MacBrowserImpl(this);
        } else if (Utilities.isUnix()) {
            return new NbDefaultUnixBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException(NbBundle.getMessage(SystemDefaultBrowser.class, "MSG_CannotUseBrowser"));
        }
    }

    /** Getter for browser name
     *  @return name of browser
     */
    @Override
    public String getName() {
        if (name == null) {
            this.name = NbBundle.getMessage(SystemDefaultBrowser.class, "CTL_SystemDefaultBrowserName");
        }
        return name;
    }

    /** Setter for browser name
     * @param name browser name
     */
    @Override
    public void setName(String name) {
        // system default browser name shouldn't be changed
    }

    /** Default command for browser execution.
     * Can be overridden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    @Override
    protected NbProcessDescriptor defaultBrowserExecutable() {
        if (Utilities.isMac()) {
            return new NbProcessDescriptor ("/usr/bin/open", // NOI18N
                "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}", // NOI18N
                ExtWebBrowser.UnixBrowserFormat.getHint());
        }
        if (!Utilities.isWindows() || USE_JDK_BROWSER) {
            return new NbProcessDescriptor("", ""); // NOI18N
        }

        String b;
        String params = ""; // NOI18N
        try {
            // finds HKEY_CLASSES_ROOT\\".html" and respective HKEY_CLASSES_ROOT\\<value>\\shell\\open\\command
            // we will ignore all params here
            b = NbDdeBrowserImpl.getDefaultOpenCommand();
            String[] args = Utilities.parseParameters(b);

            if (args == null || args.length == 0) {
                throw new NbBrowserException();
            }
            b = args[0];
            params += " {" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
        } catch (NbBrowserException e) {
            b = ""; // NOI18N
        } catch (UnsatisfiedLinkError e) {
            // someone is customizing this on non-Win platform
            b = "iexplore"; // NOI18N
        }

        NbProcessDescriptor p = new NbProcessDescriptor(b, params,
                ExtWebBrowser.UnixBrowserFormat.getHint());
        return p;
    }

    private void readObject (java.io.ObjectInputStream ois)
            throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        detected = new AtomicBoolean(false);
    }


    @Override
    public PrivateBrowserFamilyId getPrivateBrowserFamilyId() {
        detectSystemDefaultBrowser();
        return super.getPrivateBrowserFamilyId();
    }

    private synchronized void detectSystemDefaultBrowser() {
        if (detected.getAndSet(true)) {
            return;
        }
        final HtmlBrowser.Impl impl = createHtmlBrowserImpl();
        final ExtBrowserImpl extImpl = impl instanceof ExtBrowserImpl ? (ExtBrowserImpl)impl : null;
        if (extImpl != null) {
            RP.post(() -> {
                setPrivateBrowserFamilyId(extImpl.detectPrivateBrowserFamilyId());
            });
        }
    }

    private static final class JdkBrowserImpl extends ExtBrowserImpl {

        public JdkBrowserImpl() {
            assert USE_JDK_BROWSER;
        }

        @Override
        protected void loadURLInBrowserInternal(URL url) {
            assert !EventQueue.isDispatchThread();
            URL extURL = URLUtil.createExternalURL(url, false);
            try {
                URI uri = extURL.toURI();
                logger.log(Level.FINE, "Calling java.awt.Desktop.browse({0})", uri);
                Desktop.getDesktop().browse(uri);
            } catch (URISyntaxException e) {
                logger.log(Level.SEVERE,"The URL:\n{0}" + "\nis not fully RFC 2396 compliant and cannot be used with Desktop.browse().", extURL);
            } catch (IOException e) {
                // Report in GUI?
                logger.log(Level.WARNING, null, e);
            }
        }
    }
}
