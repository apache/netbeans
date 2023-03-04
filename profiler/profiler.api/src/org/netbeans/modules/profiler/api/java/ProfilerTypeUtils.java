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
package org.netbeans.modules.profiler.api.java;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.profiler.spi.java.ProfilerTypeUtilsProvider;
import org.openide.util.Lookup;

/**
 * Java types related profiler utility methods
 * 
 * @author Jaroslav Bachorik
 */
public final class ProfilerTypeUtils {
    private static ProfilerTypeUtilsProvider getProvider(Lookup.Provider project) {
        return project != null ? project.getLookup().lookup(ProfilerTypeUtilsProvider.class) : Lookup.getDefault().lookup(ProfilerTypeUtilsProvider.class);
    }
    
    /**
     * Resolves a class given its FQN
     * @param className The class FQN
     * @param project A project to resolve the class in
     * @return Returns a resolved {@linkplain SourceClassInfo} or null
     */
    public static SourceClassInfo resolveClass(String className, Lookup.Provider project) {
        ProfilerTypeUtilsProvider p = getProvider(project);
        return p != null ? p.resolveClass(className) : null;
    }
    
    /**
     * @param project A project to get the main classes for
     * @return Returns a list of all main classes present in the project
     */
    public static Collection<SourceClassInfo> getMainClasses(Lookup.Provider project) {
        ProfilerTypeUtilsProvider p = getProvider(project);
        
        return p != null ? p.getMainClasses() : Collections.EMPTY_LIST;
    }
    
    /**
     * Retrieves project's packages
     * @param subprojects Flag indicating whether subprojects should be taken into account
     * @param scope A {@linkplain SourcePackageInfo.Scope} - SOURCE or DEPENDENCIES
     * @param project A project to get the packages for
     * @return Returns a list of project's packages
     */
    public static Collection<SourcePackageInfo> getPackages(boolean subprojects, SourcePackageInfo.Scope scope, Lookup.Provider project) {
        ProfilerTypeUtilsProvider p = getProvider(project);
        
        return p != null ? p.getPackages(subprojects, scope) : Collections.EMPTY_LIST;
    }
    
    /**
     * Case insensitive regexp class search
     * @param pattern Class pattern as a regular expression
     * @param scope A {@linkplain SourcePackageInfo.Scope} - SOURCE or DEPENDENCIES
     * @param project A project to get the packages for
     * @return Returns a collection of classes matching the given pattern
     * 
     * @since  1.3
     */
    public static Collection<SourceClassInfo> findClasses(String pattern, Set<SourcePackageInfo.Scope> scope, Lookup.Provider project) {
        ProfilerTypeUtilsProvider p = getProvider(project);
        
        return p != null ? p.findClasses(pattern, scope) : Collections.EMPTY_LIST;
    }
}
