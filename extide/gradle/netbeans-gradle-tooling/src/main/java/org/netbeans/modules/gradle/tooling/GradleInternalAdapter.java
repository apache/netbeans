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
package org.netbeans.modules.gradle.tooling;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.PluginManagerInternal;
import org.gradle.api.internal.plugins.PluginRegistry;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.provider.PropertyInternal;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.provider.ValueSupplier;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.plugin.use.PluginId;
import org.gradle.util.VersionNumber;
import org.netbeans.modules.gradle.tooling.NbProjectInfoBuilder.ExceptionCallable;
import org.netbeans.modules.gradle.tooling.NbProjectInfoBuilder.ValueAndType;

/**
 * Adapts to various Gradle implementations. As *.internal.* interfaces may change between
 * releases, operations on them may be customized by this Adapter. The adapter should be compiled
 * against the Gradle distribution shipped with NetBeans, but should use reflection to access 
 * the relevant data if the internal API changes between versions.
 * 
 * @author sdedic
 */
public class GradleInternalAdapter {
    private static final Logger LOG =  Logging.getLogger(NbProjectInfoBuilder.class);

    private final Project project;
    private final VersionNumber gradleVersion;
    /**
     * Accummulates error messages, so that just one problem is logger a given type of error.
     */
    private Set<String> reportedIncompatibilities = new HashSet<>();
    
    protected NbProjectInfoModel model;
    
    /**
     * Guards {@link #pluginManager} and {@link #registry} initialization
     */
    protected boolean pluginsInitialized;
    protected PluginManagerInternal pluginManager;
    protected PluginRegistry registry;

    public GradleInternalAdapter(Project project) {
        this.project = project;
        this.gradleVersion = VersionNumber.parse(project.getGradle().getGradleVersion());
    }
    
    boolean initPlugins() {
        if (!pluginsInitialized) {
            if (project.getPluginManager() instanceof PluginManagerInternal) {
                pluginManager = (PluginManagerInternal)project.getPluginManager();
            }
            if (project instanceof ProjectInternal) {
                registry = safeCall(() -> ((ProjectInternal)project).getServices().get(PluginRegistry.class), "plugin registry").orElse(null);
            } else {
                registry = null;
            }
        }
        return pluginManager != null;
    }
    
    public void setModel(NbProjectInfoModel model) {
        this.model = model;
    }
    
    protected boolean isFixedValue(String description, ValueSupplier.ExecutionTimeValue etv) {
        return etv.isFixedValue();
    }
    
    public boolean isMutableType(Object potentialValue) {
        if (potentialValue instanceof PropertyInternal) {
            return true;
        } else if ((potentialValue instanceof NamedDomainObjectContainer) && (potentialValue instanceof HasPublicType)) {
            return true;
        } else if (potentialValue instanceof Iterable || potentialValue instanceof Map) {
            return true;
        }
        return false;
    }
    
    public boolean hasPluginManager() {
        return initPlugins();
    }
    
    public ValueAndType findPropertyValueInternal(String propName, Object val) {
        return sinceGradleOrDefault("6.4",() -> safeCall(() -> {
            if (val instanceof ProviderInternal) {
                ProviderInternal provided = (ProviderInternal)val;
                ValueSupplier.ExecutionTimeValue etv;
                try {
                    etv = provided.calculateExecutionTimeValue();
                } catch (RuntimeException ex) {
                    // probably expected, ignore
                    return new ValueAndType(provided.getType());
                }
                if (isFixedValue("property " + propName, etv)) {
                    Object fixed = etv.getFixedValue();
                    Class t = provided.getType();
                    if (t == null && fixed != null) {
                        t = fixed.getClass();
                    }
                    return new ValueAndType(t, fixed);
                } else {
                    return new ValueAndType(provided.getType());
                }
            } else {
                return new ValueAndType(val != null ? val.getClass() : null, val);
            }
        }, "property " + propName).orElse(null), null);
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable exception) throws T {
            throw (T) exception;
    }        
    
    private <T, E extends Throwable> T sinceGradleOrDefault(String version, NbProjectInfoBuilder.ExceptionCallable<T, E> c, Supplier<T> def) {
        if (gradleVersion.compareTo(VersionNumber.parse(version)) >= 0) {
            try {
                return c.call();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                sneakyThrow(t);
                return null;
            }
        } else {
            return def.get();
        }
    }

    public Optional<PluginId> findPluginId(Class fc) {
        if (!initPlugins()) {
            return Optional.empty();
        }
        // with Gradle 7.1+, plugins can be better enumerated. Prior to 7.1 I can only get IDs for registry-supplied plugins.
        Optional<PluginId> id = sinceGradleOrDefault("7.1", () -> safeCall(() -> (PluginId)pluginManager.findPluginIdForClass(fc).orElse(null), "plugins"), Optional::empty); // NOI18N
        if (id.isPresent() || registry == null) {
            return id;
        }
        return safeCall(() -> registry.findPluginForClass(fc).orElse(null), "plugin class " + fc.getName());
    }
    
    private void noteAndLogError(Throwable ex, String description) {
        String msg = "Error inspecting " + (description == null ? "project" : description);
        model.noteProblem(msg + ": " + ex.toString());
        LOG.log(LogLevel.LIFECYCLE, msg, ex);
    }
    
    protected <T, E extends Throwable> Optional<T> safeCall(ExceptionCallable<T, E> sup, String description) {
        try {
            return Optional.ofNullable(sup.call());
        } catch (RuntimeException ex) {
            noteAndLogError(ex, description);
            return Optional.empty();
        } catch (Error ex) {
            if (reportedIncompatibilities.add(ex.toString())) {
                noteAndLogError(ex, description);
            }
            return Optional.empty();
        } catch (Throwable t) {
            sneakyThrow(t);
            return null;
        }
    }

    public static class Gradle76 extends GradleInternalAdapter {
        private static Optional<Method> refHasValue;

        public Gradle76(Project project) {
            super(project);
        }
        
        @Override
        protected boolean isFixedValue(String description, ValueSupplier.ExecutionTimeValue etv) {
            if (refHasValue == null) {
                refHasValue = safeCall(() -> ValueSupplier.ExecutionTimeValue.class.getMethod("hasFixedValue"), "Gradle 7.6+ ExecutionTimeValue");
            }
            if (refHasValue.isPresent()) {
                return safeCall(() -> (Boolean)refHasValue.get().invoke(etv), description).orElse(false);
            } else {
                return false;
            }
        }
    }
}
