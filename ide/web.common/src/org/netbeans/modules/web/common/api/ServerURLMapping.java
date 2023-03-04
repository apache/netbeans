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

package org.netbeans.modules.web.common.api;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ServerURLMappingImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Provides mapping between project's source file and its location on server
 * and vice versa. If project has not deployed files to a server the file:///
 * URL will be returned instead.
 * 
 * A single source file can have different URL when project is Run or Tested.
 * For example JS-Test-Driver used for tests execution has its own server and 
 * deploys sources and tests into this server. Resulting server URL is therefore
 * different based on context in which the source is being used. Clients
 * of this API should use appropriate CONTEXT_PROJECT_* constants.
 */
public final class ServerURLMapping {

    /**
     * Constant to indicate that project's source file to server URL mapping should be
     * evaluated in the context of "Run Project" action.
     */
    public static final int CONTEXT_PROJECT_SOURCES = 1;
    
    /**
     * Constant to indicate that project's source file to server URL mapping should be
     * evaluated in the context of "Test Project" action.
     */
    public static final int CONTEXT_PROJECT_TESTS = 2;
    
    private ServerURLMapping() {
    }
    
    /**
     * Convert given project's file into server URL.
     * @return could return null if file is not deployed to server and therefore
     *   not accessible
     */
    public static URL toServer(Project p, FileObject projectFile) {
        return toServer(p, CONTEXT_PROJECT_SOURCES, projectFile);
    }
    
    /**
     * Convert given project's file into server URL.
     * @param projectContext see {@link #CONTEXT_PROJECT_SOURCES} and {@link #CONTEXT_PROJECT_TESTS}
     * @return could return null if file is not deployed to server and therefore
     *   not accessible
     */
    public static URL toServer(Project p, int projectContext, FileObject projectFile) {
        Parameters.notNull("project", p); //NOI18N
        Parameters.notNull("projectFile", projectFile); //NOI18N
        
        ServerURLMappingImplementation impl = p.getLookup().lookup(ServerURLMappingImplementation.class);
        if (impl != null) {
            URL u = impl.toServer(projectContext, projectFile);
            if (u != null) {
                return u;
            }
        }
        try {
            URL url = projectFile.toURL();
            String urlString = url.toURI().toString();
            String urlString2 = urlString.replace("file:/", "file:///"); //NOI18N
            if (!urlString.equals(urlString2)) {
                url = new URL(urlString2);
            }
            return url;
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    /**
     * Convert given server URL into project's file.
     * @return returns null if nothing is known about this server URL
     */
    public static FileObject fromServer(Project p, URL serverURL) {
        return fromServer(p, CONTEXT_PROJECT_SOURCES, serverURL);
    }
    
    /**
     * Convert given server URL into project's file.
     * @param projectContext see {@link #CONTEXT_PROJECT_SOURCES} and {@link #CONTEXT_PROJECT_TESTS};
     *   it is very unlikely that server URL could be translated into two different sources
     *   but for API parity with toServer the context param is available here as well
     * @return returns null if nothing is known about this server URL
     */
    public static FileObject fromServer(Project p, int projectContext, URL serverURL) {
        Parameters.notNull("project", p); //NOI18N
        Parameters.notNull("serverURL", serverURL); //NOI18N
        ServerURLMappingImplementation impl = p.getLookup().lookup(ServerURLMappingImplementation.class);
        if (impl != null) {
            FileObject fo = impl.fromServer(projectContext, serverURL);
            if (fo != null) {
                return fo;
            }
        }
        if ("file".equals(serverURL.getProtocol())) { //NOI18N
            try {
                URI serverURI = serverURL.toURI();
                if (serverURI.getQuery() != null || serverURI.getFragment() != null) {
                    // #236532 - strip down query part from the URL:
                    serverURI = WebUtils.stringToUrl(WebUtils.urlToString(serverURL, true)).toURI();
                }
                File f = FileUtil.normalizeFile(BaseUtilities.toFile(serverURI));
                return FileUtil.toFileObject(f);
                //FileObject fo = URLMapper.findFileObject(serverURL);
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
}
