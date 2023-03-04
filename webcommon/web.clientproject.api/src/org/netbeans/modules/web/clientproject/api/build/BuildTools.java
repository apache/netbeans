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
package org.netbeans.modules.web.clientproject.api.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.build.ui.CustomizerPanel;
import org.netbeans.modules.web.clientproject.build.ui.NavigatorPanelImpl;
import org.netbeans.modules.web.clientproject.build.ui.TasksMenu;
import org.netbeans.modules.web.clientproject.spi.build.BuildToolImplementation;
import org.netbeans.modules.web.clientproject.spi.build.CustomizerPanelImplementation;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Support for build tools.
 * @since 1.81
 */
public final class BuildTools {

    private static final BuildTools INSTANCE = new BuildTools();


    private BuildTools() {
    }

    /**
     * Get instance of build tools.
     * @return instance of build tools
     */
    public static BuildTools getDefault() {
        return INSTANCE;
    }

    /**
     * Run "build" for the given project and command identifier.
     * <p>
     * If the are more build tools available, all the tools are run (in random order).
     * @param project project to be used for the command
     * @param commandId command identifier (build, rebuild, run etc.)
     * @param waitFinished wait till the command finishes?
     * @param warnUser warn user (show dialog, customizer) if any problem occurs (e.g. command is not known/set to this build tool)
     * @return {@code true} if command was run (at least by one build tool), {@code false} otherwise
     */
    public boolean run(@NonNull Project project, @NonNull String commandId, boolean waitFinished, boolean warnUser) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("commandId", commandId); // NOI18N
        boolean run = false;
        for (BuildToolImplementation buildTool : getEnabledBuildTools(project)) {
            if (buildTool.run(commandId, waitFinished, warnUser)) {
                run = true;
            }
        }
        return run;
    }

    /**
     * Check whether any build tool supports (is enabled in) the given project.
     * @param project project to be checked
     * @return {@code true} if any build tool supports the given project, {@code false} otherwise
     * @since 1.82
     */
    public boolean hasBuildTools(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        return !getEnabledBuildTools(project).isEmpty();
    }

    /**
     * Helper method for creating standard UI component for build tool customizer.
     * @param customizerSupport support for the UI component
     * @return standard UI component for build tool customizer
     * @since 1.87
     */
    public JComponent createCustomizerComponent(@NonNull CustomizerSupport customizerSupport) {
        Parameters.notNull("customizerSupport", customizerSupport); // NOI18N
        return new CustomizerPanel(customizerSupport);
    }

    /**
     * Helper method for creating standard UI for context menu for build tool tasks.
     * <p>
     * This menu supports loading tasks, showing and running tasks as well as configuring build tool.
     * @param tasksMenuSupport support for the UI
     * @return standard UI for context menu for build tool tasks
     * @since 1.92
     */
    public JMenu createTasksMenu(@NonNull TasksMenuSupport tasksMenuSupport) {
        Parameters.notNull("tasksMenuSupport", tasksMenuSupport); // NOI18N
        return new TasksMenu(tasksMenuSupport);
    }

    /**
     * Helper method for creating Navigator panel.
     * <p>
     * This panel simply lists and runs tasks of the build tool.
     * @param navigatorPanelSupport support for the panel
     * @return Navigator panel
     * @since 1.104
     */
    public NavigatorPanel createNavigatorPanel(@NonNull NavigatorPanelSupport navigatorPanelSupport) {
        Parameters.notNull("navigatorPanelSupport", navigatorPanelSupport); // NOI18N
        return new NavigatorPanelImpl(navigatorPanelSupport);
    }

    private Collection<BuildToolImplementation> getEnabledBuildTools(Project project) {
        assert project != null;
        Collection<? extends BuildToolImplementation> allBuildTools = project.getLookup()
                .lookupAll(BuildToolImplementation.class);
        List<BuildToolImplementation> enabledBuildTools = new ArrayList<>(allBuildTools.size());
        for (BuildToolImplementation buildTool : allBuildTools) {
            if (buildTool.isEnabled()) {
                enabledBuildTools.add(buildTool);
            }
        }
        return enabledBuildTools;
    }

    //~ Inner classes

    /**
     * Support for standard UI component for build tool customizer.
     * @since 1.87
     */
    public interface CustomizerSupport {

        /**
         * Get customizer category.
         * @return customizer category
         */
        @NonNull
        ProjectCustomizer.Category getCategory();

        /**
         * Get header which will be shown in the top of UI component.
         * @return header which will be shown in the top of UI component
         */
        @NonNull
        String getHeader();

        /**
         * Get task for the given command identifier. Can return {@code null}
         * if none assigned.
         * <p>
         * Note: This method is called in the UI thread.
         * @param commandId command identifier
         * @return task for the given command identifier, can be {@code null} if none assigned
         */
        @CheckForNull
        String getTask(@NonNull String commandId);

        /**
         * Set the given task for the given command identifier. Task can
         * be {@code null} if none assigned.
         * <p>
         * Note: This method is called in a background thread.
         * @param commandId command identifier
         * @param task task for the given command identifier, can be {@code null} if none assigned
         */
        void setTask(@NonNull String commandId, @NullAllowed String task);

        /**
         * Get panel for extending the existing build tool customizer.
         * @return panel for extending the existing build tool customizer, can be {@code null}
         * @since 1.102
         */
        @CheckForNull
        CustomizerPanelImplementation getCustomizerPanel();
    }

    /**
     * Support for creating standard UI for context menu for build tool tasks.
     * @since 1.92
     */
    public interface TasksMenuSupport extends BuildToolSupport {

        /**
         * Required titles.
         */
        enum Title {
            MENU,
            LOADING_TASKS,
            CONFIGURE_TOOL,
            MANAGE_ADVANCED,
            TASKS_LABEL,
        }

        /**
         * Gets title for the given {@link Title}.
         * @param title title to be "labeled"
         * @return title for the given {@link Title}
         */
        @NonNull
        String getTitle(@NonNull Title title);

        /**
         * Gets default task name (typically "default"); can be {@code null} if not supported.
         * @return default task name (typically "default"); can be {@code null} if not supported
         */
        @CheckForNull
        String getDefaultTaskName();

        /**
         * Reloads tasks.
         * <p>
         * This method always runs in a background thread.
         */
        void reloadTasks();

        /**
         * Configures build tool (typically shows IDE Options or Project Properties dialog).
         */
        void configure();

    }

    /**
     * Support for creating Navigator panels.
     * @since 1.104
     */
    public interface NavigatorPanelSupport {

        /**
         * Gets display name of the panel.
         * @return display name of the panel
         */
        @NonNull
        String getDisplayName();

        /**
         * Gets display hint of the panel.
         * @return display hint of the panel
         */
        @NonNull
        String getDisplayHint();

        /**
         * Gets build tool support for the given file. Can be {@code null}
         * if the given file is not supported.
         * @param buildFile file to create build tool support for, never {@code null}
         * @return build tool support for the given file, can be {@code null}
         */
        @CheckForNull
        BuildToolSupport getBuildToolSupport(@NonNull FileObject buildFile);

        /**
         * Adds listener to listen on panel changes.
         * @param listener listener to be added
         */
        void addChangeListener(ChangeListener listener);

        /**
         * Removes listener.
         * @param listener listener to be removed
         */
        void removeChangeListener(ChangeListener listener);

    }

    /**
     * Basic support for build tool.
     * @since 1.104
     */
    public interface BuildToolSupport {

        /**
         * Gets unique identifier, <b>non-localized</b> identifier of the build tool.
         * @return unique identifier, <b>non-localized</b> identifier of the build tool
         * @see BuildToolImplementation#getIdentifier()
         */
        @NonNull
        String getIdentifier();

        /**
         * Gets executable name of the build tool, e.g. <tt>grunt</tt>.
         * @return executable name of the build tool, e.g. <tt>grunt</tt>
         */
        @NonNull
        String getBuildToolExecName();

        /**
         * Gets project we run tasks for.
         * @return project we run tasks for
         */
        @NonNull
        Project getProject();

        /**
         * Gets working directory. This path is used for storing/loading
         * advanced tasks.
         * @return working directory, never {@code null}
         */
        @NonNull
        FileObject getWorkDir();

        /**
         * Gets future representing tasks.
         * <p>
         * Loading of tasks is done in a background thread automatically.
         * @return future representing tasks
         */
        @NonNull
        Future<List<String>> getTasks();

        /**
         * Runs the given task. No task is given for {@link #getDefaultTaskName() default} task.
         * <p>
         * This method always runs in a background thread.
         * @param args task arguments
         */
        void runTask(@NullAllowed String... args);

    }

}
