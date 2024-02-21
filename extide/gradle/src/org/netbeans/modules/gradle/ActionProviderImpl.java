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
package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.*;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.actions.ActionToTaskUtils;
import org.netbeans.modules.gradle.execute.GradleExecutorOptionsPanel;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

import static org.netbeans.modules.gradle.Bundle.*;
import org.netbeans.modules.gradle.actions.KeyValueTableModel;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleCommandLine;
import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import org.netbeans.modules.gradle.customizer.CustomActionMapping;
import org.netbeans.modules.gradle.spi.actions.AfterBuildActionHook;
import org.netbeans.modules.gradle.spi.actions.BeforeBuildActionHook;
import org.netbeans.modules.gradle.spi.actions.BeforeReloadActionHook;
import org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static javax.swing.Action.NAME;
import javax.swing.DefaultCellEditor;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.api.execute.RunConfig.ExecFlag;
import org.netbeans.modules.gradle.execute.ProjectConfigurationSupport;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = {ActionProvider.class}, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class ActionProviderImpl implements ActionProvider {

    private static final RequestProcessor RP = new RequestProcessor(ActionProviderImpl.class.getName(), 3);
    private static final Logger LOG = Logger.getLogger(ActionProviderImpl.class.getName());

    public static final String COMMAND_DL_SOURCES = "download.sources"; //NOI18N
    public static final String COMMAND_DL_JAVADOC = "download.javadoc"; //NOI18N
    public static final String COMMAND_DL_SOURCES_JAVADOC = "download.sourcesanddoc"; //NOI18N

    private static final Pattern INPUT_PROP_REGEXP = Pattern.compile("\\$\\{input:([ \\w]+)(,([^\\}]+))?\\}"); //NOI18N

    private final Project project;

    public ActionProviderImpl(Project project) {
        this.project = project;
    }

    @Override
    public String[] getSupportedActions() {
        Set<String> actions = new HashSet<>(ActionToTaskUtils.getAllSupportedActions(project));
        // add a fixed 'prime build' action
        actions.add(ActionProvider.COMMAND_PRIME);
        actions.add(COMMAND_DL_SOURCES);
        actions.add(COMMAND_DL_JAVADOC);
        return actions.toArray(new String[0]);
    }
    
    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return;
        }
        if (ActionProvider.COMMAND_PRIME.equals(command)) {
            // delegate to prooblem provider we know exists & is registered
            NbGradleProjectImpl prjImpl = project.getLookup().lookup(NbGradleProjectImpl.class);
            ActionProgress prg = ActionProgress.start(context);
            LOG.log(Level.FINER, "Priming build starting for {0}", project);
            if (prjImpl.isProjectPrimingRequired()) {
                prjImpl.primeProject().
                        thenAccept(gp -> {
                            LOG.log(Level.FINER, "Priming build of {0} finished with status {1}, ", new Object[] { project, prjImpl.isProjectPrimingRequired() });
                            prg.finished(!prjImpl.isProjectPrimingRequired());
                        }).
                        exceptionally((e) -> { 
                            LOG.log(Level.FINER, e, () -> String.format("Priming build errored: %s", project));
                            prg.finished(false);
                            return null;
                        });
                return;
            } else {
                // no action, but report finish to unblock potential observers
                LOG.log(Level.FINER, "Priming build unncessary for {0}", project);
                prg.finished(true);
                return;
            }
            
        }
        NbGradleProject gp = NbGradleProject.get(project);
        GradleExecConfiguration execCfg = ProjectConfigurationSupport.getEffectiveConfiguration(project, context);
        ProjectConfigurationSupport.executeWithConfiguration(project, execCfg, () -> {
            ActionMapping mapping = ActionToTaskUtils.getActiveMapping(command, project, context);
            invokeProjectAction2(project, mapping, execCfg, context, false);
        });
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            GradleBaseProject gbp = GradleBaseProject.get(project);
            return gbp != null && gbp.getSubProjects().isEmpty() && ProjectOperations.isDeleteOperationSupported(project);
        }
        if (ActionProvider.COMMAND_PRIME.equals(command)) {
            NbGradleProjectImpl prjImpl = project.getLookup().lookup(NbGradleProjectImpl.class);
            boolean enabled = prjImpl.isProjectPrimingRequired();
            LOG.log(Level.FINEST, "Priming build action for {0} is: {1}", new Object[] { project, enabled });
            return enabled;
        }
        return ActionToTaskUtils.isActionEnabled(command, null, project, context);
    }

    @NbBundle.Messages({
        "# {0} - artifactId", "TXT_Run=Run ({0})",
        "# {0} - artifactId", "TXT_Debug=Debug ({0})",
        "# {0} - artifactId", "TXT_ApplyCodeChanges=Apply Code Changes ({0})",
        "# {0} - artifactId", "TXT_Profile=Profile ({0})",
        "# {0} - artifactId", "TXT_Test=Test ({0})",
        "# {0} - artifactId", "TXT_Build=Build ({0})",
        "# {0} - artifactId", "TXT_Delete=Delete ({0})",
    })

    static String taskName(Project project, String action, Lookup lkp) {
        String title;
        DataObject dobj = lkp.lookup(DataObject.class);
        String dobjName = dobj != null ? dobj.getName() : "";
        Project prj = project != null ? project : lkp.lookup(Project.class);
        String prjLabel = prj != null ? ProjectUtils.getInformation(prj).getDisplayName() : "No Project on Lookup"; //NOI18N
        switch (action) {
            case ActionProvider.COMMAND_RUN:
                title = TXT_Run(prjLabel);
                break;
            case ActionProvider.COMMAND_DEBUG:
                title = TXT_Debug(prjLabel);
                break;
            case ActionProvider.COMMAND_DELETE:
                title = TXT_Delete(prjLabel);
                break;
            case ActionProvider.COMMAND_PROFILE:
                title = TXT_Profile(prjLabel);
                break;
            case ActionProvider.COMMAND_TEST:
                title = TXT_Test(prjLabel);
                break;
            case ActionProvider.COMMAND_RUN_SINGLE:
                title = TXT_Run(dobjName);
                break;
            case ActionProvider.COMMAND_DEBUG_SINGLE:
            case ActionProvider.COMMAND_DEBUG_TEST_SINGLE:
                title = TXT_Debug(dobjName);
                break;
            case ActionProvider.COMMAND_PROFILE_SINGLE:
            case ActionProvider.COMMAND_PROFILE_TEST_SINGLE:
                title = TXT_Profile(dobjName);
                break;
            case ActionProvider.COMMAND_TEST_SINGLE:
                title = TXT_Test(dobjName);
                break;
            case "debug.fix":
                title = TXT_ApplyCodeChanges(prjLabel);
                break;
            default:
                title = TXT_Build(prjLabel);
        }
        return title;
    }

    private static void invokeProjectAction(final Project project, final ActionMapping mapping, Lookup context, boolean showUI) {
        GradleExecConfiguration execCfg = ProjectConfigurationSupport.getEffectiveConfiguration(project, context);
        ProjectConfigurationSupport.executeWithConfiguration(project, execCfg, () ->  {
            if (!invokeProjectAction2(project, mapping, execCfg, context, showUI)) {
                // the caller may wait on the action not knowing that it's not going to be executed at all. Report a failure.
                ActionProgress prg = ActionProgress.start(context);
                prg.finished(false);
            }
        });
    }
    
    private static boolean invokeProjectAction2(final Project project, final ActionMapping mapping, final GradleExecConfiguration execCfg, Lookup context, boolean showUI) {
        final String action = mapping.getName();
        if (ActionMapping.isDisabled(mapping)) {
            LOG.log(Level.FINE, "Attempt to run a config-disabled action: {0}", action);
            return false;
        }
        if (!ActionToTaskUtils.isActionEnabled(action, mapping, project, context)) {
            LOG.log(Level.FINE, "Attempt to run action that is not enabled: {0}", action);
            return false;
        }
        String argLine = askInputArgs(mapping.getDisplayName(), mapping.getArgs());
        if (argLine == null) {
            return false;
        }
        final StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        Lookup ctx = project.getLookup().lookup(BeforeBuildActionHook.class).beforeAction(action, context, out);

        final NbGradleProjectImpl prj = project.getLookup().lookup(NbGradleProjectImpl.class);
        final String[] args = RunUtils.evaluateActionArgs(project, action, argLine, ctx);
        Set<ExecFlag> flags = mapping.isRepeatable() ? EnumSet.of(ExecFlag.REPEATABLE) : EnumSet.noneOf(ExecFlag.class);
        RunConfig cfg = RunUtils.createRunConfig(project, action, taskName(project, action, ctx), ctx, execCfg, flags, args);

        if (showUI) {
            GradleExecutorOptionsPanel pnl = new GradleExecutorOptionsPanel(project);
            DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Run_Gradle());
            pnl.setCommandLine(cfg.getCommandLine(), execCfg);
            Object retValue = DialogDisplayer.getDefault().notify(dd);

            if (retValue == DialogDescriptor.OK_OPTION) {
                pnl.rememberAs();
                cfg = cfg.withCommandLine(pnl.getCommandLine());
            } else {
                return false;
            }
        }
        
        final String loadReason;
        if  (mapping.getDisplayName() != null && !mapping.getDisplayName().equals(mapping.getName())) {
            loadReason = mapping.getDisplayName();
        } else {
            loadReason = null;
        }

        boolean reloadOnly = !showUI && (args.length == 0);
        if (!reloadOnly) {
            //Trust project only if it does execute a real action
            ProjectTrust.getDefault().trustProject(project);
        }
        final boolean needReload;
        final Quality maxQualily = (cfg.getCommandLine().hasFlag(GradleCommandLine.Flag.OFFLINE))
                && (mapping.getReloadRule() != ActionMapping.ReloadRule.ALWAYS_ONLINE)
                ? FULL : FULL_ONLINE;
        switch (mapping.getReloadRule()) {
            case ALWAYS_ONLINE:
            case ALWAYS:
                needReload = true;
                break;
            case DEFAULT:
                //Reload only if we can retrieve better Quality from Gradle after build.

                needReload = maxQualily.betterThan(prj.getGradleProject().getQuality());
                break;
            default:
                needReload = false;
        }

        if (reloadOnly) {
            boolean canReload = project.getLookup().lookup(BeforeReloadActionHook.class).beforeReload(action, ctx, 0, null);
            if (needReload && canReload) {
                String[] reloadArgs = RunUtils.evaluateActionArgs(project, mapping.getName(), mapping.getReloadArgs(), ctx);
                final ActionProgress g = ActionProgress.start(context);
                RequestProcessor.Task reloadTask = prj.forceReloadProject(loadReason, false, maxQualily, reloadArgs);
                reloadTask.addTaskListener((t) -> {
                    g.finished(true);
                });
            }
        } else {
            final ExecutorTask task = RunUtils.executeGradle(cfg, writer.toString());
            final ActionProgress g = ActionProgress.start(context);
            final Lookup outerCtx = ctx;
            task.addTaskListener((Task t) -> {
                ProjectConfigurationSupport.executeWithConfiguration(project, execCfg, () -> {
                    try {
                        OutputWriter out1 = task.getInputOutput().getOut();
                        boolean canReload = project.getLookup().lookup(BeforeReloadActionHook.class).beforeReload(action, outerCtx, task.result(), out1);
                        if (needReload && canReload) {
                            String[] reloadArgs = RunUtils.evaluateActionArgs(project, mapping.getName(), mapping.getReloadArgs(), outerCtx);
                            RequestProcessor.Task reloadTask = prj.forceReloadProject(null, true, maxQualily, reloadArgs);
                            reloadTask.waitFinished();
                        }
                        project.getLookup().lookup(AfterBuildActionHook.class).afterAction(action, outerCtx, task.result(), out1);
                        for (AfterBuildActionHook l : context.lookupAll(AfterBuildActionHook.class)) {
                            l.afterAction(action, outerCtx, task.result(), out1);
                        }
                    } finally {
                        task.getInputOutput().getOut().close();
                        task.getInputOutput().getErr().close();
                        g.finished(task.result() == 0);
                    }
                });
            });
        }
        return true;
    }

    public static Action createCustomGradleAction(Project project, String name, ActionMapping mapping, Lookup context, boolean showUI) {
        return new CustomAction(project, name, mapping, context, showUI);
    }

    public static Action createCustomGradleAction(Project project, String name, String command, Lookup context, boolean showUI) {
        ActionMapping mapping = ActionToTaskUtils.getActiveMapping(command, project, context);
        return new CustomAction(project, name, mapping, context, showUI);
    }

    public static Action createCustomGradleAction(Project project, String name, String command, Lookup context) {
        return createCustomGradleAction(project, name, command, context, false);
    }

    public static Action createCustomGradleAction(Project project, String name, String command) {
        return createCustomGradleAction(project, name, command, Lookup.EMPTY, false);
    }

    private static final class CustomAction extends AbstractAction {

        private final ActionMapping mapping;
        private final boolean showUI;
        private final Lookup context;
        private final Project project;

        @SuppressWarnings("LeakingThisInConstructor")
        private CustomAction(Project project, String name, ActionMapping mapping, Lookup context, boolean showUI) {
            super(name);
            this.mapping = mapping;
            this.showUI = showUI;
            this.context = new ProxyLookup(context, Lookups.singleton(this));
            this.project = project;
        }

        @NbBundle.Messages("TIT_Run_Gradle=Run Gradle")
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            invokeProjectAction(project, mapping, context, showUI);
        }

    }

    // Copied from the Maven Plugin with minimal changes applied.
    private abstract static class ConditionallyShownAction extends AbstractAction implements ContextAwareAction {

        protected boolean triggeredOnFile = false;
        protected boolean triggeredOnGradle = false;

        @SuppressWarnings("OverridableMethodCallInConstructor")
        protected ConditionallyShownAction() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        public final @Override
        void actionPerformed(ActionEvent e) {
            assert false;
        }

        protected abstract Action forProject(@NonNull Project p, @NullAllowed FileObject file);

        @Override
        public final Action createContextAwareInstance(Lookup actionContext) {
            triggeredOnFile = false;
            triggeredOnGradle = false;
            Collection<? extends Project> projects = actionContext.lookupAll(Project.class);
            if (projects.size() != 1) {
                Collection<? extends FileObject> fobs = actionContext.lookupAll(FileObject.class);
                if (fobs.size() == 1) {
                    FileObject fo = fobs.iterator().next();
                    if (GradleFiles.BUILD_FILE_NAME.equals(fo.getNameExt())) {
                        Project p = null;
                        FileObject parent = fo.getParent();
                        for (Project prj : projects) {
                            if(prj.getProjectDirectory().equals(parent)) {
                                p = prj;
                                break;
                            }
                        }
                        if(p == null) {
                            p = FileOwnerQuery.getOwner(fo);
                        }
                        if (p != null) {
                             triggeredOnFile = true;
                             triggeredOnGradle = true;
                             Action a = forProject(p, null);
                             return a != null ? a : this;
                        }
                    } else {
                        Project p = findOwner(projects, fo);
                        if(p == null) {
                            p = FileOwnerQuery.getOwner(fo);
                        }
                        if (p != null) {
                             triggeredOnFile = true;
                             Action a = forProject(p, fo);
                             return a != null ? a : this;
                        }
                    }
                }
                return this;
            }
            Action a = forProject(projects.iterator().next(), null);
            return a != null ? a : this;
        }

        private Project findOwner(Collection<? extends Project> projects, FileObject fo) {
            FileObject parent = fo.getParent();
            if(parent == null) {
                return null;
            }
            for (Project prj : projects) {
                if(prj.getProjectDirectory().equals(fo.getParent())) {
                    return prj;
                }
            }
            return null;
        }
    }

    @ActionID(id = "org.netbeans.modules.gradle.customPopup", category = "Project")
    @ActionRegistration(displayName = "#LBL_Custom_Run", lazy = false)
    @ActionReferences({
        @ActionReference(position = 1400, path = "Projects/" + NbGradleProject.GRADLE_PROJECT_TYPE + "/Actions"),
        @ActionReference(position = 250, path = "Loaders/text/x-gradle+x-groovy/Actions"),
        @ActionReference(position = 250, path = "Loaders/text/x-gradle+x-kotlin/Actions"),
        @ActionReference(position = 1295, path = "Loaders/text/x-java/Actions"),
        @ActionReference(position = 1825, path = "Editors/text/x-java/Popup"),
        @ActionReference(position = 1295, path = "Loaders/text/x-groovy/Actions"),
        @ActionReference(position = 1825, path = "Editors/text/x-groovy/Popup")
    })
    @NbBundle.Messages({"LBL_Custom_Run=Run Gradle", "LBL_Custom_Run_File=Run Gradle"})
    public static ContextAwareAction customPopupActions() {
        return new ConditionallyShownAction() {

            protected @Override
            Action forProject(Project p, FileObject fo) {
                ActionProviderImpl ap = p.getLookup().lookup(ActionProviderImpl.class);
                return ap != null ? ap.new CustomPopupActions(triggeredOnFile, triggeredOnGradle, fo) : null;
            }
        };
    }

    private final class CustomPopupActions extends AbstractAction implements Presenter.Popup {

        private final boolean onFile;
        private final boolean onGradle;
        private final Lookup lookup;

        @SuppressWarnings("OverridableMethodCallInConstructor")
        private CustomPopupActions(boolean onFile, boolean onGradleFile, FileObject fo) {
            putValue(Action.NAME, onFile ? LBL_Custom_Run_File() : LBL_Custom_Run());
            this.onFile = onFile;
            this.onGradle = onGradleFile;
            this.lookup = fo != null ? Lookups.singleton(fo) : Lookup.EMPTY;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }

        @NbBundle.Messages({
            "LBL_Loading_tasks=Loading...",
            "LBL_Custom_run_tasks=Tasks..."
        })
        @Override
        public JMenuItem getPopupPresenter() {

            final JMenu menu = new JMenu(onFile ? LBL_Custom_Run_File() : LBL_Custom_Run());
            final JMenuItem loading = new JMenuItem(LBL_Loading_tasks());

            menu.add(loading);
            /*using lazy construction strategy*/
            RP.post(new Runnable() {

                @Override
                public void run() {
                    // should also handle the active configuration.
                    ProjectActionMappingProvider provider = project.getLookup().lookup(ProjectActionMappingProvider.class);

                    final List<Action> acts = new ArrayList<>();
                    for (String action : provider.customizedActions()) {
                        if (action.startsWith(CustomActionMapping.CUSTOM_PREFIX)) {
                            ActionMapping mapp = provider.findMapping(action);
                            if (ActionMapping.isDisabled(mapp)) {
                                // action is disabled.
                                continue;
                            }
                            Action act = createCustomGradleAction(project, mapp.getName(), mapp, lookup, false);
                            String displayName = INPUT_PROP_REGEXP.matcher(mapp.getArgs()).find() ? mapp.getDisplayName() + "..." : mapp.getDisplayName();

                            act.putValue(NAME, displayName);
                            acts.add(act);
                        }
                    }
                    acts.add(createCustomGradleAction(project, LBL_Custom_run_tasks(),
                            new CustomActionMapping(ActionMapping.CUSTOM_PREFIX), lookup, true));
                    SwingUtilities.invokeLater(() -> {
                        boolean selected = menu.isSelected();
                        menu.remove(loading);
                        for (Action a : acts) {
                            menu.add(new JMenuItem(a));
                        }
                        menu.getPopupMenu().pack();
                        menu.repaint();
                        menu.updateUI();
                        menu.setSelected(selected);
                    });
                }
            }, 100);
            return menu;
        }
    }

    @Messages({
        "# {0} - Command Display Name",
        "TIT_BuildParameters={0} Parameters"
    })
    private static String askInputArgs(String command, String argLine) {
        Matcher m = INPUT_PROP_REGEXP.matcher(argLine);
        List<String> keys = new ArrayList<>();
        List<String> defaults = new ArrayList<>();
        while (m.find()) {
            keys.add(m.group(1));
            defaults.add(m.group(3) != null ? m.group(3) : "");
        }
        String ret = argLine;

        if (!keys.isEmpty()) {
            KeyValueTableModel kvModel = new KeyValueTableModel("input:",
                    keys.toArray(new String[0]),
                    defaults.toArray(new String[0])
            );
            JPanel panel = new JPanel(new BorderLayout());
            JTable table = new JTable(kvModel);
            table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); //NOI18N
            table.setRowHeight(table.getRowHeight() * 3 / 2);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);

            try {
                //This code might not work on every Look and Feel
                DefaultCellEditor defaultEditor = (DefaultCellEditor) table.getDefaultEditor(String.class);
                defaultEditor.setClickCountToStart(1);
            } catch (ClassCastException ex) {
            }

            DialogDescriptor dlg = new DialogDescriptor(panel, TIT_BuildParameters(command));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(dlg)) {
                ret = ReplaceTokenProvider.replaceTokens(argLine, kvModel.getProperties());
            } else {
                //Mark Cancel is pressed, so build shall be aborted.
                ret = null;
            }
        }
        return ret;
    }
}
