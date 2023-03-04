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
package org.netbeans.api.project;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * This represents context for a project model query. The context describe a project action
 * for which the query should be evaluated, project configuration (if not the active one), 
 * and possible custom overrides.
 * {@link ProjectConfiguration} or the IDE-supported project action and the associated properties, profiles, or Lookup may
 * affect behaviour of queries across the IDE.
 * <p>
 * Properties map to user-settable properties of the build system, such as system properties in Maven
 * or project properties in Gradle.
 * <p>
 * Profiles map to profiles in Maven, but do not have any effect in Gradle at the moment; this might change.
 * <p>
 * Instances of {@link ProjectActionContext} may be passed to some queries as (part of the) parameters explicitly. In case
 * the API does not support such flexibility directly, the implementation(s) may - the context may be published for the
 * query computation using {@link #apply}; the implementation may then use {@link #find} to obtain a {@link ProjectActionContext}
 * effective for that project.
 * <p>
 * <b>Important note:</b> Not all project queries support {@link ProjectActionContext}. Queries which do should mention that
 * fact in their documentation, or expose it in their APIs.
 * 
 * @author sdedic
 * @since 1.89
 */
public final class ProjectActionContext {
    private final Project project;

    ProjectActionContext(Project project) {
        this.project = project;
    }
   
    /**
     * The project action.
     */
    private String projectAction;
    
    /**
     * The desired project configuration.
     */
    private ProjectConfiguration configuration;
    
    /**
     * Specific property values.
     */
    private Map<String, String> properties;
    
    /**
     * Specific profiles to be included.
     */
    private Set<String> profiles;
    
    /**
     * Specific Lookup contents that may modify the processing.
     */
    private Lookup lookup = Lookup.EMPTY;

    /**
     * Returns the project this context applies to. Having {@link Project} as a property allows multiple
     * contexts, individually for each project, to be active.
     * @return the target project.
     */
    public @NonNull Project getProject() {
        return project;
    }

    /**
     * Additional information for the underlying project implementation, similar to 
     * context lookup in {@link org.netbeans.spi.project.ActionProvider#invokeAction}.
     * Project service implementors may optimize, if the returned value is {@link Lookup#EMPTY}
     * reference.
     * 
     * @return Lookup with additional information.
     */
    public Lookup getLookup() {
        return lookup;
    }

    /**
     * The project action in whose context the project queries are performed. May be
     * left unspecified, so the implementation can choose an appropriate default behaviour.
     * 
     * @return project aciton or {@code null} for unspecified.
     */
    public @CheckForNull String getProjectAction() {
        return projectAction;
    }

    /**
     * The project configuration to use for the project query. Can be {@code null} to 
     * indicate the project's default Configuration should be used.
     * 
     * @return the project's configuration or {@code null}.
     */
    public @CheckForNull ProjectConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * User-customized properties that should be effective during the computation. The same
     * customization should be made during project action execution to obtain the same results as 
     * evaluated by the query. If none specific properties are present, an empty map is returned.
     * 
     * @return user properties
     */
    public @NonNull Map<String, String> getProperties() {
        return properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(properties);
    }

    /**
     * Profiles or some project system features/tags that should be applied for the evaluation. If no
     * specific profiles are set an empty set is returned.
     * @return applied additional profiles.
     */
    public @NonNull Set<String> getProfiles() {
        return profiles == null ? Collections.emptySet() : Collections.unmodifiableSet(profiles);
    }
    
    /**
     * Creates a Builder to create a similar ProjectActionContext to this one. All
     * settings are copied to the Builder, so just calling {@link Builder#context()} on
     * the result will produce a copy of this {@link ProjectActionContext}.
     * @return preconfigured {@link Builder} instance.
     */
    public @NonNull Builder newDerivedBuilder() {
        return newBuilder(project)
                .forProjectAction(projectAction)
                .useConfiguration(configuration)
                .withProfiles(profiles)
                .withProperties(properties);
    }
    
    /**
     * Creates a new {@link ProjectActionContext} builder for the given project.
     * @param p the project
     * @return the builder instance
     */
    public static @NonNull Builder newBuilder(Project p) {
        return new Builder(p);
    }

    /**
     * Builder used to construct the {@link ProjectActionContext}.
     */
    public static final class Builder {
        private ProjectActionContext ctx;
        
        Builder(Project p) {
            ctx = new ProjectActionContext(p);
        }
        
        
        /**
         * Specifies a Lookup to be included in the context. To remove 
         * lookup customizations, pass in {@link Lookup#EMPTY} value.
         * 
         * @param lkp Lookup instance
         * @return the builder
         */
        public Builder withLookup(Lookup lkp) {
            Parameters.notNull("lkp", lkp);
            if (ctx.lookup == lkp) {
                return this;
            }
            ctx.lookup = lkp;
            return this;
        }
        
        // PENDING: if a pattern of ADDING lookups emerges, addLookup() could
        // be added that uses ProxyLookup.Controller to merge the lookups.
        // not done ATM.
        
        /**
         * Specifies the intended project action. {@code null} (the default)
         * means an unspecified action.
         * 
         * @param projectAction project action.
         * @return the builder instance
         */
        public @NonNull Builder forProjectAction(String projectAction) {
            ctx.projectAction = projectAction;
            return this;
        }

        /**
         * Binds to a specific {@link ProjectConfiguration}, which must resolve to an instance of
         * {@link MavenConfiguration}.
         * @param configuration
         * @return builder instance
         */
        public @NonNull Builder useConfiguration(ProjectConfiguration configuration) {
            ctx.configuration = configuration;
            return this;
        }

        /**
         * Uses specific user properties for the query computation.
         * @param properties user properties.
         * @return builder instance
         */
        public @NonNull Builder withProperties(Map<String, String> properties) {
            if (properties == null) {
                return this;
            }
            if (ctx.properties == null) {
                ctx.properties = new HashMap<>();
            }
            ctx.properties.putAll(properties);
            return this;
        } 

        /**
         * Use specific build system profiles or tags for the query evaluation. 
         * @param profiles applied profile(s).
         * @return builder instance
         */
        public @NonNull Builder withProfiles(Collection<String> profiles) {
            if (profiles == null) {
                return this;
            }
            if (ctx.profiles == null) {
                ctx.profiles = new HashSet<>();
            }
            ctx.profiles.addAll(profiles);
            return this;
        }

        /**
         * Uses specific property value for the query computation.
         * @param n property name
         * @param v property value
         * @return builder instance
         */
        public @NonNull Builder withProperty(String n, String v) {
            if (ctx.properties == null) {
                ctx.properties = new HashMap<>();
            }
            ctx.properties.put(n, v);
            return this;
        }
        
        /**
         * Use specific build system profiles or tags for the query evaluation. 
         * @param profiles applied profile(s).
         * @return builder instance
         */
        public @NonNull Builder withProfiles(String... profiles) {
            return withProfiles(Arrays.asList(profiles));
        }
        
        /**
         * @return the configured {@link ProjectActionContext}
         */
        public @NonNull ProjectActionContext context() {
            return ctx;
        }
    }
    
    /**
     * Find the ProjectActionContext for the project. If no context was specified explicitly, 
     * @param p the project in question
     * @return the context
     */
    @NonNull
    public static ProjectActionContext find(Project p) {
        for (ProjectActionContext pac : Lookup.getDefault().lookupAll(ProjectActionContext.class)) {
            if (p == pac.getProject()) {
                return pac;
            }
        }
        return new ProjectActionContext(p);
    }
    
    /**
     * Executes a query using this project context. Other contexts may be specified as well (optional).
     * During the passed {@link Runnable}, or tasks initiated from the Runnable, the {@link #find} method
     * will find the appropriate {@link ProjectActionContext} specified as parameter.
     * 
     * @param r the code to execute.
     * @param otherProjectContexts optional instances for other projects
     */
    public void apply(Runnable r, ProjectActionContext... otherProjectContexts) {
        Lookup add;
        if (otherProjectContexts == null || otherProjectContexts.length == 0) {
            add = Lookups.fixed(this);
        } else {
            ProjectActionContext[] all = Arrays.copyOf(otherProjectContexts, otherProjectContexts.length + 1);
            all[all.length - 1] = this;
            add = Lookups.fixed(all);
        }
        Lookup localDefLookup = new ProxyLookup(add, Lookup.getDefault());
        Lookups.executeWith(localDefLookup, r);
    }
    
    
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
    
    /**
     * Executes a query using this project context. Other contexts may be specified as well (optional).
     * During the passed {@link Runnable}, or tasks initiated from the Runnable, the {@link #find} method
     * will find the appropriate {@link ProjectActionContext} specified as parameter.
     * 
     * @param r the code to execute.
     * @param <V> the return type
     * @param <E> exception thrown from the executed code.
     * @param otherProjectContexts optional instances for other projects
     */
    public <V, E extends Exception> V apply(ProjectCallback<V, E> r, ProjectActionContext... otherProjectContexts) throws E {
        Lookup add;
        if (otherProjectContexts == null || otherProjectContexts.length == 0) {
            add = Lookups.fixed(this);
        } else {
            ProjectActionContext[] all = Arrays.copyOf(otherProjectContexts, otherProjectContexts.length + 1);
            all[all.length - 1] = this;
            add = Lookups.fixed(all);
        }
        Lookup localDefLookup = new ProxyLookup(add, Lookup.getDefault());
        Object[] res = new Object[1];
        Exception[] t = new Exception[1];
        
        Lookups.executeWith(localDefLookup, () -> {
            try {
                res[0] = r.call();
            } catch (Error | RuntimeException td) {
                throw td;
            } catch (Exception ex) {
                t[0] = ex;
            }
        });
        if (t[0] != null) {
            sneakyThrow(t[0]);
            // never reached
            return null;
        } else {
            return (V)res[0];
        }
    }
    
    /**
     * Functional callback interface to be used with {@link #apply(org.netbeans.modules.project.dependency.ProjectActionContext.ProjectCallback, org.netbeans.modules.project.dependency.ProjectActionContext...) }
     * @param <V> value produced by the callback
     * @param <E> exception thrown by the callback
     */
    @FunctionalInterface
    public interface ProjectCallback<V,E extends Exception> extends Callable<V> {
        /**
         * Performs the project operation, returning a value. The method may throw one
         * checked exception.
         * 
         * @return the operation's result
         * @throws E on failure.
         */
        public V call() throws E;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.project);
        hash = 59 * hash + Objects.hashCode(this.projectAction);
        hash = 59 * hash + Objects.hashCode(this.configuration);
        hash = 59 * hash + Objects.hashCode(this.properties);
        hash = 59 * hash + Objects.hashCode(this.profiles);
        hash = 59 * hash + Objects.hashCode(this.lookup);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjectActionContext other = (ProjectActionContext) obj;
        if (!Objects.equals(this.projectAction, other.projectAction)) {
            return false;
        }
        if (!Objects.equals(this.project, other.project)) {
            return false;
        }
        if (!Objects.equals(this.configuration, other.configuration)) {
            return false;
        }
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        if (!Objects.equals(this.lookup, other.lookup)) {
            return false;
        }
        return Objects.equals(this.profiles, other.profiles);
    }
}
