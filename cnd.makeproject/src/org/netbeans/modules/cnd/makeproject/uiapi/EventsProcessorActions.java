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
package org.netbeans.modules.cnd.makeproject.uiapi;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class EventsProcessorActions {

    public interface EventsProcessorActionsFactory {

        EventsProcessorActions getEventsProcessorActions(BuildActionsProvider.EventsProcessor ep);
    }

    public abstract void setEnableRerunAction(boolean enable);

    public abstract void setEnableRerunModAction(boolean enable);

    public abstract void setEnableStopAction(boolean enable);

    public abstract void stopAction();

    public abstract ProjectActionHandler getActiveHandler();

    public abstract void setActiveHandler(ProjectActionHandler handler);

    public abstract Action[] getActions(String name);
    
    public abstract List<BuildActionsProvider.BuildAction> getAdditional();

    public abstract void setAdditional(String name);

    private static final Default DEFAULT = new Default();

    public static EventsProcessorActionsFactory getEventsProcessorActionsFactory() {
        EventsProcessorActionsFactory defaultFactory = Lookup.getDefault().lookup(EventsProcessorActionsFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    private static final class Default implements EventsProcessorActionsFactory {

        @Override
        public EventsProcessorActions getEventsProcessorActions(BuildActionsProvider.EventsProcessor ep) {
            return new EventsProcessorActions() {
                @Override
                public void setEnableRerunAction(boolean enable) {
                }

                @Override
                public void setEnableRerunModAction(boolean enable) {
                }

                @Override
                public void setEnableStopAction(boolean enable) {
                }

                @Override
                public void stopAction() {
                }

                @Override
                public ProjectActionHandler getActiveHandler() {
                    return null;
                }

                @Override
                public void setActiveHandler(ProjectActionHandler handler) {
                }

                @Override
                public List<BuildActionsProvider.BuildAction> getAdditional() {
                    return Collections.emptyList();
                }

                @Override
                public void setAdditional(String name) {
                }

                @Override
                public Action[] getActions(String name) {
                    return new Action[0];
                }
            };
        }

    }
}
