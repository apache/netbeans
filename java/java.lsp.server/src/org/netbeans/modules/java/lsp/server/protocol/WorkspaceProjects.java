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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import static org.netbeans.modules.java.lsp.server.protocol.Server.projectPath;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/** Synchronizes workspace with list of opened projects.
 * 
 * @see OpenProjects
 */
final class WorkspaceProjects {
    private final WorkspaceServiceImpl workspaceService;
    private final Supplier<LanguageClient> lazyClient;

    WorkspaceProjects(WorkspaceServiceImpl workspaceService, Supplier<LanguageClient> lazyClient) {
        this.workspaceService = workspaceService;
        this.lazyClient = lazyClient;
    }

    /**
     * For diagnostic purposes
     */
    private final AtomicInteger openRequestId = new AtomicInteger(1);

    /**
     * Workspace folders (nonproject) accepted by the user; we do not ask for
     * opening projects underneath these folders.
     */
    // @GuardedBy(this)
    private final List<FileObject> acceptedWorkspaceFolders = new ArrayList<>();

    /**
     * All projects opened by this LSP server. The collection is replaced every
     * time the set of opened projects change, collections are never modified.
     */
    private volatile Collection<Project> openedProjects = Collections.emptyList();

    /**
     * Holds projects opened in the LSP workspace; these projects serve as root
     * points for other projects opened behind the scenes. The value is
     * initially uncompleted, but is replaced by a <b>completed</b> future at
     * any time the set of workspace projects change.
     */
    private volatile CompletableFuture<Project[]> workspaceProjects = new CompletableFuture<>();

    /**
     * Projects opened based on files. This registry avoids duplicate questions
     * if more files are opened at the same time; the project question is
     * displayed just for the first time.
     */
    // @GuardedBy(this)
    private final Map<Project, CompletableFuture<Project>> openingFileOwners = new HashMap<>();

    /**
     * Projects that are or were opened. After projects open, their
     * CompletableFutures remain here to signal no further priming build is
     * required.
     */
    // @GuardedBy(this)
    private final Map<Project, CompletableFuture<Void>> beingOpened = new HashMap<>();

    /**
     * Open projects that own the `projectCandidates` files asynchronously.
     * Returns immediately, results or errors are reported through the Future.
     *
     * @param projectCandidates files whose projects should be opened.
     * @return future that yields the opened project instances.
     */
    CompletableFuture<Project[]> asyncOpenSelectedProjects(List<FileObject> projectCandidates, boolean addWorkspace) {
        if (projectCandidates == null || projectCandidates.isEmpty()) {
            return CompletableFuture.completedFuture(new Project[0]);
        }
        CompletableFuture<Project[]> f = new CompletableFuture<>();
        Server.LOG.log(Level.FINER, "Asked to open project(s): {0}", Arrays.asList(projectCandidates));
        Server.LOG.log(Level.FINER, "Caller:", new Throwable());
        Server.SERVER_INIT_RP.post(() -> {
            asyncOpenSelectedProjects0(f, projectCandidates, addWorkspace, false);
        });
        return f;
    }

