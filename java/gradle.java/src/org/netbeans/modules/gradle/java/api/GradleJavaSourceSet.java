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

package org.netbeans.modules.gradle.java.api;

import org.netbeans.modules.gradle.spi.Utils;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.openide.util.NbBundle.Messages;

public final class GradleJavaSourceSet implements Serializable {

    @Messages({
        "LBL_JAVA=Java",
        "LBL_GROOVY=Groovy",
        "LBL_SCALA=Scala",
        "LBL_KOTLIN=Kotlin",
        "LBL_RESOURCES=Resources",
        "LBL_GENERATED=Generated"
    })
    public static enum SourceType {

        JAVA, GROOVY, SCALA, RESOURCES,
        /** @since 1.8 */
        GENERATED,
        /** @since 1.15 */
        KOTLIN;

        @Override
        public String toString() {
            switch (this) {
                case JAVA: return Bundle.LBL_JAVA();
                case GROOVY: return Bundle.LBL_GROOVY();
                case SCALA: return Bundle.LBL_SCALA();
                case KOTLIN: return Bundle.LBL_KOTLIN();
                case RESOURCES: return Bundle.LBL_RESOURCES();
                case GENERATED: return Bundle.LBL_GENERATED();
            }
            return super.toString();
        }
    }

    public static enum ClassPathType {

        COMPILE, RUNTIME
    }

    public static final String MAIN_SOURCESET_NAME = "main"; //NOI18N
    public static final String TEST_SOURCESET_NAME = "test"; //NOI18N
    private static final String DEFAULT_SOURCE_COMPATIBILITY = "1.5"; //NOI18N

    Map<SourceType, Set<File>> sources = new EnumMap<>(SourceType.class);
    Map<SourceType, File> outputs = new EnumMap<>(SourceType.class);
    String name;
    String runtimeConfigurationName;
    String compileConfigurationName;
    String annotationProcessorConfigurationName;

    Map<SourceType, String> sourcesCompatibility = Collections.emptyMap();
    Map<SourceType, String> targetCompatibility = Collections.emptyMap();
    Map<SourceType, File> compilerJavaHomes = Collections.emptyMap();
    Map<SourceType, List<String>> compilerArgs = Collections.emptyMap();
    boolean testSourceSet;
    Set<File> outputClassDirs;
    File outputResources;
    //Add silent support for webapp docroot.
    File webApp;
    Set<File> annotationProcessorPath;
    Set<File> compileClassPath;
    Set<File> runtimeClassPath;
    Set<GradleJavaSourceSet> sourceDependencies = Collections.emptySet();

    public GradleJavaSourceSet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isTestSourceSet() {
        return testSourceSet;
    }

    /**
     * This method returns the Java source compatibility defined for this source
     * set.
     *
     * @deprecated Use {@link #getSourcesCompatibility(org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType)} instead.
     * @return
     */
    @Deprecated
    public String getSourcesCompatibility() {
        return getSourcesCompatibility(SourceType.JAVA);
    }

    /**
     * This method returns the source compatibility defined for this source
     * set for the given language type.
     *
     * The value is actually extracted from the compiler task defined for
     * this source set and language type. If that cannot be determined for some
     * reason this method returns "1.5".
     *
     * @since 1.4
     * @param type
     * @return the defined source compatibility or "1.5"
     */
    public String getSourcesCompatibility(SourceType type) {
        return fixJavaCompatibility(type, "-source", sourcesCompatibility).orElse(DEFAULT_SOURCE_COMPATIBILITY);
    }

    /**
     * This method returns the Java target compatibility defined for this source
     * set.
     *
     * @deprecated Use {@link #getTargetCompatibility(org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType)} instead.
     * @return
     */
    @Deprecated
    public String getTargetCompatibility() {
        return getTargetCompatibility(SourceType.JAVA);
    }

    /**
     * This method returns the target compatibility defined for this source
     * set for the given language type.
     *
     * The value is actually extracted from the compiler task defined for
     * this source set and language type. If that cannot be determined for some
     * reason this method returns the defined source compatibility.
     *
     * @since 1.4
     * @param type
     * @return the defined target compatibility
     */
    public String getTargetCompatibility(SourceType type) {
        return fixJavaCompatibility(type, "-target", targetCompatibility).orElse(getSourcesCompatibility(type));
    }

