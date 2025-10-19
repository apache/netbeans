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

package org.netbeans.modules.maven.repository.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerPanelProvider;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.repository.dependency.AddAsDependencyAction;
import static org.netbeans.modules.maven.repository.ui.Bundle.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author mkleint
 */
@ServiceProvider( service=ArtifactViewerFactory.class )
public final class ArtifactMultiViewFactory implements ArtifactViewerFactory {

    private static final RequestProcessor RP = new RequestProcessor(ArtifactMultiViewFactory.class);

    @Override @NonNull public Lookup createLookup(@NonNull Artifact artifact, @NullAllowed List<ArtifactRepository> repos) {
        return createViewerLookup(null, artifact, repos);
    }
    @Override @NonNull public Lookup createLookup(@NonNull NBVersionInfo info) {
        return createViewerLookup(info, RepositoryUtil.createArtifact(info), null);
    }

    @Override @CheckForNull public Lookup createLookup(@NonNull Project prj) {
        NbMavenProject mvPrj = prj.getLookup().lookup(NbMavenProject.class);
        MavenProject mvn = mvPrj.getMavenProject();
        Artifact artifact = mvn.getArtifact();
        //artifact null with unloadable projects??
        return artifact != null ? createPomEditorLookup(prj, artifact) : null;
    }

    @Override @NonNull public TopComponent createTopComponent(@NonNull Lookup lookup) {
        Artifact artifact = lookup.lookup(Artifact.class);
        assert artifact != null;
        TopComponent existing = findExistingTc(artifact);
        if (existing != null) {
            return existing;
        }
        Collection<? extends ArtifactViewerPanelProvider> provs = Lookup.getDefault().lookupAll(ArtifactViewerPanelProvider.class);
        MultiViewDescription[] panels = new MultiViewDescription[provs.size()];
        int i = 0;
        for (ArtifactViewerPanelProvider prov : provs) {
            panels[i] = prov.createPanel(lookup);
            i = i + 1;
        }
        TopComponent tc = MultiViewFactory.createMultiView(panels, panels[0]);
        tc.setDisplayName(artifact.getArtifactId() + ":" + artifact.getVersion()); //NOI18N
        tc.setToolTipText(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion()); //NOI18N
        tc.putClientProperty(MAVEN_TC_PROPERTY, getTcId(artifact));
        return tc;
    }

    @Messages({
        "Progress_Download=Downloading Maven dependencies for {0}",
        "TIT_Error=Panel loading error.",
        "BTN_CLOSE=&Close"
    })
    @NonNull private Lookup createViewerLookup(final @NullAllowed NBVersionInfo info, final @NonNull Artifact artifact, final @NullAllowed List<ArtifactRepository> fRepos) {
        final InstanceContent ic = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(ic);
        ic.add(artifact);
        if (info != null) {
            ic.add(info);
        }
        final Artifact fArt = artifact;

            RP.post(new Runnable() {
                    @Override
                public void run() {
                    MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
                    AggregateProgressHandle hndl = AggregateProgressFactory.createHandle(Progress_Download(artifact.getId()),
                                new ProgressContributor[] {
                                    AggregateProgressFactory.createProgressContributor("zaloha") },  //NOI18N
                                ProgressTransferListener.cancellable(), null);
                    ProgressTransferListener.setAggregateHandle(hndl);
                    hndl.start();
                    try {
                            List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
                            if (fRepos != null) {
                                repos.addAll(fRepos);
                            }
                            if (repos.isEmpty()) {
                                //add central repo
                                repos.add(embedder.createRemoteRepository(RepositorySystem.DEFAULT_REMOTE_REPO_URL, RepositorySystem.DEFAULT_REMOTE_REPO_ID));
                                //add repository form info
                                if (info != null && !RepositorySystem.DEFAULT_REMOTE_REPO_ID.equals(info.getRepoId())) {
                                    RepositoryInfo rinfo = RepositoryPreferences.getInstance().getRepositoryInfoById(info.getRepoId());
                                    if (rinfo != null) {
                                        String url = rinfo.getRepositoryUrl();
                                        if (url != null) {
                                            repos.add(embedder.createRemoteRepository(url, rinfo.getId()));
                                        }
                                    }
                                }
                            }
                            MavenProject mvnprj = readMavenProject(embedder, fArt, repos);

                        if(mvnprj != null){
                            DependencyNode root = DependencyTreeFactory.createDependencyTree(mvnprj, embedder, List.of(Artifact.SCOPE_TEST));
                            ic.add(root);
                            ic.add(mvnprj);
                        }
                    } catch (ProjectBuildingException | MavenExecutionException ex) {
                        ErrorPanel pnl = new ErrorPanel(ex);
                        DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Error());
                        JButton close = new JButton();
                        org.openide.awt.Mnemonics.setLocalizedText(close, BTN_CLOSE());
                        dd.setOptions(new Object[] { close });
                        dd.setClosingOptions(new Object[] { close });
                        DialogDisplayer.getDefault().notify(dd);
                        ic.add(new MavenProject()); // XXX is this useful for anything?
                    } catch (IllegalStateException ise) { //download interrupted in dependent thread. #213812
                        throw ise;
                    } finally {
                        hndl.finish();
                        ProgressTransferListener.clearAggregateHandle();
                    }
                }
            });

