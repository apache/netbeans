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

package org.netbeans.modules.maven.execute;

import org.netbeans.modules.maven.execute.cmd.ExecutionEventObject;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import static org.netbeans.modules.maven.execute.Bundle.*;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import org.netbeans.modules.maven.execute.ui.ShowExecutionPanel;
import org.netbeans.modules.maven.options.MavenOptionController;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;

/**
 * common code for MAvenExecutors, sharing tabs and actions..
 * @author mkleint
 */
public abstract class AbstractMavenExecutor extends OutputTabMaintainer<AbstractMavenExecutor.TabContext> implements MavenExecutor, Cancellable {
    public static final class TabContext {
        ReRunAction rerun;
        ReRunAction rerunDebug;
        ResumeAction resume;
        ShowOverviewAction overview;
        StopAction stop;
        OptionsAction options;
        @Override protected TabContext clone() {
            TabContext c = new TabContext();
            c.rerun = rerun;
            c.rerunDebug = rerunDebug;
            c.resume = resume;
            c.overview = overview;
            c.stop = stop;
            c.options = options;
            return c;
        }
    }

    @Override protected Class<TabContext> tabContextType() {
        return TabContext.class;
    }

    protected RunConfig config;
    private TabContext tabContext;
    private List<String> messages = new ArrayList<String>();
    private List<OutputListener> listeners = new ArrayList<OutputListener>();
    protected ExecutorTask task;
    protected MavenItem item;
    protected final Object SEMAPHORE = new Object();
    
    
    protected AbstractMavenExecutor(RunConfig conf) {
        this(conf, new TabContext());
    }
    
    AbstractMavenExecutor(RunConfig conf, TabContext tc) {
        super(conf.getExecutionName());
        config = conf;
        this.tabContext = tc == null ? new TabContext() : tc;
    }


    @Override public final void setTask(ExecutorTask task) {
        synchronized (SEMAPHORE) {
            this.task = task;
            this.item = new MavenItem();
            SEMAPHORE.notifyAll();
        }
    }

    @Override public final void addInitialMessage(String line, OutputListener listener) {
        messages.add(line);
        listeners.add(listener);
    }

