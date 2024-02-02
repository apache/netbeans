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

import java.io.File;
import java.util.logging.Level;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Jan Stola
 */
public class MicrosoftEdgeBrowser extends ExtWebBrowser {
    /** Determines whether this browser should be hidden. */
    private static Boolean hidden;

    /**
     * Determines whether the browser should be visible or not.
     * 
     * @return {@code false} when the OS is Windows and Microsoft Edge is available,
     * returns {@code true} otherwise.
     */
    public static Boolean isHidden() {
        if (hidden == null) {
            if (Utilities.isWindows()) {
                hidden = getAppUserModelId().isEmpty();
            } else {
                hidden = true;
            }
        }
        return hidden;
    }

    private static final long serialVersionUID = 4333320552804224866L;

    public MicrosoftEdgeBrowser() {
        super(PrivateBrowserFamilyId.EDGE);
    }

    @Override
    public String getName () {
        if (name == null) {
            name = NbBundle.getMessage(MicrosoftEdgeBrowser.class, "CTL_MicrosoftEdgeBrowserName"); // NOI18N
        }
        return name;
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not Windows 10.
     * @return browserImpl implementation of browser.
     */
    @Override
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        ExtBrowserImpl impl = null;

        if (isHidden()) {
            throw new UnsupportedOperationException(NbBundle.getMessage(MicrosoftEdgeBrowser.class, "MSG_CannotUseBrowser")); // NOI18N
        } else {
            impl = new NbDdeBrowserImpl(this);
        }

        return impl;
    }
        
    /**
     * Default command for browser execution.
     *
     * @return process descriptor that allows to start browser.
     */
    @Override
    protected NbProcessDescriptor defaultBrowserExecutable () {
        String command = "cmd"; // NOI18N
        String params = "/C start msedge {" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}"; // NOI18N
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "{0} MicrosoftEdge: defaultBrowserExecutable: {1}, {2}", new Object[] { System.currentTimeMillis(), params, command });
        }
        return new NbProcessDescriptor(command, params);
    }

    private static String appUserModelId;
    private static String getAppUserModelId() {
        if (appUserModelId == null) {
            File folder = new File("C:\\Windows\\SystemApps"); // NOI18N
            String id = null;
            if (folder.exists()) {
                for (File file : folder.listFiles()) {
                    String fileName = file.getName();
                    if (fileName.startsWith("Microsoft.MicrosoftEdge")) { // NOI18N
                        id = fileName;
                    }
                }
            }
            appUserModelId = (id == null) ? "" : (id + "!MicrosoftEdge"); // NOI18N
        }
        return appUserModelId;
    }

    private void readObject (java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

}
