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

package org.netbeans.modules.maven.hints.pom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.hints.pom.spi.SelectionPOMFixProvider;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Parent;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */
@MimeRegistration(mimeType=Constants.POM_MIME_TYPE, service=UpToDateStatusProviderFactory.class)
public final class StatusProvider implements UpToDateStatusProviderFactory {

    private static final String LAYER_POM = "pom"; //NOI18N
    private static final String LAYER_POM_SELECTION = "pom-selection"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor("StatusProvider"); //NOI18N
    private static final Logger LOG = Logger.getLogger(StatusProvider.class.getName());

    @Override
    public UpToDateStatusProvider createUpToDateStatusProvider(Document document) {
        return new StatusProviderImpl(document);
    }

    static class StatusProviderImpl extends UpToDateStatusProvider {
        private final Document document;
        private @NullAllowed POMModel model;
        private Project project;
        private final FileChangeListener listener;
        private final PreferenceChangeListener prefListener;
        
        StatusProviderImpl(Document doc) {
            this.document = doc;
            listener = new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent fe) {
                    // XXX fire PROP_UP_TO_DATE
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            checkHints();
                        }
                    });
                }
            };
            prefListener = new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if(EmbedderFactory.PROP_COMMANDLINE_PATH.equals(evt.getKey())) {
                        // given by the registered mvn client, the pom validation result may change ...
                        checkHints();
                    }
                }
            };
            Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven");
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, prefs));
            
            RP.post(new Runnable() {
                @Override
                public void run() {
                    initializeModel(); //#204067 moved to RP 
                    checkHints();
                }
            });
        }


        private void checkHints() {
            if (model == null) {
                return;
            }
            HintsController.setErrors(document, LAYER_POM, findHints(model, project, -1, -1, -1));
        }

        static List<ErrorDescription> findHints(final @NonNull POMModel model, final Project project, final int selectionStart, final int selectionEnd, final int caretPosition) {
            final List<ErrorDescription> err = new ArrayList<ErrorDescription>();
            //before checkModelValid because of #216093
            runMavenValidation(model, err);
            if (!checkModelValid(model)) {
                return err;
            }

            return ProjectManager.mutex().readAccess(new Mutex.Action<List<ErrorDescription>>() {
                public @Override List<ErrorDescription> run() {
                        Lookup lkp = Lookups.forPath("org-netbeans-modules-maven-hints"); //NOI18N
                        if (selectionStart == -1 && selectionEnd == -1) {
                            Lookup.Result<POMErrorFixProvider> res = lkp.lookupResult(POMErrorFixProvider.class);
                            for (POMErrorFixProvider prov : res.allInstances()) {
                                if (!prov.getConfiguration().isEnabled(prov.getConfiguration().getPreferences())) {
                                    continue;
                                }
                                List<ErrorDescription> lst = prov.getErrorsForDocument(model, project);
                                if (lst != null) {
                                    err.addAll(lst);
                                }
                            }
                        } else {
                            Lookup.Result<SelectionPOMFixProvider> res = lkp.lookupResult(SelectionPOMFixProvider.class);
                            for (SelectionPOMFixProvider prov : res.allInstances()) {
                                if (!prov.getConfiguration().isEnabled(prov.getConfiguration().getPreferences())) {
                                    continue;
                                }
                                List<ErrorDescription> lst = prov.getErrorsForDocument(model, project, selectionStart, selectionEnd, caretPosition);
                                if (lst != null) {
                                    err.addAll(lst);
                                }
                            }
                        }
                        return err;
                }
            });
        }
        
        private static boolean checkModelValid(POMModel model) {
            assert model != null;
            if (!model.getModelSource().isEditable()) {
                return false;
            }
            try {
                model.getBaseDocument(); // #187615
                model.sync();
                // model.refresh();
            } catch (IOException ex) {
                LOG.log(Level.FINE, "Error while syncing pom model.", ex);
            }

            if (!model.getState().equals(Model.State.VALID)) {
                LOG.log(Level.FINE, "Pom model document is not valid, is {0}", model.getState());
                return false;
            }
            if (model.getProject() == null) {
                LOG.log(Level.FINE, "Pom model root element missing");
                return false;
            }
            return true;
        }

        private void initializeModel() {
            FileObject fo = NbEditorUtilities.getFileObject(document);
            if (fo != null) {
                //#236116 passing document protects from looking it up later and causing a deadlock.
                ModelSource ms = Utilities.createModelSource(fo, null, document instanceof BaseDocument ? (BaseDocument)document : null);
                model = POMModelFactory.getDefault().createFreshModel(ms);
                project = FileOwnerQuery.getOwner(fo);
                fo.addFileChangeListener(FileUtil.weakFileChangeListener(listener, fo));
            }
        }

        @Override
        public UpToDateStatus getUpToDate() {
            if (model == null) {
                return UpToDateStatus.UP_TO_DATE_OK;
            }
            FileObject fo = NbEditorUtilities.getFileObject(document);
            boolean ok = false;
            try {
                if (fo.isValid()) {
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie ed = dobj.getLookup().lookup(EditorCookie.class);
                    if (ed != null) {
                        JEditorPane[] panes = ed.getOpenedPanes();
                        if (panes != null && panes.length > 0) {
                            //#214527
                            JEditorPane pane = panes[0];
                            if (panes.length > 1) {
                                for (JEditorPane p : panes) {
                                    if (p.isFocusOwner()) {
                                        pane = p;
                                        break;
                                    }
                                }
                            }
                            //TODO this code is called very often apparently.
                            //we should only run the checks if something changed..
                            //something means file + selection start + selection end.
                            final int selectionStart = pane.getSelectionStart();
                            final int selectionEnd = pane.getSelectionEnd();
                            final int caretPosition = pane.getCaretPosition();
                            RP.post(new Runnable() {
                                @Override
                                public void run() {
                                    refreshLinkAnnotations(document, model, selectionStart, selectionEnd);
                                }
                            });
                            if (selectionStart != selectionEnd) { //maybe we want to remove the condition?
                                RP.post(new Runnable() {
                                    @Override public void run() {
                                        //this condition is important in order not to break any running hints
                                        //the model sync+refresh renders any existing POMComponents people
                                        // might be holding useless
                                        if (!model.isIntransaction()) {
                                            HintsController.setErrors(document, LAYER_POM_SELECTION, findHints(model, project, selectionStart, selectionEnd, caretPosition));
                                        } else {
                                            HintsController.setErrors(document, LAYER_POM_SELECTION, Collections.<ErrorDescription>emptyList());
                                        }
                                        
                                    }
                                });
                                ok = true;
                                return UpToDateStatus.UP_TO_DATE_PROCESSING;
                            }
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                //#166011 just a minor issue, just log, but don't show to user directly
                LOG.log(Level.INFO, "Touched somehow invalidated FileObject", ex);
            } finally {
                if (!ok) {
                    HintsController.setErrors(document, LAYER_POM_SELECTION, Collections.<ErrorDescription>emptyList());
                }
            }
            return UpToDateStatus.UP_TO_DATE_OK; // XXX should use UP_TO_DATE_PROCESSING if checkHints task is currently running
        }

        private void refreshLinkAnnotations(Document document, POMModel model, final int selectionStart, int selectionEnd) {
            if (document instanceof StyledDocument) {
                StyledDocument styled = (StyledDocument) document;
                Annotation[] old = (Annotation[]) styled.getProperty("maven_annot");
                if (old != null) {
                    for (Annotation ann : old) {
                        NbDocument.removeAnnotation(styled, ann);
                    }
                    styled.putProperty("maven_annot", null); //217741
                }
                if (checkModelValid(model)) {
                    try {
                        List<Annotation> anns = new ArrayList<Annotation>();
                        //now add a link to parent pom.
                        Parent p = model.findComponent(selectionStart, Parent.class, true);
                        if (p != null && p.getArtifactId() != null && p.getGroupId() != null && p.getVersion() != null) { //217741
                            Annotation ann = new ParentPomAnnotation();
                            anns.add(ann);
                            Position position = NbDocument.createPosition(document, selectionStart, Position.Bias.Forward);
                            NbDocument.addAnnotation(styled, position, selectionEnd - selectionStart, ann);
                        }
                        styled.putProperty("maven_annot", anns.toArray(new Annotation[0]));
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }                    
                }
            }
 
        }

    }

    private static void runMavenValidation(final POMModel model, final List<ErrorDescription> err) {
        File pom = model.getModelSource().getLookup().lookup(File.class);
        if (pom == null) {
            return;
        }
        
        List<ModelProblem> problems = runMavenValidationImpl(pom);
        for (ModelProblem problem : problems) {
            if (!problem.getSource().equals(pom.getAbsolutePath())) {
                LOG.log(Level.FINE, "found problem not in {0}: {1}", new Object[] {pom, problem.getSource()});
                continue;
            }
            int line = problem.getLineNumber();
            if (line <= 0) { // probably from a parent POM
                /* probably more irritating than helpful:
                line = 1; // fallback
                Parent parent = model.getProject().getPomParent();
                if (parent != null) {
                    Line l = NbEditorUtilities.getLine(model.getBaseDocument(), parent.findPosition(), false);
                    if (l != null) {
                        line = l.getLineNumber() + 1;
                    }
                }
                */
                continue;
            }
            if (problem.getException() instanceof UnresolvableModelException) {
                // If a <parent> reference cannot be followed because e.g. no projects are opened (so no repos registered), just ignore it.
                continue;
            }
            try {
                err.add(ErrorDescriptionFactory.createErrorDescription(problem.getSeverity() == ModelProblem.Severity.WARNING ? Severity.WARNING : Severity.ERROR, problem.getMessage(), model.getBaseDocument(), line));
            } catch (IndexOutOfBoundsException x) {
                LOG.log(Level.WARNING, "improper line number: {0}", problem);
            }
        }
        
    }
    
    //non-private because of tests..   
    static List<ModelProblem> runMavenValidationImpl(final File pom) {
        MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
        MavenExecutionRequest meReq = embedder.createMavenExecutionRequest();
        ProjectBuildingRequest req = meReq.getProjectBuildingRequest();
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0); // 3.1 currently enables just <reporting> warning, see issue 223562 for details on why it's bad to show.
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
        List<ModelProblem> toRet = new LinkedList<ModelProblem>();
        for (ModelProblem problem : problems) {
            if(ModelUtils.checkByCLIMavenValidationLevel(problem)) {
                toRet.add(problem);
            }
        }
        return toRet;
    }
    
    public static class ParentPomAnnotation extends Annotation {

        @Override
        public String getAnnotationType() {
            return "org-netbeans-modules-editor-annotations-implements";
        }

        @Override
        public String getShortDescription() {
            return "Go to parent POM declaration";
        }
    }
}
