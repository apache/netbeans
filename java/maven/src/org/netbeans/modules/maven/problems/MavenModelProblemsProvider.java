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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import org.openide.util.Pair;

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
    static final RequestProcessor RP  = new RequestProcessor(MavenModelProblemsProvider.class);
    private static final Logger LOG = Logger.getLogger(MavenModelProblemsProvider.class.getName());
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final Project project;
    private final PrimingActionProvider primingProvider = new PrimingActionProvider();

    private ProblemReporterImpl problemReporter;

    // @GuardedBy(this)
    private Pair<Collection<ProjectProblem>, Boolean> problemsCache = null;
    // @GuardedBy(this)
    private boolean projectListenerSet;

    /**
     * The Maven project that has been processed already.
     */
    private Reference<MavenProject> analysedProject = new WeakReference<>(null);
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
     * Flag set during creation of sanity build action. Usable only inside synchronized
     * section of the problem resolver.
     */
    private boolean sanityBuildStatus;
            
    public boolean isSanityBuildNeeded() {
        return doGetProblems1(true).second();
    }
    
    /**
     * Compute problems. If 'sync' is true, the computation is done synchronously. Caches results,
     * returns cache content immediately, if available.
     * @param sync true = run synchronously. False = fork computation/
     * @return project problems.
     */
    Collection<? extends ProjectProblem> doGetProblems(boolean sync) {
        return doGetProblems1(sync).first();
    }
        
    /**
     * Analyzes problem, returns list of problems and priming build status. The returned {@link Pair}
     * contains the list of problems and true/false whether the priming build seems necessary. The last result
     * is cached for the given maven model instance. If the project was reloaded, the problems will be computed
     * again for the new project instance. The call might block waiting on the pending project reload. If `sync' 
     * is false, the method will just post in request processor and return {@code null}.
     * 
     * @param sync if the call should complete synchronously
     */
    private Pair<Collection<ProjectProblem>, Boolean> doGetProblems1(boolean sync) {
        final CompletableFuture<MavenProject> pending = ((NbMavenProjectImpl)project).getFreshOriginalMavenProject();
        Callable<Pair<Collection<ProjectProblem>, Boolean>> c;
    
        synchronized (this) {
            //lazy adding listener only when someone asks for the problems the first time
            if (!projectListenerSet) {
                projectListenerSet = true;
                //TODO do we check only when the project is opened?
                problemReporter = project.getLookup().lookup(NbMavenProjectImpl.class).getProblemReporter();
                assert problemReporter != null;
                project.getLookup().lookup(NbMavenProject.class).addPropertyChangeListener(projectListener);
            
            }
            MavenProject o = analysedProject.get();
            //for non changed project models, no need to recalculate, always return the cached value
            if (pending.isDone()) {
                try {
                    // cannot block, if .isDone().
                    MavenProject updatedPrj = pending.get();
                    LOG.log(Level.FINER, "Called getProblems for {0}, analysed = {1}, current = {2}", 
                            new Object[] { project, System.identityHashCode(o), System.identityHashCode(updatedPrj) });
                    Object wasprocessed = updatedPrj.getContextValue(MavenModelProblemsProvider.class.getName());
                    if (o == updatedPrj && wasprocessed != null) {
                        Pair<Collection<ProjectProblem>, Boolean> cached = problemsCache;
                        LOG.log(Level.FINER, "getProblems: Project was processed, cached is: {0}", cached);
                        if (cached != null) {
                            return cached;
                        }
                    } 
                } catch (ExecutionException | InterruptedException ex) {
                    LOG.log(Level.FINER, "Project load for {0} threw exception {1}", new Object[] { project, ex.getMessage() });
                    LOG.log(Level.FINER, "Stacktrace:", ex);
                }
            } else {
                LOG.log(Level.FINER, "Called getProblems for {0}, analysed = {1}, current = PENDING",
                        new Object[] { project, System.identityHashCode(o) });
            }
            
            SanityBuildAction sba = cachedSanityBuild.get();
            if (sba != null && sba.getPendingResult() == null) {
                cachedSanityBuild.clear();
            }
            
            // PENDING: think if .thenApplyAsync would be more useful.
            c = () -> {
                // double check, the project may be invalidated during the time.
                MavenProject prj;
                
                try {
                    prj = ((NbMavenProjectImpl)project).getFreshOriginalMavenProject().get();
                    LOG.log(Level.FINER, "Evaluating getProblems for {0}, analysed = {1}, current = {2}", 
                            new Object[] { project, System.identityHashCode(o), System.identityHashCode(prj) });
                } catch (ExecutionException | InterruptedException ex) {
                    // should not happen
                    LOG.log(Level.FINER, "Project load for {0} threw exception {1}", new Object[] { project, ex.getMessage() });
                    LOG.log(Level.FINER, "Stacktrace:", ex);
                    return Pair.of( new ArrayList<>(), sanityBuildStatus);
                }
                Object wasprocessed2 = prj.getContextValue(MavenModelProblemsProvider.class.getName());
                synchronized (MavenModelProblemsProvider.this) {
                    if (wasprocessed2 != null) {
                        Pair<Collection<ProjectProblem>, Boolean> cached = problemsCache;
                        LOG.log(Level.FINER, "getProblems: Project was processed #2, cached is: {0}", cached);
                        if (cached != null) {                            
                            return cached;
                        }
                    } 
                }
                int round = 0;
                List<ProjectProblem> toRet = null;
                while (round <= 1) {
                    try {
                        LOG.log(Level.FINER, "Analysing project {0}@{1}, round {2}", new Object[] { prj, System.identityHashCode(prj), round });
                        boolean ok = false;
                        synchronized (MavenModelProblemsProvider.this) {
                            try {
                                sanityBuildStatus = false;
                                checkMissing = round < 1;
                                toRet = new ArrayList<>();
                                MavenExecutionResult res = MavenProjectCache.getExecutionResult(prj);
                                if (res != null && res.hasExceptions()) {
                                    Collection<ProjectProblem> exceptions = reportExceptions(res);
                                    LOG.log(Level.FINE, "Project has loaded with exceptions: {0}", exceptions);
                                    toRet.addAll(exceptions);
                                }
                                //#217286 doArtifactChecks can call FileOwnerQuery and attempt to aquire the project mutex.
                                toRet.addAll(doArtifactChecks(prj));
                                LOG.log(Level.FINER, "getProblems: Project {1} processing finished, result is: {0}",
                                        new Object[] { toRet, prj });
                                ok = true;
                                break;
                            } finally {
                                if (ok || round > 0) {
                                    LOG.log(Level.FINER, "Project {0} problems: {1}, sanity {2}, ok {3}, round {4}", new Object[] {
                                        prj, sanityBuildStatus, ok, round
                                    });
                                    //mark the project model as checked once and cached
                                    prj.setContextValue(MavenModelProblemsProvider.class.getName(), new Object());
                                    // change globals before exiting synchronized section
                                    problemsCache = Pair.of(toRet, sanityBuildStatus);
                                    analysedProject = new WeakReference<>(prj);
                                }
                                checkMissing = true;
                            }
                        }
                    } catch (ProblemReporterImpl.ArtifactFoundException ex) {
                        // should never happen with round > 0
                        assert round < 1;
                        round++;
                        LOG.log(Level.FINER, "getProblems: Project {1} reported missing artifact that actually exists, restarting - {0} round",
                                new Object[] { round, prj });
                        // force reload, then wait for the reload to complete
                        NbMavenProject.fireMavenProjectReload(project);
                        try {
                            prj = ((NbMavenProjectImpl)project).getFreshOriginalMavenProject().get();
                        } catch (ExecutionException | InterruptedException ex2) {
                            // should not happen
                            LOG.log(Level.FINER, "Project load for {0} threw exception {1}", new Object[] { project, ex2.getMessage() });
                            LOG.log(Level.FINER, "Stacktrace:", ex2);
                            break;
                        }
                    }
                }
                if (prj != null && !sanityBuildStatus) {
                    prj.setContextValue("org.netbeans.modules.maven.problems.primingNotDone", null);
                    LOG.log(Level.FINE, "Clearing priming status of {0}, fallback status is {1}", new Object[] { prj, NbMavenProject.isErrorPlaceholder(prj) });
                }
                //mark the project model as checked once and cached
                firePropertyChange();
                return Pair.of(toRet, sanityBuildStatus);
            };
        }
        if(sync || Boolean.getBoolean("test.reload.sync")) {
            try {
                return c.call();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            RP.submit(c);
        }
        
        // indicate that we do not know
        return Pair.of(null, true);
    }
    
    private void firePropertyChange() {
        support.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
    }
    
    // @GuardedBy(this)
    private boolean checkMissing = true;
    
    private void addMissingArtifact(Artifact a) {
        problemReporter.addMissingArtifact(a, checkMissing);
    }
    
    private static String artifactId(Artifact a) {
        return a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion() + ":" + 
                (a.getClassifier() == null ? "" : a.getClassifier()) + "/" + a.getType();
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
        LOG.log(Level.FINE, "Performing artifact checks for {0}", project);
        
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
        List<Artifact> artifactsToCheck = new ArrayList<>(project.getArtifacts());
        MavenProject partial = MavenProjectCache.getPartialProject(project);
        Collection<Artifact> fakes = (Collection<Artifact>)project.getContextValue("NB_FakedArtifacts");
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Checking artifacts: {0}", artifactsToCheck);
            if (partial != null && partial != project) {
                Collection<Artifact> partialFakes = (Collection<Artifact>)partial.getContextValue("NB_FakedArtifacts");
                LOG.log(Level.FINER, "Partial project for {0}@{1} is: {2}@{3}, fake artifacts: {4}", new Object[] { 
                    project, System.identityHashCode(project), partial, System.identityHashCode(partial), partialFakes
                });
            }
            LOG.log(Level.FINER, "Fake artifacts for {0}@{1}: {2}", new Object[] { 
                project, System.identityHashCode(project), fakes
            });
        }
        
        Collection<Artifact> toCheck = new HashSet<>(project.getArtifacts());
        if (fakes != null) {
            // the fake artifacts are typically without a scope, so ignore scope when merging with other reported pieces.
            Set<String> ids = toCheck.stream().map(MavenModelProblemsProvider::artifactId).collect(Collectors.toSet());
            fakes.stream().filter(a -> !ids.contains(artifactId(a))).forEach(toCheck::add);
        }
        
        for (Artifact art : toCheck) {
            File file = art.getFile();
            LOG.log(Level.FINEST, "Checking {0}", art);
            if (file == null || !file.exists()) {                
                LOG.log(Level.FINEST, "File does not exist for {0}", art);
                if(Artifact.SCOPE_SYSTEM.equals(art.getScope())){
                    //TODO create a correction action for this.
                    toRet.add(ProjectProblem.createWarning(ERR_SystemScope(), MSG_SystemScope(), new ProblemReporterImpl.MavenProblemResolver(OpenPOMAction.instance().createContextAwareInstance(Lookups.fixed(project)), "SCOPE_DEPENDENCY")));
                } else {
                    addMissingArtifact(art);
                    if (file == null) {
                        missingNonSibling = true;
                    } else {
                        final URL archiveUrl = FileUtil.urlForArchiveOrDir(file);
                        LOG.log(Level.FINEST, "File for {0} is {1}, archive URL {2}", new Object[] { art, file, archiveUrl });
                        if (archiveUrl != null) { //#236050 null check
                            //a.getFile should be already normalized
                            SourceForBinaryQuery.Result2 result = SourceForBinaryQuery.findSourceRoots2(archiveUrl);
                            if (!result.preferSources() || /* SourceForBinaryQuery.EMPTY_RESULT2.preferSources() so: */ result.getRoots().length == 0) {
                                LOG.log(Level.FINE, "Missing nonsibling artifact: {0}", art);
                                missingNonSibling = true;
                            } // else #189442: typically a snapshot dep on another project
                        }
                    }
                    missingJars.add(art);
                }
            } else if (NbArtifactFixer.isFallbackFile(file)) {
                LOG.log(Level.FINEST, "Artifact is a fallback {0} with file {1}", new Object[] { art, file });
                addMissingArtifact(art);
                missingJars.add(art);
                missingNonSibling = true;
            }
        }
        if (!missingJars.isEmpty()) {
            StringBuilder mess = new StringBuilder();
            for (Artifact art : missingJars) {
                mess.append(art.getId()).append('\n');
            }
            LOG.log(Level.FINER, "Project is missing artifacts: {0}, nonlocal = {1}", new Object[] { missingJars, missingNonSibling });
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
                addMissingArtifact(EmbedderFactory.getProjectEmbedder().createArtifact(m.getGroupId(), m.getArtifactId(), m.getVersion(), "pom"));
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
            sanityBuildStatus = true;
            LOG.log(Level.FINE, "Creating sanity build action for {0}", project.getProjectDirectory());
            if (a != null) {
                Future<ProjectProblemsProvider.Result> r = a.getPendingResult();
                if (r != null) {
                    return a;
                }
            }
            a = new SanityBuildAction(project, this::isSanityBuildNeeded);
            project.getLookup().lookup(NbMavenProject.class).getMavenProject().setContextValue("org.netbeans.modules.maven.problems.primingNotDone", true);
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
                addMissingArtifact(((ArtifactResolutionException) e).getArtifact());
                
            } else if (e instanceof ArtifactNotFoundException) { // XXX when does this occur?
                toRet.add(ProjectProblem.createError(TXT_Artifact_Not_Found(), getDescriptionText(e)));
                addMissingArtifact(((ArtifactNotFoundException) e).getArtifact());
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
                            addMissingArtifact(EmbedderFactory.getProjectEmbedder().createProjectArtifact(ume.getGroupId(), ume.getArtifactId(), ume.getVersion()));
                        } else if (mp.getException() instanceof PluginResolutionException) {
                            Plugin plugin = ((PluginResolutionException) mp.getException()).getPlugin();
                            // XXX this is not actually accurate; should rather pick out the ArtifactResolutionException & ArtifactNotFoundException inside
                            addMissingArtifact(EmbedderFactory.getProjectEmbedder().createArtifact(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion(), "jar"));
                        } else if (mp.getException() instanceof PluginManagerException) {
                            PluginManagerException ex = (PluginManagerException) mp.getException();                            
                            addMissingArtifact(EmbedderFactory.getProjectEmbedder().createArtifact(ex.getPluginGroupId(), ex.getPluginArtifactId(), ex.getPluginVersion(), "jar"));
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
                CompletableFuture<ProjectProblemsProvider.Result> r = saba.resolve(context);
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
