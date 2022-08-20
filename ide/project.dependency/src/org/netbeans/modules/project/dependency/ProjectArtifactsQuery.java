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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.project.dependency.spi.ProjectArtifactsImplementation;
import org.openide.util.*;

/**
 * This project query provides access to artifacts produced or managed by a project. An artifact
 * is essentially a file (may extend to a collection of files in the future) that is a product of a project build,
 * or generally of a project or build system action. A project can have multiple artifacts produced by a build,
 * or can have multiple sets of artifacts produced by specific project actions. 
 * <p>
 * By default the query will return artifacts produced by project's compilation (incl. packaging, in maven terminology) - 
 * but the exact meaning depends on a build system used, and the project's settings and the active configuration.
 * 
 * @author sdedic
 */
public final class ProjectArtifactsQuery {
    
    /**
     * Computes artifacts produced by a project. The returned Result can enumerate artifacts
     * from the project, along with their location(s) either actual or supposed. The Result can
     * be listened of changes that may change the artifacts reported.
     * 
     * @param project the project to be queried
     * @param filter artifact filter
     * @return list of artifacts that can be listened on.
     */
    public static ArtifactsResult findArtifacts(Project project, Filter filter) {
        Parameters.notNull("project", project);
        Parameters.notNull("filter", filter);
        
        List<ProjectArtifactsImplementation> impls = new ArrayList<>(project.getLookup().lookupAll(ProjectArtifactsImplementation.class));
        SortedMap<Integer, List<E<?>>> buckets = new TreeMap<>();
        for (ProjectArtifactsImplementation impl : impls) {
            Object r = impl.evaluate(filter);
            if (r != null) {
                buckets.computeIfAbsent(impl.getOrder(), i -> new ArrayList<>()).add(new E(r, impl));
            }
        }
        List<E<?>> delegates;
        if (buckets.size() == 1) {
            delegates = buckets.values().iterator().next();
        } else {
            delegates = buckets.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
        }
        return new ArtifactsResult(delegates);
    }
    
    private static final class E<T> {
        final T data;
        final ProjectArtifactsImplementation<T> impl;

        public E(T data, ProjectArtifactsImplementation<T> impl) {
            this.data = data;
            this.impl = impl;
        }
        
        public Project findProject() {
            return impl.findProject(data);
        }

        public List<ArtifactSpec> findArtifacts() {
            return impl.findArtifacts(data);
        }

        public Collection<ArtifactSpec> findExcludedArtifacts() {
            return impl.findExcludedArtifacts(data);
        }

        public void handleChangeListener(ChangeListener l, boolean add) {
            impl.handleChangeListener(data, l, add);
        }

        public boolean computeSupportsChanges() {
            return impl.computeSupportsChanges(data);
        }
    }
    
    public static final class ArtifactsResult {
        private final List<E<?>> delegates;

        // @GuardedBy(this)
        private final List<ChangeListener> listeners = new ArrayList<>();
        // @GuardedBy(this)
        private ChangeListener delListener;
        // @GuardedBy(this)
        private List<ArtifactSpec> artifacts;
        // @GuardedBy(this)
        private Boolean supportsChanges;

        ArtifactsResult(List<E<?>> delegates) {
            this.delegates = delegates;
        }
        
        /**
         * Returns project artifacts. The result may become invalida after the result fires
         * an event to its {@link #addChangeListener(javax.swing.event.ChangeListener) ChangeListeners}.
         * @return artifacts produced by the project. 
         */
        public List<ArtifactSpec> getArtifacts() {
            synchronized (this) {
                if (artifacts != null) {
                    return artifacts;
                }
            }
            return updateResults();
        }
        
