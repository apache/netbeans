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
package org.netbeans.modules.java.j2sedeploy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = "org-netbeans-modules-java-j2seproject")
public class J2SEDeployProjectOpenHook extends ProjectOpenedHook {

    private static final Logger LOG = Logger.getLogger(J2SEDeployProjectOpenHook.class.getName());
    
    private final Project prj;
    private final J2SEPropertyEvaluator eval;

    public J2SEDeployProjectOpenHook(@NonNull final Lookup baseLookup) {
        Parameters.notNull("baseLookup", baseLookup);   //NOI18N
        this.prj = baseLookup.lookup(Project.class);
        Parameters.notNull("prj", prj);                 //NOI18N
        this.eval = baseLookup.lookup(J2SEPropertyEvaluator.class);
        Parameters.notNull("eval", eval);   //NOI18N
    }

    @Override
    protected void projectOpened() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(
                Level.FINE,
                "Project opened {0} ({1}).",  //NOI18N
                new Object[] {
                    ProjectUtils.getInformation(prj).getDisplayName(),
                    FileUtil.getFileDisplayName(prj.getProjectDirectory())
                });
        }
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                updateBuildScript();
            }
        });
    }

    @Override
    protected void projectClosed() {
    }


    private void updateBuildScript() {
        assert ProjectManager.mutex().isWriteAccess();
        final AntBuildExtender extender = prj.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            LOG.log(
                Level.WARNING,
                "The project {0} ({1}) does not support AntBuildExtender.",     //NOI18N
                new Object[] {
                    ProjectUtils.getInformation(prj).getDisplayName(),
                    FileUtil.getFileDisplayName(prj.getProjectDirectory())
                });
            return;
        }
        if (extender.getExtension(J2SEDeployProperties.getCurrentExtensionName()) == null) {
            if (LOG.isLoggable(Level.FINE)) {
                //Prevent expensive ProjectUtils.getInformation(prj) when not needed
                LOG.log(
                    Level.FINE,
                    "The project {0} ({1}) does not have a current version ({2}) of J2SEDeploy extension.", //NOI18N
                    new Object[] {
                        ProjectUtils.getInformation(prj).getDisplayName(),
                        FileUtil.getFileDisplayName(prj.getProjectDirectory()),
                        J2SEDeployProperties.getCurrentExtensionName()
                    });
            }
            return;
        }
        if (J2SEDeployProperties.isBuildNativeUpToDate(prj)) {
            if (LOG.isLoggable(Level.FINE)) {
                //Prevent expensive ProjectUtils.getInformation(prj) when not needed
                LOG.log(
                    Level.FINE,
                    "The project {0} ({1}) have an up to date J2SEDeploy extension.", //NOI18N
                    new Object[] {
                        ProjectUtils.getInformation(prj).getDisplayName(),
                        FileUtil.getFileDisplayName(prj.getProjectDirectory())
                    });
            }
            return;
        }
        try {
            J2SEDeployProperties.copyBuildNativeTemplate(prj);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
