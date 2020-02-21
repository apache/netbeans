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
package org.netbeans.modules.cnd.makeproject.ui.runprofiles;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.uiapi.EventsProcessorActions;
import org.netbeans.modules.dlight.api.terminal.TerminalSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public final class EventsProcessorActionsImpl extends EventsProcessorActions {
    
    @org.openide.util.lookup.ServiceProvider(service=EventsProcessorActionsFactory.class)
    public static final class EventsProcessorActionsFactoryImpl implements EventsProcessorActionsFactory {

        @Override
        public EventsProcessorActions getEventsProcessorActions(BuildActionsProvider.EventsProcessor ep) {
            return new EventsProcessorActionsImpl(ep);
        }
    }
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private final AtomicReference<ProjectActionHandler> activeHandlerRef = new AtomicReference<>(null);
    private final StopAction stopAction = new StopAction(activeHandlerRef);
    private final RerunAction rerunAction;
    private final RerunModAction rerunModAction;
    private final TermAction ta;
    private List<BuildActionsProvider.BuildAction> additional;
    private final BuildActionsProvider.EventsProcessor ep;

    public EventsProcessorActionsImpl(BuildActionsProvider.EventsProcessor ep) {
        this.ep = ep;
        rerunAction = new RerunAction(ep);
        rerunModAction = new RerunModAction(ep);
        ta = new TermAction(ep);
    }

    @Override
    public Action[] getActions(String name) {
        List<Action> list = new ArrayList<>();
        list.add(stopAction);
        list.add(rerunAction);
        for (int i = 0; i < ep.getProjectActionEvents().length; i++) {
            if (ep.getProjectActionEvents()[i].getType() == ProjectActionEvent.PredefinedType.RUN) {
                list.add(rerunModAction);
                break;
            }
        }
        list.add(ta);
        if (additional == null) {
            additional = BuildActionsProvider.getDefault().getActions(name, ep.getProjectActionEvents());
        }
        // TODO: actions should have acces to output writer. Action should listen output writer.
        // Provide parameter outputListener for DefaultProjectActionHandler.ProcessChangeListener
        list.addAll(additional);
        return list.toArray(new Action[list.size()]);
    }

    @Override
    public void setEnableRerunAction(boolean enable) {
        rerunAction.setEnabled(enable);
    }

    @Override
    public void setEnableRerunModAction(boolean enable) {
        rerunModAction.setEnabled(enable);
    }

    @Override
    public void setEnableStopAction(boolean enable) {
        stopAction.setEnabled(enable);
    }

    @Override
    public void stopAction() {
        stopAction.actionPerformed(null);
    }

    @Override
    public ProjectActionHandler getActiveHandler() {
        return activeHandlerRef.get();
    }

    @Override
    public void setActiveHandler(ProjectActionHandler handler) {
        activeHandlerRef.set(handler);
    }

    @Override
    public List<BuildActionsProvider.BuildAction> getAdditional() {
        return additional;
    }

    @Override
    public void setAdditional(String name) {
        this.additional = BuildActionsProvider.getDefault().getActions(name, ep.getProjectActionEvents());
    }

    private static final class StopAction extends AbstractAction {

        private final AtomicReference<ProjectActionHandler> activeHandlerRef;

        public StopAction(AtomicReference<ProjectActionHandler> activeHandlerRef) {
            this.activeHandlerRef = activeHandlerRef;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/stop.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, getString("TargetExecutor.StopAction.stop")); // NOI18N
            //System.out.println("handleEvents 1 " + handleEvents);
            //setEnabled(false); // initially, until ready
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            setEnabled(false);
            ProjectActionHandler handler = activeHandlerRef.getAndSet(null);
            if (handler != null) {
                handler.cancel();
            }
        }
    }

    private static final class RerunAction extends AbstractAction {

        private final BuildActionsProvider.EventsProcessor eventsProcessor;

        public RerunAction(final BuildActionsProvider.EventsProcessor eventsProcessor) {
            this.eventsProcessor = eventsProcessor;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/rerun.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, getString("TargetExecutor.RerunAction.rerun")); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            eventsProcessor.submitTask();
        }
    }

    private static final class RerunModAction extends AbstractAction {

        private final BuildActionsProvider.EventsProcessor eventsProcessor;

        public RerunModAction(final BuildActionsProvider.EventsProcessor eventsProcessor) {
            this.eventsProcessor = eventsProcessor;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/rerun-mod.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, getString("TargetExecutor.RerunAction.rerun-mod")); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            if (new RerunArguments(eventsProcessor.getProjectActionEvents()).showMe()) {
                eventsProcessor.submitTask();
            } else {
                setEnabled(true);
            }
        }
    }

    private static final class TermAction extends AbstractAction {

        private final BuildActionsProvider.EventsProcessor handleEvents;

        public TermAction(BuildActionsProvider.EventsProcessor handleEvents) {
            this.handleEvents = handleEvents;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/terminal/ui/term.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, getString("TargetExecutor.TermAction.text")); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = handleEvents.getProjectActionEvents().length - 1; i >= 0; i--) {
                ProjectActionEvent pae = handleEvents.getProjectActionEvents()[i];
                if (handleEvents.checkProject(pae)) {
                    String projectName = ProjectUtils.getInformation(pae.getProject()).getDisplayName();
                    String dir = pae.getProfile().getRunDirectory();
                    ExecutionEnvironment env = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
                    if (env.isRemote()) {
                        if (RemoteFileUtil.getProjectSourceExecutionEnvironment(pae.getProject()).isLocal()) {
                            PathMap pathMap = RemoteSyncSupport.getPathMap(pae.getProject());
                            if (pathMap != null) {
                                String aDir = pathMap.getRemotePath(dir);
                                if (aDir != null) {
                                    dir = aDir;
                                }
                            } else {
                                LOGGER.log(Level.SEVERE, "Path Mapper not found for project {0} - using local path {1}", new Object[]{pae.getProject(), dir}); //NOI18N
                            }
                        }
                    }
                    TerminalSupport.openTerminal(getString("TargetExecutor.TermAction.tabTitle", projectName, env.getDisplayName()), env, dir); // NOI18N
                }
                break;
            }
        }
    }
    
    private static String getString(String s) {
        return NbBundle.getMessage(EventsProcessorActionsImpl.class, s);
    }

    private static String getString(String s, String... arg) {
        return NbBundle.getMessage(EventsProcessorActionsImpl.class, s, arg);
    }
}
