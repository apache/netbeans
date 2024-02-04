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

import groovy.lang.GroovySystem;
import groovy.lang.MetaBeanProperty;
import groovy.lang.MetaClass;
import groovy.lang.MetaProperty;
import groovy.lang.MissingPropertyException;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.metaclass.MultipleSetterProperty;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.PublishArtifactSet;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Category;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtensionsSchema.ExtensionSchema;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.testing.Test;
import org.gradle.internal.resolve.ArtifactResolveException;
import org.gradle.jvm.JvmLibrary;
import org.gradle.jvm.toolchain.JavaCompiler;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.language.java.artifact.JavadocArtifact;
import org.gradle.plugin.use.PluginId;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.util.GradleVersion;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo.Report;

/**
 *
 * @author Laszlo Kishalmi
 */
class NbProjectInfoBuilder {
    
    /**
     * The logger. Use Gradle logging - use {@code lifecycle} level for messages that should
     * be printed regularly, and {@code info} for verbose debug messages. This is because if
     * debug loglevel is enabled, gradle will spit out enormous number of diagnostics. This
     * Plugin is not for 'end-user' use anyway, so INFO level is enabled when the logging level gradle
     * project loader is enabled to FINER level.
     */
    private static final Logger LOG =  Logging.getLogger(NbProjectInfoBuilder.class);
    
    /**
     * Maximum recursion depth into structures, arrays and maps. If this depth is reached, the 
     * builder will record a warning and stop descending.
     */
    private static final int MAX_INTROSPECTION_DEPTH = 25;
    
    /**
     * If a warning is recorded during introspection, further warnings will
     * be suppressed until the introspector goes up to this level.
     */
    private static final int INTROSPECTION_RESET_DEPTH_WARNING = 5;

    /**
     * Name of the default extensibility point for various domain objects defined by Gradle.
     */
    private static final String DEFAULT_EXTENSION_NAME = "ext"; // NOI18N
    
    private static final String NB_PREFIX = "netbeans.";
    private static final Set<String> CONFIG_EXCLUDES = new HashSet<>(asList( new String[]{
        "archives",
        "checkstyle",
        "classycle",
        "codenarc",
        "findbugs",
        "findbugsPlugins",
        "jacocoAgent",
        "jacocoAnt",
        "jdepend",
        "pmd",
        ".*DependenciesMetadata"
    }));
    
    private static final Pattern CONFIG_EXCLUDES_PATTERN = Pattern.compile(
        CONFIG_EXCLUDES.stream().reduce("", (s1, s2) -> s1 + "|" + s2)
    );

    private static final Set<String> RECOGNISED_PLUGINS = new HashSet<>(asList(new String[]{
        "antlr",
        "application",
        "base",
        "checkstyle",
        "com.android.application",
        "com.android.library",
        "com.github.lkishalmi.gatling",
        "distribution",
        "ear",
        "findbugs",
        "groovy",
        "groovy-base",
        "io.micronaut.application",
        "ivy-publish",
        "jacoco",
        "java",
        "java-base",
        "java-library-distribution",
        "java-platform",
        "maven",
        "maven-publish",
        "org.jetbrains.kotlin.js",
        "org.jetbrains.kotlin.jvm",
        "org.jetbrains.kotlin.android",
        "org.springframework.boot",
        "osgi",
        "play",
        "pmd",
        "scala",
        "scala-base",
        "war"
    }));

    private static final GradleVersion GRADLE_VERSION = GradleVersion.current().getBaseVersion();

    final Project project;
    final GradleInternalAdapter adapter;

    public static final class ValueAndType {
        final Class type;
        final Optional<Object> value;

        public ValueAndType(Class type, Object value) {
            this.type = type;
            this.value = Optional.ofNullable(value);
        }

        public ValueAndType(Class type) {
            this.type = type;
            this.value = Optional.empty();
        }
    }

    NbProjectInfoBuilder(Project project) {
        this.project = project;
        this.adapter = sinceGradleOrDefault("7.6", () -> new GradleInternalAdapter.Gradle76(project), () -> new GradleInternalAdapter(project));
    }
    
    private NbProjectInfoModel model = new NbProjectInfoModel();

    public NbProjectInfo buildAll() {
        adapter.setModel(model);
        
        runAndRegisterPerf(model, "meta", this::detectProjectMetadata);
        detectProps(model);
        detectLicense(model);
        runAndRegisterPerf(model, "plugins", this::detectPlugins);
        runAndRegisterPerf(model, "sources", this::detectSources);
        detectTests(model);
        runAndRegisterPerf(model, "dependencies", this::detectDependencies);
        runAndRegisterPerf(model, "artifacts", this::detectArtifacts);
        detectDistributions(model);
        
        // introspection is only allowed for gradle 7.4 and above.
        // TODO: investigate if some of the instrospection could be done for earlier Gradles.
        sinceGradle("7.0", () -> {
            initIntrospection();
            
            runAndRegisterPerf(model, "detectExtensions", this::detectExtensions);
            runAndRegisterPerf(model, "detectPlugins2", this::detectAdditionalPlugins);
            runAndRegisterPerf(model, "taskDependencies", this::detectTaskDependencies);
            runAndRegisterPerf(model, "taskProperties", this::detectTaskProperties);
        });
        runAndRegisterPerf(model, "artifacts", this::detectConfigurationArtifacts);
        storeGlobalTypes(model);
        return model;
    }
    
    @SuppressWarnings("null")
    private void detectDistributions(NbProjectInfoModel model) {
        if (project.getPlugins().hasPlugin("distribution")) {
            DistributionContainer distributions = project.getExtensions().findByType(DistributionContainer.class);
            model.getInfo().put("distributions", storeSet(distributions.getNames()));
        }
    }

    @SuppressWarnings("null")
    private void detectLicense(NbProjectInfoModel model) {
        String license = project.hasProperty("netbeans.license") ? project.property("netbeans.license").toString() : null;
        if (license == null) {
            license = project.hasProperty("license") ? project.property("license").toString() : null;
        }
        model.getInfo().put("license", license);
    }
    
    private void addTypes(Class c, Set<Class> types) {
        if (c == null || !types.add(c)) {
            return;
        }
        if (c.getSuperclass() != Object.class) {
            addTypes(c.getSuperclass(), types);
        }
        for (Class i : c.getInterfaces()) {
            addTypes(i, types);
        }
    }
    
    private String getTaskInheritance(Task t) {
        Class nonDecorated = findNonDecoratedClass(t.getClass());
        Set<Class> classes = new HashSet<>();
        addTypes(nonDecorated, classes);
        return classes.stream().map(Class::getName).sorted().collect(Collectors.joining(","));
    }
   
    private static final Set<String> EXCLUDE_TASK_PROPERTIES = new HashSet<>(Arrays.asList(
        "dependsOn",
        "project",
        "actions",
        "taskDependencies",
        "dependsOn",
        "ant",
        "logger",
        "logging",
        "outputs",
        "destroyables",
        "mustRunAfter",
        "finalizedBy",
        "shouldRunAfter",
        "enabled",
        "description",
        "group"
    ));
    
    private void detectTaskProperties(NbProjectInfoModel model) {
        Map<String, Object> taskProperties = new HashMap<>();
        Map<String, String> taskPropertyTypes = new HashMap<>();
        
        // make a copy of the task map; may mutate.
        for (Task task : new ArrayList<>(project.getTasks().getAsMap().values())) {
            Class taskClass = task.getClass();
            Class nonDecorated = findNonDecoratedClass(taskClass);
            
            taskPropertyTypes.put(task.getName(), nonDecorated.getName());
            startInspectObjectAndValues(taskClass, task, task.getName() + ".", globalTypes, taskPropertyTypes, taskProperties, EXCLUDE_TASK_PROPERTIES, true); // NOI18N
        }
        
        model.getInfo().put("tasks.propertyValues", taskProperties); // NOI18N
        model.getInfo().put("tasks.propertyTypes", taskPropertyTypes); // NOI18N
    }
    
    private void detectTaskDependencies(NbProjectInfoModel model) {
        Map<String, Object> tasks = new HashMap<>();
        
        // make a copy of the task map; may mutate.
        for (Task task : new ArrayList<>(project.getTasks().getAsMap().values())) {
            Map<String, String> taskInfo = new HashMap<>();
            taskInfo.put("name", task.getPath()); // NOI18N
            taskInfo.put("enabled", Boolean.toString(task.getEnabled())); // NOI18N
            taskInfo.put("mustRunAfter", dependenciesAsString(task, task.getMustRunAfter())); // NOI18N
            taskInfo.put("shouldRunAfter", dependenciesAsString(task, task.getShouldRunAfter())); // NOI18N
            taskInfo.put("taskDependencies", dependenciesAsString(task, task.getTaskDependencies())); // NOI18N
            
            Class taskClass = task.getClass();
            Class nonDecorated = findNonDecoratedClass(taskClass);
            
            taskInfo.put("type", nonDecorated.getName()); // NOI18N
            taskInfo.put("inherits", getTaskInheritance(task)); // NOI18N

            tasks.put(task.getName(), taskInfo);
        }
        
        model.getInfo().put("taskDetails", tasks); // NOI18N
    }
    
    private String dependenciesAsString(Task t, TaskDependency td) {
        try {
            Set<? extends Task> deps = td.getDependencies(t);
            if (deps.isEmpty()) {
                return "";
            }
            return deps.stream().map(Task::getPath).collect(Collectors.joining(","));
        } catch (LinkageError | RuntimeException ex) {
            LOG.warn("Error getting dependencies for task {}: {}", t.getName(), ex.getLocalizedMessage(), ex);
            return "";
        }
    }
    