    @NbBundle.Messages({
        "PROMPT_AskOpenProjectForFile=File {0} belongs to project {1}. To enable all features, the project should be opened"
        + " and initialized by the Language Server. Do you want to proceed ?",
        "PROMPT_AskOpenProject=To enable all features of project {0}, it should be opened"
        + " and initialized by the Language Server. Do you want to proceed ?",
        "PROMPT_AskOpenProjectForFile_Yes=Open and initialize",
        "PROMPT_AskOpenProjectForFile_No=No",
        "PROMPT_AskOpenProjectForFile_Unnamed=(unnamed)"
    })
    CompletableFuture<Project> asyncOpenFileOwner(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        if (prj == null) {
            return CompletableFuture.completedFuture(null);
        }
        // first wait on the initial workspace open/init.
        return workspaceProjects.thenCompose((wprj) -> {
            CompletableFuture<Project[]> f = new CompletableFuture<>();
            CompletableFuture<Project> g = f.thenApply(arr -> arr.length > 0 ? arr[0] : null);
            Collection<Project> prjs = Arrays.asList(wprj);

            boolean openImmediately = false;
            synchronized (this) {
                if (openedProjects.contains(prj)) {
                    // shortcut
                    return CompletableFuture.completedFuture(prj);
                }
                CompletableFuture<Void> h = beingOpened.get(prj);
                if (h != null) {
                    // already being really opened
                    return h.thenApply((unused) -> prj);
                }
                // the project is already being asked for; otherwise leave
                // a trace + flag so the project is not asked again.
                CompletableFuture<Project> p = openingFileOwners.putIfAbsent(prj, g);
                if (p != null) {
                    return p;
                }
                // if any of the parent projects is among the opened ones,
                // then we are permitted
                for (Project check : projectPath(prj, false)) {
                    if (prjs.contains(check)) {
                        openImmediately = true;
                        break;
                    }
                }
                if (!openImmediately) {
                    FileObject pdir = prj.getProjectDirectory();
                    // accept projects in folders which were not recognized as project parts.
                    for (FileObject wf : acceptedWorkspaceFolders) {
                        if (wf.equals(pdir) || FileUtil.isParentOf(wf, pdir)) {
                            openImmediately = true;
                        }
                    }
                }
            }
            if (openImmediately) {
                // open without asking
                Server.SERVER_INIT_RP.post(() -> {
                    asyncOpenSelectedProjects0(f, Collections.singletonList(file), false, false);
                });
            } else {
                ProjectInformation pi = ProjectUtils.getInformation(prj);
                String dispName = pi != null ? pi.getDisplayName() : Bundle.PROMPT_AskOpenProjectForFile_Unnamed();
                final MessageActionItem yes = new MessageActionItem(Bundle.PROMPT_AskOpenProjectForFile_Yes());
                ShowMessageRequestParams smrp = new ShowMessageRequestParams(Arrays.asList(
                        yes,
                        new MessageActionItem(Bundle.PROMPT_AskOpenProjectForFile_No())
                ));
                if (prj.getProjectDirectory() == file) {
                    smrp.setMessage(Bundle.PROMPT_AskOpenProject(dispName));
                } else {
                    smrp.setMessage(Bundle.PROMPT_AskOpenProjectForFile(file.getNameExt(), dispName));
                }
                smrp.setType(MessageType.Info);

                getClient().showMessageRequest(smrp).thenAccept(ai -> {
                    if (!yes.equals(ai)) {
                        f.completeExceptionally(new CancellationException());
                        return;
                    }
                    Server.SERVER_INIT_RP.post(() -> {
                        asyncOpenSelectedProjects0(f, Collections.singletonList(file), false, false);
                    });
                });
            }
            return f.thenApply(arr -> arr.length > 0 ? arr[0] : null);
        });
    }

    public List<FileObject> getAcceptedWorkspaceFolders() {
        return acceptedWorkspaceFolders;
    }

    void asyncOpenSelectedProjects0(CompletableFuture<Project[]> f, List<FileObject> projectCandidates, boolean asWorkspaceProjects, boolean validParents) {
        List<Project> projects = new ArrayList<>();
        List<FileObject> nonProjects = new ArrayList<>();
        List<FileObject> haveProjects = new ArrayList<>();
        Project[] candidateMapping = new Project[projectCandidates.size()];
        try {
            int index = 0;
            if (projectCandidates != null) {
                for (FileObject candidate : projectCandidates) {
                    Project prj = FileOwnerQuery.getOwner(candidate);
                    Server.LOG.log(Level.FINER, "Opening {0} for candidate {1}, directory is {2}", new Object[]{prj, candidate, prj == null ? null : prj.getProjectDirectory()});
                    if (prj != null) {
                        candidateMapping[index] = prj;
                        projects.add(prj);
                        haveProjects.add(prj.getProjectDirectory());
                    } else if (validParents && candidate.isFolder()) {
                        nonProjects.add(candidate);
                    }
                    index++;
                }
                synchronized (this) {
                    boolean nwp = asWorkspaceProjects;
                    for (FileObject pd : haveProjects) {
                        for (FileObject wf : new ArrayList<>(acceptedWorkspaceFolders)) {
                            if (wf.equals(pd) || FileUtil.isParentOf(pd, wf)) {
                                Server.LOG.log(Level.FINE, "Nonproject workspace folder turned to project: {0}", projectCandidates.get(0));
                                acceptedWorkspaceFolders.remove(wf);
                                // we should call asyncOpenSelectedProjects1 twice, once to add to workspace, once to not add -
                                // but it should only happen with single-file open = just one project.
                                if (projectCandidates.size() == 1) {
                                    nwp = true;
                                }
                            }
                        }
                    }
                    A:
                    for (FileObject np : nonProjects) {
                        for (FileObject c : acceptedWorkspaceFolders) {
                            if (c.equals(np) || FileUtil.isParentOf(c, np)) {
                                continue A;
                            }
                        }
                        Server.LOG.log(Level.FINE, "Not recognized as a project, but accepting as workspace : {0}", np);
                        acceptedWorkspaceFolders.add(np);
                    }
                    asWorkspaceProjects = nwp;
                }
            }
            Project[] previouslyOpened;
            try {
                previouslyOpened = OpenProjects.getDefault().openProjects().get();
                if (previouslyOpened.length > 0) {
                    Level level = Level.FINEST;
                    assert (level = Level.CONFIG) != null;
                    for (Project p : previouslyOpened) {
                        Server.LOG.log(level, "Previously opened project at {0}", p.getProjectDirectory());
                    }
                }
            } catch (InterruptedException | ExecutionException ex) {
                throw new IllegalStateException(ex);
            }
            asyncOpenSelectedProjects1(f, previouslyOpened, candidateMapping, projects, asWorkspaceProjects);
        } catch (RuntimeException ex) {
            f.completeExceptionally(ex);
        }
    }

