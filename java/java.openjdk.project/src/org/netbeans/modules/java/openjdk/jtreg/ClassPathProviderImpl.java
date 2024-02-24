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
package org.netbeans.modules.java.openjdk.jtreg;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.netbeans.modules.java.openjdk.common.ShortcutUtils;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=ClassPathProvider.class, position=9999)
public class ClassPathProviderImpl implements ClassPathProvider {

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        TestRootDescription rootDesc = TestRootDescription.findRootDescriptionFor(file);

        if (rootDesc == null) {
            return null;
        }

        FileObject testProperties = rootDesc.testProperties;
        FileObject testRoot = rootDesc.testRoot;
        FileObject testRootFile = rootDesc.testRootFile;

        boolean javac = (Utilities.isLangtoolsRepository(testRoot.getParent()) || testRoot.getNameExt().equals("langtools")) &&
                        ShortcutUtils.getDefault().shouldUseCustomTest("langtools", FileUtil.getRelativePath(testRoot.getParent(), file));
        FileObject keyRoot = javac ? testRoot.getNameExt().equals("langtools") ? Utilities.getLangtoolsKeyRoot(testRoot.getParent().getParent()) : Utilities.getLangtoolsKeyRoot(testRoot.getParent()) : null;
        //XXX: hack to make things work for langtools:
        switch (type) {
            case ClassPath.COMPILE:
                if (javac) {
                    ClassPath langtoolsCP = ClassPath.getClassPath(keyRoot, ClassPath.COMPILE);
                    Library testngLib = LibraryManager.getDefault().getLibrary("testng");
                    Library junit5Lib = LibraryManager.getDefault().getLibrary("junit_5");

                    if (testngLib != null || junit5Lib != null) {
                        List<ClassPath> parts = new ArrayList<>();

                        if (testngLib != null) {
                            parts.add(ClassPathSupport.createClassPath(testngLib.getContent("classpath").toArray(new URL[0])));
                        }
                        if (junit5Lib != null) {
                            parts.add(ClassPathSupport.createClassPath(junit5Lib.getContent("classpath").toArray(new URL[0])));
                        }

                        parts.add(langtoolsCP);

                        return ClassPathSupport.createProxyClassPath(parts.toArray(new ClassPath[0]));
                    }

                    if (langtoolsCP == null)
                        return ClassPath.EMPTY;
                    else
                        return langtoolsCP;
                }
                else return null;
            case ClassPath.BOOT:
                if (javac) {
                    try {
                        ClassPath langtoolsBCP = ClassPath.getClassPath(keyRoot, ClassPath.BOOT);
                        List<URL> roots = new ArrayList<>();
                        for (String rootPaths : new String[] {"build/classes/",
                                                              "build/java.compiler/classes/",
                                                              "build/jdk.compiler/classes/",
                                                              "build/jdk.javadoc/classes/",
                                                              "build/jdk.dev/classes/"}) {
                            roots.add(testRoot.getParent().toURI().resolve(rootPaths).toURL());
                        }
                        return ClassPathSupport.createProxyClassPath(ClassPathSupport.createClassPath(roots.toArray(new URL[0])), langtoolsBCP);
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return null;
            case ClassPath.SOURCE:
                break;
            default:
                return null;
        }

        Set<FileObject> roots = new LinkedHashSet<>();

        if (testProperties != null) {
            roots.add(testProperties.getParent());

            try (InputStream in = testProperties.getInputStream()) {
                Properties p = new Properties();
                p.load(in);
                String libDirsText = p.getProperty("lib.dirs");
                FileObject libDirsRoot = libDirsText != null ? resolve(testProperties, testRoot, libDirsText) : null;

                if (libDirsRoot != null) roots.add(libDirsRoot);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            if (file.isFolder()) return null;

            roots.add(file.getParent());
            try (Reader r = new InputStreamReader(file.getInputStream(), FileEncodingQuery.getEncoding(file))) {
                StringBuilder content = new StringBuilder();
                int read;

                while ((read = r.read()) != (-1)) {
                    content.append((char) read);
                }

                Pattern library = Pattern.compile("@library (.*)\n");
                Matcher m = library.matcher(content.toString());

                if (m.find()) {
                    List<FileObject> libDirs = new ArrayList<>();
                    try (InputStream in = testRootFile.getInputStream()) {
                        Properties p = new Properties();
                        p.load(in);
                        String externalLibRoots = p.getProperty("external.lib.roots");
                        if (externalLibRoots != null) {
                            for (String extLib : externalLibRoots.split("\\s+")) {
                                FileObject libDir = BuildUtils.getFileObject(testRoot, extLib);

                                if (libDir != null) {
                                    libDirs.add(libDir);
                                }
                            }
                        }
                    }
                    libDirs.add(testRoot);
                    String libraryPaths = m.group(1).trim();
                    for (String libraryPath : libraryPaths.split(" ")) {
                        for (FileObject libDir : libDirs) {
                            FileObject libFO = resolve(file, libDir, libraryPath);

                            if (libFO != null) {
                                roots.add(libFO);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        //XXX:
        for (FileObject root : roots) {
            initializeUsagesQuery(root);
        }

        return ClassPathSupport.createClassPath(roots.toArray(new FileObject[0]));
    }

    private FileObject resolve(FileObject file, FileObject root, String spec) {
        if (spec.startsWith("/")) {
            return BuildUtils.getFileObject(root, spec.substring(1));
        } else {
            return BuildUtils.getFileObject(file.getParent(), spec);
        }
    }

    private void initializeUsagesQuery(FileObject root) {
        try {
            ClassLoader cl = JavaSource.class.getClassLoader();
            Class<?> transactionContextClass = Class.forName("org.netbeans.modules.java.source.indexing.TransactionContext", false, cl);
            Class<?> serviceClass = Class.forName("org.netbeans.modules.java.source.indexing.TransactionContext$Service", false, cl);
            Method beginTrans = transactionContextClass.getDeclaredMethod("beginTrans");
            Method commit = transactionContextClass.getDeclaredMethod("commit");
            Method register = transactionContextClass.getDeclaredMethod("register", Class.class, serviceClass);
            Class<?> classIndexEventsTransactionClass = Class.forName("org.netbeans.modules.java.source.usages.ClassIndexEventsTransaction", false, cl);
            Method cietcCreate;
            Object[] cietcCreateParams;
            try {
                cietcCreate = classIndexEventsTransactionClass.getDeclaredMethod("create", boolean.class);
                cietcCreateParams = new Object[] {true};
            } catch (NoSuchMethodException ex) {
                cietcCreate = classIndexEventsTransactionClass.getDeclaredMethod("create", boolean.class, Supplier.class);
                cietcCreateParams = new Object[] {true, (Supplier<Boolean>) () -> true};
            }
            Class<?> classIndexManagerClass = Class.forName("org.netbeans.modules.java.source.usages.ClassIndexManager", false, cl);
            Method cimcGetDefault = classIndexManagerClass.getDeclaredMethod("getDefault");
            Method createUsagesQuery = classIndexManagerClass.getDeclaredMethod("createUsagesQuery", URL.class, boolean.class);
            Class<?> classIndexImplClass = Class.forName("org.netbeans.modules.java.source.usages.ClassIndexImpl", false, cl);
            Class<?> stateClass = Class.forName("org.netbeans.modules.java.source.usages.ClassIndexImpl$State", false, cl);
            Method setState = classIndexImplClass.getDeclaredMethod("setState", stateClass);
            Field initialized = stateClass.getDeclaredField("INITIALIZED");

            Object transaction = beginTrans.invoke(null);
            register.invoke(transaction, classIndexEventsTransactionClass, cietcCreate.invoke(null, cietcCreateParams));
            try {
                Object classIndexImpl = createUsagesQuery.invoke(cimcGetDefault.invoke(null), root.toURL(), true);
                setState.invoke(classIndexImpl, initialized.get(null));
            } finally {
                commit.invoke(transaction);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
