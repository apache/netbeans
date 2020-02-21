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

package org.netbeans.modules.cnd.remote.sync;

import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * ProjectActionHandlerFactory for remote builds using RFS
 */
@ServiceProvider(service=ProjectActionHandlerFactory.class, position=3000)
public class RemoteBuildProjectActionHandlerFactory implements ProjectActionHandlerFactory {

    private static boolean canHandleType(Type type) {
        if (type instanceof PredefinedType) {
            PredefinedType predefinedType = (PredefinedType) type;
            switch (predefinedType) {
                case PRE_BUILD:
                case BUILD:
                case BUILD_TESTS:
                case CLEAN:
                case COMPILE_SINGLE:
                    return true;
                case RUN:
                case DEBUG:
                case DEBUG_STEPINTO:
                case ATTACH:
                case DEBUG_TEST:
                case DEBUG_STEPINTO_TEST:
                case CHECK_EXECUTABLE:
                case CUSTOM_ACTION:
                case TEST:
                    return false;
                default:
                    AssertionError e = new AssertionError("Unexpected action type " + predefinedType.name()); //NOI18N
                    if (CndUtils.isDebugMode()) {
                        throw e;
                    } else {
                        e.printStackTrace(System.err);
                    }
            }
        }
        return false;
    }

    @Override
    public boolean canHandle(Type type, Lookup context, Configuration configuration) {
        if (canHandleType(type)) {
            if (configuration instanceof MakeConfiguration) {
                MakeConfiguration conf = (MakeConfiguration) configuration;
                if (conf.getDevelopmentHost().getExecutionEnvironment().isRemote()) {
                    return RfsSyncFactory.ENABLE_RFS;
                }
            }
        }
        return false;
    }

    @Override
    public ProjectActionHandler createHandler() {
        return new RemoteBuildProjectActionHandler();
    }

    /* package-local */
    static ProjectActionHandler createDelegateHandler(ProjectActionEvent pae) {
        boolean selfFound = false;
        for (ProjectActionHandlerFactory factory : Lookup.getDefault().lookupAll(ProjectActionHandlerFactory.class)) {
            if (factory instanceof RemoteBuildProjectActionHandlerFactory) {
                selfFound = true;
            } else if (selfFound) {
                if (factory.canHandle(pae)) {
                    return factory.createHandler();
                }
            }
        }
        return null;
    }

    @Override
    public boolean canHandle(ProjectActionEvent pae) {
        return canHandle(pae.getType(), pae.getContext(), pae.getConfiguration());
    }
}
