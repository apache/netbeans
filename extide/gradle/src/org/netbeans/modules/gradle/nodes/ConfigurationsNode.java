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

package org.netbeans.modules.gradle.nodes;

import org.netbeans.modules.gradle.ActionProviderImpl;
import org.netbeans.modules.gradle.GradleArtifactStore;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.spi.GradleSettings;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Laszlo Kishalmi
 */
public class ConfigurationsNode extends AbstractNode {

    private final NbGradleProjectImpl project;
    private static final String REQUESTED_COMPONENT = "requestedComponent";

    @StaticResource
    private static final String LIBRARIES_ICON = "org/netbeans/modules/gradle/resources/libraries.png"; //NOI18N
    @StaticResource
    private static final String WARNING_BADGE = "org/netbeans/modules/gradle/resources/warning-badge.png"; //NOI18N
    @StaticResource
    private static final String SOURCES_BADGE = "org/netbeans/modules/gradle/resources/sources-badge.png"; //NOI18N
    @StaticResource
    private static final String JAVADOC_BADGE = "org/netbeans/modules/gradle/resources/javadoc-badge.png"; //NOI18N
    @StaticResource
    private static final String UNRESOLVED_ICON = "org/netbeans/modules/gradle/resources/empty.png"; //NOI18N
    @StaticResource
    private static final String ARTIFACT_ICON = "org/netbeans/modules/gradle/resources/module-artifact.png"; //NOI18N

    private final Action downloadSourcesAction;
    private final Action downloadJavadocAction;

    @NbBundle.Messages({
        "LBL_ConfigurationsNode=Configurations"
    })
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ConfigurationsNode(NbGradleProjectImpl project) {
        super(Children.create(new ConfigurationsChildren(project), true), Lookups.singleton(project));
        this.project = project;
        setName("configurations"); //NOI18N
        setDisplayName(Bundle.LBL_ConfigurationsNode());
        downloadSourcesAction = ActionProviderImpl.createCustomGradleAction(project, "Download Sources",
                ActionProviderImpl.COMMAND_DL_SOURCES, Lookups.singleton(RunUtils.simpleReplaceTokenProvider(REQUESTED_COMPONENT, "ALL"))); //NOI18N
        downloadJavadocAction = ActionProviderImpl.createCustomGradleAction(project, "Download Javadoc",
                ActionProviderImpl.COMMAND_DL_JAVADOC, Lookups.singleton(RunUtils.simpleReplaceTokenProvider(REQUESTED_COMPONENT, "ALL"))); //NOI18N
    }