    private void detectConfigurationArtifacts(NbProjectInfoModel model) {
        List<Configuration> configs = project.getConfigurations()
            .stream()
            .filter(Configuration::isCanBeConsumed)
            .filter(c -> !c.isCanBeResolved())
            .sorted(Comparator.comparing(Configuration::getName))
            .collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        for (Configuration c : configs) {
            PublishArtifactSet publishSet = c.getAllArtifacts();
            if (publishSet.isEmpty()) {
                continue;
            }
            Map<String, Object> confData = new HashMap<>();
            LOG.lifecycle("Configuration {} artifacts:", c.getName());
            for (PublishArtifact a : publishSet) {
                Map<String, String> artData = new HashMap<>();
                artData.put("name", a.getName()); // NOI18N
                artData.put("classifier", a.getClassifier()); // NOI18N
                artData.put("extension", a.getExtension()); // NOI18N
                artData.put("type", a.getType()); // NOI18N
                
                File f = a.getFile();
                LOG.info("\t{}: name: {}, type: {}, classifier: {}, extension: {}", 
                        f.getPath(), a.getName(), a.getType(), a.getClassifier(), a.getExtension());
                Set<? extends Task> tasks = a.getBuildDependencies().getDependencies(null);
                String taskList = tasks.stream().map(t -> t.getName()).collect(Collectors.joining(","));
                artData.put("buildDeps", taskList); // NOI18N
                LOG.info("\tbuilt by: {}", taskList); // NOI18N
                confData.put(f.getPath(), artData);
            }
            data.put(c.getName(), confData);
        }
        model.getInfo().put("configurationArtifacts", data); // NOI18N
    }
    
    /**
     * Relies on PluginManagerInternal.findPluginIdForClass (gradle 7.1) and PluginRegistry.findPluginForClass (gradle 7.0)
     * @param model 
     */
    private void detectAdditionalPlugins(NbProjectInfoModel model) {
        if (!adapter.hasPluginManager()) {
            return;
        }
        LOG.lifecycle("Detecting additional plugins");
        final Set<String> plugins = new LinkedHashSet<>();
        
        project.getPlugins().matching((Plugin p) -> {
            for (Class c = p.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                Optional<PluginId> id = adapter.findPluginId(c);
                if (id.isPresent()) {
                    LOG.info("Plugin: {} -> {}", id.get(), p);
                    plugins.add(id.get().getId());
                    break;
                }
            }
            return false;
        }).toArray(); // force iteration, gradle is lazier than I :)
        // rely on Set to avoid duplicities.
        ((Set)model.getInfo().get("plugins")).addAll(plugins); // NOI18N
    }
    
    private void runAndRegisterPerf(NbProjectInfoModel model, String s, Consumer<NbProjectInfoModel> r) {
        runAndRegisterPerf(model, s, () -> r.accept(model));
    }
    
    private void runAndRegisterPerf(NbProjectInfoModel model, String s, Runnable r) {
        long time = System.currentTimeMillis();
        try {
            r.run();
        } catch (RuntimeException ex) {
            // convert will eventually throw a different exception
            convertOfflineException(ex);
            LOG.debug("Error encountered during {0}: {1}", s, ex);
        } finally {
            long span = System.currentTimeMillis() - time;
            model.registerPerf(s, span);
        }
    }
    
    private void storeGlobalTypes(NbProjectInfoModel model) {
        model.getInfo().put("extensions.globalTypes", globalTypes); // NOI18N
    }
    
    private void initIntrospection() {
        StringBuilder sb = new StringBuilder();
        for (String s : IGNORED_SYSTEM_CLASSES_REGEXP) {
            if (sb.length() > 0) {
                sb.append("|"); // NOI18N
            }
            sb.append(s);
        }
        ignoreClassesPattern = Pattern.compile(sb.toString());
    }
    
    private void detectExtensions(NbProjectInfoModel model) {
        inspectExtensions("", project.getExtensions());
        model.getInfo().put("extensions.propertyTypes", propertyTypes); // NOI18N
        model.getInfo().put("extensions.propertyValues", values); // NOI18N
    }

    /**
     * Ignored properties, which are exposed by Groovy's Metaobject protocol, but should have
     * been hidden.
     */
    private static final Set<String> IGNORED_SYSTEM_PROPERTIES = new HashSet<>(Arrays.asList(
            "asDynamicObject", 
            "convention", 
            "class", 
            "conventionMapping", 
            "extensions", 
            "modelIdentityDisplayName",
            "project", 
            "taskThatOwnsThisObject",
            "additionalMethods",
            "elementsAsDynamicObject",
            "collectionSchema",
            "didWork",
            "onlyIf"
    ));
    
    /**
     * Classes that are ignored during value/type introspection. 
     */
    private static final String[] IGNORED_SYSTEM_CLASSES_REGEXP = {
            "java\\..*",
            "org\\.gradle\\.api\\.file\\..*",
            "org\\.gradle\\.api\\.tasks\\..*",
            "org\\.gradle\\.api\\.reflect\\..*",
            "org\\.gradle\\.api\\.NamedDomainObject.*",
            "org\\.gradle\\.api\\.internal\\..*",
            "org.gradle.api.internal.tasks.DefaultTaskDependency",
            "org\\.gradle\\.api\\.specs\\..*"
    };

    private Class findIterableItemClass(Class clazz) {
        if (clazz == null) {
            return null;
        }
        Map<TypeVariable<?>, Type> parameters = TypeUtils.getTypeArguments(clazz, Iterable.class);
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        for (Map.Entry<TypeVariable<?>, Type> e : parameters.entrySet()) {
            TypeVariable<?> tv = e.getKey();
            if (tv.getGenericDeclaration() == Iterable.class) {
                Type t = e.getValue();
                if (!(t instanceof Class) || t == Object.class) {
                    return null;
                } else {
                    return (Class)t;
                }
            }
        }
        return null;
    }        
    
    public static final String COLLECTION_TYPE_MARKER = "#col"; // NOI18N
    public static final String COLLECTION_TYPE_NAMED = "named"; // NOI18N
    public static final String COLLECTION_TYPE_MAP = "map"; // NOI18N
    public static final String COLLECTION_TYPE_LIST = "list"; // NOI18N
    public static final String COLLECTION_ITEM_MARKER = "#itemType"; // NOI18N
    public static final String COLLECTION_ITEM_PREFIX = COLLECTION_ITEM_MARKER + "."; // NOI18N
    public static final String COLLECTION_CONTENT_PREFIX = "#content"; // NOI18N
    public static final String COLLECTION_KEYS_MARKER = "#keys"; // NOI18N
    
    
    /**
     * Prevents the recursive descent to loop back to an already processed structure. 
     */
    private Map<Object, Boolean> valueIdentities = new IdentityHashMap<>();
    
    private static boolean isPrimitiveOrString(Class c) {
        // Cannot export a Class as an object, the Class may not exist in the netbeans VM.
        if (c == Object.class || c == Class.class) {
            return false;
        }
        String n = c.getName();
        return c.isPrimitive() || n.startsWith("java.lang.");
    }
    
    /**
     * Inspects extension or plugin objects for properties and their types. Object values are inspected recursively.
     * Property values are read; values that are {@link ProviderInternal}s are computed
     * 
     * @param clazz class to inspect
     * @param object the value to inspect and dump property values, possibly {@code null}
     * @param prefix prefix for type and value maps
     * @param globalTypes map to store information for individual types
     * @param propertyTypes
     * @param defaultValues 
     */
    private void startInspectObjectAndValues(Class clazz, Object object, String prefix, Map<String, Map<String, String>> globalTypes, Map<String, String> propertyTypes, Map<String, Object> defaultValues) {
        depth = 0;
        inspectObjectAndValues(clazz, object, prefix, globalTypes, propertyTypes, defaultValues, null, true);
    }
    
    private void startInspectObjectAndValues(Class clazz, Object object, String prefix, Map<String, Map<String, String>> globalTypes, Map<String, String> propertyTypes, Map<String, Object> defaultValues, Set<String> excludes, boolean type) {
        depth = 0;
        inspectObjectAndValues(clazz, object, prefix, globalTypes, propertyTypes, defaultValues, excludes, type);
    }

    private void inspectObjectAndValues(Class clazz, Object object, String prefix, Map<String, Map<String, String>> globalTypes, Map<String, String> propertyTypes, Map<String, Object> defaultValues) {
        inspectObjectAndValues(clazz, object, prefix, globalTypes, propertyTypes, defaultValues, null, true);
    }
    /**
     * The current introspection depth
     */
    private int depth;
    
    /**
     * If true, depth exceeded warnings will not be logged.
     */
    private boolean suppressDepthWarning;
    
