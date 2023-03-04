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
