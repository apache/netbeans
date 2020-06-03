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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.class)
public class AttachOutputActionProvider extends BuildActionsProvider {

    @Override
    public List<BuildAction> getActions(String ioTabName, ProjectActionEvent[] events) {
        if (events != null && events.length > 0 && events[events.length - 1].getType() == ProjectActionEvent.PredefinedType.RUN) {
            return Collections.<BuildAction>singletonList(new AttachAction(events));
        }
        return Collections.emptyList();
    }

    private static final class AttachAction extends AbstractAction implements BuildAction {

        ProjectActionEvent[] events;
        private int step = -1;
        private int pid = ExecutionListener.UNKNOWN_PID;

        public AttachAction(ProjectActionEvent[] events) {
            this.events = events;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/actions/Attach.gif", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getBundle(AttachOutputActionProvider.class).getString("OUTPUT_ATTACH_ACTION_TEXT")); // NOI18N
            setEnabled(false);
        }

        @Override
        public void executionStarted(int pid) {
            if (step == events.length - 1 && pid != ExecutionListener.UNKNOWN_PID) {
                this.pid = pid;
                setEnabled(true);
            }
        }

        @Override
        public void executionFinished(int rc) {
            setEnabled(false);
        }

        @Override
        public void setStep(int step) {
            this.step = step;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            //do the real attach
            if (pid == ExecutionListener.UNKNOWN_PID) {
                return;
            }

            ProjectActionEvent event = events[step];
            ProjectInformation info = ProjectUtils.getInformation(event.getProject());
            if (info == null) {
                return;
            }

            //IZ 184743 we need winpid for MinGW gdb
            int attachPid = pid;
            final ExecutionEnvironment exEnv = (event.getConfiguration()).getDevelopmentHost().getExecutionEnvironment();
            if (exEnv.isLocal() && Utilities.isWindows()) {
                attachPid = WindowsSupport.getInstance().getWinPID(pid);
            }

            Project project = info.getProject();
            MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(project);
            if (conf != null) {
                // Get debugger type
                EngineType projectDebuggerType = NativeDebuggerManager.debuggerType(conf);

                String path = conf.getAbsoluteOutputValue().replace("\\", "/"); // NOI18N
                path = RemoteSyncSupport.getPathMap(exEnv, project).getRemotePath(path, true);

                if (projectDebuggerType != null) {
                    // do not change the original configuration!
                    DebugTarget dt = new DebugTarget(conf.clone());
                    dt.setExecutable(path);
                    dt.setPid(attachPid);
                    dt.setHostName(ExecutionEnvironmentFactory.toUniqueID(exEnv));
                    dt.setEngine(projectDebuggerType);

                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, 
                                            ProjectActionEvent.PredefinedType.ATTACH, 
                                            dt.getExecutable(), dt.getConfig(), 
                                            dt.getRunProfile(), false, 
                                            Lookups.fixed(dt)
                                    );
                    ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[] {projectActionEvent});
                }
            }
        }
    }
}
