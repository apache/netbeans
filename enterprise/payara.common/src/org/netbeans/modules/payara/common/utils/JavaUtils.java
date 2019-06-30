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
package org.netbeans.modules.payara.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.data.PayaraJavaSEConfig;
import org.netbeans.modules.payara.tooling.server.config.ConfigBuilderProvider;
import org.netbeans.modules.payara.tooling.server.config.JavaSEPlatform;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.java.j2seplatform.api.J2SEPlatformCreator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Java platform utilities.
 * <p/>
 * @author Tomas Kraus
 */
public class JavaUtils {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(JavaUtils.class);

    /** Platform display name prefix (including trailing space). */
    private static final String
            PF_PLATFORM_DISPLAY_NAME_PREFIX = "JDK ";

    /** Platform display name suffix (including leading space). */
    private static final String
            PF_PLATFORM_DISPLAY_NAME_SUFFIX = " (Payara)";

    /** Java SE specification name. */
    public static final String
            JAVA_SE_SPECIFICATION_NAME = "j2se";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get installed Java SE platforms.
     * <p/>
     * @return Installed Java SE platforms.
     */
    public static JavaPlatform[] getInstalledJavaSEPlatforms() {
        return JavaPlatformManager.getDefault()
                .getPlatforms(null, new Specification(
                JAVA_SE_SPECIFICATION_NAME, null));
    }

    /**
     * Check if provided Java SE platform is supported by Payara server
     * instance.
     * <p/>
     * @param instance Payara server instance to be started.
     * @param javaHome Java SE home currently selected.
     */
    public static boolean isJavaPlatformSupported(
            @NonNull final PayaraInstance instance,
            @NonNull final File javaHome) {
        // Avoid NPEs and troll developer a bit.
        Parameters.notNull("instance", instance);
        Parameters.notNull("javaHome", javaHome);
        // Java SE platfofms supported by Payara .
        PayaraJavaSEConfig javaSEConfig
                = ConfigBuilderProvider.getBuilder(instance)
                .getJavaSEConfig(instance.getVersion());
        Set<JavaSEPlatform> supportedPlatforms = javaSEConfig.getPlatforms();
        // Try to find Java SE home in installed Java SE platformsList first.
        // It's much faster than external platform version check.
        JavaPlatform platform = findInstalledPlatform(javaHome);
        if (platform != null) {
            JavaSEPlatform javaSEPlatform = JavaSEPlatform.toValue(
                    platform.getSpecification().getVersion().toString());
            if (javaSEPlatform != null
                    && supportedPlatforms.contains(javaSEPlatform)) {
                return true;
            }
        }
        // Java SE home was not found in installed Java SE platforms.
        // Version should be retrieved using external check.
        String javaExec = org.netbeans.modules.payara.tooling.utils.JavaUtils
                .javaVmExecutableFullPath(javaHome.getAbsolutePath());
        org.netbeans.modules.payara.tooling.utils.JavaUtils.JavaVersion version
                = org.netbeans.modules.payara.tooling.utils.JavaUtils
                .javaVmVersion(new File(javaExec));
        JavaSEPlatform javaSEPlatform
                = version != null ? version.toPlatform() : null;
        return javaSEPlatform != null
                    && supportedPlatforms.contains(javaSEPlatform);
    }
    
    /**
     * Search provided Java SE home in installed platformsList.
     * <p/>
     * @param javaHome Java SE home currently selected.
     * @return Returns Java SE platform {@see JavaPlatform} object matching
     *         provided Java SE home or <code>null</code> if no such
     *         installed platform was found.
     */
    public static JavaPlatform findInstalledPlatform(
            @NonNull final File javaHome) {
        // Avoid NPEs and troll developer a bit.
        Parameters.notNull("javaHome", javaHome);
        // Scan all install folders of all onstalled platformsList
        // for Java SE home.
        JavaPlatform[] platforms = getInstalledJavaSEPlatforms();
        JavaPlatform javaPlatform = null;
        for (JavaPlatform platform : platforms) {
            for (FileObject fo : platform.getInstallFolders()) {
                if (javaHome.equals(FileUtil.toFile(fo))) {
                    javaPlatform = platform;
                    break;
                }
            }
            if (javaPlatform != null) {
                break;
            }
        }
        return javaPlatform;        
    }

    /**
     * Search Java SE with provided java home in provided platforms array.
     * <p/>
     * @param javaPlatforms Java SE platforms to search in.
     * @param javaHome      Java SE home to search for.
     * @return Returns Java SE platform {@see JavaPlatform} object matching
     *         provided Java SE home or <code>null</code> if no such
     *         installed platform was found.
     */
    public static JavaPlatform findPlatformByJavaHome(
            @NonNull final JavaPlatform[] javaPlatforms,
            @NonNull final File javaHome) {
        // Avoid NPEs and troll developer a bit.
        Parameters.notNull("javaPlatforms", javaPlatforms);
        Parameters.notNull("javaHome", javaHome);
        // Scan all install folders of all onstalled platformsList
        // for Java SE home.
        JavaPlatform javaPlatform = null;
        for (JavaPlatform platform : javaPlatforms) {
            for (FileObject fo : platform.getInstallFolders()) {
                if (javaHome.equals(FileUtil.toFile(fo))) {
                    javaPlatform = platform;
                    break;
                }
            }
            if (javaPlatform != null) {
                break;
            }
        }
        return javaPlatform;        
    }

