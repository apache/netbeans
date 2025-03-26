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

package org.netbeans.modules.maven.nodes;

import org.netbeans.modules.maven.ModuleInfoSupport;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.util.FileUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.BasicAggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.maven.NbArtifactFixer;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.dependencies.DependencyExcludeNodeVisitor;
import org.netbeans.modules.maven.dependencies.ExcludeDependencyPanel;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Exclusion;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.ui.PathFinder;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.modules.maven.queries.RepositoryForBinaryQueryImpl;
import org.netbeans.modules.maven.queries.RepositoryForBinaryQueryImpl.Coordinates;
import org.netbeans.modules.maven.spi.IconResources;
import org.netbeans.modules.maven.spi.nodes.DependencyTypeIconBadge;
import org.netbeans.modules.maven.spi.queries.ForeignClassBundler;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.PropertiesAction;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * node representing a dependency
 * @author  Milos Kleint 
 */
public class DependencyNode extends AbstractNode implements PreferenceChangeListener {

    
    private boolean longLiving;
    private PropertyChangeListener listener;
    private ChangeListener listener2;
    final Data data;
    
    private volatile String iconBase = IconResources.DEPENDENCY_ICON;
    
    private static final String toolTipJavadoc = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(IconResources.JAVADOC_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_JavadocBadge");//NOI18N
    private static final String toolTipSource = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(IconResources.SOURCE_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_SourceBadge");//NOI18N
    private static final String toolTipMissing = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(IconResources.BROKEN_PROJECT_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_MissingBadge");//NOI18N
    private static final String toolTipManaged = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(IconResources.MANAGED_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_ManagedBadge");//NOI18N
    private static final String toolTipForeignBundler = "<img src=\"" + DependencyNode.class.getClassLoader().getResource(IconResources.BROKEN_PROJECT_BADGE_ICON) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(DependencyNode.class, "ICON_PreferSourcesBadge");//NOI18N
    
    private static final RequestProcessor RP = new RequestProcessor(DependencyNode.class);

    private static Children createChildren(@NullAllowed Node nodeDelegate) {
        return nodeDelegate == null ? Children.LEAF : new JarContentFilterChildren(nodeDelegate);
    }

    @NonNull
    private static Lookup createLookup(@NonNull final Project project, @NonNull final Artifact art, @NullAllowed final Node nodeDelegate, Supplier<Boolean> canAddToModuleInfo) {
        final PathFinder pathFinderDelegate = nodeDelegate == null ? null : nodeDelegate.getLookup().lookup(PathFinder.class);
        final FileObject fo = nodeDelegate == null ? null : nodeDelegate.getLookup().lookup(FileObject.class);
        if (fo != null) {
            return Lookups.fixed(new Data(art, project, fo, canAddToModuleInfo), project, art, PathFinders.createDelegatingPathFinder(pathFinderDelegate), fo);
        } else {
            return Lookups.fixed(new Data(art, project, null, canAddToModuleInfo), project, art, PathFinders.createDelegatingPathFinder(pathFinderDelegate));
        }
    }

    @CheckForNull
    static Node createNodeDelegate(@NonNull final Artifact art, FileObject fo, final boolean longLiving) {
        if (!longLiving) {
            return null;
        }
        //artifact.getFile() should be eagerly normalized
        if (fo != null && FileUtil.isArchiveFile(fo)) {
            return PackageView.createPackageView(new ArtifactSourceGroup(art));
        }
        return null;        
    }

    public DependencyNode(
            NbMavenProjectImpl project,
            final Artifact art,
            final FileObject fo,
            boolean isLongLiving,
            Node nodeDelegate, 
            Supplier<Boolean> canAddToModuleInfo) {
        super(createChildren(nodeDelegate), createLookup(project, art, nodeDelegate, canAddToModuleInfo));
        this.data = getLookup().lookup(Data.class);
        data.fileObject.set(fo);
        longLiving = isLongLiving;

        if (longLiving) {
            listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        refreshNode();
                    }
                }
            };
            NbMavenProject.addPropertyChangeListener(project, WeakListeners.propertyChange(listener, project.getProjectWatcher()));
            listener2 = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent event) {
                    if (event instanceof MavenFileOwnerQueryImpl.GAVCHangeEvent) {
                        MavenFileOwnerQueryImpl.GAVCHangeEvent ev = (MavenFileOwnerQueryImpl.GAVCHangeEvent) event;
                        if (ev.matches(art)) {
                            refreshNode();
                        }
                    } else {
                        refreshNode();
                    }
                }
            };
            //TODO check if this one is a performance bottleneck.
            MavenFileOwnerQueryImpl.getInstance().addChangeListener(
                    WeakListeners.change(listener2,
                    MavenFileOwnerQueryImpl.getInstance()));
            DependenciesNode.prefs().addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, DependenciesNode.prefs()));
        }
        setDisplayName(createName(false));
        setIconBase(false);
        if (longLiving) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    refreshNode();
                }
            });
        }
        data.setNode(this);
    }

    

    private void setIconBase(boolean longLiving) {
        String base;
        if (longLiving && data.isDependencyProjectAvailable()) {
            if (data.isTransitive()) {
                base = IconResources.TRANSITIVE_MAVEN_ICON;
            } else {
                base = IconResources.MAVEN_ICON;
            }
        } else if (data.isTransitive()) {
            if (data.isAddedToCP()) {
                base = IconResources.TRANSITIVE_DEPENDENCY_ICON;
            } else {
                base = IconResources.TRANSITIVE_ARTIFACT_ICON;
            }
        } else if (data.isAddedToCP()) {
            base = IconResources.DEPENDENCY_ICON;
        } else {
            base = IconResources.ARTIFACT_ICON;
        }
        this.iconBase = base;
        setIconBaseWithExtension(base);
    }

    @Messages({
        "DESC_Dep1=GroupId:",
        "DESC_Dep2=ArtifactId:",
        "DESC_Dep3=Version:",
        "DESC_Dep4=Type:",
        "DESC_Dep5=Classifier:",
        "DESC_scope=Scope:",
        "DESC_via=Via:"
    })
    @Override public String getShortDescription() {
        StringBuilder buf = new StringBuilder();
        buf.append("<html><i>").append(DESC_Dep1()).append("</i><b> ").append(data.art.getGroupId()).append("</b><br><i>"); //NOI18N
        buf.append(DESC_Dep2()).append("</i><b> ").append(data.art.getArtifactId()).append("</b><br><i>");//NOI18N
        buf.append(DESC_Dep3()).append("</i><b> ").append(data.art.getVersion()).append("</b><br><i>");//NOI18N
        buf.append(DESC_Dep4()).append("</i><b> ").append(data.art.getType()).append("</b><br>");//NOI18N
        if (data.art.getClassifier() != null) {
            buf.append("<i>").append(DESC_Dep5()).append("</i><b> ").append(data.art.getClassifier()).append("</b><br>");//NOI18N
        }
        buf.append("<i>").append(DESC_scope()).append("</i><b> ").append(data.art.getScope()).append("</b><br>");
        List<String> trail = data.art.getDependencyTrail();
        for (int i = trail.size() - 2; i > 0 && /* just to be safe */ i < trail.size(); i--) {
            String[] id = trail.get(i).split(":"); // g:a:t[:c]:v
            buf.append("<i>").append(DESC_via()).append("</i> ").append(id[1]).append("<br>");
        }
        // it seems that with ending </html> tag the icon descriptions are not added.
//        buf.append("</html>");//NOI18N
        return buf.toString();
    }

    

    
    private void refreshNode() {
        assert !SwingUtilities.isEventDispatchThread();
        FileObject fo = FileUtil.toFileObject(data.art.getFile());
        
        data.sourceExists.set(data.getSourceFile().exists());
        data.javadocExists.set(data.getJavadocFile().exists());
        data.fileObject.set(fo);
        setDisplayName(createName(longLiving));
        setIconBase(longLiving);
        fireIconChange();
        fireDisplayNameChange(null, getDisplayName());
//        if (longLiving) {
//            ((DependencyChildren)getChildren()).doRefresh();
//        }
        //#142784
        if (longLiving) {
            if (Children.LEAF == getChildren()) {
                final Node nodeDelegate = createNodeDelegate(data.art, fo, true);
                Children childs = createChildren(nodeDelegate);
                if (childs != Children.LEAF) {
                    setChildren(childs);
                    PathFinders.updateDelegate(getLookup().lookup(PathFinder.class), nodeDelegate.getLookup().lookup(PathFinder.class));
                }
            }
        }
    }

    @Override
    public String getHtmlDisplayName() {
        StringBuilder n = new StringBuilder("<html>");
        n.append(getDisplayName());
        if (ArtifactUtils.isSnapshot(data.art.getVersion()) && data.art.getVersion().indexOf("SNAPSHOT") < 0) { //NOI18N
            n.append(" <b>[").append(data.art.getVersion()).append("]</b>");
        }
        if (!data.art.getArtifactHandler().isAddedToClasspath() && !Artifact.SCOPE_COMPILE.equals(data.art.getScope())) {
            n.append("  <i>[").append(data.art.getScope()).append("]</i>");
        }
        n.append("</html>");
        return n.toString();
    }

    private String createName(boolean longLiving) {
        if (longLiving) {
            //TODO when the name changes on the other end (dep project) we have no way of knowing..
            Project prj = data.getDependencyProject();
            if (prj != null) {
                return ProjectUtils.getInformation(prj).getDisplayName();
            }
        }
        if (NbArtifactFixer.isFallbackFile(data.art.getFile())) {
            return data.art.getArtifactId() + "-" + data.art.getBaseVersion() + (data.art.getClassifier() != null ? "-" + data.art.getClassifier() : "") + "." + data.art.getArtifactHandler().getExtension();
        }
        return data.art.getFile().getName();
    }

    @Override
    public Action[] getActions(boolean context) {
        Collection<Action> acts = new ArrayList<Action>();
        if (longLiving && data.isDependencyProjectAvailable()) {
            acts.add(OpenProjectAction.SINGLETON);
        }
        boolean local = data.isLocal();
        if (data.isAddedToCP()) {
            InstallLocalArtifactAction act = new InstallLocalArtifactAction();
            acts.add(act);
            if (!local) {
                act.setEnabled(true);
            }
        }
        if (local) {
            acts.add(new CopyLocationAction());
        }

//        acts.add(new EditAction());
//        acts.add(RemoveDepAction.get(RemoveDepAction.class));
        if (!data.hasJavadocInRepository()) {
            acts.add(DOWNLOAD_JAVADOC_ACTION);
            if (data.isAddedToCP()) {
                acts.add(new InstallLocalJavadocAction());
            }
        }
        if (!data.hasSourceInRepository()) {
            acts.add(DOWNLOAD_SOURCE_ACTION);
            if (data.isAddedToCP()) {
                acts.add(new InstallLocalSourcesAction());
            }
        }
        if (data.isTransitive()) {
            acts.add(new ExcludeTransitiveAction());
            acts.add(SETINCURRENTINSTANCE);
        } else {
            acts.add(REMOVEDEPINSTANCE);
        }
        if (data.canAddToModuleInfo()) {             
            acts.add(ADDTOMODULEINFO);
        }
        
        acts.add(null);
        acts.add(CommonArtifactActions.createViewArtifactDetails(data.art, data.getMavenProject().getRemoteArtifactRepositories()));
        acts.add(CommonArtifactActions.createFindUsages(data.art));
        acts.add(null);
        acts.add(CommonArtifactActions.createViewJavadocAction(data.art));
        /* #164992: disabled
        acts.add(CommonArtifactActions.createViewProjectHomeAction(art, project.getOriginalMavenProject().getRemoteArtifactRepositories()));
        acts.add(CommonArtifactActions.createViewBugTrackerAction(art, project.getOriginalMavenProject().getRemoteArtifactRepositories()));
        acts.add(CommonArtifactActions.createSCMActions(art, project.getOriginalMavenProject().getRemoteArtifactRepositories()));
         */
        acts.add(null);
        acts.add(PropertiesAction.get(PropertiesAction.class));
        return acts.toArray(new Action[0]);
    }

    

    @Override
    public boolean canDestroy() {
        return !data.isTransitive();
    }
    @Override
    public void destroy() throws IOException {
        REMOVEDEPINSTANCE.createContextAwareInstance(getLookup()).actionPerformed(null);
    }

    @Override
    public boolean canRename() {
        return false;
    }

    
    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean hasCustomizer() {
        return false;
    }

    void downloadJavadocSources(ProgressContributor progress, boolean isjavadoc) {
        downloadJavadocSources(progress, isjavadoc, data.art, data.project);
        refreshNode();
    }

    /**
     * 
     * @param progress
     * @param isjavadoc
     * @param artifact
     * @param prj
     * @return the sources/javadoc artifact
     */
    public static Artifact downloadJavadocSources(ProgressContributor progress, boolean isjavadoc, Artifact artifact, Project prj) {
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        progress.start(2);
        if ( Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) {
            progress.finish();
            return null;
        }
        NbMavenProjectImpl prjimpl = prj.getLookup().lookup(NbMavenProjectImpl.class);
        Artifact sources = null;
        try {
            String classifier;
            String baseClassifier;
            String bundleName;
            if (isjavadoc) {
                if (artifact.getClassifier() != null) {
                    if ("tests".equals(artifact.getClassifier())) {
                        classifier = "test-javadoc";
                    } else {
                        classifier = artifact.getClassifier() + "-javadoc";
                    }
                } else {
                    classifier = "javadoc";
                }
                baseClassifier = "javadoc";
                bundleName = "MSG_Checking_Javadoc";
            } else {
                baseClassifier = "sources";
                if (artifact.getClassifier() != null) {
                    if ("tests".equals(artifact.getClassifier())) {
                        classifier = "test-sources";
                    } else {
                        classifier = artifact.getClassifier() + "-sources";
                    }
                } else {
                    classifier = "sources";
                }
                bundleName = "MSG_Checking_Sources";
            }
            
            sources = prjimpl.getEmbedder().createArtifactWithClassifier(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion(),
                artifact.getType(),
                classifier); 
            progress.progress(org.openide.util.NbBundle.getMessage(DependencyNode.class, bundleName,artifact.getId()), 1);
            online.resolveArtifact(sources, prjimpl.getOriginalMavenProject().getRemoteArtifactRepositories(), prjimpl.getEmbedder().getLocalRepository());
            if (artifact.getFile() != null && artifact.getFile().exists()) {
                List<Coordinates> coordinates = RepositoryForBinaryQueryImpl.getJarMetadataCoordinates(artifact.getFile());
                if (coordinates != null) {
                    for (Coordinates coordinate : coordinates) {
                        sources = prjimpl.getEmbedder().createArtifactWithClassifier(
                            coordinate.groupId,
                            coordinate.artifactId,
                            coordinate.version,
                            "jar",
                            baseClassifier);
                        progress.progress(org.openide.util.NbBundle.getMessage(DependencyNode.class, bundleName, artifact.getId()), 1);
                        online.resolveArtifact(sources, prjimpl.getOriginalMavenProject().getRemoteArtifactRepositories(), prjimpl.getEmbedder().getLocalRepository());
                    }
                }
            }
        } catch (ArtifactNotFoundException ex) {
            // just ignore..ex.printStackTrace();
        } catch (ArtifactResolutionException ex) {
            // just ignore..ex.printStackTrace();
        } finally {
            progress.finish();
        }
        return sources;
    }


    
    @Override
    public Image getIcon(int param) {
        return badge(super.getIcon(param));
    }
    
    private boolean isIconProjectBased() {
        String base = iconBase;
        return IconResources.TRANSITIVE_MAVEN_ICON.equals(base) || IconResources.MAVEN_ICON.equals(base);
    }

    private Image badge(Image retValue) {
        if (data.isLocal()) {
            if (!isIconProjectBased()) {
                if (data.hasJavadocInRepository()) {
                    Image ann = ImageUtilities.loadImage(IconResources.JAVADOC_BADGE_ICON); //NOI18N
                    ann = ImageUtilities.addToolTipToImage(ann, toolTipJavadoc);
                    retValue = ImageUtilities.mergeImages(retValue, ann, 12, 0);//NOI18N
                }
                if (data.hasSourceInRepository()) {
                    Image ann = ImageUtilities.loadImage(IconResources.SOURCE_BADGE_ICON); //NOI18N
                    ann = ImageUtilities.addToolTipToImage(ann, toolTipSource);
                    retValue = ImageUtilities.mergeImages(retValue, ann, 12, 8);//NOI18N
                }
            } else {
                Project p = data.getDependencyProject();
                if (p != null) {
                    ForeignClassBundler fcb = p.getLookup().lookup(ForeignClassBundler.class);
                    if (fcb != null && !fcb.preferSources()) {
                        Image ann = ImageUtilities.loadImage(IconResources.BROKEN_PROJECT_BADGE_ICON); //NOI18N
                        ann = ImageUtilities.addToolTipToImage(ann, toolTipForeignBundler);
                        retValue =  ImageUtilities.mergeImages(retValue, ann, 0, 0);//NOI18N
                    }
                }
            }
            if (showManagedState() && data.isManaged()) {
                Image ann = ImageUtilities.loadImage(IconResources.MANAGED_BADGE_ICON); //NOI18N
                ann = ImageUtilities.addToolTipToImage(ann, toolTipManaged);
                retValue = ImageUtilities.mergeImages(retValue, ann, 0, 8);//NOI18N
            }
            FileObject fo = data.fileObject.get();
            if (fo != null && fo.isValid()) {
                for (DependencyTypeIconBadge badge : Lookup.getDefault().lookupAll(DependencyTypeIconBadge.class)) {
                    Image ann = badge.getBadgeIcon(fo, data.art);
                    if (ann != null) {
                        retValue = ImageUtilities.mergeImages(retValue, ann, 0, 0);//NOI18N
                        break;
                    }
                }
            }
            return retValue;
        } else if (!isIconProjectBased()) {
            Image ann = ImageUtilities.loadImage(IconResources.BROKEN_PROJECT_BADGE_ICON); //NOI18N
            ann = ImageUtilities.addToolTipToImage(ann, toolTipMissing);
            return ImageUtilities.mergeImages(retValue, ann, 0, 0);//NOI18N
        } else {
            Project p = data.getDependencyProject();
            if (p != null) {
                ForeignClassBundler fcb = p.getLookup().lookup(ForeignClassBundler.class);
                if (fcb != null && !fcb.preferSources()) {
                    Image ann = ImageUtilities.loadImage(IconResources.BROKEN_PROJECT_BADGE_ICON); //NOI18N
                    ann = ImageUtilities.addToolTipToImage(ann, toolTipForeignBundler);
                    retValue = ImageUtilities.mergeImages(retValue, ann, 0, 0);//NOI18N
                }
            }
            return retValue;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return badge(super.getOpenedIcon(type));
    }

    @Override
    public Component getCustomizer() {
        return null;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set basicProps = sheet.get(Sheet.PROPERTIES);
        try {
            PropertySupport.Reflection artifactId = new PropertySupport.Reflection<String>(data.art, String.class, "getArtifactId", null); //NOI18N
            artifactId.setName("artifactId"); //NOI18N
            artifactId.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Artifact"));
            artifactId.setShortDescription(""); //NOI18N
            PropertySupport.Reflection groupId = new PropertySupport.Reflection<String>(data.art, String.class, "getGroupId", null); //NOI18N
            groupId.setName("groupId"); //NOI18N
            groupId.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Group"));
            groupId.setShortDescription(""); //NOI18N
            PropertySupport.Reflection version = new PropertySupport.Reflection<String>(data.art, String.class, "getVersion", null); //NOI18N
            version.setName("version"); //NOI18N
            version.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Version"));
            version.setShortDescription(org.openide.util.NbBundle.getMessage(DependencyNode.class, "HINT_Version"));
            PropertySupport.Reflection type = new PropertySupport.Reflection<String>(data.art, String.class, "getType", null); //NOI18N
            type.setName("type"); //NOI18N
            type.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Type"));
            PropertySupport.Reflection scope = new PropertySupport.Reflection<String>(data.art, String.class, "getScope", null); //NOI18N
            scope.setName("scope"); //NOI18N
            scope.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Scope"));
            PropertySupport.Reflection classifier = new PropertySupport.Reflection<String>(data.art, String.class, "getClassifier", null); //NOI18N
            classifier.setName("classifier"); //NOI18N
            classifier.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Classifier"));
            PropertySupport.Reflection hasJavadoc = new PropertySupport.Reflection<Boolean>(data, Boolean.TYPE, "hasJavadocInRepository", null); //NOI18N
            hasJavadoc.setName("javadoc"); //NOI18N
            hasJavadoc.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Javadoc_Locally"));
            PropertySupport.Reflection hasSources = new PropertySupport.Reflection<Boolean>(data, Boolean.TYPE, "hasSourceInRepository", null); //NOI18N
            hasSources.setName("sources"); //NOI18N
            hasSources.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Sources_Locally"));
            PropertySupport.Reflection transitive = new PropertySupport.Reflection<Boolean>(data, Boolean.TYPE, "isTransitive", null); //NOI18N
            transitive.setName("transitive"); //NOI18N
            transitive.setDisplayName(org.openide.util.NbBundle.getMessage(DependencyNode.class, "PROP_Transitive"));
            
            PropertySupport.Reflection path = new PropertySupport.Reflection<File>(data.art, File.class, "getFile", null); //NOI18N
            path.setName("path"); //NOI18N
            path.setDisplayName("Path");
            path.setShortDescription("Absolute path to the artifact in the local filesystem.");
            
            PropertySupport.Reflection repositorypath = new PropertySupport.Reflection<String>(data, String.class, "getArtifactRepositoryPath", null); //NOI18N
            repositorypath.setName("repositorypath"); //NOI18N
            repositorypath.setDisplayName("Repository Path");
            repositorypath.setShortDescription("Relative path within the local maven repository.");
            

            basicProps.put(new Node.Property[] {
                artifactId, groupId, version, type, scope, classifier, transitive, hasJavadoc, hasSources, path, repositorypath
            });
        } catch (NoSuchMethodException exc) {
            exc.printStackTrace();
        }
        return sheet;
    }
    
    
    
//    private class DownloadJavadocAndSourcesAction extends AbstractAction implements Runnable {
//        public DownloadJavadocAndSourcesAction() {
//            putValue(Action.NAME, "Download Javadoc & Source");
//        }
//
//        public void actionPerformed(ActionEvent event) {
//            RP.post(this);
//        }
//
//        public void run() {
//            ProgressContributor contrib = AggregateProgressFactory.createProgressContributor("single"); //NOI18N
//            AggregateProgressHandle handle = AggregateProgressFactory.createHandle("Download Javadoc and Sources", new ProgressContributor[] {contrib}, null, null);
//            handle.start();
//            downloadJavadocSources(EmbedderFactory.getOnlineEmbedder(), contrib);
//            handle.finish();
//        }
//    }

    //extract the model out of the node class
    public static class Data {

        private final Artifact art;
        private final Project project;
        private final Supplier<Boolean> canAddToModuleInfo;
        private java.util.concurrent.atomic.AtomicReference<FileObject> fileObject;
        private final java.util.concurrent.atomic.AtomicBoolean sourceExists = new AtomicBoolean(false);
        private final java.util.concurrent.atomic.AtomicBoolean javadocExists = new AtomicBoolean(false);
        private DependencyNode node;

        public Data(Artifact art, Project project, FileObject fileObject, Supplier<Boolean> canAddToModuleInfo) {
            this.art = art; 
            this.project = project;
            this.fileObject = new AtomicReference<>(fileObject);
            this.canAddToModuleInfo = canAddToModuleInfo;
        }

        public String getArtifactRepositoryPath() {
            return EmbedderFactory.getProjectEmbedder().getLocalRepository().pathOf(art);
        }
        
        public boolean isLocal() {
            FileObject fo = fileObject.get();
            return fo != null && fo.isValid() && !NbArtifactFixer.isFallbackFile(art.getFile());
        }

        public boolean hasJavadocInRepository() {
            return javadocExists.get() && (!Artifact.SCOPE_SYSTEM.equals(art.getScope()));
        }

        //normalized
        public File getJavadocFile() {
            File artifact = art.getFile();
            String version = artifact.getParentFile().getName();
            String artifactId = artifact.getParentFile().getParentFile().getName();
            return new File(artifact.getParentFile(), artifactId + "-" + version + (art.getClassifier() != null ? ("tests".equals(art.getClassifier()) ? "-test" : "-" + art.getClassifier()) : "") + "-javadoc.jar"); //NOI18N
        }

        //normalized
        public File getSourceFile() {
            File artifact = art.getFile();
            String version = artifact.getParentFile().getName();
            String artifactId = artifact.getParentFile().getParentFile().getName();
            return new File(artifact.getParentFile(), artifactId + "-" + version + (art.getClassifier() != null ? ("tests".equals(art.getClassifier()) ? "-test" : "-" + art.getClassifier()) : "") + "-sources.jar"); //NOI18N
        }

        public boolean hasSourceInRepository() {
            return sourceExists.get() && (!Artifact.SCOPE_SYSTEM.equals(art.getScope()));
        }
        
        MavenProject getMavenProject() {
            return project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        }

        /**
         * public because of the property sheet
         */
        public boolean isTransitive() {
            List trail = art.getDependencyTrail();
            return trail != null && trail.size() > 2;
        }

        boolean canAddToModuleInfo() {
            return canAddToModuleInfo.get();
        }
        
        public boolean isManaged() {
            DependencyManagement dm = project.getLookup().lookup(NbMavenProject.class).getMavenProject().getDependencyManagement();
            if (dm != null) {
                List<Dependency> dmList = dm.getDependencies();
                for (Dependency d : dmList) {
                    if (art.getGroupId().equals(d.getGroupId())
                            && art.getArtifactId().equals(d.getArtifactId())) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isAddedToCP() {
            return art.getArtifactHandler().isAddedToClasspath();
        }

        /**
         * this call is slow
         *
         * @return
         */
        private boolean isDependencyProjectAvailable() {
            return getDependencyProject() != null;
        }

        /**
         * this call is slow
         *
         * @return
         */
        private Project getDependencyProject() {
            if (Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
                return null;
            }
            URI uri = org.openide.util.Utilities.toURI(art.getFile());
            // TODO should be patched, along with other uses of Artifact.getFile among nodes
            return FileOwnerQuery.getOwner(uri);
        }

        private void setNode(DependencyNode aThis) {
            node = aThis;
        }

        public DependencyNode getNode() {
            return node;
        }
        
    }

    
    //why oh why do we have to suffer through this??
    private static final RemoveDependencyAction REMOVEDEPINSTANCE = new RemoveDependencyAction(Lookup.EMPTY);

    @Messages("BTN_Remove_Dependency=Remove Dependency")
    private static class RemoveDependencyAction extends AbstractAction implements ContextAwareAction {

        private final Lookup lkp;

        RemoveDependencyAction(Lookup look) {
            putValue(Action.NAME, BTN_Remove_Dependency());
            lkp = look;
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<NbMavenProjectImpl>(res);
            if (prjs.size() != 1) {
                setEnabled(false);
            }
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new RemoveDependencyAction(actionContext);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            final Collection<? extends Artifact> artifacts = lkp.lookupAll(Artifact.class);
            if (artifacts.isEmpty()) {
                return;
            }
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<NbMavenProjectImpl>(res);
            if (prjs.size() != 1) {
                return;
            }

            final NbMavenProjectImpl project = prjs.iterator().next();
            final List<Artifact> unremoved = new ArrayList<Artifact>();

            final ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    for (Artifact art : artifacts) {
                        org.netbeans.modules.maven.model.pom.Dependency dep =
                                model.getProject().findDependencyById(art.getGroupId(), art.getArtifactId(), null);
                        if (dep != null) {
                            model.getProject().removeDependency(dep);
                        } else {
                            unremoved.add(art);
                        }
                    }
                }
            };
            RP.post(new Runnable() {
                @Override
                public void run() {
                    FileObject fo = FileUtil.toFileObject(project.getPOMFile());
                    Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
                    if (unremoved.size() > 0) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DependencyNode.class, "MSG_Located_In_Parent", unremoved.size()), Integer.MAX_VALUE);
                    }
                }
            });
        }
    }

    private static final String SHOW_MANAGED_DEPENDENCIES = "show.managed.dependencies";

    private static boolean showManagedState() {
        return DependenciesNode.prefs().getBoolean(SHOW_MANAGED_DEPENDENCIES, false);
    }

    @Override public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().equals(SHOW_MANAGED_DEPENDENCIES)) {
            refreshNode();
        }
    }

    static class ShowManagedStateAction extends AbstractAction implements Presenter.Popup {

        @NbBundle.Messages("LBL_ShowManagedState=Show Managed State for dependencies")
        ShowManagedStateAction() {
            String s = LBL_ShowManagedState();
            putValue(Action.NAME, s);
        }

        @Override public void actionPerformed(ActionEvent e) {
            DependenciesNode.prefs().putBoolean(SHOW_MANAGED_DEPENDENCIES, !showManagedState());
        }

        @Override public JMenuItem getPopupPresenter() {
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem(this);
            mi.setSelected(showManagedState());
            return mi;
        }

    }
    @Messages({"BTN_Exclude_Dependency=Exclude Dependency",
              "TIT_Exclude=Exclude Transitive Dependency"})
    private class ExcludeTransitiveAction extends AbstractAction {

        public ExcludeTransitiveAction() {
            putValue(Action.NAME, BTN_Exclude_Dependency());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    org.apache.maven.shared.dependency.tree.DependencyNode rootnode = DependencyTreeFactory.createDependencyTree(data.getMavenProject(), EmbedderFactory.getOnlineEmbedder(), Artifact.SCOPE_TEST);
                    DependencyExcludeNodeVisitor nv = new DependencyExcludeNodeVisitor(data.art.getGroupId(), data.art.getArtifactId(), data.art.getType());
                    rootnode.accept(nv);
                    final Set<org.apache.maven.shared.dependency.tree.DependencyNode> nds = nv.getDirectDependencies();
                    Collection<org.apache.maven.shared.dependency.tree.DependencyNode> directs;
                    if (nds.size() > 1) {
                        final ExcludeDependencyPanel pnl = new ExcludeDependencyPanel(data.getMavenProject(), data.art, nds, rootnode);
                        DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Exclude());
                        Object ret = DialogDisplayer.getDefault().notify(dd);
                        if (ret == DialogDescriptor.OK_OPTION) {
                            directs = pnl.getDependencyExcludes().get(data.art);
                        } else {
                            return;
                        }
                    } else {
                        directs = nds;
                    }
                    runModifyExclusions(data.art, directs);
                }
            });
        }

        private void runModifyExclusions(final Artifact art, final Collection<org.apache.maven.shared.dependency.tree.DependencyNode> nds) {
            ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    for (org.apache.maven.shared.dependency.tree.DependencyNode nd : nds) {
                        Artifact directArt = nd.getArtifact();
                        org.netbeans.modules.maven.model.pom.Dependency dep = model.getProject().findDependencyById(directArt.getGroupId(), directArt.getArtifactId(), null);
                        if (dep == null) {
                            // now check the active profiles for the dependency..
                            List<String> profileNames = new ArrayList<String>();
                            Iterator it = data.getMavenProject().getActiveProfiles().iterator();
                            while (it.hasNext()) {
                                Profile prof = (Profile) it.next();
                                profileNames.add(prof.getId());
                            }
                            for (String profileId : profileNames) {
                                org.netbeans.modules.maven.model.pom.Profile modProf = model.getProject().findProfileById(profileId);
                                if (modProf != null) {
                                    dep = modProf.findDependencyById(directArt.getGroupId(), directArt.getArtifactId(), null);
                                    if (dep != null) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (dep == null) {
                            // relocation maybe?
                            List<org.netbeans.modules.maven.model.pom.Dependency> deps = model.getProject().getDependencies();
                            for (org.netbeans.modules.maven.model.pom.Dependency d : deps) {
                                Relocation rel = getRelocation(d);
                                if(rel != null && 
                                   rel.getArtifactId().equals(directArt.getArtifactId()) &&
                                   rel.getGroupId().equals(directArt.getGroupId()) &&
                                   rel.getVersion().equals(directArt.getVersion())) 
                                {
                                    dep = d;
                                    break;    
                                }
                            }                            
                        }                        
                        if (dep == null) {
                            dep = model.getFactory().createDependency();
                            dep.setGroupId(directArt.getGroupId());
                            dep.setArtifactId(directArt.getArtifactId());
                            dep.setVersion(directArt.getVersion());
                            if (!"jar".equals(directArt.getType())) {
                                dep.setType(directArt.getType());
                            }
                            model.getProject().addDependency(dep);
                            //mkleint: TODO why is the dependency being added? i forgot already..
                        }
                        Exclusion ex = dep.findExclusionById(art.getGroupId(), art.getArtifactId());
                        if (ex == null) {
                            Exclusion exclude = model.getFactory().createExclusion();
                            exclude.setGroupId(art.getGroupId());
                            exclude.setArtifactId(art.getArtifactId());
                            dep.addExclusion(exclude);
                        }
                    }
                }
            };
            FileObject fo = FileUtil.toFileObject(data.project.getLookup().lookup(NbMavenProjectImpl.class).getPOMFile());
            org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
        }
    }
    
    private Relocation getRelocation(org.netbeans.modules.maven.model.pom.Dependency d) {
        ProjectBuildingRequest dpbr = EmbedderFactory.getProjectEmbedder().createMavenExecutionRequest().getProjectBuildingRequest();
        dpbr.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        dpbr.setProcessPlugins(false);
        dpbr.setResolveDependencies(false);
        ArrayList<ArtifactRepository> remoteRepos = new ArrayList<>();
        dpbr.setRemoteRepositories(remoteRepos);
        String groupId = d.getGroupId();
        String artifactId = d.getArtifactId();
        String version = d.getVersion();
        if(groupId != null && !"".equals(groupId.trim()) &&
           artifactId != null && !"".equals(artifactId.trim()) &&
           version != null && !"".equals(version.trim())) 
        {           
            MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
            Artifact a = embedder.createProjectArtifact(groupId, artifactId, version);
            try {
                ProjectBuildingResult r = embedder.buildProject(a, dpbr);
                DistributionManagement dm = r.getProject().getDistributionManagement();
                return dm != null ? dm.getRelocation() : null;
            } catch (ProjectBuildingException ex) {
                // just log and hope for the best ...
                Logger.getLogger(DependencyNode.class.getName()).log(Level.INFO, version, ex);                
            }
        }
        return null;
    }    
    
    private static Action DOWNLOAD_JAVADOC_ACTION = new DownloadJavadocSrcAction(true);
    private static Action DOWNLOAD_SOURCE_ACTION = new DownloadJavadocSrcAction(false);
    
    
    @SuppressWarnings("serial")
    private static class DownloadJavadocSrcAction extends AbstractAction implements ContextAwareAction {
        private final boolean javadoc;
        private final Lookup actionContext;
        public DownloadJavadocSrcAction(boolean javadoc) {
            this(javadoc, Lookup.EMPTY);
        }

        private DownloadJavadocSrcAction(boolean javadoc, Lookup actionContext) {
            putValue(Action.NAME, javadoc ? LBL_Download_Javadoc() : LBL_Download__Sources());
            this.javadoc = javadoc;
            this.actionContext = actionContext;
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            
            return new DownloadJavadocSrcAction(javadoc, actionContext);
        }       
        
        @Override
        public void actionPerformed(ActionEvent evnt) {
            if (actionContext == null) {
                return;
            }
            RP.post(new Runnable() {
                public @Override void run() {
                    ProgressContributor contributor = BasicAggregateProgressFactory.createProgressContributor("multi-1");
                   
                    String label = javadoc ? Progress_Javadoc() : Progress_Source();
                    AggregateProgressHandle handle = BasicAggregateProgressFactory.createHandle(label, 
                            new ProgressContributor [] {contributor}, ProgressTransferListener.cancellable(), null);
                    handle.start();
                    try {
                        ProgressTransferListener.setAggregateHandle(handle);
                        for (Data data : actionContext.lookupAll(Data.class)) {
                            ProgressContributor contributor2 = BasicAggregateProgressFactory.createProgressContributor("multi-1");
                            handle.addContributor(contributor2);
                            if (javadoc && !data.hasJavadocInRepository()) {
                                data.getNode().downloadJavadocSources(contributor2, javadoc);
                            } else if (!javadoc && !data.hasSourceInRepository()) {
                                data.getNode().downloadJavadocSources(contributor2, javadoc);
                            } else {
                                contributor2.finish();
                            }
                        }
                        
                    } catch (ThreadDeath d) { // download interrupted
                    } catch (IllegalStateException ise) { //download interrupted in dependent thread. #213812
                        if (!(ise.getCause() instanceof ThreadDeath)) {
                            throw ise;
                        }
                    } finally {
                        handle.finish();
                        ProgressTransferListener.clearAggregateHandle();
                    }
                }
            });
        }
    } 
    
    //why oh why do we have to suffer through this??
    private static final SetInCurrentAction SETINCURRENTINSTANCE = new SetInCurrentAction(Lookup.EMPTY);

    @Messages("BTN_Set_Dependency=Declare as Direct Dependency")
    private static class SetInCurrentAction extends AbstractAction  implements ContextAwareAction {
        private final Lookup lkp;

        SetInCurrentAction(Lookup lookup) {
            putValue(Action.NAME, BTN_Set_Dependency());
            lkp = lookup;
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<NbMavenProjectImpl>(res);
            if (prjs.size() != 1) {
                setEnabled(false);
            }
        }
        
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new SetInCurrentAction(actionContext);
        }


        @Override
        public void actionPerformed(ActionEvent event) {
            final Collection<? extends Artifact> artifacts = lkp.lookupAll(Artifact.class);
            if (artifacts.isEmpty()) {
                return;
            }
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<NbMavenProjectImpl>(res);
            if (prjs.size() != 1) {
                return;
            }
            final NbMavenProjectImpl project = prjs.iterator().next();

            final ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    for (Artifact art : artifacts) {
                        org.netbeans.modules.maven.model.pom.Dependency dep = model.getProject().findDependencyById(art.getGroupId(), art.getArtifactId(), null);
                        if (dep == null) {
                            // now check the active profiles for the dependency..
                            List<String> profileNames = new ArrayList<String>();
                            Iterator it = project.getOriginalMavenProject().getActiveProfiles().iterator();
                            while (it.hasNext()) {
                                Profile prof = (Profile) it.next();
                                profileNames.add(prof.getId());
                            }
                            for (String profileId : profileNames) {
                                org.netbeans.modules.maven.model.pom.Profile modProf = model.getProject().findProfileById(profileId);
                                if (modProf != null) {
                                    dep = modProf.findDependencyById(art.getGroupId(), art.getArtifactId(), null);
                                    if (dep != null) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (dep == null) {
                            dep = model.getFactory().createDependency();
                            dep.setGroupId(art.getGroupId());
                            dep.setArtifactId(art.getArtifactId());
                            dep.setVersion(art.getVersion());
                            if (!"jar".equals(art.getType())) {
                                dep.setType(art.getType());
                            }
                            if (!Artifact.SCOPE_COMPILE.equals(art.getScope())) {
                                dep.setScope(art.getScope());
                            }
                            if (art.getClassifier() != null) {
                                dep.setClassifier(art.getClassifier());
                            }
                            model.getProject().addDependency(dep);
                        }
                    }
                }
            };
            RP.post(new Runnable() {
                @Override
                public void run() {
                    FileObject fo = FileUtil.toFileObject(project.getPOMFile());
                    org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
                }
            });
        }

    }
    
    private static final AddToModuleInfoAction ADDTOMODULEINFO = new AddToModuleInfoAction(Lookup.EMPTY);

    @Messages("BTN_AddToModuleInfo=Add to ModuleInfo")
    private static class AddToModuleInfoAction extends AbstractAction  implements ContextAwareAction {
        private final Lookup lkp;

        AddToModuleInfoAction(Lookup lookup) {
            putValue(Action.NAME, BTN_AddToModuleInfo());
            lkp = lookup;
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<>(res);
            if (prjs.size() != 1) {
                setEnabled(false);
            }
        }
        
        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new AddToModuleInfoAction(actionContext);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            final Collection<? extends Artifact> artifacts = lkp.lookupAll(Artifact.class);
            if (artifacts.isEmpty()) {
                return;
            }
            Collection<? extends NbMavenProjectImpl> res = lkp.lookupAll(NbMavenProjectImpl.class);
            Set<NbMavenProjectImpl> prjs = new HashSet<>(res);
            if (prjs.size() != 1) {
                return;
            }
            ModuleInfoSupport.addRequires(prjs.iterator().next().getOriginalMavenProject(), artifacts);
        }
    }

    @Messages("BTN_Manually_install=Manually install artifact")
    private class InstallLocalArtifactAction extends AbstractAction {

        public InstallLocalArtifactAction() {
            putValue(Action.NAME, BTN_Manually_install());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            File fil = InstallPanel.showInstallDialog(data.art);
            if (fil != null) {
                InstallPanel.runInstallGoal(data.project.getLookup().lookup(NbMavenProjectImpl.class), fil, data.art);
            }
        }
    }

    private class CopyLocationAction extends AbstractAction {

        @Messages("CopyLocationAction.name=Copy Location")
        CopyLocationAction() {
            super(CopyLocationAction_name());
        }

        @Override public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data.art.getFile().getAbsolutePath()), null);
        }

    }

    @Messages("BTN_Add_javadoc=Add local Javadoc")
    private class InstallLocalJavadocAction extends AbstractAction implements Runnable {

        private File source;

        public InstallLocalJavadocAction() {
            putValue(Action.NAME, BTN_Add_javadoc());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            source = InstallDocSourcePanel.showInstallDialog(true);
            if (source != null) {
                RP.post(this);
            }
        }

        @Override
        public void run() {
            File target = data.getJavadocFile();
            try {
                FileUtils.copyFile(source, target);
            } catch (IOException ex) {
                ex.printStackTrace();
                target.delete();
            }
            refreshNode();
        }
    }

    @Messages("BTN_Add_sources=Add local sources")
    private class InstallLocalSourcesAction extends AbstractAction implements Runnable {

        private File source;

        public InstallLocalSourcesAction() {
            putValue(Action.NAME, BTN_Add_sources());
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            source = InstallDocSourcePanel.showInstallDialog(false);
            if (source != null) {
                RP.post(this);
            }
        }

        @Override
        public void run() {
            File target = data.getSourceFile();
            try {
                FileUtils.copyFile(source, target);
            } catch (IOException ex) {
                ex.printStackTrace();
                target.delete();
            }
            refreshNode();
        }
    }

