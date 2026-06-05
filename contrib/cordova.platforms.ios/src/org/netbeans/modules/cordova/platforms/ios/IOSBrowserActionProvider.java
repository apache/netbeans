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
package org.netbeans.modules.cordova.platforms.ios;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class IOSBrowserActionProvider implements ActionProvider {

    private BrowserSupport browserSupport;
    private final Project project;
    private final String browserId;

    public IOSBrowserActionProvider(BrowserSupport support, String browserId, Project project) {
        this.browserSupport = support;
        this.project = project;
        this.browserId = browserId;
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{
            COMMAND_RUN,
            COMMAND_RUN_SINGLE
        };
    }

    @Override
    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException {
        try {
            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    WebKitDebuggingSupport.getDefault().stopDebugging(true);
                    IOSBrowser.openBrowser(command, context, IOSBrowser.Kind.valueOf(browserId), project, browserSupport);
                }
            }, IOSBrowser.Kind.valueOf(browserId) == IOSBrowser.Kind.IOS_DEVICE_DEFAULT ? Bundle.LBL_OpeningiOS() : Bundle.LBL_Opening(), new AtomicBoolean(), true);
        } catch (IllegalStateException ise) {
            WebKitDebuggingSupport.getDefault().stopDebugging(true);
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return Utilities.isMac();
    }
}
