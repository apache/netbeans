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

package org.netbeans.modules.maven.grammar;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Position;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import static org.netbeans.modules.maven.grammar.Bundle.*;
import org.netbeans.modules.maven.grammar.effpom.AnnotationBarManager;
import org.netbeans.modules.maven.grammar.effpom.LocationAwareMavenXpp3Writer;
import org.netbeans.modules.maven.grammar.effpom.LocationAwareMavenXpp3Writer.Location;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Annotation;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

public class EffectivePomMD implements MultiViewDescription, Serializable {
    private static final Logger LOG = Logger.getLogger(EffectivePomMD.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(EffectivePomMD.class);

    private final Lookup lookup;

    private EffectivePomMD(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Messages("TAB_EFF_Pom=Effective POM")
    @Override public String getDisplayName() {
        return TAB_EFF_Pom();
    }

    @Override public Image getIcon() {
        return ImageUtilities.loadImage(POMDataObject.POM_ICON, true);
    }

    @Override public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override public String preferredID() {
        return "effpom"; // XXX could be ArtifactViewer.HINT_* constant
    }

    @Override public MultiViewElement createElement() {
        return new EffPOMView(lookup);
    }

//    @ServiceProvider(service=ArtifactViewerPanelProvider.class, position=600)
//    public static class Factory implements ArtifactViewerPanelProvider {
//
//        @Override public MultiViewDescription createPanel(Lookup lookup) {
//            return new EffectivePomMD(lookup);
//        }
//    }
    
    @MultiViewElement.Registration(
        displayName="#TAB_Effective",
        iconBase=POMDataObject.POM_ICON,
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="effectivePom",
        mimeType=Constants.POM_MIME_TYPE,
        position=101
    )
    @Messages("TAB_Effective=Effective")
    public static MultiViewElement forPOMEditor(final Lookup editor) {
        return new EffPOMView(editor);
    }    

    private static class EffPOMView implements MultiViewElement, Runnable, PropertyChangeListener {
        private static final String EFF_MIME_TYPE = "text/x-maven-pom+xml";

        private final Lookup lookup;
        private final RequestProcessor.Task task = RP.create(this);
        private JToolBar toolbar;
        private JPanel panel;
        boolean firstTimeShown = true;
        private final Lookup mime;

        EffPOMView(Lookup lookup) {
            this.lookup = lookup;
            mime = MimeLookup.getLookup(EFF_MIME_TYPE);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (panel != null && panel.isVisible()) {
                            panel.add(new JLabel(LBL_loading_Eff(), SwingConstants.CENTER), BorderLayout.CENTER);

                            task.schedule(0);
                        } else {
                            firstTimeShown = true;
                        }
                    }
                });
            }
        }
        

        @Override public JComponent getVisualRepresentation() {
            if (panel == null) {
                panel = new JPanel(new BorderLayout());
                if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                    panel.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
                }                
            }
            return panel;
        }

        @Override public JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                toolbar = new JToolBar();
                toolbar.setFloatable(false);
                if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                    toolbar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
                }
                effDiffAction = new ShowEffPomDiffAction(lookup);
                effDiffAction.setEnabled(false);
                toolbar.add(effDiffAction);
                //TODO
            }
            return toolbar;
        }
        private ShowEffPomDiffAction effDiffAction;

        @Override public void setMultiViewCallback(MultiViewElementCallback callback) {}

        @Override public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }

        @Override public Action[] getActions() {
            return new Action[0];
        }

        @Override public Lookup getLookup() {
            return lookup;
        }

        @Override public void componentOpened() {}

        @Override public void componentClosed() {}

        private @NullAllowed JEditorPane pane;
        
        private int caretPosition;
        
        @Messages("LBL_loading_Eff=Loading Effective POM...")
        @Override public void componentShowing() {
            if (firstTimeShown) {
                firstTimeShown = false;
                getVisualRepresentation().add(new JLabel(LBL_loading_Eff(), SwingConstants.CENTER), BorderLayout.CENTER);
                task.schedule(0);
            } else {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        if (pane != null) {
                            pane.requestFocus();
                            pane.setCaretPosition(caretPosition);
                        }
                    }
                });
            }
        }

        @Override public void componentHidden() {}

        @Override public void componentActivated() {}

        @Override public void componentDeactivated() {
            if (pane != null) {
                caretPosition = pane.getCaretPosition();
            }
        }

        @Override public UndoRedo getUndoRedo() {
            return UndoRedo.NONE;
        }

        @Messages({
            "ERR_No_Project_Loaded=No project associated with the project or loading failed. See Source tab for errors", 
            "ERR_Unloadable=POM's project currently unloadable.",
            "ERR_In_Jar=Cannot show effective POM for jar file content."
        })
        @Override public void run() {
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    effDiffAction.setEnabled(false);
                }
            });
            try {
                FileObject pom = lookup.lookup(FileObject.class);
                Model model = null;
                String errorMessage = null;
                if (pom != null) {
                    Project p = null;
                    
                    try {
                        p = ProjectManager.getDefault().findProject(pom.getParent());
                    } catch (IOException ex) {
                        LOG.log(Level.FINE, ex.getMessage(), ex);
                    } catch (IllegalArgumentException ex) {
                        LOG.log(Level.FINE, ex.getMessage(), ex);
                    }
                    if (p != null) {
                        NbMavenProject nbmp = p.getLookup().lookup(NbMavenProject.class);
                        if (nbmp != null) {
                            model = nbmp.getMavenProject().getModel();
                            if (model == null || nbmp.isUnloadable()) {
                                errorMessage = ERR_Unloadable();
                            }
                            nbmp.addPropertyChangeListener(WeakListeners.propertyChange(this, nbmp));
                        } else {
                            LOG.log(Level.WARNING, "not a Maven project: {0}", p);
                            ModelBuildingResult res = EmbedderFactory.getProjectEmbedder().executeModelBuilder(FileUtil.toFile(pom));
                            model = res.getEffectiveModel();
                        }
                    } else {
                        LOG.log(Level.WARNING, "not a project: {0}", p);
                        File fl = FileUtil.toFile(pom);
                        if (fl != null) {
                            ModelBuildingResult res = EmbedderFactory.getProjectEmbedder().executeModelBuilder(fl);
                            model = res.getEffectiveModel();
                        } else {
                            errorMessage = ERR_In_Jar();
                        }
                    }
                } else {
                    LOG.log(Level.WARNING, "no file in lookup");
                    errorMessage = "No file in editor lookup";
                }                
                
                
                if (errorMessage != null) {
                    final String message = errorMessage;
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                            JComponent pnl = getVisualRepresentation();
                            pnl.removeAll();
                            pnl.add(new JLabel(message, SwingConstants.CENTER), BorderLayout.CENTER);
                            pnl.revalidate();
                        }
                    });
                    return;
                }
                assert model != null;
                assert pom != null;
                LocationAwareMavenXpp3Writer writer = new LocationAwareMavenXpp3Writer();
                final StringWriter sw = new StringWriter();
                final List<Location> loc = writer.write(sw, model);
                List<ModelProblem> problems = new ArrayList<ModelProblem>();
                final Map<ModelProblem, Location> prblmMap = new HashMap<ModelProblem, Location>();
                if (pom != null) {
                    File file = FileUtil.toFile(pom);
                    problems.addAll(runMavenValidationImpl(file));
                    for (Location lo : loc) {
                        Iterator<ModelProblem> it2 = problems.iterator();
                        while (it2.hasNext()) {
                            ModelProblem modelProblem = it2.next();
                            if (modelProblem.getLineNumber() == lo.loc.getLineNumber() && modelProblem.getModelId().equals(lo.loc.getSource().getModelId())) {
                                prblmMap.put(modelProblem, lo);
                                it2.remove();
                            }
                        }
                    }
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        EditorKit kit = mime.lookup(EditorKit.class);
                        NbEditorDocument doc = (NbEditorDocument) kit.createDefaultDocument();
                        pane = new JEditorPane(EFF_MIME_TYPE, null);
                        pane.setDocument(doc);
                        JComponent pnl = getVisualRepresentation(); 
                        pnl.removeAll();
                        pnl.add(doc.createEditor(pane), BorderLayout.CENTER);
                        pane.setEditable(false);

                        try {
                            doc.insertString(0, sw.toString(), null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        pane.requestFocus();
                        pane.setCaretPosition(0);

                        for (final Map.Entry<ModelProblem, Location> ent : prblmMap.entrySet()) {
                            doc.addAnnotation(new Position() {

                                @Override
                                public int getOffset() {
                                    return ent.getValue().startOffset;
                                }
                            }, 1, new Annotation() {

                                @Override
                                public String getAnnotationType() {
                                    if (ent.getKey().getSeverity() == ModelProblem.Severity.ERROR || ent.getKey().getSeverity() == ModelProblem.Severity.FATAL) {
                                        return "org-netbeans-spi-editor-hints-parser_annotation_err";
                                    } else {
                                        return "org-netbeans-spi-editor-hints-parser_annotation_warn";
                                    }
                                }

                                @Override
                                public String getShortDescription() {
                                    return ent.getKey().getMessage();
                                }
                            });
                        }
                        pnl.revalidate();
                        AnnotationBarManager.showAnnotationBar(pane, loc);
                    }
                });
            } catch (final Exception x) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        JComponent pnl = getVisualRepresentation();
                        pnl.removeAll();
                        pnl.add(new JLabel(LBL_failed_to_load(x.getLocalizedMessage()), SwingConstants.CENTER), BorderLayout.CENTER);
                        pnl.revalidate();
                        LOG.log(Level.FINE, "Exception thrown while loading effective POM", x);
                        Exceptions.printStackTrace(x);
                        firstTimeShown = true;
                    }
                });
            } finally {
                EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    effDiffAction.calculateEnabledState();
                }
            });
            }
        }

    }
    
    //copied from maven.hints Status Provider..
    //eventually should have the model problems coming from either the Mavenproject or the modelBuilder result.
    static List<ModelProblem> runMavenValidationImpl(final File pom) {
        //TODO profiles based on current configuration??
        MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
        MavenExecutionRequest meReq = embedder.createMavenExecutionRequest();
        ProjectBuildingRequest req = meReq.getProjectBuildingRequest();
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_1); // currently enables just <reporting> warning
        req.setLocalRepository(embedder.getLocalRepository());
        List<ArtifactRepository> remoteRepos = RepositoryPreferences.getInstance().remoteRepositories(embedder);
        req.setRemoteRepositories(remoteRepos);
        req.setRepositorySession(((DefaultMaven) embedder.lookupComponent(Maven.class)).newRepositorySession(meReq));
        List<ModelProblem> problems;
        try {
            problems = embedder.lookupComponent(ProjectBuilder.class).build(pom, req).getProblems();
        } catch (ProjectBuildingException x) {
            problems = new ArrayList<ModelProblem>();
            List<ProjectBuildingResult> results = x.getResults();
            if (results != null) { //one code point throwing ProjectBuildingException contains results,
                for (ProjectBuildingResult result : results) {
                    problems.addAll(result.getProblems());
                }
            } else {
                // another code point throwing ProjectBuildingException doesn't contain results..
                Throwable cause = x.getCause();
                if (cause instanceof ModelBuildingException) {
                    problems.addAll(((ModelBuildingException) cause).getProblems());
                }
            }
        }
        return problems;
    }    
}
