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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
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
     * {@link https://docs.gradle.org/current/javadoc/org/gradle/api/attributes/AttributeContainer.html AttributeContainer},
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


}
