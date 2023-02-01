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
package org.netbeans.modules.refactoring.java.test;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.MimeTypes;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.gototest.TestLocator;
import org.netbeans.spi.gototest.TestLocator.FileType;
import org.netbeans.spi.gototest.TestLocator.LocationListener;
import org.netbeans.spi.gototest.TestLocator.LocationResult;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

public class RefactoringTestBase extends NbTestCase {

    public RefactoringTestBase(String name) {
        super(name);
        sourcelevel = "1.6";
    }

    public RefactoringTestBase(String name, String sourcelevel) {
        super(name);
        this.sourcelevel = sourcelevel;
    }

    protected static void writeFilesAndWaitForScan(FileObject sourceRoot, File... files) throws Exception {
        for (FileObject c : sourceRoot.getChildren()) {
            c.delete();
        }

        for (File f : files) {
            FileObject fo = FileUtil.createData(sourceRoot, f.filename);
            TestUtilities.copyStringToFile(fo, f.content);
        }

        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.toURL(), null, true);
    }

    protected void verifyContent(FileObject sourceRoot, File... files) throws Exception {
        List<FileObject> todo = new LinkedList<FileObject>();

        todo.add(sourceRoot);

        Map<String, String> content = new HashMap<String, String>();

        FileUtil.refreshFor(FileUtil.toFile(sourceRoot));

        while (!todo.isEmpty()) {
            FileObject file = todo.remove(0);

            if (file.isData()) {
                content.put(FileUtil.getRelativePath(sourceRoot, file), copyFileToString(FileUtil.toFile(file)));
            } else {
                todo.addAll(Arrays.asList(file.getChildren()));
            }
        }

        for (File f : files) {
            String fileContent = content.remove(f.filename);

            assertNotNull(f);
            assertNotNull(f.content);
            assertNotNull("Cannot find " + f.filename + " in map " + content, fileContent);
            try {
            assertEquals(getName() ,f.content.replaceAll("[ \t\r\n\n]+", " "), fileContent.replaceAll("[ \t\r\n\n]+", " "));
            } catch (Throwable t) {
                System.err.println("expected:");
                System.err.println(f.content);
                System.err.println("actual:");
                System.err.println(fileContent);
                throw t;
            }
        }

        assertTrue(content.toString(), content.isEmpty());
    }
    
    /**
     * Returns a string which contains the contents of a file.
     *
     * @param f the file to be read
     * @return the contents of the file(s).
     */
    private static String copyFileToString(java.io.File f) throws java.io.IOException {
        int s = (int) f.length();
        byte[] data = new byte[s];
        int len = new FileInputStream(f).read(data);
        if (len != s) {
            throw new EOFException("truncated file");
        }
        return new String(data, Charset.forName("UTF8"));
    }

    protected static void addAllProblems(List<Problem> problems, Problem head) {
        while (head != null) {
            problems.add(head);
            head = head.getNext();
        }
    }

    protected static void assertProblems(Iterable<? extends Problem> golden, Iterable<? extends Problem> real) {
        Iterator<? extends Problem> g = golden.iterator();
        Iterator<? extends Problem> r = real.iterator();

        while (g.hasNext() && r.hasNext()) {
            Problem gp = g.next();
            Problem rp = r.next();

            assertEquals(gp.isFatal(), rp.isFatal());
            assertEquals(gp.getMessage(), rp.getMessage());
        }
        boolean goldenHasNext = g.hasNext();
        boolean realHasNext = r.hasNext();

        assertFalse(goldenHasNext?"Expected: " + g.next().getMessage():"", goldenHasNext);
        assertFalse(realHasNext?"Unexpected: " + r.next().getMessage():"", realHasNext);
    }

    static {
        NbBundle.setBranding("test");
    }

    protected static final class File {
        public final String filename;
        public final String content;

        public File(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }
    }

    protected FileObject src;
    protected FileObject test;
    protected Project prj;
    private ClassPath sourcePath;
    private final String sourcelevel;

    @Override
    protected void setUp() throws Exception {
        System.setProperty("org.netbeans.modules.java.source.usages.SourceAnalyser.fullIndex", "true");
        Logger.getLogger("").setLevel(Level.SEVERE); //turn off chatty logs
        MimeTypes.setAllMimeTypes(new HashSet<String>());
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/openide/loaders/layer.xml",
                    "org/netbeans/modules/java/source/resources/layer.xml",
                    "org/netbeans/modules/java/editor/resources/layer.xml",
            "org/netbeans/modules/refactoring/java/test/resources/layer.xml", "META-INF/generated-layer.xml"}, new Object[] {
                    new ClassPathProvider() {
                        @Override
                        public ClassPath findClassPath(FileObject file, String type) {
                    if (sourcePath != null && sourcePath.contains(file)){
                                if (ClassPath.BOOT.equals(type)) {
                                    return TestUtil.getBootClassPath();
                                }
                                if (JavaClassPathConstants.MODULE_BOOT_PATH.equals(type)) {
                                    return BootClassPathUtil.getModuleBootPath();
                                }
                                if (ClassPath.COMPILE.equals(type)) {
                                    return ClassPathSupport.createClassPath(new FileObject[0]);
                                }
                                if (ClassPath.SOURCE.equals(type)) {
                                    return sourcePath;
                                }
                            }

                            return null;
                        }
                    },
                    new ProjectFactory() {
                        @Override
                        public boolean isProject(FileObject projectDirectory) {
                            return src != null && src.getParent() == projectDirectory;
                        }
                        @Override
                        public Project loadProject(final FileObject projectDirectory, ProjectState state) throws IOException {
                if (!isProject(projectDirectory)) {
                    return null;
                }
                            return new Project() {
                                @Override
                                public FileObject getProjectDirectory() {
                                    return projectDirectory;
                                }
                                @Override
                                public Lookup getLookup() {
                                    final Project p = this;
                                    return Lookups.singleton(new Sources() {

                                        @Override
                                        public SourceGroup[] getSourceGroups(String type) {
                                return new SourceGroup[] {GenericSources.group(p, src.getParent(), "source", "Java Sources", null, null),
                                                        GenericSources.group(p, test, "testsources", "Test Sources", null, null)};
                                        }

                                        @Override
                                        public void addChangeListener(ChangeListener listener) {
                                        }

                                        @Override
                                        public void removeChangeListener(ChangeListener listener) {
                                        }
                                    });
                                }
                            };
                        }
                        @Override
            public void saveProject(Project project) throws IOException, ClassCastException {}
                    },
                    new TestLocator() {

                        @Override
                        public boolean appliesTo(FileObject fo) {
                            return true;
                        }

                        @Override
                        public boolean asynchronous() {
                            return false;
                        }

                        @Override
                        public LocationResult findOpposite(FileObject fo, int caretOffset) {
                            ClassPath srcCp;

                            if ((srcCp = ClassPath.getClassPath(fo, ClassPath.SOURCE)) == null) {
                                return new LocationResult("File not found"); //NOI18N
                            }

                            String baseResName = srcCp.getResourceName(fo, '/', false);
                            String testResName = getTestResName(baseResName, fo.getExt());
                            assert testResName != null;
                            FileObject fileObject = test.getFileObject(testResName);
                if(fileObject != null) {
                                return new LocationResult(fileObject, -1);
                            }

                            return new LocationResult("File not found"); //NOI18N
                        }

                        @Override
                        public void findOpposite(FileObject fo, int caretOffset, LocationListener callback) {
                            throw new UnsupportedOperationException("This should not be called on synchronous locators.");
                        }

                        @Override
                        public FileType getFileType(FileObject fo) {
                if(FileUtil.isParentOf(test, fo)) {
                                return FileType.TEST;
                } else if(FileUtil.isParentOf(src, fo)) {
                                return FileType.TESTED;
                            }
                            return FileType.NEITHER;
                        }

                        private String getTestResName(String baseResName, String ext) {
                StringBuilder buf
                        = new StringBuilder(baseResName.length() + ext.length() + 10);
                            buf.append(baseResName).append("Test");                         //NOI18N
                            if (ext.length() != 0) {
                                buf.append('.').append(ext);
                            }
                            return buf.toString();
                        }
                    },
                    new SourceLevelQueryImplementation() {

                        @Override
                        public String getSourceLevel(FileObject javaFile) {
                            return sourcelevel;
                        }
                    }});
        Main.initializeURLFactory();
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        
//        org.netbeans.modules.java.source.TreeLoader.DISABLE_CONFINEMENT_TEST = true;
        
        prepareTest();
        org.netbeans.api.project.ui.OpenProjects.getDefault().open(new Project[] {prj = ProjectManager.getDefault().findProject(src.getParent())}, false);
        MimeTypes.setAllMimeTypes(Collections.singleton("text/x-java"));
        sourcePath = ClassPathSupport.createClassPath(src, test);
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        RepositoryUpdater.getDefault().start(true);
        super.setUp();
        FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        System.setProperty("org.netbeans.modules.parsing.impl.Source.excludedTasks", ".*");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        org.netbeans.api.project.ui.OpenProjects.getDefault().close(new Project[] {prj});
        CountDownLatch cdl = new CountDownLatch(1);
        RepositoryUpdater.getDefault().stop(() -> {
            cdl.countDown();
        });
        cdl.await();
        prj = null;
    }

    private void prepareTest() throws Exception {
        FileObject workdir = SourceUtilsTestUtil.makeScratchDir(this);

        FileObject projectFolder = FileUtil.createFolder(workdir, "testProject");
        src = FileUtil.createFolder(projectFolder, "src");
        test = FileUtil.createFolder(projectFolder, "test");

            FileObject cache = FileUtil.createFolder(workdir, "cache");

            CacheFolder.setCacheFolder(cache);
        }

    @ServiceProvider(service=MimeDataProvider.class)
    public static final class MimeDataProviderImpl implements MimeDataProvider {

        private static final Lookup L = Lookups.singleton(new JavaCustomIndexer.Factory());

        @Override
        public Lookup getLookup(MimePath mimePath) {
            if ("text/x-java".equals(mimePath.getPath())) {
                return L;
            }

            return null;
        }
        
    }

    protected static boolean problemIsFatal(List<Problem> problems) {
        for (Problem problem : problems) {
            Problem next = problem;
            do {
                if (next.isFatal()) {
                    return true;
                }
                next = next.getNext();
            } while (next != null);
        }
        return false;
    }

    private static final int RETRIES = 3;

    @Override
    protected void runTest() throws Throwable {
        //the tests are unfortunatelly not 100% stable, try to recover by retrying:
        Throwable exc = null;
        for (int i = 0; i < RETRIES; i++) {
            try {
                super.runTest();
                return;
            } catch (Throwable t) {
                if (exc == null) exc = t;
            }
        }
        throw exc;
    }

}