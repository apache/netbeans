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

package org.netbeans.modules.maven.j2ee;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.spi.nodes.OtherSourcesExclude;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.j2ee.Bundle.*;
import org.openide.filesystems.FileUtil;

/**
 * Implementation of {@link Sources} interface for Java EE Maven projects.
 *
 * This class is <i>thread safe</i>.
 *
 * @author  Milos Kleint
 */
@ProjectServiceProvider(
    service = {
        Sources.class,
        OtherSourcesExclude.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_OSGI,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_JAR // #233476
    }
)
public class J2eeMavenSourcesImpl implements Sources, OtherSourcesExclude {
    
    public static final String TYPE_DOC_ROOT = "doc_root"; // NOI18N
    public static final String TYPE_WEB_INF  = "web_inf";  // NOI18N

    private final Project project;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final PropertyChangeListener pcl;

    // @GuardedBy("this")
    private List<SourceGroup> webResourceRoots;


    public J2eeMavenSourcesImpl(Project project) {
        this.project = project;
        this.pcl = new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (NbMavenProject.PROP_PROJECT.equals(event.getPropertyName())) {
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
        // If no listener were registered until now, start listening at project changes
        if (!cs.hasListeners()) {
            NbMavenProject.addPropertyChangeListener(project, pcl);
        }
        cs.addChangeListener(changeListener);
    }
    
    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        cs.removeChangeListener(changeListener);
        
        // If this was the last registered listener, stop listening at project changes
        if (!cs.hasListeners()) {
            NbMavenProject.removePropertyChangeListener(project, pcl);
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
        if (webRootProvider != null) {
            Collection<FileObject> webRoots = webRootProvider.getWebRoots();
            String projectDirPath = project.getProjectDirectory().getPath() + "/src/main/webapp"; // NOI18N
            for (FileObject webRoot : webRoots) {
                boolean isDefault = webRoot.getPath().equals(projectDirPath);
                WebResourceGroup g = new WebResourceGroup(project, webRoot, TYPE_DOC_ROOT, getDisplayName(webRoot, isDefault));
                // put the /src/main/webapp first; see #248687
                if (isDefault) {
                    sourceGroups.add(0, g);
                } else {
                    sourceGroups.add(g);
                }
            }
        }
        return sourceGroups;
    }

    @NbBundle.Messages("LBL_WebPages=Web Pages")
    private String getDisplayName(FileObject webRoot, boolean isDefault) {
        // To preserve current behavior, don't show web root name in the node name for default "webapp"
        if (isDefault) {
            return LBL_WebPages();
        } else {
            return LBL_WebPages() + " (" + webRoot.getPath() + ")"; // NOI18N
        }
    }

    @Override
    public Set<Path> excludedFolders() {
        Set<Path> result = new HashSet<>();
        for (SourceGroup sourceGroup : getSourceGroups(TYPE_DOC_ROOT)) {
            result.add(FileUtil.toFile(sourceGroup.getRootFolder()).toPath());
        }
        return result;
    }

    /**
     * Wrapper class around {@link SourceGroup}.
     *
     * <p>
     * Implementing {@link Object#equals(java.lang.Object)} and {@link Object#hashCode()},
     * so it will be possible to track changes in declared Web resources.
     * </p>
     *
     * <p>
     * This class is <i>immutable</i> and thus <i>thread safe</i>.
     * </p>
     */
    private static class WebResourceGroup implements SourceGroup {

        private final SourceGroup group;
        private final Project project;


        private WebResourceGroup(Project project, FileObject webRoot, String name, String displayName) {
            this.project = project;
            this.group = GenericSources.group(project, webRoot, name, displayName, null, null);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.project != null ? this.project.hashCode() : 0);
            hash = 89 * hash + (this.group.getRootFolder() != null ? this.group.getRootFolder().hashCode() : 0);
            hash = 89 * hash + (this.group.getName() != null ? this.group.getName().hashCode() : 0);
            hash = 89 * hash + (this.group.getDisplayName() != null ? this.group.getDisplayName().hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final WebResourceGroup other = (WebResourceGroup) obj;
            if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
                return false;
            }
            if (this.group.getRootFolder() != other.group.getRootFolder() && (this.group.getRootFolder() == null || !this.group.getRootFolder().equals(other.group.getRootFolder()))) {
                return false;
            }
            if ((this.group.getName() == null) ? (other.group.getName() != null) : !this.group.getName().equals(other.group.getName())) {
                return false;
            }
            if ((this.group.getDisplayName() == null) ? (other.group.getDisplayName() != null) : !this.group.getDisplayName().equals(other.group.getDisplayName())) {
                return false;
            }
            return true;
        }

        @Override
        public FileObject getRootFolder() {
            return group.getRootFolder();
        }

        @Override
        public String getName() {
            return group.getName();
        }

        @Override
        public String getDisplayName() {
            return group.getDisplayName();
        }

        @Override
        public Icon getIcon(boolean opened) {
            return group.getIcon(opened);
        }

        @Override
        public boolean contains(FileObject file) {
            return group.contains(file);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            group.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            group.removePropertyChangeListener(listener);
        }
    }
}
