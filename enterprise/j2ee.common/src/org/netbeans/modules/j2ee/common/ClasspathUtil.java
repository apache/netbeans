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
package org.netbeans.modules.j2ee.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
                    Enumeration<JarEntry> entries = jf.entries();
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
                        File[] files = j2eePlatformLocal.getClasspathEntries(j2eeModuleProvider.getConfigSupport().getLibraries());
                        sortClassPathEntries(files);
                        return files;
                    } catch (ConfigurationException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                        File[] files = j2eePlatformLocal.getClasspathEntries();
                        sortClassPathEntries(files);
                        return files;
                    }
                }
            }
        }
        if (j2eePlatform != null) {
            File[] files = j2eePlatform.getClasspathEntries();
            sortClassPathEntries(files);
            return files;
        }
        return new File[]{};
    }
    
    private static void sortClassPathEntries(File[] files) {
        Arrays.sort(files, new Comparator < File > () {
            @Override
            public int compare(File f1, File f2) {
                return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
            }
        });
    }

}
