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

package org.netbeans.modules.java.api.common.util;

import java.awt.Window;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.OptionsDialog;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Common project utilities. This is a helper class; all methods are static.
 * @author Tomas Mysik
 */
public final class CommonProjectUtils {

    /**
     * J2SE Java Platform Type.
     * @since 1.72
     */
    public static final String J2SE_PLATFORM_TYPE ="j2se";  //NOI18N

    private CommonProjectUtils() {
    }

    // XXX copied from J2SEProjectUtilities, should be part of some API probably (JavaPlatformManager?)
    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    @CheckForNull
    public static JavaPlatform getActivePlatform(final String activePlatformId) {
        return getActivePlatform(activePlatformId, null);
    }

    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @param platformType the type of {@link JavaPlatform}
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     * @since 1.59
     */
    @CheckForNull
    public static JavaPlatform getActivePlatform(
        @NullAllowed final String activePlatformId,
        @NullAllowed String platformType) {
        if (platformType == null) {
            platformType = J2SE_PLATFORM_TYPE;
        }
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (activePlatformId == null) {
            final JavaPlatform candidate = pm.getDefaultPlatform();
            return candidate == null || !platformType.equals(candidate.getSpecification().getName()) ?
                null :
                candidate;
        }
        JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification(platformType, null)); //NOI18N
        for (JavaPlatform javaPlatform : installedPlatforms) {
            String antName = javaPlatform.getProperties().get("platform.ant.name"); //NOI18N
            if (antName != null && antName.equals(activePlatformId)) {
                return javaPlatform;
            }
        }
        return null;
    }

    /**
     * Converts the ant reference to the name of the referenced property.
     * @param property the name of the referenced property.
     * @return the referenced property.
     */
    public static String getAntPropertyName(String property) {
        if (property != null
                && property.startsWith("${") // NOI18N
                && property.endsWith("}")) { // NOI18N
            return property.substring(2, property.length() - 1);
        }
        return property;
    }

    public static Collection<ElementHandle<TypeElement>> getMainMethods (final FileObject fo) {
        // support for unit testing
        if (fo == null || MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return Collections.<ElementHandle<TypeElement>>emptySet();
        }
        return SourceUtils.getMainClasses(fo);
    }

    /** Check if the given file object represents a source with the main method.
     *
     * @param fo source
     * @return true if the source contains the main method
     */
    public static boolean hasMainMethod(FileObject fo) {
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue ();
        }
        if (fo == null) {
            // ??? maybe better should be thrown IAE
            return false;
        }
        return !SourceUtils.getMainClasses(fo).isEmpty();
    }

    public static boolean isMainClass (final String className, ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        return SourceUtils.isMainClass(className, cpInfo);
    }

    /**
     * Returns the name of the project's build script.
     * @param eval the project's {@link PropertyEvaluator}
     * @param propName the name of property holding the reference to project build
     * script or null. In case of null the {@link ProjectProperties#BUILD_SCRIPT} is used.
     * @return the name of project build script
     * @since 1.65
     */
    @NonNull
    public static String getBuildXmlName(
        @NonNull final PropertyEvaluator eval,
        @NullAllowed final String propName) {
        String buildScriptPath = eval.getProperty(
                propName != null ? propName : ProjectProperties.BUILD_SCRIPT);
        if (buildScriptPath == null || buildScriptPath.isEmpty()) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildScriptPath;
    }

    /**
     * Creates a {@link LibraryImplementation3} that can subsequently be used with
     * both Ant and Maven based Java projects.
     * @param classPath local library classpath for use by Ant
     * @param sources local library sources for use by Ant
     * @param javadoc local Javadoc path for use by Ant
     * @param mavendeps list of maven dependencies in the form of groupId:artifactId:version:type,
     *    for example org.eclipse.persistence:eclipselink:2.3.2:jar
     * @param mavenrepos list of maven repositories in the form of layout:url,
     *    for example default:http://download.eclipse.org/rt/eclipselink/maven.repo/
     * @return {@link LibraryImplementation3} representing the information passed as parameters
     * @since 1.40
     */
    public static LibraryImplementation3 createJavaLibraryImplementation(
            @NonNull final String name,
            @NonNull final URL[] classPath,
            @NonNull final URL[] sources,
            @NonNull final URL[] javadoc,
            @NonNull final String[] mavendeps,
            @NonNull final String[] mavenrepos) {
        Parameters.notNull("name", name);   //NOI18N
        Parameters.notNull("classPath", classPath); //NOI18N
        Parameters.notNull("src", sources); //NOI18N
        Parameters.notNull("javadoc", javadoc); //NOI18N
        Parameters.notNull("mavendeps", mavendeps);  //NOI18N
        Parameters.notNull("mavenrepos", mavenrepos);   //NOI18N
        final LibraryImplementation3 impl = LibrariesSupport.createLibraryImplementation3(
                J2SE_PLATFORM_TYPE,
                "classpath",    //NOI18N
                "src",          //NOI18N
                "javadoc"       //NOI18N
                );
        impl.setName(name);
        impl.setContent("classpath", Arrays.asList(classPath)); //NOI18N
        impl.setContent("src", Arrays.asList(sources));     //NOI18N
        impl.setContent("javadoc", Arrays.asList(javadoc));     //NOI18N
        final Map<String,String> props = new HashMap<String, String>();
        // properties: "maven-dependencies", "maven-repositories"
        props.put("maven-dependencies", getPropertyValue(mavendeps));  //NOI18N
        props.put("maven-repositories", getPropertyValue(mavenrepos)); //NOI18N
        impl.setProperties(props);
        return impl;
    }

    @NonNull
    private static String getPropertyValue(@NonNull final String[] values) {
        final StringBuilder result = new StringBuilder();
        for (String value : values) {
            result.append(value);
            result.append(' '); //NOI18N
        }
        return result.length() == 0 ?
           result.toString() :
           result.substring(0, result.length()-1);
    }

}
