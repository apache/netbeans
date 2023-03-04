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
package org.netbeans.modules.php.project.runconfigs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 * Run configuration for INTERNAL SERVER (PHP 5.4+).
 */
public final class RunConfigInternal {

    public static final String DEFAULT_HOSTNAME = "localhost"; // NOI18N
    public static final int DEFAULT_PORT = 8000;

    private File workDir;
    private File documentRoot;
    private String hostname;
    private String port;
    private String routerRelativePath;


    private RunConfigInternal() {
    }

    public static PhpProjectProperties.RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.INTERNAL;
    }

    public static String getDisplayName() {
        return getRunAsType().getLabel();
    }

    //~ Factories

    public static RunConfigInternal create() {
        return new RunConfigInternal();
    }

    public static RunConfigInternal forProject(final PhpProject project) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<RunConfigInternal>() {
            @Override
            public RunConfigInternal run() {
                return new RunConfigInternal()
                        .setHostname(ProjectPropertiesSupport.getHostname(project))
                        .setPort(ProjectPropertiesSupport.getPort(project))
                        .setWorkDir(FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project)))
                        .setDocumentRoot(FileUtil.toFile(ProjectPropertiesSupport.getWebRootDirectory(project)))
                        .setRouterRelativePath(ProjectPropertiesSupport.getInternalRouter(project));
            }
        });
    }

    //~ Methods

    public String getServer() {
        return hostname + ":" + port; // NOI18N
    }

    // XXX use this for url validation as well?
    public URL getUrl() throws MalformedURLException {
        return new URL("http://" + getServer() + "/"); // NOI18N
    }

    public String getUrlHint() {
        try {
            return getUrl().toExternalForm();
        } catch (MalformedURLException ex) {
            // ignored
        }
        return null;
    }

    public String getRelativeDocumentRoot() {
        FileObject workDirFo = FileUtil.toFileObject(workDir);
        FileObject documentRootFo = FileUtil.toFileObject(documentRoot);
        String relativePath = FileUtil.getRelativePath(workDirFo, documentRootFo);
        assert relativePath != null : "Document root " + documentRoot + " must be underneath workdir " + workDir;
        return StringUtils.hasText(relativePath) ? relativePath : null;
    }

    //~ Getters & Setters

    public File getWorkDir() {
        return workDir;
    }

    public RunConfigInternal setWorkDir(File workDir) {
        this.workDir = workDir;
        return this;
    }

    public String getHostname() {
        return hostname;
    }

    public RunConfigInternal setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public String getPort() {
        return port;
    }

    public RunConfigInternal setPort(String port) {
        this.port = port;
        return this;
    }

    public File getDocumentRoot() {
        return documentRoot;
    }

    public RunConfigInternal setDocumentRoot(File documentRoot) {
        this.documentRoot = documentRoot;
        return this;
    }

    public String getRouterRelativePath() {
        return routerRelativePath;
    }

    public RunConfigInternal setRouterRelativePath(String routerRelativePath) {
        this.routerRelativePath = routerRelativePath;
        return this;
    }

}
