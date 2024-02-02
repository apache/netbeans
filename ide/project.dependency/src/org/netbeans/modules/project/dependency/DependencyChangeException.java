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
package org.netbeans.modules.project.dependency;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @since 1.7
 * @author sdedic
 */
public final class DependencyChangeException extends Exception {
    /**
     * The reason for the failed operation.
     * PENDING: shouldn't this be String const for a compatible evolution ?
     */
    public static enum Reason {
        /**
         * A conflicting dependency is present for or change.
         */
        CONFLICT,
        
        /**
         * The dependency requested to be removed is missing.
         */
        MISSING,
        
        /**
         * A dependency in the request is invalid and cannot be applied.
         */
        MALFORMED,
    };
    
    private final Reason reason;
    private final DependencyChange request;
    
    /**
     * Map of offending dependencies. The Map is keyed by the dependencies
     * from the requested change. 
     */
    private final Map<Dependency, Dependency> offendingDependencies;
    
    public DependencyChangeException(DependencyChange request, Dependency d, Reason r) {
        this.reason = r;
        this.request = request;
        offendingDependencies = new HashMap<>();
        offendingDependencies.put(d, null);
    }
    
    public DependencyChangeException(DependencyChange request, Reason reason, Map<Dependency, Dependency> offendingDependencies) {
        this.reason = reason;
        this.request = request;
        this.offendingDependencies = Collections.unmodifiableMap(offendingDependencies);
    }

    /**
     * @return reason for a failed operation
     */
    public Reason getReason() {
        return reason;
    }
    
    /**
     * @return a list of failed dependencies.
     */
    public Collection<Dependency> getFailedDependencies() {
        return offendingDependencies.keySet();
    }

    /**
     * For a given dependency, get the associated conflict. For reasons other that {@link Reason#CONFLICT}, the
     * result value is undefined.
     * @param failed the dependency in conflict
     * @return the conflicting dependency.
     */
    public Dependency getConflictSource(Dependency failed) {
        return offendingDependencies.get(failed);
    }
}
