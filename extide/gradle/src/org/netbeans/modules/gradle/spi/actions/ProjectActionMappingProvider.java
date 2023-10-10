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

import java.util.Set;
import org.netbeans.modules.gradle.api.execute.ActionMapping;
import org.netbeans.modules.gradle.api.execute.RunUtils;

/**
 * An implementation of this interface can be registered in the Gradle project
 * lookup. Gradle support already has an implementation using {@link GradleActionsProvider}-s.
 * <p>
 * This interface also can be used to query the Gradle command line arguments used
 * for a specific action. Like:
 * <pre>
 *     Project project = some Gradle project
 *     String action = some IDE action
 *     Lookup context = Lookups.singleton(project); // The context of the action, it is advised to have the project in the context.
 *     ProjectActionMappingProvider pamp = project.getLookup().lookup(ProjectActionMappingProvider.class);
 *     ActionMapping actionMapping = pamp.findMapping(action);
 *     GradleCommandLine cli = new GradleCommandLine(
 *             RunUtils.evaluateActionArgs(project, actionMapping.getArgs(), action, context));
 * </pre>
 * <p>
 * Since 2.14, the Provider may return a disabled {@link ActionMapping}. Check using 
 * {@link ActionMapping#isDisabled}, rather than just {@code == null}.
 * 
 * @author Laszlo Kishalmi
 * @since 2.6
 * @since 2.14 support for disabling an action
 */
public interface ProjectActionMappingProvider {

    /**
     * Try to find an ActionMapping for the given action.
     * If no mapping has been found it returns {@code null}.
     *
     * @param action the action name
     * @return the action mapping for the action or {@code null} if none found
     */
    ActionMapping findMapping(String action);

    /**
     * Get the list of those actions which has been customized.
     *
     * @return the list of customized action names.
     */
    Set<String> customizedActions();
}
