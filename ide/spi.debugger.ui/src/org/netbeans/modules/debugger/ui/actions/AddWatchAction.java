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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Dialog;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.debugger.ui.WatchPanel;
import org.netbeans.modules.debugger.ui.views.VariablesViewButtons;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * DebuggerManager Window action.
 *
 * @author   Jan Jancura
 */
public class AddWatchAction extends CallableSystemAction {

    private static String watchHistory = ""; // NOI18N

    
    public AddWatchAction () {
        // The action is not in the toolbar by default, so it should not have the
        // icon in the menu.
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    protected boolean asynchronous () {
        return false;
    }

    @Override
    public String getName () {
        return NbBundle.getMessage (
            AddWatchAction.class,
            "CTL_New_Watch"
        );
    }
    
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx (AddWatchAction.class);

    }

    /** The action's icon location.
    * @return the action's icon location
    */
    @Override
    protected String iconResource () {
        return "org/netbeans/modules/debugger/resources/actions/NewWatch.gif"; // NOI18N
    }
    
    @Override
    public void performAction () {
        final AddWatchListener addWatchListener = performEngineAddWatchAction();
        if (addWatchListener != null) {
            if (addWatchListener.getTask().isFinished()) {
                if (addWatchListener.isWatchAdded()) {
                    openWatchesView();
                }
                DebuggerManager.getDebuggerManager().removeDebuggerListener(addWatchListener);
            } else {
                RequestProcessor.getDefault().post(new Runnable() {
                    public @Override void run() {
                        addWatchListener.getTask().waitFinished();
                        if (addWatchListener.isWatchAdded()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public @Override void run() {
                                    openWatchesView();
                                }
                            });
                        }
                        DebuggerManager.getDebuggerManager().removeDebuggerListener(addWatchListener);
                    }
                });
            }
        } else {
            ResourceBundle bundle = NbBundle.getBundle (AddWatchAction.class);

            WatchPanel wp = new WatchPanel (watchHistory);
            JComponent panel = wp.getPanel ();

            // <RAVE>
            // Add help ID for 'Add Watch' dialog
            // org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (
            //      panel,
            //      bundle.getString ("CTL_WatchDialog_Title") // NOI18N
            // );
            // ====
            org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (
                panel,
                bundle.getString ("CTL_WatchDialog_Title"), // NOI18N
                true,
                org.openide.DialogDescriptor.OK_CANCEL_OPTION,
                null,
                org.openide.DialogDescriptor.DEFAULT_ALIGN,
                new org.openide.util.HelpCtx("debug.add.watch"),
                null
            );
            // </RAVE>
            Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
            dialog.setVisible (true);
            dialog.dispose ();

            if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
            String watch = wp.getExpression ();
            if ( (watch == null) ||
                 (watch.trim ().length () == 0)
            )   return;

            String s = watch;
            int i = s.indexOf (';');
            while (i > 0) {
                String ss = s.substring (0, i).trim ();
                if (ss.length () > 0)
                    DebuggerManager.getDebuggerManager ().createWatch (ss);
                s = s.substring (i + 1);
                i = s.indexOf (';');
            }
            s = s.trim ();
            if (s.length () > 0)
                DebuggerManager.getDebuggerManager ().createWatch (s);

            watchHistory = watch;

            openWatchesView();
        }
    }

    private static void openWatchesView() {
        // open watches view
        TopComponent watchesView = WindowManager.getDefault().findTopComponent("watchesView"); // NOI18N
        if (watchesView != null && watchesView.isOpened()) {
            Mode mw = WindowManager.getDefault().findMode(watchesView);
            if (mw != null && mw.getSelectedTopComponent() == watchesView) {
                return ; // Watches is already selected
            }
        }
        String viewName = VariablesViewButtons.isWatchesViewNested() ? "localsView" : "watchesView";
        ViewActions.openComponent (viewName, false).requestVisible();
    }

    private static AddWatchListener performEngineAddWatchAction() {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            ActionsManager manager = engine.getActionsManager();
            if (manager.isEnabled(ActionsManager.ACTION_NEW_WATCH)) {
                AddWatchListener addWatchListener = new AddWatchListener();
                DebuggerManager.getDebuggerManager().addDebuggerListener(addWatchListener);
                Task addWatchTask = manager.postAction(ActionsManager.ACTION_NEW_WATCH);
                addWatchListener.setTask(addWatchTask);
                return addWatchListener;
            }
        }
        return null;
    }

    private static final class AddWatchListener extends DebuggerManagerAdapter {

        private boolean watchAdded;
        private Task addWatchTask;

        @Override
        public void watchAdded(Watch watch) {
            watchAdded = true;
        }

        public boolean isWatchAdded() {
            return watchAdded;
        }

        public void setTask(Task addWatchTask) {
            this.addWatchTask = addWatchTask;
        }

        public Task getTask() {
            return addWatchTask;
        }
        
    }
}
