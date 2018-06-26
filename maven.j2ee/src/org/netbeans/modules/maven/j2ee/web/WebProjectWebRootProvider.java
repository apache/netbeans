/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
