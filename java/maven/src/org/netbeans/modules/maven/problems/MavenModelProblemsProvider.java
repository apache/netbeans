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

package org.netbeans.modules.maven.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbArtifactFixer;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.actions.OpenPOMAction;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.maven.InternalActionDelegate;

/**
 * Suggests to run priming build. Also serves as a provider for Priming Build action,
 * as it can share cache with the ProblemProvider.
 * 
 * @author mkleint
 */
@ProjectServiceProvider(service = { 
        ProjectProblemsProvider.class, 
        InternalActionDelegate.class,
    }, projectType = "org-netbeans-modules-maven"
)
public class MavenModelProblemsProvider implements ProjectProblemsProvider, InternalActionDelegate {
    static final ScheduledExecutorService RP  = new RequestProcessor(MavenModelProblemsProvider.class);
    private static final Logger LOG = Logger.getLogger(MavenModelProblemsProvider.class.getName());
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final Project project;
    private final AtomicBoolean projectListenerSet = new AtomicBoolean(false);
    private final AtomicReference<Collection<ProjectProblem>> problemsCache = new AtomicReference<Collection<ProjectProblem>>();
    private final PrimingActionProvider primingProvider = new PrimingActionProvider();
    private ProblemReporterImpl problemReporter;
    private final PropertyChangeListener projectListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                firePropertyChange();
            }
        }
    };

    public MavenModelProblemsProvider(Project project) {
        this.project = project;
    }
    
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        Collection<? extends ProjectProblem> prbs = doGetProblems(false);
        return prbs != null ? prbs : Collections.emptyList();
    }
    
    
    /**
     * Compute problems. If 'sync' is true, the computation is done synchronously. Caches results,
     * returns cache content immediately, if available.
     * @param sync true = run synchronously. False = fork computation/
     * @return project problems.
     */
    Collection<? extends ProjectProblem> doGetProblems(boolean sync) {
        final MavenProject prj = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        synchronized (this) {
            LOG.log(Level.FINER, "Called getProblems for {0}", project);
            //lazy adding listener only when someone asks for the problems the first time
            if (projectListenerSet.compareAndSet(false, true)) {
                //TODO do we check only when the project is opened?
                problemReporter = project.getLookup().lookup(NbMavenProjectImpl.class).getProblemReporter();
                assert problemReporter != null;
                project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(projectListener);
            
            }
            
            //for non changed project models, no need to recalculate, always return the cached value
            Object wasprocessed = prj.getContextValue(MavenModelProblemsProvider.class.getName());
            if (wasprocessed != null) {
                Collection<ProjectProblem> cached = problemsCache.get();
                LOG.log(Level.FINER, "Project was processed, cached is: {0}", cached);
                if (cached != null) {
                    return cached;
                }
            } 
            Callable<Collection<? extends ProjectProblem>> c = new Callable<Collection<? extends ProjectProblem>>() {
                @Override
                public Collection<? extends ProjectProblem> call() throws Exception {
                    Object wasprocessed = prj.getContextValue(MavenModelProblemsProvider.class.getName());
                    if (wasprocessed != null) {
                        Collection<ProjectProblem> cached = problemsCache.get();
                        LOG.log(Level.FINER, "Project was processed #2, cached is: {0}", cached);
                        if (cached != null) {                            
                            return cached;
                        }
                    } 
                    List<ProjectProblem> toRet = new ArrayList<>();
                    MavenExecutionResult res = MavenProjectCache.getExecutionResult(prj);
                    if (res != null && res.hasExceptions()) {
                        toRet.addAll(reportExceptions(res));
                    }
                    //#217286 doArtifactChecks can call FileOwnerQuery and attempt to aquire the project mutex.
                    toRet.addAll(doArtifactChecks(prj));
                    //mark the project model as checked once and cached
                    prj.setContextValue(MavenModelProblemsProvider.class.getName(), new Object());
                    synchronized(MavenModelProblemsProvider.this) {
                        LOG.log(Level.FINER, "Project processing finished, result is: {0}", toRet);
                        problemsCache.set(toRet);
                    }
                    firePropertyChange();
                    return toRet;
                }                
            };
            if(sync || Boolean.getBoolean("test.reload.sync")) {
                try {
                    return c.call();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                RP.submit(c);
            }
        }
        
        // indicate that we do not know
        return null;
    }

    private void firePropertyChange() {
        support.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
    }
    
    @NbBundle.Messages({
        "ERR_SystemScope=A 'system' scope dependency was not found. Code completion is affected.",
        "MSG_SystemScope=There is a 'system' scoped dependency in the project but the path to the binary is not valid.\n"
            + "Please check that the path is absolute and points to an existing binary.",
        "ERR_NonLocal=Some dependency artifacts are not in the local repository.",
        "# {0} - list of artifacts", "MSG_NonLocal=Your project has dependencies that are not resolved locally. "
            + "Code completion in the IDE will not include classes from these dependencies or their transitive dependencies (unless they are among the open projects).\n"
            + "Please download the dependencies, or install them manually, if not available remotely.\n\n"
            + "The artifacts are:\n {0}",
        "ERR_Participant=Custom build participant(s) found",
        "MSG_Participant=The IDE will not execute any 3rd party extension code during Maven project loading.\nThese can have significant influence on performance of the Maven model (re)loading or interfere with IDE''s own codebase. "
            + "On the other hand the model loaded can be incomplete without their participation. In this project "
            + "we have discovered the following external build participants:\n{0}"
    })
    public Collection<ProjectProblem> doArtifactChecks(@NonNull MavenProject project) {
        List<ProjectProblem> toRet = new ArrayList<ProjectProblem>();
        
        if (MavenProjectCache.unknownBuildParticipantObserved(project)) {
            StringBuilder sb = new StringBuilder();
            for (String s : MavenProjectCache.getUnknownBuildParticipantsClassNames(project)) {
                sb.append(s).append("\n");
            }
            toRet.add(ProjectProblem.createWarning(ERR_Participant(), MSG_Participant(sb.toString())));
        }
        toRet.addAll(checkParents(project));
        
        boolean missingNonSibling = false;
        List<Artifact> missingJars = new ArrayList<Artifact>();
        for (Artifact art : project.getArtifacts()) {
            File file = art.getFile();
            if (file == null || !file.exists()) {                
                if(Artifact.SCOPE_SYSTEM.equals(art.getScope())){
                    //TODO create a correction action for this.
                    toRet.add(ProjectProblem.createWarning(ERR_SystemScope(), MSG_SystemScope(), new ProblemReporterImpl.MavenProblemResolver(OpenPOMAction.instance().createContextAwareInstance(Lookups.fixed(project)), "SCOPE_DEPENDENCY")));
                } else {
                    problemReporter.addMissingArtifact(art);
                    if (file == null) {
                        missingNonSibling = true;
                    } else {
                        final URL archiveUrl = FileUtil.urlForArchiveOrDir(file);
                        if (archiveUrl != null) { //#236050 null check
                            //a.getFile should be already normalized
                            SourceForBinaryQuery.Result2 result = SourceForBinaryQuery.findSourceRoots2(archiveUrl);
                            if (!result.preferSources() || /* SourceForBinaryQuery.EMPTY_RESULT2.preferSources() so: */ result.getRoots().length == 0) {
                                missingNonSibling = true;
                            } // else #189442: typically a snapshot dep on another project
                        }
                    }
                    missingJars.add(art);
                }
            } else if (NbArtifactFixer.isFallbackFile(file)) {
                problemReporter.addMissingArtifact(art);
                missingJars.add(art);
                missingNonSibling = true;
            }
        }
        if (!missingJars.isEmpty()) {
            StringBuilder mess = new StringBuilder();
            for (Artifact art : missingJars) {
                mess.append(art.getId()).append('\n');
            }
            if (missingNonSibling) {
                toRet.add(ProjectProblem.createWarning(ERR_NonLocal(), MSG_NonLocal(mess), createSanityBuildAction()));
            } else {
                //we used to have a LOW severity ProblemReport here.
            }
        }
        return toRet;
    }

    @NbBundle.Messages({
        "ERR_NoParent=Parent POM file is not accessible. Project might be improperly setup.",
        "# {0} - Maven coordinates", "MSG_NoParent=The parent POM with id {0} was not found in sources or local repository. "
            + "Please check that <relativePath> tag is present and correct, the version of parent POM in sources matches the version defined. \n"
            + "If parent is only available through a remote repository, please check that the repository hosting it is defined in the current POM."
    })
    private Collection<ProjectProblem> checkParents(@NonNull MavenProject project) {
        List<MavenEmbedder.ModelDescription> mdls = MavenEmbedder.getModelDescriptors(project);
        boolean first = true;
        if (mdls == null) { //null means just about broken project..
            return Collections.emptyList();
        }
        List<ProjectProblem> toRet = new ArrayList<ProjectProblem>();
        for (MavenEmbedder.ModelDescription m : mdls) {
            if (first) {
                first = false;
                continue;
            }
            if (NbArtifactFixer.FALLBACK_NAME.equals(m.getName())) {
                toRet.add(ProjectProblem.createError(ERR_NoParent(), MSG_NoParent(m.getId()), createSanityBuildAction()));
                problemReporter.addMissingArtifact(EmbedderFactory.getProjectEmbedder().createArtifact(m.getGroupId(), m.getArtifactId(), m.getVersion(), "pom"));
            }
        }
        return toRet;
    }

    /**
     * Will keep a reference to the sanity build, if it was created, as long as
     * Problems exist.
     */
    private Reference<SanityBuildAction> cachedSanityBuild = new WeakReference<>(null);

    public SanityBuildAction createSanityBuildAction() {
        synchronized (this) {
            SanityBuildAction a = cachedSanityBuild.get();
            if (a != null) {
                Future<ProjectProblemsProvider.Result> r = a.getPendingResult();
                if (r != null) {
                    return a;
                }
            }
            a = new SanityBuildAction(project);
            cachedSanityBuild = new WeakReference<>(a);
            return a;
        }
    }
    
    @NbBundle.Messages({
        "TXT_Artifact_Resolution_problem=Artifact Resolution problem",
        "TXT_Artifact_Not_Found=Artifact Not Found",
        "TXT_Cannot_Load_Project=Unable to properly load project",
        "TXT_Cannot_read_model=Error reading project model",
        "TXT_NoMsg=Exception thrown while loading maven project at {0}. See messages.log for more information."
    })
    private Collection<ProjectProblem> reportExceptions(MavenExecutionResult res) {
        List<ProjectProblem> toRet = new ArrayList<ProjectProblem>();
        for (Throwable e : res.getExceptions()) {
            LOG.log(Level.FINE, "Error on loading project " + project.getProjectDirectory(), e);
            if (e instanceof ArtifactResolutionException) { // XXX when does this occur?
                toRet.add(ProjectProblem.createError(TXT_Artifact_Resolution_problem(), getDescriptionText(e)));
                problemReporter.addMissingArtifact(((ArtifactResolutionException) e).getArtifact());
                
            } else if (e instanceof ArtifactNotFoundException) { // XXX when does this occur?
                toRet.add(ProjectProblem.createError(TXT_Artifact_Not_Found(), getDescriptionText(e)));
                problemReporter.addMissingArtifact(((ArtifactNotFoundException) e).getArtifact());
            } else if (e instanceof ProjectBuildingException) {
                LOG.log(Level.FINE, "Creating sanity build action for {0}", project.getProjectDirectory());
                toRet.add(ProjectProblem.createError(TXT_Cannot_Load_Project(), getDescriptionText(e), createSanityBuildAction()));
                if (e.getCause() instanceof ModelBuildingException) {
                    ModelBuildingException mbe = (ModelBuildingException) e.getCause();
                    for (ModelProblem mp : mbe.getProblems()) {
                        LOG.log(Level.FINE, mp.toString(), mp.getException());
                        if (mp.getException() instanceof UnresolvableModelException) {
                            // Probably obsoleted by ProblemReporterImpl.checkParent, but just in case:
                            UnresolvableModelException ume = (UnresolvableModelException) mp.getException();
                            problemReporter.addMissingArtifact(EmbedderFactory.getProjectEmbedder().createProjectArtifact(ume.getGroupId(), ume.getArtifactId(), ume.getVersion()));
                        } else if (mp.getException() instanceof PluginResolutionException) {
                            Plugin plugin = ((PluginResolutionException) mp.getException()).getPlugin();
                            // XXX this is not actually accurate; should rather pick out the ArtifactResolutionException & ArtifactNotFoundException inside
                            problemReporter.addMissingArtifact(EmbedderFactory.getProjectEmbedder().createArtifact(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion(), "jar"));
                        } else if (mp.getException() instanceof PluginManagerException) {
                            PluginManagerException ex = (PluginManagerException) mp.getException();                            
                            problemReporter.addMissingArtifact(EmbedderFactory.getProjectEmbedder().createArtifact(ex.getPluginGroupId(), ex.getPluginArtifactId(), ex.getPluginVersion(), "jar"));
                        }
                    }
                }
            } else {
                String msg = e.getMessage();
                if(msg != null) {
                    LOG.log(Level.INFO, "Exception thrown while loading maven project at " + project.getProjectDirectory(), e); //NOI18N
                    toRet.add(ProjectProblem.createError(TXT_Cannot_read_model(), msg));
                } else {
                    String path = project.getProjectDirectory().getPath();
                    toRet.add(ProjectProblem.createError(TXT_Cannot_read_model(), TXT_NoMsg(path)));
                    LOG.log(Level.WARNING, "Exception thrown while loading maven project at " + path, e); //NOI18N
                }
            }
        }
        return toRet;
    }

    private String getDescriptionText(Throwable e) {
        String msg = e.getMessage();        
        if(msg != null) {
            return msg;
        } else {
            String path = project.getProjectDirectory().getPath();
            return TXT_NoMsg(path);
        }
    }
    
    //-------------------------------------------------------------------------
    // ActionProvider implementation
    
    private static final String[] PROBLEM_ACTIONS = { ActionProvider.COMMAND_PRIME };

    @Override
    public ActionProvider getActionProvider() {
        return primingProvider;
    }
    
    private class PrimingActionProvider implements ActionProvider {
        @Override
        public String[] getSupportedActions() {
            return PROBLEM_ACTIONS;
        }

        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            final ActionProgress listener = ActionProgress.start(context);
            if (!PROBLEM_ACTIONS[0].equals(command)) {
                throw new IllegalArgumentException(command);
            }
            // just keep the reference, so SABA is not collected.
            Collection<? extends ProjectProblem> probs = doGetProblems(true);
            // sanity build action has been created
            SanityBuildAction saba = cachedSanityBuild.get();
            if (saba == null) {
                LOG.log(Level.FINE, "Sanity build action does not exist");
                listener.finished(true);
            } else {
                LOG.log(Level.FINE, "Resolving sanity build action");
                CompletableFuture<ProjectProblemsProvider.Result> r = saba.resolve();
                r.whenComplete((a, e) -> {
                   listener.finished(e == null); 
                });
            }
        }

        @Override
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            if (!PROBLEM_ACTIONS[0].equals(command)) {
                return false;
            }
            Collection<? extends ProjectProblem> probs = doGetProblems(false);
            if (probs == null) {
                // no value means that cache was not populated yet, Conservatively enable.
                LOG.log(Level.FINE, "Priming action enabled because problems are not yet evaluated.");
                return true;
            }
            if (probs.isEmpty()) {
                // problems identified: there are none. No primiing build.
                LOG.log(Level.FINE, "Priming action disabled, no problems found.");
                return false;
            }
            // sanity build action has been created
            SanityBuildAction saba = cachedSanityBuild.get();
            if (saba == null) {
                // other problems, but no need to prime.
                LOG.log(Level.FINE, "Problems present, but no SanityBuildAction created");
                return false;
            }
            Future<?> res = saba.getPendingResult();
            // do not enabel, if the priming build was already started.
            LOG.log(Level.FINE, "Sanity build state is: {0}", res == null || res.isDone());
            return res == null || !res.isDone();
        }

    }
}
