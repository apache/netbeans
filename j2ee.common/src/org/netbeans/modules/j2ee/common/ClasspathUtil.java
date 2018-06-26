/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;

public class ClasspathUtil {

    private static final Logger LOGGER = Logger.getLogger(ClasspathUtil.class.getName());

    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     *
     * @param classpath consists of jar urls and folder urls containing classes
     * @param className the name of the class
     *
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     *
     * @throws IOException if an I/O error has occurred
     *
     * @since 1.15
     */
    public static boolean containsClass(List<URL> classPath, String className) throws IOException {
        Parameters.notNull("classpath", classPath); // NOI18N
        Parameters.notNull("className", className); // NOI18N

        List<File> diskFiles = new ArrayList<File>();
        for (URL url : classPath) {
            URL archiveURL = FileUtil.getArchiveFile(url);

            if (archiveURL != null) {
                url = archiveURL;
            }

            if ("nbinst".equals(url.getProtocol())) { // NOI18N
                // try to get a file: URL for the nbinst: URL
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                    if (localURL != null) {
                        url = localURL;
                    }
                }
            }

            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                File diskFile = FileUtil.toFile(fo);
                if (diskFile != null) {
                    diskFiles.add(diskFile);
                }
            }
        }

        return containsClass(diskFiles, className);
    }

    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     *
     * @param classpath consists of jar files and folders containing classes
     * @param className the name of the class
     *
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     *
     * @throws IOException if an I/O error has occurred
     *
     * @since 1.15
     */
    public static boolean containsClass(Collection<File> classpath, String className) throws IOException {
        Parameters.notNull("classpath", classpath); // NOI18N
        Parameters.notNull("driverClassName", className); // NOI18N
        String classFilePath = className.replace('.', '/') + ".class"; // NOI18N
        for (File file : classpath) {
            if (file.isFile()) {
                JarInputStream is = new JarInputStream(new BufferedInputStream(
                        new FileInputStream(file)), false);
                try {
                    JarEntry entry;
                    while ((entry = is.getNextJarEntry()) != null) {
                        if (classFilePath.equals(entry.getName())) {
                            return true;
                        }
                    }
                } finally {
                    is.close();
                }
            } else {
                if (new File(file, classFilePath).exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Search the provided classpath for specified classes. The classpath is
     * iterated at most once. The value returned is the key of corresponding
     * classname value in the <code>classNames</code> map. The priority of the
     * classnames is determined by the Map iterator, so if the key values to be
     * returned are distinct the Map with defined iteration should be used
     * (such as {@link TreeMap} or {@link LinkedHashMap}).
     *
     * @param <T> the type of token to be returned
     * @param classPath consists of jar urls and folder urls containing classes
     * @param classNames token - classname map containing the searched classnames
     *             and corresponding tokens to be returned
     * @return the token corresponding to classname or <code>null</code> if there
     *             is no match
     * @throws IOException if an I/O error has occurred
     * @since 1.81
     * @see #containsClass(java.util.Collection, java.util.Map)
     */
    @CheckForNull
    public static <T> T containsClass(@NonNull List<URL> classPath, @NonNull Map<T, String> classNames) throws IOException {
        Parameters.notNull("classpath", classPath); // NOI18N
        Parameters.notNull("className", classNames); // NOI18N
        if (classNames.isEmpty()) {
            throw new IllegalArgumentException("classNames can't be empty"); // NOI18N
        }

        List<File> diskFiles = new ArrayList<File>();
        for (URL url : classPath) {
            URL archiveURL = FileUtil.getArchiveFile(url);

            if (archiveURL != null) {
                url = archiveURL;
            }

            if ("nbinst".equals(url.getProtocol())) { // NOI18N
                // try to get a file: URL for the nbinst: URL
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                    if (localURL != null) {
                        url = localURL;
                    }
                }
            }

            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                File diskFile = FileUtil.toFile(fo);
                if (diskFile != null) {
                    diskFiles.add(diskFile);
                }
            }
        }

        return containsClass(diskFiles, classNames);
    }

    /**
     * Search the provided classpath for specified classes. The classpath is
     * iterated at most once. The value returned is the key of corresponding
     * classname value in the <code>classNames</code> map. The priority of the
     * classnames is determined by the Map iterator, so if the key values to be
     * returned are distinct the Map with defined iteration should be used
     * (such as {@link TreeMap} or {@link LinkedHashMap}).
     *
     * @param <T> the type of token to be returned
     * @param classPath consists of jar files and folders containing classes
     * @param classNames token - classname map containing the searched classnames
     *             and corresponding tokens to be returned
     * @return the token corresponding to classname or <code>null</code> if there
     *             is no match
     * @throws IOException if an I/O error has occurred
     * @since 1.81
     * @see #containsClass(java.util.List, java.util.Map)
     */
    @CheckForNull
    public static <T> T containsClass(@NonNull Collection<File> classpath, @NonNull Map<T, String> classNames) throws IOException {
        Parameters.notNull("classpath", classpath); // NOI18N
        Parameters.notNull("classNames", classNames); // NOI18N
        if (classNames.isEmpty()) {
            throw new IllegalArgumentException("classNames can't be empty"); // NOI18N
        }

        String classFilePathFirst = null;
        T tokenFirst = null;

        LinkedHashMap<T, String> classFilePaths = new LinkedHashMap<T, String>();
        for (Map.Entry<T, String> entry : classNames.entrySet()) {
            String classFilePath = entry.getValue().replace('.', '/') + ".class"; // NOI18N
            if (classFilePathFirst == null) {
                classFilePathFirst = classFilePath;
                tokenFirst = entry.getKey();
            } else {
                classFilePaths.put(entry.getKey(), classFilePath);
            }
        }

        int weight = Integer.MAX_VALUE;
        T token = null;

        for (File file : classpath) {
            if (file.isFile()) {
                JarFile jf = new JarFile(file);
                try {
                    Enumeration entries = jf.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        if (classFilePathFirst.equals(entry.getName())) {
                            return tokenFirst;
                        }
                        int i = 0;
                        for (Map.Entry<T, String> entryTokens : classFilePaths.entrySet()) {
                            if (i < weight && entryTokens.getValue().equals(entry.getName())) {
                                token = entryTokens.getKey();
                                weight = i;
                            } else if (i > weight) {
                                break;
                            }
                            i++;
                        }
                    }
                } finally {
                    jf.close();
                }
            } else {
                if (new File(file, classFilePathFirst).exists()) {
                    return tokenFirst;
                }
                int i = 0;
                for (Map.Entry<T, String> entryTokens : classFilePaths.entrySet()) {
                    if (i < weight && new File(file, entryTokens.getValue()).exists()) {
                        token = entryTokens.getKey();
                        weight = i;
                    } else if (i > weight) {
                        break;
                    }
                    i++;
                }
            }
        }
        return token;
    }

    public static File[] getJ2eePlatformClasspathEntries(@NullAllowed Project project, @NullAllowed J2eePlatform j2eePlatform) {
        if (project != null) {
            J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null) {
                J2eePlatform j2eePlatformLocal = j2eePlatform != null ? j2eePlatform : Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
                if (j2eePlatformLocal != null) {
                    try {
                        return j2eePlatformLocal.getClasspathEntries(j2eeModuleProvider.getConfigSupport().getLibraries());
                    } catch (ConfigurationException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                        return j2eePlatformLocal.getClasspathEntries();
                    }
                }
            }
        }
        if (j2eePlatform != null) {
            return j2eePlatform.getClasspathEntries();
        }
        return new File[]{};
    }

}
