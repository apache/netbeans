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
package org.netbeans.modules.java.lsp.server.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages(value = {
    "# {0} - project name", 
    "# {1} - problem's display name", 
    "ProjectProblem_Title=Project {0}: {1}", 
    
    "# {0} - project name", 
    "ProjectProblems_Resolved_Info=Project {0} issues resolved", 
    
    "# {0} - project name", 
    "ProjectProblems_Resolved_Warning=Project {0} issues resolved", 
    
    "# {0} - project name", 
    "ProjectProblems_Resolved_Error=Project {0} issues not fixed", 
    
    "# {0} - problem display", 
    "# {1} - message", 
    "ProjectProblems_Resolved_ErrorMessage1=The fix for \"{0}\" failed: {1}", 
    
    "# {0} - problem display", 
    "# {1} - message", 
    "ProjectProblems_Resolved_WarningMessage1=Problem \"{0}\" was resolved: {1}", 
    
    "# {0} - problem display", 
    "# {1} - message", 
    "# {2} - other errors note", 
    "ProjectProblems_Resolved_ErrorMessage2=The fix for \"{0}\" failed: {1}. {2}", 
    
    "# {0} - problem display", 
    "# {1} - message", 
    "ProjectProblems_Resolved_WarningMessage2=Problem \"{0}\" was resolved: {1}", 
    
    "# {0} - number of fixes", 
    "# {1} - the main message", 
    "ProjectProblems_Additional={1}. Additional fixes ({0}) are available."})
class ProjectAlertPresenter {
    private static final Logger LOG = Logger.getLogger(ProjectAlertPresenter.class.getName());
    
    static int MAX_PRESENTED_ERRORS = Integer.getInteger(ProjectAlertPresenter.class.getName() + ".maxErrors", 10);
    static int ERRORS_WAKEUP_DELAY = Integer.getInteger(ProjectAlertPresenter.class.getName() + ".errorsWakeUp", 2 * 60 * 1000);
    static int QUESTION_WAKEUP_DELAY = Integer.getInteger(ProjectAlertPresenter.class.getName() + ".questionWakeUp", 5 * 60 * 1000);
    
    /**
     * All resolutions are executed in this thread, to prevent interactions between project actions.
     */
    private static final RequestProcessor  RESOLVE_RP = new RequestProcessor(BrokenReferencesImpl.class.getName());
    
    /**
     * Mainly a 'watcher' thread that continues the process if the user does not react on prompts.
     */
    private static final RequestProcessor  RP = new RequestProcessor(BrokenReferencesImpl.class.getName());
    
    private static final int WAKEUP_DELAY = 2 * 60 * 1000;
    

    /**
     * The project.
     */
    private final Project project;
    
    /**
     * Project name.
     */
    private final String projectName;
    
    /**
     * Updating model of the project's problems.
     */
    private final BrokenReferencesModel model;
    
    /**
     * The controller.
     */
    private final Env controller;
    
    /**
     * Problems that were already seen and presented to the user.
     */
    private final Set<ProjectProblemsProvider.ProjectProblem> seen = Collections.synchronizedSet(new HashSet<>());
    
    final Object restOption = Bundle.ProjectProblems_RestOption();
    final Object detailOption = Bundle.ProjectProblems_DetailsOption();

    /**
     * Overall result. The future will complete after all errors were seen, all questions were answered or timed out.
     */
    final CompletableFuture<Boolean> completion;
    
    /**
     * All errors already acknowledged by the process, but not necessarily presented
     */
    // @GuardedBy(this)
    Set<BrokenReferencesModel.ProblemReference> snapshot = Collections.emptySet();
    
    /**
     * Invocation attempt, i.e. after a timeout or new project alert. The process continues after user response only
     * if another run attempt has not started yet.
     */
    // @GuardedBy(this)
    int runAttempt;

    /**
     * If true, will re-display project problems that have been already presented to the client.
     */
    boolean includeSeen;

    /**
     * Timer task that will push the process further if the user ignores the prompts. The task is run by {@link #RP} processor.
     */
    // @GuardedBy(this)
    private RequestProcessor.Task wakeUpTask;
    
    /**
     * The resolution process is cancelled. The process will terminate after the first timeout or user input.
     */
    // @GuardedBy(this)
    private volatile CancellationException cancelled;
    
