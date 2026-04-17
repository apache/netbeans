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

package org.netbeans.modules.java.editor.base.semantic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;

import javax.swing.text.Document;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.editor.base.imports.UnusedImports;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class UnusedImportsTest extends NbTestCase {

    public UnusedImportsTest(String name) {
        super(name);
    }

    public void testUnresolvableImports1() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve;\n" +
                                          "public class Main {}"));

        performUnusedImportsTest("import cannot.resolve;\n");
    }

    public void testUnresolvableImports2() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve;\n" +
                                          "public class Main {resolve r;}"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports3() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve;\n" +
                                          "public class Main extends Parent {resolve r;}"),
                                 new File("test/Parent.java",
                                          "package test;\n" +
                                          "public class Parent { static class resolve {} }")
                                 );

        performUnusedImportsTest("import cannot.resolve;\n");
    }

    public void testUnresolvableImports4() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve;\n" +
                                          "public class Main extends UnresolvableParent {resolve r;}"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports5() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve;\n" +
                                          "public class Main {resolve r; private static class resolve {}}"));

        performUnusedImportsTest("import cannot.resolve;\n");
    }

    public void testUnresolvableImports6() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.*;\n" +
                                          "public class Main { }"));

        performUnusedImportsTest("import cannot.*;\n");
    }

    public void testUnresolvableImports7() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.*;\n" +
                                          "public class Main { resolve r; }"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports9() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.*;\n" +
                                          "public class Main { { resolve(); } }"));

        performUnusedImportsTest("import cannot.*;\n");
    }

    public void testUnresolvableImports10() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve;\n" +
                                          "/** {@link resolve} */ public class Main { }"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports11() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static can.not.resolve;\n" +
                                          "public class Main { }"));

        performUnusedImportsTest("import static can.not.resolve;\n");
    }

    public void testUnresolvableImports12() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static can.not.resolve;\n" +
                                          "public class Main { resolve r; }"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports13() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static can.not.resolve;\n" +
                                          "public class Main { { resolve(); } }"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports14() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static can.not.*;\n" +
                                          "public class Main { }"));

        performUnusedImportsTest("import static can.not.*;\n");
    }

    public void testUnresolvableImports15() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static can.not.*;\n" +
                                          "public class Main { resolve r; }"));

        performUnusedImportsTest("import static can.not.*;\n");
    }

    public void testUnresolvableImports16() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static can.not.*;\n" +
                                          "public class Main { { resolve(); } }"));

        performUnusedImportsTest("import static can.not.*;\n");
    }

    public void testUnresolvableImports17() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static java.lang.Object.doesNotExist;\n" +
                                          "public class Main { }"));

        performUnusedImportsTest("import static java.lang.Object.doesNotExist;\n");
    }

    public void testUnresolvableImports18() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static java.lang.Object.doesNotExist;\n" +
                                          "public class Main { doesNotExist r; }"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports19() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import static java.lang.Object.doesNotExist;\n" +
                                          "public class Main { { doesNotExist(); } }"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports20() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot1.resolve;\n" +
                                          "import cannot2.resolve;\n" +
                                          "public class Main { }"));

        performUnusedImportsTest("import cannot1.resolve;\n", "import cannot2.resolve;\n");
    }
    
    public void testUnresolvableImports21() throws Exception {
        //cannot decide which of these two imports is really useless, do not remove any of them:
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot1.resolve;\n" +
                                          "import cannot2.resolve;\n" +
                                          "public class Main { resolve r; }"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports22() throws Exception {
        //if the import would be resolvable, the single class import would win - do not remove it:
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve.List;\n" +
                                          "import java.util.*;\n" +
                                          "public class Main { List r; }"));

        performUnusedImportsTest();
    }

    public void testUnresolvableImports23() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve.List;\n" +
                                          "import java.util.*;\n" +
                                          "public class Main { }"));

        performUnusedImportsTest("import cannot.resolve.List;\n", "import java.util.*;\n");
    }

    public void testUnresolvableImportsQuestionable1() throws Exception {
        //what to do in this case? two unresolvable imports - unclear which of them is the correct one
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import other.resolve;\n" +
                                          "import cannot.*;\n" +
                                          "public class Main { resolve r; }"));

        performUnusedImportsTest("import cannot.*;\n");
    }

    public void testUnresolvableImportsQuestionable2() throws Exception {
        //if cannot.resolve would contain List, the outcome would not compile - removing the import seems to be reasonable
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.resolve.*;\n" +
                                          "import java.util.*;\n" +
                                          "public class Main { List l;}"));

        performUnusedImportsTest("import cannot.resolve.*;\n");
    }

    public void testBrokenImport() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          "package test;\n" +
                                          "import cannot.\n" +
                                          "public class Main { }"));

        performUnusedImportsTest();
    }

    public void testBrokenSource() throws Exception {
        writeFilesAndWaitForScan(src, new File("test/State.java",
                                               "public enum State implements Iface {\n" +
                                               "    A(1) {\n" +
                                               "    }\n" +
                                               "    int x;\n" +
                                               "    private State(int x) {\n" +
                                               "        this.x = x;\n" +
                                               "    }\n" +
                                               "}\n"));
    }

    public void testModuleInfo() throws Exception {
        SourceUtilsTestUtil.setSourceLevel(src, "11");
        writeFilesAndWaitForScan(src,
                                 new File("module-info.java",
                                          "import java.util.List;\n" +
                                          "import test.Ann;\n" +
                                          "import test.Service1;\n" +
                                          "import test.Service2;\n" +
                                          "import test.Service2Impl;\n" +
                                          "@Ann\n" +
                                          "module m {\n" +
                                          "    uses Service1;\n" +
                                          "    uses Service2;\n" +
                                          "    provides Service2 with Service2Impl;\n" +
                                          "}\n"),
                                 new File("test/Ann.java",
                                          "package test;\n" +
                                          "public @interface Ann { }"),
                                 new File("test/Service1.java",
                                          "package test;\n" +
                                          "public interface Service1 { }"),
                                 new File("test/Service2.java",
                                          "package test;\n" +
                                          "public interface Service2 { }"),
                                 new File("test/Service2Impl.java",
                                          "package test;\n" +
                                          "public class Service2Impl implements Service2 { }"));

        performUnusedImportsTestForFile("module-info.java",
                                        "import java.util.List;\n");
    }

    public void testModuleImport1() throws Exception {
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--enable-preview"));
        SourceUtilsTestUtil.setSourceLevel(src, "" + SourceVersion.latest().ordinal());
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          """
                                          package test;
                                          import module java.base;
                                          public class Main {
                                              List<String> l = new ArrayList<>();
                                          }
                                          """));

        performUnusedImportsTest();
    }

    public void testModuleImport2() throws Exception {
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--enable-preview"));
        SourceUtilsTestUtil.setSourceLevel(src, "" + SourceVersion.latest().ordinal());
        writeFilesAndWaitForScan(src,
                                 new File("test/Main.java",
                                          """
                                          package test;
                                          import module java.base;
                                          import java.util.*;
                                          public class Main {
                                              List<String> l = new ArrayList<>();
                                          }
                                          """));

        performUnusedImportsTest("import module java.base;\n");
    }

    public void testModuleImport3() throws Exception {
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--enable-preview"));
        SourceUtilsTestUtil.setSourceLevel(src, "" + SourceVersion.latest().ordinal());
        writeFilesAndWaitForScan(src,
                                 new File("module-info.java",
                                          """
                                          module m {
                                              requires transitive java.compiler;
                                          }
                                          """),
                                 new File("test/Main.java",
                                          """
                                          package test;
                                          import module m;
                                          public class Main {
                                              ToolProvider p;
                                          }
                                          """));

        performUnusedImportsTest();
    }

    public void testModuleImport4() throws Exception {
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--enable-preview"));
        SourceUtilsTestUtil.setSourceLevel(src, "" + SourceVersion.latest().ordinal());
        writeFilesAndWaitForScan(src,
                                 new File("module-info.java",
                                          """
                                          module m {
                                              requires java.compiler;
                                          }
                                          """),
                                 new File("test/Main.java",
                                          """
                                          package test;
                                          import module m;
                                          public class Main {
                                              ToolProvider p;
                                          }
                                          """));

        performUnusedImportsTest("import module m;\n");
    }

    public void testModuleImport5() throws Exception {
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--enable-preview"));
        SourceUtilsTestUtil.setSourceLevel(src, "" + SourceVersion.latest().ordinal());
        writeFilesAndWaitForScan(src,
                                 new File("module-info.java",
                                          """
                                          module m {
                                              requires java.compiler;
                                          }
                                          """),
                                 new File("test/Main.java",
                                          """
                                          package test;
                                          import module m;
                                          import module java.compiler;
                                          public class Main {
                                              ToolProvider p;
                                          }
                                          """));

        performUnusedImportsTest("import module m;\n");
    }

    public void testModuleImportSamePackage() throws Exception {
        SourceUtilsTestUtil.setCompilerOptions(src, List.of("--enable-preview"));
        SourceUtilsTestUtil.setSourceLevel(src, "" + SourceVersion.latest().ordinal());
        writeFilesAndWaitForScan(src,
                                 new File("module-info.java",
                                          """
                                          module m {
                                              exports m_api;
                                          }
                                          """),
                                 new File("test/Test.java",
                                          """
                                          package m_api;
                                          public class Test {
                                          }
                                          """),
                                 new File("test/Main.java",
                                          """
                                          package m_api;
                                          import module m;
                                          public class Main {
                                              Test p;
                                          }
                                          """));

        performUnusedImportsTest("import module m;\n");
    }

    private void performUnusedImportsTest(String... golden) throws Exception {
        performUnusedImportsTestForFile("test/Main.java", golden);
    }

    private void performUnusedImportsTestForFile(String fileName, String... golden) throws Exception {
        CompilationInfo ci = SourceUtilsTestUtil.getCompilationInfo(JavaSource.forFileObject(src.getFileObject(fileName)), Phase.RESOLVED);
        SourceUtilsTestUtil2.disableConfinementTest();
        Document doc = ci.getSnapshot().getSource().getDocument(true);
        doc.putProperty(Language.class, JavaTokenId.language());
        List<TreePathHandle> unused = UnusedImports.computeUnusedImports(ci);

        Set<String> out = new HashSet<String>();

        for (TreePathHandle h : unused) {
            out.add(h.resolve(ci).getLeaf().toString());
        }

        assertEquals(new HashSet<String>(Arrays.asList(golden)), out);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        Main.initializeURLFactory();
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        prepareTest();
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {ClassPathSupport.createClassPath(src)});
        RepositoryUpdater.getDefault().start(true);
        super.setUp();
    }

    protected FileObject src;

    private void prepareTest() throws Exception {
        FileObject workdir = SourceUtilsTestUtil.makeScratchDir(this);

        src = FileUtil.createFolder(workdir, "src");

        FileObject build = FileUtil.createFolder(workdir, "build");
        FileObject cache = FileUtil.createFolder(workdir, "cache");

        CacheFolder.setCacheFolder(cache);

        SourceUtilsTestUtil.prepareTest(src, build, cache);
    }
    
    private static void writeFilesAndWaitForScan(FileObject sourceRoot, File... files) throws Exception {
        for (FileObject c : sourceRoot.getChildren()) {
            c.delete();
        }

        for (File f : files) {
            FileObject fo = FileUtil.createData(sourceRoot, f.filename);
            TestUtilities.copyStringToFile(fo, f.content);
        }

        SourceUtils.waitScanFinished();
    }

    public static final class File {
        public final String filename;
        public final String content;

        public File(String filename, String content) {
            this.filename = filename;
            this.content = content;
        }
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
