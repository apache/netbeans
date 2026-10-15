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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.file.launcher.queries.MultiSourceRootProvider;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.parsing.JavacParserResult;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.IndexingUtils;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
public class ScanUtilsTest extends NbTestCase {
    
    public ScanUtilsTest(String s) {
        super(s);
    }
    
    private static final String REPLACE_PATTERN = "/*TODO:Changed-by-test*/";
    private static final String TEST_FILE_CONTENT=
                "public class {0} '{\n"+
                "   public static void main (String[] args) {\n"+
                "       javax.swing.JTable table = new javax.swing.JTable ();\n"+
                "       Class c = table.getModel().getClass();\n"+
                "       "+REPLACE_PATTERN+"\n"+
                "   }'\n"+
                "   public {1} getOtherClass() '{ return null; }'\n" +
                "}//end'\n";


    private FileObject createTestFile (String className, String otherClassName) {
        try {
            File workdir = this.getWorkDir();
            File root = new File (workdir, "src");
            root.mkdir();
            File data = new File (root, className+".java");

            PrintWriter out = new PrintWriter (new FileWriter (data));
            try {
                out.println(MessageFormat.format(TEST_FILE_CONTENT, new Object[] {className, otherClassName}));
            } finally {
                out.close ();
            }
            return FileUtil.toFileObject(data);
        } catch (IOException ioe) {
            return null;
        }
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
    
    static class ScannerBlock implements Runnable {
            private final CountDownLatch start;
            private final CountDownLatch latch;

            public ScannerBlock(final CountDownLatch start, final CountDownLatch latch) {
                assert start != null;
                assert latch != null;
                this.start = start;
                this.latch = latch;
            }


            public void run() {
                try {
                    this.start.countDown();
                    this.latch.await();
                    doInScanning();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            public void doInScanning() {}
    }
    
    private JavaSource testSource;
    private FileObject  testFile1;
    private FileObject  testFile2;
    
    private void setupTestFile(boolean wait) throws Exception {
        setupTestFile("Test1", wait);
    }
    
    private void setupTestFile(String filename, boolean wait) throws Exception {
        String fn2 = filename + "Other";
        testFile1 = createTestFile(filename, fn2);
        testFile2 = createTestFile(fn2, filename);
        if (wait) {
            IndexingManager.getDefault().refreshIndexAndWait(testFile1.getParent().getURL(), null);
        }

        Thread.sleep (1000); //Indexing task already finished, but we want to wait until JS working thread is waiting on task to dispatch
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClassPath srcPath = createSourcePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,srcPath);
        testSource = JavaSource.create(cpInfo,testFile1);
    }
    
    public void testRunWhenScanFinished () throws Exception {
        setupTestFile(true);
        
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

        CountDownLatch latch = new CountDownLatch (1);
        
        Future res = ScanUtils.postUserActionTask(testSource, new T(latch));
        assertEquals(0,latch.getCount());
        res.get(1,TimeUnit.SECONDS);
        assertTrue(res.isDone());
        assertFalse (res.isCancelled());
    }

    /**
     * Checks that errors raised in a task immediately run by post* method are catched
     * and not propagated to the caller.
     * 
     * @throws Exception 
     */
    public void testIgnoreErrorsInstantPostJava() throws Exception {
        setupTestFile(true);

        Future res = ScanUtils.postUserActionTask(testSource, new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                throw new RuntimeException("Must be catched and logged");
            }
        });
        // the action should be finished, the error logged, but not thrown
        assertTrue(res.isDone());
    }

