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

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.tools.Diagnostic;
import com.sun.source.tree.*;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.SourcePositions;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import junit.framework.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.parsing.DocPositionRegion;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.TestUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.indexing.IndexingUtils;
import org.netbeans.modules.parsing.lucene.IndexFactory;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManagerTestUtilities;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
/**
 *
 * @author Tomas Zezula
 */
public class JavaSourceTest extends NbTestCase {

    static {
        JavaSourceTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", JavaSourceTest.Lkp.class.getName());
        Assert.assertEquals(JavaSourceTest.Lkp.class, Lookup.getDefault().getClass());
    }

    public static class Lkp extends ProxyLookup {

        private static Lkp DEFAULT;

        public Lkp () {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = Lkp.class.getClassLoader();
            this.setLookups(
                 new Lookup [] {
                    Lookups.metaInfServices(l),
                    Lookups.singleton(l),
            });
        }

        public void setLookupsWrapper(Lookup... l) {
            setLookups(l);
        }

    }

    private static final String REPLACE_PATTERN = "/*TODO:Changed-by-test*/";
    private static final String TEST_FILE_CONTENT=
                "public class {0} '{\n"+
                "   public static void main (String[] args) {\n"+
                "       javax.swing.JTable table = new javax.swing.JTable ();\n"+
                "       Class c = table.getModel().getClass();\n"+
                "       "+REPLACE_PATTERN+"\n"+
                "   }\n"+
                "}//end'\n";