    /**
     * Use compiler arguments to override source/target compatibility for JAVA.
     * Look for something like "-flag" or "--flag" in args, the last occurrence;
     * return  it if found.
     * <br>
     * For example for source, flag is "-source", and args is
     * "... --source 13 ..." then return "13". If not in args
     * then don't change the compatibility, return the current value.
     */
    private Optional<String> fixJavaCompatibility(SourceType sourceType, String flag, Map<SourceType, String> compatibilityMap) {
        String compatibility = compatibilityMap.get(sourceType);
        if(sourceType == SourceType.JAVA) { // only fixup for java
            List<String> args = getCompilerArgs(sourceType);
            // index of last occurrence of flag in args, +1 is flag's value
            int idx = Math.max(args.lastIndexOf(flag), args.lastIndexOf("-" + flag)) + 1;
            int idx2 = args.lastIndexOf("--release") + 1;
            if(idx2 > 0) // --release wins; if flag was also set, compile will fail
                idx = idx2;
            if(idx > 0 && idx < args.size()) {
                compatibility = args.get(idx); // note: arg not validated
            }
        }
        return Optional.ofNullable(compatibility);
    }

    /**
     * Returns the name of the configuration used by this SourceSet for compile.
     * This method returns an <code>null</code> from Gradle 7.0 as the
     * corresponding method has been removed in that version. 
     * @return the name of the configuration or <code>null</code> if that's not available.
     * @deprecated No replacement.
     */
    @Deprecated
    public String getRuntimeConfigurationName() {
        return runtimeConfigurationName;
    }

    /**
     * Returns the name of the configuration used by this SourceSet for runtime.
     * This method returns an <code>null</code> from Gradle 7.0 as the
     * corresponding method has been removed in that version. 
     * @return the name of the configuration or <code>null</code> if that's not available.
     * @deprecated No replacement.
     */
    @Deprecated
    public String getCompileConfigurationName() {
        return compileConfigurationName;
    }

    /**
     * The name of the annotation processor path configuration.
     * @return 
     * @since 1.8
     */
    public String getAnnotationProcessorConfigurationName() {
        return annotationProcessorConfigurationName;
    }
    
    public Set<File> getSourceDirs(SourceType type) {
        Set<File> ret = sources.get(type);
        return ret != null ? ret : Collections.<File>emptySet();
    }

    public final Set<File> getJavaDirs() {
        return getSourceDirs(SourceType.JAVA);
    }

    public final Set<File> getGroovyDirs() {
        return getSourceDirs(SourceType.GROOVY);
    }

    public final Set<File> getScalaDirs() {
        return getSourceDirs(SourceType.SCALA);
    }

    /** @since 1.15 */
    public final Set<File> getKotlinDirs() {
        return getSourceDirs(SourceType.KOTLIN);
    }

    public final Set<File> getResourcesDirs() {
        return getSourceDirs(SourceType.RESOURCES);
    }

    /**
     * The directories where sources are generated, e.g. by annotation processors.
     * It only works for builds with Gradle 5.2 and above.
     * @return
     *
     * @since 1.8
     */
    public Set<File> getGeneratedSourcesDirs() {
        return getSourceDirs(SourceType.GENERATED);
    }

    /**
     * Returns all possible configured source directories regardless
     * of their existence.
     *
     * @param deduplicate 
     * @return all possible source directories.
     */
    public final Collection<File> getAllDirs(boolean deduplicate) {
        Collection<File> ret = deduplicate ? new HashSet<>() : new ArrayList<>();
        if (sources != null) {
            for (Set<File> s : sources.values()) {
                ret.addAll(s);
            }
        }
        return ret;
    }

    public final Collection<File> getAllDirs() {
        return getAllDirs(true);
    }

    /**
     * Returns all configured and existing source directories.
     *
     * @return all existing source directories.
     */
    public final Collection<File> getAvailableDirs(boolean deduplicate) {
        Collection<File> ret = deduplicate ? new HashSet<File>() : new ArrayList<File>();
        if (sources != null) {
            for (Set<File> s : sources.values()) {
                for (File f : s) {
                    if (f.isDirectory()) {
                        ret.add(f);
                    }
                }
            }
        }
        return ret;
    }

    public final Collection<File> getAvailableDirs() {
        return getAvailableDirs(true);
    }

    public Set<File> getCompileClassPath() {
        return compileClassPath != null ? compileClassPath : Collections.<File>emptySet();
    }

    public Set<File> getRuntimeClassPath() {
        return runtimeClassPath != null ? runtimeClassPath : getCompileClassPath();
    }

    /**
     * The annotation processor path configured for this source set. If not
     * defined it returns with the compile classpath.
     *
     * @return the annotation processor path for this sourceset.
     * @since 1.8
     */
    public Set<File> getAnnotationProcessorPath() {
        Set<File> ret = annotationProcessorPath != null ? annotationProcessorPath : Collections.<File>emptySet();
        return ret.isEmpty() ? getCompileClassPath() : ret;
    }

