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

package org.netbeans.modules.profiler.ppoints.ui;

import org.netbeans.modules.profiler.ppoints.GlobalProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPointWizard;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import java.awt.Dialog;
import javax.swing.SwingUtilities;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;


/**
 * Opens the Insert New Profiling Points Wizard.
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "InsertProfilingPointAction_ActionName=&Insert Profiling Point...",
    "InsertProfilingPointAction_ProfilingInProgressMsg=Cannot create new Profiling Point during profiling session.",
    "InsertProfilingPointAction_NoProjectMsg=Cannot create new Profiling Point because no project is open."
})
public class InsertProfilingPointAction extends NodeAction {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public InsertProfilingPointAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return new HelpCtx(InsertProfilingPointAction.class);
    }

    public String getName() {
        return Bundle.InsertProfilingPointAction_ActionName();
    }

    public void performAction(Lookup.Provider project) {
        ProfilingPointsManager manager = ProfilingPointsManager.getDefault();
        if (manager.isProfilingSessionInProgress()) {
            ProfilerDialogs.displayWarning(
                    Bundle.InsertProfilingPointAction_ProfilingInProgressMsg());

            return;
        }

        if (ProjectUtilities.getOpenedProjects().length == 0) {
            ProfilerDialogs.displayWarning(
                    Bundle.InsertProfilingPointAction_NoProjectMsg());

            return;
        }

        ProfilingPointWizard ppWizard = ProfilingPointWizard.getDefault();
        final WizardDescriptor wd = ppWizard.getWizardDescriptor(project);

        if (wd != null) { // if null then another PP is currently being created/customized and user is already notified

            final Dialog d = DialogDisplayer.getDefault().createDialog(wd);
            d.setVisible(true);

            boolean createPPoint = wd.getValue() == WizardDescriptor.FINISH_OPTION;
            ProfilingPoint profilingPoint = ppWizard.finish(!createPPoint); // Wizard must be finished even in cancelled to release its resources

            if (createPPoint) {
                manager.addProfilingPoint(profilingPoint);

                if (profilingPoint instanceof GlobalProfilingPoint) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ProfilingPointsWindow ppWin = ProfilingPointsWindow.getDefault();
                            if (!ppWin.isOpened()) {
                                ppWin.open();
                                ppWin.requestVisible();
                            }
                        }
                    });
                }
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] nodes) {
        //    return Utils.getOpenedProjects().length > 0;
        return true; // Let's have this action enabled and show a warning if no project is opened, otherwise the user might not understand why it's disabled
    }

    protected void performAction(Node[] nodes) {
        performAction((Lookup.Provider)null);
    }
}