    @Override
    public Image getIcon(int type) {
        GradleProject gp = project.getGradleProject();
        Image ret = ImageUtilities.loadImage(LIBRARIES_ICON);
        if (gp.getQuality().worseThan(Quality.FULL) || !gp.getBaseProject().isResolved()) {
            Image warn = ImageUtilities.loadImage(WARNING_BADGE);
            ret = ImageUtilities.mergeImages(ret, warn, 8, 0);
        }
        return ret;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            downloadSourcesAction,
            downloadJavadocAction
        };
    }

    @NbBundle.Messages({
        "HINT_ConfigurationsNode=Project Configurations",
        "HINT_ConfigurationsNodeUnresolved=There are unresolved configurations!"
    })
    @Override
    public String getShortDescription() {
        GradleProject gp = project.getGradleProject();
        return gp.getBaseProject().isResolved() ? Bundle.HINT_ConfigurationsNode() : Bundle.HINT_ConfigurationsNodeUnresolved();
    }

    private static class ConfigurationsChildren extends ChildFactory.Detachable<GradleConfiguration> implements PreferenceChangeListener, PropertyChangeListener {

        private final NbGradleProjectImpl project;

        public ConfigurationsChildren(NbGradleProjectImpl project) {
            this.project = project;
        }

        @Override
        protected Node createNodeForKey(GradleConfiguration conf) {
            AbstractNode ret = new AbstractNode(Children.create(new ConfigurationChildren(project, conf.getName()), true));
            ret.setName(conf.getName());
            ret.setShortDescription(conf.getDescription());
            StringBuilder displayName = new StringBuilder(conf.getName());
            if (!conf.getExtendsFrom().isEmpty()) {
                displayName.append(" [");
                String separator = "";
                for (GradleConfiguration ext : conf.getExtendsFrom()) {
                    displayName.append(separator);
                    displayName.append(ext.getName());
                    separator = ", ";
                }
                displayName.append(']');
            }
            ret.setDisplayName(displayName.toString());
            ret.setIconBaseWithExtension(LIBRARIES_ICON);
            return ret;
        }

        @Override
        protected boolean createKeys(List<GradleConfiguration> list) {
            boolean hideEmpty = GradleSettings.getDefault().isHideEmptyConfigurations();
            ArrayList<GradleConfiguration> ret = new ArrayList<>();
            for (GradleConfiguration conf : project.getGradleProject().getBaseProject().getConfigurations().values()) {
                if (!hideEmpty || !conf.isEmpty()) {
                    ret.add(conf);
                }
            }
            Collections.sort(ret);
            list.addAll(ret);
            return true;
        }

        @Override
        protected void removeNotify() {
            NbGradleProject.removePropertyChangeListener(project, this);
            GradleSettings.getDefault().getPreferences().removePreferenceChangeListener(this);
        }

        @Override
        protected void addNotify() {
            GradleSettings.getDefault().getPreferences().addPreferenceChangeListener(this);
            NbGradleProject.addPropertyChangeListener(project, this);
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (GradleSettings.PROP_HIDE_EMPTY_CONF.equals(evt.getKey())) {
                refresh(false);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            refresh(false);
    }

    }

    private static class ConfigurationChildren extends ChildFactory.Detachable<GradleDependency> implements PropertyChangeListener {

        private final NbGradleProjectImpl project;
        private final String configuration;

        public ConfigurationChildren(NbGradleProjectImpl project, String configuration) {
            this.project = project;
            this.configuration = configuration;
        }

        @NbBundle.Messages({
            "LBL_LocalDependenciesNode=Local Files",
        })
        @Override
        protected Node[] createNodesForKey(GradleDependency key) {
            GradleProject gp = project.getGradleProject();
            ArrayList<Node> ret = new ArrayList<>(1);
            switch (key.getType()) {
                case MODULE: {
                    GradleDependency.ModuleDependency dep = (GradleDependency.ModuleDependency) key;

                    for (File artifact : dep.getArtifacts()) {
                        FileObject fo = FileUtil.toFileObject(artifact);
                        if (fo != null) {
                            try {
                                DataObject dataObject = DataObject.find(fo);
                                ret.add(new ModuleFilterNode(project, dep, artifact, dataObject.getNodeDelegate().cloneNode()));
                            } catch (DataObjectNotFoundException ex) {
                                // Should not happen here
                            }
                        }
                    }
                    break;
                }
                case PROJECT: {
                    GradleDependency.ProjectDependency dep = (GradleDependency.ProjectDependency) key;
                    //TODO: Shouldn't be done here on AWT
                    File projDir = dep.getPath();
                    FileObject fo = FileUtil.toFileObject(projDir);
                    if (fo != null) {
                        try {
                            Project prj = ProjectManager.getDefault().findProject(fo);
                            if (prj != null && prj.getLookup().lookup(NbGradleProjectImpl.class) != null) {
                                NbGradleProjectImpl proj = (NbGradleProjectImpl) prj;
                                assert prj.getLookup().lookup(LogicalViewProvider.class) != null;
                                Node original = proj.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
                                ret.add(new SubProjectsNode.ProjectFilterNode(proj, original));
                            }
                        } catch (IllegalArgumentException | IOException ex) {
                            ex.printStackTrace();//TODO log ?
                        }
                    } else {
                        //TODO broken module reference.. show as such..
                    }
                    break;
                }
                case FILE: {
                    GradleDependency.FileCollectionDependency dep = (GradleDependency.FileCollectionDependency) key;
                    AbstractNode node = new AbstractNode(Children.create(new FileDependencyChildren(dep), true));
                    node.setDisplayName(Bundle.LBL_LocalDependenciesNode());
                    node.setIconBaseWithExtension(LIBRARIES_ICON);
                    ret.add(node);
                    break;
                }
                case UNRESOLVED: {
                    GradleDependency.UnresolvedDependency dep = (GradleDependency.UnresolvedDependency) key;

                    AbstractNode node = new AbstractNode(Children.LEAF);
                    node.setName(dep.getId());
                    node.setIconBaseWithExtension(UNRESOLVED_ICON);
                    ret.add(node);
                    break;
                }

            }
            return ret.toArray(new Node[ret.size()]);
        }

        @Override
        protected boolean createKeys(List<GradleDependency> list) {
            GradleProject gp = project.getGradleProject();
            ArrayList<GradleDependency> ret = new ArrayList<>();
            GradleConfiguration conf = gp.getBaseProject().getConfigurations().get(configuration);
            // We can get null here in some extreme cases, e.g. when the project is being deleted
            if (conf != null) {
                ret.addAll(conf.getUnresolved());
                ret.addAll(conf.getProjects());
                ret.addAll(conf.getModules());
                GradleDependency.FileCollectionDependency fileDeps = conf.getFiles();
                if ((fileDeps != null) && !fileDeps.getFiles().isEmpty()) {
                    ret.add(fileDeps);
                }
                Collections.sort(ret);
                list.addAll(ret);
            }
            return true;
        }

        @Override
        protected void removeNotify() {
            NbGradleProject.removePropertyChangeListener(project, this);
        }

        @Override
        protected void addNotify() {
            NbGradleProject.addPropertyChangeListener(project, this);

        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            refresh(false);
        }

    }

    private static class ModuleFilterNode extends FilterNode implements ChangeListener {

        private final NbGradleProjectImpl project;
        private final GradleDependency.ModuleDependency module;
        private final File mainJar;

        public ModuleFilterNode(NbGradleProjectImpl project, GradleDependency.ModuleDependency module, File mainJar, Node original) {
            super(original);
            this.project = project;
            this.module = module;
            this.mainJar = mainJar;
            GradleArtifactStore.getDefault().addChangeListener(this);
        }

        @Override
        public Action[] getActions(boolean context) {
            GradleArtifactStore store = GradleArtifactStore.getDefault();
            List<Action> actions = new ArrayList<>(3);
            actions.add(new OpenJavadocAction(FileUtil.toFileObject(mainJar)));
            if (store.getSources(mainJar) == null) {
                Action download = ActionProviderImpl.createCustomGradleAction(project, "Download Sources",
                        ActionProviderImpl.COMMAND_DL_SOURCES, Lookups.singleton(RunUtils.simpleReplaceTokenProvider(REQUESTED_COMPONENT, module.getId())));
                actions.add(download);
            }
            if (store.getJavadoc(mainJar) == null) {
                Action download = ActionProviderImpl.createCustomGradleAction(project, "Download Javadoc",
                        ActionProviderImpl.COMMAND_DL_JAVADOC, Lookups.singleton(RunUtils.simpleReplaceTokenProvider(REQUESTED_COMPONENT, module.getId())));
                actions.add(download);
            }
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getShortDescription() {
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("Artifact Id: <b>").append(module.getId()).append("</b><br/>");
            sb.append("File: ").append(mainJar.getAbsolutePath());
            return sb.toString();
        }

        @Override
        public String getDisplayName() {
            return module.getVersion().isEmpty() ? module.getName() : module.getName() + ":" + module.getVersion();
        }

        @Override
        public Image getIcon(int type) {
            GradleArtifactStore store = GradleArtifactStore.getDefault();
            Image ret = ImageUtilities.loadImage(ARTIFACT_ICON);
            if (store.getJavadoc(mainJar) != null) {
                Image javadoc = ImageUtilities.loadImage(JAVADOC_BADGE);
                ret = ImageUtilities.mergeImages(ret, javadoc, 0, 8);
            }
            if (store.getSources(mainJar) != null) {
                Image sources = ImageUtilities.loadImage(SOURCES_BADGE);
                ret = ImageUtilities.mergeImages(ret, sources, 8, 8);
            }
            return ret;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            fireIconChange();
        }

        @Override
        public void destroy() throws IOException {
            GradleArtifactStore.getDefault().removeChangeListener(this);
            super.destroy();
        }
    }

    private static class FileDependencyChildren extends ChildFactory<FileObject> {

        final GradleDependency.FileCollectionDependency dep;

        public FileDependencyChildren(GradleDependency.FileCollectionDependency dep) {
            this.dep = dep;
        }

        @Override
        protected boolean createKeys(List<FileObject> keys) {
            ArrayList<FileObject> ret = new ArrayList<>(dep.getFiles().size());
            for (File file : dep.getFiles()) {
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    ret.add(fo);
                }
            }
            ret.sort(new Comparator<FileObject>() {
                @Override
                public int compare(FileObject o1, FileObject o2) {
                    return o1.getNameExt().compareTo(o2.getNameExt());
                }
            });
            keys.addAll(ret);
            return true;
        }

        @Override
        protected Node createNodeForKey(FileObject key) {
            try {
                DataObject dataObject = DataObject.find(key);
                return new LocalFileFilterNode(key, dataObject.getNodeDelegate().cloneNode());
            } catch (DataObjectNotFoundException ex) {
                // Should not happen here
            }
            return null;
        }


    }

    private static class LocalFileFilterNode extends FilterNode {

        private final FileObject mainJar;

        public LocalFileFilterNode(FileObject mainJar, Node original) {
            super(original);
            this.mainJar = mainJar;
        }

        @Override
        public Action[] getActions(boolean context) {
            List<Action> actions = new ArrayList<>(3);
            actions.add(new OpenJavadocAction(mainJar));
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getShortDescription() {
            return mainJar.getPath();
        }

        @Override
        public Image getIcon(int type) {
            GradleArtifactStore store = GradleArtifactStore.getDefault();
            Image ret = super.getIcon(type);
            if (store.getJavadoc(FileUtil.toFile(mainJar)) != null) {
                Image javadoc = ImageUtilities.loadImage(JAVADOC_BADGE);
                ret = ImageUtilities.mergeImages(ret, javadoc, 0, 8);
            }
            if (store.getSources(FileUtil.toFile(mainJar)) != null) {
                Image sources = ImageUtilities.loadImage(SOURCES_BADGE);
                ret = ImageUtilities.mergeImages(ret, sources, 8, 8);
            }
            return ret;
        }

    }

    private static class OpenJavadocAction extends AbstractAction {

        private final FileObject mainJar;

        @NbBundle.Messages({
            "LBL_OpenJavadocAction=Show Javadoc"
        })
        public OpenJavadocAction(FileObject mainJar) {
            super(Bundle.LBL_OpenJavadocAction());
            this.mainJar = mainJar;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File javadoc = GradleArtifactStore.getDefault().getJavadoc(FileUtil.toFile(mainJar));
            FileObject fo = FileUtil.toFileObject(javadoc);
            if (fo != null) {
                FileObject arch = FileUtil.getArchiveRoot(fo);
                if (arch != null) {
                    FileObject index = arch.getFileObject("index.html"); //NOI18N
                    if (index != null) {
                        URL indexUrl = index.toURL();
                        HtmlBrowser.URLDisplayer.getDefault().showURL(indexUrl);
                    }
                }
            }
        }

        @Override
        public boolean isEnabled() {
            File javadoc = GradleArtifactStore.getDefault().getJavadoc(FileUtil.toFile(mainJar));
            return javadoc != null;
        }

    }
}
