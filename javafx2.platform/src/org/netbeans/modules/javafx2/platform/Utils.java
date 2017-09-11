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
