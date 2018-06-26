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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
                return webResourceRoots.toArray(new SourceGroup[webResourceRoots.size()]);
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