    /**
     * Returns the {@link SourceType} of the given file or {@code null} if that
     * file cannot be associated with one.
     *
     * @param f the file to check.
     * @return the matching {@link SourceType} or {@code null}.
     */
    public SourceType getSourceType(File f) {
        for (Map.Entry<SourceType, Set<File>> entry : sources.entrySet()) {
            SourceType type = entry.getKey();
            Set<File> dirs = entry.getValue();
            for (File dir : dirs) {
                if (parentOrSame(f, dir)) {
                    return type;
                }
            }
        }
        return null;
    }

    public boolean hasOverlappingSourceDirs() {
        Set<File> check = new HashSet<>();
        for (SourceType type : SourceType.values()) {
            for (File f : getSourceDirs(type)) {
                if (!check.add(f)) return true;
            }
        }
        return false;
    }

    public Set<SourceType> getSourceTypes(File f) {
        Set<SourceType> ret = EnumSet.noneOf(SourceType.class);
        for (Map.Entry<SourceType, Set<File>> entry : sources.entrySet()) {
            SourceType type = entry.getKey();
            Set<File> dirs = entry.getValue();
            for (File dir : dirs) {
                if (parentOrSame(f, dir)) {
                    ret.add(type);
                }
            }
        }
        return ret;
    }

    /**
     * Checks if a file most probably belongs to the output of this source set.
     * It checks the  output class dirs the output resource dir.
     *
     * @param f a file
     * @return true if the file is in one of the output dirs of this source set.
     */
    public boolean outputContains(File f) {
        List<File> checkList = new LinkedList<>(getOutputClassDirs());
        if (outputResources != null) {
            checkList.add(outputResources);
        }
        for (File check : checkList) {
            if (parentOrSame(f, check)) {
                return true;
            }
        }
        return false;
    }

    public Set<File> getOutputClassDirs() {
        return outputClassDirs != null ? outputClassDirs : Collections.<File>emptySet();
    }
    
    /**
     * Represents an unknown value. This is different from a value that is not present,
     * i.e. an output directory for a language that is not used in the project.
     * @since 1.19
     */
    public static final File UNKNOWN = new File("");
    
    /**
     * Returns output directories for the given source type in the sourceset. Returns
     * null, if the source type has no output directories. Returns UNKNOWN, if the 
     * output location is not known.
     * 
     * @param srcType language type
     * @return location or {@code null}.
     * @since 1.19
     */
    public File getOutputClassDir(SourceType srcType) {
        File f = outputs.get(srcType);
        if (UNKNOWN.equals(f)) {
            // make the value canonical, so == can be used.
            return UNKNOWN;
        }
        return f;
    }

    /**
     * Return the directory of resources output.
     * 
     * @return return the directory of output resources, it might be <code>null</code>.
     */
    public File getOutputResources() {
        return outputResources;
    }

    /**
     * Returns those SourceSets within this project which output is on our
     * compile/runtime classpath. Most common example is: 'test' SourceSet
     * usually returns the 'main' SourceSet as dependency.
     *
     * @return the in project SourceSet dependencies of this SourceSet.
     */
    public Set<GradleJavaSourceSet> getSourceDependencies() {
        return sourceDependencies;
    }

    /**
     * Returns {@code true} if the given file belongs either to the sources or
     * the outputs of this SourceSet. Due to practical consideration if the
     * project is a war project and this SourceSet is the main SourceSet then
     * the content of the project 'webapp' folder is also associated with this
     * SourceSet.
     *
     * @param f the file to test
     * @return {@code true} if the given file can be associated with this
     *         SourceSet
     */
    public boolean contains(File f) {
        boolean web = (webApp != null) && parentOrSame(f, webApp);
        return web || outputContains(f) || getSourceType(f) != null;
    }

    /**
     * Tries to find a resource given by its relative path name in the
     * directories associated with this SourceSet. The output directories
     * are checked before the source ones.
     * This method returns with the first resource if it is found.
     *
     * @param name the name of the resources, use "/" as separator character.
     * @return the full path of the first resource found or {@code null}
     *         if no such resource can be associated with this SourceSet.
     */
    public File findResource(String name) {
        return findResource(name, true);
    }

