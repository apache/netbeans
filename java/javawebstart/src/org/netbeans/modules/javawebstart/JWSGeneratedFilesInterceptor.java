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
package org.netbeans.modules.javawebstart;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectPropertiesUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ant.GeneratedFilesInterceptor;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(
        service = GeneratedFilesInterceptor.class,
        projectType = "org-netbeans-modules-java-j2seproject")
public class JWSGeneratedFilesInterceptor implements GeneratedFilesInterceptor {

    private static final Logger LOG = Logger.getLogger(JWSGeneratedFilesInterceptor.class.getName());   

    private final ThreadLocal<Boolean> reenter = new ThreadLocal<Boolean>();
    
    @Override
    public void fileGenerated(
            final Project project,
            final String path) {
        if (reenter.get() == Boolean.TRUE) {
            return;
        }
        if (GeneratedFilesHelper.BUILD_IMPL_XML_PATH.equals(path)) {
            final AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
            if (extender == null) {
                LOG.log(
                    Level.WARNING,
                    "The project {0} ({1}) does not support AntBuildExtender.",     //NOI18N
                    new Object[] {
                        ProjectUtils.getInformation(project).getDisplayName(),
                        FileUtil.getFileDisplayName(project.getProjectDirectory())
                    });
                return;
            }
            runDeferred(new Runnable() {
                @Override
                public void run() {
                    updateIfNeeded(project, extender);
                }
            });
        }
    }
    
    private void runDeferred(final Runnable r) {
        ProjectManager.mutex().postReadRequest(new Runnable() {
            @Override
            public void run() {                
                ProjectManager.mutex().postWriteRequest(r);
            }
        });
    }

    private void updateIfNeeded(
            final Project project,
            final AntBuildExtender extender) {
        if (extender.getExtension(JWSProjectPropertiesUtils.getCurrentExtensionName()) != null) {
            //Already has a current version of extension
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                        Level.FINE,
                        "The project {0} ({1}) already has a current version ({2}) of JWS extension.", //NOI18N
                        new Object[]{
                    ProjectUtils.getInformation(project).getDisplayName(),
                    FileUtil.getFileDisplayName(project.getProjectDirectory()),
                    JWSProjectPropertiesUtils.getCurrentExtensionName()
                });
            }
            return;
        }
        reenter.set(Boolean.TRUE);
        try {
            boolean needsUpdate = false;
            for (String oldExt : JWSProjectPropertiesUtils.getOldExtensionNames()) {
                final AntBuildExtender.Extension extension = extender.getExtension(oldExt);
                if (extension != null) {
                    extender.removeExtension(oldExt);
                    needsUpdate = true;
                }
            }
            if (needsUpdate) {
                try {
                    //There was an old extension which needs to be updated
                    JWSProjectPropertiesUtils.updateJnlpExtension(project);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } finally {
            reenter.remove();
        }
    }

}