        Action[] toolbarActions = new Action[] {
            new AddAsDependencyAction(fArt),
            CommonArtifactActions.createScmCheckoutAction(lookup),
            CommonArtifactActions.createLibraryAction(lookup)
        };
        ic.add(toolbarActions);

        return lookup;
    }

    private static MavenProject readMavenProject(MavenEmbedder embedder, Artifact artifact, List<ArtifactRepository> remoteRepos) throws  ProjectBuildingException {
        ProjectBuilder bldr = embedder.lookupComponent(ProjectBuilder.class);
        assert bldr !=null : "ProjectBuilder component not found in maven";
        DefaultMaven maven = (DefaultMaven) embedder.lookupComponent(Maven.class);
        assert bldr !=null : "DefaultMaven component not found in maven";
        
        MavenExecutionRequest req = embedder.createMavenExecutionRequest();
        req.setLocalRepository(embedder.getLocalRepository());
        req.setRemoteRepositories(remoteRepos);

        ProjectBuildingRequest configuration = req.getProjectBuildingRequest();
        configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        configuration.setResolveDependencies(true);
        configuration.setRepositorySession(maven.newRepositorySession(req));
        ProjectBuildingResult projectBuildingResult = bldr.build(artifact, configuration);
        return projectBuildingResult.getProject();    
    }
    
    private static final String MAVEN_TC_PROPERTY = "mvn_tc_id";

    private static TopComponent findExistingTc(Artifact artifact) {
        String id = getTcId(artifact);
        Set<TopComponent> tcs = WindowManager.getDefault().getRegistry().getOpened();
        for (TopComponent tc : tcs) {
            if (id.equals(tc.getClientProperty(MAVEN_TC_PROPERTY))) {
                return tc;
            }
        }
        return null;
    }

    private static String getTcId(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() +
                ":" + artifact.getVersion();
    }

    private Lookup createPomEditorLookup(final Project prj, @NonNull Artifact artifact) {
        final InstanceContent ic = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(ic);
        ic.add(artifact);
        if (prj != null) {
            ic.add(prj);
            //lookup for project non null means we are part of pom editor
            RP.post(new Runnable() {
                @Override
                public void run() {
                    NbMavenProject im = prj.getLookup().lookup(NbMavenProject.class);
                    MavenProject mvnprj = im.getMavenProject();
                    DependencyNode tree;
                    try {
                        tree = DependencyTreeFactory.createDependencyTree(mvnprj, EmbedderFactory.getProjectEmbedder(), List.of(Artifact.SCOPE_TEST));
                    } catch (MavenExecutionException ex) {
                        Logger.getLogger(ArtifactMultiViewFactory.class.getName()).log(Level.WARNING, "Dependency tree scan failed", ex);
                        return;
                    }
                    FileObject fo = prj.getLookup().lookup(FileObject.class);
                    POMModel pommodel = null;
                    if (fo != null) {
                        ModelSource ms = Utilities.createModelSource(fo);
                        if (ms.isEditable()) {
                            POMModel model = POMModelFactory.getDefault().getModel(ms);
                            if (model != null) {
                                pommodel = model;
                            }
                        }
                    }
                    //add all in one place to prevent large time delays between additions
                    if (pommodel != null) {
                        ic.add(pommodel);
                    }
                    ic.add(tree);
                    ic.add(mvnprj);
                }
            });
        }
        Action[] toolbarActions = new Action[] {
            new AddAsDependencyAction(artifact),
            CommonArtifactActions.createScmCheckoutAction(lookup),
            CommonArtifactActions.createLibraryAction(lookup)
        };
        ic.add(toolbarActions);
        
        return lookup;
    }

}
