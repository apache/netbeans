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

package org.netbeans.modules.gradle.execute;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.io.File;
import java.util.ArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import static org.netbeans.modules.gradle.api.execute.RunConfig.ExecFlag.REPEATABLE;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class AbstractGradleExecutor extends OutputTabMaintainer<AbstractGradleExecutor.TabContext> implements GradleExecutor {

    public static final class TabContext {

        ReRunAction rerun;
        ReRunAction rerunDebug;
        StopAction stop;
        OptionsAction options;

        protected TabContext copy() {
            TabContext c = new TabContext();
            c.rerun = rerun;
            c.rerunDebug = rerunDebug;
            c.stop = stop;
            c.options = options;
            return c;
        }
    }

    private TabContext tabContext = new TabContext();
    protected ExecutorTask task;
    protected GradleItem item;
    protected RunConfig config;
    protected final Object taskSemaphore = new Object();

    @Override
    protected Class<TabContext> tabContextType() {
        return TabContext.class;
    }

    @Override
    protected final TabContext createContext() {
        return tabContext.copy();
    }

    @Override
    protected Action[] createNewTabActions() {
        ArrayList<Action> ret = new ArrayList<>(4);
        if (config.getExecFlags().contains(RunConfig.ExecFlag.REPEATABLE)) {
            tabContext.rerun = new ReRunAction(false);
            tabContext.rerun.setConfig(config);
            ret.add(tabContext.rerun);
            tabContext.rerunDebug = new ReRunAction(true);
            tabContext.rerunDebug.setConfig(config);
            ret.add(tabContext.rerunDebug);
        }

        tabContext.stop = new StopAction();
        tabContext.stop.setExecutor(this);
        ret.add(tabContext.stop);
        tabContext.options = new OptionsAction();
        ret.add(tabContext.options);
        return ret.toArray(new Action[0]);
    }

    @Override
    protected void reassignAdditionalContext(TabContext tabContext) {
        this.tabContext = tabContext;
        if (config.getExecFlags().contains(REPEATABLE)){
            tabContext.rerun.setConfig(config);
            tabContext.rerunDebug.setConfig(config);
        }
        tabContext.stop.setExecutor(this);
    }

    public AbstractGradleExecutor(RunConfig config) {
        super(config.getTaskDisplayName());
        this.config = config;
    }

    @Override
    public void setTask(ExecutorTask task) {
        synchronized (taskSemaphore) {
            this.task = task;
            this.item = new GradleItem();
            taskSemaphore.notifyAll();
        }
    }

    protected final void actionStatesAtStart() {
        invokeUILater(new Runnable() {
            @Override
            public void run() {
                disableAction(tabContext.rerun);
                disableAction(tabContext.rerunDebug);
                enableAction(tabContext.stop);
            }
        });
    }

    protected final void actionStatesAtFinish() {
        invokeUILater(new Runnable() {
            @Override
            public void run() {
                enableAction(tabContext.rerun);
                enableAction(tabContext.rerunDebug);
                disableAction(tabContext.stop);
            }
        });
    }

    protected final void checkForExternalModifications() {
        Project project = config.getProject();
        if (project != null) {
            project.getProjectDirectory().refresh();
            GradleBaseProject bp = GradleBaseProject.get(config.getProject());
            File buildDir = bp.getBuildDir();
            if (buildDir != null) {
                FileUtil.refreshFor(buildDir);
            }
        }
    }

    static class ReRunAction extends AbstractAction {

        private RunConfig config;
        private final boolean debug;

        @NbBundle.Messages({
            "TXT_Rerun_extra=Re-run with different parameters",
            "TXT_Rerun=Re-run the tasks.",
            "TIP_Rerun_Extra=Re-run with different parameters",
            "TIP_Rerun=Re-run the tasks."
        })
        @SuppressWarnings("OverridableMethodCallInConstructor")
        ReRunAction(boolean debug) {
            this.debug = debug;
            this.putValue(Action.SMALL_ICON, debug ? ImageUtilities.loadImageIcon("org/netbeans/modules/gradle/resources/refreshdebug.png", false) : //NOI18N
                    ImageUtilities.loadImageIcon("org/netbeans/modules/gradle/resources/refresh.png", false));//NOI18N

            putValue(Action.NAME, debug ? Bundle.TXT_Rerun_extra() : Bundle.TXT_Rerun());
            putValue(Action.SHORT_DESCRIPTION, debug ? Bundle.TIP_Rerun_Extra() : Bundle.TIP_Rerun());
            setEnabled(false);

        }

        void setConfig(RunConfig config) {
            this.config = config;
        }

        @NbBundle.Messages("TIT_Run_Gradle=Run Gradle")
        @Override
        public void actionPerformed(ActionEvent e) {
            if (debug) {
                GradleExecutorOptionsPanel pnl = new GradleExecutorOptionsPanel(config.getProject());
                DialogDescriptor dd = new DialogDescriptor(pnl, Bundle.TIT_Run_Gradle());
                pnl.setCommandLine(config.getCommandLine(), config.getExecConfig());
                Object retValue = DialogDisplayer.getDefault().notify(dd);
                if (retValue == DialogDescriptor.OK_OPTION) {
                    GradleCommandLine cmd = pnl.getCommandLine();
                    pnl.rememberAs();
                    setConfig(config.withCommandLine(cmd));
                    RunUtils.executeGradle(config, null);
                }
            } else {
                RunUtils.executeGradle(config, null);
            }
            //TODO the waiting on tasks won't work..
        }
    }

    static class StopAction extends AbstractAction {

        private AbstractGradleExecutor exec;

        @NbBundle.Messages({
            "TXT_Stop_execution=Stop execution",
            "TIP_Stop_Execution=Stop the currently executing build"
        })
        @SuppressWarnings("OverridableMethodCallInConstructor")
        StopAction() {
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/gradle/resources/stop.png", false)); //NOi18N

            putValue(Action.NAME, Bundle.TXT_Stop_execution());
            putValue(Action.SHORT_DESCRIPTION, Bundle.TIP_Stop_Execution());
            setEnabled(false);
        }

        void setExecutor(AbstractGradleExecutor ex) {
            exec = ex;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    exec.cancel();
                }
            });
        }
    }

    public static final class OptionsAction extends AbstractAction {

        @NbBundle.Messages("LBL_OptionsAction=Gradle Settings")
        public OptionsAction() {
            super(Bundle.LBL_OptionsAction(), ImageUtilities.loadImageIcon("org/netbeans/modules/gradle/resources/options.png", true));
            putValue(Action.SHORT_DESCRIPTION, Bundle.LBL_OptionsAction());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OptionsDisplayer.getDefault().open("Java/Gradle"); //NOI18N
        }

    }
    
    private static void enableAction(Action a) {
        if (a != null) a.setEnabled(true);
    }
    
    private static void disableAction(Action a) {
        if (a != null) a.setEnabled(false);
    }

    protected class GradleItem implements BuildExecutionSupport.ActionItem {

        @Override
        public String getAction() {
            return config.getActionName() != null ? config.getActionName() : "xxx-custom"; //NOI18N
        }

        @Override
        public FileObject getProjectDirectory() {
            return config.getProject().getProjectDirectory();
        }

        @Override
        public String getDisplayName() {
            return config.getTaskDisplayName();
        }

        @Override
        public void repeatExecution() {
            //TODO: Implement
        }

        @Override
        public boolean isRunning() {
            return !task.isFinished();
        }

        @Override
        public void stopRunning() {
            AbstractGradleExecutor.this.cancel();
        }

    }
    private static void invokeUILater(Runnable runnable) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        EventQueue.invokeLater(runnable);
    }
}