    protected final void processInitialMessage() {
        Iterator<String> it1 = messages.iterator();
        Iterator<OutputListener> it2 = listeners.iterator();
        InputOutput ioput = getInputOutput();
        try {
            while (it1.hasNext()) {
                OutputListener ol = it2.next();
                if (ol != null) {
                    ioput.getErr().println(it1.next(), ol, true);
                } else {
                    ioput.getErr().println(it1.next());
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected final void actionStatesAtStart() {
        updateUILater(false, () -> {
            createNewTabActions();
            tabContext.rerun.setEnabled(false);
            tabContext.rerunDebug.setEnabled(false);
            tabContext.overview.setRoot(null);
            tabContext.resume.setFinder(null);
            tabContext.stop.setEnabled(true);
        });
    }

    protected interface ResumeFromFinder {
        @CheckForNull NbMavenProject find(@NonNull Project root);
    }

    protected final void actionStatesAtFinish(final @NullAllowed ResumeFromFinder resumeFromFinder, final @NullAllowed ExecutionEventObject.Tree root) {
        updateUILater(false, () -> {
            createNewTabActions();
            tabContext.rerun.setEnabled(true);
            tabContext.rerunDebug.setEnabled(true);
            tabContext.resume.setFinder(resumeFromFinder);
            tabContext.overview.setRoot(root);
            tabContext.stop.setEnabled(false);
        });
    }

    @Override
    protected void reassignAdditionalContext(TabContext tabContext) {
        this.tabContext = tabContext;
        tabContext.rerun.setConfig(config);
        tabContext.rerunDebug.setConfig(config);
        tabContext.resume.setConfig(config);
        tabContext.stop.setExecutor(this);
        tabContext.overview.setExecutor((MavenCommandLineExecutor)this);
        tabContext.overview.setRoot(null);
    }

    

    @Override protected final TabContext createContext() {
        return tabContext.clone();
    }

    @Override protected Action[] createNewTabActions() {
        if (tabContext.rerun == null) {
        tabContext.rerun = new ReRunAction(false);
        tabContext.rerunDebug = new ReRunAction(true);
        tabContext.resume = new ResumeAction();
        tabContext.stop = new StopAction();
        tabContext.options = new OptionsAction();
        tabContext.overview = new ShowOverviewAction();
        }
        tabContext.rerun.setConfig(config);
        tabContext.rerunDebug.setConfig(config);
        tabContext.resume.setConfig(config);
        tabContext.overview.setExecutor((MavenCommandLineExecutor) this);
        tabContext.stop.setExecutor(this);
        return new Action[] {
            tabContext.rerun,
            tabContext.rerunDebug,
            tabContext.resume,
            tabContext.overview,
            tabContext.stop,
            tabContext.options,
        };
    }

    class ReRunAction extends AbstractAction {

        private RunConfig config;
        private boolean debug;

        @Messages({
            "TXT_Rerun_extra=Re-run with different parameters",
            "TXT_Rerun=Re-run the goals.",
            "TIP_Rerun_Extra=Re-run with different parameters",
            "TIP_Rerun=Re-run the goals."
        })
        ReRunAction(boolean debug) {
            this.debug = debug;
            this.putValue(Action.SMALL_ICON, debug ? ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/refreshdebug.png", false) : //NOI18N
                    ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/refresh.png", false));//NOI18N

            putValue(Action.NAME, debug ? TXT_Rerun_extra() : TXT_Rerun());
            putValue(Action.SHORT_DESCRIPTION, debug ? TIP_Rerun_Extra() : TIP_Rerun());
            setEnabled(false);

        }

        void setConfig(RunConfig config) {
            this.config = config;
        }

        @Messages("TIT_Run_maven=Run Maven")
        @Override public void actionPerformed(ActionEvent e) {
            BeanRunConfig newConfig = new BeanRunConfig(config);
            if (debug) {
                RunGoalsPanel pnl = new RunGoalsPanel();
                DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Run_maven());
                pnl.readConfig(config);
                Object retValue = DialogDisplayer.getDefault().notify(dd);
                if (retValue == DialogDescriptor.OK_OPTION) {
                    pnl.applyValues(newConfig);
            } else {
                    return;
            }
            }
            actionStatesAtStart();
            InputOutput inputOutput = getInputOutput();
            try {
                inputOutput.getOut().reset();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            MavenCommandLineExecutor.executeMaven(newConfig, inputOutput, tabContext);
        //TODO the waiting on tasks won't work..
        }
    }

    private static class ResumeAction extends AbstractAction {

        private static final RequestProcessor RP = new RequestProcessor(ResumeAction.class);
        private RunConfig config;
        private ResumeFromFinder finder;

        @Messages("TIP_resume=Resume build starting from failed submodule.")
        ResumeAction() {
            setEnabled(false);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/forward.png", true));
            putValue(SHORT_DESCRIPTION, TIP_resume());
        }

        void setConfig(RunConfig config) {
            this.config = config;
        }

        void setFinder(ResumeFromFinder finder) {
            this.finder = finder;
            setEnabled(finder != null);
        }

        @Messages({
            "ResumeAction_scanning=Searching for faulty module",
            "ResumeAction_could_not_find_module=Could not determine module from which to resume build."
        })
        @Override public void actionPerformed(ActionEvent e) {
            final Project p = config.getProject();
            if (p == null) {
                setFinder(null);
                StatusDisplayer.getDefault().setStatusText(ResumeAction_could_not_find_module());
                return;
            }
            final AtomicReference<Thread> t = new AtomicReference<Thread>();
            final ProgressHandle handle = ProgressHandle.createHandle(ResumeAction_scanning(), new Cancellable() {
                @Override public boolean cancel() {
                    Thread _t = t.get();
                    if (_t != null) {
                        _t.interrupt();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            RP.post(new Runnable() {
                @Override public void run() {
                    t.set(Thread.currentThread());
                    handle.start();
                    NbMavenProject nbmp;
                    try {
                        nbmp = finder.find(p);
                    } finally {
                        handle.finish();
                    }
                    t.set(null);
                    if (nbmp == null || NbMavenProject.isErrorPlaceholder(nbmp.getMavenProject())) {
                        setFinder(null);
                        StatusDisplayer.getDefault().setStatusText(ResumeAction_could_not_find_module());
                        return;
                    }
                    File root = config.getExecutionDirectory();
                    File module = nbmp.getMavenProject().getBasedir();
                    String rel = root != null && module != null ? FileUtilities.relativizeFile(root, module) : null;
                    String id = rel != null ? rel : nbmp.getMavenProject().getGroupId() + ':' + nbmp.getMavenProject().getArtifactId();
                    BeanRunConfig newConfig = new BeanRunConfig(config);
                    List<String> goals = new ArrayList<String>(config.getGoals());
                    int rf = goals.indexOf("--resume-from");
                    if (rf != -1) {
                        goals.set(rf + 1, id);
                    } else {
                        goals.add(0, "--resume-from");
                        goals.add(1, id);
                    }
                    newConfig.setGoals(goals);
                    RunUtils.executeMaven(newConfig);
                }
            });
        }

    }

    static class StopAction extends AbstractAction {

        private AbstractMavenExecutor exec;

        @Messages({
            "TXT_Stop_execution=Stop execution",
            "TIP_Stop_Execution=Stop the currently executing build"
        })
        StopAction() {
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/stop.png", false)); //NOi18N

            putValue(Action.NAME, TXT_Stop_execution());
            putValue(Action.SHORT_DESCRIPTION, TIP_Stop_Execution());
            setEnabled(false);
        }

        void setExecutor(AbstractMavenExecutor ex) {
            exec = ex;
        }

        @Override public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override public void run() {
                    exec.cancel();
                }
            });
        }
    }

    public static final class OptionsAction extends AbstractAction {

        @Messages("LBL_OptionsAction=Maven Settings")
        public OptionsAction() {
            super(LBL_OptionsAction(), ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/options.png", true));
            putValue(Action.SHORT_DESCRIPTION, LBL_OptionsAction());
        }

        @Override public void actionPerformed(ActionEvent e) {
            OptionsDisplayer.getDefault().open(JavaOptions.JAVA + "/" + MavenOptionController.OPTIONS_SUBPATH);
        }

    }
    
    private static final class ShowOverviewAction extends AbstractAction {
        private MavenCommandLineExecutor executor;
        private ExecutionEventObject.Tree root;
        private Dialog d;
        @Messages("LBL_ShowOverviewAction=Show Build Overview")
        ShowOverviewAction() {
            super(LBL_ShowOverviewAction(), ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/ui/buildplangoals.png", true));
            putValue(Action.SHORT_DESCRIPTION, LBL_ShowOverviewAction());
            setEnabled(false);
        }

        @Override 
        public void actionPerformed(ActionEvent e) {
            if (root == null) {
                return; //#238704 not clear when this would happen for an enabled action.
            }
            ShowExecutionPanel panel = new ShowExecutionPanel();
            panel.setTreeToDisplay(root, executor != null ? executor.config : null);
            DialogDescriptor dd = new DialogDescriptor(panel, "Build execution overview");
            dd.setOptions(new Object[] {"Close"});
            dd.setClosingOptions(new Object[] {"Close"});
            d = DialogDisplayer.getDefault().createDialog(dd);
            d.setModal(false);
            d.setVisible(true);
        }

        private void setExecutor(MavenCommandLineExecutor aThis) {
            executor = aThis;
            updateUILater(false, () -> {
                if (d != null && d.isVisible()) {
                    d.setVisible(false);
                    d = null;
                }
            });
        }

        private void setRoot(final ExecutionEventObject.Tree root) {
            this.root = root;
            updateUILater(true, () -> {
                setEnabled(root != null);
            });
        }
        
    }

    protected class MavenItem implements BuildExecutionSupport.ActionItem {
        
        @Override public String getDisplayName() {
            return config.getTaskDisplayName();
        }

        @Override public void repeatExecution() {
            RunUtils.executeMaven(config);
        }

        @Override public boolean isRunning() {
            return !task.isFinished();
        }

        @Override public void stopRunning() {
            AbstractMavenExecutor.this.cancel();
        }

        @Override
        public String getAction() {
            return config.getActionName() != null ? config.getActionName() : "xxx-custom";
        }

        @Override
        public FileObject getProjectDirectory() {
            return config.getProject() != null ? config.getProject().getProjectDirectory() : null;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + getAction().hashCode();
            hash = 41 * hash + getProjectDirectory().hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MavenItem other = (MavenItem) obj;
            if (!this.getAction().equals(other.getAction())) {
                return false;
            }
            if (this.getProjectDirectory() != other.getProjectDirectory() && (this.getProjectDirectory() == null || !this.getProjectDirectory().equals(other.getProjectDirectory()))) {
                return false;
            }
            return true;
        }
    }

    private static void updateUILater(boolean asap, Runnable ui) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        if (asap && EventQueue.isDispatchThread()) {
            ui.run();
        } else {
            EventQueue.invokeLater(ui);
        }
    }
}
