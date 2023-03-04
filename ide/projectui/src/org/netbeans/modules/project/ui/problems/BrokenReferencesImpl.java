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
package org.netbeans.modules.project.ui.problems;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;
import static org.netbeans.modules.project.ui.problems.Bundle.*;
import org.netbeans.spi.project.ui.ProjectProblemsImplementation;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=ProjectProblemsImplementation.class)
public class BrokenReferencesImpl implements ProjectProblemsImplementation {

    private static final boolean suppressBrokenRefAlert = Boolean.getBoolean("BrokenReferencesSupport.suppressBrokenRefAlert"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(BrokenReferencesImpl.class);
    private static int BROKEN_ALERT_TIMEOUT = 1000;

    // @GuardedBy(this)
    private BrokenReferencesModel.Context context;
    // @GuardedBy(this)
    private RequestProcessor.Task rpTask;
    // @GuardedBy(this)
    private CompletableFuture<Void> runningFuture;
    

    @Override
    @NbBundle.Messages({
        "CTL_Broken_References_Resolve=Resolve Problems...",
        "AD_Broken_References_Resolve=N/A",
        "CTL_Broken_References_Close=Close",
        "AD_Broken_References_Close=N/A",
        "MSG_Broken_References_Title=Open Project",
        "LBL_Broken_References_Resolve_Panel_Close=Close",
        "AD_Broken_References_Resolve_Panel_Close=N/A",
        "LBL_Broken_References_Resolve_Panel_Title=Resolve Project Problems"
    })
    public CompletableFuture<Void> showAlert(Project project) {
        CompletableFuture<Void> result;
        Parameters.notNull("project", project); //NOI18N

        if (!BrokenReferencesSettings.isShowAgainBrokenRefAlert() || suppressBrokenRefAlert) {
            return CompletableFuture.completedFuture(null);
        } 
        
        final BrokenReferencesModel.Context ctx;
        final RequestProcessor.Task t;
        
        synchronized (this) {
            if (context == null) {
                assert rpTask == null;

                final Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        final BrokenReferencesModel.Context ctx;
                        CompletableFuture<Void> res = null;
                        synchronized (BrokenReferencesImpl.this) {
                            rpTask = null;
                            ctx = context;
                            if (ctx == null) {
                                res = runningFuture;
                                runningFuture = null;
                            }
                        }
                        if (res != null) {
                            res.complete(null);
                            return;
                        }
                        try {
                            final JButton resolveOption = new JButton(CTL_Broken_References_Resolve());
                            resolveOption.getAccessibleContext().setAccessibleDescription(AD_Broken_References_Resolve());
                            JButton closeOption = new JButton (CTL_Broken_References_Close());
                            closeOption.getAccessibleContext().setAccessibleDescription(AD_Broken_References_Close());
                            DialogDescriptor dd = new DialogDescriptor(new BrokenReferencesAlertPanel(),
                                MSG_Broken_References_Title(),
                                true,
                                new Object[] {resolveOption, closeOption},
                                closeOption,
                                DialogDescriptor.DEFAULT_ALIGN,
                                null,
                                null);
                            dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                            ctx.addChangeListener(new ChangeListener() {
                                @Override
                                public void stateChanged(ChangeEvent e) {
                                    resolveOption.setVisible(!ctx.isEmpty());
                                }
                            });
                            resolveOption.setVisible(!ctx.isEmpty());
                            if (DialogDisplayer.getDefault().notify(dd) == resolveOption) {
                                final BrokenReferencesModel model = new BrokenReferencesModel(ctx, true);
                                if (GraphicsEnvironment.isHeadless()) {
                                    fixAllProblems(model, new HashSet<>());
                                    return;
                                }
                                final BrokenReferencesCustomizer customizer = new BrokenReferencesCustomizer(model);
                                JButton close = new JButton (Bundle.LBL_Broken_References_Resolve_Panel_Close());
                                close.getAccessibleContext ().setAccessibleDescription (Bundle.AD_Broken_References_Resolve_Panel_Close());
                                dd = new DialogDescriptor(customizer,
                                    Bundle.LBL_Broken_References_Resolve_Panel_Title(),
                                    true,
                                    new Object[] {closeOption},
                                    closeOption,
                                    DialogDescriptor.DEFAULT_ALIGN,
                                    null,
                                    null);
                                customizer.setNotificationLineSupport(dd.createNotificationLineSupport());
                                DialogDisplayer.getDefault().notify(dd);
                            }
                        } finally {
                            synchronized (BrokenReferencesImpl.this) {
                                //Clean seen references and start from empty list
                                context = null;
                                res = runningFuture;
                                runningFuture = null;
                            }
                            res.complete(null);
                        }
                    }
                };
                
                context = new BrokenReferencesModel.Context();
                rpTask = RP.create(new Runnable() {
                    @Override
                    public void run() {
                        if (GraphicsEnvironment.isHeadless()) {
                            task.run();
                        } else {
                            WindowManager.getDefault().invokeWhenUIReady(task);
                        }
                    }
                });
                this.runningFuture = new CompletableFuture<>();
                return runningFuture;
            }
            
            ctx = this.context;
            result = this.runningFuture;
            t = this.rpTask;
        }

