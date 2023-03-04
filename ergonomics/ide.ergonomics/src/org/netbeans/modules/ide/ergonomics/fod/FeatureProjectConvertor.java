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
package org.netbeans.modules.ide.ergonomics.fod;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.ide.ergonomics.fod.FeatureProjectFactory.loadIcon;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

class FeatureProjectConvertor implements ProjectConvertor {
    private final FileObject def;

    private FeatureProjectConvertor(FileObject def) {
        this.def = def;
    }
    
    static ProjectConvertor create(FileObject def) {
        return new FeatureProjectConvertor(def);
    }
    
    @Override
    public ProjectConvertor.Result isProject(FileObject projectDirectory) {
        return new ProjectConvertor.Result(
            Lookup.EMPTY, 
            new Factory(projectDirectory), 
            projectDirectory.getName(), 
            loadIcon()
        );
    }

    private final class Factory implements Callable<Project>, ProgressMonitor {
        private final FileObject projectDir;

        public Factory(FileObject projectDir) {
            this.projectDir = projectDir;
        }
        
        @Override
        public Project call() throws Exception {
            FeatureInfo info = FoDLayersProvider.getInstance().whichProvides(def);
            FindComponentModules findModules = new FindComponentModules(info);
            Collection<UpdateElement> toInstall = findModules.getModulesForInstall();
            Collection<UpdateElement> toEnable = findModules.getModulesForEnable();
            if (toInstall != null && !toInstall.isEmpty()) {
                ModulesInstaller installer = new ModulesInstaller(toInstall, findModules, this);
                installer.getInstallTask().schedule(0);
                installer.getInstallTask().waitFinished();
            } else if (toEnable != null && !toEnable.isEmpty()) {
                ModulesActivator enabler = new ModulesActivator(toEnable, findModules, this);
                enabler.getEnableTask().schedule(0);
                enabler.getEnableTask().waitFinished();
            }
            ProjectConvertor delegate = (ProjectConvertor) def.getAttribute("fod");
            if (delegate == null) {
                throw new IOException("Cannot find project convertor for " + projectDir);
            }
            final ProjectConvertor.Result res = delegate.isProject(projectDir);
            if (res == null) {
                throw new IOException("Cannot recognize project for " + projectDir + " by " + delegate);
            }
            return res.createProject();
        }

        @Override
        public void onDownload(ProgressHandle progressHandle) {
        }

        @Override
        public void onValidate(ProgressHandle progressHandle) {
        }

        @Override
        public void onInstall(ProgressHandle progressHandle) {
        }

        @Override
        public void onEnable(ProgressHandle progressHandle) {
        }

        @Override
        public void onError(String message) {
        }
    } // end of Factory
}