    /**
     * Checks that task, which signals incomplete data is deferred and is retried again
     * 
     * @throws Exception 
     */
    public void testSignalPostJava() throws Exception {
        setupTestFile(false);
        
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean passed = new AtomicBoolean(false);
        
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(1);
        
        final ScannerBlock block = new ScannerBlock(start, latch);
        
        IndexingUtils.runAsScanWork(block);
        IndexingManager.getDefault().refreshIndex(testFile1.getParent().getURL(), null);
        
        // wait for the parsing to begin, stop it
        block.start.await();

        Future res = ScanUtils.postUserActionTask(testSource, new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                count.incrementAndGet();
                ScanUtils.signalIncompleteData(parameter, null);
                passed.set(true);
            }
        });
        // the action should be finished, the error logged, but not thrown
        assertFalse(res.isDone());
        // the task was invoked ONCE
        assertEquals(1, count.intValue());
        
        // free the parser
        block.latch.countDown();
        
        // should not fail !
        res.get(100, TimeUnit.SECONDS);
        
        assertEquals(2, count.intValue());
        assertTrue(passed.get());
    }
    
    /**
     * Checks that wait* method completes immediately if no signal is raised.
     * 
     * @throws Exception 
     */
    public void testInstantWaitJava() throws Exception {
    }
    
    /**
     * Checks that wait() method will wait until scanning finishes on signal raise
     * @throws Exception 
     */
    public void testDeferredWaitJava() throws Exception {
        setupTestFile(false);
        
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean passed = new AtomicBoolean(false);
        final AtomicReference<String> error = new AtomicReference<String>();
        
        CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        
        final ScannerBlock block = new ScannerBlock(start, latch) {

            @Override
            public void doInScanning() {
                // check that the counter is still 1:
                if (count.get() != 1) {
                    error.set("Task was re-run before scan finish");
                }
                try {
                    // block enough for the user task to be rescheduled
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        };
        
        IndexingUtils.runAsScanWork(block);
        IndexingManager.getDefault().refreshIndex(testFile1.getParent().getURL(), null);
        
        // wait for the parsing to begin, stop it
        block.start.await();

        ScanUtils.waitUserActionTask(testSource, new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                count.incrementAndGet();
                // wake up scanning work
                latch.countDown();
                ScanUtils.signalIncompleteData(parameter, null);
                passed.set(true);
            }
        });
        // the action should be finished, the error logged, but not thrown
        // the task was invoked ONCE
        assertEquals(2, count.intValue());
        assertTrue(passed.get());
        assertNull(error.get(), error.get());
    }
    
    /**
     * Checks that exceptions thrown by user task are propagated to the caller.
     * 
     * @throws Exception 
     */
    public void testDeferredWaitJavaError() throws Exception {
        setupTestFile(false);
        
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean passed = new AtomicBoolean(false);
        final AtomicReference<String> error = new AtomicReference<String>();
        
        CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        
        final ScannerBlock block = new ScannerBlock(start, latch) {

            @Override
            public void doInScanning() {
                // check that the counter is still 1:
                try {
                    // block enough for the user task to be rescheduled
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        };
        
        IndexingUtils.runAsScanWork(block);
        IndexingManager.getDefault().refreshIndex(testFile1.getParent().getURL(), null);
        
        // wait for the parsing to begin, stop it
        block.start.await();

        try {
            ScanUtils.waitUserActionTask(testSource, new Task<CompilationController>() {
                @Override
                public void run(CompilationController parameter) throws Exception {
                    count.incrementAndGet();
                    // wake up scanning work
                    latch.countDown();
                    ScanUtils.signalIncompleteData(parameter, null);
                    passed.set(true);
                    throw new UnsupportedOperationException("Caller should get this one");
                }
            });
            fail("Expected IOException from the failed user task");
        } catch (IOException ex) {
            assertEquals(2, count.intValue());
            assertTrue(passed.get());
            assertNull(error.get(), error.get());
        }
    }
    
    /**
     * Checks that errors are propagated from the restarted task
     * 
     * @throws Exception 
     */
    public void testJavaPostResolveError() throws Exception {
        setupTestFile("TestResolve", false);
        
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean passed = new AtomicBoolean(false);
        final AtomicReference<String> error = new AtomicReference<String>();
        
        CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        
        final ScannerBlock block = new ScannerBlock(start, latch) {

            @Override
            public void doInScanning() {
                // check that the counter is still 1:
                try {
                    // block enough for the user task to be rescheduled
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        };
        
        IndexingUtils.runAsScanWork(block);
        IndexingManager.getDefault().refreshIndex(testFile1.getParent().getURL(), null);
        
        // wait for the parsing to begin, stop it
        block.start.await();

        Future f = ScanUtils.postUserActionTask(testSource, new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                count.incrementAndGet();

                // FIXME - re-run on NPE ? Shouldn't !
                TypeElement el = parameter.getElements().getTypeElement("TestResolve");
                ScanUtils.checkElement(parameter, el);

                List<? extends Element> members = el.getEnclosedElements();
                Element e = null;
                for (Element m : members) {
                    if ("getOtherClass".equals(m.getSimpleName())) {
                        e = m;
                        break;
                    }
                }

                ScanUtils.checkElement(parameter, e);

                passed.set(true);
                throw new UnsupportedOperationException("Caller should get this one");
            }
        });
        
        assertEquals(1, count.get());
        assertFalse(f.isDone());
        
        // wake up scanning work
        latch.countDown();

        f.get(30, TimeUnit.SECONDS);
        
        assertEquals(2, count.get());
        assertTrue(passed.get());
    }
    
    /**
     * Checks that errors are propagated from the restarted task
     * 
     * @throws Exception 
     */
    public void testJavaRepeatResolveError() throws Exception {
        setupTestFile("TestResolve", false);
        
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean passed = new AtomicBoolean(false);
        final AtomicReference<String> error = new AtomicReference<String>();
        
        CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        
        final ScannerBlock block = new ScannerBlock(start, latch) {

            @Override
            public void doInScanning() {
                // check that the counter is still 1:
                try {
                    // block enough for the user task to be rescheduled
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        };
        
        IndexingUtils.runAsScanWork(block);
        IndexingManager.getDefault().refreshIndex(testFile1.getParent().getURL(), null);
        
        // wait for the parsing to begin, stop it
        block.start.await();

        try {
            ScanUtils.waitUserActionTask(testSource, new Task<CompilationController>() {
                @Override
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    count.incrementAndGet();
                    
                    // FIXME - re-run on NPE ? Shouldn't !
                    TypeElement el = parameter.getElements().getTypeElement("TestResolve");
                    ScanUtils.checkElement(parameter, el);
                    
                    // wake up scanning work
                    latch.countDown();

                    List<? extends Element> members = el.getEnclosedElements();
                    Element e = null;
                    for (Element m : members) {
                        if ("getOtherClass".equals(m.getSimpleName())) {
                            e = m;
                            break;
                        }
                    }
                    
                    ScanUtils.checkElement(parameter, e);
                    
                    passed.set(true);
                    throw new UnsupportedOperationException("Caller should get this one");
                }
            });
            fail("Expected IOException from the failed user task");
        } catch (IOException ex) {
            assertEquals(2, count.intValue());
            assertTrue(passed.get());
            assertNull(error.get(), error.get());
        }
    }
    
    /**
     * Checks that errors are propagated from the restarted task
     * 
     * @throws Exception 
     */
    public void testParsingRepeatResolveError() throws Exception {
        setupTestFile("TestResolve", false);
        
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean passed = new AtomicBoolean(false);
        final AtomicReference<String> error = new AtomicReference<String>();
        
        CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        
        final ScannerBlock block = new ScannerBlock(start, latch) {

            @Override
            public void doInScanning() {
                // check that the counter is still 1:
                try {
                    // block enough for the user task to be rescheduled
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        };
        
        IndexingUtils.runAsScanWork(block);
        IndexingManager.getDefault().refreshIndex(testFile1.getParent().getURL(), null);
        
        // wait for the parsing to begin, stop it
        block.start.await();
        
        Source src = Source.create(testFile1);

        try {
            ScanUtils.waitUserTask(src, new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {

                    count.incrementAndGet();
                    
                    JavacParserResult r = (JavacParserResult)resultIterator.getParserResult();
                    
                    CompilationController parameter = CompilationController.get(r);
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    
                    // wake up scanning work
                    latch.countDown();

                    // FIXME - re-run on NPE ? Shouldn't !
                    TypeElement el = parameter.getElements().getTypeElement("TestResolve");
                    
                    ScanUtils.checkElement(parameter, el);
                    
                    List<? extends Element> members = el.getEnclosedElements();
                    
                    Element e = null;
                    for (Element m : members) {
                        if ("getOtherClass".equals(m.getSimpleName())) {
                            e = m;
                            break;
                        }
                    }
                    
                    ScanUtils.checkElement(parameter, e);
                    
                    passed.set(true);
                    throw new UnsupportedOperationException("Caller should get this one");
                }
            });
            fail("Expected IOException from the failed user task");
        } catch (ParseException ex) {
            assertEquals(2, count.intValue());
            assertTrue(passed.get());
            assertNull(error.get(), error.get());
            assertTrue(ex.getCause() instanceof UnsupportedOperationException);
        }
    }
    
    /**
     * Checks that the 'retry context' is properly restored when ScanUtils is invoked
     * recursively.
     * 
     * @throws Exception 
     */
    public void testJavaNestedInvocation() throws Exception {
        setupTestFile("TestResolve", false);
        
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean passed = new AtomicBoolean(false);
        final AtomicReference<String> error = new AtomicReference<String>();
        
        CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        
        final ScannerBlock block = new ScannerBlock(start, latch) {

            @Override
            public void doInScanning() {
                // check that the counter is still 1:
                try {
                    // block enough for the user task to be rescheduled
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
        };
        
        IndexingUtils.runAsScanWork(block);
        IndexingManager.getDefault().refreshIndex(testFile1.getParent().getURL(), null);
        
        // wait for the parsing to begin, stop it
        block.start.await();

        try {
            ScanUtils.waitUserActionTask(testSource, new Task<CompilationController>() {
                @Override
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    count.incrementAndGet();
                    
                    ScanUtils.waitUserActionTask(testSource, new Task<CompilationController>() {
                        @Override
                        public void run(CompilationController parameter) throws Exception {
                            // not important
                        }
                    });
                    
                    // FIXME - re-run on NPE ? Shouldn't !
                    TypeElement el = parameter.getElements().getTypeElement("TestResolve");
                    
                    // this should not fail
                    ScanUtils.checkElement(parameter, el);
                    
                    // wake up scanning work
                    latch.countDown();

                    List<? extends Element> members = el.getEnclosedElements();
                    Element e = null;
                    for (Element m : members) {
                        if ("getOtherClass".equals(m.getSimpleName())) {
                            e = m;
                            break;
                        }
                    }
                    
                    ScanUtils.checkElement(parameter, e);
                    
                    passed.set(true);
                    throw new UnsupportedOperationException("Caller should get this one");
                }
            });
            fail("Expected IOException from the failed user task");
        } catch (IOException ex) {
            assertEquals(2, count.intValue());
            assertTrue(passed.get());
            assertNull(error.get(), error.get());
        }
    }

    static {
        MultiSourceRootProvider.DISABLE_MULTI_SOURCE_ROOT = true;
    }
}
