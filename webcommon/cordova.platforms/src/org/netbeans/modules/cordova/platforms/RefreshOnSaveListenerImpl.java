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

package org.netbeans.modules.cordova.platforms;

import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.openide.filesystems.FileObject;

public class RefreshOnSaveListenerImpl implements RefreshOnSaveListener {

    private final BrowserSupport support;
    private final Project project;
    private ClientProjectEnhancedBrowserImplementation cfg;

    public RefreshOnSaveListenerImpl(Project project, BrowserSupport support, ClientProjectEnhancedBrowserImplementation cfg) {
        this.support = support;
        this.project = project;
        this.cfg = cfg;
    }
    
    @Override
    public void fileChanged(FileObject fo) {
        if (!cfg.isAutoRefresh()) {
            return;
        }
        if (support.ignoreChange(fo)) {
            return;
        }
        support.reload();
    }

    @Override
    public void fileDeleted(FileObject fo) {
        // TODO: close browser tab?
    }

}
