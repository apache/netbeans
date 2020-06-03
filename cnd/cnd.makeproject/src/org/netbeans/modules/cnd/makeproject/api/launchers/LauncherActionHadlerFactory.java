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
package org.netbeans.modules.cnd.makeproject.api.launchers;

import java.util.Collection;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.InputOutput;

/**
 *
 */

@ServiceProvider(service = ProjectActionHandlerFactory.class, position = 0)
public final class LauncherActionHadlerFactory implements ProjectActionHandlerFactory {
    private ExecutionListener listener;

    @Override
    public boolean canHandle(ProjectActionEvent.Type type, Lookup context, Configuration configuration) {
        return false;
    }

    @Override
    public boolean canHandle(ProjectActionEvent pae) {
        Lookup context = pae.getContext();
        ExecutionListener el = context.lookup(ExecutionListener.class);
        if (el != null) {
            this.listener = el;
            return true;
        }
        return false;
    }

    @Override
    public ProjectActionHandler createHandler() {
        ProjectActionHandler handler = new LauncherActionHandler(listener);
        return handler;
    }
    
    /*package*/static ProjectActionHandler createDelegateHandler(ProjectActionEvent pae) {
        boolean selfFound = false;
        for (ProjectActionHandlerFactory factory : Lookup.getDefault().lookupAll(ProjectActionHandlerFactory.class)) {
            if (factory instanceof LauncherActionHadlerFactory) {
                selfFound = true;
            } else if (selfFound) {
                if (factory.canHandle(pae)) {
                    return factory.createHandler();
                }
            }
        }
        return null;
    }

    private static class LauncherActionHandler implements ProjectActionHandler {
        private ProjectActionHandler delegate;
        private final ExecutionListener listener;

        public LauncherActionHandler(ExecutionListener listener) {
            this.listener = listener;
        }

        @Override
        public void init(ProjectActionEvent pae, ProjectActionEvent[] paes, Collection<BuildActionsProvider.OutputStreamHandler> outputHandlers) {
            this.delegate = LauncherActionHadlerFactory.createDelegateHandler(pae);
            this.delegate.init(pae, paes, outputHandlers);
            this.delegate.addExecutionListener(listener);
        }

        @Override
        public void execute(InputOutput io) {
            delegate.execute(io);
        }

        @Override
        public boolean canCancel() {
            return delegate.canCancel();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public void addExecutionListener(ExecutionListener l) {
            delegate.addExecutionListener(l);
        }

        @Override
        public void removeExecutionListener(ExecutionListener l) {
            delegate.removeExecutionListener(l);
        }
    }
}