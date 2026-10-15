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

package org.netbeans.modules.java.api.common.project.ui;

import java.awt.Image;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PathFinder;
import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Utilities;



/**
 * ProjectNode represents a dependent project under the Libraries Node.
 * It is a leaf node with the following actions: {@link OpenProjectAction},
 * {@link ShowJavadocAction} and {@link RemoveClassPathRootAction}
 * @author Tomas Zezula
 */
class ProjectNode extends AbstractNode {

    private static final String PROJECT_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/projectDependencies.gif";    //NOI18N

    private final AntArtifact antArtifact;
    private final URI artifactLocation;
    private Image cachedIcon;

    ProjectNode (
                @NonNull final AntArtifact antArtifact,
                @NonNull final URI artifactLocation,
                @NonNull final UpdateHelper helper,
                @NonNull final String classPathId,
                @NonNull final String entryId,
                @NullAllowed final String webModuleElementName, 
                @NonNull final ClassPathSupport cs,
                @NonNull final ReferenceHelper rh,
                @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
                @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
                boolean removeFromProject) {
        super (Children.LEAF, createLookup (
                antArtifact, artifactLocation, 
                helper, classPathId, entryId, webModuleElementName,
                cs, rh, preRemoveAction, postRemoveAction, removeFromProject));
        this.antArtifact = antArtifact;
        this.artifactLocation = artifactLocation;
    }

    @Override
    public String getDisplayName () {        
        ProjectInformation info = getProjectInformation();        
        if (info != null) {
            return NbBundle.getMessage(ProjectNode.class,"TXT_ProjectArtifactFormat",
                    new Object[] {info.getDisplayName(), artifactLocation.toString()});
        }
        else {
            return NbBundle.getMessage (ProjectNode.class,"TXT_UnknownProjectName");
        }
    }

    @Override
    public String getName () {
        return this.getDisplayName();
    }

    @Override
    public Image getIcon(int type) {
        if (cachedIcon == null) {
            ProjectInformation info = getProjectInformation();
            if (info != null) {
                Icon icon = info.getIcon();
                cachedIcon = ImageUtilities.icon2Image(icon);
            }
            else {
                cachedIcon = ImageUtilities.loadImage(PROJECT_ICON);
            }
        }
        return cachedIcon;
    }

    @Override
    public String getShortDescription() {
        final Project p = this.antArtifact.getProject();
        FileObject fo;
        if (p != null && (fo = p.getProjectDirectory()) != null) {
            return FileUtil.getFileDisplayName(fo);
        } else {
            return super.getShortDescription();
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return this.getIcon(type);
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get (OpenProjectAction.class),
            SystemAction.get (ShowJavadocAction.class),
            SystemAction.get (RemoveClassPathRootAction.class),
        };
    }

    @Override
    public Action getPreferredAction () {
        return getActions(false)[0];
    }
    
    private ProjectInformation getProjectInformation () {
        Project p = this.antArtifact.getProject();
        if (p != null) {
            return ProjectUtils.getInformation(p);
        }
        return null;
    }
    
    private static Lookup createLookup (
            @NonNull final AntArtifact antArtifact,
            @NonNull final URI artifactLocation, 
            @NonNull final UpdateHelper helper, 
            @NonNull final String classPathId,
            @NonNull final String entryId,
            @NullAllowed final String webModuleElementName,
            @NonNull final ClassPathSupport cs,
            @NonNull final ReferenceHelper rh,
            @NullAllowed final Consumer<Pair<String,String>> preRemoveAction,
            @NullAllowed final Consumer<Pair<String,String>> postRemoveAction,
            boolean removeFromProject) {
        Project p = antArtifact.getProject();
        Object[] content;
        if (p == null) {
            content = new Object[1];
        }
        else {
            content = new Object[4];
            content[1] = new JavadocProvider(antArtifact, artifactLocation);
            content[2] = p;
            content[3] = new PathFinderImpl();  //Needed by Source Inspect View to display errors in project reference
        }
        content[0] = new ActionFilterNode.Removable(
                helper, classPathId, entryId, webModuleElementName,
                cs, rh, preRemoveAction, postRemoveAction, removeFromProject);
        Lookup lkp = Lookups.fixed(content);
        return lkp;
    }

    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {

        private final AntArtifact antArtifact;
        private final URI artifactLocation;
        
        JavadocProvider (AntArtifact antArtifact, URI artifactLocation) {
            this.antArtifact = antArtifact;
            this.artifactLocation = artifactLocation;
        }


        @Override
        public boolean hasJavadoc() {
            return findJavadoc().size() > 0;
        }

        @Override
        public void showJavadoc() {
            Set<URL> us = findJavadoc();
            URL[] urls = us.toArray(new URL[0]);
            URL pageURL = ShowJavadocAction.findJavadoc("overview-summary.html",urls);
            if (pageURL == null) {
                pageURL = ShowJavadocAction.findJavadoc("index.html",urls);
            }
            ProjectInformation info = null;
            Project p = this.antArtifact.getProject ();
            if (p != null) {
                info = ProjectUtils.getInformation(p);
            }
            ShowJavadocAction.showJavaDoc (pageURL, info == null ?
                NbBundle.getMessage (ProjectNode.class,"TXT_UnknownProjectName") : info.getDisplayName());
        }
        
        private Set<URL> findJavadoc() {            
            File scriptLocation = this.antArtifact.getScriptLocation();            
            Set<URL> urls = new HashSet<URL>();
            try {
                URL artifactURL = BaseUtilities.normalizeURI(Utilities.toURI(scriptLocation).resolve(this.artifactLocation)).toURL();
                if (FileUtil.isArchiveFile(artifactURL)) {
                    artifactURL = FileUtil.getArchiveRoot(artifactURL);
                }
                urls.addAll(Arrays.asList(JavadocForBinaryQuery.findJavadoc(artifactURL).getRoots()));                
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);                
            }                                    
            return urls;
        }
        
    }

    private static class OpenProjectAction extends NodeAction {

        @Override
        protected void performAction(Node[] activatedNodes) {
            Project[] projects = new Project[activatedNodes.length];
            for (int i=0; i<projects.length;i++) {
                final Project p = getProject(activatedNodes[i]);
                if (p == null) {
                    //Should not happen, only for case when project is deleted after enabled called
                    return;
                }
                projects[i] = p;
            }
            OpenProjects.getDefault().open(projects, false);
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            final Collection<Project> openedProjects =Arrays.asList(OpenProjects.getDefault().getOpenProjects());
            for (int i=0; i<activatedNodes.length; i++) {
                final Project p = getProject (activatedNodes[i]);
                if (p == null) {
                    return false;
                }
                if (openedProjects.contains(p)) {
                    return false;
                }
            }
            return true;
        }
        
        private static Project getProject (final Node node) {
            assert node != null;
            final Project p = node.getLookup().lookup(Project.class);
            if (p != null) {
                final FileObject projectRoot = p.getProjectDirectory();
                if (projectRoot == null || !projectRoot.isValid()) {
                    return null;
                }
            }
            return p;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage (ProjectNode.class,"CTL_OpenProject");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx (OpenProjectAction.class);
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }
    }

    private static final class PathFinderImpl implements PathFinder {

        @Override
        public Node findPath(Node root, Object target) {
            final Project p = root.getLookup().lookup(Project.class);
            if (p != null && p.getProjectDirectory().equals(target)) {
                return root;
            }
            return null;
        }
    }

}