        assert ctx != null;
        assert result != null;
        if (project != null) {
            ctx.offer(project);
        }
        if (t != null) {
            //Not yet shown, move
            t.schedule(BROKEN_ALERT_TIMEOUT);
        }
        return result;
    }
    
    @NbBundle.Messages({
        "# {0} - problem display name",
        "ERROR_ProblemResolutionFailed=Resolution failed: {0}"
    })
    private void fixAllProblems(BrokenReferencesModel model, Collection<BrokenReferencesModel.ProblemReference> seen) {
        model.refresh();
        for (int i = 0; i < model.getSize(); i++) {
            Object value = model.getElementAt(i);
            if (!(value instanceof BrokenReferencesModel.ProblemReference)) {
                return;
            }
            final BrokenReferencesModel.ProblemReference or = (BrokenReferencesModel.ProblemReference) value;
            if (or.resolved || seen.contains(or)) {
                continue;
            }
            BrokenReferencesCustomizer.performProblemFix(or, (result) -> {
                seen.add(or);
                final String msg = result.getMessage();
                if (msg != null) {
                    int importance = result.isResolved() ? 0 : StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT;
                    StatusDisplayer.getDefault().setStatusText(msg, importance);
                } else if (!result.isResolved()) {
                    // note that resolution has failed:
                    StatusDisplayer.getDefault().setStatusText(
                            Bundle.ERROR_ProblemResolutionFailed(or.getDisplayName()), 
                            StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                }
                // next round:
                fixAllProblems(model, seen);
            });
            break;
        }
    }


    @NbBundle.Messages({
        "LBL_BrokenLinksCustomizer_Close=Close",
        "ACSD_BrokenLinksCustomizer_Close=N/A",
        "LBL_BrokenLinksCustomizer_Title=Resolve Project Problems - \"{0}\" Project"
    })
    @Override
    public CompletableFuture<Void> showCustomizer(@NonNull Project project) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Parameters.notNull("project", project); //NOI18N
        BrokenReferencesModel model = new BrokenReferencesModel(project);
        BrokenReferencesCustomizer customizer = new BrokenReferencesCustomizer(model);
        JButton close = new JButton (LBL_BrokenLinksCustomizer_Close()); // NOI18N
        close.getAccessibleContext ().setAccessibleDescription (ACSD_BrokenLinksCustomizer_Close()); // NOI18N
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        DialogDescriptor dd = new DialogDescriptor(customizer,
            LBL_BrokenLinksCustomizer_Title(projectDisplayName), // NOI18N
            true, new Object[] {close}, close, DialogDescriptor.DEFAULT_ALIGN, null, null);
        customizer.setNotificationLineSupport(dd.createNotificationLineSupport());
        Runnable r = new Runnable() {
            public void run() {
                Dialog dlg = null;
                try {
                    dlg = DialogDisplayer.getDefault().createDialog(dd);
                    if (SwingUtilities.isEventDispatchThread()) {
                        dlg.setVisible(true);
                    }
                } catch (RuntimeException ex) {
                    result.completeExceptionally(ex);
                } finally {
                    if (dlg != null) {
                        dlg.dispose();
                    }
                    result.complete(null);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
        return result;
    }

}
