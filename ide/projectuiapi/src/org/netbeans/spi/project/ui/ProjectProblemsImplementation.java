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
package org.netbeans.spi.project.ui;

import java.util.concurrent.CompletableFuture;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;

/**
 * Interface that plugs in the actual implementation of UI for {@link ProjectProblems} API.
 * 
 * @author Tomas Zezula, Svatopluk Dedic
 */
public interface ProjectProblemsImplementation {
    
    /**
     * Show alert message box informing user that a project has problems (broken references).
     * references. The implementation should handle gracefully repeated alerts for the same
     * project, by e.g. ignoring requests to alert for a project that has still its problem
     * resolution UI opened.
     * 
     * @param project to show the alert for.
     * @return future that will be completed when the user finishes the UI.
     */
    CompletableFuture<Void> showAlert(@NonNull Project project);

    /**
     * Shows a customizer, or another UI to handle project problems.
     * @param project whose problems should be resolved.
     * @return future that will be completed once the customizer finishes.
     */
    CompletableFuture<Void> showCustomizer(@NonNull Project project);
}
