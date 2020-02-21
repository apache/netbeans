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
package org.netbeans.modules.cnd.makeproject.api;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.openide.util.Lookup;

/**
 * Provider of additional actions in the build log window
 * 
 */
public abstract class BuildActionsProvider {

    private static final BuildActionsProvider DEFAULT = new Default();

    public abstract List<BuildAction> getActions(String ioTabName, ProjectActionEvent[] events);

    protected BuildActionsProvider() {
    }

    /**
     * Static method to obtain the BuildActionsProvider implementation.
     * @return the BuildActionsProvider
     */
    public static synchronized BuildActionsProvider getDefault() {
        return DEFAULT;
    }

    public interface BuildAction extends Action, ExecutionListener {

        void setStep(int step);
    }

    public interface OutputStreamHandler {
        void handleLine(String line);

        void flush();

        void close();
    }

    public interface EventsProcessor {
        void submitTask();
        ProjectActionEvent[] getProjectActionEvents();
        boolean checkProject(ProjectActionEvent pae);
    }

    /**
     * Implementation of the default BuildActionsProvider
     */
    private static final class Default extends BuildActionsProvider {

        private final Lookup.Result<BuildActionsProvider> res;

        Default() {
            res = Lookup.getDefault().lookupResult(BuildActionsProvider.class);
        }

        @Override
        public List<BuildAction> getActions(String ioTabName, ProjectActionEvent[] events) {
            List<BuildAction> list = new ArrayList<>();
            res.allInstances().forEach((provider) -> {
                list.addAll(provider.getActions(ioTabName, events));
            });
            return list;
        }
    }
}
