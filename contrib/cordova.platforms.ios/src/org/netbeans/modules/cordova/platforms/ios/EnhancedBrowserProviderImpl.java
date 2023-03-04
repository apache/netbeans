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

package org.netbeans.modules.cordova.platforms.ios;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.api.ClientProjectUtilities;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Parameters;

/**
 * @author Jan Becicka
 */
@ProjectServiceProvider(
       projectTypes = {
           @LookupProvider.Registration.ProjectType(id = "org-netbeans-modules-web-clientproject")
       },
       service = ClientProjectEnhancedBrowserProvider.class)
public class EnhancedBrowserProviderImpl implements ClientProjectEnhancedBrowserProvider {
    private Project p;

    public EnhancedBrowserProviderImpl(Project p) {
        Parameters.notNull("Project", p);
        this.p = p;
    }
    
    @Override
    public ClientProjectEnhancedBrowserImplementation getEnhancedBrowser(final WebBrowser webBrowser) {
        if (webBrowser == null) {
            return null;
        }
        if (BrowserFamilyId.IOS == webBrowser.getBrowserFamily()) {
            BrowserSupport support = BrowserSupport.create(webBrowser);
            return ClientProjectUtilities.createMobileBrowser(
                    p, 
                    webBrowser, 
                    support, 
                    new IOSBrowserActionProvider(support, webBrowser.getId(), p));
        }
        return null;
    }

}
