/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Provider of configurations for a project.
 * Should be registered in a project's {@link org.netbeans.api.project.Project#getLookup lookup}.
 * Besides the implementor, only the project UI infrastructure is expected to use this class.
 * @param C the type of configuration created by this provider
 *
 * @author Adam Sotona, Jesse Glick
 * @since org.netbeans.modules.projectapi/1 1.11
 * @see <a href="doc-files/configurations.html">Project Configurations design document</a>
 */
public interface ProjectConfigurationProvider<C extends ProjectConfiguration> {

    /**
     * Property name for the active configuration.
     * Use it when firing a change in the active configuration.
     */
    String PROP_CONFIGURATION_ACTIVE = "activeConfiguration"; // NOI18N

    /**
     * Property name of the set of configurations.
     * Use it when firing a change in the set of configurations.
     */
    String PROP_CONFIGURATIONS = "configurations"; // NOI18N

    /**
     * Gets a list of configurations.
     * Permitted to return different instances from one invocation to the next
     * but it is advisable for the "same" instances to compare as equal.
     * <p>Should be called within {@link org.netbeans.api.project.ProjectManager#mutex read access}.
     * @return all available configurations for this project
     */
    Collection<C> getConfigurations();

    /**
     * Gets the currently active configuration.
     * <p>Should be called within {@link org.netbeans.api.project.ProjectManager#mutex read access}.
     * @return the active configuration for this project (should be a member of {@link #getConfigurations}, or null only if that is empty)
     */
    @CheckForNull C getActiveConfiguration();

    /**
     * Sets the active configuration.
     * Should fire a change in {@link #PROP_CONFIGURATION_ACTIVE}.
     * It should be true afterwards that <code>configuration.equals(getActiveConfiguration())</code>
     * though it might not be true that <code>configuration == getActiveConfiguration()</code>.
     * <p class="nonnormative">
     * If possible, the choice of configuration should be persisted for the next IDE session.
     * If applicable, the persisted choice should be kept in per-user settings, not shared or versioned.
     * </p>
     * <p>Should be called within {@link org.netbeans.api.project.ProjectManager#mutex write access}.
     * @param configuration new active configuration
     * @throws IllegalArgumentException if the requested configuration is not a member of {@link #getConfigurations}
     * @throws IOException if storing the configuration change failed
     */
    void setActiveConfiguration(C configuration) throws IllegalArgumentException, IOException;

    /**
     * Checks if this project can provide a GUI customizer for its configurations.
     * @return true if {@link #customize} may be called
     */
    boolean hasCustomizer();

    /**
     * Customize this project's configurations.
     * Only permitted if {@link #hasCustomizer} is true.
     * May, for example, open the project properties dialog.
     */
    void customize();

    /**
     * Indicates if a project action is affected by the choice of configuration.
     * If so, a GUI for this action is permitted to show a list of configurations and
     * let the user select a configuration to apply to one action invocation only.
     * Such a GUI can avoid the need to first select an active configuration and
     * then run the action as two steps.
     * This is done by including a {@link ProjectConfiguration} in the context passed
     * to {@link ActionProvider#invokeAction}.
     * A project is free to return <code>false</code> even if the configuration
     * <em>might</em> affect the behavior of the action, if it simply does not
     * wish for such a GUI to be shown.
     * <p class="nonnormative">
     * The likely values of <code>command</code> are those actions
     * normally shown in the IDE's tool bar with main project bindings:
     * {@link ActionProvider#COMMAND_BUILD}, {@link ActionProvider#COMMAND_REBUILD},
     * {@link ActionProvider#COMMAND_RUN}, and {@link ActionProvider#COMMAND_DEBUG}.
     * </p>
     * @param command one of {@link ActionProvider#getSupportedActions}
     * @return true if the named command refers to an action affected by configurations
     */
    boolean configurationsAffectAction(String command);

    /**
     * Adds a listener to check for changes in {@link #PROP_CONFIGURATION_ACTIVE} or {@link #PROP_CONFIGURATIONS}.
     * @param lst a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener lst);

    /**
     * Removes a listener.
     * @param lst a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener lst);

}
