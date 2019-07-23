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

package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.tools.javac.code.Source;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
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
import org.netbeans.modules.openide.util.GlobalLookup;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.java.source.base.SourceLevelUtils;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author lahvac
 */
public class JavacParserTest extends NbTestCase {

    public JavacParserTest(String name) {
        super(name);
    }

    private FileObject sourceRoot;
    private FileObject cp;

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {settings});
        clearWorkDir();
        prepareTest();
    }

    public void testMultiSource() throws Exception {
        FileObject f1 = createFile("test/Test1.java", "package test; class Test1");
        FileObject f2 = createFile("test/Test2.java", "package test; class Test2{}");
        FileObject f3 = createFile("test/Test3.java", "package test; class Test3{}");

        ClasspathInfo cpInfo = ClasspathInfo.create(f2);
        JavaSource js = JavaSource.create(cpInfo, f2, f3);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        js.runUserActionTask(new Task<CompilationController>() {
            TypeElement storedJLObject;
            public void run(CompilationController parameter) throws Exception {
                if ("Test3".equals(parameter.getFileObject().getName())) {
                    TypeElement te = parameter.getElements().getTypeElement("test.Test1");
                    assertNotNull(te);
                    assertNotNull(parameter.getTrees().getPath(te));
                }
                assertEquals(Phase.PARSED, parameter.toPhase(Phase.PARSED));
                assertNotNull(parameter.getCompilationUnit());
                TypeElement jlObject = parameter.getElements().getTypeElement("java.lang.Object");

                if (storedJLObject == null) {
                    storedJLObject = jlObject;
                } else {
                    assertEquals(storedJLObject, jlObject);
                }
            }
        }, true);
    }

    public void testMultiSourceVanilla() throws Exception {
        Lookup noSP = Lookups.exclude(Lookup.getDefault(), JavacParser.SequentialParsing.class);
        GlobalLookup.execute(noSP, () -> {
            try {
                FileObject f1 = createFile("test/Test1.java", "package test; class Test1");
                FileObject f2 = createFile("test/Test2.java", "package test; class Test2{}");
                FileObject f3 = createFile("test/Test3.java", "package test; class Test3{}");

                ClasspathInfo cpInfo = ClasspathInfo.create(f2);
                JavaSource js = JavaSource.create(cpInfo, f2, f3);

                SourceUtilsTestUtil.compileRecursively(sourceRoot);

                js.runUserActionTask(new Task<CompilationController>() {
                    TypeElement storedJLObject;
                    public void run(CompilationController parameter) throws Exception {
                        assertEquals(Phase.PARSED, parameter.toPhase(Phase.PARSED));
                        assertNotNull(parameter.getCompilationUnit());
                        TypeElement jlObject = parameter.getElements().getTypeElement("java.lang.Object");

                        if (storedJLObject == null) {
                            storedJLObject = jlObject;
                        } else {
                            assertFalse(Objects.equals(storedJLObject, jlObject));
                        }
                    }
                }, true);
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }
        });
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
                
                new ErrorAwareTreePathScanner<Void, long[]>() {

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
                
                new ErrorAwareTreePathScanner<Void, long[]>() {

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
    
    public void testInvalidFile654() throws Exception {
        FileObject f = createFile("test/Test.java", "package test; class Test { }");
        JavaSource js = JavaSource.forFileObject(f);


        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                f.delete();
                assertTrue(Phase.RESOLVED.compareTo(parameter.toPhase(Phase.RESOLVED)) <= 0);
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
        assertEquals("Keeps 1.7, as Object and StringBuilder on bootCP, but no AutoCloseable", SourceLevelUtils.JDK1_7, ret);
    }

    public void testIfMissingObjectOnBootCPUseCPToGuessSourceLevelWithStringBuilderAndAutoCloseable() throws Exception {
        Source ret = guessSourceLevel(false, true, true);
        assertEquals("Kept to 1.7", SourceLevelUtils.JDK1_7, ret);
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
    
    public void testEmbeddedJava() throws Exception {
        Lookups.executeWith(Lookups.exclude(Lookup.getDefault(), JavacParser.SequentialParsing.class), () -> {
            try {
                //set-up taken from EmbeddingTest:
                MockMimeLookup.setInstances (
                    MimePath.get ("text/jsh"),
                    new ParserFactory () {
                        public Parser createParser (Collection<Snapshot> snapshots2) {
                            return new Parser () {
                                private Snapshot last;

                                public void parse (Snapshot snapshot, org.netbeans.modules.parsing.api.Task task, SourceModificationEvent event) throws ParseException {
                                    last = snapshot;
                                }

                                public Parser.Result getResult (org.netbeans.modules.parsing.api.Task task) throws ParseException {
                                    return new Parser.Result(last) {
                                        public void invalidate () {
                                        }
                                    };
                                }

                                public void cancel () {
                                }

                                public void addChangeListener (ChangeListener changeListener) {
                                }

                                public void removeChangeListener (ChangeListener changeListener) {
                                }

                                @Override
                                public String toString () {
                                    return "TestParser";
                                }
                            };
                        }
                    },
                    new TaskFactory () {
                        public Collection<SchedulerTask> create (Snapshot snapshot) {
                            return Arrays.asList (new SchedulerTask[] {
                                new EmbeddingProvider() {
                                    public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                        Embedding embedding = snapshot.create("JAVA".length(), snapshot.getText().length() - "JAVA".length(), "text/x-java");
                                        return Arrays.asList (new Embedding[] {
                                            embedding
                                        });
                                    }

                                    public int getPriority () {
                                        return 10;
                                    }

                                    public void cancel () {
                                    }

                                    @Override
                                    public String toString () {
                                        return "TestEmbeddingProvider " + getPriority ();
                                    }
                                },
                            });
                        }
                    }

                );

                clearWorkDir ();
                FileObject workDir = FileUtil.toFileObject (getWorkDir ());
                FileObject testFile = FileUtil.createData (workDir, "bla.jsh");
                FileUtil.setMIMEType ("jsh", "text/jsh");
                try (OutputStreamWriter writer = new OutputStreamWriter (testFile.getOutputStream ())) {
                    writer.append ("JAVAclass T {}");
                }
                assertEquals(Kind.SOURCE, FileObjects.sourceFileObject(testFile, workDir).getKind());
                org.netbeans.modules.parsing.api.Source source = org.netbeans.modules.parsing.api.Source.create(testFile);
                ParserManager.parse(Collections.singletonList(source), new UserTask() {
                    @Override
                    public void run(ResultIterator ri) throws Exception {
                        Embedding emb = ri.getEmbeddings().iterator().next();
                        assertEquals("text/x-java", emb.getMimeType());
                        CompilationController cc = CompilationController.get(ri.getResultIterator(emb).getParserResult());
                        cc.toPhase(Phase.RESOLVED);
                        assertEquals(Collections.emptyList(), cc.getDiagnostics());
                        assertEquals("T", cc.getTopLevelElements().get(0).getQualifiedName().toString());
                    }
                });
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    public void testValidateCompilerOptions() {
        List<String> input = Arrays.asList("--add-exports", "foo/bar=foobar",
                                           "--add-exports=foo2/bar=foobar",
                                           "--limit-modules", "foo",
                                           "--add-modules", "foo",
                                           "--add-reads", "foo=foo2");
        assertEquals(Collections.emptyList(), JavacParser.validateCompilerOptions(input, com.sun.tools.javac.code.Source.lookup("1.8")));
        assertEquals(input, JavacParser.validateCompilerOptions(input, com.sun.tools.javac.code.Source.lookup("9")));
        assertEquals(input, JavacParser.validateCompilerOptions(input, com.sun.tools.javac.code.Source.lookup("10")));
    }

    public void testMissingSupertype() throws Exception {
        FileObject file = createFile("test/Test.java", "package test; class Test extends Super {}");

        createFile("test/Super.java", "package test; class Super extends Missing {}");

        List<String> missingCompileOptions = Arrays.asList("-source", "8", "-d", FileUtil.toFile(cp).getAbsolutePath());
        SimpleJavaFileObject missingFO = new SimpleJavaFileObject(URI.create("mem://Missing.java"), Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return "package test; public class Missing {}";
            }
            @Override
            public boolean isNameCompatible(String simpleName, Kind kind) {
                return "Missing".equals(simpleName);
            }
        };

        assertTrue(ToolProvider.getSystemJavaCompiler().getTask(null, null, null, missingCompileOptions, null, Arrays.asList(missingFO)).call());

        FileObject testClass = cp.getFileObject("test/Missing.class");

        assertNotNull(testClass);

        JavaSource js = JavaSource.forFileObject(file);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        testClass.delete();

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
            }
        }, true);
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
        cp = workFO.createFolder("cp");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, new FileObject[] {cp});
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
