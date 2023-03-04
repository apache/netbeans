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
package org.netbeans.modules.cordova.platforms.api;

import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cordova.platforms.BrowserURLMapperImpl;
import org.netbeans.modules.cordova.platforms.EnhancedBrowserImpl;
import org.netbeans.modules.cordova.platforms.MobilePlatformsSetup;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.clientproject.api.ClientSideModule;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public final class ClientProjectUtilities {
    public static FileObject getSiteRoot(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT);
        if (sourceGroups.length == 0 ) {
            return project.getProjectDirectory().getFileObject("www");
        }
        return sourceGroups[0].getRootFolder();
    }

    public static FileObject getStartFile(Project project) {
        ClientSideModule clientSide = project.getLookup().lookup(ClientSideModule.class);
        return clientSide.getProperties().getStartFile();
    }

    public static String getWebContextRoot(Project p) {
        ClientSideModule clientSide = p.getLookup().lookup(ClientSideModule.class);
        return clientSide.getProperties().getWebContextRoot();
    }

    public static boolean isUsingEmbeddedServer(Project p) {
        ClientSideModule clientSide = p.getLookup().lookup(ClientSideModule.class);
        return clientSide != null;
    }
    
    public static ClientProjectEnhancedBrowserImplementation createMobileBrowser(Project project, WebBrowser browser, BrowserSupport support, ActionProvider provider) {
        return new EnhancedBrowserImpl(project, browser, support,provider);
    }
    
    public static BrowserURLMapperImplementation  createMobileBrowserURLMapper() {
        return new BrowserURLMapperImpl();
    }
    
    public static JPanel createMobilePlatformsSetupPanel() {
        return new MobilePlatformsSetup();
    }

}
