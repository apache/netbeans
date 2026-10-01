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
import java.io.PrintWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

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

    public void testPatchModule() throws Exception {
        clearWorkDir();

        FileObject wd = FileUtil.toFileObject(getWorkDir());

        FileObject patchDir = FileUtil.createFolder(wd, "patch");
        FileObject patch = FileUtil.createData(patchDir, "java/lang/Patched.java");
        TestUtilities.copyStringToFile(patch, """
                                              package java.lang;
                                              public class Patched {
                                              }
                                              """);

        FileObject srcDir = FileUtil.createFolder(wd, "src");
        FileObject source = FileUtil.createData(srcDir, "Test.java");
        String code = """
                      public class Test {
                          private Patched p;
                      }
                      """;

        FileObject classesDir = FileUtil.createFolder(wd, "classes");
        FileObject cacheDir = FileUtil.createFolder(wd, "cache");

        SourceUtilsTestUtil.prepareTest(srcDir,
                                        classesDir,
                                        cacheDir,
                                        new FileObject[0]);

        initSourceQuery(List.of(patchDir.toURL())); //force creation of usages query

        SourceUtilsTestUtil.setSourceLevel(srcDir, "17");
        JavaSource js = JavaSource.forFileObject(source);

        for (List<String> options : List.of(List.of("--patch-module", "java.base=" + FileUtil.toFile(patchDir).getAbsolutePath()),
                                            List.of("--patch-module=java.base=" + FileUtil.toFile(patchDir).getAbsolutePath()))) {
            TestUtilities.copyStringToFile(source, code); //force reparse
            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    assertEquals(List.of("compiler.err.cant.resolve.location"),
                                 parameter.getDiagnostics().stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).map(d -> d.getCode()).toList());
                }
            }, true);

            SourceUtilsTestUtil.setCompilerOptions(srcDir, options);
            TestUtilities.copyStringToFile(source, code); //force reparse

            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    assertEquals(List.of(),
                                 parameter.getDiagnostics().stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).map(d -> d.getCode()).toList());
                }
            }, true);
            SourceUtilsTestUtil.setCompilerOptions(srcDir, List.of());
        }
    }

    private static final void initSourceQuery(final Collection<URL> urls) throws IOException {
        final ClasspathInfo cpInfo = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPath.EMPTY);
        final ClassIndexManager mgr  = ClassIndexManager.getDefault();
        final JavaSource js = JavaSource.create(cpInfo);
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                for (final URL url : urls) {
                    TransactionContext ctx = TransactionContext.beginStandardTransaction(url, false, ()->true, false);
                    try {
                        mgr.createUsagesQuery(url, true);
                    } finally {
                        ctx.commit();
                    }
                }
            }
        }, true);
    }
}
