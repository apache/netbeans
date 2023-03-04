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
package org.netbeans.modules.javafx2.platform;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Utility class for platform properties manipulation
 * 
 * @author Anton Chechel
 * @author Petr Somol
 */
public final class Utils {
    /**
     * Default name for automatically registered JavaFX platform
     */
    public static final String DEFAULT_FX_PLATFORM_NAME = NbBundle.getMessage(Utils.class, "Default_JavaFX_Platform"); // NOI18N
    
    /**
     * Property for not checking matching JavaFX and running JVM architecture
     */
    public static final String NO_PLATFORM_CHECK_PROPERTY = "org.netbeans.modules.javafx2.platform.NoPlatformCheck"; // NOI18N

    private static final String JFXRT_JAR_NAME = "jfxrt.jar"; //NOI18N
    private static final String JDK_JRE_PATH = "jre/"; //NOI18N
    private static final String[] JFXRT_JAR_JRE_PATHS = {"lib/", "lib/ext/"}; //NOI18N
    private static final String[] JFXRT_OPTIONAL_JARS = {"javaws.jar", "deploy.jar", "plugin.jar"}; // NOI18N
    private static final SpecificationVersion JDK9 = new SpecificationVersion("9");   //NOI18N
    private static final String MODULE_PROTOCOL = "nbjrt";   //NOI18N
    private static final String MODULE_JFX_BASE = "javafx.base";    //NOI18N
    private static final String URL_SEPARATOR = "/";    //NOI18N

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.javafx2.platform.Utils"); // NOI18N
    
    private Utils() {
    }
    
    /**
     * Indicates whether running inside a test.
     * Used to bypass J2SE platform creation
     * which causes problems in test environment.
     */
    private static boolean isTest = false;
    
    /**
     * Returns isTest flag value
     * 
     * @return isTest flag value
     */
    public static boolean isTest() {
        return isTest;
    }

    /**
     * Sets isTest flag, unit test should set it to true
     * 
     * @param isTest flag
     */
    public static void setIsTest(boolean test) {
        isTest = test;
    }

    /**
     * Return paths relative to FX RT installation dir where
     * FX RT artifacts may be found
     * @return relative paths
     */
    @NonNull
    public static String[] getJavaFxRuntimeLocations() {
        return JFXRT_JAR_JRE_PATHS;
    }

    /**
     * Return subdirectory in which FX RT resider under JDK
     * @param platform the platform to return the FX RT subdirectory for
     * @return relative path
     */
    @NonNull
    public static String getJavaFxRuntimeSubDir(@NonNull final JavaPlatform platform) {
        return JDK9.compareTo(platform.getSpecification().getVersion())  <= 0 && isModular(platform) ?
                "":             //NOI18N
                JDK_JRE_PATH;
    }

    /**
    * Return file name of FX RT jar
     * @return file name
     */
    @NonNull
    public static String getJavaFxRuntimeArchiveName() {
        return JFXRT_JAR_NAME;
    }

    /**
     * Return file names of optional jars than may need to be added to classpath
     * together with FX RT jar
     * @return file names
     */
    @NonNull
    public static String[] getJavaFxRuntimeOptionalNames() {
        return JFXRT_OPTIONAL_JARS;
    }
    
    /**
     * Checks if JavaFx is present as a module in the platform.
     * @param javaPlatform to check
     * @return returns true if the JavaFx is available as a platform module
     */
    public static boolean hasJavaFxModule(@NonNull final JavaPlatform javaPlatform) {
        if (JDK9.compareTo(javaPlatform.getSpecification().getVersion()) > 0) {
            return false;
        }
        for (ClassPath.Entry e : javaPlatform.getBootstrapLibraries().entries()) {
            final URL url = e.getURL();
            if (!MODULE_PROTOCOL.equals(url.getProtocol())) {
                continue;
            }
            if (MODULE_JFX_BASE.equals(getModuleName(url))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines whether architecture (32b vs 64b) of currently running VM
     * matches given JavaFX Runtime
     * 
     * @param runtimePath JavaFX Runtime location
     * @return is correct architecture
     */
    public static boolean isArchitechtureCorrect(@NonNull String runtimePath) {
        Parameters.notNull("runtimePath", runtimePath); // NOI18N
        
        if (Boolean.getBoolean(NO_PLATFORM_CHECK_PROPERTY)) { 
            return true;
        }
        
//        try {
//            if (Utilities.isUnix() || Utilities.isMac()) {
//                System.load(runtimePath + File.separatorChar + "bin" + File.separatorChar + "libmat.jnilib"); // NOI18N
//                return true;
//            } else if (Utilities.isWindows()) {
//                System.load(runtimePath + File.separatorChar + "bin" + File.separatorChar + "mat.dll"); // NOI18N
//            }
//        } catch (Throwable t) {
//            return false;
//        }
        return true;
    }

    // TODO what if jar names/locations will be changed?
    @NonNull
    public static List<? extends URL> getRuntimeClassPath(@NonNull final File javafxRuntime) {
        Parameters.notNull("javafxRuntime", javafxRuntime); //NOI18N
        final List<URL> result = new ArrayList<URL>();
        final File lib = new File (javafxRuntime,"lib");    //NOI18N
        final File[] children = lib.listFiles(new FileFilter() {
            @Override
            public boolean accept(@NonNull final File pathname) {
                return pathname.getName().toLowerCase().endsWith(".jar");  //NOI18N
            }
        });
        if (children != null) {
            for (File f : children) {
                final URL root = FileUtil.urlForArchiveOrDir(f);
                if (root != null) {
                    result.add(root);
                }
            }
        }
        return result;
    }

    private static boolean isModular(@NonNull final JavaPlatform platform) {
        boolean modular = false;
        for (ClassPath.Entry e : platform.getBootstrapLibraries().entries()) {
            if (MODULE_PROTOCOL.equals(e.getURL().getProtocol())) {
                modular = true;
                break;
            }
        }
        return modular;
    }
    
    @NonNull
    private static String getModuleName(@NonNull final URL url) {
        final String path = url.getPath();
        final int end = path.endsWith(URL_SEPARATOR) ?
                            path.length() - URL_SEPARATOR.length() :
                            path.length();
        int start = end == 0 ? -1 : path.lastIndexOf(URL_SEPARATOR, end - 1);
        start = start < 0 ? 0 : start + URL_SEPARATOR.length();
        return path.substring(start, end);
    }

}
