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
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.SourceUtilsTestUtil.FileDescription;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.indexing.JavaBinaryIndexer;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class ClasspathInfoTest extends NbTestCase {

    public ClasspathInfoTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {
            new JavaBinaryIndexerProvider()
        });
    }

    public void testGetClassIndex() throws Exception {
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
                   new FileDescription("api/AddedPatch.java",
                                       """
                                       package api;
                                       public class AddedPatch {
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
        assertEquals(Set.of("api.Api", "api2.Api2", /*the index returns all element from the cpInfo, only only those visible from the source: */"api3.Api3"), found);
        found = getDeclaredTypes(cpInfo, testFile, "String");
        assertTrue(found.contains("java.lang.String"));
        assertTrue(found.contains("java.lang.StringBuilder"));

        //module classpath:
        cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY) //attempt to cause source level downgrade
                .setModuleBootPath(BootClassPathUtil.getModuleBootPath())
                .setModuleCompilePath(ClassPathSupport.createClassPath(module1Classes.toURL(), module2Classes.toURL(), module3Classes.toURL()))
                .setModuleClassPath(ClassPathSupport.createClassPath(module3Classes.toURL()))
                .setSourcePath(srcPath).build();
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api.Api", "api2.Api2", /*the index returns all element from the cpInfo, only only those visible from the source: */"api3.Api3"), found);
        found = getDeclaredTypes(cpInfo, testFile, "String");
        assertTrue(found.contains("java.lang.String"));
        assertTrue(found.contains("java.lang.StringBuilder"));
        found = getDeclaredTypes(cpInfo, testFile, "List");
        assertTrue(found.contains("java.util.List"));
        assertTrue(found.contains("java.awt.List")); //the index returns all element from the cpInfo, only only those visible from the source

        //module classpath:
        cpInfo = new ClasspathInfo.Builder(BootClassPathUtil.getBootClassPath()) //bootclasspath: prevent source level downgrade
                .setModuleBootPath(BootClassPathUtil.getModuleBootPath())
                .setModuleCompilePath(ClassPathSupport.createClassPath(module1Classes.toURL(), module2Classes.toURL(), module3Classes.toURL()))
                .setModuleClassPath(ClassPathSupport.createClassPath(module3Classes.toURL()))
                .setSourcePath(srcPath).build();
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api.Api", "api2.Api2", /*the index returns all element from the cpInfo, only only those visible from the source: */"api3.Api3"), found);
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--add-reads=test=ALL-UNNAMED"));
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api.Api", "api2.Api2", "api3.Api3"), found);
        SourceUtilsTestUtil.setCompilerOptions(src, null);

        //unnamed module:
        src.getFileObject("module-info.java").delete();
        SourceUtilsTestUtil.compileRecursively(src);
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--limit-modules=module2", "--add-modules=module2"));
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api.Api", "api2.Api2", /*the index returns all element from the cpInfo, only only those visible from the source: */"api3.Api3"), found);
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--limit-modules=module3", "--add-modules=module3"));
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of("api3.Api3", /*the index returns all element from the cpInfo, only only those visible from the source: */"api.Api", "api2.Api2"), found);
        SourceUtilsTestUtil.setCompilerOptions(src, null);

        //patch module is sent using CompilerOptionsQuery, and is not available for ClasspathInfo:
//        //patch-module:
//        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--add-modules=module1", "--patch-module=module1=" + FileUtil.toFile(patchClasses).getAbsolutePath()));
//        found = getDeclaredTypes(cpInfo, testFile, "AddedPat");
//        assertEquals(Set.of("api.AddedPatch"), found);

        //source level == 8:
        SourceUtilsTestUtil.setSourceLevel(src, "8");
        found = getDeclaredTypes(cpInfo, testFile, "Api");
        assertEquals(Set.of(/*the index returns all element from the cpInfo, only only those visible from the source: */"api.Api", "api2.Api2", "api3.Api3"), found);
        cpInfo = new ClasspathInfo.Builder(BootClassPathUtil.getBootClassPath())
                .setClassPath(ClassPathSupport.createClassPath(module3Classes.toURL()))
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
                  .runWhenScanFinished(cc -> {}, true).get(); //ensure indexing finished

        ClassIndex ci = cpInfo.getClassIndex();
        Set<ElementHandle<TypeElement>> types = ci.getDeclaredTypes(prefix, ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.DEPENDENCIES));
        types.stream()
             .map(eh -> eh.getBinaryName())
             .forEach(bn -> {
                 assertFalse(found.contains(bn)); //ensure no duplicates
                 found.add(bn);
             });

        found.remove("com.sun.tools.javac.util.DefinedBy$Api"); //TODO: workaround
        return found;
    }

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
