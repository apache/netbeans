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
 *
 * @author sdedic
 */
public final class ProjectArtifactsQuery {
    
    public static ArtifactsResult findArtifacts(Project project, Filter filter) {
        Parameters.notNull("project", project);
        Parameters.notNull("filter", filter);
        List<ProjectArtifactsImplementation.Result> delegates = new ArrayList<>();
        for (ProjectArtifactsImplementation impl : project.getLookup().lookupAll(ProjectArtifactsImplementation.class)) {
            ProjectArtifactsImplementation.Result r = impl.findArtifacts(filter);
            if (r != null) {
                delegates.add(r);
            }
        }
        
        return new ArtifactsResult(delegates);
    }
    
    public static final class ArtifactsResult {
        private final List<ProjectArtifactsImplementation.Result> delegates;

        // @GuardedBy(this)
        private final List<ChangeListener> listeners = new ArrayList<>();
        // @GuardedBy(this)
        private ChangeListener delListener;
        // @GuardedBy(this)
        private List<ArtifactSpec> artifacts;
        // @GuardedBy(this)
        private Boolean supportsChanges;

        ArtifactsResult(List<ProjectArtifactsImplementation.Result> delegates) {
            this.delegates = delegates;
        }
        
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
            for (ProjectArtifactsImplementation.Result d : delegates) {
                Collection<ArtifactSpec> ex = d.getExcludedArtifacts();
                if (ex != null) {
                    specs.removeAll(ex);
                }
                Collection<ArtifactSpec> add = d.getArtifacts();
                if (add != null) {
                    specs.addAll(add);
                }
                changes |= d.supportsChanges();
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
        
        public void addChangeListener(ChangeListener l) {
            synchronized (this) {
                if (supportsChanges == Boolean.FALSE) {
                    return;
                }
                if (delListener == null) {
                    delListener = WeakListeners.change((e) -> updateResults(), null);
                    for (ProjectArtifactsImplementation.Result d : delegates) {
                        d.addChangeListener(delListener);
                    }
                }
                listeners.add(l);
            }
        }
        
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
