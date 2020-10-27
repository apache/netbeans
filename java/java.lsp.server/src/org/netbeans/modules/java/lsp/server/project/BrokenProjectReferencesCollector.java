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
package org.netbeans.modules.java.lsp.server.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author sdedic
 */
public class BrokenProjectReferencesCollector implements PropertyChangeListener {
    private static final RequestProcessor BROKEN_REPORTER_RP = new RequestProcessor(BrokenProjectReferencesCollector.class);
    private static final int REPORT_DELAY = 3000;
    
    private final Lookup    clientLookup;
    private final Project   project;
    private final ProjectProblemsProvider projectProvider;
    private final AtomicInteger serial = new AtomicInteger(0);
    private final List<MessageActionItem>   resolveItems = new ArrayList<>();
    
    private Set<ProblemReference> problems = Collections.emptySet();
    private Set<ProblemReference> alreadyReported = Collections.emptySet();
    private PropertyChangeListener weakL;
    private Consumer<BrokenProjectReferencesCollector> unregister;
    private RequestProcessor.Task scheduled;

    private int reportedSerial;
    private State state = State.IDLE;
    private List<ProblemReference> reportingProblems = Collections.emptyList();
    private NbCodeLanguageClient client;
    private CompletableFuture<Void> resolving = CompletableFuture.completedFuture(null);
    
    
    static enum State {
        IDLE,
        COLLECT,
        REPORTING,
        RESOLVING,
    }
    
    @NbBundle.Messages({
        "OPTION_RESOLVE=Resolve...",
        "OPTION_IGNORE=Ignore",
    })
    public BrokenProjectReferencesCollector(Lookup clientLookup, Project project, ProjectProblemsProvider provider) {
        this.clientLookup = clientLookup;
        this.project = project;
        this.projectProvider = provider;
        
        resolveItems.add(new MessageActionItem(Bundle.OPTION_RESOLVE()));
        resolveItems.add(new MessageActionItem(Bundle.OPTION_IGNORE()));
    }
    
    Set<ProblemReference> loadProblems() {
        return ProjectManager.mutex().readAccess(() -> {
            final Set<ProblemReference> all = new LinkedHashSet<ProblemReference>();
            for (ProjectProblemsProvider.ProjectProblem problem : projectProvider.getProblems()) {
                all.add(new ProblemReference(problem, project, false));
            }
            return all;
        });
    }
    
    void cancel() {
        Consumer<BrokenProjectReferencesCollector> un;
        synchronized (this) {
            if (scheduled != null) {
                scheduled.cancel();
            }
            if (weakL != null) {
                projectProvider.removePropertyChangeListener(weakL);
                weakL = null;
            }
            un = unregister;
            unregister = null;
        }
        if (un != null) {
            un.accept(this);
        }
    }
    
    void touch() {
        serial.incrementAndGet();
    }
    
    @NbBundle.Messages({
        "# {0} - problem title",
        "# {1} - project name",
        "FMT_SingleProjectProblem=A problem was found in the project {1}:\n{0}",
        "# {0} - project name",
        "# {1} - problem count",
        "FMT_MultipleProjectsProblemHeader={1} problems were found in the project {0}:\n",
        "# {0} - problem title",
        "FMT_MultipleProjectsProblemItem=- {0}",
        "# {0} - project name",
        "# {1} - problem count",
        "FMT_ProbjectProblemsCaption1=Problem(s) found in project {0}"
    })
    CompletableFuture<MessageActionItem> reportToClient() {
        synchronized (this) {
            this.reportingProblems = new ArrayList<>(problems);
        }
        
        sendClientQuery().thenCompose(this::processClientResponse);
        return null;
    }
    
    void markItemsReported() {
        synchronized (this) {
            alreadyReported.removeAll(reportingProblems);
            reportingProblems.stream().filter((r) -> !r.resolved).forEach(alreadyReported::add);
            reportingProblems.clear();
        }
    }
    
    CompletableFuture<Void> processClientResponse(MessageActionItem selection) {
        synchronized (this) {
            if (state != State.REPORTING) {
                CompletableFuture v = CompletableFuture.completedFuture(null);
                v.completeExceptionally(new IllegalStateException());
                return v;
            }
        }
        if (selection == null || !resolveItems.get(0).equals(selection)) {
            // cancelled
            markItemsReported();
            return CompletableFuture.completedFuture(null);
        }
        synchronized (this) {
            state = State.RESOLVING;
            resolving = new CompletableFuture<>();
            BROKEN_REPORTER_RP.submit(this::resolveReportedProblems);
            return resolving.thenRun(this::problemsResolved);
        }
    }
    
    void problemsResolved() {
        boolean cancel = false;
        boolean advanced;
        synchronized (this) {
            markItemsReported();
            state = State.IDLE;
            advanced = reportedSerial != serial.get();
            if (alreadyReported.isEmpty() && advanced) {
                cancel = true;
            }
        }
        if (cancel) {
            cancel();
        } else if (advanced) {
            updateProblems(false);
        }
    }
    