    private void asyncOpenSelectedProjects1(CompletableFuture<Project[]> f, Project[] previouslyOpened, Project[] candidateMapping, List<Project> initialProjects, boolean addToWorkspace) {
        long t = System.currentTimeMillis();
        int id = openRequestId.getAndIncrement();
        Map<Project, CompletableFuture<Void>> local = new HashMap<>();
        CompletableFuture[] primingBuilds = primeProjects(initialProjects, id, local);
            AtomicReference<Consumer<Collection<Project>>> subprojectProcessor = new AtomicReference();
            Set<Project> processedProjects = new HashSet<>();
            AtomicInteger level = new AtomicInteger(1);
            subprojectProcessor.set((projects) -> {
                Set<Project> additionalProjects = new LinkedHashSet<>();
                for (Project prj : projects) {
                    Set<Project> containedProjects = ProjectUtils.getContainedProjects(prj, true);
                    if (containedProjects != null) {
                        Server.LOG.log(Level.FINE, "Project {0} reports contained projects: {1}", new Object[] { prj, containedProjects });
                        additionalProjects.addAll(containedProjects);
                    }
                }
                additionalProjects.removeAll(processedProjects);
                additionalProjects.removeAll(projects);

                processedProjects.addAll(projects);

                Server.LOG.log(Level.FINE, "Processing subprojects, level {0}: {1}", new Object[] { level.getAndIncrement(), additionalProjects });

                if (additionalProjects.isEmpty()) {
                    OpenProjects.getDefault().open(processedProjects.toArray(new Project[processedProjects.size()]), false);
                    try {
                        Server.LOG.log(Level.FINER, "{0}: Calling openProjects() for : {1}", new Object[]{id, Arrays.asList(processedProjects)});
                        OpenProjects.getDefault().openProjects().get();
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new IllegalStateException(ex);
                    }
                    for (Project prj : processedProjects) {
                        //init source groups/FileOwnerQuery:
                        ProjectUtils.getSources(prj).getSourceGroups(Sources.TYPE_GENERIC);
                        final CompletableFuture<Void> prjF = local.get(prj);
                        if (prjF != null) {
                            prjF.complete(null);
                        }
                    }
                    Set<Project> projectSet = new HashSet<>(Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
                    projectSet.retainAll(openedProjects);
                    projectSet.addAll(processedProjects);

                    Project[] prjsRequested = projects.toArray(new Project[processedProjects.size()]);
                    Project[] prjs = projects.toArray(new Project[processedProjects.size()]);
                    Server.LOG.log(Level.FINER, "{0}: Finished opening projects: {1}", new Object[]{id, Arrays.asList(processedProjects)});
                    synchronized (this) {
                        openedProjects = projectSet;
                        if (addToWorkspace) {
                            Set<Project> ns = new HashSet<>(processedProjects);
                            List<Project> current = Arrays.asList(workspaceProjects.getNow(new Project[0]));
                            int s = current.size();
                            ns.addAll(current);
                            Server.LOG.log(Level.FINER, "Current is: {0}, ns: {1}", new Object[] { current, ns });
                            if (s != ns.size()) {
                                prjs = ns.toArray(new Project[ns.size()]);
                                workspaceProjects.complete(prjs);
                                workspaceProjects = CompletableFuture.completedFuture(prjs);
                            }
                        }
                        for (Project p : prjs) {
                            // override flag in opening cache, no further questions asked.
                            openingFileOwners.put(p, f.thenApply(unused -> p));
                        }
                    }
                    f.complete(candidateMapping);
                    List<FileObject> workspaceClientFolders = workspaceService.getClientWorkspaceFolders();
                    Server.LSP_SERVER_TELEMETRY.sendWorkspaceInfo(getClient(), workspaceClientFolders, openedProjects, System.currentTimeMillis() - t);
                    Server.LOG.log(Level.INFO, "{0} projects opened in {1}ms", new Object[] { prjsRequested.length, (System.currentTimeMillis() - t) });
                } else {
                    Server.LOG.log(Level.FINER, "{0}: Collecting projects to prime from: {1}", new Object[]{id, Arrays.asList(additionalProjects)});
                    CompletableFuture[] nextPrimingBuilds = primeProjects(additionalProjects, id, local);
                    CompletableFuture.allOf(nextPrimingBuilds).thenRun(() -> {
                        subprojectProcessor.get().accept(additionalProjects);
                    }).exceptionally(e -> {
                        f.completeExceptionally(e);
                        return null;
                    });
                }
            });        // Wait for all priming builds, even those already pending, to finish:
        CompletableFuture.allOf(primingBuilds).thenRun(() -> {
            subprojectProcessor.get().accept(initialProjects);
        }).exceptionally(e -> {
            f.completeExceptionally(e);
            return null;
        });
    }

    CompletableFuture<Void>[] primeProjects(Collection<Project> projects, int id, Map<Project, CompletableFuture<Void>> local) {
        List<Project> toOpen = new ArrayList<>();
        List<CompletableFuture<Void>> primingBuilds = new ArrayList<>();

        synchronized (this) {
            Server.LOG.log(Level.FINER, "{0}: Opening project(s): {1}", new Object[]{id, Arrays.asList(projects)});
            for (Project p : projects) {
                CompletableFuture<Void> pending = beingOpened.get(p);
                if (pending != null) {
                    primingBuilds.add(pending);
                } else {
                    toOpen.add(p);
                    local.put(p, new CompletableFuture<Void>());
                }
            }
            beingOpened.putAll(local);
        }

        Server.LOG.log(Level.FINER, id + ": Opening projects: {0}", Arrays.asList(toOpen));

        // before the projects are officialy 'opened', try to prime the projects
        for (Project p : toOpen) {
            ActionProvider pap = p.getLookup().lookup(ActionProvider.class);
            if (pap == null) {
                Server.LOG.log(Level.FINER, "{0}: No action provider at all !", id);
                continue;
            }
            if (!Arrays.asList(pap.getSupportedActions()).contains(ActionProvider.COMMAND_PRIME)) {
                Server.LOG.log(Level.FINER, "{0}: No action provider gives PRIME", id);
                // this may take some while; so better call outside of any locks.
                continue;
            }
            Server.LOG.log(Level.FINER, "{0}: Found Priming action: {1}", new Object[]{id, p});
            if (pap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY)) {
                final CompletableFuture<Void> primeF = new CompletableFuture<>();
                Server.LOG.log(Level.FINER, "{0}: Found enabled Priming build for: {1}", new Object[]{id, p});
                ActionProgress progress = new ActionProgress() {
                    @Override
                    protected void started() {
                    }

                    @Override
                    public void finished(boolean success) {
                        Server.LOG.log(Level.FINER, id + ": Priming build completed for project " + p);
                        primeF.complete(null);
                    }
                };
                primingBuilds.add(primeF);

                pap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(progress));
            }
        }
        return primingBuilds.toArray(new CompletableFuture[0]);
    }

    CompletableFuture<Project[]> getWorkspaceProjects() {
        return workspaceProjects;
    }

    private LanguageClient getClient() {
        return lazyClient.get();
    }

}
