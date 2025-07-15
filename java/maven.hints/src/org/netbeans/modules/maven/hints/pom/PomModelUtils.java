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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.building.Source;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelSource2;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixBase;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public final class PomModelUtils {
    private static final Logger LOG = Logger.getLogger(PomModelUtils.class.getName());
    
    static final String LAYER_POM = "pom"; //NOI18N
    static final String LAYER_POM_SELECTION = "pom-selection"; //NOI18N

    public static boolean implementInTransaction(Model m, Runnable r) {
        m.startTransaction();
        try {
            r.run();
        } finally {
            try {
                m.endTransaction();
            } catch (IllegalStateException ex) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(PomModelUtils.class, "ERR_UpdatePomModel",
                        Exceptions.findLocalizedMessage(ex)));
                return false;
            }
        }
        return true;
    }

    static boolean checkModelValid(POMModel model) {
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


    static void runMavenValidation(final POMModel model, final List<ErrorDescription> err) {
        File pom = model.getModelSource().getLookup().lookup(File.class);
        if (pom == null) {
            return;
        }
        
        List<ModelProblem> problems = runMavenValidationImpl(pom, model.getModelSource().getLookup().lookup(Document.class));
        for (ModelProblem problem : problems) {
            if (!problem.getSource().equals(pom.getAbsolutePath())) {
                LOG.log(Level.FINE, "found problem not in {0}: {1}", new Object[] {pom, problem.getSource()});
                continue;
            }
            int line = problem.getLineNumber();
            if (line <= 0) { // probably from a parent POM
                continue;
            }
            if (problem.getException() instanceof UnresolvableModelException) {
                // If a <parent> reference cannot be followed because e.g. no projects are opened (so no repos registered), just ignore it.
                continue;
            }
            try {
                Severity severity=Severity.WARNING;
                String problemMessage=problem.getMessage();
                switch (problem.getSeverity()){
                    case FATAL:
                    case ERROR:
                        severity= Severity.ERROR;
                        problemMessage =problem.getSeverity().toString()+" the bundled maven (version "
                                + getActiveMavenVersion().toString()
                                + ") detected a problem: "+problem.getMessage();
                        break;
                    case WARNING:// nothing to do
                        break;
                }
                err.add(ErrorDescriptionFactory.createErrorDescription( severity,problemMessage, model.getBaseDocument(),line));
            } catch (IndexOutOfBoundsException x) {
                LOG.log(Level.WARNING, "improper line number: {0}", problem);
            }
        }
        
    }
    
    public static List<ErrorDescription> findHints(final @NonNull POMModel model, final Project project) {
        final List<ErrorDescription> err = new ArrayList<>();
        //before checkModelValid because of #216093
        runMavenValidation(model, err);
        if (!checkModelValid(model)) {
            return err;
        }

        for (POMErrorFixProvider prov : PomModelUtils.hintProviders(project, POMErrorFixProvider.class)) {
            List<ErrorDescription> lst = prov.getErrorsForDocument(model, project);
            if (lst != null) {
                err.addAll(lst);
            }
        }
        return err;
    }

    /**
     * Source for maven model that can serve both editor contents and file contents.
     */
    private static class M2S implements ModelSource2, Source {
        private final File pomFile;
        private final Document doc;

        public M2S(File pomFile, Document doc) {
            this.pomFile = pomFile;
            this.doc = doc;
        }

        @Override
        public ModelSource2 getRelatedSource(String relative) {
            return new M2S(new File(pomFile.getParentFile(), relative), null);
        }

        @Override
        public URI getLocationURI() {
            return pomFile.toURI();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            AtomicReference<String> content = new AtomicReference<>();
            AtomicReference<BadLocationException> err = new AtomicReference<>();
            if (doc != null) {
                doc.render(() -> {
                    try {
                        content.set(doc.getText(0, doc.getLength()));
                    } catch (BadLocationException ex) {
                        err.set(ex);
                    }
                });
                if (err.get() != null) {
                    throw new IOException(err.get());
                } else {
                    return new ByteArrayInputStream(content.get().getBytes(StandardCharsets.UTF_8));
                }
            }
            return new FileInputStream(pomFile);
        }

        @Override
        public String getLocation() {
            return pomFile.getPath();
        }
    }
    
    static List<ModelProblem> runMavenValidationImpl(final File pom, Document doc) {
        MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
        MavenExecutionRequest meReq = embedder.createMavenExecutionRequest();
        ProjectBuildingRequest req = meReq.getProjectBuildingRequest();
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0); // 3.1 currently enables just <reporting> warning, see issue 223562 for details on why it's bad to show.
        req.setLocalRepository(embedder.getLocalRepository());
        List<ArtifactRepository> remoteRepos = RepositoryPreferences.getInstance().remoteRepositories(embedder);
        req.setRemoteRepositories(remoteRepos);
        req.setRepositorySession(((DefaultMaven) embedder.lookupComponent(Maven.class)).newRepositorySession(meReq));
        List<ModelProblem> problems;
        long t = System.currentTimeMillis();
        try {
            problems = embedder.lookupComponent(ProjectBuilder.class).build(new M2S(pom, doc), req).getProblems();
        } catch (ProjectBuildingException x) {
            problems = new ArrayList<>();
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
        List<ModelProblem> toRet = new LinkedList<>();
        for (ModelProblem problem : problems) {
            if(ModelUtils.checkByCLIMavenValidationLevel(problem)) {
                toRet.add(problem);
            }
        }
        long d = System.currentTimeMillis() - t;
        LOG.fine("Maven validation of " + pom.getPath() + " run for: " + d);
        return toRet;
    }
    
    static <T extends POMErrorFixBase> List<T> hintProviders(Project project, Class<T> providerType) {
        List<T> providers =  ProjectManager.mutex().readAccess(new Mutex.Action<List<T>>() {
            public @Override List<T> run() {
                Lookup lkp = Lookups.forPath("org-netbeans-modules-maven-hints"); //NOI18N
                Lookup.Result<T> res = lkp.lookupResult(providerType);
                List<T> r = new ArrayList<>(res.allInstances());
                for (Iterator<T> it = r.iterator(); it.hasNext(); ) {
                    T prov = it.next();
                    if (!prov.getConfiguration().isEnabled(prov.getConfiguration().getPreferences())) {
                        it.remove();
                    }
                }
                return r;
            }
        });
        return providers;
    }

    /**
     * Returns true if the given text could be a maven property.
     */
    static boolean isPropertyExpression(String expression) {
        return expression != null && expression.startsWith("${") && expression.endsWith("}");
    }

    /**
     * Returns the property name of a property expression.
     */
    static String getPropertyName(String expression) {
        if (isPropertyExpression(expression)) {
            return expression.substring(2, expression.length() - 1);
        }
        return expression;
    }

    /**
     * Returns the value of the maven property or null.
     * @param expression the property text, for example: <code>${java.version}</code>.
     */
    static String getProperty(POMModel model, String expression) {
        Properties properties = model.getProject().getProperties();
        if (properties != null) {
            return properties.getProperty(getPropertyName(expression));
        }
        return null;
    }

    /**
     * Returns the first child component with the given name or null.
     */
    static POMComponent getFirstChild(POMComponent parent, String name) {
        for (POMComponent child : parent.getChildren()) {
            if (name.equals(child.getPeer().getNodeName())) {
                return child;
            }
        }
        return null;
    }
        
    /*tests*/ static ComparableVersion activeMavenVersion = null;
    private static File lastHome = null;
    
    static ComparableVersion getActiveMavenVersion() {
        File home = EmbedderFactory.getMavenHome();
        if (home != null && !home.equals(lastHome)) {
            lastHome = home;
            String version = MavenSettings.getCommandLineMavenVersion(home);
            if (version != null) {
                activeMavenVersion = new ComparableVersion(version);
            }
        }
        return activeMavenVersion;
    }

}
