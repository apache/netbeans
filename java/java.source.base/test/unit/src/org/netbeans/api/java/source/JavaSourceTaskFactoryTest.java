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
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.JavaSourceTaskFactoryManager;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Lahoda
 */
public class JavaSourceTaskFactoryTest extends NbTestCase {
    
    public JavaSourceTaskFactoryTest(String testName) {
        super(testName);
    }
    
    private List<FileObject> files;
    private List<FileObject> filesWithTasks = new ArrayList<FileObject>();
    private Map<FileObject, CancellableTask<CompilationInfo>> file2Task = new HashMap<>();
    
    private Map<FileObject, CancellableTask<CompilationInfo>> addedTasks = new HashMap<FileObject, CancellableTask<CompilationInfo>>();
    private Map<FileObject, CancellableTask<CompilationInfo>> removedTasks = new HashMap<FileObject, CancellableTask<CompilationInfo>>();
    private Map<FileObject, CancellableTask<CompilationInfo>> rescheduled = new HashMap<FileObject, CancellableTask<CompilationInfo>>();

    private FileObject testDir;
    private FileObject testFile1;
    private FileObject testFile2;
    private DummyCancellableTask<CompilationInfo> task1;
    private DummyCancellableTask<CompilationInfo> task2;
    
    private JavaSourceTaskFactoryImplImpl jstf;
    private ClassPathProvider cpp;
    
    private Lookup.Result<JavaSourceTaskFactory> factories;
    
    protected void setUp() throws Exception {
        JavaSourceTaskFactory.SYNCHRONOUS_EVENTS = true;
        cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type == ClassPath.SOURCE)
                    return ClassPathSupport.createClassPath(new FileObject[] {FileUtil.toFileObject(getDataDir())});
                    if (type == ClassPath.COMPILE)
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    if (type == ClassPath.BOOT)
                        return createBootPath();
                    return null;
            }
        };
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            cpp
        }, this.getClass().getClassLoader());


        jstf = new JavaSourceTaskFactoryImplImpl();
        JavaSourceTaskFactory.ACCESSOR2 = new AccessorImpl();
        testDir = SourceUtilsTestUtil.makeScratchDir(this);
        testFile1 = testDir.createData("test1.java");
        testFile2 = testDir.createData("test2.java");
        task1 = new DummyCancellableTask<CompilationInfo>();
        task2 = new DummyCancellableTask<CompilationInfo>();

        file2Task.put(testFile1, task1);
        file2Task.put(testFile2, task2);

        assertNotNull(JavaSource.forFileObject(testFile1));
        assertNotNull(JavaSource.forFileObject(testFile2));

        assertEquals(2, file2Task.size());

        JavaSourceTaskFactoryManager.register();
    }

    public void testTasksRegistration() throws Exception {
        JavaSourceTaskFactory.SYNCHRONOUS_EVENTS = true;
        
        files = Arrays.asList(testFile1);
        
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            jstf,
            cpp
        }, this.getClass().getClassLoader());

        /*
         * Dirty hack to wait for finish of assynchronous initialization... 
         */
        synchronized (this) {
            wait(1000);
        }

        assertEquals(1, addedTasks.size());
        assertEquals(testFile1, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), addedTasks.values().iterator().next());
        
        assertEquals(0, removedTasks.size());
        
        files = Arrays.asList(testFile2);
        
        addedTasks.clear();
        
        jstf.fireChangeEvent();
        
        assertEquals(1, removedTasks.size());
        assertEquals(testFile1, removedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), removedTasks.values().iterator().next());
        
        assertEquals(1, addedTasks.size());
        assertEquals(testFile2, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile2), addedTasks.values().iterator().next());

        files = Collections.emptyList();
        
        addedTasks.clear();
        removedTasks.clear();
        
        jstf.fireChangeEvent();
        
        assertEquals(1, removedTasks.size());
        assertEquals(testFile2, removedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile2), removedTasks.values().iterator().next());
        
        assertEquals(0, addedTasks.size());
        
        files = Arrays.asList(testFile1);
        
        addedTasks.clear();
        removedTasks.clear();
        
        jstf.fireChangeEvent();
        
        assertEquals(1, addedTasks.size());
        assertEquals(testFile1, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), addedTasks.values().iterator().next());
        
        assertEquals(0, removedTasks.size());
        
        files = Collections.emptyList();
        
        addedTasks.clear();
        removedTasks.clear();
        
        jstf.fireChangeEvent();
        
        assertEquals(1, removedTasks.size());
        assertEquals(testFile1, removedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), removedTasks.values().iterator().next());
        
        assertEquals(0, addedTasks.size());
    }
    
    public void testTasksRescheduling() throws Exception {
        files = Arrays.asList(testFile1);
        
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            jstf,
            cpp
        }, this.getClass().getClassLoader());

        synchronized (this) {
            wait(1000);
        }

        assertEquals(1, addedTasks.size());
        assertEquals(testFile1, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), addedTasks.values().iterator().next());
        
        jstf.reschedule(testFile1);
        
        assertEquals(1, rescheduled.size());
        assertEquals(testFile1, rescheduled.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), rescheduled.values().iterator().next());
        
        //#84783: the IAE was temporarily disabled:
