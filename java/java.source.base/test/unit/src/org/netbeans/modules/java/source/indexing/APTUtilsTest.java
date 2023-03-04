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
package org.netbeans.modules.java.source.indexing;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class APTUtilsTest extends NbTestCase {

    private FileObject root1;
    private FileObject root2;
    private FileObject root3;
    private FileObject root4;
    private MutableCP processorPath;
    private MockHandler handler;
    private Logger imLogger;
    private Level imLoggerOrigLevel;

    public APTUtilsTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject cache = FileUtil.createFolder(
            FileUtil.normalizeFile(
                new File(getWorkDir(),"cache")));   //NOI18N
        CacheFolder.setCacheFolder(cache);
        root1 = FileUtil.createFolder(
            FileUtil.normalizeFile(
                new File(getWorkDir(),"src1"))); //NOI18N
        assertNotNull(root1);
        assertTrue(root1.isValid());
        assertTrue(root1.isFolder());
        root2 = FileUtil.createFolder(
            FileUtil.normalizeFile(
                new File(getWorkDir(),"src2"))); //NOI18N
        assertNotNull(root2);
        assertTrue(root2.isValid());
        assertTrue(root2.isFolder());  
        root3 = FileUtil.createFolder(
            FileUtil.normalizeFile(
                new File(getWorkDir(),"src3"))); //NOI18N
        assertNotNull(root3);
        assertTrue(root3.isValid());
        assertTrue(root3.isFolder());  
        root4 = FileUtil.createFolder(
            FileUtil.normalizeFile(
                new File(getWorkDir(),"src4"))); //NOI18N
        assertNotNull(root4);
        assertTrue(root4.isValid());
        assertTrue(root4.isFolder());

        processorPath = new MutableCP();
        processorPath.add(root1);
        CPP.cps = Collections.singletonMap(
            JavaClassPathConstants.PROCESSOR_PATH,
            Collections.singletonMap(root1, ClassPathFactory.createClassPath(processorPath)));
        SLQ.result.setSourceLevel("1.5");   //NOI18N
        APQ.result.setAnnotationProcessingEnabled(EnumSet.of(Trigger.ON_SCAN));
        MockServices.setServices(CPP.class, SLQ.class, APQ.class);
        MockHandler.currentRoot = root1;
        handler = new MockHandler();
        imLogger = Logger.getLogger(IndexingManager.class.getName());
        imLoggerOrigLevel = imLogger.getLevel();
        imLogger.setLevel(Level.FINEST);
        imLogger.addHandler(handler);

    }

    @Override
    protected void tearDown() throws Exception {
        if (imLogger != null) {
            imLogger.setLevel(imLoggerOrigLevel);
            imLogger.removeHandler(handler);
        }
        super.tearDown();
    }



                
    
    public void testRefreshedIndexOnceAfterProcessorPathChange() throws InterruptedException {
        final APTUtils au = APTUtils.get(root1);
        assertNotNull(au);
        handler.reset();
        processorPath.add(root2);
        processorPath.add(root3);
        processorPath.add(root4);
        assertEquals(1, handler.awaitEvents(10, 2500));
        handler.reset();
        processorPath.remove(root2);
        processorPath.remove(root3);
        processorPath.remove(root4);
        assertEquals(1, handler.awaitEvents(10, 2500));
    }

    public void testRefreshedIndexOnceAfterSourceLevelChange() throws InterruptedException {
        final APTUtils au = APTUtils.get(root1);
        assertNotNull(au);
        handler.reset();
        SLQ.result.setSourceLevel("1.6");   //NOI18N
        SLQ.result.setSourceLevel("1.7");   //NOI18N
        SLQ.result.setSourceLevel("1.5");   //NOI18N
        SLQ.result.setSourceLevel("1.6");   //NOI18N
        assertEquals(1, handler.awaitEvents(10, 2500));
    }


    public void testRefreshedIndexOnceAfterAnnotationProcessingChange() throws InterruptedException {
        final APTUtils au = APTUtils.get(root1);
        assertNotNull(au);
        handler.reset();
        APQ.result.setAnnotationProcessingEnabled(EnumSet.of(Trigger.ON_SCAN, Trigger.IN_EDITOR));
        APQ.result.setAnnotationProcessingEnabled(EnumSet.noneOf(Trigger.class));
        APQ.result.setAnnotationProcessingEnabled(EnumSet.of(Trigger.ON_SCAN));
        APQ.result.setAnnotationProcessingEnabled(EnumSet.of(Trigger.IN_EDITOR));
        assertEquals(1, handler.awaitEvents(10, 2500));
    }


    private static final class MockHandler extends Handler {

        static FileObject currentRoot;

        //@GuardedBy("this")
        private int numberOfEvents;

        public synchronized void reset() {
            numberOfEvents = 0;
        }

        @Override
        public void publish(LogRecord record) {
            if ("Request to add indexing job for root: {0}".equals(record.getMessage()) &&
                 record.getParameters().length == 1 &&
                 currentRoot.toURL().equals(record.getParameters()[0])) {  //NOI18N
                synchronized (this) {
                    numberOfEvents++;
                    notifyAll();
                }
            }
        }

        public int awaitEvents(int count, long deadline) throws InterruptedException {
            long st = System.currentTimeMillis();
            synchronized (this) {
                while (count != numberOfEvents) {
                    wait(deadline);
                    long et = System.currentTimeMillis();
                    if (et-st > deadline) {
                        break;
                    }
                }
                return numberOfEvents;
            }            
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }


    private static final class MutableCP implements ClassPathImplementation {

        private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
        private final List<FileObject> roots = Collections.synchronizedList(new ArrayList<FileObject>());


        void add(FileObject... fos) {
            roots.addAll(Arrays.asList(fos));
            listeners.firePropertyChange(PROP_RESOURCES,null,null);
        }

        void remove(FileObject... fos) {
            roots.removeAll(roots);
            listeners.firePropertyChange(PROP_RESOURCES,null,null);
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            final List<PathResourceImplementation> res = new ArrayList<PathResourceImplementation>();
            synchronized (roots) {
                for (FileObject root : roots) {
                    res.add(ClassPathSupport.createResource(root.toURL()));
                }
            }
            return res;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.removePropertyChangeListener(listener);
        }
    }

    public static final class CPP implements ClassPathProvider {

        static volatile Map<String,Map<FileObject, ClassPath>> cps;

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            final Map<String,Map<FileObject, ClassPath>> dc = cps;
            if (dc == null) {
                return null;
            }
            final Map<FileObject,ClassPath> root2cps = dc.get(type);
            if (root2cps == null) {
                return null;
            }
            return root2cps.get(file);
        }                

    }
    
    public static final class MSL implements SourceLevelQueryImplementation2.Result {
        
        private final ChangeSupport support = new ChangeSupport(this);
        private volatile String sl;
        
        
        void setSourceLevel(String sourceLevel) {
            sl = sourceLevel;
            support.fireChange();
        }

        @Override
        public String getSourceLevel() {
            return sl;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            support.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            support.removeChangeListener(listener);
        }
        
    }

    public static final class SLQ implements SourceLevelQueryImplementation2 {

        static final MSL result = new MSL();

        @Override
        public Result getSourceLevel(FileObject javaFile) {
            return result;
        }

    }
    
    public static final class MAP implements AnnotationProcessingQuery.Result {
        
        private final ChangeSupport listeners = new ChangeSupport(this);
        private volatile Set<? extends Trigger> mode = Collections.emptySet();
        
        
        void setAnnotationProcessingEnabled(Set<? extends Trigger> newMode) {
            this.mode = newMode;
            listeners.fireChange();
        }

        @Override
        public Set<? extends Trigger> annotationProcessingEnabled() {
            return mode;
        }

        @Override
        public Iterable<? extends String> annotationProcessorsToRun() {
            return null;
        }

        @Override
        public URL sourceOutputDirectory() {
            return null;
        }

        @Override
        public Map<? extends String, ? extends String> processorOptions() {
            return Collections.emptyMap();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            listeners.removeChangeListener(l);
        }
        
    }

    public static final class APQ implements AnnotationProcessingQueryImplementation {

        static final MAP result = new MAP();

        @Override
        public AnnotationProcessingQuery.Result getAnnotationProcessingOptions(FileObject file) {
            return result;
        }

    }

}
