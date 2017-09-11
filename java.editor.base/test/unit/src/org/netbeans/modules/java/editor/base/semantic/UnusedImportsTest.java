/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.base.semantic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.Document;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.editor.base.imports.UnusedImports;
import org.netbeans.modules.java.source.TreeLoader;
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

    private void performUnusedImportsTest(String... golden) throws Exception {
        CompilationInfo ci = SourceUtilsTestUtil.getCompilationInfo(JavaSource.forFileObject(src.getFileObject("test/Main.java")), Phase.RESOLVED);
        TreeLoader.DISABLE_CONFINEMENT_TEST = true;
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

}
