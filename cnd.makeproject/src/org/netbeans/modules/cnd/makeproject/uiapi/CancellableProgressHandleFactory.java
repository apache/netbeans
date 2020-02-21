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

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.nativeexecution.api.execution.IOTabsController;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class CancellableProgressHandleFactory {
    public abstract ProgressHandle createProgressHandle(IOTabsController.InputOutputTab ioTab, ProjectActionHandler handlerToUse, EventsProcessorActions epa);
    
    private static final Default DEFAULT = new Default();

    public static CancellableProgressHandleFactory getProgressHandleFactory() {
        CancellableProgressHandleFactory defaultFactory = Lookup.getDefault().lookup(CancellableProgressHandleFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }
    private static final class Default extends CancellableProgressHandleFactory {

        @Override
        public ProgressHandle createProgressHandle(IOTabsController.InputOutputTab ioTab, ProjectActionHandler handlerToUse, EventsProcessorActions epa) {
            return ProgressHandle.createHandle(ioTab.getName());
        }
    }
    
}
