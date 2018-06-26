/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject;

import java.net.URL;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.spi.ServerURLMappingImplementation;
import org.netbeans.modules.web.common.api.WebServer;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class ServerURLMappingImpl implements ServerURLMappingImplementation {

    private final ClientSideProject project;

    public ServerURLMappingImpl(ClientSideProject project) {
        this.project = project;
    }

    @Override
    public URL toServer(int projectContext, FileObject projectFile) {
        if (projectContext == ServerURLMapping.CONTEXT_PROJECT_SOURCES) {
            URL u = null;
            if (project.isUsingEmbeddedServer()) {
                u = WebServer.getWebserver().toServer(projectFile);
            } else {
                String relPath = FileUtil.getRelativePath(project.getSiteRootFolder(), projectFile);
                String root = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL);
                if (root == null) {
                    return null;
                }
                if (!root.endsWith("/")) { //NOI18N
                    root += "/"; //NOI18N
                }

                // #231417
                if (root.contains(":80/")) {
                    root = root.replace(":80/", "/");
                } else if (root.contains(":443/") && root.contains("https")) {
                    root = root.replace(":443/", "/");
                }

                u = WebUtils.stringToUrl(root + relPath);
            }
            WebBrowser browser = project.getProjectWebBrowser();
            if (browser != null && u != null) {
                u = browser.toBrowserURL(project, projectFile, u);
            }
            return u;
        } else {
            JsTestingProvider testingProvider = project.getJsTestingProvider(false);
            if (testingProvider != null) {
                URL url = testingProvider.toServer(project, projectFile);
                if (url != null) {
                    return url;
                }
            }
            return null;
        }
    }

    @Override
    public FileObject fromServer(int projectContext, URL serverURL) {
        // #219339 - strip down query and/or fragment:
        serverURL = WebUtils.stringToUrl(WebUtils.urlToString(serverURL, true));
        if (serverURL == null) {
            return null;
        }

        WebBrowser browser = project.getProjectWebBrowser();
        if (browser != null) {
            serverURL = browser.fromBrowserURL(project, serverURL);
        }
        FileObject fo = null;
        if (project.isUsingEmbeddedServer()) {
            fo = WebServer.getWebserver().fromServer(serverURL);
        } else {
            String root = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL);
            if (root == null) {
                return null;
            }
            String u = WebUtils.urlToString(serverURL);

            // #231417
            if (!u.startsWith(root)) {
                if (root.contains(":80/")) {
                    if (u.startsWith(root.replace(":80/", "/"))) {
                        root = root.replace(":80/", "/");
                    }
                } else if (root.contains(":443/")) {
                    if (u.startsWith(root.replace(":443/", "/"))) {
                        root = root.replace(":443/", "/");
                    }
                }
            }

            if (u.startsWith(root)) {
                u = u.substring(root.length());
                if (u.startsWith("/")) { //NOI18N
                    u = u.substring(1);
                }
                FileObject siteRoot = project.getSiteRootFolder();
                if (siteRoot != null) {
                    fo = siteRoot.getFileObject(u);
                } else {
                    return null;
                }
            }
        }
        if (fo == null) {
            JsTestingProvider testingProvider = project.getJsTestingProvider(false);
            if (testingProvider != null) {
                fo = testingProvider.fromServer(project, serverURL);
            }
        }
        return fo;
    }

}
