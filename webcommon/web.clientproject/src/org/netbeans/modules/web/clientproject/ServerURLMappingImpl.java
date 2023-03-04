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
