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

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extbrowser.PrivateBrowserFamilyId;
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
    private static RequestProcessor RP = new RequestProcessor(SystemDefaultBrowser.class.getName(), 3);
    private transient AtomicBoolean detected = new AtomicBoolean(false);

    private static final boolean ACTIVE;
    static {
        if (Boolean.getBoolean("org.netbeans.modules.extbrowser.UseDesktopBrowse")) {
            if (Boolean.getBoolean("java.net.useSystemProxies") && Utilities.isUnix()) {
                // remove this check if JDK's bug 6496491 is fixed or if we can assume ORBit >= 2.14.2 and gnome-vfs >= 2.16.1
                logger.log(Level.FINE, "Ignoring java.awt.Desktop.browse support to avoid hang from #89540");
                ACTIVE = false;
            } else {
                ACTIVE = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
            }
        } else {
            ACTIVE = false;
        }
    }

    /** Determines whether the browser should be visible or not
     *  @return true when OS is not Windows and is not Unix with Default Browser capability and Desktop is inactive.
     *          false in all other cases.
     */
    public static Boolean isHidden() {
        return Boolean.valueOf(
                (!Utilities.isWindows() && !defaultBrowserUnixReady() && !Utilities.isMac())
                && !ACTIVE);
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
        if (ACTIVE) {
            return new Jdk6BrowserImpl();
        } else if (Utilities.isWindows()) {
            return new NbDdeBrowserImpl(this);
        } else if (Utilities.isMac()) {
            return new MacBrowserImpl(this);
        } else if (Utilities.isUnix() && !Utilities.isMac()) {
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
     * Can be overriden to return browser that suits to platform and settings.
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
        if (!Utilities.isWindows() || ACTIVE) {
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
        final ExtBrowserImpl extImpl = impl != null && impl instanceof ExtBrowserImpl ? (ExtBrowserImpl)impl : null;
        if (extImpl != null) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    setPrivateBrowserFamilyId(extImpl.detectPrivateBrowserFamilyId());
                }
            });
        }
    }

    private static final class Jdk6BrowserImpl extends ExtBrowserImpl {

        public Jdk6BrowserImpl() {
            assert ACTIVE;
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