    private void inspectObjectAndValues(Class clazz, Object object, String prefix, Map<String, Map<String, String>> globalTypes, Map<String, String> propertyTypes, Map<String, Object> defaultValues, Set<String> excludes, boolean type) {
        if (valueIdentities.put(object, Boolean.TRUE) == Boolean.TRUE) {
            return;
        }
        try {
            if (depth++ >= MAX_INTROSPECTION_DEPTH) {
                if (!suppressDepthWarning) {
                    LOG.warn("Too deep structure, truncating");
                    model.noteProblem(Report.Severity.WARNING, 
                            String.format("Object structure too deep encountered in class %s", clazz),
                            String.format("Object structure is too deep for the project model builder. This is unlikely to affect basic project operations. "
                                    + "Check logs and report this issue. The path for the object: %s, current value: %s", prefix, object));
                    suppressDepthWarning = true;
                }
                return;
            } else if (depth <= INTROSPECTION_RESET_DEPTH_WARNING) {
                suppressDepthWarning = false;
            }
            inspectObjectAndValues0(clazz, object, prefix, globalTypes, propertyTypes, defaultValues, excludes, type);
        } catch (RuntimeException ex) {
            LOG.warn("Error during inspection of {}, value {}, prefix {}", clazz, object, prefix);
            model.noteProblem(ex, true);
        } finally {
            depth--;
            valueIdentities.remove(object);
        }
    }
    
    private boolean isMutableType(Object potentialValue) {
        return adapter.isMutableType(potentialValue);
    }
    
    private void addNestedKeys(String prefix, Map<String, String> propertyTypes, Collection<String> keys) {
            propertyTypes.merge(prefix + COLLECTION_KEYS_MARKER, String.join(";;", keys), (old, add) -> {
                List<String> oldList = new ArrayList<>(Arrays.asList(old.split(";;")));
                List<String> newList = Arrays.asList(add.split(";;"));
                oldList.addAll(newList);
                return String.join(";;", oldList);
            });
    }
    
    private void inspectObjectAndValues0(Class clazz, Object object, String prefix, Map<String, Map<String, String>> globalTypes, Map<String, String> propertyTypes, Map<String, Object> defaultValues, Set<String> excludes, boolean type) {
        Class nonDecorated = findNonDecoratedClass(clazz);
        
        // record object's type first, even though the value may be excluded (i.e. ignored class). Do not add types for collection or map items -- to much clutter.
        String typeKey = prefix;
        if (prefix.endsWith(".")) {
            typeKey = prefix.substring(0, prefix.length() - 1);
        } else {
            typeKey = prefix;
        }
        if (type) {
            if (propertyTypes.putIfAbsent(typeKey, nonDecorated.getName()) != null && object == null) {
                // type already introspected, no value to fetch properties from.
                return;
            }
        }
        
        if (clazz == null || clazz.isEnum()) {
            return;
        }
        if (clazz.isArray() || (excludes == null && ignoreClassesPattern.matcher(clazz.getName()).matches())) {
            // try to dump as a list/map
            dumpValue(object, typeKey, propertyTypes, defaultValues, clazz, null);
            return;
        }

        MetaClass mclazz = GroovySystem.getMetaClassRegistry().getMetaClass(clazz);
        Map<String, String> globTypes = globalTypes.computeIfAbsent(nonDecorated.getName(), cn -> new HashMap<>());
        List<MetaProperty> props = mclazz.getProperties();
        List<String> addedKeys = new ArrayList<>();
        for (MetaProperty mp : props) {
            Class propertyDeclaringClass = null;
            String getterName = null;
            String propName = mp.getName();
            // some properties are added by DSL wrappers, we should better ignore them.
            if ((excludes != null && excludes.contains(propName)) || IGNORED_SYSTEM_PROPERTIES.contains(propName)) {
                continue;
            }
            LOG.info("Inspecting {}.{}", clazz.getName(), propName);
            Class t = mp.getType();
            if (t == Object.class) {
                // MultipleSetter is probably for an overloaded setter
                if (mp instanceof MultipleSetterProperty) {
                    MultipleSetterProperty msp = (MultipleSetterProperty)mp;
                    if (msp.getGetter() == null) {
                        continue;
                    }
                    t = msp.getGetter().getReturnType();
                }
            }

            if (mp instanceof MetaBeanProperty) {
                MetaBeanProperty mbp = (MetaBeanProperty)mp;

                if (mbp.getGetter() == null) {
                    continue;
                }
                
                // PENDING: not all settable stuff has a setter. Some are containers or objects which are always present,
                // and have to be configured using their properties, i.e. java.modularity. But there are TOOOO MANY things
                // that have a getter and are DEFINITELY not meant for a DSL. 
                // Giving up this time, they should be added if a decent way to filter out implementation garbage is found.
                if (mbp.getSetter() == null) {
                    if (object == null) {
                        continue;
                    }
                    Object potentialValue;
                    try {
                        potentialValue = mclazz.getProperty(object, propName);
                    } catch (RuntimeException ex) {
                        // just ignore - the value cannot be obtained
                        continue;
                    }
                    
                    if (!isMutableType(potentialValue)) {
                        continue;
                    }
                }

                getterName = mbp.getGetter().getName();
                propertyDeclaringClass = mbp.getGetter().getDeclaringClass().getTheClass();
            }
            List<Type> typeParameters = null;
            if (propertyDeclaringClass != null && t.getTypeParameters().length > 0) {
                try {
                    Method m = propertyDeclaringClass.getDeclaredMethod(getterName);
                    Type rt = m.getGenericReturnType();
                    if (rt instanceof ParameterizedType) {
                        typeParameters = new ArrayList<>(Arrays.asList(((ParameterizedType)rt).getActualTypeArguments()));
                    }
                } catch (ReflectiveOperationException ex) {
                }
            }
            Object value = null;
            if ((mp.getModifiers() & Modifier.PUBLIC) == 0) {
                continue;
            }
            if (object != null) {
                // Provider must NOT be asked for a value, otherwise it might run a Task in order to compute
                // the value.
                try {
                    value = mclazz.getProperty(object, propName);
                    if (Provider.class.isAssignableFrom(t)) {
                        ValueAndType vt = adapter.findPropertyValueInternal(propName, value);
                        if (vt != null) {
                            if (vt.value.isPresent()) {
                                value = vt.value.get();
                            }
                            if (vt.type != null) {
                                t = vt.type;
                            } else if (typeParameters != null && !typeParameters.isEmpty() && (typeParameters.get(0) instanceof Class)) {
                                // derive the type from the provider's type parameter
                                t = (Class)typeParameters.get(0);
                            } else {
                                // null value with an unspecified type from the provider
                                t = Object.class; 
                            }
                        }
                    }
                } catch (RuntimeException ex) {
                    // just ignore - the property value cannot be obtained
                }
            }
            if (value != null && !(value instanceof Provider)) {
                if (isPrimitiveOrString(value.getClass())) {
                    defaultValues.put(prefix + propName, value);
                } else {
                    try {
                        defaultValues.put(prefix + propName, value.toString());
                    } catch (RuntimeException ex) {
                        // just ignore... some properties cannot be computed at this time, and their toString() attempts to do that.
                        LOG.info("Could not get value of {}", propName, ex);
                    }
                }
            }
            
            addedKeys.add(propName);
            
            String cn = findNonDecoratedClass(t).getName();
            globTypes.put(propName, cn);
            propertyTypes.put(prefix + propName, cn);

            boolean dumped = false;
            
            if (value != null) {
                // attemtp to enumerate membrs of a Container or an Iterable.
                Class itemClass = null;
                if ((value instanceof NamedDomainObjectContainer) && (value instanceof HasPublicType)) {
                    String newPrefix;
                    
                    TypeOf pubType = ((HasPublicType)value).getPublicType();
                    if (pubType != null && pubType.getComponentType() != null) {
                        itemClass = pubType.getComponentType().getConcreteClass();
                    } else {
                        itemClass = findIterableItemClass(pubType.getConcreteClass());
                    }
                    propertyTypes.put(prefix + propName + COLLECTION_TYPE_MARKER, COLLECTION_TYPE_NAMED);
                    propertyTypes.put(prefix + propName, findNonDecoratedClass(t).getName());
                    if (itemClass != null) {
                        propertyTypes.put(prefix + propName + COLLECTION_ITEM_MARKER, itemClass.getName());
                        newPrefix = prefix + propName + COLLECTION_ITEM_PREFIX; // NOI18N
                        inspectObjectAndValues(itemClass, null, newPrefix, globalTypes, propertyTypes, defaultValues);
                    }

                    NamedDomainObjectContainer nc = (NamedDomainObjectContainer)value;
                    Map<String, ?> m = new HashMap<>(nc.getAsMap());
                    dumpContainerProperties(m, prefix + propName, defaultValues);
                    dumped = true;
                } else {
                    dumped = dumpValue(value, prefix + propName, propertyTypes, defaultValues, t, typeParameters);
                }
                
                if (!dumped) {
                    if (!isPrimitiveOrString(t) && !Provider.class.isAssignableFrom(t)) {
                        String newPrefix = prefix + propName + "."; // NOI18N

                        // recursively inspect a structured value.
                        inspectObjectAndValues(t, value, newPrefix, globalTypes, propertyTypes, defaultValues);
                    }
                }
            }
        }
        addNestedKeys(typeKey, propertyTypes, addedKeys);
    }
    
    private void dumpContainerProperties(Map<String, ?> m, String prefix, Map<String, Object> defaultValues) {
        // merge with other properties
        addNestedKeys(prefix, propertyTypes, m.keySet());

        for (Map.Entry<String, ?> it : m.entrySet()) {
            String k = it.getKey();
            String newPrefix = prefix + "." + k + "."; // NOI18N
            Object v = it.getValue();
            if (v == null) {
                defaultValues.put(prefix + "." + k, null); // NOI18N
            } else {
                defaultValues.put(prefix + "." + k, objectToString(v)); // NOI18N
                inspectObjectAndValues(v.getClass(), v, newPrefix, globalTypes, propertyTypes, defaultValues, null, false);
            }
        }
    }
    
