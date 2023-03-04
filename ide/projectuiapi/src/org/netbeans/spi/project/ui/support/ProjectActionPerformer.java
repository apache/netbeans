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

package org.netbeans.spi.project.ui.support;

import org.netbeans.api.project.Project;

/**
 * Callback interface for project- and main project-sensitive actions.
 * @author Petr Hrebejk
 */
public interface ProjectActionPerformer {

    /**
     * Called when the context of the action changes and the action should
     * be enabled or disabled within the new context, according to the newly
     * selected project.
     * @param project the currently selected project, or null if no project is selected
     * @return true to enable the action, false to disable it
     */
    boolean enable(Project project);
        
    /**
     * Called when the user invokes the action.
     * @param project the project this action was invoked for (XXX can this be null or not?)
     */
    void perform(Project project);
    
}
