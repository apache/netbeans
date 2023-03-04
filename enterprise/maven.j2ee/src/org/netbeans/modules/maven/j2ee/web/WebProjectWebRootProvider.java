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

package org.netbeans.modules.maven.j2ee.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * This class is immutable and thus <i>thread safe</i>.
 *
 * @author marekfukala
 * @author Martin Janicek <mjanicek@netbeans.org>
 */
@ProjectServiceProvider(
    service = {
        ProjectWebRootProvider.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_JAR // #233476
    }
)
public class WebProjectWebRootProvider implements ProjectWebRootProvider {

    private final Project project;
    private final FileObject projectDir;


    public WebProjectWebRootProvider(Project project) {
        this.project = project;
        this.projectDir = project.getProjectDirectory();
    }

    @Override
    public FileObject getWebRoot(FileObject file) {
        return getDefaultWebRoot();
    }

    @Override
    public Collection<FileObject> getWebRoots() {
        Set<FileObject> webRootsFO = new LinkedHashSet<>();
        List<String> webRoots = WebProjectUtils.getPluginProperty(project, new WebRootsBuilder());

        if (webRoots != null) {
            for (String webRoot : webRoots) {
                FileObject webRootFo = projectDir.getFileObject(webRoot);

                // NPE check is here because the directory might be listed in pom.xml
                // but the directory still don't need to exist on the disk
                if (webRootFo != null) {
                    webRootsFO.add(webRootFo);
                }
            }
        }

        // Default web resource directory is usually webapp
        // See also maven-war-plugin documentation for more details
        FileObject defaultWebRoot = getDefaultWebRoot();
        if (defaultWebRoot != null) {
            webRootsFO.add(defaultWebRoot);
        }

        return webRootsFO;
    }

    private FileObject getDefaultWebRoot() {
        String webSourceDir = WebProjectUtils.getPluginProperty(project, "warSourceDirectory"); // NOI18N
        if (webSourceDir == null) {
            webSourceDir = "src/main/webapp"; // NOI18N
        }

        // Try to find root using relative path
        FileObject sourceRoot = projectDir.getFileObject(webSourceDir);
        if (sourceRoot != null) {
            return sourceRoot;
        }

        // Try to find resources root using absolute path --> See issue #241205
        return FileUtil.toFileObject(FileUtil.normalizeFile(new File(webSourceDir)));
    }

    /**
     * Iterates through the maven-war-plugin configuration and finds out all declared web resource directories.
     */
    private static class WebRootsBuilder implements PluginPropertyUtils.ConfigurationBuilder<List<String>> {

        @Override
        public List<String> build(Xpp3Dom configRoot, ExpressionEvaluator eval) {
            List<String> webRoots = new ArrayList<>();

            if (configRoot != null) {
                Xpp3Dom webResources = configRoot.getChild("webResources"); // NOI18N
                if (webResources != null) {
                    Xpp3Dom[] resources = webResources.getChildren("resource"); // NOI18N

                    for (Xpp3Dom resource : resources) {
                        if (resource != null) {
                            Xpp3Dom directory = resource.getChild("directory"); // NOI18N

                            if (directory != null) {
                                try {
                                    String directoryValue = (String) eval.evaluate(directory.getValue());
                                    if (directoryValue != null && !"".equals(directoryValue.trim())) { // NOI18N
                                        webRoots.add(directoryValue);
                                    }
                                } catch (ExpressionEvaluationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
            }
            return webRoots;
        }
    }
}
