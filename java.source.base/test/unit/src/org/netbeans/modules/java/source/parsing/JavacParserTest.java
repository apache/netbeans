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

package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Source;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import static junit.framework.Assert.assertEquals;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author lahvac
 */
public class JavacParserTest extends NbTestCase {

    public JavacParserTest(String name) {
        super(name);
    }

    private FileObject sourceRoot;

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {settings});
        clearWorkDir();
        prepareTest();
    }

    public void Dtest1() throws Exception {
        FileObject f1 = createFile("test/Test1.java", "package test; class Test1");
        FileObject f2 = createFile("test/Test2.java", "package test; class Test2{}");
        FileObject f3 = createFile("test/Test3.java", "package test; class Test3{}");

        ClasspathInfo cpInfo = ClasspathInfo.create(f2);
        JavaSource js = JavaSource.create(cpInfo, f2, f3);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                if ("Test3".equals(parameter.getFileObject().getName())) {
                    TypeElement te = parameter.getElements().getTypeElement("test.Test1");
                    assertNotNull(te);
                    assertNotNull(parameter.getTrees().getPath(te));
                }
                assertEquals(Phase.PARSED, parameter.toPhase(Phase.PARSED));
                assertNotNull(parameter.getCompilationUnit());
            }
        }, true);
    }

    public void test199332() throws Exception {
        settings.commandLine = "-Xlint:serial";

        FileObject f2 = createFile("test/Test2.java", "package test; class Test2 implements Runnable, java.io.Serializable {}");
        JavaSource js = JavaSource.forFileObject(f2);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
                assertEquals(parameter.getDiagnostics().toString(), 2, parameter.getDiagnostics().size());

                Set<String> codes = new HashSet<String>();

                for (Diagnostic d : parameter.getDiagnostics()) {
                    codes.add(d.getCode());
                }

                assertEquals(new HashSet<String>(Arrays.asList("compiler.warn.missing.SVUID", "compiler.err.does.not.override.abstract")), codes);
            }
        }, true);
        
        settings.commandLine = null;
    }
    
    public void testPartialReparseSanity() throws Exception {
        FileObject f2 = createFile("test/Test2.java", "package test; class Test2 { private void test() { System.err.println(\"\"); System.err.println(1); } }");
        DataObject d = DataObject.find(f2);
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        JavaSource js = JavaSource.forFileObject(f2);

        doc.putProperty(Language.class, JavaTokenId.language());
        
        //initialize the tokens hierarchy:
        TokenSequence<?> ts = TokenHierarchy.get(doc).tokenSequence();
        
        ts.moveStart();
        
        while (ts.moveNext());
        
        final AtomicReference<CompilationUnitTree> tree = new AtomicReference<CompilationUnitTree>();
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
                tree.set(parameter.getCompilationUnit());
            }
        }, true);
        
        doc.insertString(doc.getText(0, doc.getLength()).indexOf("\"") + 1, "aaaaaaaa", null);
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(final CompilationController parameter) throws Exception {
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
                
                assertSame(tree.get(), parameter.getCompilationUnit());
                
                new TreePathScanner<Void, long[]>() {

                    @Override
                    public Void scan(Tree tree, long[] parentSpan) {
                        if (tree == null) return null;
                        if (parameter.getTreeUtilities().isSynthetic(new TreePath(getCurrentPath(), tree))) return null;
                        long start = parameter.getTrees().getSourcePositions().getStartPosition(parameter.getCompilationUnit(), tree);
                        long end   = parameter.getTrees().getSourcePositions().getEndPosition(parameter.getCompilationUnit(), tree);
                        assertTrue(start <= end);
                        if (parentSpan != null) {
                            assertTrue(parentSpan[0] <= start);
                            assertTrue(end <= parentSpan[1]);
                        }
                        return super.scan(tree, new long[] {start, end});
                    }
                    
                }.scan(parameter.getCompilationUnit(), null);
            }
        }, true);
    }
    
    public void testPartialReparseAnonymous() throws Exception {
        FileObject f2 = createFile("test/Test2.java", "package test; class Test2 { private void test() { new Runnable() { public void run() {} }; } }");
        DataObject d = DataObject.find(f2);
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        JavaSource js = JavaSource.forFileObject(f2);

        doc.putProperty(Language.class, JavaTokenId.language());
        
        //initialize the tokens hierarchy:
        TokenSequence<?> ts = TokenHierarchy.get(doc).tokenSequence();
        
        ts.moveStart();
        
        while (ts.moveNext());
        
        final AtomicReference<CompilationUnitTree> tree = new AtomicReference<CompilationUnitTree>();
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
                tree.set(parameter.getCompilationUnit());
            }
        }, true);
        
        doc.insertString(doc.getText(0, doc.getLength()).indexOf("new"), "int i = 0;", null);
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(final CompilationController parameter) throws Exception {
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
                System.err.println(parameter.getText());
                assertSame(tree.get(), parameter.getCompilationUnit());
                assertEquals(parameter.getDiagnostics().toString(), 0, parameter.getDiagnostics().size());
            }
        }, true);
    }

    public void testPartialReparseSanity225977() throws Exception {
        FileObject f2 = createFile("test/Test2.java", "package test; class Test2 { private void test() { TreePath toSplit = null; if (toSplit == null) { toSplit =  } }");
        DataObject d = DataObject.find(f2);
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        JavaSource js = JavaSource.forFileObject(f2);

        doc.putProperty(Language.class, JavaTokenId.language());
        
        //initialize the tokens hierarchy:
        TokenSequence<?> ts = TokenHierarchy.get(doc).tokenSequence();
        
        ts.moveStart();
        
        while (ts.moveNext());
        
        final AtomicReference<CompilationUnitTree> tree = new AtomicReference<CompilationUnitTree>();
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
                tree.set(parameter.getCompilationUnit());
            }
        }, true);
        
        doc.insertString(doc.getText(0, doc.getLength()).indexOf("if"), " ", null);
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(final CompilationController parameter) throws Exception {
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
                
                assertSame(tree.get(), parameter.getCompilationUnit());
                
                new TreePathScanner<Void, long[]>() {

                    @Override
                    public Void scan(Tree tree, long[] parentSpan) {
                        if (tree == null) return null;
                        if (parameter.getTreeUtilities().isSynthetic(new TreePath(getCurrentPath(), tree))) return null;
                        long start = parameter.getTrees().getSourcePositions().getStartPosition(parameter.getCompilationUnit(), tree);
                        long end   = parameter.getTrees().getSourcePositions().getEndPosition(parameter.getCompilationUnit(), tree);
                        assertTrue(tree.toString() + ":" + start + "-" + end, start <= end);
                        if (parentSpan != null) {
                            assertTrue(parentSpan[0] <= start);
                            assertTrue(end <= parentSpan[1]);
                        }
                        return super.scan(tree, new long[] {start, end});
                    }
                    
                }.scan(parameter.getCompilationUnit(), null);
            }
        }, true);
    }
    
    public void testIfMissingObjectOnBootCPUseCPToGuessSourceLevel() throws Exception {
        Source ret = guessSourceLevel(false, false, false);
        assertEquals("Downgraded to 1.4", Source.JDK1_4, ret);
    }

    public void testIfObjectPresentOnBootDontUseCPToGuessSourceLevel() throws Exception {
        Source ret = guessSourceLevel(true, false, false);
        assertEquals("Downgraded to 1.4, as Object on bootCP, but no AssertError", Source.JDK1_3, ret);
    }

    public void testIfMissingObjectOnBootCPUseCPToGuessSourceLevelWithStringBuilder() throws Exception {
        Source ret = guessSourceLevel(false, true, false);
        assertEquals("Keeps 1.7, as Object and StringBuilder on bootCP, but no AutoCloseable", Source.JDK1_7, ret);
    }

    public void testIfMissingObjectOnBootCPUseCPToGuessSourceLevelWithStringBuilderAndAutoCloseable() throws Exception {
        Source ret = guessSourceLevel(false, true, true);
        assertEquals("Kept to 1.7", Source.JDK1_7, ret);
    }
    
    private Source guessSourceLevel(boolean objectOnBCP, boolean sbOnCP, boolean acOnCP) throws Exception {
        clearWorkDir();
        File bcp = new File(getWorkDir(), "bootcp");
        bcp.mkdirs();
        
        File cp = new File(getWorkDir(), "cp");
        cp.mkdirs();
        
        File src = new File(getWorkDir(), "src");
        src.mkdirs();

        if (objectOnBCP) {
            copyResource(
                JavacParserTest.class.getResource("/java/lang/Object.class"),
                new File(new File(new File(bcp, "java"), "lang"), "Object.class")
            );
        }

        copyResource(
            JavacParserTest.class.getResource("/java/lang/AssertionError.class"),
            new File(new File(new File(cp, "java"), "lang"), "AssertionError.class")
        );
        
        if (sbOnCP) {
            copyResource(
                JavacParserTest.class.getResource("/java/lang/StringBuilder.class"),
                new File(new File(new File(cp, "java"), "lang"), "StringBuilder.class")
            );
        }

        if (acOnCP) {
            copyResource(
                JavacParserTest.class.getResource("/java/lang/AutoCloseable.class"),
                new File(new File(new File(cp, "java"), "lang"), "AutoCloseable.class")
            );
        }
        
        ClasspathInfo info = ClasspathInfo.create(
            ClassPathSupport.createClassPath(bcp.toURI().toURL()), 
            ClassPathSupport.createClassPath(cp.toURI().toURL()), 
            ClassPathSupport.createClassPath(src.toURI().toURL())
        );
        
        return JavacParser.validateSourceLevel("1.7", info, false);
    }
    
    private FileObject createFile(String path, String content) throws Exception {
        FileObject file = FileUtil.createData(sourceRoot, path);
        TestUtilities.copyStringToFile(file, content);

        return file;
    }

    private void prepareTest() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
    }
    
    private static void copyResource(URL resource, File file) throws IOException {
        assertNotNull("Resource found", resource);
        file.getParentFile().mkdirs();
        assertTrue("New file " + file + " created", file.createNewFile());
        FileOutputStream os = new FileOutputStream(file);
        InputStream is = resource.openStream();
        FileUtil.copy(is, os);
        is.close();
        os.close();
    }
    
    private static final CompilerSettingsImpl settings = new CompilerSettingsImpl();
    
    private static final class CompilerSettingsImpl extends CompilerSettings {
        private String commandLine;
        @Override
        protected String buildCommandLine(FileObject file) {
            return commandLine;
        }
        
    }
}
