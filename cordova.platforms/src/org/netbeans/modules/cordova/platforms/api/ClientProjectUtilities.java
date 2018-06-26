/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
