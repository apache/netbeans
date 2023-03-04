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

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.ProjectProblemsImplementation;

/**
 * Implements the ProjectProblem resolution process in a way more suitable for LSP. When a project Alert
 * or Customizer is requested, a ProjectAlertPresenter is created and will be live until the user processes
 * all the alerts, or the responses time out. With a new alert, the alerts will show up again.
 * Each Presenter works with a single Project.
 * <p>
 * When fatal (non-resolvable) problems occur, they are reported using ShowMessage requests. Once they are
 * confirmed, resolvable issues will be processed. There's a timeout {@link #WAKEUP_DELAY} for the user
 * inactivity after last user's response (message ok/dismiss): if no response is received, the process
 * continues.
 * <p>
 * If there are recoverable problems, the user is presented with the first one's description. If there are
 * more problems, a note "There are XX other fixable problems." is added.
 * <p>
 * The main loop supports two modes: autoresolve true will immediately attempt to resolve 1st resolvable
 * problem, while autoresolve false provides the description and asks for confirmation. This allows the
 * "OK" and "Rest" button to switch between the two modes:
 * <ul>
 * <li>resolve first, report issues if any
 * <li>ask with details about the fix first, then resolve
 * </ul>
 * When the user is asked to decide, a timeout task is scheduled: if the user does not respond within
 * the defined timeout, the process continues.
 * <p>
 * To avoid possible issues with multiple project actions, <b>all resolve calls from all ProblemResolvers</b>
 * are serialized to a single dedicated RP.
 * <p/>
 * A request to <b>alert</b> the project will just push the process further: if timeout is under way, it will 
 * terminate it and display the next question.
 * <p/>
 * A request to <b>display a customizer</b> will invalidate the current presenter, and display all the issues
 * anew. This allows for an explicit CodeAction or Command that will reiterate questions the user may have closed.
 * <p/>
 * Note: if a ShowMessage request is delivered to vscode client and the same ShowMessage request is pending, the client 
 * will cancel the former ShowMessage as if the user pressed ESC or dismissed the message. The new ShowMessage will be 
 * displayed
 * @author sdedic
 */
public class BrokenReferencesImpl implements ProjectProblemsImplementation, ProjectAlertPresenter.Env {
    private static final Logger LOG = Logger.getLogger(BrokenReferencesImpl.class.getName());
    
    // @GuardedBy(this)
    /**
     * Holds all alerted projects. Will be freed after the last project's Presenter will
     * be removed.
     */
    private BrokenReferencesModel.Context   context;
    
    /**
     * Active Presenters
     */
    // @GuadedBy(this)
    private final Map<Project, ProjectAlertPresenter> presenters = new WeakHashMap<>();
    
    @Override
    public CompletableFuture<Void> showAlert(@NonNull Project project) {
        ProjectAlertPresenter p;
        
        synchronized (this) {
            if (context == null) {
                context = new BrokenReferencesModel.Context();
                LOG.log(Level.FINEST, "Initializing new Context");
            }
            context.offer(project);
            
            p = presenters.computeIfAbsent(project, (p2) -> createPresenter(p2, new BrokenReferencesModel(context, true)));
        }
        p.cleanAndProcess(false);
        return p.getCompletion().thenApply(x -> null);
    }
    
    ProjectAlertPresenter createPresenter(Project project, BrokenReferencesModel model) {
        return new ProjectAlertPresenter(project, model, this);
    }

    @Override
    public CompletableFuture<Void> showCustomizer(@NonNull Project project) {
        return fixBrokenProject(project).thenApply(x -> null);
    }
    
    public CompletableFuture<Boolean> fixBrokenProject(@NonNull Project project) {
        ProjectAlertPresenter p;
        
        synchronized (this) {
            p = new ProjectAlertPresenter(project, new BrokenReferencesModel(context, true), this);
            presenters.put(project, p);
        }
        p.processProject(true);
        return p.getCompletion();
    }
    
    /**
     * Checks if the presenter is the active one for the project.
     * @param p presenter to check
     * @return true, if the presenter is active
     */
    @Override
    public boolean isActivePresenter(ProjectAlertPresenter p) {
        synchronized (this) {
            return p == presenters.get(p.getProject());
        }
    }
    
    /**
     * Finishes the work on a project. If the presenter is the active one, the method will
     * remove the presenter from the presenters queue. Removing the last presenter will also
     * release context, that is all project references.
     * 
     * @param presenter presenter to terminate
     * @param p the presenter's project
     */
    @Override
    public void finishProject(ProjectAlertPresenter presenter) {
        synchronized (this) {
            presenter.completion.complete(presenter.allProcessed);
            if (!presenters.remove(presenter.getProject(), presenter)) {
                return;
            }
            if (presenters.isEmpty()) {
                context = null;
            }
        }
    }
    
    /* tests only */
    ProjectAlertPresenter getPresenter(Project p) {
        synchronized (this) {
            return presenters.get(p);
        }
    }
}
