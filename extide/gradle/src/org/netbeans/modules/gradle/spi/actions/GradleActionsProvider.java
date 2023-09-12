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

package org.netbeans.modules.gradle.spi.actions;

import java.io.InputStream;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 * Interface to contribute action mappings for the build. The Provider can declare actions to be added to the
 * project. From <b>version 2.13</b> the provider can also supply (declare) {@link GradleExecConfiguration}s that
 * will appear in project's UI and can alter action command line. Individual actions can be also customized for
 * the provided Configurations. See {@link #getSupportedActions()} for details.
 * <p>
 * Actions (and configurations) are collected in Lookup order.  In the case an action is defined by multiple Providers, 
 * the action that takes precedence will be determined by {@code priority} attribute and number of matching Plugins.
 * <p>
 * From <b>version 2.13</b> Project Lookup read and
 * processed <b>before Lookup.getDefault()</b>. You may register the providers using {@link ProjectServiceProvider} for
 * either base Gradle project's Lookup (for project type {@code org-netbeans-modules-gradle}, or for a specific Plugin
 * that must be present in the project in order for the {@link GradleActionsProvider} to activate. In that case, use 
 * {@link ProjectServiceProvider#projectType()} = {@code "org-netbeans-modules-gradle/Plugins/<plugin-id>"}.
 * <p>
 * <b>From version 2.14</b> the {@code InputStream} returned by {@link #defaultActionMapConfig()} can <b>disable</b> an action.
 * Overridance rules (priority, strength, ...) apply as usual.
 * <div class="nonnormative">
 * This is an example, how to disable e.g. {@code debug.single} action in a specific configuration the user may activate:
 * {@snippet file="org/netbeans/modules/gradle/actions/declarative-actions2.xml" region="disable-action-xml"}
 * </div>
 * @author Laszlo Kishalmi
 * @author Svata Dedic
 */
public interface GradleActionsProvider {
    /**
     * Determines if an action should be enabled. Usually determined by project structure and/or state. The Provider must no answer
     * {@code true} for actions it does not support.
     * @param action action name
     * @param project the target project
     * @param context context that would be used for action invocation.
     * @return true, if the action should be enabled.
     */
    boolean isActionEnabled(String action, Project project, Lookup context);
    
    /**
     * Returns the set of supported actions. If the Provider supplies actions for a certain {@link GradleExecConfiguration} only,
     * it should <b>still report them</b> here.
     * @return set of all supported actions.
     */
    Set<String> getSupportedActions();
    
    /**
     * Provides a declarative action description. The returned {@link InputStream} must contain XML content conforming to
     * {@code action-mapping.dtd}.
     * <div class="nonnormative">
     * An example of an action mapping:
     * {@snippet file="org/netbeans/modules/gradle/actions/action-mapping.xml" region="action-declaration-xml"}
     * </div>
     * <p>
     * Starting from <b>version 2.13</b> the InputStream can contain entries for {@link GradleExecConfiguration}s.
     * <div class="nonnormative">
     * Example of how configuration is declared in action mapping:
     * {@snippet file="org/netbeans/modules/gradle/actions/declarative-actions.xml" region="configuration-declaration-xml"}
     * </div>
     * @return stream with action description.
     */
    InputStream defaultActionMapConfig();
}
