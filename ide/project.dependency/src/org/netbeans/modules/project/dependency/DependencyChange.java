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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Describes one change to dependencies declaration. A change may be either addition or
 * removal. More change types, like version change, sub-dependency override or dependency
 * exclusion may be added in the future.
 * 
 * @since 1.7
 * @author sdedic
 */
public final class DependencyChange {
    
    /**
     * Kind of operation
     */
    public enum Kind {
        /**
         * Adds dependencies
         */
        ADD, 
        
        /**
         * Removes dependencies
         */
        REMOVE, 
    }
    
    /**
     * Additional options that affect how the operation is performed. Some options only affect
     * certain operations.
     */
    public enum Options {
        /**
         * Skip silently dependencies that exists (add) or do not exist (remove)
         */
        skipConflicts,
        
        /**
         * Accept any other versions (the dependency matches the group:artifact:classifier regardless of version
         */
        ignoreVersions,
    }
    
    /**
     * Options applied to the operation.
     */
    private final EnumSet<Options> options;

    /**
     * The kind of the operation.
     */
    private final Kind  kind;
    
    /**
     * The dependency being worked on.
     */
    private final List<Dependency>  dependencies;

    private DependencyChange(EnumSet<Options> options, Kind kind, List<Dependency> dependencies) {
        this.options = options;
        this.kind = kind;
        this.dependencies = Collections.unmodifiableList(dependencies);
    }
    
    public EnumSet<Options> getOptions() {
        return options;
    }

    public Kind getKind() {
        return kind;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DependencyChange{");
        switch (kind) {
            case ADD:
                sb.append("ADD: ");
                break;
            case REMOVE:
                sb.append("REMOVE:" );
                break;
        }
        boolean next = false;
        for (Dependency d : dependencies) {
            if (next) {
                sb.append(", ");
            }
            sb.append(d.toString());
            next = true;
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Creates a simple "add" change. Adds listed dependencies, with optional options.
     * 
     * @param dependencies dependencies to add
     * @param options options for the operation
     * @return the change description
     */
    public static DependencyChange add(List<Dependency> dependencies, Options... options) {
        return new DependencyChange(
                options == null ? EnumSet.noneOf(Options.class) : EnumSet.copyOf(Arrays.asList(options)), 
                Kind.ADD, dependencies);
    }

    /**
     * Creates a simple "remove" change. Removes listed dependencies, with optional options.
     * 
     * @param dependencies dependencies to remove
     * @param options options for the operation
     * @return the change description
     */
    public static DependencyChange remove(List<Dependency> dependencies, Options... options) {
        return new DependencyChange(
                options == null ? EnumSet.noneOf(Options.class) : EnumSet.copyOf(Arrays.asList(options)), 
                Kind.REMOVE, dependencies);
    }

    /**
     * Creates a new change Builder.
     * @param k type of change
     * @return builder instance.
     */
    public static Builder builder(Kind k) {
        return new Builder(k);
    }
   
    /**
     * Builder that can create non trivial dependency changes.
     */
    public static final class Builder {
        private List<Dependency>  dependencies = new ArrayList<>();
        private EnumSet<Options> options = EnumSet.noneOf(Options.class);
        private Kind kind;
        
        private Builder(Kind kind) {
            this.kind = kind;
        }
        
        /**
         * Produces the dependency change
         * @return the dependency change description
         */
        public DependencyChange create() {
            return new DependencyChange(options, kind, dependencies);
        }
        
        public Builder dependency(Dependency... deps) {
            return dependency(Arrays.asList(deps));
        }
        
        public Builder dependency(Collection<Dependency> deps) {
            dependencies.addAll(deps);
            return this;
        }
        
        public Builder option(Options... toAdd) {
            if (toAdd == null) {
                return this;
            }
            options.addAll(Arrays.asList(toAdd));
            return this;
        }
    }
}
