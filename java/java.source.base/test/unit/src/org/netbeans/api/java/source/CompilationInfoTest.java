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
package org.netbeans.api.java.source;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.junit.Test;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil.FileDescription;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.indexing.JavaBinaryIndexer;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
public class CompilationInfoTest extends NbTestCase {

    private static final String TEST_FILE_CONTENT =
            "public class {0} '{\n" + "   public static void main (String[] args) {\n" + "   }\n" + "}'\n";

    public CompilationInfoTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }

    /**
     * Test of getSourceVersion method, of class CompilationInfo.
     */
    @Test
    public void testGetSourceVersion() throws Exception {
        FileObject test = createTestFile("Test1");
        JavaSource js = JavaSource.forFileObject(test);
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {

                SourceVersion version = parameter.getSourceVersion();
                assertNotNull(version);
            }
        }, true);
    }

    private FileObject createTestFile(String className) {
        try {
            File workdir = this.getWorkDir();
            File root = new File(workdir, "src");
            root.mkdir();
            File data = new File(root, className + ".java");

            PrintWriter out = new PrintWriter(new FileWriter(data));
            try {
                out.println(MessageFormat.format(TEST_FILE_CONTENT, new Object[]{className}));
            } finally {
                out.close();
            }
            return FileUtil.toFileObject(data);
        } catch (IOException ioe) {
            return null;
        }
    }

    public void testCacheEviction() throws Exception {
        clearWorkDir();

        FileObject source = FileUtil.createData(new File(getWorkDir(), "Test.java"));
        TestUtilities.copyStringToFile(source, "public class Test {\n void test() {\n  //whatever\n }\n}\n");

        DataObject sourceDO = DataObject.find(source);
        EditorCookie ec = sourceDO.getLookup().lookup(EditorCookie.class);

        assertNotNull(ec);

        Document doc = ec.openDocument();

        doc.putProperty(Language.class, JavaTokenId.language());

        TokenHierarchy.get(doc).tokenSequence().tokenCount();

        JavaSource js = JavaSource.forDocument(doc);
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                parameter.putCachedValue("1", 1, CacheClearPolicy.ON_TASK_END);
                parameter.putCachedValue("2", 2, CacheClearPolicy.ON_CHANGE);
                parameter.putCachedValue("3", 3, CacheClearPolicy.ON_SIGNATURE_CHANGE);

                assertEquals(1, parameter.getCachedValue("1"));
                assertEquals(2, parameter.getCachedValue("2"));
                assertEquals(3, parameter.getCachedValue("3"));

                parameter.putCachedValue("rewrite", 4, CacheClearPolicy.ON_SIGNATURE_CHANGE);
                parameter.putCachedValue("rewrite", null, CacheClearPolicy.ON_TASK_END);

                assertNull(parameter.getCachedValue("rewrite"));
            }
        }, true);

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                assertNull(parameter.getCachedValue("1"));
                assertEquals(2, parameter.getCachedValue("2"));
                assertEquals(3, parameter.getCachedValue("3"));
            }
        }, true);

        doc.insertString(41, "a", null);

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                assertNull(parameter.getCachedValue("1"));
                assertNull(parameter.getCachedValue("2"));
                assertEquals(3, parameter.getCachedValue("3"));
            }
        }, true);

        doc.insertString(20, "void t2() {}", null);

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                assertNull(parameter.getCachedValue("1"));
                assertNull(parameter.getCachedValue("2"));
                assertNull(parameter.getCachedValue("3"));
            }
        }, true);
    }

    public void testGetClassIndex1() throws Exception {
        clearWorkDir();

        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        FileObject module1 = workFO.createFolder("module1");
        FileObject module1Src = module1.createFolder("src");
        FileObject module1Classes = module1.createFolder("classes");

        SourceUtilsTestUtil.writeFiles(module1Src,
                   new FileDescription("module-info.java",
                                       """
                                       module module1 {
                                           exports api;
                                       }
                                       """),
                   new FileDescription("api/Api.java",
                                       """
                                       package api;
                                       public class Api {
                                       }
                                       """));
        SourceUtilsTestUtil.compile(module1Src, module1Classes, "21");

        FileObject module2 = workFO.createFolder("module2");
        FileObject module2Src = module2.createFolder("src");
        FileObject module2Classes = module2.createFolder("classes");

        SourceUtilsTestUtil.writeFiles(module2Src,
                   new FileDescription("module-info.java",
                                       """
                                       module module2 {
                                           requires transitive module1;
                                           exports api2;
                                       }
                                       """),
                   new FileDescription("api2/Api2.java",
                                       """
                                       package api2;
                                       public class Api2 {
                                       }
                                       """));
        SourceUtilsTestUtil.compile(module2Src, module2Classes, "21", "--module-path", FileUtil.toFile(module1Classes).getAbsolutePath());

        FileObject module3 = workFO.createFolder("module3");
        FileObject module3Src = module3.createFolder("src");
        FileObject module3Classes = module3.createFolder("classes");

        SourceUtilsTestUtil.writeFiles(module3Src,
                   new FileDescription("module-info.java",
                                       """
                                       module module3 {
                                           exports api3;
                                       }
                                       """),
                   new FileDescription("api3/Api3.java",
                                       """
                                       package api3;
                                       public class Api3 {
                                       }
                                       """));
        SourceUtilsTestUtil.compile(module3Src, module3Classes, "21");

        FileObject patch = workFO.createFolder("patch");
        FileObject patchSrc = patch.createFolder("src");
        FileObject patchClasses = patch.createFolder("classes");

        SourceUtilsTestUtil.writeFiles(patchSrc,
                   new FileDescription("api/Patch.java",
                                       """
                                       package api;
                                       public class Patch {
                                       }
                                       """));
        SourceUtilsTestUtil.compile(patchSrc, patchClasses, "21");

        FileObject src = workFO.createFolder("src");
        FileObject classes = workFO.createFolder("classes");

        SourceUtilsTestUtil.writeFiles(src,
                   new FileDescription("module-info.java",
                                       """
                                       module test {
                                           requires module2;
                                       }
                                       """),
                   new FileDescription("test/Test.java",
                                       """
                                       package test;
                                       public class Test {
                                       }
                                       """));
        
        SourceUtilsTestUtil.prepareTest(src, classes, workFO.createFolder("cache"), new FileObject[] {
            module1Classes, module2Classes, module3Classes, patchClasses //to make indexing work ( :-( ) - TODO: can be made better?
        }, new FileObject[] {
            module1Classes, module2Classes, module3Classes
        });
        SourceUtilsTestUtil.setSourceLevel(src, "21");
        SourceUtilsTestUtil.compileRecursively(src);

        ClassPath srcPath = ClassPathSupport.createClassPath(src);
        FileObject testFile = src.getFileObject("test/Test.java");

        ClasspathInfo cpInfo;

        cpInfo = new ClasspathInfo.Builder(BootClassPathUtil.getBootClassPath()) //bootclasspath: prevent source level downgrade
                .setModuleBootPath(BootClassPathUtil.getModuleBootPath())
                .setModuleCompilePath(ClassPathSupport.createClassPath(module1Classes.toURL(), module2Classes.toURL(), module3Classes.toURL()))
                .setSourcePath(srcPath).build();
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[]{
            srcPath});
        Set<String> found;
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api.Api", "api2.Api2"), found);
        found = getDeclaredTypes(cpInfo, testFile, "String");
        assertTrue(found.contains("java.lang.String"));
        assertTrue(found.contains("java.lang.StringBuilder"));

        //module classpath (whatever that means):
        cpInfo = new ClasspathInfo.Builder(BootClassPathUtil.getBootClassPath()) //bootclasspath: prevent source level downgrade
                .setModuleBootPath(BootClassPathUtil.getModuleBootPath())
                .setModuleCompilePath(ClassPathSupport.createClassPath(module1Classes.toURL(), module2Classes.toURL(), module3Classes.toURL()))
                .setModuleClassPath(ClassPathSupport.createClassPath(module3Classes.toURL()))
                //TODO: classpath/module classpath
                .setSourcePath(srcPath).build();
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api.Api", "api2.Api2"), found);
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--add-reads=test=ALL-UNNAMED"));
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api.Api", "api2.Api2", "api3.Api3"), found);
        SourceUtilsTestUtil.setCompilerOptions(src, null);

        //unnamed module:
        src.getFileObject("module-info.java").delete();
        SourceUtilsTestUtil.compileRecursively(src);
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--limit-modules=module2", "--add-modules=module2"));
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api.Api", "api2.Api2"), found);
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--limit-modules=module3", "--add-modules=module3"));
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api3.Api3"), found);
        SourceUtilsTestUtil.setCompilerOptions(src, null);

        //TODO: patch module?
