/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gradle.javaee;

import org.netbeans.modules.gradle.api.NbGradleProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

import static org.netbeans.modules.gradle.javaee.Bundle.*;
import org.netbeans.modules.gradle.javaee.api.GradleWebProject;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = Sources.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public class JavaEESourcesImpl implements Sources {

    public static final String TYPE_DOC_ROOT = "doc_root"; // NOI18N

    private final Project project;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final PropertyChangeListener pcl;

    private List<SourceGroup> webResourceRoots;

    public JavaEESourcesImpl(Project project) {
        this.project = project;
        this.pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (NbGradleProject.PROP_PROJECT_INFO.equals(event.getPropertyName())) {
                    if (hasChanged()) {
                        cs.fireChange();
                    }
                }
            }
        };
    }

    private boolean hasChanged() {
        List<SourceGroup> resourceRoots = getWebSourceGroups();

        synchronized (this) {
            if (webResourceRoots == null || !webResourceRoots.equals(resourceRoots)) {
                // Set the cached value to the current resource roots
                webResourceRoots = resourceRoots;
                return true;
            }
        }

        return false;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        if (!cs.hasListeners()) {
            NbGradleProject.addPropertyChangeListener(project, pcl);
        }
        cs.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        cs.removeChangeListener(changeListener);

        if (!cs.hasListeners()) {
            NbGradleProject.removePropertyChangeListener(project, pcl);
        }
    }

    @Override
    public SourceGroup[] getSourceGroups(String str) {
        if (TYPE_DOC_ROOT.equals(str)) {
            synchronized (this) {
                if (webResourceRoots == null) {
                    webResourceRoots = getWebSourceGroups();
                }
                return webResourceRoots.toArray(new SourceGroup[0]);
            }
        }
        return new SourceGroup[0];
    }

    private List<SourceGroup> getWebSourceGroups() {
        List<SourceGroup> sourceGroups = new ArrayList<>();

        ProjectWebRootProvider webRootProvider = project.getLookup().lookup(ProjectWebRootProvider.class);
        GradleWebProject wp = GradleWebProject.get(project);
        if (webRootProvider != null && wp != null) {
            Collection<FileObject> webRoots = webRootProvider.getWebRoots();
            String projectDirPath = wp.getWebAppDir().getAbsolutePath();
            for (FileObject webRoot : webRoots) {
                boolean isDefault = webRoot.getPath().equals(projectDirPath);
                SourceGroup g = GenericSources.group(project, webRoot, TYPE_DOC_ROOT, getDisplayName(webRoot, isDefault), null, null);
                if (isDefault) {
                    sourceGroups.add(0, g);
                } else {
                    sourceGroups.add(g);
                }
            }
        }
        return sourceGroups;
    }

    @NbBundle.Messages({
        "LBL_DefaultWebPages=Web Pages",
        "# {0} - The folder name for custom webpages",
        "LBL_WebPages=Web Pages ({0})"
    })
    private String getDisplayName(FileObject webRoot, boolean isDefault) {
        return isDefault ? LBL_DefaultWebPages() : LBL_WebPages(webRoot.getPath());
    }

}
