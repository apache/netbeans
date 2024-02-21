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
package org.netbeans.modules.java.source.ui;

import org.netbeans.api.java.source.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.jumpto.type.JumptoAccessor;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Test setup taken from org.netbeans.api.java.source.ClassIndexTest.
 *
 * @author Tomas Zezula
 * @author markiewb
 */
public class JavaTypeProviderTest extends NbTestCase {

    private static FileObject srcRoot;
    private static FileObject binRoot2;
    private static FileObject libSrc2;
    private static ClassPath sourcePath;
    private static ClassPath compilePath;
    private static ClassPath bootPath;
    private static JavaTypeProviderTest.MutableCp spiCp;
    private static JavaTypeProviderTest.MutableCp spiSrc;
    private JavaTypeProvider provider;

    public JavaTypeProviderTest(String name) {
        super(name);
    }
    String[] cA = {"ClassA", "org.me.pkg1"};
    String[] cB = {"ClassA", "org.me.pkg1sibling"};
    String[] cC = {"ClassA", "org.me.pkg1.subpackage"};
    String[] cD = {"ClassB", "org.me.pkg2"};
    String[] cE = {"ClassA", "com.other"};
    String[] cF = {"ClassB", "org.me.pkg1.subpackage"};

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File cache = new File(getWorkDir(), "cache");       //NOI18N
        cache.mkdirs();
        IndexUtil.setCacheFolder(cache);
        File src = new File(getWorkDir(), "src");           //NOI18N
        src.mkdirs();
        srcRoot = FileUtil.toFileObject(src);
        srcRoot.createFolder("foo");                        //NOI18N
        src = new File(getWorkDir(), "src2");               //NOI18N
        src.mkdirs();
        src = new File(getWorkDir(), "lib");               //NOI18N
        src.mkdirs();
        src = new File(getWorkDir(), "lib2");               //NOI18N
        src.mkdirs();
        binRoot2 = FileUtil.toFileObject(src);
        src = new File(getWorkDir(), "lib2Src");            //NOI18N
        src.mkdirs();
        libSrc2 = FileUtil.toFileObject(src);
        spiSrc = new JavaTypeProviderTest.MutableCp(Collections.singletonList(ClassPathSupport.createResource(srcRoot.getURL())));
        sourcePath = ClassPathFactory.createClassPath(spiSrc);
        spiCp = new JavaTypeProviderTest.MutableCp();
        compilePath = ClassPathFactory.createClassPath(spiCp);
        bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        MockServices.setServices(JavaTypeProviderTest.ClassPathProviderImpl.class, JavaTypeProviderTest.SFBQ.class);

        //create some types to be searched for later
        String[][] testTypes = {
            cA, cB, cC, cD, cE, cF
        };