    public JavaSourceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
//        TestSuite suite = new NbTestSuite(JavaSourceTest.class);
        TestSuite suite = new NbTestSuite ();
//        suite.addTest(new JavaSourceTest("testPhaseCompletionTask"));
//        suite.addTest(new JavaSourceTest("testCompileControlJob"));
        suite.addTest(new JavaSourceTest("testModificationJob"));
//        suite.addTest(new JavaSourceTest("testInterference"));
        suite.addTest(new JavaSourceTest("testDocumentChanges"));
        suite.addTest(new JavaSourceTest("testMultipleFiles"));
        suite.addTest(new JavaSourceTest("testMultipleFilesSameJavac"));
        suite.addTest(new JavaSourceTest("testMultipleFilesWithErrors"));
        /*
        suite.addTest(new JavaSourceTest("testParsingDelay"));
//        suite.addTest(new JavaSourceTest("testJavaSourceIsReclaimable"));     fails in trunk
        suite.addTest(new JavaSourceTest("testChangeInvalidates"));
        suite.addTest(new JavaSourceTest("testInvalidatesCorrectly"));
        suite.addTest(new JavaSourceTest("testCancelCall"));
        suite.addTest(new JavaSourceTest("testMultiJavaSource"));
        suite.addTest(new JavaSourceTest("testEmptyJavaSource"));
        suite.addTest(new JavaSourceTest("testCancelDeadLock"));
        suite.addTest(new JavaSourceTest("testCompileTaskStartedFromPhaseTask"));
//        suite.addTest(new JavaSourceTest("testUnsharedUserActionTask"));           failing due to missing shared flag
        suite.addTest(new JavaSourceTest("testRescheduleDoesNotStore"));
//        suite.addTest(new JavaSourceTest("testNestedActions"));                           failing due to missing shared flag
//        suite.addTest(new JavaSourceTest("testCouplingErrors"));                          failing even in main
        suite.addTest(new JavaSourceTest("testRunWhenScanFinished"));
//        suite.addTest(new JavaSourceTest("testNested2"));                     fails in trunk
        suite.addTest(new JavaSourceTest("testIndexCancel"));
        suite.addTest(new JavaSourceTest("testIndexCancel2"));
        suite.addTest(new JavaSourceTest("testIndexCancel3"));
        suite.addTest(new JavaSourceTest("testRegisterSameTask"));
        suite.addTest(new JavaSourceTest("testIncrementalReparse"));
        suite.addTest(new JavaSourceTest("testCreateTaggedController"));
        suite.addTest(new JavaSourceTest("testInvalidate"));
        suite.addTest(new JavaSourceTest("testWrongClassPathWhileParsingClassFile"));
        */
        return suite;
    }


    public void testPhaseCompletionTask () throws MalformedURLException, InterruptedException, IOException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath srcPath = createSourcePath ();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), test);
        DataObject dobj = DataObject.find(test);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy h = TokenHierarchy.get(doc);
        TokenSequence ts = h.tokenSequence(JavaTokenId.language());
        Thread.sleep(500);
        CountDownLatch[] latches1 = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1)
        };
        CountDownLatch[] latches2 = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1)
        };
        AtomicInteger counter = new AtomicInteger (0);
        CancellableTask<CompilationInfo> task1 = new DiagnosticTask(latches1, counter, Phase.RESOLVED);
        CancellableTask<CompilationInfo> task2 =  new DiagnosticTask(latches2, counter, Phase.PARSED);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,task1,Phase.RESOLVED,Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,task2,Phase.PARSED,Priority.LOW, TaskIndexingMode.ALLOWED_DURING_SCAN);
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches1[0], latches2[0]}, 150000));
        assertEquals ("Called more times than expected",2,counter.getAndSet(0));
        Thread.sleep(1000);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {
                    try {
                        String text = doc.getText(0,doc.getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc.remove(index,REPLACE_PATTERN.length());
                        doc.insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }
                }
        });
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches1[1], latches2[1]}, 15000));
        assertEquals ("Called more times than expected",2,counter.getAndSet(0));
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task1);
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task2);
    }

    public void testCompileControlJob () throws MalformedURLException, IOException, InterruptedException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath srcPath = createSourcePath();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), test);
        CountDownLatch latch = new CountDownLatch (1);
        js.runUserActionTask(new CompileControlJob(latch),true);
        assertTrue ("Time out",latch.await(15,TimeUnit.SECONDS));
    }

    public void testModificationJob () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final FileObject testFile2 = createTestFile("Test2");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), testFile1, testFile2);
        CountDownLatch latch = new CountDownLatch (2);
        js.runModificationTask(new WorkingCopyJob (latch)).commit();
        assertTrue ("Time out",latch.await(15,TimeUnit.SECONDS));
    }

    public void testMultipleFiles () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final FileObject testFile2 = createTestFile("Test2");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), testFile1, testFile2);
        boolean[] test1 = new boolean[1];
        boolean[] test2 = new boolean[1];
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                
                //TODO: safer checks!
                if (parameter.getCompilationUnit().toString().contains("Test1")) {
                    test1[0] = true;
                } else if (parameter.getCompilationUnit().toString().contains("Test2")) {
                    test2[0] = true;
                } else {
                   fail();
                }
            }
        }, true);
        assertTrue ("Test1", test1[0]);
        assertTrue ("Test2", test2[0]);
    }

    public void testMultipleFilesSameJavac() throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final FileObject testFile2 = createTestFile("Test2");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), testFile1, testFile2);
        js.runUserActionTask(new Task<CompilationController>() {
            private JavacTaskImpl seenTask;

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                
                JavacTaskImpl currentTask = parameter.impl.getJavacTask();
                
                if (seenTask == null) {
                    seenTask= currentTask;
                } else if (seenTask != currentTask) {
                   fail();
                }
            }
        }, true);
    }

    public void testInterference () throws MalformedURLException, IOException, InterruptedException {
        FileObject testFile1 = createTestFile ("Test1");
        FileObject testFile2 = createTestFile ("Test2");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath srcPath = createSourcePath();
        JavaSource js1 = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), testFile1);
        JavaSource js2 = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), testFile2);
        DataObject dobj = DataObject.find(testFile1);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy h = TokenHierarchy.get(doc);
        TokenSequence ts = h.tokenSequence(JavaTokenId.language());
        Thread.sleep(500);
        CountDownLatch[] latches1 = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1),
        };
        CountDownLatch[] latches2 = new CountDownLatch[] {
            new CountDownLatch (1),
        };
        CountDownLatch latch3 = new CountDownLatch (1);
        AtomicInteger counter = new AtomicInteger (0);

        DiagnosticTask task1 = new DiagnosticTask(latches1, counter, Phase.RESOLVED);
        CancellableTask<CompilationInfo> task2 = new DiagnosticTask(latches2, counter, Phase.RESOLVED);

        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js1,task1,Phase.RESOLVED,Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        js2.runUserActionTask(new CompileControlJob(latch3),true);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js2,task2,Phase.RESOLVED,Priority.MAX, TaskIndexingMode.ALLOWED_DURING_SCAN);
        boolean result = waitForMultipleObjects (new CountDownLatch[] {latches1[0], latches2[0], latch3}, 15000);
        if (!result) {
            assertTrue (String.format("Time out, latches1[0]: %d latches2[0]: %d latches3: %d",latches1[0].getCount(), latches2[0].getCount(), latch3.getCount()), false);
        }
        assertEquals ("Called more times than expected",2,counter.getAndSet(0));

        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {
                    try {
                        String text = doc.getText(0,doc.getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc.remove(index,REPLACE_PATTERN.length());
                        doc.insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }
                }
        });
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches1[1]}, 15000));
        assertEquals ("Called more times than expected",1,counter.getAndSet(0));
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js1,task1);
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js2,task2);
    }

    public void testDocumentChanges () throws Exception {
        FileObject testFile1 = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath srcPath = createSourcePath();
        JavaSource js1 = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), testFile1);

        final CountDownLatch start = new CountDownLatch (1);
        final CountDownLatch stop =  new CountDownLatch (1);
        final AtomicBoolean last = new AtomicBoolean (false);
        final AtomicInteger counter = new AtomicInteger (0);

        CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo>() {

            private int state = 0;

            public void cancel() {
            }

            public void run(CompilationInfo ci) throws Exception {
                switch (state) {
                    case 0:
                        state = 1;
                        start.countDown();
                        break;
                    case 1:
                        counter.incrementAndGet();
                        if (last.get()) {
                            stop.countDown();
                        }
                        break;
                }
            }
        };
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js1,task,Phase.PARSED,Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
        start.await();
        Thread.sleep(500);
        final DataObject dobj = DataObject.find(testFile1);
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy h = TokenHierarchy.get(doc);
        TokenSequence ts = h.tokenSequence(JavaTokenId.language());
        for (int i=0; i<10; i++) {
            if (i == 9) {
                last.set(true);
            }
            NbDocument.runAtomic (doc,
                new Runnable () {
                    public void run () {
                        try {
                            doc.insertString(0," ",null);
                        } catch (BadLocationException ble) {
                            ble.printStackTrace(System.out);
                        }
                    }
            });
            Thread.sleep(100);
        }
        assertTrue ("Time out",stop.await(15000, TimeUnit.MILLISECONDS));
        assertEquals("Called more time than expected",1,counter.get());
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js1,task);
    }


    public void testParsingDelay() throws MalformedURLException, InterruptedException, IOException, BadLocationException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath sourcePath = createSourcePath();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, sourcePath), test);
        DataObject dobj = DataObject.find(test);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy h = TokenHierarchy.get(doc);
        TokenSequence ts = h.tokenSequence(JavaTokenId.language());
        Thread.sleep(500);  //It may happen that the js is invalidated before the dispatch of task is done and the test of timers may fail
        CountDownLatch[] latches = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1)
        };
        long[] timers = new long[2];
        AtomicInteger counter = new AtomicInteger (0);
        CancellableTask<CompilationInfo> task = new DiagnosticTask(latches, timers, counter, Phase.PARSED);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js,task,Phase.PARSED, Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[0]}, 15000));
        assertEquals ("Called more times than expected",1,counter.getAndSet(0));
        long start = System.currentTimeMillis();
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {
                    try {
                        String text = doc.getText(0,doc.getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc.remove(index,REPLACE_PATTERN.length());
                        doc.insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }
                }
        });
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[1]}, 15000));
        assertEquals ("Called more times than expected",1,counter.getAndSet(0));
        assertTrue("Took less time than expected time=" + (timers[1] - start), (timers[1] - start) >= TestUtil.getReparseDelay());
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task);
    }

    public void testJavaSourceIsReclaimable() throws MalformedURLException, InterruptedException, IOException, BadLocationException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath srcPath = createSourcePath();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), test);
        DataObject dobj = DataObject.find(test);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument[] doc = new StyledDocument[] {ec.openDocument()};
        doc[0].putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy h = TokenHierarchy.get(doc[0]);
        TokenSequence ts = h.tokenSequence(JavaTokenId.language());
        Thread.sleep(500);
        CountDownLatch[] latches = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1)
        };
        AtomicInteger counter = new AtomicInteger (0);
        CancellableTask<CompilationInfo> task = new DiagnosticTask(latches, counter, Phase.PARSED);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js,task,Phase.PARSED,Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[0]}, 15000));

        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc[0],
            new Runnable () {
                public void run () {
                    try {
                        String text = doc[0].getText(0,doc[0].getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc[0].remove(index,REPLACE_PATTERN.length());
                        doc[0].insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }
                }
        });

        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[1]}, 15000));
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task);

        Reference jsWeak = new WeakReference(js);
        Reference testWeak = new WeakReference(test);

        SaveCookie sc = (SaveCookie) dobj.getCookie(SaveCookie.class);

        sc.save();

        sc = null;

        js = null;
        test = null;
        dobj = null;
        ec = null;
        doc[0] = null;

        //give the worker thread chance to remove the task:
        //if the tests starts to fail randomly, try to increment the timeout
        Thread.sleep(1000);

        assertGC("JavaSource is reclaimable", jsWeak);
        //the file objects is held by the timers component
        //and maybe others:
        assertGC("FileObject is reclaimable", testWeak);
    }

    public void testChangeInvalidates() throws MalformedURLException, InterruptedException, IOException, BadLocationException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath srcPath = createSourcePath();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), test);
        int originalReparseDelay = TestUtil.getReparseDelay();

        try {
            TestUtil.setReparseDelay(JavaSourceAccessor.getINSTANCE().getSources(js).iterator().next(),Integer.MAX_VALUE,false); //never automatically reparse
            CountDownLatch latch1 = new CountDownLatch (1);
            final CountDownLatch latch2 = new CountDownLatch (1);
            AtomicInteger counter = new AtomicInteger (0);
            CancellableTask<CompilationInfo> task = new DiagnosticTask(new CountDownLatch[] {latch1}, counter, Phase.PARSED);
            JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js,task,Phase.PARSED,Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
            assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latch1}, 15000));

            DataObject dobj = DataObject.find(test);
            EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
            final StyledDocument[] doc = new StyledDocument[] {ec.openDocument()};
            doc[0].putProperty(Language.class, JavaTokenId.language());
            TokenHierarchy h = TokenHierarchy.get(doc[0]);
            TokenSequence ts = h.tokenSequence(JavaTokenId.language());
            Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
            NbDocument.runAtomic (doc[0],
                    new Runnable () {
                public void run () {
                    try {
                        String text = doc[0].getText(0,doc[0].getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc[0].remove(index,REPLACE_PATTERN.length());
                        doc[0].insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }
                }
            });

            final boolean[] contentCorrect = new boolean[1];

            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controler) {
                    try {
                        controler.toPhase(Phase.PARSED);
                        contentCorrect[0] = controler.getText().contains("System.out.println");
                        latch2.countDown();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            },true);

            assertTrue("Time out",waitForMultipleObjects(new CountDownLatch[] {latch2}, 15000));
            assertTrue("Content incorrect", contentCorrect[0]);

            JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task);
        } finally {
            if (js != null) {
                TestUtil.setReparseDelay(JavaSourceAccessor.getINSTANCE().getSources(js).iterator().next(), originalReparseDelay, true);
            }
        }
    }

    //this test is quite unreliable (it often passes even in cases it should fail):
    public void testInvalidatesCorrectly() throws MalformedURLException, InterruptedException, IOException, BadLocationException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath srcPath = createSourcePath();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), test);
        DataObject dobj = DataObject.find(test);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument[] doc = new StyledDocument[] {ec.openDocument()};
        doc[0].putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy h = TokenHierarchy.get(doc[0]);
        TokenSequence ts = h.tokenSequence(JavaTokenId.language());
        Thread.sleep (500);
        CountDownLatch[] latches = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1),
            new CountDownLatch (1),
        };
        AtomicInteger counter = new AtomicInteger (0);
        DiagnosticTask task = new DiagnosticTask(latches, counter, Phase.PARSED);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js,task,Phase.PARSED,Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[0]}, 15000));
        final int[] index = new int[1];
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc[0],
                new Runnable () {
            public void run () {
                try {
                    String text = doc[0].getText(0,doc[0].getLength());
                    index[0] = text.indexOf(REPLACE_PATTERN);
                    assertTrue (index[0] != -1);
                    doc[0].remove(index[0],REPLACE_PATTERN.length());
                    doc[0].insertString(index[0],"System.out.println();",null);
                } catch (BadLocationException ble) {
                    ble.printStackTrace(System.out);
                }
            }
        });

        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[1]}, 15000));
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic(doc[0],
                new Runnable() {
            public void run() {
                try {
                    doc[0].insertString(index[0],"System.out.println();",null);
                } catch (BadLocationException ble) {
                    ble.printStackTrace(System.out);
                }
            }
        });
        //not sure how to make this 100% reliable.
        //this task has to be the first run after the previous change to document.
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controler) {
                try {
                    controler.toPhase(Phase.PARSED);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        },true);

        assertTrue("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[2]}, 15000));

        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task);
    }


    public void testCancelCall () throws Exception {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        ClassPath srcPath = createSourcePath();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, srcPath), test);
        WaitTask wt = new WaitTask (3000);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,wt, Phase.PARSED, Priority.BELOW_NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
        Thread.sleep(1000);
        WaitTask wt2 = new WaitTask (0);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,wt2, Phase.PARSED,Priority.MAX, TaskIndexingMode.ALLOWED_DURING_SCAN);
        Thread.sleep(10000);
        int cancelCount = wt.getCancelCount();
        assertEquals(1,cancelCount);
        int runCount = wt.getRunCount();
        assertEquals(2,runCount);
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js,wt);
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js,wt2);
    }

    public void testMultiJavaSource () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final FileObject testFile2 = createTestFile("Test2");
        final FileObject testFile3 = createTestFile("Test3");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        final JavaSource js = JavaSource.create(cpInfo,testFile1, testFile2, testFile3);
        CountDownLatch latch = new CountDownLatch (3);
        CompileControlJob ccj = new CompileControlJob (latch);
        ccj.multiSource = true;
        js.runUserActionTask(ccj,true);
        assertTrue(waitForMultipleObjects(new CountDownLatch[] {latch},10000));
