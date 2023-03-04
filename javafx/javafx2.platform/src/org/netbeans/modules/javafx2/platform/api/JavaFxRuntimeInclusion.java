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
package org.netbeans.modules.javafx2.platform.api;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.javafx2.platform.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Type of JavaFX runtime in {@link JavaPlatform}.
 * @author Tomas Zezula
 * @author Petr Somol
 * @since 1.13
 */
public class JavaFxRuntimeInclusion {

    public enum Support {
    /**
     * No JavaFX runtime.
     */
        MISSING, 
    /**
     * JavaFX is a part of {@link JavaPlatform} but it's not
     * on classpath.
     */
        PRESENT, 
    /**
     * JavaFX is a part of {@link JavaPlatform} and it's a part
     * of platform classpath.
     */
        INCLUDED
    };
    
    private static final String PROP_PLATFORM_ANT_NAME = "platform.ant.name";   //NOI18N
    private static final String PROP_JAVA_HOME = "java.home";    //NOI18N
    private static final String SPEC_J2SE = "j2se"; //NOI18N

    private final Support support;
    private final List<String> artifacts;

    private JavaFxRuntimeInclusion(
            final Support supported,
            final List<String> artifacts
            ) {
        this.support = supported;
        this.artifacts = artifacts;
    }

    /**
     * Returns true if the JavaFX is supported.
     * @return true if there is jfxrt.jar installed
     */
    public boolean isSupported() {
        return support == Support.INCLUDED || support == Support.PRESENT;
    }

    /**
     * Returns true if the JavaFX runtime is on boot classpath.
     * @return true if jfxrt.jar is on boot classpath
     */
    public boolean isIncludedOnClassPath() {
        return support == Support.INCLUDED;
    }
    
    /**
     * Returns list of relative paths to artifacts that are needed
     * by FX Projects but that are not on boot classpath and need
     * to be added (jfxrt.jar in JDK7, javaws.jar..)
     * @return list of relative paths
     */
    public List<String> getExtensionArtifactPaths() {
        return artifacts;
    }

    /**
     * Returns {@link JavaFxRuntimeInclusion} for given {@link JavaPlatform}.
     * @param javaPlatform the {@link JavaPlatform} to return the {@link JavaFxRuntimeInclusion} for
     * @return the {@link JavaFxRuntimeInclusion}
     */
    @NonNull
    public static JavaFxRuntimeInclusion forPlatform(@NonNull final JavaPlatform javaPlatform) {
        Parameters.notNull("javaPlatform", javaPlatform);   //NOI18N
        boolean isDefault = JavaPlatform.getDefault().equals(javaPlatform);
        List<String> paths = new ArrayList<>();
        Support runtimeSupport = Support.MISSING;
        String runtimePath = null;
        if (Utils.hasJavaFxModule(javaPlatform)) {
            runtimeSupport = Support.INCLUDED;
        } else {
            for(String runtimeLocation : Utils.getJavaFxRuntimeLocations()) {
                runtimePath = runtimeLocation + Utils.getJavaFxRuntimeArchiveName();
                runtimeSupport = forRuntime(javaPlatform, Utils.getJavaFxRuntimeSubDir(javaPlatform) + runtimePath);
                if(runtimeSupport != Support.MISSING) {
                    break;
                }
            }
            if(runtimeSupport != Support.MISSING && runtimePath != null) {
                if(runtimeSupport == Support.PRESENT) {
                    paths.add((isDefault ? "" : Utils.getJavaFxRuntimeSubDir(javaPlatform)) + runtimePath);
                }
                for(String optionalName : Utils.getJavaFxRuntimeOptionalNames()) {
                    Support optionalSupport = Support.MISSING;
                    String optionalPath = null;
                    for(String optionalLocation : Utils.getJavaFxRuntimeLocations()) {
                        optionalPath = optionalLocation + optionalName;
                        optionalSupport = forRuntime(javaPlatform, Utils.getJavaFxRuntimeSubDir(javaPlatform) + optionalPath);
                        if(optionalSupport == Support.PRESENT) {
                            break;
                        }
                    }
                    if(optionalSupport == Support.PRESENT && optionalPath != null) {
                        paths.add((isDefault ? "" : Utils.getJavaFxRuntimeSubDir(javaPlatform)) + optionalPath);
                    }
                }
            }
        }
        return new JavaFxRuntimeInclusion(runtimeSupport, paths);
    }