        for (String[] entry : testTypes) {
            createJavaFile(srcRoot, entry[1], entry[0]);
        }
        IndexingManager.getDefault().refreshIndexAndWait(srcRoot.getURL(), null, true);
        final ClassPath scp = ClassPathSupport.createClassPath(srcRoot);
        final ClasspathInfo cpInfo = ClasspathInfo.create(
                ClassPathSupport.createClassPath(new URL[0]),
                ClassPathSupport.createClassPath(new URL[0]),
                scp);
        provider = new JavaTypeProvider(cpInfo, null);

    }

    @Override
    protected void tearDown() throws Exception {
        MockServices.setServices();
    }


    /**
     * Tests the behaviour, when no package name is included in searchText.
     * Packagenames should be ignored.
     *
     * @throws Exception
     */
    public void testGetDeclaredTypesScopes_DefaultBehaviour() throws Exception {
        //original behaviour - without packagename
        {
            String[][] expectedResults = {cE, cA, cC, cB};
            assertComputeTypeNames(expectedResults, "ClassA", provider);
        }
        //original behaviour - without packagename
        {
            String[][] expectedResults = {cF, cD};
            assertComputeTypeNames(expectedResults, "ClassB", provider);
        }

    }

    /**
     * A partially given FQN may result in more than one class type. The
     * packagename is used for retrival of package and subpackages, which
     * results in more results.
     *
     * @throws Exception
     */
    public void testGetDeclaredTypesScopes_FullyQualifiedName() throws Exception {
        //search for classes with prefix "ClassA" and packages with (sub-)package "org.me.pkg1"
        {
            String[][] expectedResults = {cA, cC, cB};
            assertComputeTypeNames(expectedResults, "org.me.pkg1.ClassA", provider);
        }
        //search for classes with prefix "ClassA" and packages with (sub-)package "org"
        {
            String[][] expectedResults = {cA, cC, cB};
            assertComputeTypeNames(expectedResults, "org.ClassA", provider);
        }
        //search for classes with prefix "ClassA" and packages with (sub-)package "org.me"
        {
            String[][] expectedResults = {cA, cC, cB};
            assertComputeTypeNames(expectedResults, "org.me.ClassA", provider);
        }
        //search for classes with prefix "Cl" and packages with (sub-)package "org.me"
        {
            String[][] expectedResults = {cA, cC, cB, cF, cD};
            assertComputeTypeNames(expectedResults, "org.me.Cl", provider);
        }
        //search for classes with prefix "Cl" and packages with (sub-)package "o*.m*"
        {
            String[][] expectedResults = {cA, cC, cB, cF, cD};
            assertComputeTypeNames(expectedResults, "o.m.Cl", provider);
        }
        //search for classes with camel case "CA" and packages with (sub-)package "o*.m*"
        {
            String[][] expectedResults = {cA, cC, cB};
            assertComputeTypeNames(expectedResults, "o.m.CA", provider);
        }
        //search for classes with camel case "CA" and packages with (sub-)package "o*.m*"
        {
            String[][] expectedResults = {cB};
            assertComputeTypeNames(expectedResults, "o.m.pkg1sib.CA", provider);
        }

    }

    private FileObject createJavaFile(
            final FileObject root,
            final String pkg,
            final String name,
            final String content) throws IOException {
        final FileObject file = FileUtil.createData(
                root,
                String.format("%s/%s.java",
                FileObjects.convertPackage2Folder(pkg),
                name));
        final FileLock lck = file.lock();
        try {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(file.getOutputStream(lck)));
            try {
                out.print(content);
            } finally {
                out.close();
            }
        } finally {
            lck.releaseLock();
        }
        return file;
    }

    private void assertComputeTypeNames(String[][] expectedResults, String searchText, TypeProvider provider) {
        List<TypeDescriptor> results = new ArrayList<TypeDescriptor>();
        TypeProvider.Context c = JumptoAccessor.createContext(null, searchText, SearchType.PREFIX);
        TypeProvider.Result res = JumptoAccessor.createResult(results, c);

        provider.computeTypeNames(c, res);

        assertEquals(expectedResults.length, results.size());
        //sort to make the result test run reproducable
        results.sort(new Comparator<TypeDescriptor>() {
            @Override
            public int compare(TypeDescriptor o1, TypeDescriptor o2) {
                int compareValue = o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
                if (compareValue != 0) {
                    return compareValue;
                }
                return o1.getContextName().compareToIgnoreCase(o2.getContextName());
            }
        });

        for (int i = 0; i < results.size(); i++) {

            assertEquals("not equals at index " + i, expectedResults[i][0], results.get(i).getSimpleName());
            assertEquals("not equals at index " + i, " (" + expectedResults[i][1] + ")", results.get(i).getContextName());
        }
    }

    private FileObject createJavaFile(FileObject srcRoot1, String packageName, String className) throws IOException {
        return createJavaFile(srcRoot1, packageName, className, "package " + packageName + ";\npublic class " + className + " {}\n");
    }

    public static class ClassPathProviderImpl implements ClassPathProvider {

        public ClassPath findClassPath(final FileObject file, final String type) {
            final FileObject[] roots = sourcePath.getRoots();
            for (FileObject root : roots) {
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    if (type == ClassPath.SOURCE) {
                        return sourcePath;
                    }
                    if (type == ClassPath.COMPILE) {
                        return compilePath;
                    }
                    if (type == ClassPath.BOOT) {
                        return bootPath;
                    }
                }
            }
            if (libSrc2.equals(file) || FileUtil.isParentOf(libSrc2, file)) {
                if (type == ClassPath.SOURCE) {
                    return ClassPathSupport.createClassPath(new FileObject[]{libSrc2});
                }
                if (type == ClassPath.COMPILE) {
                    return ClassPathSupport.createClassPath(new URL[0]);
                }
                if (type == ClassPath.BOOT) {
                    return bootPath;
                }
            }
            return null;
        }
    }

    public static class SFBQ implements SourceForBinaryQueryImplementation {

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            try {
                if (binaryRoot.equals(binRoot2.getURL())) {
                    return new SourceForBinaryQuery.Result() {
                        public FileObject[] getRoots() {
                            return new FileObject[]{libSrc2};
                        }

                        public void addChangeListener(ChangeListener l) {
                        }

                        public void removeChangeListener(ChangeListener l) {
                        }
                    };
                }
            } catch (FileStateInvalidException e) {
            }
            return null;
        }
    }

    private static final class MutableCp implements ClassPathImplementation {

        private final PropertyChangeSupport support;
        private List<? extends PathResourceImplementation> impls;

        public MutableCp() {
            this(Collections.<PathResourceImplementation>emptyList());
        }

        public MutableCp(final List<? extends PathResourceImplementation> impls) {
            assert impls != null;
            support = new PropertyChangeSupport(this);
            this.impls = impls;
        }

        public List<? extends PathResourceImplementation> getResources() {
            return impls;
        }

        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            assert listener != null;
            this.support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            assert listener != null;
            this.support.removePropertyChangeListener(listener);
        }

        void setImpls(final List<? extends PathResourceImplementation> impls) {
            assert impls != null;
            this.impls = impls;
            this.support.firePropertyChange(PROP_RESOURCES, null, null);
        }
    }
}