    private boolean dumpValue(Object value, String prefix, Map<String, String> propertyTypes, Map<String, Object> defaultValues, Class t, List<Type> typeParameters) {
        // attemtp to enumerate membrs of a Container or an Iterable.
        Class itemClass = null;
        String cn = null;
        boolean dumped = false;
        if ((value instanceof NamedDomainObjectContainer) && (value instanceof HasPublicType)) {
            String newPrefix;

            TypeOf pubType = ((HasPublicType)value).getPublicType();
            if (pubType != null && pubType.getComponentType() != null) {
                itemClass = pubType.getComponentType().getConcreteClass();
            } else {
                itemClass = findIterableItemClass(pubType.getConcreteClass());
            }
            propertyTypes.put(prefix + COLLECTION_TYPE_MARKER, COLLECTION_TYPE_NAMED);
            if (itemClass != null) {
                propertyTypes.put(prefix + COLLECTION_ITEM_MARKER, itemClass.getName());
                newPrefix = prefix + COLLECTION_ITEM_PREFIX; // NOI18N
                inspectObjectAndValues(itemClass, null, newPrefix, globalTypes, propertyTypes, defaultValues);
            }

            NamedDomainObjectContainer nc = (NamedDomainObjectContainer)value;
            Map<String, ?> m = new HashMap<>(nc.getAsMap());
            List<String> ss = new ArrayList<>(m.keySet());
            propertyTypes.put(prefix + COLLECTION_KEYS_MARKER, String.join(";;", ss));
            for (String k : m.keySet()) {
                newPrefix = prefix + "." + k + "."; // NOI18N
                Object v = m.get(k);
                if (v == null) {
                    defaultValues.put(prefix + "." + k, null); // NOI18N
                } else if (!v.equals(value)) {
                    defaultValues.put(prefix + "." + k, objectToString(v)); // NOI18N
                    inspectObjectAndValues(v.getClass(), v, newPrefix, globalTypes, propertyTypes, defaultValues, null, false);
                }
            }
            dumped = true;
        } else if (Iterable.class.isAssignableFrom(t)) {
            itemClass = findIterableItemClass(t);
            if (itemClass == null && typeParameters != null && !typeParameters.isEmpty()) {
                if (typeParameters.get(0) instanceof Class) {
                    itemClass = (Class)typeParameters.get(0);
                }
            }

            propertyTypes.put(prefix + COLLECTION_TYPE_MARKER, COLLECTION_TYPE_LIST);
            
            if (itemClass != null) {
                cn = findNonDecoratedClass(itemClass).getName();
                propertyTypes.put(prefix + COLLECTION_ITEM_MARKER, cn);
                String newPrefix = prefix + COLLECTION_ITEM_PREFIX; // NOI18N
                if (!cn.startsWith("java.lang.") && !Provider.class.isAssignableFrom(t)) { //NOI18N
                    inspectObjectAndValues(itemClass, null, newPrefix, globalTypes, propertyTypes, defaultValues);
                }
            }

            if (value instanceof Iterable) {
                int index = 0;
                try {
                    for (Object o : (Iterable)value) {
                        String newPrefix = prefix + "[" + index + "]."; // NOI18N
                        if (o == null) {
                            defaultValues.put(prefix + "[" + index + "]", null); //NOI18N
                        } else if (!o.equals(value)) {
                            defaultValues.put(prefix + "[" + index + "]", Objects.toString(o)); //NOI18N
                            inspectObjectAndValues(o.getClass(), o, newPrefix, globalTypes, propertyTypes, defaultValues, null, false);
                        }
                        index++;
                    }
                } catch (RuntimeException ex) {
                    // values cannot be obtained
                }
                dumped = true;
            }
        } else if (value instanceof Map) {
            Map mvalue = (Map)value;
            Set<Object> ks = mvalue.keySet();
            Class keyClass = findIterableItemClass(ks.getClass());
            if (keyClass == null || keyClass == java.lang.Object.class) {
                boolean ok = true;
                for (Object k : ks) {
                    if (!(k instanceof String)) {
                        ok = false;
                    }
                }
                if (ok) {
                    keyClass = String.class;
                }
            }
            if (keyClass == String.class) {
                itemClass = findIterableItemClass(mvalue.values().getClass());

                String keys = ks.stream().map(k -> k.toString()).map((String k) -> k.replace(";", "\\;")).collect(Collectors.joining(";;"));
                propertyTypes.put(prefix + COLLECTION_KEYS_MARKER, keys);

                propertyTypes.put(prefix + COLLECTION_TYPE_MARKER, COLLECTION_TYPE_MAP);
                if (itemClass != null) {
                    cn = findNonDecoratedClass(itemClass).getName();
                    propertyTypes.put(prefix + COLLECTION_ITEM_MARKER, cn);
                    String newPrefix = prefix + COLLECTION_ITEM_PREFIX; // NOI18N
                    if (!cn.startsWith("java.lang.") && !Provider.class.isAssignableFrom(t)) { //NOI18N
                        inspectObjectAndValues(itemClass, null, newPrefix, globalTypes, propertyTypes, defaultValues);
                    }
                }

                for (Object o : ks) {
                    String k = o.toString();
                    String newPrefix = prefix + "[" + k + "]"; // NOI18N
                    Object v = mvalue.get(o);
                    if (v == null) {
                        defaultValues.put(newPrefix, null); // NOI18N
                    } else if (!v.equals(value)) {
                        defaultValues.put(newPrefix, objectToString(v)); // NOI18N
                        inspectObjectAndValues(v.getClass(), v, newPrefix + ".", globalTypes, propertyTypes, defaultValues, null, itemClass == null);
                    }
                }
                dumped = true;
            }
        }
        return dumped;
    }
    
    private static String objectToString(Object o) {
        if (o instanceof Map || o instanceof Iterable) {
            return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
        } 
        try {
            return Objects.toString(o);
        } catch (RuntimeException ex) {
            return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
        }
    }
    
    private static Class findNonDecoratedClass(Class clazz) {
        if (clazz == null || clazz.isInterface()) {
            return clazz;
        }
        while (clazz != Object.class && (clazz.getModifiers() & 0x1000 /* Modifiers.SYNTHETIC */) > 0) {
            clazz = clazz.getSuperclass();
        }
        return clazz;
    }

    /**
     * Regexp that combines names in {@link #IGNORED_SYSTEM_CLASSES_REGEXP}. Computed at the start of {@link #inspectExtensions}
     */
    private Pattern ignoreClassesPattern;
    
    Map<String, Map<String, String>> globalTypes = new HashMap<>();
    Map<String, String> propertyTypes = new HashMap<>();
    Map<String, Object> values = new HashMap<>();

    /**
     * Inspects extension container on an object. The prefix should be name of the object, including "." delimiter, will be used
     * to prefix all properties found.
     * 
     * @param prefix prefix for the extension, including "." tail delimiter
     * @param container extension container to enumerate.
     */
    private void inspectExtensions(String prefix, ExtensionContainer container) {
        for (ExtensionSchema es : container.getExtensionsSchema().getElements()) {
            String extName = es.getName();
            
            LOG.info("Extension: {}{}", prefix, extName);
            
            Object ext;
            try {
                ext = container.getByName(extName);
                if (ext == null) {
                    continue;
                }
            } catch (UnknownDomainObjectException ex) {
                // ignore, the extension could not be obtained, ignore.
                continue;
            }
            
            // special case for 'ext' DefaultProperiesExtension whose properties are 'merged' with the base object
            // can be read in the buildscript as if the object itself defined them
            if (DEFAULT_EXTENSION_NAME.equals(extName) && (ext instanceof ExtraPropertiesExtension)) {
                String p = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
                dumpContainerProperties(((ExtraPropertiesExtension)ext).getProperties(), p, values);
                continue;
            }
            
            Class c = findNonDecoratedClass(ext.getClass());
            propertyTypes.put(prefix + extName, c.getName());
            startInspectObjectAndValues(ext.getClass(), ext, prefix + extName + ".", globalTypes, propertyTypes, values);
            if (ext instanceof ExtensionAware) {
                inspectExtensions(prefix + extName + ".", ((ExtensionAware)ext).getExtensions());  // NOI18N
            }
        }
        List<String> propNames = new ArrayList<>(propertyTypes.keySet());
        Collections.sort(propNames);
        for (String p : propNames) {
            LOG.info("Extension property: {}: {} = {}", p, propertyTypes.get(p), values.get(p));  // NOI18N
        }
    }