    /**
     * Returns status of the artifact at relative path runtimePath in platform javaPlatform
     * @param javaPlatform the {@link JavaPlatform} where the artifact is to be searched for
     * @param runtimePath relative path to artifact
     * @return status of artifact presence/inclusion in platform
     */
    @NonNull
    private static Support forRuntime(@NonNull final JavaPlatform javaPlatform, @NonNull final String runtimePath) {
        Parameters.notNull("javaPlatform", javaPlatform);   //NOI18N
        Parameters.notNull("rtPath", runtimePath);   //NOI18N
        for (FileObject installFolder : javaPlatform.getInstallFolders()) {
            final FileObject jfxrtJar = installFolder.getFileObject(runtimePath);
            if (jfxrtJar != null  && jfxrtJar.isData()) {
                final URL jfxrtRoot = FileUtil.getArchiveRoot(jfxrtJar.toURL());
                for (ClassPath.Entry e : javaPlatform.getBootstrapLibraries().entries()) {
                    if (jfxrtRoot.equals(e.getURL())) {
                        return Support.INCLUDED;
                    }
                }
                return Support.PRESENT;
            }
        }
        return Support.MISSING;
    }
    
    /**
     * Returns the classpath entries which should be included into project's classpath
     * to include JavaFX on given platform.
     * @param javaPlatform for which the classpath entries should be created
     * @return the classpath entries separated by {@link java.io.File#pathSeparatorChar} to include
     * to project classpath or an empty string if JavaFX is already a part of the {@link JavaPlatform}s
     * classpath.
     * @throws IllegalArgumentException if given {@link JavaPlatform} does not support JavaFX or
     * the platform is not a valid J2SE platform.
     *
     * <p class="nonnormative">
     * Typical usage of this method is:
     * <pre>
     * {@code
     * if (JavaFxRuntimeInclusion.forPlatform(javaPlatform).isSupported()) {
     *      Set&lt;String&gt; cpEntries = JavaFxRuntimeInclusion.getProjectClassPathExtension(javaPlatform);
     *      if (cpEntries.length > 0) {
     *          appendToProjectClasspath(cpEntries);
     *      }
     * }
     * </pre>
     * </p>
     *
     */
    public static Set<String> getProjectClassPathExtension(@NonNull final JavaPlatform javaPlatform) {
        Parameters.notNull("javaPlatform", javaPlatform);   //NOI18N
        if (!SPEC_J2SE.equals(javaPlatform.getSpecification().getName())) {
            final Collection<? extends FileObject> installFolders = javaPlatform.getInstallFolders();
            throw new IllegalArgumentException(
                String.format(
                    "Java platform %s (%s) installed in %s is not a valid J2SE platform.",    //NOI18N
                    javaPlatform.getDisplayName(),
                    javaPlatform.getSpecification(),
                    installFolders.isEmpty() ?
                        "???" : //NOI18N
                        FileUtil.getFileDisplayName(installFolders.iterator().next())));
        }
        final JavaFxRuntimeInclusion inclusion = forPlatform(javaPlatform);
        if (!inclusion.isSupported()) {
            return new LinkedHashSet<String>();
        }
        List<String> artifacts = inclusion.getExtensionArtifactPaths();
        if(!artifacts.isEmpty()) {
            Set<String> extensionProp = new LinkedHashSet<String>();
            Iterator<String> i = artifacts.iterator();
            while(i.hasNext()) {
                String artifact = i.next();
                extensionProp.add(
                        String.format(
                            "${%s}/%s",  //NOI18N
                            getPlatformHomeProperty(javaPlatform),
                            artifact));
            }
            return extensionProp; //.toArray(new String[0]);
        }
        return new LinkedHashSet<String>();
    }

    /**
     * Returns name of property that should contain valid path to platform install folder
     * @param javaPlatform the {@link JavaPlatform} whose location the property contains
     * @return property name
     */
    @NonNull
    public static String getPlatformHomeProperty(@NonNull final JavaPlatform javaPlatform) {
        Parameters.notNull("javaPlatform", javaPlatform);   //NOI18N
        return javaPlatform.equals(JavaPlatformManager.getDefault().getDefaultPlatform()) ?
            PROP_JAVA_HOME :
            String.format(
                "platforms.%s.home",   //NOI18N
                javaPlatform.getProperties().get(PROP_PLATFORM_ANT_NAME));
    }

}
