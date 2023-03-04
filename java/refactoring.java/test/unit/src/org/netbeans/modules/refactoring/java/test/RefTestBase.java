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
package org.netbeans.modules.refactoring.java.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
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
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

public class RefTestBase extends NbTestCase {

    public RefTestBase(String name) {
        super(name);
    }

    protected static void writeFilesAndWaitForScan(FileObject sourceRoot, RefTestBase.File... files) throws Exception {
        for (FileObject c : sourceRoot.getChildren()) {
            c.delete();
        }

        for (RefTestBase.File f : files) {
            FileObject fo = FileUtil.createData(sourceRoot, f.filename);
            TestUtilities.copyStringToFile(fo, f.content);
        }

        RepositoryUpdater.getDefault().refreshAll(false, true, false, null);
    }

    protected void assertContent(FileObject sourceRoot, RefTestBase.File... files) throws Exception {
        verifyContent(sourceRoot, files);
    }
    protected void verifyContent(FileObject sourceRoot, RefTestBase.File... files) throws Exception {
        List<FileObject> todo = new LinkedList<FileObject>();

        todo.add(sourceRoot);

        Map<String, String> content = new HashMap<String, String>();
        
        FileUtil.refreshFor(FileUtil.toFile(sourceRoot));

        while (!todo.isEmpty()) {
            FileObject file = todo.remove(0);

            if (file.isData()) {
                content.put(FileUtil.getRelativePath(sourceRoot, file), TestUtilities.copyFileToString(FileUtil.toFile(file)));
            } else {
                todo.addAll(Arrays.asList(file.getChildren()));
            }
        }

        for (RefTestBase.File f : files) {
            String fileContent = content.remove(f.filename);

            assertNotNull(f);
            assertNotNull(f.content);
            assertNotNull("Cannot find " + f.filename + " in map " + content, fileContent);
            assertEquals(getName() ,f.content.replaceAll("[ \t\n]+", " "), fileContent.replaceAll("[ \t\n]+", " "));
        }

        assertTrue(content.toString(), content.isEmpty());
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

    @Override
    protected void setUp() throws Exception {
        Logger.getLogger("").setLevel(Level.SEVERE);
        MimeTypes.setAllMimeTypes(new HashSet<String>());
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/openide/loaders/layer.xml",
            "org/netbeans/modules/java/source/resources/layer.xml",
            "org/netbeans/modules/java/editor/resources/layer.xml",
            "org/netbeans/modules/java/hints/resources/layer.xml",
            "org/netbeans/modules/project/ui/resources/layer.xml",
            "META-INF/generated-layer.xml"}, new Object[] {
            new ClassPathProvider() {
            @Override
                public ClassPath findClassPath(FileObject file, String type) {
                    if ((src != null && (file == src || FileUtil.isParentOf(src, file)))
                            || (test != null && (file == test || FileUtil.isParentOf(test, file)))){
                        if (ClassPath.BOOT.equals(type)) {
                            return BootClassPathUtil.getBootClassPath();
                        }
                        if (ClassPath.COMPILE.equals(type)) {
                            return ClassPathSupport.createClassPath(new FileObject[0]);
                        }
                        if (ClassPath.SOURCE.equals(type)) {
                            return ClassPathSupport.createClassPath(src, test);
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
                if (!isProject(projectDirectory)) return null;
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
                                return new SourceGroup[] {GenericSources.group(p, src.getParent(), "", "", null, null)};//,
//                                                          GenericSources.group(p, test, "testsources", "Test Sources", null, null)};
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
        }});
        Main.initializeURLFactory();
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        prepareTest();
        org.netbeans.api.project.ui.OpenProjects.getDefault().open(new Project[] {prj = ProjectManager.getDefault().findProject(src.getParent())}, false);
        MimeTypes.setAllMimeTypes(Collections.singleton("text/x-java"));
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {ClassPathSupport.createClassPath(src),
                                                                                    ClassPathSupport.createClassPath(test)});
        RepositoryUpdater.getDefault().start(true);
        super.setUp();
        FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java");
        FileUtil.getConfigFile("Templates/Classes/Class.java").getOutputStream().close();
        System.setProperty("org.netbeans.modules.parsing.impl.Source.excludedTasks", ".*");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        org.netbeans.api.project.ui.OpenProjects.getDefault().open(new Project[] {prj}, false);
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
            if (problem.isFatal()) {
                return true;
            }
        }
        return false;
    }
}
