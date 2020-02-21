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

package org.netbeans.modules.cnd.discovery.buildsupport;

import org.netbeans.modules.cnd.discovery.api.BuildTraceSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * ProjectActionHandlerFactory for for builds using exec traces
 */
@ServiceProvider(service=ProjectActionHandlerFactory.class, position=3050)
public class BuildProjectActionHandlerFactory implements ProjectActionHandlerFactory {

    @Override
    public boolean canHandle(Type type, Lookup context, Configuration configuration) {
        if (type == PredefinedType.PRE_BUILD || type == PredefinedType.CLEAN || type == PredefinedType.BUILD) {
            if (configuration instanceof MakeConfiguration) {
                Node node = context.lookup(Node.class);
                if (node != null) {
                    Item item = (Item) node.getValue("Item"); // NOI18N
                    if (item != null) {
                        return false;
                    }
                }
                MakeConfiguration conf = (MakeConfiguration) configuration;
                final ExecutionEnvironment executionEnvironment = conf.getDevelopmentHost().getExecutionEnvironment();
                if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
                    if (BuildTraceSupport.useBuildTrace(conf)) {
                        BuildTraceSupport.BuildTrace support = BuildTraceSupport.supportedPlatforms(executionEnvironment, conf, null);
                        return support != null;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ProjectActionHandler createHandler() {
        return new BuildProjectActionHandler();
    }

    /* package-local */
    static ProjectActionHandler createDelegateHandler(ProjectActionEvent pae) {
        boolean selfFound = false;
        for (ProjectActionHandlerFactory factory : Lookup.getDefault().lookupAll(ProjectActionHandlerFactory.class)) {
            if (factory instanceof BuildProjectActionHandlerFactory) {
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