    /**
     * Indicates that all questions were answered. Will eventually reset to {@code false} during the process, if the user
     * cancels further fixes.
     */
    // @GuardedBy(this)
    boolean allProcessed = true;
    
    public ProjectAlertPresenter(Project project, BrokenReferencesModel model, final Env master) {
        this.controller = master;
        this.project = project;
        this.model = model;
        ProjectInformation pi = ProjectUtils.getInformation(project);
        String pn = pi.getDisplayName();
        if (pn == null) {
            pn = pi.getName();
        }
        this.projectName = pn;
        LOG.log(Level.FINE, "Initializing alert presenter for {0}", projectName);
        completion = new CompletableFuture<Boolean>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean r = super.cancel(mayInterruptIfRunning);
                if (r) {
                    ProjectAlertPresenter.this.cancel();
                }
                return r;
            }
        };
        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                awake();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                awake();
            }
        });
    }

    public Project getProject() {
        return project;
    }

    public CompletableFuture<Boolean> getCompletion() {
        return completion;
    }

    public void cancel() {
        synchronized (this) {
            cancelled = new CancellationException();
            if (wakeUpTask != null) {
                wakeUpTask.cancel();
            }
        }
    }
    
    /**
     * Resumes the process after user timeout.
     * @param autoResolve
     */ 
    void resumeAfterTimeout(Ctx ctx) {
        assert RP.isRequestProcessorThread();
        LOG.log(Level.FINE, "User unresponsive for project {0}, continue", projectName);
        ErrorQueue oq;
        synchronized (this) {
            if (!ctx.valid()) {
                return;
            }
            if (wakeUpTask != null && !wakeUpTask.isFinished()) {
                wakeUpTask.cancel();
                wakeUpTask = null;
            }
            oq = errorQueue;
            errorQueue = null;
            runAttempt++;
            ctx = new Ctx(ctx.autoResolve);
        }
        if (oq != null) {
            oq.terminate(true);
        }
        processOneRound(ctx);
    }
    
    private void awake() {
        processProject(false);
    }
    
    Set<ProjectProblemsProvider.ProjectProblem> seenProblems() {
        return new LinkedHashSet<>(this.seen);
    }
    
    public void cleanAndProcess(boolean autoResolve) {
        synchronized (this) {
            if (wakeUpTask != null) {
                wakeUpTask.cancel();
            }
            wakeUpTask = null;
            snapshot = Collections.emptySet();
        }
        processProject(autoResolve);
    }
    
    /**
     * Starts or restarts processing. If {@code autoResolve} is {@code true}, 
     * @param autoResolve 
     */
    public void processProject(boolean autoResolve) {
        model.refresh(false);
        Ctx ctx;
        synchronized (this) {
            List<BrokenReferencesModel.ProblemReference> probs = new ArrayList<>();
            List<BrokenReferencesModel.ProblemReference> fatals = findProblems(probs);
            LOG.log(Level.FINE, "Processing {0} fatals, {1} resolvables of {2}", new Object[]{fatals.size(), probs.size(), projectName});
            if (snapshot.containsAll(probs) && snapshot.containsAll(fatals) && wakeUpTask != null) {
                // activate only if something new happens, there's timeout task that will eventually
                // continue if user is unresponsive.
                LOG.log(Level.FINE, "All already seen or being processed, wakeup task available");
                return;
            }
            if (seen.containsAll(fatals) && wakeUpTask != null) {
                // all fatals reported, but user did not dismissed - a new fix will be handled by the
                // running process.
                LOG.log(Level.FINE, "Fatals reported, wakeup task available, wait for user or timeout");
                return;
            }
            if (wakeUpTask != null && !wakeUpTask.isFinished()) {
                if (!wakeUpTask.cancel()) {
                    // will invoke process anyway
                    return;
                }
            }
            if (cancelled != null) {
                finishThis();
                return;
            }
            runAttempt++;
            ctx = new Ctx(autoResolve);
            LOG.log(Level.FINE, "Wakeup cancelled, new presenter run");
        }
        processOneRound(ctx);
    }

    private List<BrokenReferencesModel.ProblemReference> findProblems(List<BrokenReferencesModel.ProblemReference> probs) {
        List<BrokenReferencesModel.ProblemReference> fatals = new ArrayList<>();
        for (BrokenReferencesModel.ProblemReference ref : model.projectProblems(project, includeSeen)) {
            if (seen.contains(ref.problem) || ref.resolved) {
                continue;
            }
            if (ref.seen && !includeSeen) {
                continue;
            }
            ProjectProblemsProvider.ProjectProblem pp = ref.problem;
            if (!pp.isResolvable()) {
                fatals.add(ref);
            } else {
                probs.add(ref);
            }
        }
        return fatals;
    }
    
    void finishThis() {
        controller.finishProject(this);
        if (cancelled != null) {
            if (completion.completeExceptionally(cancelled)) {
                return;
            }
        } 
        completion.complete(allProcessed);
    }
    
    final class Ctx {
        final int attempt;
        final boolean autoResolve;

        public Ctx(boolean autoResolve) {
            this(runAttempt, autoResolve);
        }
        
        public Ctx(int attempt, boolean autoResolve) {
            this.attempt = attempt;
            this.autoResolve = autoResolve;
        }
        
        boolean valid() {
            synchronized (ProjectAlertPresenter.this) {
                return this.attempt == runAttempt && controller.isActivePresenter(ProjectAlertPresenter.this);
            }
        }
        
        Ctx autoresolve(boolean r) {
            return new Ctx(attempt, r);
        }
    }

    /**
     * Hnadles display of errors limited to some number. The value is cleared when all
     * the errors were presented + confirmed.
     */
    // @GuardedBy(this)
    ErrorQueue errorQueue = null;
    
    /**
     * Displays fatal errors, at most {@link #MAX_PRESENTED_ERRORS} at a time. When an error message is closed,
     * displays a new one, if it is in the queue. Completes the {@link #allDone} Future after all messages
     * have been confirmed or were cancelled.
     */
    class ErrorQueue {
        final Ctx ctx;
        
        /**
         * All messages to be processed.
         */
        final List<BrokenReferencesModel.ProblemReference> toProcess = new ArrayList<>();
        
        /**
         * Messages already processed.
         */
        final Set<BrokenReferencesModel.ProblemReference> processed = new HashSet<>();
        
        /**
         * Future which will be completed after all the messages are shown or the process is cancelled.
         */
        CompletableFuture allDone = new CompletableFuture();
        
        /**
         * Becomes true once the process displays the 1st dialog.
         */
        boolean started;
        
        /**
         * The number of messages currently displayed, should be at most {@link #MAX_PRESENTED_ERRORS}.
         */
        int fatalErrorsOnScreen;
        
        /**
         * Non-null if some of the messages complete with an exception.
         */
        Throwable error;


        public ErrorQueue(Ctx ctx) {
            this.ctx = ctx;
        }
        
        public void runOrDelay(BrokenReferencesModel.ProblemReference p) {
            synchronized (this) {
                if (fatalErrorsOnScreen >= MAX_PRESENTED_ERRORS) {
                    // delay:
                    return;
                }
                fatalErrorsOnScreen++;
            } 
            displayError(p);
        }
        
        CompletableFuture moreErrors(List<BrokenReferencesModel.ProblemReference> newErrors) {
            synchronized (this) {
                if (started && processed.containsAll(toProcess)) {
                    return null;
                }
                started = true;
                toProcess.addAll(newErrors);
            }
            for (BrokenReferencesModel.ProblemReference r : newErrors) {
                runOrDelay(r);
            }
            return allDone;
        }
        
        void terminate(boolean all) {
            Throwable exception;
            
            synchronized (this) {
                for (BrokenReferencesModel.ProblemReference ref : toProcess) {
                    if (!all && seen.contains(ref.problem)) {
                        continue;
                    }
                    processed.add(ref);
                }
                if (!processed.containsAll(toProcess)) {
                    return;
                }
                synchronized (ProjectAlertPresenter.this) {
                    if (this == errorQueue) {
                        errorQueue = null;
                    }
                }
                exception = error;
            }
            // complete the future outside the lock.
            if (exception != null) {
                allDone.completeExceptionally(exception);
            } else {
                allDone.complete(null);
            }
        }
        
        public void displayError(BrokenReferencesModel.ProblemReference toPresent) {
            ProjectProblemsProvider.ProjectProblem p = toPresent.problem;
            seen.add(p);
            LOG.log(Level.FINE, "Reporting fatal {0}", p.getDisplayName());
            int type;
            switch (p.getSeverity()) {
                default:
                case ERROR:
                    type = NotifyDescriptor.ERROR_MESSAGE;
                    break;
                case WARNING:
                    type = NotifyDescriptor.WARNING_MESSAGE;
                    break;
            }
            // hack: the LSP protocol does not support title. Until fixed, or implemented through a custom message,
            // embed the title into description:
            String title = Bundle.ProjectProblem_Title(projectName, p.getDisplayName());
            NotifyDescriptor msg = new NotifyDescriptor(title + ": " + Utils.html2plain(p.getDescription()), title, NotifyDescriptor.DEFAULT_OPTION, type, new Object[]{NotifyDescriptor.OK_OPTION}, null);

            // Note: the number of 'fatal' dialogs displayed at the same time is limited by the RP throughput. Dialog API does not support CompletableFuture<> interface
            // so threads may dangle.
            CompletableFuture<Integer> running = DialogDisplayer.getDefault().notifyFuture(msg).handle((n, e) -> {
                Optional<BrokenReferencesModel.ProblemReference> o;
                int result = 0;
                synchronized (this) {
                    processed.add(toPresent);
                    if (!ctx.valid() || cancelled != null) {
                        // will complete the 'running' future.
                        return 2;
                    }
                    o = toProcess.stream().filter((a) -> 
                        !seen.contains(a.problem)).findFirst();
                    // o.isPresent(), will replace the current error with a new one, no change to the counter.
                    if (!o.isPresent()) {
                        result = 1;
                        if (fatalErrorsOnScreen > 0) {
                            fatalErrorsOnScreen--;
                        }
                    }
                }
                if (o.isPresent()) {
                    // just run outside the synchronized block - display another error.
                    displayError(o.get());
                }
                return result;
            });
            // chaining instead of try - finally ;)
            running.whenComplete((t, ex) -> {
                if (t > 0) {
                    terminate(t > 1);
                } else {
                    synchronized (ProjectAlertPresenter.this) {
                        // attempt to postpone the task after user's reaction.
                        if (wakeUpTask != null) {
                            LOG.log(Level.FINER, "Trying to postpone wakeup for {0}ms", WAKEUP_DELAY);
                            wakeUpTask.schedule(ERRORS_WAKEUP_DELAY);
                        }
                    }
                }
            });
        }
    }

    @NbBundle.Messages(value = {"# {0} - project name", "# {1} - issue title", "ProjectProblems_Fixable_Title=Project {0}: {1}"})
    void processOneRound(Ctx ctx) {
        if (!RP.isRequestProcessorThread()) {
            RP.post(() -> processOneRound(ctx));
            return;
        }
        
        assert RP.isRequestProcessorThread();

        model.refresh(false);
        List<BrokenReferencesModel.ProblemReference> probs = new ArrayList<>();
        List<BrokenReferencesModel.ProblemReference> fatals = findProblems(probs);
        synchronized (this) {
            if (!ctx.valid()) {
                return;
            }
            wakeUpTask = null;
            snapshot = new HashSet<>();
            snapshot.addAll(probs);
            snapshot.addAll(fatals);
        }
        if (probs.isEmpty() && fatals.isEmpty()) {
            LOG.log(Level.FINE, "Project {0} clear, finishing", projectName);
            finishThis();
            return;
        }
        if (cancelled != null) {
            finishThis();
            return;
        }

        if (!fatals.isEmpty()) {
            ErrorQueue activeBatch;
            CompletableFuture f = null;
            boolean newBatch;
            
            // loop in case the batch just terminates & clears the reference.
            do {
                newBatch = false;
                synchronized (this) {
                    activeBatch = errorQueue;
                    if (activeBatch == null) {
                        errorQueue = activeBatch = new ErrorQueue(ctx);
                        newBatch = true;
                    }
                }
                f = activeBatch.moreErrors(fatals);
                
                synchronized (this) {
                    if (f == null && errorQueue == activeBatch) {
                        errorQueue = null;
                    }
                }
            } while (f == null);
            
            if (newBatch) {
                synchronized (this) {
                    LOG.log(Level.FINE, "Waiting for {0} items, scheduling wakeup task", fatals.size());
                    wakeUpTask = RP.create(() -> resumeAfterTimeout(ctx));
                    wakeUpTask.schedule(ERRORS_WAKEUP_DELAY);
                }
            }
            f.thenAccept(r -> continueResetPending(ctx));
            return;
        }
        
        if (probs.isEmpty()) {
            finishThis();
            return;
        }
        BrokenReferencesModel.ProblemReference ref = probs.iterator().next();
        
        BiFunction<ProjectProblemsProvider.Result, Throwable, Void> handlerFn = (ProjectProblemsProvider.Result r, Throwable e)  -> {
            if (!ctx.valid()) {
                return null;
            }
            if (e instanceof CompletionException) {
                e = e.getCause();
            }
            if (e instanceof CancellationException) {
                // do not proceeed further
                finishIfNoMoreErrors(ctx, probs);
                return null;
            }
            if (r != null) {
                processOneRound(ctx.autoresolve(true));
            } else {
                processOneRound(ctx.autoresolve(false));
            }
            return null;
        };
        if (ctx.autoResolve) {
            postResolveProblem(ctx, ref).handle(handlerFn);
        } else {
            seen.add(ref.problem);
            // present a question
            
            if (cancelled != null) {
                finishThis();
                return;
            }

            RequestProcessor.Task t;
            synchronized (this) {
                if (wakeUpTask == null) {
                    t = wakeUpTask = RP.post(() -> resumeAfterTimeout(ctx),  QUESTION_WAKEUP_DELAY);
                } else {
                    t = null;
                }
            }
            
            String title = Bundle.ProjectProblems_Fixable_Title(projectName, ref.problem.getDisplayName());
            String msg = Utils.html2plain(ref.problem.getDescription());
            if (probs.size() > 1) {
                msg = Bundle.ProjectProblems_Additional(probs.size() - 1, msg);
            }
            Object[] options = probs.size() > 1 ? new Object[]{NotifyDescriptor.OK_OPTION, restOption, NotifyDescriptor.CANCEL_OPTION} : new Object[]{NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION};
            NotifyDescriptor desc = new NotifyDescriptor(title + ": " + msg, title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.QUESTION_MESSAGE, options, null);
            // FIXME - Bad bad - this should be done in a separate RP as it may block in the client.
            DialogDisplayer.getDefault().notifyFuture(desc).whenComplete((d, ex) -> {
                synchronized (this) {
                    if (t != null && wakeUpTask == t) {
                        wakeUpTask.cancel();
                        wakeUpTask = null;
                    }
                }
                if (cancelled != null) {
                    finishThis();
                    return;
                }
                Object res = d.getValue();
                if (restOption.equals(res)) {
                    synchronized (this) {
                        if (!ctx.valid()) {
                            // resolve just that one issue, the rest will be solved by other questions.
                            postResolveProblem(ctx.autoresolve(false), ref).handle(handlerFn);
                            return;
                        } else {
                            seen.remove(ref.problem);
                        }
                    }
                    processOneRound(ctx.autoresolve(true));
                } else if (res != NotifyDescriptor.OK_OPTION) {
                    handlerFn.apply(null, null);
                } else {
                    postResolveProblem(ctx.autoresolve(false), ref).handle(handlerFn);
                }
            }).exceptionally(x -> {
                if (x instanceof CompletionException) {
                    x = x.getCause();
                }
                if (x instanceof CancellationException) {
                    finishIfNoMoreErrors(ctx, probs);
                }
                return null;
            });
        }
    }

    /**
     * Finishes the process, if no more errors is reported in the meantime. Will dismiss all questions
     * collected so far.
     * @param dismiss questions to dismiss.
     */
    private void finishIfNoMoreErrors(Ctx ctx, Collection<BrokenReferencesModel.ProblemReference> dismiss) {
        synchronized (this) {
            allProcessed &= dismiss.isEmpty();
            seen.addAll(dismiss.stream().map(r -> r.problem).collect(Collectors.toList()));
        }
        List<BrokenReferencesModel.ProblemReference> probs = new ArrayList<>();
        List<BrokenReferencesModel.ProblemReference> fatals = findProblems(probs);
        if (!fatals.isEmpty()) {
            processOneRound(ctx);
        } else {
            finishThis();
        }
    }

    /**
     * Schedules problem resolution into the common RequestProcessor. The returned CompletableFuture
     * will complete after the fix is done and potentially after the user answers question after fix reports
     * some warning or error. {@code non-null} is returned to indicate that no further questions are to be asked.
     * If the user cancels the dialog, [@link CancellationException} will be recorded in the Future.
     * @param ref problem to resolve
     * @return future with null/non-null result or cancellation
     */
    @NbBundle.Messages(value = {"ProjectProblems_RestOption=&Fix All", "ProjectProblems_DetailsOption=Display &Details"})
    private CompletableFuture<ProjectProblemsProvider.Result> postResolveProblem(Ctx ctx, BrokenReferencesModel.ProblemReference ref) {
        CompletableFuture<ProjectProblemsProvider.Result> f = // convert the exception to an error message to the user.
        // do not bother
        CompletableFuture.supplyAsync(() -> {
            try {
                seen.add(ref.problem);
                return ref.problem.resolve().get();
            } catch (ExecutionException ex) {
                // convert the exception to an error message to the user.
                return ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED, ex.getCause().getLocalizedMessage());
            } catch (InterruptedException ex) {
                // do not bother
                return null;
            }
        }, RESOLVE_RP).thenApply(r -> {
            if (r.isResolved()) {
                if (r.getMessage() != null) {
                    StatusDisplayer.getDefault().setStatusText(Utils.html2plain(r.getMessage()));
                }
                return ctx.autoResolve ? r : null;
            }
            List<BrokenReferencesModel.ProblemReference> probs = new ArrayList<>();
            List<BrokenReferencesModel.ProblemReference> fatals = findProblems(probs);
            NotifyDescriptor desc = createNotifyDescriptor(ref.problem, r, probs.size());
            if (fatals.isEmpty() && !probs.isEmpty()) {
                desc.setOptions(new Object[]{restOption, detailOption, NotifyDescriptor.CANCEL_OPTION});
                desc.setValue(detailOption);
            }
            Object v = DialogDisplayer.getDefault().notify(desc);
            if (cancelled != null) {
                throw new CancellationException();            
            }
            if (v == NotifyDescriptor.CANCEL_OPTION || v == null) {
                throw new CancellationException();
            } else if (restOption.equals(v)) {
                return r;
            } else {
                return null;
            }
        });
        return f;
    }
    
    private NotifyDescriptor createNotifyDescriptor(ProjectProblemsProvider.ProjectProblem pp, ProjectProblemsProvider.Result r, int probs) {
        String title;
        String msg;
        int type;
        String plainMessage = Utils.html2plain(r.getMessage());
        if (r.getStatus() == ProjectProblemsProvider.Status.UNRESOLVED) {
            title = Bundle.ProjectProblems_Resolved_Error(projectName);
            msg = Bundle.ProjectProblems_Resolved_ErrorMessage1(pp.getDisplayName(), plainMessage);
            type = NotifyDescriptor.ERROR_MESSAGE;
        } else {
            title = Bundle.ProjectProblems_Resolved_Warning(projectName);
            msg = Bundle.ProjectProblems_Resolved_WarningMessage1(pp.getDisplayName(), plainMessage);
            type = NotifyDescriptor.WARNING_MESSAGE;
        }
        if (probs > 0) {
            msg = Bundle.ProjectProblems_Additional(probs, msg);
        }
        // hack: the LSP protocol does not support title. Until fixed, or implemented through a custom message,
        // embed the title into description:
        msg = title + ": " + msg; // NOI18N
        NotifyDescriptor desc = new NotifyDescriptor(msg, title, NotifyDescriptor.DEFAULT_OPTION, type, new Object[]{NotifyDescriptor.OK_OPTION}, null);
        return desc;
    }

    private void continueResetPending(Ctx ctx) {
        LOG.log(Level.FINE, "All dialogs confirmed");
        synchronized (this) {
            if (!ctx.valid()) {
                return;
            }
            if (wakeUpTask != null && !wakeUpTask.isFinished() && !wakeUpTask.cancel()) {
                return;
            }
        }
        processOneRound(ctx);
    }
    
    interface Env {
        public void finishProject(ProjectAlertPresenter p);
        public boolean isActivePresenter(ProjectAlertPresenter p);
    }
}