    CompletableFuture<MessageActionItem> sendClientQuery() {
        synchronized (this) {
            if (state != State.REPORTING) {
                // TODO: report / exception, invalid state
                return CompletableFuture.completedFuture(null);
            }
            reportedSerial = serial.get();
        }
        String contents;
        
        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        if (reportingProblems.size() == 1) {
            contents = Bundle.FMT_SingleProjectProblem(reportingProblems.get(0).problem.getDisplayName(), projectName);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(Bundle.FMT_MultipleProjectsProblemHeader(projectName, reportingProblems.size()));
            for (ProblemReference pr : reportingProblems) {
                sb.append(Bundle.FMT_MultipleProjectsProblemItem(pr.problem.getDisplayName()));
            }
            contents = sb.toString();
        }
        client = clientLookup.lookup(NbCodeLanguageClient.class);
        if (client == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        ShowMessageRequestParams params = new ShowMessageRequestParams(resolveItems);
        params.setType(MessageType.Error);
        params.setMessage(contents);
        
        return client.showMessageRequest(params);
    }
    
    void updateProblems(boolean displayIfNoChange) {
        boolean c;
        
        synchronized (this) {
            if (state != State.IDLE && state != State.COLLECT) {
                return;
            }
            state = State.COLLECT;
            scheduled = null;
            c = unregister == null;
        }
        if (c) {
            cancel();
            return;
        }
        Set<ProblemReference> problemsNow = loadProblems();
        synchronized (this) {
            problemsNow.removeAll(alreadyReported);
            if (problemsNow.equals(problems)) {
                if (displayIfNoChange) {
                    state = State.REPORTING;
                    BROKEN_REPORTER_RP.submit(this::reportToClient);
                    return;
                }
            } else {
                this.problems = problemsNow;
            }
            if (problemsNow.isEmpty()) {
                state = State.IDLE;
                return;
            }
        }
        scheduleRefresh();
    }
    
    void scheduleRefresh() {
        synchronized (this) {
            if (state != State.COLLECT) {
                return;
            }
            if (scheduled != null) {
                scheduled.schedule(REPORT_DELAY);
            } else {
                scheduled = BROKEN_REPORTER_RP.post(() -> updateProblems(true), REPORT_DELAY);
            }
        }
    }
    
    void updateReportingProblems() {
        for (ProblemReference p : loadProblems()) {
            int index = reportingProblems.indexOf(p);
            if (index != -1 && !reportingProblems.get(index).resolved) {
                reportingProblems.set(index, p);
            }
        }
    }
    
    @NbBundle.Messages({
        "# {0} - project name",
        "FMT_ResolutionFailures1=Some actions on project {0} did not complete:\n",
        "# {0} - failure description",
        "FMT_ResolutionFailuresItem=- {0}\n",
        "FMT_ResolutionErrors1=In addition, errors occurred while resolving problems. See log for the details.",
        "# {0} - project name",
        "FMT_ResolutionErrors2=Errors occurred while resolving problems of project {0}. See log for the details.",
        "# {0} - project name",
        "TEXT_ProjectProblemsResolved=Problems of project {0} have been resolved."
    })
    MessageParams formatResolvedReport(List<ProjectProblemsProvider.Result> reportResults, List<String> reportErrors) {
        StringBuilder sb = new StringBuilder();
        MessageType t = MessageType.Info;
        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        if (!reportResults.isEmpty()) {
            sb.append(Bundle.FMT_ResolutionFailures1(projectName));
            for (ProjectProblemsProvider.Result r : reportResults) {
                MessageType nt = t;
                switch (r.getStatus()) {
                    case RESOLVED_WITH_WARNING:
                        nt = MessageType.Warning;
                        break;
                    case UNRESOLVED:
                        nt = MessageType.Warning;
                        break;
                }
                if (t.getValue() > nt.getValue()) {
                    t = nt;
                }
                sb.append(Bundle.FMT_ResolutionFailuresItem(r.getMessage()));
            }
            if (!reportErrors.isEmpty()) {
                sb.append(Bundle.FMT_ResolutionErrors1());
            }
        } else if (!reportErrors.isEmpty()) {
            sb.append(Bundle.FMT_ResolutionErrors2(projectName));
        } else {
            return new MessageParams(t, Bundle.TEXT_ProjectProblemsResolved(projectName));
        }
        return new MessageParams(t, sb.toString());
    }
    
    void resolveReportedProblems() {
        synchronized (this) {
            if (state != State.RESOLVING) {
                return;
            }
        }
        List<ProjectProblemsProvider.Result> reportResults = new ArrayList<>();
        List<String> reportErrors = new ArrayList<>();
        for (int i = 0; i < reportingProblems.size(); i++) {
            ProblemReference r = reportingProblems.get(i);
            ProjectProblemsProvider.Result res;
            try {
                res = r.problem.resolve().get();
                ProjectProblemsProvider.Status s = res.getStatus();
                
                if (s != ProjectProblemsProvider.Status.RESOLVED) {
                    reportResults.add(res);
                } else {
                    r.resolved = true;
                }
                // avoid resolving a potentially resolved problem
                updateReportingProblems();
            } catch (InterruptedException | ExecutionException ex) {
                reportErrors.add(ex.getLocalizedMessage());
            }
        }
        
        MessageParams params = formatResolvedReport(reportResults, reportErrors);
        if (params != null) {
            client.showMessage(params);
        }
    }
    
    /**
     * Activates or reactivates the controller. The controller waits after activation before reporting
     * problems to the user ({@link #REPORT_DELAY}). Then it makes another problem snapshot and if
     * the set is stable, will report them to the user.
     * 
     * @param cancel 
     */
    void activate(Consumer<BrokenProjectReferencesCollector> cancel) {
        if (projectProvider == null) {
            cancel.accept(this);
            return;
        }
        synchronized (this) {
            this.unregister = cancel;
            if (weakL == null) {
                weakL = WeakListeners.propertyChange(this, projectProvider);
                projectProvider.addPropertyChangeListener(weakL);
            }
        }
        updateProblems(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        serial.incrementAndGet();
        synchronized (this) {
            if (state == State.COLLECT) {
                
            }
        }
    }
}
