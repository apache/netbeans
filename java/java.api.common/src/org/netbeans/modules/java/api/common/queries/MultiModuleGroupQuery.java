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
package org.netbeans.modules.java.api.common.queries;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.SourceGroup;

/**
 * Provides module-related extended information on SourceGroup. In a multi-module project with
 * multiple source roots and module paths defined by masks, several package roots may reside in
 * folder with the same name (e.g. {@code "classes"}). The default {@link SourceGroup} presentation
 * (display name) includes module information, so the user can identify individual package root
 * folders. In some cases, different presentation, or more precise information on
 * relationship between a SourceGroup and module is needed. Such information can be obtained
 * using {@code MultiModuleGroupQuery}.
 * <p>
 * The object can be obtained from project Lookup. If it is not present, the project does not support
 * java modules.
 * <p>
 * If the source level of the project is 8 or less, the {@code MultiModuleGroupQuery} may be present,
 * but will return {@code null} on all queries, as modules are not supported before JDK9.
 * @author sdedic
 * @since 1.103
 */
public interface MultiModuleGroupQuery {
    /**
     * Obtains module-related information for the passed {@link SourceGroup}.
     * Returns {@code null}, if {@code SourceGroup} is not known, is not owned by any module, or language
     * level for the project does not support modules.
     * 
     * @param grp not null, the {@code SourceGroup} instance
     * @return module-related information, or {@code null} 
     */
    @CheckForNull 
    public Result       findModuleInfo(@NonNull SourceGroup grp);
    
    /**
     * Determines which {@link SourceGroup}s are owned by a particular module.
     * Returns {@link SourceGroup}s contained within the module, empty array
     * if module does not exist or does not contain any of the passed {@code SourceGroups}.
     * @param modName module name
     * @param groups groups, to filter.
     * @return SourceGroups contained by that particular module.
     */
    @NonNull
    public SourceGroup[] filterModuleGroups(@NonNull String modName, @NonNull SourceGroup[] groups);
    
    /**
     * Describes properties of a {@link SourceGroup} related to modular
     * structure of the project.
     */
    public static final class Result {
        private final String  moduleName;
        private final String  modulePath;
        
        Result(String moduleName, String modulePath) {
            this.moduleName = moduleName;
            this.modulePath = modulePath;
        }

        /**
         * @return name of module owning the source group
         */
        public String getModuleName() {
            return moduleName;
        }
        
        /**
         * @return path to source group within the module
         */
        public String getPathFromModule() {
            return modulePath;
        }
    }
}
