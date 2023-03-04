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

package org.netbeans.modules.web.clientproject.browser;

import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserProvider;
import org.netbeans.spi.project.ProjectServiceProvider;

@ProjectServiceProvider(
        projectType = "org-netbeans-modules-web-clientproject",
        service=ClientProjectEnhancedBrowserProvider.class)
public class ClientProjectEnhancedBrowserProviderImpl implements ClientProjectEnhancedBrowserProvider {
    private Project p;

    public ClientProjectEnhancedBrowserProviderImpl(Project p) {
        this.p = p;
    }
    
    @Override
    public ClientProjectEnhancedBrowserImplementation getEnhancedBrowser(WebBrowser webBrowser) {
        //is this necessary?
        if (webBrowser == null) {
            return null;
        }
        if (webBrowser.getBrowserFamily().isMobile()) {
            return null;
        }
        return new ClientProjectEnhancedBrowserImpl((ClientSideProject)p, webBrowser);
    }

}
