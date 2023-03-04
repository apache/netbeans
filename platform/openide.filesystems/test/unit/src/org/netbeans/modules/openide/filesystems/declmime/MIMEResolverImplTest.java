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

package org.netbeans.modules.openide.filesystems.declmime;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;

public class MIMEResolverImplTest extends NbTestCase {
    List<MIMEResolver> resolvers;
    FileObject root;
    FileObject resolversRoot;
    Logger LOG;
           
    public MIMEResolverImplTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LOG = Logger.getLogger("test.mime.resolver." + getName());
        
        URL u = this.getClass().getResource ("code-fs.xml");        
        FileSystem fs = new XMLFileSystem(u);
        
        resolversRoot = fs.getRoot().getFileObject("root");
        resolversRoot.refresh();
        
        FileObject fos[] = resolversRoot.getChildren();
        resolvers = new ArrayList<MIMEResolver>();
        for (int i = 0; i<fos.length; i++) {
            resolvers.add(createResolver(fos[i]));
        }
        
        u = this.getClass().getResource ("data-fs.xml");                
        fs = new XMLFileSystem(u);
        
        root = fs.getRoot().getFileObject("root");
        root.refresh();
    }
    
    protected MIMEResolver createResolver(FileObject fo) throws Exception {
        if (fo == null) throw new NullPointerException();
        return MIMEResolverImpl.forDescriptor(fo);
    }

    private String resolve(FileObject fo) {
        for (MIMEResolver r : resolvers) {
            String s = r.findMIMEType(fo);
            if (s != null) return s;
        }
        return null;
    }

    public void testMultithreading() throws Exception {
        
        Object tl1 = new Object();
        Object tl2 = new Object();
        
        TestThread t1 = new TestThread(tl1);
        TestThread t2 = new TestThread(tl2);

        Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                ((TestThread) t).fail = e.getMessage();
            }
        };

        t1.setUncaughtExceptionHandler(exceptionHandler);
        t2.setUncaughtExceptionHandler(exceptionHandler);

        // call resolver from two threads

        t1.start();
        t2.start();
        Thread.currentThread().join(100);
        synchronized (tl1) {tl1.notify();}
        synchronized (tl2) {tl2.notify();}
 
        t1.join(5000);
        t2.join(5000);

        if (t1.fail != null) fail(t1.fail);
        if (t2.fail != null) fail(t2.fail);
    }

    private class TestThread extends Thread {
        
        final Object lock;
        String fail;
        
        private TestThread(Object lock) {
            this.lock = lock;
        }
        
        @Override
        public void run() {
            String s;
            FileObject fo;

            fo = root.getFileObject("test","elf");
            s = resolve(fo);
            if ("magic-mask.xml".equals(s) == false) fail = "magic-mask rule failure: " + fo + " => " + s;
            
            fo = root.getFileObject("test","exe");
            s = resolve(fo);
            if ("magic.xml".equals(s) == false) fail = "magic rule failure: " + fo + " => " + s;

            fo = root.getFileObject("root","xml");
            s = resolve(fo);
            if ("root.xml".equals(s) == false) fail = "root rule failure" + fo + " => " + s;

            fo = root.getFileObject("ns","xml");
            s = resolve(fo);
            if ("ns.xml".equals(s) == false) fail = "ns rule failure"  + fo + " => " + s;

            try {
                synchronized (lock) {
                    lock.wait(5000);  // switch threads here
                }
            } catch (Exception ex) {
                //
            }
            
            fo = root.getFileObject("empty","dtd");
            s = resolve(fo);
            if (null != s) fail = "null rule failure"  + fo + " => " + s;

            fo = root.getFileObject("pid","xml");
            s = resolve(fo);
            if ("pid.xml".equals(s) == false) fail = "pid rule failure"  + fo + " => " + s;
                        
        }
    }
    
    /** See #15672.
     * @author Jesse Glick
     */
    public void testParseFailures() {
        assertEquals("build1.xml recognized as Ant script", "text/x-ant+xml", resolve(root.getFileObject("build1", "xml")));
        assertEquals("bogus.xml not recognized as anything", null, resolve(root.getFileObject("bogus", "xml")));
        assertEquals("build2.xml recognized as Ant script", "text/x-ant+xml", resolve(root.getFileObject("build2", "xml")));
        // see #126496
        assertEquals("NPE at XMLEntityScanner.skipChar not ignored.", null, resolve(root.getFileObject("126496-skipCharNPE", "xml")));
    }
    
    public void testIllegalXMLEncoding() {
        assertEquals("illegal-encoding.xml recognized as a XML file", "text/x-springconfig+xml", resolve(root.getFileObject("illegal-encoding", "xml")));
    }

    /** Test possible cascading of pattern elements. */
    private void doTestPatternElementValidity(String resolver, boolean fails) throws Exception {
        final AtomicBoolean failed = new AtomicBoolean(false);
        final StringBuffer sb = new StringBuffer();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                sb.append(record).append("\n");
                if (startsWith(record)) {
                    LOG.info("failed.set(true");
                    LOG.log(record);
                    failed.set(true);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }

            private boolean startsWith(LogRecord record) {
                if (record == null) {
                    return false;
                }
                if (record.getThrown() == null) {
                    return false;
                }
                if (record.getThrown().getMessage() == null) {
                    return false;
                }
                return record.getThrown().getMessage().startsWith("Second pattern element on the same level not allowed");
            }
        };
        Logger.getLogger(DefaultParser.class.getName()).setLevel(Level.ALL);
        Logger.getLogger(DefaultParser.class.getName()).addHandler(handler);
        MIMEResolver declarativeResolver = createResolver(resolversRoot.getFileObject(resolver));
        declarativeResolver.findMIMEType(root.getFileObject("empty.dtd"));
        if (fails) {
            assertTrue("Resolver " + resolver + " should fail\nMessages:\n" + sb, failed.get());
        } else {
            assertFalse("Resolver " + resolver + " should succeed\nMessages:\n" + sb, failed.get());
        }
    }

    public void testPatternElementValidityValid() throws Exception {
        doTestPatternElementValidity("pattern-resolver-valid.xml", false);
    }

    public void testPatternElementValidityInvalid1() throws Exception {
        doTestPatternElementValidity("pattern-resolver-invalid1.xml", true);
    }

    public void testPatternElementValidityInvalid2() throws Exception {
        doTestPatternElementValidity("pattern-resolver-invalid2.xml", true);
    }

    private void assertMimeType(MIMEResolver resolver, String expectedMimeType, String... filenames) {
        for (String filename : filenames) {
            String mimeType = resolver.findMIMEType(root.getFileObject(filename));
            assertEquals("File " + filename + " not properly resolved by " + resolver +".", expectedMimeType, mimeType);
        }
    }

    /** Test pattern element in declarative MIME resolver. */
    public void testPatternElement() throws Exception {
        MIMEResolver resolver = createResolver(resolversRoot.getFileObject("php-resolver1.xml"));
        assertMimeType(resolver, "text/x-php5", "php.txt");
        assertMimeType(resolver, null, "not-php.txt");
        assertMimeType(resolver, "text/x-php5", "html-php.txt");

        resolver = createResolver(resolversRoot.getFileObject("php-resolver2.xml"));
        assertMimeType(resolver, null, "php.txt");
        assertMimeType(resolver, "text/x-php5", "html-php.txt");

        resolver = createResolver(resolversRoot.getFileObject("php-resolver3.xml"));
        assertMimeType(resolver, null, "php.txt");
        assertMimeType(resolver, null, "html-php.txt");
    }

    /** Test name element in declarative MIME resolver. */
    public void testNameElement() throws Exception {
        MIMEResolver resolver = createResolver(resolversRoot.getFileObject("makefile-resolver.xml"));
        assertMimeType(resolver, "text/x-make", "makefile", "Makefile", "MaKeFiLe", "mymakefile", "gnumakefile", "makefile1", "makefileRakefile", "makefile.Rakefile");

        assertMimeType(resolver, null, "empty.dtd", "rakefile", "Rakefile");

        resolver = createResolver(resolversRoot.getFileObject("rakefile-resolver.xml"));
        assertMimeType(resolver, "text/x-ruby", "rakefile", "Rakefile");
        assertMimeType(resolver, null, "empty.dtd", "makefile", "makefileRakefile", "makefile.Rakefile");
    }

    /** Test ruby declarative MIME resolver. */
    public void testRubyResolver() throws Exception {
        MIMEResolver resolver = createResolver(resolversRoot.getFileObject("ruby-resolver.xml"));
        assertMimeType(resolver, "text/x-ruby", "ruby.cgi");
    }

    /** Test empty extension MIME resolver. */
    public void testEmptyExtensionResolver() throws Exception {
        MIMEResolver resolver = createResolver(resolversRoot.getFileObject("empty-extension-resolver.xml"));
        assertMimeType(resolver, "empty/extension", "empty-extension");
        assertMimeType(resolver, null, "empty.dtd");
    }

    /** Test cpp declarative MIME resolver. */
    public void testCppResolver() throws Exception {
        MIMEResolver resolver = createResolver(resolversRoot.getFileObject("cpp-resolver.xml"));
        assertMimeType(resolver, "text/x-c++", "cpp");
        assertMimeType(resolver, null, "cpp.not");
    }

    /** Test c declarative MIME resolver. It must be case insensitive on windows too (#176448). */
    public void testCResolver() throws Exception {
        MIMEResolver resolver = createResolver(resolversRoot.getFileObject("c-resolver.xml"));
        assertMimeType(resolver, "text/x-c", "c.c");
        assertMimeType(resolver, "text/x-c++", "cplusplus.C");
    }

    /** Test exit element in MIME resolver. */
    public void testExitResolver() throws Exception {
        MIMEResolver resolver = createResolver(resolversRoot.getFileObject("exit-resolver.xml"));
        assertMimeType(resolver, null, "php.txt");
    }

    /** Test ns element in xml-rule resolver. */
    public void testNSResolver() throws Exception {
        MIMEResolver resolver = createResolver(resolversRoot.getFileObject("ns.xml"));
        assertMimeType(resolver, "ns.xml", "ns.xml");
        // #177443
        resolver = createResolver(resolversRoot.getFileObject("ns1.xml"));
        assertMimeType(resolver, "ns1", "ns.xml");
    }

    /** Tests concurrent threads accessing MIMEResolverImpl. */
    public void testDeadlock163378() throws Exception {
        final MIMEResolver declarativeResolver = createResolver(resolversRoot.getFileObject("pattern-resolver-valid.xml"));
        Handler handler = new Handler() {

            private boolean threadStarted = false;

            @Override
            public void publish(LogRecord record) {
                if (!threadStarted && "findMIMEType - smell.resolve.".equals(record.getMessage())) {
                    Thread lockingThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            declarativeResolver.findMIMEType(root.getFileObject("empty.dtd"));
                        }
                    }, "Locking");
                    threadStarted = true;
                    lockingThread.start();
                    try {
                        lockingThread.join();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        Logger logger = Logger.getLogger(MIMEResolverImpl.class.getName());
        logger.addHandler(handler);
        logger.setLevel(Level.FINEST);
        declarativeResolver.findMIMEType(root.getFileObject("empty.dtd"));
        logger.removeHandler(handler);
    }

    /**
     * Bug 240518 - org.openide.filesystems.FileAlreadyLockedException:
     * Services/MIMEResolver/user-defined-mime-resolver.xml.
     *
     * @throws InterruptedException
     */
    public void testStoreUserDefinedResolver() throws InterruptedException {
        final Logger mimeResLog = Logger.getLogger(
                MIMEResolverImpl.class.getName());
        final Map<String, Set<String>> mimeToExtensions
                = new HashMap<String, Set<String>>();
        final Throwable[] throwable = new Throwable[1];
        mimeToExtensions.put("text/plan", Collections.singleton("log"));

        Runnable r = new Runnable() {
            @Override
            public void run() {
                MIMEResolverImpl.storeUserDefinedResolver(mimeToExtensions);
            }
        };

        Handler h = new Handler() {

            @Override
            public void publish(LogRecord record) {
                String msg = record.getMessage();
                if (msg != null && msg.startsWith("Cannot delete resolver ")) {
                    throwable[0] = record.getThrown();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        mimeResLog.addHandler(h);
        try {
            // run now to initialize the file
            r.run();

            // run twice in parallel.
            for (int i = 0; i < 10; i++) {
                Thread t1 = new Thread(r, "T1");
                Thread t2 = new Thread(r, "T2");
                t1.start();
                t2.start();
                t1.join();
                t2.join();
                assertNull("No error should occur", throwable[0]);
            }
        } finally {
            mimeResLog.removeHandler(h);
        }
    }
}