    /**
     * Tries to find a resource given by its relative path name in the
     * directories associated with this SourceSet. The output directories
     * are checked before the source ones (if they are included).
     * This method returns with the first resource if it is found.
     *
     * @param name the name of the resources, use "/" as separator character.
     * @param includeOutputs include the outputs (classes and resources) in the search
     * @param types Source types to check, if omitted, all source types will be included.
     * @return the full path of the first resource found or {@code null}
     *         if no such resource can be associated with this SourceSet.
     */
    public File findResource(String name, boolean includeOutputs, SourceType... types) {
        List<File> roots = new ArrayList<>();
        if (includeOutputs) {
            roots.addAll(outputClassDirs);
            if (outputResources != null) {
                roots.add(outputResources);
            }
        }
        SourceType[] checkedRoots = types.length > 0 ? types : SourceType.values();
        for (SourceType checkedRoot : checkedRoots) {
            roots.addAll(getSourceDirs(checkedRoot));
        }
        for (File root : roots) {
            File test = new File(root, name);
            if (test.exists()) {
                return test;
            }
        }
        return null;
    }

    public String relativePath(File f) {
        if (!f.isAbsolute()) return null;
        List<Path> roots = new ArrayList<>();
        for (File dir : getAllDirs()) {
            roots.add(dir.toPath());
        }
        for (File dir : getOutputClassDirs()) {
            roots.add(dir.toPath());
        }
        if (outputResources != null) {
            roots.add(outputResources.toPath());
        }
        Path path = f.toPath();
        for (Path root : roots) {
            if (path.startsWith(root)) {
                return root.relativize(path).toString().replace('\\', '/');
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "JavaSourceSet[" + name + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
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
        final GradleJavaSourceSet other = (GradleJavaSourceSet) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.sources, other.sources)) {
            return false;
        }
        if (!Objects.equals(this.outputClassDirs, other.outputClassDirs)) {
            return false;
        }
        if (!Objects.equals(this.outputResources, other.outputResources)) {
            return false;
        }
        if (!Objects.equals(this.webApp, other.webApp)) {
            return false;
        }
        if (!Objects.equals(this.compileClassPath, other.compileClassPath)) {
            return false;
        }
        if (!Objects.equals(this.runtimeClassPath, other.runtimeClassPath)) {
            return false;
        }
        return Objects.equals(this.sourceDependencies, other.sourceDependencies);
    }

    static boolean parentOrSame(File f, File supposedParent) {
        if ((f == null) || (supposedParent == null)) {
            return false;
        }
        boolean ret = supposedParent.equals(f);
        File sparent = supposedParent.getParentFile();
        File parent = f;
        while (!ret && (parent != null) && !parent.equals(sparent)) {
            parent = parent.getParentFile();
            ret = supposedParent.equals(parent);
        }
        return ret;
    }

    public String getBuildTaskName(SourceType type) {
        switch (type) {
            case RESOURCES:
                return getProcessResourcesTaskName();
            case JAVA:
                return getCompileTaskName("Java"); //NOI18N
            case GROOVY:
                return getCompileTaskName("Groovy"); //NOI18N
            case SCALA:
                return getCompileTaskName("Scala"); //NOI18N
            case KOTLIN:
                return getCompileTaskName("Kotlin"); //NOI18N
        }
        return null;
    }

    /**
     * Returns the JDK Home directory of the JVM what would be used during the
     * compilation. Currently the {@linkplain SourceType#JAVA JAVA}, {@linkplain SourceType#GROOVY GROOVY}, and {@linkplain SourceType#SCALA SCALA}
     * are expected to return a non {@code null} value. The home directory
     * is determined by using the sourceSet default compile task. In Gradle 
     * it is possible to define additional compile tasks with different Java Toolchain.
     * NetBeans would ignore those.
     * 
     * @param type The source type of the compiler.
     * @return The home directory of the JDK used for the default compile task.
     * @since 1.26
     */
    public File getCompilerJavaHome(SourceType type) {
        return compilerJavaHomes.get(type);
    }
    
    /**
     * Returns the compiler arguments for this source set defined for the given
     * language.
     *
     * The value is actually extracted from the compiler task defined for
     * this source set and language type. If that cannot be determined for some
     * reason this method returns an empty list.
     * @since 1.4
     * @param type
     * @return 
     */
    public List<String> getCompilerArgs(SourceType type) {
        List<String> args = compilerArgs.get(type);
        return args != null ? args : Collections.<String>emptyList();
    }

    public String getCompileTaskName(String language) {
        return getTaskName("compile", language);
    }

    public String getProcessResourcesTaskName() {
        return getTaskName("process", "Resources");
    }

    public String getClassesTaskName() {
        return getTaskName("classes", null);
    }

    public String getTaskName(String verb, String target) {
        String n = MAIN_SOURCESET_NAME.equals(name) ? "" : Utils.capitalize(name);
        String t = target == null ? "" : Utils.capitalize(target);
        return verb + n + t;
    }
}