    /**
     * Search for installed Java SE platformsList supported by Payara server.
     * <p/>
     * @param instance Payara server instance used to search
     *                 for supported platformsList.
     * @return Returns Array of Java SE platform {@see JavaPlatform} objects
     *         supported by Payara server. Empty array is returned if there
     *         is no such a platform.
     */
    public static JavaPlatform[] findSupportedPlatforms(
            @NonNull final PayaraInstance instance) {
        // Avoid NPEs and troll developer a bit.
        Parameters.notNull("instance", instance);
        // Search for supported Java SE platforms.
        List<JavaPlatform> platformsList = new LinkedList<JavaPlatform>();
        JavaPlatform[] allPlatforms = getInstalledJavaSEPlatforms();
        PayaraJavaSEConfig javaSEConfig
                = ConfigBuilderProvider.getBuilder(instance)
                .getJavaSEConfig(instance.getVersion());
        Set<JavaSEPlatform> supportedPlatforms = javaSEConfig.getPlatforms();
        // Finish quickly for empty set.
        if (supportedPlatforms == null || supportedPlatforms.isEmpty()) {
            return new JavaPlatform[0];
        }
        // Processs non-empty set.
        for (JavaPlatform platform : allPlatforms) {
            for (FileObject fo : platform.getInstallFolders()) {
                if (supportedPlatforms.contains(JavaSEPlatform.toValue(
                        platform.getSpecification()
                        .getVersion().toString()))) {
                    platformsList.add(platform);
                }
            }
        }
        JavaPlatform[] platforms = new JavaPlatform[platformsList.size()];
        int i = 0;
        for (JavaPlatform platform : platformsList) {
            platforms[i++] = platform;
        }
        return platforms;
    }

    /**
     * Search for first available installed folder in Java SE platform.
     * <p/>
     * @param platform Java SE platform to search for first available
     *                 installed folder.
     * @return First available installed folder or <code>null</code> if no such
     *         folder exists.
     */
    public static String getJavaHome(JavaPlatform platform) {
        String javaHome = null;
        Iterator<FileObject> platformIterator
                = platform.getInstallFolders().iterator();
        if (platformIterator.hasNext()) {
            FileObject javaHomeFO = platformIterator.next();
            if (javaHomeFO != null) {
                javaHome = FileUtil.toFile(javaHomeFO).getAbsolutePath();
            }
        }
        return javaHome;
    }

    /**
     * Get default Java SE platform home.
     * <p/>
     * @return Default Java SE platform home.
     */
    public static String getDefaultJavaHome() {
        return getJavaHome(
                JavaPlatformManager.getDefault().getDefaultPlatform());
    }

    /**
     * Verify Java platform and register it when needed.
     * <p/>
     * @param javaHome Installation folder of Java platform.
     * @return Value of <code>true</code> when Java platform is valid JDK
     *         installation folder or <code>false</code> otherwise.
     */
    public static boolean checkAndRegisterJavaPlatform(final String javaHome) {
        // Value of null is not installation folder of Java platform.
        if (javaHome == null) {
            return false;
        }
        File javaHomeFile = new File(javaHome);
        // Java home must be readable directory.
        if (javaHomeFile.isDirectory() && javaHomeFile.canRead()) {
            // Check for already registered platforms.
            if (findInstalledPlatform(javaHomeFile) != null) {
                return true;
            }
            // Otherwise check for Java version and register when valid.
            File javaVm = new File(org.netbeans.modules.payara.tooling.utils
                    .JavaUtils.javaVmExecutableFullPath(javaHome));
            if (javaVm.canExecute()) {
                org.netbeans.modules.payara.tooling.utils.JavaUtils.JavaVersion javaVersion
                        = org.netbeans.modules.payara.tooling.utils
                        .JavaUtils.javaVmVersion(javaVm);
                if (javaVersion != null) {
                    String platformversion = javaVersion.toPlatform().toString();
                    StringBuilder sb = new StringBuilder(
                            PF_PLATFORM_DISPLAY_NAME_PREFIX.length()
                            + PF_PLATFORM_DISPLAY_NAME_SUFFIX.length()
                            + platformversion.length());
                    sb.append(PF_PLATFORM_DISPLAY_NAME_PREFIX);
                    sb.append(platformversion);
                    sb.append(PF_PLATFORM_DISPLAY_NAME_SUFFIX);
                    try {                        
                        J2SEPlatformCreator.createJ2SEPlatform(
                                FileUtil.toFileObject(javaHomeFile),
                                sb.toString());
                        return true;
                    } catch (IOException ioe) {
                        LOGGER.log(Level.INFO,
                                "Unable to register Java platform {0}", javaHome);
                    }
                }
            }
        }
        return false;
    }

}
