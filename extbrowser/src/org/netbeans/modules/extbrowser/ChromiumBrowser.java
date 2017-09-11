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

import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.beans.PropertyChangeListener;
import java.io.File;

public class ChromiumBrowser extends ExtWebBrowser implements PropertyChangeListener {

    private static final long serialVersionUID = -2700536003661091286L;
    
    private static final String CHROMIUM_PATH = "/usr/bin/chromium-browser";    // NOI18N
    private static final String CHROMIUM_PATH2 = "/usr/bin/chromium";    // NOI18N

    /** Creates new ExtWebBrowser */
    public ChromiumBrowser() {
        super(PrivateBrowserFamilyId.CHROMIUM);
        ddeServer = ExtWebBrowser.CHROMIUM;
    }

    public static Boolean isHidden () {
        File file = null;
        if (Utilities.isUnix() && !Utilities.isMac()) {
            file = new File(CHROMIUM_PATH);
            if (!file.exists()) {
                file = new File(CHROMIUM_PATH2);
            }
        } else if (Utilities.isWindows()) {
            file = getLocalAppPath();
        }
        return (file == null) || !file.exists() || !file.canExecute();
    }

    /** Getter for browser name
     *  @return name of browser
     */
    @Override
    public String getName () {
        if (name == null) {
            this.name = NbBundle.getMessage(ChromiumBrowser.class, 
                    "CTL_ChromiumBrowserName");  // NOI18N
        }
        return name;
    }

    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not supported.
     * @return browserImpl implementation of browser.
     */
    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        ExtBrowserImpl impl = null;

        if (Utilities.isUnix() && !Utilities.isMac()) {
            impl = new UnixBrowserImpl(this);
        } else if (Utilities.isWindows()) {
            impl = new NbDdeBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException (NbBundle.
                    getMessage(FirefoxBrowser.class, "MSG_CannotUseBrowser"));  // NOI18N
        }
        
        return impl;
    }

    /** Default command for browser execution.
     * Can be overridden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    @Override
    protected NbProcessDescriptor defaultBrowserExecutable() {
        File file = null;
        if (Utilities.isUnix() && !Utilities.isMac()) {
            file = new File(CHROMIUM_PATH);
            if (!file.exists()) {
                file = new File(CHROMIUM_PATH2);
            }
        } else if (Utilities.isWindows()) {
            file = getLocalAppPath();
        }
        if ((file != null) && file.exists()) {
            return new NbProcessDescriptor (
                    file.getAbsolutePath(), "{" + 
                    ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}", 
                    NbBundle.getMessage (ChromiumBrowser.class, 
                            "MSG_BrowserExecutorHint")          // NOI18N
                );                
        }

        return null;        
    }

    private static File getLocalAppPath(){
        String localFiles = System.getenv("LOCALAPPDATA"); // NOI18N
        String chrome = localFiles+ "\\Chromium\\Application\\chrome.exe"; // NOI18N
        return new File(chrome);
    }

}