    @SuppressWarnings("null")
    private void detectProjectMetadata(NbProjectInfoModel model) {
        model.getInfo().put("project_name", project.getName());
        model.getInfo().put("project_path", project.getPath());
        model.getInfo().put("project_status", project.getStatus());
        if (project.getParent() != null) {
            model.getInfo().put("project_parent_name", project.getParent().getName());
        }
        model.getInfo().put("project_description", project.getDescription());
        model.getInfo().put("project_group", project.getGroup().toString());
        model.getInfo().put("project_version", project.getVersion().toString());
        model.getInfo().put("project_buildDir", project.getBuildDir());
        model.getInfo().put("project_projectDir", project.getProjectDir());
        model.getInfo().put("project_rootDir", project.getRootDir());
        model.getInfo().put("gradle_user_home", project.getGradle().getGradleUserHomeDir());
        model.getInfo().put("gradle_home", project.getGradle().getGradleHomeDir());
        model.getInfo().put("gradle_version", project.getGradle().getGradleVersion());

        Set<Configuration> visibleConfigurations = configurationsToSave();
        model.getInfo().put("configurations", visibleConfigurations.stream().map(conf->conf.getName()).collect(Collectors.toCollection(HashSet::new )));

        Map<String, File> sp = new HashMap<>();
        for(Project p: project.getSubprojects()) {
            sp.put(p.getPath(), p.getProjectDir());
        }
        model.getInfo().put("project_subProjects", sp);
        
        Map<String, File> ib = new HashMap<>();
        LOG.lifecycle("Gradle Version: {}", GradleVersion.current());
        sinceGradle("3.1", () -> {
            for(IncludedBuild p: project.getGradle().getIncludedBuilds()) {
                LOG.lifecycle("Include Build: {}", p.getName());
                ib.put(p.getName(), p.getProjectDir());
            }
        });
        model.getInfo().put("project_includedBuilds", ib);

        sinceGradle("3.3", () -> {
            model.getInfo().put("project_display_name", project.getDisplayName());
        });

        try {
            model.getInfo().put("buildClassPath", storeSet(project.getBuildscript().getConfigurations().getByName("classpath").getFiles()));
        } catch (RuntimeException e) {
            // unexpected exception, build classpath should be available.
            model.noteProblem(e, true);
        }
        Set<String[]> tasks = new HashSet<>();
        for (org.gradle.api.Task t : project.getTasks()) {
            String[] arr = new String[]{t.getPath(), t.getGroup(), t.getName(), t.getDescription()};
            tasks.add(arr);
        }
        model.getInfo().put("tasks", tasks);
    }

    private void detectPlugins(NbProjectInfoModel model) {
        Set<String> plugins = new HashSet<>();
        for (String plugin : RECOGNISED_PLUGINS) {
            if (project.getPlugins().hasPlugin(plugin)) {
                plugins.add(plugin);
            }
        }
        model.getInfo().put("plugins", plugins);
    }

    private void detectTests(NbProjectInfoModel model) {
        Set<File> testClassesRoots = new HashSet<>();
        sinceGradle("4.0", () -> {
            project.getTasks().withType(Test.class).stream().forEach(task -> {
                task.getTestClassesDirs().forEach(dir -> testClassesRoots.add(dir));
            });
        });
        beforeGradle("4.0", () -> {
            project.getTasks().withType(Test.class).stream().forEach(task -> {
                testClassesRoots.add((File) getProperty(task, "testClassesDir"));
            });
        });
        model.getInfo().put("test_classes_dirs", testClassesRoots);

        if (project.getPlugins().hasPlugin("jacoco")) {
            Set<File> coverageData = new HashSet<>();
            project.getTasks().withType(Test.class).stream().forEach(task -> {
                coverageData.add((File) getProperty(task, "jacoco", "destinationFile"));
            });
            model.getInfo().put("jacoco_coverage_files", coverageData);
        }
    }