        List<ArtifactSpec> updateResults() {
            boolean changes = false;
            Collection<ArtifactSpec> specs = new LinkedHashSet<>();
            for (E<?> e : delegates) {
                Collection<ArtifactSpec> ex = e.findExcludedArtifacts();
                if (ex != null) {
                    specs.removeAll(ex);
                }
                Collection<ArtifactSpec> add = e.findArtifacts();
                if (add != null) {
                    specs.addAll(add);
                }
                changes |= e.computeSupportsChanges();
            }
            List<ArtifactSpec> copy = new ArrayList<>(specs);
            ChangeListener[] ll;
            
            synchronized (this) {
                if (null == supportsChanges) {
                    this.supportsChanges = changes;
                }
                if (this.artifacts != null && this.artifacts.equals(specs)) {
                    return copy;
                }
                this.artifacts = copy;
                if (listeners.isEmpty()) {
                    return copy;
                }
                ll = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            ChangeEvent e = new ChangeEvent(this);
            
            for (ChangeListener l : ll) {
                l.stateChanged(e);
            }
            return copy;
        }
        
        /**
         * Adds a listener that will be informed of changes. An event will fire if
         * the project changes in a way that might affect the reported artifacts.
         * 
         * @param l the listener
         */
        public void addChangeListener(ChangeListener l) {
            synchronized (this) {
                if (Boolean.FALSE.equals(supportsChanges)) {
                    return;
                }
                if (delListener == null) {
                    delListener = WeakListeners.change((e) -> updateResults(), null);
                    for (E d : delegates) {
                        d.handleChangeListener(delListener, true);
                    }
                }
                listeners.add(l);
            }
        }
        
        /**
         * Removes a previously registered Listener.
         * @param l the listener to unregister
         */
        public void removeChangeListener(ChangeListener l) {
            synchronized (this) {
                listeners.remove(l);
            }
        }
    }
    
    /**
     * Specifies the artifact filter. By default, the default artifact type is returned,
     * perhaps determined by the configured packaging with <b>no classifier</b>. It it possible
     * to list artifacts of all types and/or artifacts with any classifier in one query.
     */
    public static class Filter {
        /**
         * Represents all types of artifacts. The query will return all build products
         */
        public static final String TYPE_ALL = "all"; // NOI18N
        
        /**
         * Will return artifacts with any classifier.
         */
        public static final String CLASSIFIER_ANY = "any"; // NOI18N
        
        private final String classifier;
        private final String artifactType;
        private final ProjectActionContext  buildContext;
        
        Filter(String artifactType, String classifier, ProjectActionContext buildContext) {
            this.classifier = classifier;
            this.artifactType = artifactType;
            this.buildContext = buildContext;
        }

        /**
         * The desired artifact classifier. Only artifact with the specific classifier will be returned. The value
         * {@link #CLASSIFIER_ANY} means that any classifier will match. {@code null} (the default) value means
         * the default (none) classifier will match.
         * 
         * @return artifact classifier
         */
        @CheckForNull
        public String getClassifier() {
            return classifier;
        }

        /**
         * The desired artifact type. Only artifacts with tha type will be returned. {@link #TYPE_ALL} means that artifacts
         * of all types will be returned. {@code null} (the default) means the default type, i.e. defined by project packaging.
         * @return artifact type
         */
        @CheckForNull
        public String getArtifactType() {
            return artifactType;
        }

        /**
         * The {@link ProjectActionContext} that shall be used during evaluation.
         * @return context instance of {@code null}  if none specified.
         */
        @CheckForNull
        public ProjectActionContext getBuildContext() {
            return buildContext;
        }
    }
    
    /**
     * Creates a new simple Filter that returns artifacts of the specified type and
     * no classifier.
     * @param artifactType the desired type
     * @return Filter instance.
     */
    @NonNull
    public static Filter newQuery(@NullAllowed String artifactType) {
        return new Filter(artifactType, null, null);
    }
    
    /**
     * Creates a Filter with the specified properties
     * @param artifactType the desired type; use {@code null} for the default artifact type (i.e. defined by packaging)
     * @param classifier the desired classifier; use {@code null} for no classifier
     * @param buildContext the action context
     * @return Filter instance.
     */
    @NonNull
    public static Filter newQuery(@NullAllowed String artifactType, @NullAllowed String classifier, @NullAllowed ProjectActionContext buildContext) {
        return new Filter(artifactType, classifier, buildContext);
    }
}
