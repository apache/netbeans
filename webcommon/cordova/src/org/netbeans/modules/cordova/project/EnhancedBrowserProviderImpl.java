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
package org.netbeans.modules.cordova.project;

import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Parameters;

/**
 * Cordova pseudo browser
 *
 * @author Jan Becicka
 */
@ProjectServiceProvider(
        projectType = "org-netbeans-modules-web-clientproject", // NOI18N
        service = ClientProjectEnhancedBrowserProvider.class)
public class EnhancedBrowserProviderImpl implements ClientProjectEnhancedBrowserProvider {

    private Project p;

    public EnhancedBrowserProviderImpl(Project p) {
        Parameters.notNull("Project", p);
        this.p = p;
    }

    @Override
    public ClientProjectEnhancedBrowserImplementation getEnhancedBrowser(WebBrowser webBrowser) {
        if (webBrowser == null) {
            return null;
        }
        if (BrowserFamilyId.PHONEGAP == webBrowser.getBrowserFamily()) {
            return new CordovaBrowserImpl(p, webBrowser);
        }
        return null;
    }
}
