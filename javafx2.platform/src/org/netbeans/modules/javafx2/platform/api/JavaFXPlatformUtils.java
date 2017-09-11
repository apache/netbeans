/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.platform.api;

import java.io.File;
import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileObject;

/**
 * API Utility class for JavaFX platform.
 *
 * @author Anton Chechel
 * @author Petr Somol
 */
public final class JavaFXPlatformUtils {

    /**
     * Key for ant platform name
     * @see {@link J2SEPlatformImpl}
     */
    public static final String PLATFORM_ANT_NAME = "platform.ant.name"; // NOI18N
    
    /**
     * Ant name of default Java Platform. Copy of constant from DefaultPlatformImpl (and J2SEProjectBuilder)
     */
    public static final String DEFAULT_PLATFORM = "default_platform"; // NOI18N
    
    /**
     * Ant name of default JavaFX Platform. Copy of constant from JavaFX Platform Utils
     */
    public static final String DEFAULT_JAVAFX_PLATFORM = "Default_JavaFX_Platform"; // NOI18N
    
    /**
     * Property name for list of artifacts to be added to classpath
     */
    public static final String JAVAFX_CLASSPATH_EXTENSION = "javafx.classpath.extension"; // NOI18N
    
    /**
     * Default on-line location of FX2 JavaDoc
     */
    private static final String JAVADOC_ONLINE_URL = "http://docs.oracle.com/javafx/2/api/"; // NOI18N

    /*
     * File name of JavaFX Runtime JAR
     */
    private static final String JFXRT_JAR_NAME = "jfxrt.jar"; // NOI18N
    
    /*
     * Standard relative locations of JavaFX Runtime JAR
     */
    private static final String JFXRT_RELATIVE_LOCATIONS[] = new String[]{"lib", "lib" + File.separatorChar + "ext"}; // NOI18N
    
    /*
     * Standard relative locations of Java Runtime dir
     */
    private static final String JRE_RELATIVE_LOCATIONS[] = new String[]{"jre"}; // NOI18N
    
    private JavaFXPlatformUtils() {
    }

    /**
     * Checks that the platform contains jfxrt.jar (JDK7u6+ should)
     * @param platform
     * @return true if jfxrt.jar can be found in platform directory structure
     */
    public static boolean isJavaFXEnabled(@NullAllowed final JavaPlatform platform) {
        if (platform == null) {
            return false;
        }
        return JavaFxRuntimeInclusion.forPlatform(platform).isSupported();
    }
     
    /**
     * Determines whether any JavaFX enabled platform exist
     * 
     * @return is there any JavaFX platform
     */
    public static boolean isThereAnyJavaFXPlatform() {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform javaPlatform : platforms) {
            if (JavaFXPlatformUtils.isJavaFXEnabled(javaPlatform)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Among existing platforms find such that contains JavaFX RT,
     * prefer the default platform if possible.
     * @return platform with JavaFX RT or null if none such exists
     */
    public static JavaPlatform findJavaFXPlatform() {
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        if(JavaFXPlatformUtils.isJavaFXEnabled(defaultPlatform)) {
            return defaultPlatform;
        }
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform javaPlatform : platforms) {
            if (!javaPlatform.equals(defaultPlatform) && JavaFXPlatformUtils.isJavaFXEnabled(javaPlatform)) {
                return javaPlatform;
            }
        }
        return null;
    }
    
    /**
     * Returns platform's Ant name or null if such does not exist
     * @param platform
     * @return 
     */
    public static String getPlatformAntName(@NonNull final JavaPlatform platform) {
        return platform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
    }
    
    /**
     * Searches for JavaPlatform of given name
     * @param platformName name of platform to search for
     * @return found JavaPlatform or null if no platform found
     */
    public static JavaPlatform findJavaPlatform(@NonNull final String platformName) {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform javaPlatform : platforms) {
            final String antName = javaPlatform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
            if (antName != null && antName.equals(platformName)) {
                return javaPlatform;
            }
        }
        return null;
    }
    
    /**
     * Return exact string that should be added to endorsed/compile classpath
     * @return string representing reference to property referencing added artifacts
     */
    public static String getClassPathExtensionProperty() {
        return "${" + JAVAFX_CLASSPATH_EXTENSION + "}";
    }

    /**
     * Returns path to jfxrt.jar if it exists in platform, null otherwise
     * @param dir
     * @return 
     */
    private static String getJavaFXRuntimeJar(@NonNull String root) {
        for(String rel : JFXRT_RELATIVE_LOCATIONS) {
            String path = root + File.separatorChar + rel + File.separatorChar + JFXRT_JAR_NAME;
            final File jfxrt = new File(path);
            if(jfxrt.exists()) {
                return path;
            }
        }
        return null;
    }
    
    /**
     * Returns path to jfxrt.jar if it exists in platform, null otherwise
     * @param platform
     * @return 
     */
    public static String getJavaFXRuntimeJar(@NonNull JavaPlatform platform) {
        for(FileObject root : platform.getInstallFolders()) {
            if(root.isFolder()) {
                // first try the most probable locations
                for(String rtDir : JRE_RELATIVE_LOCATIONS) {
                    String result = getJavaFXRuntimeJar(root.getPath() + File.separatorChar + rtDir);
                    if(result != null) {
                        return result;
                    }
                }
                // if not found, try other locations
                for(FileObject child : root.getChildren()) {
                    if(child.isFolder()) {
                        String result = getJavaFXRuntimeJar(child.getPath());
                        if(result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }
}
