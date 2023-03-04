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

package org.netbeans.modules.javascript.karma.mapping;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.exec.KarmaServers;
import org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public final class ServerMapping {

    private static final Logger LOGGER = Logger.getLogger(ServerMapping.class.getName());

    private static final String BASE_PREFIX = "base/"; // NOI18N
    // #245931 - no ending slash!
    private static final String ABSOLUTE_PREFIX = "absolute"; // NOI18N


    public FileObject fromServer(Project project, URL serverUrl) {
        String serverUrlString = WebUtils.urlToString(serverUrl);
        // try absolute first
        String prefix = KarmaServers.getInstance().getServerUrl(project, ABSOLUTE_PREFIX);
        if (prefix == null) {
            return null;
        }
        if (serverUrlString.startsWith(prefix)) {
            String absolutePath = serverUrlString.substring(prefix.length());
            try {
                absolutePath = URLDecoder.decode(absolutePath, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            if (Utilities.isWindows()) {
                absolutePath = absolutePath.replace('/', '\\'); // NOI18N
            }
            return FileUtil.toFileObject(new File(absolutePath));
        }
        // now relative
        prefix = KarmaServers.getInstance().getServerUrl(project, BASE_PREFIX);
        assert prefix != null;
        if (!serverUrlString.startsWith(prefix)) {
            return null;
        }
        assert prefix.endsWith("/") : prefix;
        String projectRelativePath = serverUrlString.substring(prefix.length());
        try {
            projectRelativePath = URLDecoder.decode(projectRelativePath, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        if (!projectRelativePath.isEmpty()) {
            return project.getProjectDirectory().getFileObject(projectRelativePath);
        }
        return null;
    }

    public URL toServer(Project project, FileObject projectFile) {
        String serverUrl = KarmaServers.getInstance().getServerUrl(project, null);
        if (serverUrl == null) {
            return null;
        }
        assert serverUrl.endsWith("/") : serverUrl;
        // absolute url?
        URL absoluteUrl = createAbsoluteUrl(serverUrl, projectFile);
        if (KarmaServers.getInstance().servesUrl(project, absoluteUrl)) {
            return absoluteUrl;
        }
        // relative url?
        FileObject projectDirectory = project.getProjectDirectory();
        String filePath = FileUtil.getRelativePath(projectDirectory, projectFile);
        if (filePath != null) {
            URL relativeUrl = createUrl(serverUrl, BASE_PREFIX, filePath);
            if (KarmaServers.getInstance().servesUrl(project, relativeUrl)) {
                return relativeUrl;
            }
        }
        return null;
    }

    private URL createAbsoluteUrl(String server, FileObject file) {
        assert server.endsWith("/") : server;
        assert file != null;
        return createUrl(server, ABSOLUTE_PREFIX, FileUtil.toFile(file).getAbsolutePath());
    }

    private URL createUrl(String server, String prefix, String filePath) {
        assert server.endsWith("/") : server;
        String urlPath;
        if (Utilities.isWindows()) {
            urlPath = filePath.replace('\\', '/'); // NOI18N
        } else {
            urlPath = filePath;
        }
        // encode only spaces, nothing more
        urlPath = urlPath.replace(" ", "%20"); // NOI18N
        try {
            return new URL(server + prefix + urlPath);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    @CheckForNull
    private FileObject getTestsFolder(Project project) {
        ProjectDirectoriesProvider directoriesProvider = project.getLookup().lookup(ProjectDirectoriesProvider.class);
        if (directoriesProvider == null) {
            return null;
        }
        return directoriesProvider.getTestDirectory(false);
    }

    private boolean isUnderneath(FileObject root, FileObject folder) {
        return root.equals(folder)
                || FileUtil.isParentOf(root, folder);
    }

}
