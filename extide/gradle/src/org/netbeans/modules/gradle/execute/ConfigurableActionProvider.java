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

package org.netbeans.modules.gradle.execute;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.gradle.internal.impldep.javax.annotation.Nullable;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.spi.actions.ProjectActionMappingProvider;
import org.netbeans.spi.project.ProjectConfiguration;

/**
 * An factory for {@linkProjectActionMappingProvider}s that understands {@link ProjectConfiguration}s.
 * This provider should be preferred over plain {@link ProjectActionMappingProvide}, though {@code ProjectActionMappingProvider}
 * is also present as a bridge.
 * 
 * @author sdedic
 */
public interface ConfigurableActionProvider {
    /**
     * Adds a listener that is informed if the set of provided configurations change.
     * @param l listener instance.
     */
    void addChangeListener(ChangeListener l);
    
    /**
     * Removes previously registered listener.
     * @param l the listener instance.
     */
    void removeChangeListener(ChangeListener l);

    /**
     * Retrieves the list of provided configurations.
     * @return configurations.
     */
    List<GradleExecConfiguration> findConfigurations();
    
    /**
     * Try to find an ActionMapping for the given action.
     * If no mapping has been found it returns {@code null}.
     *
     * @param action the action name
     * @return the action mapping for the action or {@code null} if none found
     */
    ProjectActionMappingProvider findActionProvider(@Nullable String configurationId);
    
    /**
     * Returns a default mapping for the configuration
     * @param configurationId
     * @param action
     * @return 
     */
    ActionMapping findDefaultMapping(@Nullable String configurationId, String action);
}
