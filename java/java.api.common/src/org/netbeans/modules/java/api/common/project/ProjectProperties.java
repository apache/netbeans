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

package org.netbeans.modules.java.api.common.project;

import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Radko Najman, David Konecny
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class ProjectProperties {

    /**
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static final String ANNOTATION_PROCESSING_ENABLED = "annotation.processing.enabled"; //NOI18N
    /**
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static final String ANNOTATION_PROCESSING_ENABLED_IN_EDITOR = "annotation.processing.enabled.in.editor"; //NOI18N
    /**
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static final String ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS = "annotation.processing.run.all.processors"; //NOI18N
    /**
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static final String ANNOTATION_PROCESSING_PROCESSORS_LIST = "annotation.processing.processors.list"; //NOI18N
    /**
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static final String ANNOTATION_PROCESSING_SOURCE_OUTPUT = "annotation.processing.source.output"; //NOI18N
    /**
     * @since org.netbeans.modules.java.api.common/0 1.15
     */
    public static final String ANNOTATION_PROCESSING_PROCESSOR_OPTIONS = "annotation.processing.processor.options"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    /**
     * @since org.netbeans.modules.java.api.common/0 1.14
     */
    public static final String JAVAC_PROCESSORPATH = "javac.processorpath"; //NOI18N
    public static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath"; // NOI18N
    public static final String RUN_CLASSPATH = "run.classpath"; // NOI18N
    public static final String RUN_TEST_CLASSPATH = "run.test.classpath"; // NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; //NOI18N
    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
    public static final String ENDORSED_CLASSPATH = "endorsed.classpath"; // NOI18N

    /**
     * The name of the property holding the compilation modulepath.
     * @since 1.80
     */
    public static final String JAVAC_MODULEPATH = "javac.modulepath";    //NOI18N
    /**
     * The name of the property holding the processor modulepath.
     * @since 1.80
     */
    public static final String JAVAC_PROCESSORMODULEPATH = "javac.processormodulepath";    //NOI18N
    /**
     * The name of the property holding the test compilation modulepath.
     * @since 1.80
     */
    public static final String JAVAC_TEST_MODULEPATH = "javac.test.modulepath";    //NOI18N
    /**
     * The name of the property holding the execution modulepath.
     * @since 1.80
     */
    public static final String RUN_MODULEPATH = "run.modulepath";    //NOI18N
    /**
     * The name of the property holding the test execution modulepath.
     * @since 1.80
     */
    public static final String RUN_TEST_MODULEPATH = "run.test.modulepath";    //NOI18N

    /**
     * The name of the property holding the debug modulepath.
     * @since 1.80
     */
    public static final String DEBUG_MODULEPATH = "debug.modulepath"; //NOI18N
    /**
     * The name of the property holding the test debug modulepath.
     * @since 1.80
     */
    public static final String DEBUG_TEST_MODULEPATH = "debug.test.modulepath"; //NOI18N

    /**
     * Property for the modules build folder.
     * @since 1.93
     */
    public static final String BUILD_MODULES_DIR="build.modules.dir";   //NOI18N

    /**
     * The name of the property holding the output directory for modular test compilation. Module directories
     * holding test classes will be created at this location
     * @since 1.101
     */
    public static final String BUILD_TEST_MODULES_DIR = "build.test.modules.dir"; // NOI18N

    public static final String[] WELL_KNOWN_PATHS = new String[] {
        "${" + JAVAC_CLASSPATH + "}", // NOI18N
        "${" + JAVAC_PROCESSORPATH + "}", // NOI18N
        "${" + JAVAC_TEST_CLASSPATH + "}", // NOI18N
        "${" + RUN_CLASSPATH + "}", // NOI18N
        "${" + RUN_TEST_CLASSPATH + "}", // NOI18N
        "${" + BUILD_CLASSES_DIR + "}", // NOI18N
        "${" + BUILD_MODULES_DIR + "}", // NOI18N
        "${" + ENDORSED_CLASSPATH + "}", // NOI18N
        "${" + BUILD_TEST_CLASSES_DIR + "}", // NOI18N
        "${" + BUILD_TEST_MODULES_DIR + "}", // NOI18N
        "${" + JAVAC_MODULEPATH + "}", // NOI18N
        "${" + JAVAC_PROCESSORMODULEPATH + "}", // NOI18N
        "${" + JAVAC_TEST_MODULEPATH + "}", // NOI18N
        "${" + RUN_MODULEPATH + "}", // NOI18N
        "${" + RUN_TEST_MODULEPATH + "}", // NOI18N
    };

    // Prefixes and suffixes of classpath
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N

    private static String RESOURCE_ICON_JAR = "org/netbeans/modules/java/api/common/project/ui/resources/jar.gif"; //NOI18N
    private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/java/api/common/project/ui/resources/libraries.gif"; //NOI18N
    private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/java/api/common/project/ui/resources/projectDependencies.gif"; //NOI18N
    private static String RESOURCE_ICON_BROKEN_BADGE = "org/netbeans/modules/java/api/common/project/ui/resources/brokenProjectBadge.gif"; //NOI18N
    private static String RESOURCE_ICON_SOURCE_BADGE = "org/netbeans/modules/java/api/common/project/ui/resources/jarSourceBadge.png"; //NOI18N
    private static String RESOURCE_ICON_JAVADOC_BADGE = "org/netbeans/modules/java/api/common/project/ui/resources/jarJavadocBadge.png"; //NOI18N
    private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/java/api/common/project/ui/resources/referencedClasspath.gif"; //NOI18N
        
        
    public static ImageIcon ICON_JAR = ImageUtilities.loadImageIcon(RESOURCE_ICON_JAR, false);
    public static ImageIcon ICON_LIBRARY = ImageUtilities.loadImageIcon(RESOURCE_ICON_LIBRARY, false);
    public static ImageIcon ICON_ARTIFACT  = ImageUtilities.loadImageIcon(RESOURCE_ICON_ARTIFACT, false);
    public static ImageIcon ICON_BROKEN_BADGE  = ImageUtilities.loadImageIcon(RESOURCE_ICON_BROKEN_BADGE, false);
    public static ImageIcon ICON_JAVADOC_BADGE  = ImageUtilities.loadImageIcon(RESOURCE_ICON_JAVADOC_BADGE, false);
    public static ImageIcon ICON_SOURCE_BADGE  = ImageUtilities.loadImageIcon(RESOURCE_ICON_SOURCE_BADGE, false);
    public static ImageIcon ICON_CLASSPATH  = ImageUtilities.loadImageIcon(RESOURCE_ICON_CLASSPATH, false);

    public static final String INCLUDES = "includes"; // NOI18N
    public static final String EXCLUDES = "excludes"; // NOI18N

    //General
    /**
     * Name of the property holding the project sources encoding.
     * @since 1.60
     */
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N
    /**
     * Name of the property holding the active project platform.
     * @since 1.60
     */
    public static final String PLATFORM_ACTIVE = "platform.active"; //NOI18N
    /**
     * Name of the property holding the project main build script reference.
     * @since 1.60
     */
    public static final String BUILD_SCRIPT ="buildfile";      //NOI18N
    /**
     * Name of the property holding the project license.
     * @since 1.60
     */
    public static final String LICENSE_NAME = "project.license";
    /**
     * Name of the property holding the path to project license.
     * @since 1.60
     */
    public static final String LICENSE_PATH = "project.licensePath";

    //Build & Run
    /**
     * Name of the property disabling dependency tracking.
     * @since 1.60
     */
    public static final String NO_DEPENDENCIES="no.dependencies"; // NOI18N
    /**
     * Name of the property holding the debug project classpath.
     * @since 1.60
     */
    public static final String DEBUG_CLASSPATH = "debug.classpath"; //NOI18N
    /**
     * Name of the property holding the debug test classpath.
     * @since 1.60
     */
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    /**
     * Name of the property holding the reference to folder where test results should be generated.
     * @since 1.60
     */
    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    /**
     * Name of the property holding the reference to build generated sources.
     * @since 1.60
     */
    public static final String BUILD_GENERATED_SOURCES_DIR = "build.generated.sources.dir"; //NOI18N
    /**
     * Name of the property holding the build excludes.
     * @since 1.60
     */
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String RUN_JVM_ARGS = "run.jvmargs"; // NOI18N
    static final String RUN_JVM_ARGS_IDE = "run.jvmargs.ide"; // NOI18N
    public static final String RUNTIME_ENCODING="runtime.encoding"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    public static final String MAIN_CLASS = "main.class"; // NOI18N
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    public static final String RUN_WORK_DIR = "work.dir"; // NOI18N

    public static final String SYSTEM_PROPERTIES_RUN_PREFIX = "run-sys-prop."; // NOI18N
    public static final String SYSTEM_PROPERTIES_TEST_PREFIX = "test-sys-prop."; // NOI18N

    public static final String PROP_PROJECT_CONFIGURATION_CONFIG = "config"; // NOI18N

    //Javac
    /**
     * Name of the property holding the javac extra args.
     * @since 1.60
     */
    public static final String JAVAC_COMPILERARGS = "javac.compilerargs"; //NOI18N
    /**
     * Name of the property holding the test specific javac extra args.
     * Not set, just allows user to explicitely override the module options for the unit tests.
     * @since 1.83
     */
    public static final String JAVAC_TEST_COMPILERARGS = "javac.test.compilerargs"; //NOI18N
    /**
     * Name of the property holding the javac source.
     * @since 1.60
     */
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    /**
     * Name of the property holding the javac target.
     * @since 1.60
     */
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    /**
     * Name of the property enabling javac deprecation.
     * @since 1.60
     */
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    /**
     * Name of the property holding the javac profile.
     * @since 1.60
     */
    public static final String JAVAC_PROFILE = "javac.profile"; // NOI18N
    /**
     * Name of the property turning on javac debug info generation.
     * @since 1.60
     */
    public static final String JAVAC_DEBUG = "javac.debug"; // NOI18N

    //Jar
    /**
     * Name of the property holding the reference to built jar file.
     * @since 1.60
     */
    public static final String DIST_JAR ="dist.jar";    //NOI18N
    /**
     * Name of the property holding the reference to distribution directory.
     * @since 1.60
     */
    public static final String DIST_DIR ="dist.dir";    //NOI18N
    /**
     * Name of the property enabling jar compression.
     * @since 1.60
     */
    public static final String JAR_COMPRESS = "jar.compress";   //NOI18N
    /**
     * Name of the property holding files excluded from jar file.
     * @since 1.60
     */
    public static final String DIST_ARCHIVE_EXCLUDES = "dist.archive.excludes";   //NOI18N

    //Javadoc
    /**
     * Name of property holding reference to folder where JavaDoc is genered.
     * @since 1.60
     */
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; // NOI18N
    /**
     * Name of property enabling JavaDoc for non public classes.
     * @since 1.60
     */
    public static final String JAVADOC_PRIVATE="javadoc.private"; // NOI18N
    /**
     * Name of property disabling javadoc class hierarchy generation.
     * @since 1.60
     */
    public static final String JAVADOC_NO_TREE="javadoc.notree"; // NOI18N
    /**
     * Name of property enabling creation of javadoc class and package usage pages.
     * @since 1.60
     */
    public static final String JAVADOC_USE="javadoc.use"; // NOI18N
    /**
     * Name of property disabling creation of javadoc navigation bar.
     * @since 1.60
     */
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar"; // NOI18N
    /**
     * Name of property disabling creation of javadoc index.
     * @since 1.60
     */
    public static final String JAVADOC_NO_INDEX="javadoc.noindex"; // NOI18N
    /**
     * Name of property enabling of javadoc split index.
     * @since 1.60
     */
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex"; // NOI18N
    /**
     * Name of property enabling generation of HTML 5 javadoc.
     * @since 1.119
     */
    public static final String JAVADOC_HTML5="javadoc.html5"; // NOI18N
    /**
     * Name of property holding the javadoc author.
     * @since 1.60
     */
    public static final String JAVADOC_AUTHOR="javadoc.author"; // NOI18N
    /**
     * Name of property holding the javadoc version.
     * @since 1.60
     */
    public static final String JAVADOC_VERSION="javadoc.version"; // NOI18N
    /**
     * Name of property holding the javadoc window title.
     * @since 1.60
     */
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle"; // NOI18N
    /**
     * Name of property holding the javadoc encoding.
     * @since 1.60
     */
    public static final String JAVADOC_ENCODING="javadoc.encoding"; // NOI18N
    /**
     * Name of property holding the javadoc additional parameters.
     * @since 1.60
     */
    public static final String JAVADOC_ADDITIONALPARAM="javadoc.additionalparam"; // NOI18N
    //Javadoc stored in the PRIVATE.PROPERTIES
    /**
     * Name of property enabling javadoc preview.
     * @since 1.60
     */
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N

    /** @since org.netbeans.modules.java.j2seproject/1 1.12 */
    public static final String DO_DEPEND = "do.depend"; // NOI18N
    /** @since org.netbeans.modules.java.j2seproject/1 1.12 */
    public static final String DO_JAR = "do.jar"; // NOI18N
    /** @since 1.119 */
    public static final String DO_JLINK ="do.jlink"; // NOI18N
    /** @since 1.119 */
    public static final String JLINK_STRIP ="jlink.strip"; // NOI18N
    /** @since 1.119 */
    public static final String DIST_JLINK_DIR="dist.jlink.dir";
    /** @since 1.119 */
    public static final String DIST_JLINK_OUTPUT="dist.jlink.output";   //NOI18N
    /** @since 1.119 */
    public static final String JLINK_ADDITIONALMODULES="jlink.additionalmodules"; //NOI18N
    /** @since 1.119 */
    public static final String JLINK_ADDITIONALPARAM="jlink.additionalparam"; //NOI18N
    /** @since 1.120 */
    public static final String JLINK_LAUNCHER="jlink.launcher";      //NOI18N
    /** @since 1.120 */
    public static final String JLINK_LAUNCHER_NAME="jlink.launcher.name";   //NOI18N
    /** @since org.netbeans.modules.java.j2seproject/1 1.21 */
    public static final String COMPILE_ON_SAVE = "compile.on.save"; // NOI18N
    /** @since org.netbeans.modules.java.j2seproject/1 1.19 */
    public static final String COMPILE_ON_SAVE_UNSUPPORTED_PREFIX = "compile.on.save.unsupported"; // NOI18N

    //NB 6.1 tracking of files modifications
    public static final String TRACK_FILE_CHANGES="track.file.changes"; //NOI18N

    /** @since 1.122*/
    public static final String MANIFEST_FILE="manifest.file";   //NOI18N
}