//    private class EditAction extends AbstractAction {
//        public EditAction() {
//            putValue(Action.NAME, "Edit...");
//        }
//
//        public void actionPerformed(ActionEvent event) {
//
//            DependencyEditor ed = new DependencyEditor(project, change);
//            DialogDescriptor dd = new DialogDescriptor(ed, "Edit Dependency");
//            Object ret = DialogDisplayer.getDefault().notify(dd);
//            if (ret == NotifyDescriptor.OK_OPTION) {
//                HashMap props = ed.getProperties();
//                MavenSettings.getDefault().checkDependencyProperties(props.keySet());
//                change.setNewValues(ed.getValues(), props);
//                try {
//                    NbProjectWriter writer = new NbProjectWriter(project);
//                    List changes = (List)getLookup().lookup(List.class);
//                    writer.applyChanges(changes);
//                } catch (Exception exc) {
//                    ErrorManager.getDefault().notify(ErrorManager.USER, exc);
//                }
//            }
//        }
//    }
//
    

    private static class ArtifactSourceGroup implements SourceGroup {

        private final Artifact art;

        public ArtifactSourceGroup(Artifact art) {
            this.art = art;
        }

        //art.getFile() should be normalized
        @Override
        public FileObject getRootFolder() {
            FileObject fo = FileUtil.toFileObject(art.getFile());
            if (fo != null) {
                return FileUtil.getArchiveRoot(fo);
            }
            return null;
        }

        @Override
        public String getName() {
            return art.getId();
        }

        @Override
        public String getDisplayName() {
            return art.getId();
        }

        @Override
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override public boolean contains(FileObject file) {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    private static class JarContentFilterChildren extends FilterNode.Children {

        JarContentFilterChildren(Node orig) {
            super(orig);
        }

        @Override
        protected Node copyNode(Node node) {
            return new JarFilterNode(node);
        }
    }

    private static class JarFilterNode extends FilterNode {

        JarFilterNode(Node original) {
            super(original, Children.LEAF == original.getChildren() ?
                            Children.LEAF : new JarContentFilterChildren(original));
        }

        @Override
        public Action[] getActions(boolean context) {
            List<Action> result = new ArrayList<Action>();
            result.addAll(Arrays.asList(super.getActions(false)));
            result.add(new OpenJavadocAction());

            return result.toArray(new Action[0]);
        }

        @Messages("BTN_View_Javadoc=Show Javadoc")
        private class OpenJavadocAction extends AbstractAction {

            private OpenJavadocAction() {
                putValue(Action.NAME, BTN_View_Javadoc());
            }

            @Override
            @Messages({
                "# {0} - artifact path",
                "ERR_No_Javadoc_Found=Javadoc for {0} not found."})
            public void actionPerformed(ActionEvent e) {
                DataObject dobj = getOriginal().getLookup().lookup(DataObject.class);
                if (dobj == null) {
                    return;
                }
                FileObject fil = dobj.getPrimaryFile();
                FileObject jar = FileUtil.getArchiveFile(fil);
                FileObject root = FileUtil.getArchiveRoot(jar);
                String rel = FileUtil.getRelativePath(root, fil);
                rel = rel.replaceAll("[.]class$", ".html"); //NOI18N
                JavadocForBinaryQuery.Result res = JavadocForBinaryQuery.findJavadoc(root.toURL());
                if (fil.isFolder()) {
                    rel = rel + "/package-summary.html"; //NOI18N
                }
                URL javadocUrl = findJavadoc(rel, res.getRoots());
                if (javadocUrl != null) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(javadocUrl);
                } else {
                    StatusDisplayer.getDefault().setStatusText(ERR_No_Javadoc_Found(fil.getPath()));
                }
            }

            /**
             * Locates a javadoc page by a relative name and an array of javadoc roots
             * @param resource the relative name of javadoc page
             * @param urls the array of javadoc roots
             * @return the URL of found javadoc page or null if there is no such a page.
             */
            URL findJavadoc(String resource, URL[] urls) {
                for (int i = 0; i < urls.length; i++) {
                    String base = urls[i].toExternalForm();
                    if (!base.endsWith("/")) { // NOI18N
                        base = base + "/"; // NOI18N
                    }
                    try {
                        URL u = new URL(base + resource);
                        FileObject fo = URLMapper.findFileObject(u);
                        if (fo != null) {
                            return u;
                        }
                    } catch (MalformedURLException ex) {
//                            ErrorManager.getDefault().log(ErrorManager.ERROR, "Cannot create URL for "+base+resource+". "+ex.toString());   //NOI18N
                        continue;
                    }
                }
                return null;
            }

        }
    }

    private static class OpenProjectAction extends AbstractAction implements ContextAwareAction {

        static final OpenProjectAction SINGLETON = new OpenProjectAction();

        private OpenProjectAction() {}

        public @Override void actionPerformed(ActionEvent e) {
            assert false;
        }

        public @Override Action createContextAwareInstance(final Lookup context) {
            return new AbstractAction(BTN_Open_Project()) {
                public @Override void actionPerformed(ActionEvent e) {
                    Set<Project> projects = new HashSet<Project>();
                    for (Artifact art : context.lookupAll(Artifact.class)) {
                        File f = art.getFile();
                        if (f != null) {
                            Project p = FileOwnerQuery.getOwner(org.openide.util.Utilities.toURI(f));
                            if (p != null) {
                                projects.add(p);
                            }
                        }
                    }
                    OpenProjects.getDefault().open(projects.toArray(new NbMavenProjectImpl[0]), false, true);
                }
            };
        }
    }

}
