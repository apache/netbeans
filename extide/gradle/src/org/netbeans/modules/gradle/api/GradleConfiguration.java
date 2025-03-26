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

package org.netbeans.modules.gradle.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import org.netbeans.modules.gradle.GradleModuleFileCache21;

/**
 * This object represents a configuration in a Gradle project.
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class GradleConfiguration implements Serializable, ModuleSearchSupport, Comparable<GradleConfiguration> {

    final String name;
    String description;
    Set<GradleDependency.ModuleDependency> modules = Collections.emptySet();
    Set<GradleDependency.ProjectDependency> projects = Collections.emptySet();
    Set<GradleDependency.UnresolvedDependency> unresolved = Collections.emptySet();
    Set<GradleConfiguration> extendsFrom = Collections.emptySet();
    GradleDependency.FileCollectionDependency files;
    Map<GradleDependency, Collection<GradleDependency>> dependencyMap = Collections.emptyMap();
    Set<GradleDependency> directChildren = Collections.emptySet();
    
    boolean transitive;
    boolean canBeResolved = true;
    boolean canBeConsumed;
    Map<String, String> attributes;

    GradleConfiguration(String name) {
        this.name = name;
    }

    public Set<GradleDependency.ModuleDependency> getModules() {
        return modules;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<GradleDependency.ProjectDependency> getProjects() {
        return projects;
    }

    public Set<GradleDependency.UnresolvedDependency> getUnresolved() {
        return unresolved;
    }

    public Set<GradleConfiguration> getExtendsFrom() {
        return extendsFrom;
    }
    
    /**
     * Returns set of dependencies configured for this configuration directly. Other
     * (direct) dependencies may be supplied by {@link #getExtendsFrom()} configurations.
     * @return direct dependencies
     */
    public Collection<? extends GradleDependency> getConfiguredDependencies() {
        if (canBeResolved) {
            return directChildren;
        } else {
            return unresolved;
        }
    }
    
    /**
     * Determines the origin of a given dependency. This works only for direct
     * dependencies (see {@link #getDependencies()} - as a dependency can be present
     * more than once in a dependency tree graph introduced by different intermediates in
     * different configurations.
     * <p>
     * The method retuns {@code null} if the origin cannot be determined.
     * 
     * @param d dependency to inspect
     * @return configuration of origin or {@code null}.
     */
    public GradleConfiguration getDependencyOrigin(GradleDependency d) {
        if (!getDependencies().contains(d) && !getConfiguredDependencies().contains(d)) {
            return null;
        }
        // TODO: possibly create a dependency-to-config cache in this instance to speed up further queries
        Set<GradleConfiguration> done = new HashSet<>();
        Queue<GradleConfiguration> toProcess = new ArrayDeque<>();
        toProcess.add(this);
        
        GradleConfiguration conf;
        while ((conf = toProcess.poll()) != null) {
            if (!done.add(conf)) {
                continue;
            }
            toProcess.addAll(conf.getExtendsFrom());
            if (conf.getConfiguredDependencies().contains(d)) {
                return conf;
            }
            if (!conf.isCanBeResolved()) {
                // unresolvable configurations (just buckets for dependencies) have unresolved dependencies,
                // that may lack version; compare the base g:a: against the id.
                String fullId = d.getId();
                String partialId;
                
                if (d instanceof GradleDependency.ModuleDependency) {
                    GradleDependency.ModuleDependency md = (GradleDependency.ModuleDependency)d;
                    partialId = String.format("%s:%s:", md.getGroup(), md.getName());
                } else {
                    try {
                        String[] split = GradleModuleFileCache21.gavSplit(fullId);
                        partialId = String.format("%s:%s:", split[0], split[1]);
                    } catch (IllegalArgumentException ex) {
                        continue; // next configuration
                    }
                }
                for (GradleDependency x : conf.getConfiguredDependencies()) {
                    if (x instanceof GradleDependency.UnresolvedDependency) {
                        if (x.getId().equals(fullId) || x.getId().equals(partialId)) {
                            return conf;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public Collection<GradleDependency> getDependencies() {
        return dependencyMap.getOrDefault(SELF_DEPENDENCY, Collections.emptySet());
    }
    
    public Collection<GradleDependency> getDependenciesOf(GradleDependency... path) {
        GradleDependency parent = path == null || path.length == 0 ? SELF_DEPENDENCY : path[path.length - 1];
        return dependencyMap.get(parent);
    }

    @Override
    public Set<GradleDependency.ModuleDependency> findModules(String group, String artifact, String version) {
        Set<GradleDependency.ModuleDependency> ret = new HashSet<>();
        for (GradleDependency.ModuleDependency module : modules) {
            boolean match = true;
            match &= (group != null) && module.group.matches(group);
            match &= (artifact != null) && module.name.matches(artifact);
            match &= (version != null) && module.version.matches(version);
            if (match) {
                ret.add(module);
            }
        }
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public Set<GradleDependency.ModuleDependency> findModules(String gav) {
        String parts[] = GradleModuleFileCache21.gavSplit(gav);
        String group = parts[0].isEmpty() ? null : parts[0];
        String artifact = parts[1].isEmpty() ? null : parts[1];
        String version = parts[2].isEmpty() ? null : parts[2];

        return findModules(group, artifact, version);
    }

    public Set<GradleConfiguration> getAllParents() {
        Set<GradleConfiguration> ret = new HashSet<>(extendsFrom);
        for (GradleConfiguration cfg : extendsFrom) {
            ret.addAll(cfg.getAllParents());
        }
        return Collections.unmodifiableSet(ret);
    }

    public GradleDependency.FileCollectionDependency getFiles() {
        return files;
    }

    public boolean isResolved() {
        return unresolved.isEmpty();
    }

    public boolean isTransitive() {
        return transitive;
    }

    public boolean isCanBeResolved() {
        return canBeResolved;
    }

    /**
     * Returns {@code true} if this configuration is to be consumed.
     * 
     * @return {@code true} if this configuration is consumable.
     * @since 2.24
     */
    public boolean isCanBeConsumed() {
        return canBeConsumed;
    }

    /**
     * Returns the attributes of this configuration. The returned map is a
     * simplified version of the Gradle configuration
     * <a href="https://docs.gradle.org/current/javadoc/org/gradle/api/attributes/AttributeContainer.html">AttributeContainer</a>,
     * where the attribute names are the keys and the attribute string values are the values.
     *
     * @return the attributes of this configuration
     * @since 2.24
     */
    public Map<String, String> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    public boolean isEmpty() {
        return ((files == null || files.files.isEmpty()) 
                && modules.isEmpty() 
                && unresolved.isEmpty() 
                && projects.isEmpty());
    }

    @Override
    public int compareTo(GradleConfiguration o) {
        if (o.extendsFrom.contains(this)) {
            return -1;
        }
        if (this.extendsFrom.contains(o)) {
            return 1;
        }
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "GradleConfiguration{" + "name=" + name + ", description=" + description + ", "
                + "modules=" + modules + ", projects=" + projects
                + ", unresolved=" + unresolved + ", extendsFrom="
                + extendsFrom + ", files=" + files + ", transitive=" + transitive + '}';
    }

    static final GradleDependency SELF_DEPENDENCY = new GradleDependency("") {
        @Override
        public Type getType() {
            return Type.PROJECT;
        }
    };

}
