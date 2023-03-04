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

import java.util.Collections;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;

/**
 * This is a less vague variant of the {@link SubprojectProvider} for code
 * that wants to access project's dependencies that are also projects.
 * Unlike some java level API this doesn't distinguish between compile, runtime, test level dependencies.
 * The implementation by project types is nonmandatory and if it's missing in the project's lookup, users should fallback to {@link SubprojectProvider}
 * @see Project#getLookup
 * @author mkleint
 * @since 1.56
 */
public interface DependencyProjectProvider {

    @NonNull Result getDependencyProjects();
    
 /**
     * Add a listener to changes in the set of dependency projects.
     * @param listener a listener to add
     */
    void addChangeListener(@NonNull ChangeListener listener);
    
    /**
     * Remove a listener to changes in the set of dependency projects.
     * @param listener a listener to remove
     */
    void removeChangeListener(@NonNull ChangeListener listener);
    
    /**
     * non mutable result object
     */
    public final class Result {
        private final boolean recursive;
        private final Set<? extends Project> projects;
        
        public Result(@NonNull Set<? extends Project> projects, boolean recursive) {
            this.projects = Collections.unmodifiableSet(projects);
            this.recursive = recursive;
        }
        
        public boolean isRecursive() {
            return recursive;
        }

        public @NonNull Set<? extends Project> getProjects() {
            return projects;
        }        
    }
}
