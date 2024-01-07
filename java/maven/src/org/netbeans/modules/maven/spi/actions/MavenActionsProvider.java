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

package org.netbeans.modules.maven.spi.actions;

import java.util.Set;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Interface that allows to put additional items to project's popup plus to provide specific
 * implementations of ActionProvider actions.
 * Implementations should be registered in default lookup using {@link ServiceProvider},
 * or since 2.50 may also be registered using {@link ProjectServiceProvider} if applicable to just some packagings.
 * <p>
 * <b>Since 2.149</b> the returned {@link NetbeansActionMapping} can be disabled - checked by
 * {@link ActionToGoalUtils#isDisabledMapping}. Such mapping will override the action that may be even enabled by a farther 
 * {@link MavenActionsProvider}. The {@link ActionProvider} exported from the project will report such action as disabled.
 * <p>
 * <span id="declare-run-goals">
 * <b>Since 1.161</b>, specific plugin goals can be declaratively marked as 'run' goals; if they are used in actions, NetBeans maven core
 * recognizes them and allows to configure vm, application arguments and main class name for the goal. It is a responsibility
 * of action mapping to remap the properties used by exec:exec to goal-specific properties, if it uses different naming. See the
 * following example:
 * <div class="nonnormative">
 * {@snippet file="org/netbeans/modules/maven/runjar/example-rungoals-config.xml" region="register-run-goals"}
 * </div>
 * </span>
 * @author  Milos Kleint
 */
public interface MavenActionsProvider {

    
    /**
     * Create an instance of RunConfig configured for execution.
     * @param actionName one of the ActionProvider constants
     * @return RunConfig or null, if action not supported
     */
    RunConfig createConfigForDefaultAction(String actionName, Project project, Lookup lookup);

    /**
     * get a action to maven mapping configuration for the given action. No context specific value replacements
     * happen.
     * @return
     */
    NetbeansActionMapping getMappingForAction(String actionName, Project project);

    /**
     * return is action is supported or not
     * @param action action name, see ActionProvider for details.
     * @param project project that the action is invoked on.
     * @param lookup context for the action
     * @return
     */
    boolean isActionEnable(String action, Project project, Lookup lookup);

    /**
     * returns a list of supported actions, see ActionProvider.getSupportedActions()
     * @return
     */
    Set<String> getSupportedDefaultActions();
}