//todo: restart check not yet implemented in the parsing api.
//        latch = new CountDownLatch (4);
//        CompileControlJobWithOOM ccj2 = new CompileControlJobWithOOM (latch,1);
//        js.runUserActionTask(ccj2,true);
//        assertTrue(waitForMultipleObjects(new CountDownLatch[] {latch},10000));
    }

    public void testEmptyJavaSource () throws Exception {
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo);
        CountDownLatch latch = new CountDownLatch (1);
        EmptyCompileControlJob ccj = new EmptyCompileControlJob (latch);
        js.runUserActionTask(ccj,true);
        assertTrue(waitForMultipleObjects(new CountDownLatch[] {latch},10000));
    }

    public void testCancelDeadLock () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        js.runUserActionTask(
                new Task<CompilationController>() {

                    public void run(CompilationController parameter) throws Exception {
                        final Thread t = new Thread (new Runnable() {
                            public void run () {
                                try {
                                js.runUserActionTask(new Task<CompilationController>() {

                                    public void run(CompilationController parameter) throws Exception {
                                    }
                                },true);
                                } catch (IOException e) {
                                    AssertionError er = new AssertionError ();
                                    e.initCause(e);
                                    throw er;
                                }
                            }
                        });
                        t.start();
                        Thread.sleep(1000);
                        js.runUserActionTask (new Task<CompilationController>() {

                            public void run(CompilationController parameter) throws Exception {
                            }
                        },true);
                    }
                },true
        );
    }

    public void testCompileTaskStartedFromPhaseTask () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final AtomicBoolean canceled = new AtomicBoolean (false);
        final CountDownLatch latch = new CountDownLatch (1);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,new CancellableTask<CompilationInfo> () {

            private boolean called = false;

            public void cancel() {
                canceled.set(true);
            }

            public void run(CompilationInfo parameter) throws Exception {
                if (!called) {
                    js.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController parameter) throws Exception {
                        }
                    },true);
                    called = true;
                    latch.countDown();
                }
            }
        }, Phase.PARSED, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
        assertTrue (waitForMultipleObjects(new CountDownLatch[] {latch}, 10000));
        assertFalse ("Cancel called even for JavaSource dispatch thread!",canceled.get());
    }

    public void testUnsharedUserActionTask () throws IOException {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final int[] identityHashCodes = new int[3];
        js.runUserActionTask(new Task<CompilationController> () {

            public void run (CompilationController c) {
                identityHashCodes[0] = System.identityHashCode(c.impl);
            }

        },true);

        js.runUserActionTask(new Task<CompilationController> () {

            public void run (CompilationController c) {
                identityHashCodes[1] = System.identityHashCode(c.impl);
            }

        },false);


        js.runUserActionTask(new Task<CompilationController> () {

            public void run (CompilationController c) {
                identityHashCodes[2] = System.identityHashCode(c.impl);
            }

        },false);

        assertEquals(identityHashCodes[0], identityHashCodes[1]);
        assertFalse(identityHashCodes[1] == identityHashCodes[2]);
    }

    public void testRescheduleDoesNotStore() throws IOException, InterruptedException {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath srcPath = createSourcePath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final CountDownLatch waitFor = new CountDownLatch (1);
        final CountDownLatch second = new CountDownLatch (3);
        final int[] count = new int[1];
        final CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo> () {
            public void cancel() { }
            public void run(CompilationInfo parameter) throws Exception {
                count[0]++;
                second.countDown();
            }
        };
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,new CancellableTask<CompilationInfo> () {
            public void cancel() { }
            public void run(CompilationInfo parameter) throws Exception {
                waitFor.await();
            }
        }, Phase.PARSED, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,task, Phase.PARSED, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
        JavaSourceAccessor.getINSTANCE().rescheduleTask(js,task);
        JavaSourceAccessor.getINSTANCE().rescheduleTask(js,task);
        waitFor.countDown();
        second.await(10, TimeUnit.SECONDS);
        assertEquals(1, count[0]);
    }


    public void testNestedActions () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final Object[] delegateRef = new Object[1];
        // 1)  Two consequent shared tasks have to share CompilationInfo
        js.runUserActionTask(new Task<CompilationController>() {

            public void run (CompilationController control) {
                delegateRef[0] = control.impl;
            }
        }, true);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run (CompilationController control) {
                assertTrue(delegateRef[0] == control.impl);
            }

        }, true);

        //2) Task following the unshared task has to have new CompilationInfo
        js.runUserActionTask(new Task<CompilationController>() {

            public void run (CompilationController control) {
                delegateRef[0] = control.impl;
            }

        }, false);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run (CompilationController control) {
                assertTrue(delegateRef[0] != control.impl);
            }

        }, true);

        //3) Shared task started from shared task has to have CompilationInfo from the parent
        //   The shared task follong these tasks has to have the same CompilationInfo
        js.runUserActionTask(new CancellableTask<CompilationController>() {

            public void run (CompilationController control) {
                delegateRef[0] = control.impl;
                final Object[] delegateRef2 = new Object[] {control.impl};
                try {
                    js.runUserActionTask(new Task<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef2[0] == control.impl);
                        }

                    }, true);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }
            public void cancel () {}
        }, true);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run (CompilationController controll) {
                assertTrue(delegateRef[0] == controll.impl);
            }
        }, true);

        //4) Shared task started from unshared task has to have CompilationInfo from the parent (unshared task)
        //   The shared task follong these tasks has to have new CompilationInfo
        js.runUserActionTask(new Task<CompilationController>() {

            public void run (CompilationController control) {
                delegateRef[0] = control.impl;
                final Object[] delegateRef2 = new Object[] {control.impl};
                try {
                    js.runUserActionTask(new CancellableTask<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef2[0] == control.impl);
                        }

                        public void cancel () {}
                    }, true);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }
        }, false);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run (CompilationController controll) {
                assertTrue(delegateRef[0] != controll.impl);
            }
        }, true);

        //5) Unshared task started from unshared task has to have new CompilationInfo
        //   The shared task following these tasks has to also have new CompilationInfo
        final Object[] delegateRef2 = new Object[1];
        js.runUserActionTask(new Task<CompilationController>() {
            public void run (CompilationController control) {
                delegateRef[0] = control.impl;
                try {
                    js.runUserActionTask(new Task<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef[0] != control.impl);
                            delegateRef2[0] = control.impl;
                        }

                    }, false);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }
            public void cancel () {}
        }, false);

        js.runUserActionTask(new Task<CompilationController>() {

            public void run (CompilationController controll) {
                assertTrue(delegateRef[0] != controll.impl);
                assertTrue(delegateRef2[0] != controll.impl);
            }

        }, true);

        //6)Shared task(3) started from unshared task(2) which is started from other unshared task (1)
        //  has to see the CompilationInfo from the task (2) which is not equal to CompilationInfo from (1)
        js.runUserActionTask(new Task<CompilationController>() {
            public void run (CompilationController control) {
                delegateRef[0] = control.impl;
                try {
                    js.runUserActionTask(new Task<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef[0] != control.impl);
                            delegateRef2[0] = control.impl;
                            try {
                                js.runUserActionTask(new Task<CompilationController> () {
                                    public void run (CompilationController control) {
                                        assertTrue (delegateRef[0] != control.impl);
                                        assertTrue (delegateRef2[0] == control.impl);
                                    }
                                }, true);
                            } catch (IOException ioe) {
                                RuntimeException re = new RuntimeException ();
                                re.initCause(ioe);
                                throw re;
                            }
                        }
                    }, false);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }
        }, false);

        //6)Task(4) started after unshared task(3) started from shared task(2) which is started from other shared task (1)
        //  has to have new CompilationInfo but the task (1) (2) (3) have to have the same CompilationInfo.
        js.runUserActionTask(new Task<CompilationController>() {
            public void run (CompilationController control) {
                delegateRef[0] = control.impl;
                try {
                    js.runUserActionTask(new Task<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef[0] == control.impl);
                            try {
                                js.runUserActionTask(new Task<CompilationController> () {
                                    public void run (CompilationController control) {
                                        assertTrue (delegateRef[0] == control.impl);
                                    }
                                }, false);
                                js.runUserActionTask(new Task<CompilationController> () {
                                    public void run (CompilationController control) {
                                        assertTrue (delegateRef[0] != control.impl);
                                    }
                                }, true);
                            } catch (IOException ioe) {
                                RuntimeException re = new RuntimeException ();
                                re.initCause(ioe);
                                throw re;
                            }
                        }
                    }, true);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }
        }, true);

    }

    public void testCouplingErrors() throws Exception {
        File workdir = this.getWorkDir();
        File src1File = new File (workdir, "src1");
        src1File.mkdir();
        final FileObject src1 = FileUtil.toFileObject(src1File);

        File src2File = new File (workdir, "src2");
        src2File.mkdir();
        final FileObject src2 = FileUtil.toFileObject(src2File);

        createTestFile(src1, "test/Test.java", "package test; public class Test {private long x;}");

        final FileObject test = createTestFile(src2, "test/Test.java", "package test; public class Test {private int x;}");
        final FileObject test2 = createTestFile(src2, "test/Test2.java", "package test; public class Test2 {private Test x;}");

        File cache = new File(workdir, "cache");

        cache.mkdirs();

        SourceUtilsTestUtil2.disableLocks();
        IndexUtil.setCacheFolder(cache);

        ClassLoader l = JavaSourceTest.class.getClassLoader();
        Lkp.DEFAULT.setLookupsWrapper(
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
                Lookups.singleton(new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                try {
                    if (ClassPath.BOOT == type) {
                        return createBootPath();
                    }

                    if (ClassPath.SOURCE == type) {
                        return ClassPathSupport.createClassPath(new FileObject[] {
                            src1
                        });
                    }

                    if (ClassPath.COMPILE == type) {
                        return createCompilePath();
                    }

                    if (ClassPath.EXECUTE == type) {
                        return ClassPathSupport.createClassPath(new FileObject[] {
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        }));

        IndexingManager.getDefault().refreshIndexAndWait(src1.getURL(), null);

        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = CacheClassPath.forSourcePath(ClassPathSupport.createClassPath(new FileObject[] {src1}),false);
        final ClassPath srcPath = ClassPathSupport.createClassPath(src2);
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        final JavaSource js = JavaSource.create(cpInfo, test2, test);

        final List<FileObject> files = new ArrayList<FileObject>();

        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController cc) throws IOException {
                files.add(cc.getFileObject());
                cc.toPhase(Phase.RESOLVED);
            }
        }, true);

        assertEquals(Arrays.asList(test2, test, test), files);

        files.clear();

        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy cc) throws IOException {
                files.add(cc.getFileObject());
                cc.toPhase(Phase.RESOLVED);
            }
        });

        assertEquals(Arrays.asList(test2, test, test), files);
    }


    public void testRunWhenScanFinished () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        IndexingManager.getDefault().refreshIndexAndWait(testFile1.getParent().getURL(), null);
        Thread.sleep (1000); //Indexing task already finished, but we want to wait until JS working thread is waiting on task to dispatch
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);

        class T implements Task<CompilationController> {

            private final CountDownLatch latch;

            public T (final CountDownLatch latch) {
                assert latch != null;
                this.latch = latch;
            }

            public void run(CompilationController parameter) throws Exception {
                this.latch.countDown();
            }

        };

        class RUT implements Runnable {
            private final CountDownLatch start;
            private final CountDownLatch latch;

            public RUT (final CountDownLatch start, final CountDownLatch latch) {
                assert start != null;
                assert latch != null;
                this.start = start;
                this.latch = latch;
            }


            public void run() {
                try {
                    this.start.countDown();
                    this.latch.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        CountDownLatch latch = new CountDownLatch (1);
        Future<Void> res = js.runWhenScanFinished(new T(latch), true);
        assertEquals(0,latch.getCount());
        res.get(1,TimeUnit.SECONDS);
        assertTrue(res.isDone());
        assertFalse (res.isCancelled());

        CountDownLatch rutLatch = new CountDownLatch (1);
        CountDownLatch rutStart = new CountDownLatch (1);
        RUT rut = new RUT (rutStart, rutLatch);
        IndexingUtils.runAsScanWork(rut);
        latch = new CountDownLatch (1);
        rutStart.await();
        res = js.runWhenScanFinished(new T(latch), true);
        assertEquals(1,latch.getCount());
        try {
            res.get(1,TimeUnit.SECONDS);
            assertTrue(false);
        } catch (TimeoutException te) {
            //Pass
        }
        assertFalse(res.isDone());
        assertFalse (res.isCancelled());
        rutLatch.countDown();
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        res.get(1,TimeUnit.SECONDS);
        assertTrue(res.isDone());
        assertFalse (res.isCancelled());

        rutLatch = new CountDownLatch (1);
        rutStart = new CountDownLatch (1);
        rut = new RUT (rutStart, rutLatch);
        IndexingUtils.runAsScanWork(rut);
        latch = new CountDownLatch (1);
        rutStart.await();
        res = js.runWhenScanFinished(new T(latch), true);
        assertEquals(1,latch.getCount());
        try {
            res.get(1,TimeUnit.SECONDS);
            assertTrue(false);
        } catch (TimeoutException te) {
            //Pass
        }
        assertFalse(res.isDone());
        assertFalse (res.isCancelled());
        assertTrue (res.cancel(false));
        rutLatch.countDown();
        assertFalse(latch.await(3, TimeUnit.SECONDS));
        try {
            res.get(1,TimeUnit.SECONDS);
            assertTrue(false);
        } catch (TimeoutException te) {
            //Pass
        }
        assertFalse(res.isDone());
        assertTrue (res.isCancelled());
    }


    public void testNested2 () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController c) throws Exception {
                c.toPhase(Phase.RESOLVED);
                CompilationUnitTree ct = c.getCompilationUnit();
                List <? extends Tree> trees = ct.getTypeDecls();
                assertEquals (1,trees.size());

                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy c) throws Exception {
                        c.toPhase(Phase.RESOLVED);
                        CompilationUnitTree oldTree = c.getCompilationUnit();
                        TreeMaker tm = c.getTreeMaker();
                        ClassTree cls = tm.Class(tm.Modifiers(EnumSet.of(Modifier.STATIC)), "NewClass", Collections.<TypeParameterTree>emptyList(),
                                null, Collections.<Tree>emptyList(), Collections.<Tree>emptyList());
                        List<Tree> decls = new LinkedList<Tree> ();
                        decls.addAll (oldTree.getTypeDecls());
                        decls.add (cls);
                        CompilationUnitTree newTree = tm.CompilationUnit(oldTree.getPackageName(), oldTree.getImports(),decls, oldTree.getSourceFile());
                        c.rewrite(oldTree, newTree);
                    }
                }).commit();

                c.toPhase(Phase.RESOLVED);
                ct = c.getCompilationUnit();
                trees = ct.getTypeDecls();
                assertEquals (1, trees.size());

                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController c) throws Exception {
                        c.toPhase(Phase.RESOLVED);
                        CompilationUnitTree ct = c.getCompilationUnit();
                        List <? extends Tree> trees = ct.getTypeDecls();
                        assertEquals (2, trees.size());
                    }

                }, true);
            }

        }, true);
    }


    public void testIndexCancel() throws Exception {
        final IndexFactory oldFactory = IndexManagerTestUtilities.getIndexFactory();
        final TestIndexFactory factory = new TestIndexFactory();
        IndexManagerTestUtilities.setIndexFactory(factory);
        try {
            FileObject test = createTestFile ("Test1");
            final ClassPath bootPath = createBootPath ();
            final ClassPath compilePath = createCompilePath ();
            final ClassPath sourcePath = createSourcePath ();
            final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
            regs.register(ClassPath.SOURCE, new ClassPath[]{sourcePath});
            try {
                ClassLoader l = JavaSourceTest.class.getClassLoader();
                Lkp.DEFAULT.setLookupsWrapper(
                    Lookups.metaInfServices(l),
                    Lookups.singleton(l),
                    Lookups.singleton(new ClassPathProvider() {
                    @Override
                    public ClassPath findClassPath(FileObject file, String type) {
                        if (ClassPath.BOOT == type) {
                            return bootPath;
                        }

                        if (ClassPath.SOURCE == type) {
                            return sourcePath;
                        }

                        if (ClassPath.COMPILE == type) {
                            return compilePath;
                        }
                        return null;
                    }
                }));


                JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, sourcePath), test);
                IndexingManager.getDefault().refreshIndexAndWait(sourcePath.getRoots()[0].getURL(), null);
                DataObject dobj = DataObject.find(test);
                EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                final StyledDocument doc = ec.openDocument();
                doc.putProperty(Language.class, JavaTokenId.language());
                TokenHierarchy h = TokenHierarchy.get(doc);
                TokenSequence ts = h.tokenSequence(JavaTokenId.language());
                Thread.sleep(500);  //It may happen that the js is invalidated before the dispatch of task is done and the test of timers may fail

                final CountDownLatch ready = new CountDownLatch(1);
                final CountDownLatch change = new CountDownLatch(1);
                final CountDownLatch end = new CountDownLatch (1);
                final AtomicReference<Set<String>> result = new AtomicReference<Set<String>>(Collections.<String>emptySet());

                CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo>() {

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void run(CompilationInfo p) throws Exception {
                        ready.countDown();
                        change.await();
                        ClassIndex index = p.getClasspathInfo().getClassIndex();
                        result.set(index.getPackageNames("javax", true, EnumSet.allOf(ClassIndex.SearchScope.class)));
                        end.countDown();
                    }

                };
                factory.instance.active=true;
                JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js,task,Phase.PARSED, Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
                assertTrue(ready.await(5, TimeUnit.SECONDS));
                NbDocument.runAtomic (doc,
                    new Runnable () {
                    @Override
                        public void run () {
                            try {
                                String text = doc.getText(0,doc.getLength());
                                int index = text.indexOf(REPLACE_PATTERN);
                                assertTrue (index != -1);
                                doc.remove(index,REPLACE_PATTERN.length());
                                doc.insertString(index,"System.out.println();",null);
                            } catch (BadLocationException ble) {
                                ble.printStackTrace(System.out);
                            }
                        }
                });
                change.countDown();
                assertTrue(end.await(5, TimeUnit.SECONDS));
                assertNull(result.get());
                JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task);
            } finally {
                regs.unregister(ClassPath.SOURCE, new ClassPath[]{sourcePath});
            }
        } finally {
            IndexManagerTestUtilities.setIndexFactory(oldFactory);
        }
    }
    
    public void testIndexCancel2() throws Exception {
        final IndexFactory oldFactory = IndexManagerTestUtilities.getIndexFactory();
        final TestIndexFactory factory = new TestIndexFactory();
        IndexManagerTestUtilities.setIndexFactory(factory);
        try {
            FileObject test = createTestFile ("Test1");
            final ClassPath bootPath = createBootPath ();
            final ClassPath compilePath = createCompilePath ();
            final ClassPath sourcePath = createSourcePath ();
            final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
            regs.register(ClassPath.SOURCE, new ClassPath[]{sourcePath});
            try {
                ClassLoader l = JavaSourceTest.class.getClassLoader();
                Lkp.DEFAULT.setLookupsWrapper(
                    Lookups.metaInfServices(l),
                    Lookups.singleton(l),
                    Lookups.singleton(new ClassPathProvider() {
                    @Override
                    public ClassPath findClassPath(FileObject file, String type) {
                        if (ClassPath.BOOT == type) {
                            return bootPath;
                        }

                        if (ClassPath.SOURCE == type) {
                            return sourcePath;
                        }

                        if (ClassPath.COMPILE == type) {
                            return compilePath;
                        }
                        return null;
                    }
                }));


                JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, sourcePath), test);
                IndexingManager.getDefault().refreshIndexAndWait(sourcePath.getRoots()[0].getURL(), null);
                DataObject dobj = DataObject.find(test);
                EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                final StyledDocument doc = ec.openDocument();
                doc.putProperty(Language.class, JavaTokenId.language());
                TokenHierarchy h = TokenHierarchy.get(doc);
                TokenSequence ts = h.tokenSequence(JavaTokenId.language());
                Thread.sleep(500);  //It may happen that the js is invalidated before the dispatch of task is done and the test of timers may fail

                final CountDownLatch ready = new CountDownLatch(1);
                final CountDownLatch change = new CountDownLatch(1);
                final CountDownLatch end = new CountDownLatch (1);
                final AtomicReference<Set<String>> result = new AtomicReference<Set<String>>(Collections.<String>emptySet());
                final ThreadLocal<Boolean> me = new ThreadLocal<Boolean>();

                CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo>() {

                    @Override
                    public void cancel() {
                        if (me.get() == Boolean.TRUE) {
                            change.countDown();
                        }
                    }

                    @Override
                    public void run(CompilationInfo p) throws Exception {
                        ready.countDown();
                        change.await();
                        ClassIndex index = p.getClasspathInfo().getClassIndex();
                        result.set(index.getPackageNames("javax", true, EnumSet.allOf(ClassIndex.SearchScope.class)));
                        end.countDown();
                    }

                };
                factory.instance.active=true;
                JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js,task,Phase.PARSED, Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
                assertTrue(ready.await(5, TimeUnit.SECONDS));
                me.set(Boolean.TRUE);
                try {
                    js.runUserActionTask( new Task<CompilationController>() {
                            @Override
                            public void run (final CompilationController info) {                            
                            }
                    }, true);
                } finally {
                    me.remove();
                }
                assertTrue(end.await(5, TimeUnit.SECONDS));
                assertNull(result.get());
                JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task);
            } finally {
                regs.unregister(ClassPath.SOURCE, new ClassPath[]{sourcePath});
            }
        } finally {
            IndexManagerTestUtilities.setIndexFactory(oldFactory);
        }
    }
    
    public void testIndexCancel3() throws Exception {
        final IndexFactory oldFactory = IndexManagerTestUtilities.getIndexFactory();
        final TestIndexFactory factory = new TestIndexFactory();
        IndexManagerTestUtilities.setIndexFactory(factory);
        try {
            FileObject test = createTestFile ("Test1");
            final ClassPath bootPath = createBootPath ();
            final ClassPath compilePath = createCompilePath ();
            final ClassPath sourcePath = createSourcePath ();
            final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
            regs.register(ClassPath.SOURCE, new ClassPath[]{sourcePath});
            try {
                ClassLoader l = JavaSourceTest.class.getClassLoader();
                Lkp.DEFAULT.setLookupsWrapper(
                    Lookups.metaInfServices(l),
                    Lookups.singleton(l),
                    Lookups.singleton(new ClassPathProvider() {
                    @Override
                    public ClassPath findClassPath(FileObject file, String type) {
                        if (ClassPath.BOOT == type) {
                            return bootPath;
                        }

                        if (ClassPath.SOURCE == type) {
                            return sourcePath;
                        }

                        if (ClassPath.COMPILE == type) {
                            return compilePath;
                        }
                        return null;
                    }
                }));


                final JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, sourcePath), test);
                IndexingManager.getDefault().refreshIndexAndWait(sourcePath.getRoots()[0].getURL(), null);
                DataObject dobj = DataObject.find(test);
                EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                final StyledDocument doc = ec.openDocument();
                doc.putProperty(Language.class, JavaTokenId.language());
                TokenHierarchy h = TokenHierarchy.get(doc);
                TokenSequence ts = h.tokenSequence(JavaTokenId.language());
                Thread.sleep(500);  //It may happen that the js is invalidated before the dispatch of task is done and the test of timers may fail                
                final CountDownLatch readyTask1 = new CountDownLatch(1);
                final CountDownLatch changeTask1 = new CountDownLatch(1);
                final CountDownLatch endTask1 = new CountDownLatch (1);
                final CountDownLatch endTask3 = new CountDownLatch (1);
                final AtomicReference<Set<String>> resultTask1Before = new AtomicReference<Set<String>>(Collections.<String>emptySet());
                final AtomicReference<Set<String>> resultTask1Nested = new AtomicReference<Set<String>>(Collections.<String>emptySet());
                final AtomicReference<Set<String>> resultTask1After = new AtomicReference<Set<String>>(Collections.<String>emptySet());
                final AtomicReference<Set<String>> resultTask3 = new AtomicReference<Set<String>>();
                

                CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo>() {

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void run(CompilationInfo p) throws Exception {
                        readyTask1.countDown();
                        changeTask1.await();
                        ClassIndex index = p.getClasspathInfo().getClassIndex();
                        resultTask1Before.set(index.getPackageNames("javax", true, EnumSet.allOf(ClassIndex.SearchScope.class)));
                        js.runUserActionTask(new Task<CompilationController>() {
                            @Override
                            public void run(CompilationController cc) throws Exception {
                                ClassIndex index = cc.getClasspathInfo().getClassIndex();
                                resultTask1Nested.set(index.getPackageNames("javax", true, EnumSet.allOf(ClassIndex.SearchScope.class)));
                            }
                        }, true);
                        index = p.getClasspathInfo().getClassIndex();
                        resultTask1After.set(index.getPackageNames("javax", true, EnumSet.allOf(ClassIndex.SearchScope.class)));
                        endTask1.countDown();
                    }

                };
                
                CancellableTask<CompilationInfo> task2 = new CancellableTask<CompilationInfo>() {
                    @Override
                    public void cancel() {                        
                    }

                    @Override
                    public void run(CompilationInfo parameter) throws Exception {
                    }                    
                };
                
                CancellableTask<CompilationInfo> task3 = new CancellableTask<CompilationInfo>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void run(CompilationInfo p) throws Exception {
                        ClassIndex index = p.getClasspathInfo().getClassIndex();
                        resultTask3.set(index.getPackageNames("javax", true, EnumSet.allOf(ClassIndex.SearchScope.class)));
                        endTask3.countDown();
                    }
                };
                
                factory.instance.active=true;
                JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask (js,task,Phase.PARSED, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
                assertTrue(readyTask1.await(5, TimeUnit.SECONDS));                
                JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js, task2, Phase.PARSED, Priority.HIGH, TaskIndexingMode.ALLOWED_DURING_SCAN);
                changeTask1.countDown();                
                assertTrue(endTask1.await(5, TimeUnit.SECONDS));
                assertNull(resultTask1Before.get());
                assertNull(resultTask1Nested.get());
                assertNull(resultTask1After.get());
                JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js, task3, Phase.PARSED, Priority.LOW, TaskIndexingMode.ALLOWED_DURING_SCAN);
                assertTrue(endTask3.await(5, TimeUnit.SECONDS));
                assertNotNull(resultTask3.get());  //Should not be null!!!
                
                
                JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask (js,task);
                JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js, task2);
                JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js, task3);
            } finally {
                regs.unregister(ClassPath.SOURCE, new ClassPath[]{sourcePath});
            }
        } finally {
            IndexManagerTestUtilities.setIndexFactory(oldFactory);
        }
    }

    public void testRegisterSameTask() throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
              JavaSource js = JavaSource.create(cpInfo, testFile1);
        final CountDownLatch latch1 = new CountDownLatch (1);
        final CountDownLatch latch2 = new CountDownLatch (1);
        CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo>() {
            public void cancel() {}
            public void run(CompilationInfo parameter) throws Exception {
                if (latch1.getCount() > 0) {
                    latch1.countDown();
                    return ;
                }

                latch2.countDown();
            }
        };
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,task, Phase.PARSED, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
        assertTrue(latch1.await(10, TimeUnit.SECONDS));
        JavaSourceAccessor.getINSTANCE().removePhaseCompletionTask(js,task);
        //When the last element is removed from j.u.c.PriorityBlockingQueue
        //the queue still holds a reference to it and assertGC does not work
        //see PBQ.siftDownComparator
        Field f = TaskProcessor.class.getDeclaredField("requests");   //NOI18N
        f.setAccessible(true);
        final Collection<?> requests = (Collection<?>) f.get(null);
        assertTrue(requests.isEmpty());
        f = PriorityBlockingQueue.class.getDeclaredField("queue");  //NOI18N
        f.setAccessible(true);
        final Object[] queue = (Object[]) f.get(requests);
        queue[0] = null;
        //WB
        assertTrue(requests.isEmpty());
        Reference<JavaSource> r = new WeakReference<JavaSource>(js);
        js = null;

        assertGC("", r);

        js = JavaSource.create(cpInfo, testFile1);
        JavaSourceAccessor.getINSTANCE().addPhaseCompletionTask(js,task, Phase.PARSED, Priority.NORMAL, TaskIndexingMode.ALLOWED_DURING_SCAN);
        assertTrue(latch2.await(10, TimeUnit.SECONDS));
    }
    
    public void testIncrementalReparse () throws Exception {
        final FileObject testFile = createTestFile ("Test");
        final ClassPath bootPath = createBootPath ();
        final ClassPath compilePath = createCompilePath ();
        final JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), testFile);
        final DataObject dobj = DataObject.find(testFile);
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        final TokenHierarchy h = TokenHierarchy.get(doc);
        final TokenSequence ts = h.tokenSequence(JavaTokenId.language());
        //Run sync task
        final CompilationInfoImpl[] impls = new CompilationInfoImpl[1];
        final Pair[] res = new Pair[1];
        js.runUserActionTask(new Task<CompilationController> () {
            public void run (final CompilationController c) throws IOException, BadLocationException {
                c.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cu = c.getCompilationUnit();
                FindMethodRegionsVisitor v = new FindMethodRegionsVisitor(doc, c.getTrees().getSourcePositions(), "main");
                v.visit(cu, null);
                impls[0] = c.impl;
                res[0] = v.result;
                assertEquals("}//end", doc.getText((int) cu.getLineMap().getPosition(7, 0), 6));
            }
        }, true);
        final boolean[] loggerResult = new boolean[1];
        final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                String msg = record.getMessage();
                if (msg.startsWith("Reflowed method in: ")) {
                    loggerResult[0] = true;
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        };
        final Logger logger = Logger.getLogger(JavacParser.class.getName());
        logger.setLevel (Level.FINEST);
        logger.addHandler(handler);
        try {
            assertNotNull(impls[0]);
            assertNotNull(res[0]);
            //Do modification
            NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {
                    try {
                        String text = doc.getText(0,doc.getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc.remove(index,REPLACE_PATTERN.length());
                        doc.insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }
                }
            });
            //Workaround, in test lexer events return wrong affected range
            impls[0].getParser().setChangedMethod(res[0]);
            //Run sync task
            js.runUserActionTask(new Task<CompilationController> () {
                public void run (final CompilationController c) throws IOException, BadLocationException {
                    c.toPhase(JavaSource.Phase.PARSED);
                    assertEquals("}//end", doc.getText((int) c.getCompilationUnit().getLineMap().getPosition(7, 0), 6));
                }
            }, true);
            //Check that there was an incremental reparse
            assertTrue(loggerResult[0]);
            loggerResult[0] = false;
            //Do modification
            NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {
                    try {
                        doc.insertString(0,"/** Hello **/",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }
                }
            });
            //Run sync task
            js.runUserActionTask(new Task<CompilationController> () {
                public void run (final CompilationController c) throws IOException {
                    c.toPhase(JavaSource.Phase.PARSED);
                }
            }, true);
            //Check that there was not an incremental reparse
            assertFalse(loggerResult[0]);
        } finally {
            logger.removeHandler(handler);
        }
    }
    
    public void testIncrementalReparseErrors() throws Exception {
        final FileObject testFile = createTestFile ("Test");
        final JavaSource js = JavaSource.forFileObject(testFile);
        assertNotNull(js);
        final DataObject dobj = DataObject.find(testFile);
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        final TokenHierarchy h = TokenHierarchy.get(doc);
        doc.remove(0, doc.getLength());
        final String code = "package test;\npublic class Test {\n private void test() {\n  String str = \"\\uaa\";\n }\n } ";
        doc.insertString(0, code, null);
        final TokenSequence ts = h.tokenSequence(JavaTokenId.language());
        ts.moveStart();
        while (ts.moveNext());
        final CompilationUnitTree[] cut = new CompilationUnitTree[1];
        js.runUserActionTask(new Task<CompilationController> () {
            public void run (final CompilationController c) throws IOException, BadLocationException {
                c.toPhase(JavaSource.Phase.RESOLVED);
                cut[0] = c.getCompilationUnit();
            }
        }, true);
        doc.insertString(code.indexOf("aa"), "a", null);
        js.runUserActionTask(new Task<CompilationController> () {
            public void run (final CompilationController c) throws IOException {
                c.toPhase(JavaSource.Phase.RESOLVED);
                assertSame(cut[0], c.getCompilationUnit());
                assertEquals(1, c.getDiagnostics().size());
                Diagnostic d = c.getDiagnostics().get(0);
                assertEquals(code.indexOf("\\uaa"), d.getStartPosition());
                assertEquals(code.indexOf("\\uaa") + 5, d.getEndPosition());
            }
        }, true);
    }
    
    public void testIncrementalReparseErrors231280() throws Exception {
        SourceLevelQueryImpl.sourceLevel = "1.7";
        try {
            final FileObject testFile = createTestFile ("Test");
            final JavaSource js = JavaSource.forFileObject(testFile);
            assertNotNull(js);
            final DataObject dobj = DataObject.find(testFile);
            final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
            final StyledDocument doc = ec.openDocument();
            doc.putProperty(Language.class, JavaTokenId.language());
            final TokenHierarchy h = TokenHierarchy.get(doc);
            doc.remove(0, doc.getLength());
            final String code = "package test;\n" +
                                "import java.util.LinkedHashSet;\n" +
                                "public class Test {\n" +
                                "    public void test() {\n" +
                                "        System.err.println(\"augment\");\n" +
                                "    }\n" +
                                "    static void createLRUSet(final int maxEntries) {\n" +
                                "        new LinkedHashSet<String>(maxEntries) {\n" +
                                "            protected void t() {\n" +
                                "                size();\n" +
                                "            }\n" +
                                "        };\n" +
                                "    }\n" +
                                "}";
            doc.insertString(0, code, null);
            final TokenSequence ts = h.tokenSequence(JavaTokenId.language());
            ts.moveStart();
            while (ts.moveNext());
            final CompilationUnitTree[] cut = new CompilationUnitTree[1];
            js.runUserActionTask(new Task<CompilationController> () {
                public void run (final CompilationController c) throws IOException, BadLocationException {
                    c.toPhase(JavaSource.Phase.RESOLVED);
                    assertEquals(c.getDiagnostics().toString(), 0, c.getDiagnostics().size());
                    cut[0] = c.getCompilationUnit();
                }
            }, true);
            doc.insertString(code.indexOf("augment") + 1, "a", null);
            js.runUserActionTask(new Task<CompilationController> () {
                public void run (final CompilationController c) throws IOException {
                    c.toPhase(JavaSource.Phase.RESOLVED);
                    assertSame(cut[0], c.getCompilationUnit());
                    assertEquals(c.getDiagnostics().toString(), 0, c.getDiagnostics().size());
                }
            }, true);
        } finally {
            SourceLevelQueryImpl.sourceLevel = null;
        }
    }
    
    public void testCreateTaggedController () throws Exception {
        FileObject testFile1 = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        JavaSource js1 = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), testFile1);

        final Object[] result = new Object[1];
        final long res1 = js1.createTaggedController(-1, result);
        assertTrue (result[0] instanceof CompilationController);
        ((CompilationController)result[0]).getTrees();        
        Thread.sleep(500);
        
        final long res2 = js1.createTaggedController(-1, result);
        assertTrue (result[0] instanceof CompilationController);
        assertEquals(res1, res2);
        
        
        final DataObject dobj = DataObject.find(testFile1);        
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);                        
        final StyledDocument doc = ec.openDocument();                
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy h = TokenHierarchy.get(doc);
        TokenSequence ts = h.tokenSequence(JavaTokenId.language());        
        NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {                        
                    try {                                                
                        doc.insertString(0," ",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }                 
                }
        });
        
        final long res3 = js1.createTaggedController(-1, result);
        assertTrue (result[0] instanceof CompilationController);
        assertFalse(res2 == res3);
        Thread.sleep(500);
        
        final long res4 = js1.createTaggedController(-1, result);
        assertTrue (result[0] instanceof CompilationController);
        assertEquals(res3, res4);
        
        NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {                        
                    try {                                                
                        doc.insertString(0," ",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }                 
                }
        });
        final long res5 = js1.createTaggedController(-1, result);
        assertTrue (result[0] instanceof CompilationController);
        assertFalse(res4 == res5);
        Thread.sleep(500);
        
        final long res6 = js1.createTaggedController(-1, result);
        assertTrue (result[0] instanceof CompilationController);
        assertEquals(res5, res6);
    }

    public void testInvalidate() throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final Elements[] elements = new Elements[1];
        ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
            public void run (WorkingCopy copy) throws IOException {
                copy.toPhase(Phase.PARSED);
                elements[0] = copy.getElements();
                TreeMaker make = copy.getTreeMaker();
                copy.rewrite(copy.getCompilationUnit(), make.addCompUnitImport(copy.getCompilationUnit(), make.Import(make.Identifier("foo.bar"), false)));
            }
        });

        js.runUserActionTask(new Task<CompilationController>() {
            public void run (CompilationController control) throws IOException {
                control.toPhase(Phase.PARSED);
                assertTrue(elements[0] == control.getElements());
            }
        }, true);

        mr.commit();

        js.runUserActionTask(new Task<CompilationController>() {
            public void run (CompilationController control) throws IOException {
                control.toPhase(Phase.PARSED);
                assertTrue(elements[0] != control.getElements());
            }
        }, true);
    }

    public void testWrongClassPathWhileParsingClassFile() throws Exception {
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final ClassPath boot = createBootPath();
        final ClassPath compile = ClassPathSupport.createClassPath(FileUtil.createFolder(wd, "libka"));
        final ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, null);
        final FileObject jlObject = boot.findResource("java/lang/Object.class");
        assertNotNull(jlObject);
        final JavaSource js = JavaSource.create(cpInfo, jlObject);
        assertNotNull(js);
        js.runUserActionTask(
            new Task<CompilationController>(){
                @Override
                public void run(CompilationController control) throws Exception {
                    final ClasspathInfo cpInfoInTask = control.getClasspathInfo();
                    assertNotNull(cpInfoInTask);
                    assertEquals(compile.entries(), cpInfoInTask.getClassPath(ClasspathInfo.PathKind.COMPILE).entries());
                }
            },true);
    }

    public void testMultipleFilesWithErrors() throws Exception {
        final FileObject testFile1 = createTestFile("Test1",
                                                    "public class Test1 extends Test2 {\n" +
                                                    "     public int inv(Unknown u) {\n" +
                                                    "         return this.doesNotExist(u);\n" +
                                                    "     }\n" +
                                                    "}\n");
        final FileObject testFile2 = createTestFile("Test2",
                                                    "public class Test2 {\n" +
                                                    "     public int inv(Unknown u) {\n" +
                                                    "         return this.doesNotExist(u);\n" +
                                                    "     }\n" +
                                                    "}\n");
        final JavaSource js = JavaSource.create(ClasspathInfo.create(testFile1), testFile1, testFile2);
        assertNotNull(js);
        js.runUserActionTask(new Task<CompilationController>() {
            public void run (final CompilationController c) throws IOException, BadLocationException {
                c.toPhase(JavaSource.Phase.RESOLVED);
            }
        }, true);
        js.runModificationTask(new Task<WorkingCopy>() {
            public void run (final WorkingCopy c) throws IOException, BadLocationException {
                c.toPhase(JavaSource.Phase.RESOLVED);
            }
        });
    }

    private static class FindMethodRegionsVisitor extends SimpleTreeVisitor<Void,Void> {

        final Document doc;
        final SourcePositions pos;
        final String methodName;
        CompilationUnitTree cu;

        Pair<DocPositionRegion,MethodTree> result;

        public FindMethodRegionsVisitor (final Document doc, final SourcePositions pos, String methodName) {
            assert doc != null;
            assert pos != null;
            assert methodName != null;
            this.doc = doc;
            this.pos = pos;
            this.methodName = methodName;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
            cu = node;
            for (Tree t : node.getTypeDecls()) {
                visit (t,p);
            }
            return null;
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            for (Tree t : node.getMembers()) {
                visit(t, p);
            }
            return null;
        }

        @Override
        public Void visitMethod(MethodTree node, Void p) {
            assert cu != null;
            int startPos = (int) pos.getStartPosition(cu, node.getBody());
            int endPos = (int) pos.getEndPosition(cu, node.getBody());
            if (methodName.equals(node.getName().toString()) && startPos >=0) {
                try {
                    result = Pair.<DocPositionRegion,MethodTree>of(new DocPositionRegion(doc,startPos,endPos),node);
                } catch (BadLocationException e) {
                    //todo: reocvery
                    e.printStackTrace();
                }
            }
            return null;
        }

    }

    private static class CompileControlJob implements Task<CompilationController> {

        private final CountDownLatch latch;
        boolean multiSource;

        public CompileControlJob (CountDownLatch latch) {
            this.latch = latch;
        }

        public void run (CompilationController controler) {
            try {
                controler.toPhase(Phase.PARSED);
//todo: Multisource                if (!controler.impl.needsRestart) {
                    assertTrue (Phase.PARSED.compareTo(controler.getPhase())<=0);
                    assertNotNull("No ComplationUnitTrees after parse",controler.getCompilationUnit());
                    controler.toPhase(Phase.RESOLVED);
                    if (multiSource) {
//todo: Multisource                        if (controler.impl.needsRestart) {
//todo: Multisource                            return;
//todo: Multisource                        }
                    }
                    assertTrue (Phase.RESOLVED.compareTo(controler.getPhase())<=0);

                    //all elements should be resolved now:
                    new ScannerImpl(controler).scan(controler.getCompilationUnit(), null);

                    controler.toPhase(Phase.PARSED);
                    //Was not modified should stay in {@link Phase#RESOLVED}
                    assertTrue (Phase.RESOLVED.compareTo(controler.getPhase())<=0);
                    this.latch.countDown();
//todo: Multisource                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static class ScannerImpl extends ErrorAwareTreePathScanner<Void, Void> {

        private CompilationInfo info;

        public ScannerImpl(CompilationInfo info) {
            this.info = info;
        }

        public Void visitIdentifier(IdentifierTree node, Void p) {
            assertNotNull(info.getTrees().getElement(getCurrentPath()));
            return super.visitIdentifier(node, p);
        }

    }

    private static class WorkingCopyJob implements Task<WorkingCopy> {

        private final CountDownLatch latch;

        public WorkingCopyJob (CountDownLatch latch) {
            this.latch = latch;
        }

        public void run (WorkingCopy copy) throws IOException {
            copy.toPhase(Phase.RESOLVED);
            assertTrue (Phase.RESOLVED.compareTo(copy.getPhase())<=0);
            assertNotNull("No ComplationUnitTrees after parse",copy.getCompilationUnit());

            new TransformImpl(copy).scan(copy.getCompilationUnit(), null);

            this.latch.countDown();
        }
    }

    private static class TransformImpl extends ErrorAwareTreeScanner<Void, Object> {

        private WorkingCopy copy;

        public TransformImpl(WorkingCopy copy) {
            this.copy = copy;
        }

        public Void visitClass(ClassTree node, Object p) {
            TreeMaker make = copy.getTreeMaker();
            ClassTree newNode = make.addClassMember(node, make.Variable(make.Modifiers(Collections.singleton(Modifier.PUBLIC)), "field", make.Identifier("int"), null));

            copy.rewrite(node, newNode);
            return null;
        }

    }

//todo: Run multi files not yet implemented in parsing API
//    private static class CompileControlJobWithOOM implements Task<CompilationController> {
//
//        private final CountDownLatch latch;
//        private final int oomFor;
//        private int currentIndex;
//        private URI uri;
//
//        public CompileControlJobWithOOM (CountDownLatch latch, int oomFor) {
//            this.latch = latch;
//            this.oomFor = oomFor;
//        }
//
//        public void run (CompilationController controler) {
//            try {
//                controler.toPhase(Phase.PARSED);
//                assertTrue (Phase.PARSED.compareTo(controler.getPhase())<=0);
//                CompilationUnitTree cut = controler.getCompilationUnit();
//                assertNotNull("No ComplationUnitTree after parse",cut);
//                controler.toPhase(Phase.RESOLVED);
//                assertTrue (Phase.RESOLVED.compareTo(controler.getPhase())<=0);
//                controler.toPhase(Phase.PARSED);
//                //Was not modified should stay in {@link Phase#RESOLVED}
//                assertTrue (Phase.RESOLVED.compareTo(controler.getPhase())<=0);
//                if (currentIndex == oomFor) {
//                    controler.impl.needsRestart = true;
//                    uri = cut.getSourceFile().toUri();
//                }
//                if (currentIndex == oomFor+1) {
//                    assertNotNull (uri);
//                    assertEquals(uri,cut.getSourceFile().toUri());
//                    uri = null;
//                }
//                this.latch.countDown();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            finally {
//                this.currentIndex++;
//            }
//        }
//    }


    private static class EmptyCompileControlJob implements Task<CompilationController> {

        private final CountDownLatch latch;

        public EmptyCompileControlJob (CountDownLatch latch) {
            this.latch = latch;
        }

        public void run (CompilationController controler) {
            try {
                try {
                    //Should throw exception
                    assertEquals(Phase.PARSED, controler.toPhase(Phase.PARSED));
                } catch (IllegalStateException e) {
                }
                try {
                    //Should throw exception
                    controler.getCompilationUnit();
                    throw new AssertionError ();
                } catch (IllegalStateException e) {
                }
                controler.getPhase();
                controler.getTypes();
                controler.getTrees();
                controler.getElements();
                controler.getClasspathInfo();
                controler.getJavaSource();
                this.latch.countDown();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }



    private static boolean waitForMultipleObjects (CountDownLatch[] objects, int timeOut) throws InterruptedException {
        for (CountDownLatch latch : objects) {
            long ctms = System.currentTimeMillis();
            if (!latch.await(timeOut, TimeUnit.MILLISECONDS)) {
                return false;
            }
            long ctme = System.currentTimeMillis ();
            timeOut -= (ctme - ctms);
        }
        return true;
    }


    private static class DiagnosticTask implements CancellableTask<CompilationInfo> {

        private final Phase expectedPhase;
        private final CountDownLatch[] latches;
        private final long[] times;
        private final AtomicInteger counter;
        private int currentLatch;
        private int cancelCount;
        boolean verbose;

        public DiagnosticTask (final CountDownLatch[] latches, final AtomicInteger counter, final Phase expectedPhase) {
            this(latches, new long[latches.length], counter, expectedPhase);
        }

        public DiagnosticTask (final CountDownLatch[] latches, final long[] times, final AtomicInteger counter, final Phase expectedPhase) {
            assertEquals(times.length, latches.length);
            this.latches = latches;
            this.times = times;
            this.counter = counter;
            this.expectedPhase = expectedPhase;
        }

        public synchronized void run(CompilationInfo parameter) {
            if (verbose) {
                System.out.println("run called");
            }
            if (this.cancelCount>0) {
                this.cancelCount--;
                if (verbose) {
                    System.out.println("Cancel count: " + cancelCount);
                }
                return;
            }
            if (this.counter != null) {
                int current = this.counter.incrementAndGet();
                if (verbose) {
                    System.out.println("counter="+current);
                }
            }
            if (this.currentLatch < this.times.length) {
                if (verbose) {
                    System.out.println("Firing current latch: " + this.currentLatch);
                }
                this.times[this.currentLatch] = System.currentTimeMillis();
                this.latches[this.currentLatch++].countDown();
            }
            assertNotNull (parameter);
            assertTrue (String.format("Got wrong state, expected: %s got: %s", expectedPhase.name(), parameter.getPhase().name()), this.expectedPhase.compareTo(parameter.getPhase())<=0);
        }

        public synchronized void cancel() {
            this.cancelCount++;
            if (verbose) {
                System.out.println("cancel called: " + cancelCount);
            }
        }

    }


    private static class WaitTask implements CancellableTask<CompilationInfo> {

        private long milisToWait;
        private int cancelCount;
        private int runCount;

        public WaitTask (long milisToWait) {
            this.milisToWait = milisToWait;
            this.cancelCount = 0;
            this.runCount = 0;
        }

        public void run (CompilationInfo info) {
            this.runCount++;
            if (this.milisToWait>0) {
                try {
                    Thread.sleep(this.milisToWait);
                } catch (InterruptedException ie) {}
            }
        }

        public void cancel () {
            this.cancelCount++;
        }

        public int getCancelCount () {
            return this.cancelCount;
        }

        public int getRunCount () {
            return this.runCount;
        }

    }

    private static class TestIndexFactory implements IndexFactory {
        
        final TestIndex instance = new TestIndex();

        @Override
        public Index.Transactional createIndex(File cacheFolder, Analyzer analyzer) {
            return instance;
        }

        @Override
        public Index createMemoryIndex(Analyzer analyzer) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static class TestIndex implements Index.Transactional {
        //Activate the TestIndex.await after scan is done
        //during the scan the prebuildArgs may call the index
        //and cause deadlock
        volatile boolean active;


        public TestIndex () {
        }

        @Override
        public Status getStatus(boolean tryOpen) throws IOException {
            return Status.VALID;
        }
        
        @Override
        public <T> void query(
                Collection<? super T> result,
                Convertor<? super org.apache.lucene.document.Document, T> convertor,
                Set<String> selector,
                AtomicBoolean cancel,
                Query... queries) throws IOException, InterruptedException {
            await(cancel);
        }

        @Override
        public <T> void queryTerms(
                Collection<? super T> result,
                String field, String start,
                StoppableConvertor<BytesRef, T> filter,
                AtomicBoolean cancel) throws IOException, InterruptedException {
            await (cancel);
        }

        @Override
        public <S, T> void queryDocTerms(
                Map<? super T, Set<S>> result,
                Convertor<? super org.apache.lucene.document.Document, T> convertor,
                Convertor<? super BytesRef, S> termConvertor, Set<String> selector,
                AtomicBoolean cancel,
                Query... queries) throws IOException, InterruptedException {
            await(cancel);
        }
        
        @Override
        public <S, T> void store(Collection<T> toAdd, Collection<S> toDelete, Convertor<? super T, ? extends org.apache.lucene.document.Document> docConvertor, Convertor<? super S, ? extends Query> queryConvertor, boolean optimize) throws IOException {
        }

        @Override
        public void commit() throws IOException {
        }

        @Override
        public void rollback() throws IOException {
        }

        @Override
        public <S, T> void txStore(Collection<T> toAdd, Collection<S> toDelete, Convertor<? super T, ? extends org.apache.lucene.document.Document> docConvertor, Convertor<? super S, ? extends Query> queryConvertor) throws IOException {
        }
        
        public boolean isUpToDate(String resourceName, long timeStamp) throws IOException {
            return true;
        }

        @Override
        public void clear() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        private void await (final AtomicBoolean cancel) throws InterruptedException {
            if (!active) {
                return;
            }
            if (cancel != null && cancel.get()) {
                throw new InterruptedException ();
            }            
        }
    }

    private FileObject createTestFile (String className) {
        try {
            return createTestFile(className,
                                  MessageFormat.format(TEST_FILE_CONTENT, new Object[] {className}) + System.getProperty("line.separator"));
        } catch (IOException ioe) {
            return null;
        }
    }

    private FileObject createTestFile (String className, String content) throws IOException {
        File workdir = this.getWorkDir();
        File root = new File (workdir, "src");
        root.mkdir();
        File data = new File (root, className+".java");

        try (Writer w = new FileWriter (data)) {
            w.write(content);
        }
        return FileUtil.toFileObject(data);
    }

    private ClassPath createBootPath () throws MalformedURLException {
        return BootClassPathUtil.getBootClassPath();
    }

    private ClassPath createCompilePath () {
        return ClassPathSupport.createClassPath(Collections.EMPTY_LIST);
    }

    private ClassPath createSourcePath () throws IOException {
        File workdir = this.getWorkDir();
        File root = new File (workdir, "src");
        if (!root.exists()) {
            root.mkdirs();
        }
        return ClassPathSupport.createClassPath(new URL[] {org.openide.util.Utilities.toURI(root).toURL()});
    }

    private FileObject createTestFile (FileObject srcRoot, String relativeName, String content) throws IOException {
        FileObject f = FileUtil.createData(srcRoot, relativeName);
        Writer out = new OutputStreamWriter(f.getOutputStream());

        try {
            out.write(content);
        } finally {
            out.close();
        }

        return f;
    }
    
    @ServiceProvider(service=SourceLevelQueryImplementation.class, position=100)
    public static final class SourceLevelQueryImpl implements SourceLevelQueryImplementation {
        public static String sourceLevel = null;
        @Override public String getSourceLevel(FileObject javaFile) {
            return sourceLevel;
        }
    }
}