    private void detectProps(NbProjectInfoModel model) {
        Map<String, String> nbprops = new HashMap<>();
        project.getProperties()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(NB_PREFIX))
                .forEach(e -> nbprops.put(e.getKey().substring(NB_PREFIX.length()), String.valueOf(e.getValue())));
        model.getInfo().put("nbprops", nbprops);
    }
    
    private Path longestPrefixPath(List<Path> files) {
        if (files.size() < 2) {
            return null;
        }
        Path first = files.get(0);
        Path result = null;
        Path root = first.getRoot();
        int count = first.getNameCount();
        for (int i = 1; i <= count; i++) {
            Path match = root != null ? root.resolve(first.subpath(0, i)) : first.subpath(0, i);
            
            for (int pi = 1; pi < files.size(); pi++) {
                Path p = files.get(pi);
                if (!p.startsWith(match)) {
                    return result;
                }
            }
            result = match;
        }
        // if all paths (more than one) are the same, something is strange.
        return null;
    }

    private void detectSources(NbProjectInfoModel model) {
        boolean hasJava = project.getPlugins().hasPlugin("java-base");
        boolean hasGroovy = project.getPlugins().hasPlugin("groovy-base");
        boolean hasScala = project.getPlugins().hasPlugin("scala-base");
        boolean hasKotlin = project.getPlugins().hasPlugin("org.jetbrains.kotlin.android") ||
                            project.getPlugins().hasPlugin("org.jetbrains.kotlin.js") ||
                            project.getPlugins().hasPlugin("org.jetbrains.kotlin.jvm");
        Map<String, Boolean> available = new HashMap<>();
        available.put("java", hasJava);
        available.put("groovy", hasGroovy);
        available.put("kotlin", hasKotlin);
        
        if (hasJava) {
            SourceSetContainer sourceSets = (SourceSetContainer) getProperty(project, "sourceSets");
            if (sourceSets != null) {
                model.getInfo().put("sourcesets", storeSet(sourceSets.getNames()));
                for(SourceSet sourceSet: sourceSets) {
                    String propBase = "sourceset_" + sourceSet.getName() + "_";
                    
                    Set<File> outDirs = new LinkedHashSet<>();
                    sinceGradle("4.0", () -> {
                        // classesDirs is just an iterable
                        for (File dir: (ConfigurableFileCollection) getProperty(sourceSet, "output", "classesDirs")) {
                            outDirs.add(dir);
                        }
                    });
                    beforeGradle("4.0", () -> {
                        outDirs.add((File)getProperty(sourceSet, "output", "classesDir"));
                    });
                    
                    List<Path> outPaths = outDirs.stream().map(File::toPath).collect(Collectors.toList());
                    // find the longest common prefix:
                    Path base = longestPrefixPath(outPaths);
                    
                    for(String lang: new String[] {"JAVA", "GROOVY", "SCALA", "KOTLIN"}) {
                        String langId = lang.toLowerCase();
                        Task compileTask = project.getTasks().findByName(sourceSet.getCompileTaskName(langId));
                        if (compileTask != null) {
                            Object o = null;

                            // try to get from Kotlin options; breaking change in kotlinCompile 1.7+ does not derive from
                            // SourceTask and does not have source/targetCompatibility. It reportedly ignores those options
                            // even before 1.7
                            if ("KOTLIN".equals(lang)) {
                                o = getProperty(compileTask, "kotlinOptions", "languageVersion");
                            } 
                            if (o == null && compileTask.hasProperty("sourceCompatibility")) {
                                o = getProperty(compileTask, "sourceCompatibility");
                            }
                            if (o != null) {
                                model.getInfo().put(
                                    propBase + lang + "_source_compatibility",
                                    o.toString()
                                );
                            }
                            o = null;
                            if ("KOTLIN".equals(lang)) {
                                o = getProperty(compileTask, "kotlinOptions", "jvmTarget");
                            } 
                            if (o == null && compileTask.hasProperty("targetCompatibility")) {
                                o = getProperty(compileTask, "targetCompatibility");
                            }
                            if (o != null) {
                                model.getInfo().put(
                                        propBase + lang + "_target_compatibility",
                                        o.toString()
                                );
                            }
                            
                            sinceGradle("6.7", () -> {
                                fetchJavaInstallationMetadata(compileTask).ifPresent(
                                        (meta) -> model.getInfo().put(propBase + lang + "_compiler_java_home", meta.getInstallationPath().getAsFile())
                                );
                            });
                            
                            List<String> compilerArgs;

                            compilerArgs = (List<String>) getProperty(compileTask, "options", "allCompilerArgs");
                            if (compilerArgs == null) {
                                compilerArgs = (List<String>) getProperty(compileTask, "options", "compilerArgs");
                            }
                            if (compilerArgs == null) {
                                compilerArgs = (List<String>) getProperty(compileTask, "kotlinOptions", "freeCompilerArgs");
                            }
                            model.getInfo().put(propBase + lang + "_compiler_args", new ArrayList<>(compilerArgs));
                        }
                        if (Boolean.TRUE.equals(available.get(langId))) {
                            model.getInfo().put(propBase + lang, storeSet(getProperty(sourceSet, langId, "srcDirs")));
                            asGradle("4.0", "6.1", () -> {
                                File outDir = (File) getProperty(sourceSet, langId, "outputDir");
                                model.getInfo().put(propBase + lang + "_output_classes", outDir);
                            });
                            sinceGradle("6.1", () -> {
                                DirectoryProperty dirProp = (DirectoryProperty)getProperty(sourceSet, langId, "classesDirectory");
                                if (dirProp != null) {
                                    File outDir;

                                    if (dirProp.isPresent()) {
                                        outDir = dirProp.get().getAsFile();
                                    } else {
                                        // kotlin plugin uses some weird late binding, so it has the output item, but it cannot be resolved to a
                                        // concrete file path at this time. Let's make an approximation from
                                        Path candidate = null;
                                        if (base != null) {
                                            Path prefix = base.resolve(langId);
                                            // assume the language has just one output dir in the source set:
                                            for (int i = 0; i < outPaths.size(); i++) {
                                                Path p = outPaths.get(i);
                                                if (p.startsWith(prefix)) {
                                                    if (candidate != null) {
                                                        candidate = null;
                                                        break;
                                                    } else {
                                                        candidate = p;
                                                    }
                                                }
                                            }
                                        }
                                        outDir = candidate != null ? candidate.toFile() : new File("");
                                    }

                                    model.getInfo().put(propBase + lang + "_output_classes", outDir);
                                }
                            });


                        }
                    }
   
                    model.getInfo().put(propBase + "JAVA", storeSet(getProperty(sourceSet, "java", "srcDirs")));
                    model.getInfo().put(propBase + "RESOURCES", storeSet(sourceSet.getResources().getSrcDirs()));
                    if(hasGroovy) {
                        model.getInfo().put(propBase + "GROOVY", storeSet(getProperty(sourceSet, "groovy", "srcDirs")));
                    }
                    if (hasScala) {
                        model.getInfo().put(propBase + "SCALA", storeSet(getProperty(sourceSet, "scala", "srcDirs")));
                    }
                    if (hasKotlin) {
                        model.getInfo().put(propBase + "KOTLIN", storeSet(getProperty(getProperty(sourceSet, "kotlin"), "srcDirs")));
                    }
                    model.getInfo().put(propBase + "output_classes", outDirs);
                    model.getInfo().put(propBase + "output_resources", sourceSet.getOutput().getResourcesDir());
                    sinceGradle("5.2", () -> {
                        model.getInfo().put(propBase + "GENERATED", storeSet(getProperty(sourceSet, "output", "generatedSourcesDirs", "files")));
                    });
                    try {
                        model.getInfo().put(propBase + "classpath_compile", storeSet(sourceSet.getCompileClasspath().getFiles()));
                        model.getInfo().put(propBase + "classpath_runtime", storeSet(sourceSet.getRuntimeClasspath().getFiles()));
                    } catch(Exception e) {
                        convertOfflineException(e);
                        // will not be reached
                        model.noteProblem(e, false);
                    }
                    sinceGradle("4.6", () -> {
                        try {
                            model.getInfo().put(propBase + "classpath_annotation", storeSet(getProperty(sourceSet, "annotationProcessorPath", "files")));
                        } catch(Exception e) {
                            convertOfflineException(e);
                            // will not be reached
                            model.noteProblem(e, false);
                        }
                        model.getInfo().put(propBase + "configuration_annotation", getProperty(sourceSet, "annotationProcessorConfigurationName"));
                    });
                    beforeGradle("5.0", () -> {
                        if (model.getInfo().get(propBase + "classpath_annotation") == null || ((Collection<?>) model.getInfo().get(propBase + "classpath_annotation")).isEmpty()) {
                            model.getInfo().put(propBase + "classpath_annotation", storeSet(getProperty(sourceSet, "compileClasspath", "files")));
                        }
                    });
                    beforeGradle("7.0", () -> {
                        model.getInfo().put(propBase + "configuration_compile", getProperty(sourceSet, "compileClasspathConfigurationName"));
                        model.getInfo().put(propBase + "configuration_runtime", getProperty(sourceSet, "runtimeClasspathConfigurationName"));
                    });
                }
            } else {
                model.getInfo().put("sourcesets", Collections.emptySet());
                // TODO: should be this converted to Problem, severity INFO ?
                model.noteProblem("No sourceSets found on this project. This project mightbe a Model/Rule based one which is not supported at the moment.");
            }
        }
    }

    private Optional<JavaInstallationMetadata> fetchJavaInstallationMetadata(Task task) {
        Property<JavaLauncher> launcherProperty = (Property<JavaLauncher>) getProperty(task, "javaLauncher");
        if (launcherProperty != null && launcherProperty.isPresent()) {
            return Optional.of(launcherProperty.get().getMetadata());
        }
        Property<JavaCompiler> compilerProperty = (Property<JavaCompiler>) getProperty(task, "javaCompiler");
        if (compilerProperty != null && compilerProperty.isPresent()) {
            return Optional.of(compilerProperty.get().getMetadata());
        }
        return Optional.empty();
    }
    
    private void detectArtifacts(NbProjectInfoModel model) {
        if (project.getPlugins().hasPlugin("java")) {
            model.getInfo().put("main_jar", getProperty(project, "jar", "archivePath"));
        }
        if (project.getPlugins().hasPlugin("war")) {
            model.getInfo().put("main_war", getProperty(project, "war", "archivePath"));
            model.getInfo().put("webapp_dir", getProperty(project, "webAppDir"));
            model.getInfo().put("webxml", getProperty(project, "war", "webXml"));
            try {
                model.getInfo().put("exploded_war_dir", getProperty(project, "explodedWar", "destinationDir"));
            } catch(Exception e) {
                // probably not an internal error, but misconfiguration, omit trace
                model.noteProblem(e, false);
            }
            try {
                model.getInfo().put("web_classpath", getProperty(project, "war", "classpath", "files"));
            } catch(Exception e) {
                // probably not an internal error, but misconfiguration, omit trace
                model.noteProblem(e, false);
            }
        }
        Map<String, Object> archives = new HashMap<>();
        beforeGradle("5.2", () -> {
            // The jar.getCassifier() and jar.getArchievePath() are deprecated since 5.2
            // These methods got removed in 8.0
            project.getTasks().withType(Jar.class).forEach(jar -> {
                archives.put(jar.getClassifier(), jar.getArchivePath());
            });
        });
        sinceGradle("5.2", () -> {
            project.getTasks().withType(Jar.class).forEach(jar -> {
                archives.put(jar.getArchiveClassifier().get(), jar.getDestinationDirectory().file(jar.getArchiveFileName().get()).get().getAsFile());
            });
        });
        model.getInfo().put("archives", archives);
    }

    private static boolean resolvable(Configuration conf) {
        try{
            return (boolean) getProperty(conf, "canBeResolved");
        } catch (MissingPropertyException ex){
            return true;
        }
    }
    
    private static String nonNullString(Object s) {
        return s == null ? "" : s.toString();
    }
    
    /**
     * Walker that collect resolved dependencies. The walker does not work with FLAT list of
     * dependencies from getAllDependencies, but rather with the dependency tree so it can
     * discover the dependency structures. 
     */
    class DependencyWalker {
        final Configuration configuration;
        final boolean ignoreUnresolvable;
        final Map<String, String> unresolvedProblems;
        final Set<String> componentIds;
        final String configName;
        final Set<ComponentIdentifier> ids;
        final Set<String> unresolvedIds;
        final Map<String, Set<String>> directDependencies;
        final Map<String, String> projectIds;
        final Map<String, String> resolvedVersions;
        
        int depth;

        public DependencyWalker(Configuration configuration, 
                boolean ignoreUnresolvable, 
                Map<String, String> unresolvedProblems, 
                Set<String> componentIds, 
                Set<ComponentIdentifier> ids, 
                Set<String> unresolvedIds, 
                Map<String, Set<String>> directDependencies,
                Map<String, String> projectIds, Map<String, String> resolvedVersions) {
            this.configuration = configuration;
            this.ignoreUnresolvable = ignoreUnresolvable;
            this.unresolvedProblems = unresolvedProblems;
            this.componentIds = componentIds;
            this.ids = ids;
            this.unresolvedIds = unresolvedIds;
            this.directDependencies = directDependencies;
            this.projectIds = projectIds;
            this.resolvedVersions = resolvedVersions;
            
            this.configName = configuration.getName();
        }
        
        public void walkResolutionResult(ResolvedComponentResult node) {
            walkChildren(true, "", node.getDependencies(), new HashSet<>());
        }
        
        String findNodeIdentifier(ComponentIdentifier cid) {
            String nodeIdString;
            if (cid instanceof ModuleComponentIdentifier) {
                ModuleComponentIdentifier mid = (ModuleComponentIdentifier)cid;

                nodeIdString = String.format("%s:%s:%s", nonNullString(mid.getGroup()), mid.getModule(), nonNullString(mid.getVersion()));

            } else if (cid instanceof ProjectComponentIdentifier) {
                ProjectComponentIdentifier pid = (ProjectComponentIdentifier)cid;
                String absPath = project.getRootProject().absoluteProjectPath(pid.getProjectPath());
                String rootName = project.getRootProject().getName();
                
                String aid;
                Object g = project.getGroup();
                Object v = project.getVersion();
                String gid = g == null ? "" : g.toString();
                if (project != project.getRootProject()) {
                    aid = rootName + "-" + project.getName();
                } else {
                    aid = project.getName();
                }
                String ver = v == null ? "" : v.toString();
                projectIds.put(absPath, String.format("%s:%s:%s", gid, nonNullString(aid), nonNullString(v)));
                return "*project:" + absPath;
            } else {
                return null;
            }
            
            return nodeIdString;
        }
        
        public void walkResolutionResult(ResolvedDependencyResult node, Set<String> stack) {
            String c = node.getResolvedVariant().getAttributes().getAttribute(Attribute.of("org.gradle.category", String.class)); 
            if (c != null && (Category.REGULAR_PLATFORM.equals(c) || Category.ENFORCED_PLATFORM.equals(c))) {
                // TEMPORARY FIX: do not walk children of BOMs, as they are not real dependencies, just version constraints.
                // better project dependency API needed to handle this.
                return;
            }
            depth++;
            ResolvedComponentResult rcr = node.getSelected();
            String id = findNodeIdentifier(rcr.getId());
            if (!stack.add(id)) {
                return;
            }
            walkChildren(false, id, rcr.getDependencies(), stack);
            stack.remove(id);
            depth--;
        }
        
        public void walkChildren(boolean root, String parentId, Collection<? extends DependencyResult> deps, Set<String> stack) {
            for (DependencyResult it2 : deps) {

                if (it2 instanceof ResolvedDependencyResult) {
                    ResolvedDependencyResult rdr = (ResolvedDependencyResult) it2;
                    ComponentIdentifier id = null;
                    if (rdr.getRequested() instanceof ModuleComponentSelector) {
                        ids.add(rdr.getSelected().getId());
                        // do not bother with components that only select a variant, which is itself a component
                        // TODO: represent as a special component type so the IDE shows it, but the IDE knows it is an abstract
                        // intermediate with no artifact(s).
                        if (rdr.getResolvedVariant() == null) {
                            id = rdr.getSelected().getId();
                        } else {
                            id = sinceGradle("6.8", () -> {
                                if (!rdr.getResolvedVariant().getExternalVariant().isPresent()) {
                                    return rdr.getSelected().getId();
                                } else {
                                    return null;
                                }
                            });
                        }
                    }
                    if (id != null) {
                        componentIds.add(id.toString());
                        if (id instanceof ModuleComponentIdentifier) {
                            ModuleComponentIdentifier mci = (ModuleComponentIdentifier)id;
                            resolvedVersions.putIfAbsent(
                                    String.format("%s:%s", mci.getGroup(), nonNullString(mci.getModuleIdentifier().getName())),
                                    String.format("%s:%s:%s", mci.getGroup(), nonNullString(mci.getModuleIdentifier().getName()), nonNullString(mci.getVersion()))
                            );
                        }
                    }
                    if (directDependencies.computeIfAbsent(parentId, f -> new HashSet<>()).
                        add(findNodeIdentifier(rdr.getSelected().getId()))) {
                        walkResolutionResult(rdr, stack);
                    }
                }
                if (it2 instanceof UnresolvedDependencyResult) {
                    UnresolvedDependencyResult udr = (UnresolvedDependencyResult) it2;
                    String id = udr.getRequested().getDisplayName();
                    if(componentIds.contains(id)) {
                        unresolvedIds.add(id);
                    }
                    if(!ignoreUnresolvable && (configuration.isVisible() || configuration.isCanBeConsumed())) {
                        // hidden configurations like 'testCodeCoverageReportExecutionData' might contain unresolvable artifacts.
                        // do not report problems here
                        Throwable failure = ((UnresolvedDependencyResult) it2).getFailure();
                        convertOfflineException(failure);
                        unresolvedProblems.putIfAbsent(id, ((UnresolvedDependencyResult) it2).getFailure().getMessage());
                    }
                }
            }
        }
    }
    
    private void convertOfflineException(Throwable failure) {
        if (project.getGradle().getStartParameter().isOffline()) {
            // if the unresolvable is bcs. offline mode, throw an exception to get retry in online mode.
            Throwable prev = null;
            for (Throwable t = failure; t != prev && t != null; prev = t, t = t.getCause()) {
                if (t.getMessage().contains("available for offline")) {
                    throw new NeedOnlineModeException("Need online mode", failure);
                }
            }
        }
    }
    
    private void detectDependencies(NbProjectInfoModel model) {
        Set<ComponentIdentifier> ids = new HashSet<>();
        Map<String, File> projects = new HashMap<>();
        Map<String, String> unresolvedProblems = new HashMap<>();
        Map<String, Set<File>> resolvedJvmArtifacts = new HashMap<>();
        Set<Configuration> visibleConfigurations = configurationsToSave();
        Map<String, String> projectIds = new HashMap<>();

        // NETBEANS-5846: if this project uses javaPlatform plugin with dependencies enabled, 
        // do not report unresolved problems
        boolean ignoreUnresolvable = (project.getPlugins().hasPlugin("java-platform") &&
            Boolean.TRUE.equals(getProperty(project, "javaPlatform", "allowDependencies")));

        visibleConfigurations.forEach(it -> {
            String propBase = "configuration_" + it.getName() + "_";
            model.getInfo().put(propBase + "non_resolving", !resolvable(it));
            model.getInfo().put(propBase + "transitive",  it.isTransitive());
            model.getInfo().put(propBase + "canBeConsumed", it.isCanBeConsumed());
            model.getInfo().put(propBase + "extendsFrom",  it.getExtendsFrom().stream().map(c -> c.getName()).collect(Collectors.toCollection(HashSet::new)));
            model.getInfo().put(propBase + "description",  it.getDescription());

            Map<String, String> attributes = new LinkedHashMap<>();
            AttributeContainer attrs = it.getAttributes();
            for (Attribute<?> attr : attrs.keySet()) {
                attributes.put(attr.getName(), String.valueOf(attrs.getAttribute(attr)));
            }
            model.getInfo().put(propBase + "attributes", attributes);
        });
        //visibleConfigurations = visibleConfigurations.findAll() { resolvable(it) }
        visibleConfigurations.forEach(it -> {
            
            Map<String, Set<String>> directDependencies = new HashMap<>();
            Set<String> componentIds = new HashSet<>();
            Set<String> unresolvedIds = new HashSet<>();
            Set<String> projectNames = new HashSet<>();
            Map<String, String> resolvedVersions = new HashMap<>();
            long time_inspect_conf = System.currentTimeMillis();

            it.getDependencies().withType(ModuleDependency.class).forEach(it2 -> {
                String group = it2.getGroup() != null ? it2.getGroup() : "";
                String name = it2.getName();
                String version = it2.getVersion() != null ? it2.getVersion() : "";
                String id = group + ":" + name + ":" + version;
                componentIds.add(id);
            });
            // TODO: what to do with PROJECT dependencies ?

            String configPrefix = "configuration_" + it.getName() + "_";
            if (resolvable(it)) {
                try {
                    // PENDING: this following code is duplicated in DependencyWalker. Currently IDE uses the old
                    // information, the DependencyWalker collects just dependency tree(s), which is used marginally (new feature).
                    // remove the code block in favour to DependencyWalker after stabilization.
                    it.getIncoming().getResolutionResult().getAllDependencies().forEach( it2 -> {
                        if (it2 instanceof ResolvedDependencyResult) {
                            ResolvedDependencyResult rdr = (ResolvedDependencyResult) it2;
                            if (rdr.getRequested() instanceof ModuleComponentSelector) {
                                ids.add(rdr.getSelected().getId());
                                // do not bother with components that only select a variant, which is itself a component
                                // TODO: represent as a special component type so the IDE shows it, but the IDE knows it is an abstract
                                // intermediate with no artifact(s).
                                if (rdr.getResolvedVariant() == null) {
                                    componentIds.add(rdr.getSelected().getId().toString());
                                } else {
                                    sinceGradle("6.8", () -> {
                                        if (!rdr.getResolvedVariant().getExternalVariant().isPresent()) {
                                            componentIds.add(rdr.getSelected().getId().toString());
                                        }
                                    });
                                } 
                            }
                        }
                        if (it2 instanceof UnresolvedDependencyResult) {
                            UnresolvedDependencyResult udr = (UnresolvedDependencyResult) it2;
                            String id = udr.getRequested().getDisplayName();
                            if(componentIds.contains(id)) {
                                unresolvedIds.add(id);
                            }
                            if(!ignoreUnresolvable && (it.isVisible() || it.isCanBeConsumed())) {
                                // hidden configurations like 'testCodeCoverageReportExecutionData' might contain unresolvable artifacts.
                                // do not report problems here
                                Throwable failure = ((UnresolvedDependencyResult) it2).getFailure();
                                convertOfflineException(failure);
                                unresolvedProblems.put(id, ((UnresolvedDependencyResult) it2).getFailure().getMessage());
                            }
                        }
                    });
                    
                    // PENDING: collect components separately from the depth search, so existing
                    // functions are not broken. REMOVE this duplicity after Dependency walker is tested
                    // to work the same as the original code.
                    Set<String> componentIds2 = new HashSet<>();
                    DependencyWalker walker = new DependencyWalker(
                            it, ignoreUnresolvable, unresolvedProblems, componentIds2, ids, unresolvedIds, directDependencies, projectIds, resolvedVersions);

                    walker.walkResolutionResult(it.getIncoming().getResolutionResult().getRoot());
                } catch (ResolveException ex) {
                    model.noteProblem(ex, false);
                }
            } else {
                unresolvedIds.addAll(componentIds);
                componentIds.clear();
            }

            Set<String> directChildSpecs = new HashSet<>();
            it.getDependencies().forEach(d -> {
                StringBuilder sb = new StringBuilder();
                String g;
                String a;
                if (d instanceof ProjectDependency) {
                    sb.append("*project:"); // NOI18N
                    Project other = ((ProjectDependency)d).getDependencyProject();
                    g = other.getGroup().toString();
                    a = other.getName();
                } else {
                    g = d.getGroup();
                    a = d.getName();
                }
                sb.append(g).append(':').append(a).append(":").append(nonNullString(d.getVersion()));
                String id = sb.toString();
                String resolved = resolvedVersions.get(id);
                directChildSpecs.add(resolved != null ? resolved : id);
            });
            model.getInfo().put(configPrefix + "directChildren", directChildSpecs);

            String depBase = "dependency_inspect_" + it.getName();
            String depPrefix = depBase + "_";
            long time_project_deps = System.currentTimeMillis();
            model.registerPerf(depPrefix + "module", time_project_deps - time_inspect_conf);
            it.getDependencies().withType(ProjectDependency.class).forEach(it2 -> {
                Project prj = it2.getDependencyProject();
                projects.put(prj.getPath(), prj.getProjectDir());
                projectNames.add(prj.getPath());
            });
            long time_file_deps = System.currentTimeMillis();
            model.registerPerf(depPrefix + "project", time_file_deps - time_project_deps);
            Set<File> fileDeps = new HashSet<>();
            it.getDependencies().withType(FileCollectionDependency.class).forEach(it2 -> {
                fileDeps.addAll(it2.resolve());
            });
            long time_collect = System.currentTimeMillis();
            model.registerPerf(depPrefix + "file", time_collect - time_file_deps);

            if (resolvable(it)) {
                try {
                    Set<ResolvedArtifact> arts = it.getResolvedConfiguration()
                            .getLenientConfiguration()
                            .getArtifacts();
                    
                    arts.stream().forEach(a -> {
                        if (!(a.getId().getComponentIdentifier() instanceof ProjectComponentIdentifier)) {
                            resolvedJvmArtifacts.putIfAbsent(a.getId().getComponentIdentifier().toString(), Collections.singleton(a.getFile()));
                        }
                    });
                    try {
                        it.getResolvedConfiguration()
                                .getLenientConfiguration()
                                .getFirstLevelModuleDependencies(Specs.SATISFIES_ALL)
                                .forEach(rd -> collectArtifacts(rd, resolvedJvmArtifacts));
                    } catch (ArtifactResolveException ex) {
                        convertOfflineException(ex);
                        // will not be reached, if the exception is converted
                        throw ex;
                    }
                } catch (NullPointerException ex) {
                    //This can happen if the configuration resolution had issues
                }
            }
            long time_report = System.currentTimeMillis();
            model.registerPerf(depPrefix + "collect", time_report - time_collect);
            
            model.getInfo().put(configPrefix + "components", componentIds);
            model.getInfo().put(configPrefix + "projects", projectNames);
            model.getInfo().put(configPrefix + "files", fileDeps);
            model.getInfo().put(configPrefix + "unresolved", unresolvedIds);
            model.getInfo().put(configPrefix + "dependencies", directDependencies);
            model.registerPerf(depPrefix + "file", System.currentTimeMillis() - time_report);
            model.registerPerf(depBase, System.currentTimeMillis() - time_inspect_conf);
        });

        long time_exclude = System.currentTimeMillis();
        visibleConfigurations.stream().forEach(it -> {
            String propBase = "configuration_" + it.getName() + "_";
            Set exclude = new HashSet();
            collectModuleDependencies(model, it.getName(), false, exclude);
            ((Set<String>) model.getInfo().get(propBase + "components")).removeAll(exclude);
            ((Set<String>) model.getInfo().get(propBase + "unresolved")).removeAll(exclude);
            ((Set<String>) model.getInfo().get(propBase + "files")).removeAll(exclude);
        });
        model.registerPerf("excludes", System.currentTimeMillis() - time_exclude);

        model.registerPerf("offline", project.getGradle().getStartParameter().isOffline());

        Map<String, Set<File>> resolvedSourcesArtifacts = new HashMap<>();
        Map<String, Set<File>> resolvedJavadocArtifacts = new HashMap<>();
        if (project.getGradle().getStartParameter().isOffline() || project.hasProperty("downloadSources") || project.hasProperty("downloadJavadoc")) {
            long filter_time = System.currentTimeMillis();
            Set<ComponentIdentifier> filteredIds = ids;
            List artifactTypes = project.getGradle().getStartParameter().isOffline() ? new ArrayList<>(asList(SourcesArtifact.class, JavadocArtifact.class)) : new ArrayList<>();
            if (project.hasProperty("downloadSources")) {
                String filter = (String) getProperty(project, "downloadSources");
                if (!"ALL".equals(filter)) {
                    filteredIds = ids.stream()
                            .filter(id -> id.toString().equals(filter))
                            .collect(Collectors.toSet());
                    model.setMiscOnly(true);
                }
                artifactTypes.add(SourcesArtifact.class);
            }
            if (project.hasProperty("downloadJavadoc")) {
                String filter = (String) getProperty(project, "downloadJavadoc");
                if (!"ALL".equals(filter)) {
                    filteredIds = ids.stream()
                            .filter(id -> id.toString().equals(filter))
                            .collect(Collectors.toSet());
                    model.setMiscOnly(true);
                }
                artifactTypes.add(JavadocArtifact.class);
            }
            long query_time = System.currentTimeMillis();
            model.registerPerf("dependencies_filter", query_time - filter_time);
            ArtifactResolutionResult result = project.getDependencies().createArtifactResolutionQuery()
                    .forComponents(filteredIds)
                    .withArtifacts(JvmLibrary.class, artifactTypes)
                    .execute();
            long collect_time = System.currentTimeMillis();
            model.registerPerf("dependencies_query", collect_time - query_time);

            for (ComponentArtifactsResult component: result.getResolvedComponents()) {
                Set<ArtifactResult> sources = component.getArtifacts(SourcesArtifact.class);
                if (!sources.isEmpty()) {
                    resolvedSourcesArtifacts.put(component.getId().toString(), collectResolvedArtifacts(sources));
                }
                Set<ArtifactResult> javadocs = component.getArtifacts(JavadocArtifact.class);
                if (!javadocs.isEmpty()) {
                    resolvedJavadocArtifacts.put(component.getId().toString(), collectResolvedArtifacts(javadocs));
                }
            }

            model.registerPerf("dependencies_collect", System.currentTimeMillis() - collect_time);
        }
        
        // project abs path -> project's GAV
        model.getInfo().put("project_ids", projectIds);

        model.getExt().put("resolved_jvm_artifacts", resolvedJvmArtifacts);
        model.getExt().put("resolved_sources_artifacts", resolvedSourcesArtifacts);
        model.getExt().put("resolved_javadoc_artifacts", resolvedJavadocArtifacts);
        model.getInfo().put("project_dependencies", projects);
        model.getInfo().put("unresolved_problems", unresolvedProblems);
    }

    private static Set<File> collectResolvedArtifacts(Set<ArtifactResult> res) {
        return res
                .stream()
                .filter(it -> it instanceof ResolvedArtifactResult)
                .map(rar -> ((ResolvedArtifactResult) rar).getFile())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static void collectArtifacts(ResolvedDependency dep, final Map<String, Set<File>> resolvedArtifacts) {
        String key = dep.getModuleGroup() + ":" +dep.getModuleName() + ":" + dep.getModuleVersion();
        if (!resolvedArtifacts.containsKey(key)) {
            resolvedArtifacts.put(key, dep.getModuleArtifacts().stream().map(r -> r.getFile()).collect(Collectors.toCollection(HashSet::new)));
            dep.getChildren().forEach(rd -> collectArtifacts(rd, resolvedArtifacts));
        }
    }

    private void collectModuleDependencies(final NbProjectInfoModel model, String configurationName, boolean includeRoot, final Set deps) {
        String propBase = "configuration_" + configurationName + "_";
        if (includeRoot) {
            deps.addAll((Collection<?>) model.getInfo().get(propBase + "components"));
            deps.addAll((Collection<?>) model.getInfo().get(propBase + "files"));
            if (!model.getInfo().containsKey(propBase + "non_resolving")) {
                deps.addAll((Collection<?>) model.getInfo().get(propBase + "unresolved"));
            }
        }
        ((Collection<String>) model.getInfo().get(propBase + "extendsFrom")).forEach(it -> {
            collectModuleDependencies(model, it, true, deps);
        });
    }

    private static <T extends Serializable> Set storeSet(Object o) {
        if (o == null) {
            return null;
        }
        if(! ( o instanceof Collection)) {
            throw new IllegalStateException("storeSet can only be used with Collections, but was: " + o.getClass().getName());
        }
        Collection c = (Collection) o;
        switch (c.size()) {
            case 0:
                return Collections.emptySet();
            case 1:
                return Collections.singleton(c.iterator().next());
            default:
                return new LinkedHashSet(c);
        }
    }

    private Set<Configuration> configurationsToSave() {
        return project
                .getConfigurations()
                .matching(c -> !CONFIG_EXCLUDES_PATTERN.matcher(c.getName()).matches())
                .stream()
                .flatMap(c -> c.getHierarchy().stream())
                .collect(Collectors.toSet());
    }
    
    interface ExceptionCallable<T, E extends Throwable> {
        public T call() throws E;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable exception) throws T {
            throw (T) exception;
    }        
    
    private <T, E extends Throwable> T sinceGradleOrDefault(String version, ExceptionCallable<T, E> c, Supplier<T> def) {
        if (GRADLE_VERSION.compareTo(GradleVersion.version(version)) >= 0) {
            try {
                return c.call();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                sneakyThrow(t);
                return null;
            }
        } else if (def != null) {
            return def.get();
        } else {
            return null;
        }
    }
    
    private <T, E extends Throwable> T sinceGradle(String version, ExceptionCallable<T, E> c) {
        return sinceGradleOrDefault(version, c, null);
    }
    
    private void sinceGradle(String version, Runnable r) {
        if (GRADLE_VERSION.compareTo(GradleVersion.version(version)) >= 0) {
            r.run();
        }
    }

    private void beforeGradle(String version, Runnable r) {
        if (GRADLE_VERSION.compareTo(GradleVersion.version(version)) < 0) {
            r.run();
        }
    }

    private void asGradle(String fromVersion, String toVersion, Runnable r) {
        if ((GRADLE_VERSION.compareTo(GradleVersion.version(fromVersion)) >= 0)
                && (GRADLE_VERSION.compareTo(GradleVersion.version(toVersion)) < 0)) {
            r.run();
        }
    }

    private static Object getProperty(Object obj, String... propPath) {
        Object currentObject = obj;
        try {
            for(String prop: propPath) {
                currentObject = InvokerHelper.getPropertySafe(currentObject, prop);
            }
            return currentObject;
        } catch (MissingPropertyException ex) {
            return null;
        }
    }
}