//        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--add-modules=module1", "--patch-module=module1=" + FileUtil.toFile(patchClasses).getAbsolutePath()));
//        found = getDeclaredTypes(cpInfo, testFile, "Patch");
//        assertEquals(Set.of("api.Patch"), found);

        //source level == 8:
        SourceUtilsTestUtil.setSourceLevel(src, "8");
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of(), found);
        cpInfo = new ClasspathInfo.Builder(BootClassPathUtil.getBootClassPath())
                .setClassPath(ClassPathSupport.createClassPath(module3Classes.toURL()))
                //TODO: classpath/module classpath
                .setSourcePath(srcPath).build();
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api3.Api3"), found);
    }

    private Set<String> getDeclaredTypes(ClasspathInfo cpInfo, FileObject file, String prefix) throws Exception {
        //force reparse:
        byte[] data = file.asBytes();
        try (OutputStream out = file.getOutputStream()) {
            out.write(data);
        }

        Set<String> found = new HashSet<>();

        JavaSource.create(cpInfo, file)
                .runWhenScanFinished(cc -> {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);
                    ClassIndex ci = cc.getClassIndex();
                    Set<ElementHandle<TypeElement>> types = ci.getDeclaredTypes(prefix, ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES));
                    types.stream()
                         .map(eh -> eh.getBinaryName())
                         .forEach(found::add);
                }, true).get();

        found.remove("com.sun.tools.javac.util.DefinedBy$Api"); //TODO: workaround
        return found;
    }

    @ServiceProvider(service=MimeDataProvider.class)
    public static final class JavaBinaryIndexerProvider implements MimeDataProvider {

        private final Lookup lookup = Lookups.fixed(new JavaBinaryIndexer.Factory());

        public Lookup getLookup(MimePath mimePath) {
            if (mimePath.getPath().isEmpty()) {
                return lookup;
            }
            return Lookup.EMPTY;
        }
        
    }

    static {
        System.setProperty("SourcePath.no.source.filter", "true");
    }
}
