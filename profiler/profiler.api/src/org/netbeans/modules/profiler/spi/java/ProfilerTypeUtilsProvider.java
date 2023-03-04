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
package org.netbeans.modules.profiler.spi.java;

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.profiler.api.java.ProfilerTypeUtils;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourcePackageInfo;

/**
 * An SPI for {@linkplain ProfilerTypeUtils} functionality
 * @author Jaroslav Bachorik
 */
public abstract class ProfilerTypeUtilsProvider {
    /**
     * 
     * @param className A fully qualified class name
     * @return Returns a resolved class or NULL
     */
    public abstract SourceClassInfo resolveClass(String className);
    
    /**
     * @return Returns a list of all main classes present in the project
     */
    public abstract Collection<SourceClassInfo> getMainClasses();
    
    /**
     * 
     * @param subprojects A flag indicating whether subprojects should be taken into account
     * @param scope A {@linkplain SourcePackageInfo.Scope} - SOURCE or DEPENDENCIES
     * @return Returns a list of project's packages
     */
    public abstract Collection<SourcePackageInfo> getPackages(boolean subprojects, SourcePackageInfo.Scope scope);
    
    /**
     * Case insensitive regexp class search
     * @param pattern Class pattern as a regular expression
     * @param scope A {@linkplain SourcePackageInfo.Scope} - SOURCE or DEPENDENCIES
     * @return Returns a collection of classes matching the given pattern
     * 
     * @since  1.3
     */
    public Collection<SourceClassInfo> findClasses(String pattern, Set<SourcePackageInfo.Scope> scope) {
        throw new UnsupportedOperationException();
    }
}