//        //test if the IllegalArgumentException is thrown correctly:
//        try {
//            jstf.reschedule(testFile2);
//            fail("Did not throw an IllegalArgumentException");
//        } catch (IllegalArgumentException e) {
//        }
    }
    
    public void testFileIsReclaimable() throws Exception {
        Reference fileRef = new WeakReference(testFile1);
        Reference jsRef = new WeakReference(JavaSource.forFileObject(testFile1));
        files = Arrays.asList(testFile1);
        
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
            jstf,
        }, this.getClass().getClassLoader());

        synchronized (this) {
            wait(1000);
        }
        

        assertEquals(1, addedTasks.size());
        assertEquals(testFile1, addedTasks.keySet().iterator().next());
        assertEquals(file2Task.get(testFile1), addedTasks.values().iterator().next());
        
        files = Collections.emptyList();
        
        jstf.fireChangeEvent();
        
        filesWithTasks.clear();
        file2Task.clear();
        
        addedTasks.clear();
        removedTasks.clear();
        rescheduled.clear();
        
        testDir = null;
        testFile1 = null;
        testFile2 = null;
        task1 = null;
        task2 = null;
        
        assertGC("", fileRef);
        assertGC("", jsRef);
    }
    
    public void testDeadlock88782() throws Exception {
        files = Collections.emptyList();
        
        SourceUtilsTestUtil.setLookup(new Object[] {
            JavaDataLoader.getLoader(JavaDataLoader.class),
                    jstf,
                    cpp
        }, this.getClass().getClassLoader());
        
        final CountDownLatch l = new CountDownLatch(2);
        final Object lock = new Object();
        
        Logger.getLogger(JavaSourceTaskFactory.class.getName()).setLevel(Level.FINEST);
        
        Logger.getLogger(JavaSourceTaskFactory.class.getName()).addHandler(new Handler() {
            public void publish(LogRecord record) {
                if (JavaSourceTaskFactory.BEFORE_ADDING_REMOVING_TASKS.equals(record.getMessage())) {
                    l.countDown();
                    try {
                        l.await();
                    } catch (InterruptedException e) {
                        Logger.global.log(Level.SEVERE, "", e);
                    }
                    synchronized (lock) {
                    }
                }
                if (JavaSourceTaskFactory.FILEOBJECTS_COMPUTATION.equals(record.getMessage())) {
                    l.countDown();
                    try {
                        l.await();
                    } catch (InterruptedException e) {
                        Logger.global.log(Level.SEVERE, "", e);
                    }
                }
            }
            public void flush() {}
            public void close() throws SecurityException {}
        });
        
        Thread t1 = new Thread() {
            public void run() {
                synchronized (lock) {
                    SourceUtilsTestUtil.setLookup(new Object[] {
                        JavaDataLoader.getLoader(JavaDataLoader.class),
                                jstf,
                                new JavaSourceTaskFactoryImplImpl(),
                                cpp
                    }, this.getClass().getClassLoader());
                }
            }
        };
        
        t1.start();

        Thread t2 = new Thread() {
            public void run() {
                jstf.fireChangeEvent();
            }
        };
        
        t2.start();
        
        t1.join();
        t2.join();
    }
    
    private ClassPath createBootPath () {
        try {
            String bootPath = System.getProperty ("sun.boot.class.path");
            String[] paths = bootPath.split(File.pathSeparator);
            List<URL>roots = new ArrayList<URL> (paths.length);
            for (String path : paths) {
                File f = new File (path);            
                if (!f.exists()) {
                    continue;
                }
                URL url = Utilities.toURI(f).toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot(url);
                }
                roots.add (url);
            }
            return ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
        } catch (MalformedURLException ex) {}
        return null;
    }
    
    private class AccessorImpl implements JavaSourceTaskFactory.Accessor2 {
        
        public void addPhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task, Phase phase, Priority priority, TaskIndexingMode im) {
            addedTasks.put(js.getFileObjects().iterator().next(), task);
        }

        public void removePhaseCompletionTask(JavaSource js, CancellableTask<CompilationInfo> task) {
            removedTasks.put(js.getFileObjects().iterator().next(), task);
        }
        
        public void rescheduleTask(JavaSource js, CancellableTask<CompilationInfo> task) {
            rescheduled.put(js.getFileObjects().iterator().next(), task);
        }
        
    }
    
    private static class DummyCancellableTask<CompilationInfo> implements CancellableTask<CompilationInfo> {
        
        public void cancel() {
        }

        public void run(CompilationInfo parameter) {
        }
        
    }

    private class JavaSourceTaskFactoryImplImpl extends JavaSourceTaskFactory {
        public JavaSourceTaskFactoryImplImpl() {
            super(Phase.UP_TO_DATE, Priority.MAX);
        }

        public CancellableTask<CompilationInfo> createTask(FileObject file) {
            filesWithTasks.add(file);
            CancellableTask<CompilationInfo> task = file2Task.get(file);
            if (task == null) {
                System.out.println("WARN: Instantiating empty dummy task");
                new DummyCancellableTask<CompilationInfo>();
            }
            return task;                                    
        }

        public synchronized List<FileObject> getFileObjects() {
            return files;
        }

        private void fireChangeEvent() {
            super.fileObjectsChanged();
        }
        
    }
    
    private static class ChangeableLookup extends ProxyLookup {
        
        public void setLookupsImpl(Lookup[] lookups) {
            setLookups(lookups);
        }
    }
    
}
