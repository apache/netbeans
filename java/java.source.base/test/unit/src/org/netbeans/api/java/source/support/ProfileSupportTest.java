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
package org.netbeans.api.java.source.support;


import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class ProfileSupportTest extends NbTestCase {

    private static final String JDK8Home = "/Users/tom/Projects/other/repos/jdk/jdk8/build/macosx-x86_64-normal-server-release/images/j2sdk-bundle/jdk1.8.0.jdk/Contents/Home";

    public ProfileSupportTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockServices.setServices(PP.class);
    }


    public void testProfilesByClassFiles() throws IOException {
        final JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
        if (jp == null || !jp.getSpecification().getVersion().equals(new SpecificationVersion("1.8"))) {  //NOI18N
            //Nothing to test
            return;
        }
        final Map<URL,Collection<URL>> testCp = createTestData();
        Collection<ProfileSupport.Violation> res = ProfileSupport.findProfileViolations(
            SourceLevelQuery.Profile.COMPACT1,
            toURLs(jp.getBootstrapLibraries().entries()),
            testCp.keySet(),
            Collections.<URL>emptySet(),
            EnumSet.of(ProfileSupport.Validation.BINARIES_BY_CLASS_FILES));
        assertEquals(            
            new HashMap<URL,Collection<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>>(){
                {                    
                    final Collection<URL> files = testCp.entrySet().iterator().next().getValue();
                    put(
                        files.iterator().next(),
                        Arrays.<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>asList(
                            Pair.<ElementHandle<TypeElement>,SourceLevelQuery.Profile>of(
                                ElementHandleAccessor.getInstance().create(ElementKind.CLASS, "java.sql.Date"),
                                SourceLevelQuery.Profile.COMPACT2),
                            Pair.<ElementHandle<TypeElement>,SourceLevelQuery.Profile>of(
                                ElementHandleAccessor.getInstance().create(ElementKind.CLASS, "javax.naming.Context"),
                                SourceLevelQuery.Profile.COMPACT3),
                            Pair.<ElementHandle<TypeElement>,SourceLevelQuery.Profile>of(
                                ElementHandleAccessor.getInstance().create(ElementKind.CLASS, "org.omg.CORBA.ORB"),
                                SourceLevelQuery.Profile.DEFAULT)
                        ));
                }
            },
            res);

        res = ProfileSupport.findProfileViolations(
            SourceLevelQuery.Profile.COMPACT2,
            toURLs(jp.getBootstrapLibraries().entries()),
            testCp.keySet(),
            Collections.<URL>emptySet(),
            EnumSet.of(ProfileSupport.Validation.BINARIES_BY_CLASS_FILES));
        assertEquals(
            new HashMap<URL,Collection<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>>(){
                {
                    final Collection<URL> files = testCp.entrySet().iterator().next().getValue();
                    put(
                        files.iterator().next(),
                        Arrays.<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>asList(
                            Pair.<ElementHandle<TypeElement>,SourceLevelQuery.Profile>of(
                                ElementHandleAccessor.getInstance().create(ElementKind.CLASS, "javax.naming.Context"),
                                SourceLevelQuery.Profile.COMPACT3),
                            Pair.<ElementHandle<TypeElement>,SourceLevelQuery.Profile>of(
                                ElementHandleAccessor.getInstance().create(ElementKind.CLASS, "org.omg.CORBA.ORB"),
                                SourceLevelQuery.Profile.DEFAULT)
                        ));
                }
            },
            res);
        res = ProfileSupport.findProfileViolations(
            SourceLevelQuery.Profile.COMPACT3,
            toURLs(jp.getBootstrapLibraries().entries()),
            testCp.keySet(),
            Collections.<URL>emptySet(),
            EnumSet.of(ProfileSupport.Validation.BINARIES_BY_CLASS_FILES));
        assertEquals(
            new HashMap<URL,Collection<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>>(){
                {
                    final Collection<URL> files = testCp.entrySet().iterator().next().getValue();
                    put(
                        files.iterator().next(),
                        Arrays.<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>asList(
                            Pair.<ElementHandle<TypeElement>,SourceLevelQuery.Profile>of(
                                ElementHandleAccessor.getInstance().create(ElementKind.CLASS, "org.omg.CORBA.ORB"),
                                SourceLevelQuery.Profile.DEFAULT)
                        ));
                }
            },
            res);
        res = ProfileSupport.findProfileViolations(
            SourceLevelQuery.Profile.DEFAULT,
            toURLs(jp.getBootstrapLibraries().entries()),
            testCp.keySet(),
            Collections.<URL>emptySet(),
            EnumSet.of(ProfileSupport.Validation.BINARIES_BY_CLASS_FILES));
        assertEquals(
            Collections.<URL,Collection<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>>emptyMap(),
            res);
    }

    private void assertEquals(
            Map<URL,Collection<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>> exp,
            Collection<ProfileSupport.Violation> res) {
        final Map<URL,List<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>> resM =
                new HashMap<>();
        for (ProfileSupport.Violation v : res) {
            List<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>> e = resM.get(v.getFile());
            if (e == null) {
                e = new ArrayList<>();
                resM.put(v.getFile(), e);
            }
            e.add(Pair.<ElementHandle<TypeElement>,SourceLevelQuery.Profile>of(
                    v.getUsedType(),
                    v.getRequiredProfile()));
        }
        for (List<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>> l : resM.values()) {
            l.sort(new Comparator<Pair<ElementHandle<TypeElement>,SourceLevelQuery.Profile>>() {
                @Override
                public int compare(
                        Pair<ElementHandle<TypeElement>, SourceLevelQuery.Profile> o1,
                        Pair<ElementHandle<TypeElement>, SourceLevelQuery.Profile> o2) {
                    return o1.first().getBinaryName().compareTo(o2.first().getBinaryName());
                }
            });
        }
        assertEquals(exp, resM);
    };


    @NonNull
    private Collection<URL> toURLs(@NonNull final Collection<? extends ClassPath.Entry> entries) {
        final Queue<URL> q = new ArrayDeque<>();
        for (ClassPath.Entry e : entries) {
            q.offer(e.getURL());
        }
        return q;
    }

    private Map<URL,Collection<URL>> createTestData() throws IOException {
        final File workDir = getWorkDir();
        final File src = new File (workDir, "src");     //NOI18N
        final File bin = new File (workDir, "bin");     //NOI18N
        src.mkdirs();
        bin.mkdirs();
        final File testFile = new File (src,"Test.java");   //NOI18N
        try (PrintWriter out = new PrintWriter(testFile)) {
            out.println(
                "public class Test {\n"  +       //NOI18N
                "    String s;\n"        +       //NOI18N
                "    java.sql.Date d;\n" +       //NOI18N
                "    javax.naming.Context c;\n" +//NOI18N
                "    org.omg.CORBA.ORB orb;\n"  +//NOI18N
                "}\n");                    //NOI18N
        }
        final JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager standardFileManager = jc.getStandardFileManager(null, null, null);
        final StringWriter out = new StringWriter();
        final JavaCompiler.CompilationTask task = jc.getTask(
            out,
            standardFileManager,
            null,
            Arrays.<String>asList(
                "-s", src.getAbsolutePath(),    //NOI18N
                "-d", bin.getAbsolutePath(),    //NOI18N
                "-target", "1.8",               //NOI18N
                "-source", "1.8",               //NOI18N
                "-processorpath", ""),          //NOI18N
            Collections.<String>emptySet(),
            standardFileManager.getJavaFileObjects(testFile));
        final Boolean res = task.call();
        if (res != Boolean.TRUE) {
            throw new IOException(out.toString());
        }
        return Collections.<URL,Collection<URL>>singletonMap(
            Utilities.toURL(bin),
            collectClassFiles(bin));
    }

    @NonNull
    private Collection<URL> collectClassFiles(@NonNull final File folder) throws MalformedURLException {
        final Queue<URL> q = new ArrayDeque<>();
        collectClassFiles(folder, q);
        return q;
    }

    private void collectClassFiles(@NonNull final File folder, @NonNull final Queue<? super URL> q) throws MalformedURLException {
        final File[] chld = folder.listFiles();
        if (chld == null) {
            return;
        }
        for (File f : chld) {
            if (f.isDirectory()) {
                collectClassFiles(f, q);
            } else if (FileObjects.CLASS.equals(FileObjects.getExtension(f.getName()))) {
                q.offer(Utilities.toURL(f));
            }
        }
    }


    public static class PP implements JavaPlatformProvider {

        private final JavaPlatform jp;

        public PP () {
            final Pair<File,URL> jdk8 = findJDK8();
            jp = jdk8 == null ? null : new JP(jdk8);
        }


        @Override
        public JavaPlatform[] getInstalledPlatforms() {
            return jp == null ?
                new JavaPlatform[0] :
                new JavaPlatform[] {jp};
        }

        @Override
        public JavaPlatform getDefaultPlatform() {
            return jp;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @CheckForNull
        private static Pair<File,URL> findJDK8() {
            File jdkHome = null;
            if (JDK8Home != null) {
                jdkHome = new File(JDK8Home);
            } else {
                //TODO:
            }
            if (jdkHome == null || !jdkHome.isDirectory()) {
                return null;
            }
            final File rtJar = new File(
                    jdkHome,
                    "jre/lib/rt.jar".replace('/', File.separatorChar)); //NOI18N
            if (!rtJar.isFile()) {
                return null;
            }
            return Pair.of(
                    FileUtil.normalizeFile(jdkHome),
                    FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(rtJar)));
        }

        private static class JP extends JavaPlatform {

            private final FileObject home;
            private final ClassPath boot;


            JP(Pair<File,URL> p) {
                this.home = FileUtil.toFileObject(p.first());
                this.boot = ClassPathSupport.createClassPath(p.second());
            }


            @Override
            public String getDisplayName() {
                return "Mock Java Platform";    //NOI18N
            }

            @Override
            public Map<String, String> getProperties() {
                return Collections.<String,String>emptyMap();
            }

            @Override
            public ClassPath getBootstrapLibraries() {
                return boot;
            }

            @Override
            public ClassPath getStandardLibraries() {
                return ClassPath.EMPTY;
            }

            @Override
            public String getVendor() {
                return "me";    //NOI18N
            }

            @Override
            public Specification getSpecification() {
                return new Specification("j2se", new SpecificationVersion("1.8"));  //NOI18N
            }

            @Override
            public Collection<FileObject> getInstallFolders() {
                return Collections.singleton(home);
            }

            @Override
            public FileObject findTool(String toolName) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ClassPath getSourceFolders() {
                return ClassPath.EMPTY;
            }

            @Override
            public List<URL> getJavadocFolders() {
                return Collections.<URL>emptyList();
            }
        }
    }
}