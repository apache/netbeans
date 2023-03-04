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

package org.netbeans.modules.parsing.impl.indexing.errors;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.FileObjectIndexable;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.Convertor;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache.ErrorKind;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.tasklist.trampoline.Accessor;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
public class TaskProviderTest extends NbTestCase {
    
    public TaskProviderTest(String testName) {
        super(testName);
    }

    private FileObject src;
    private FileObject file1;
    private FileObject file2;
    private FileObject file3;

    private static ClassPath SOURCE;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        File f = getWorkDir();
        
        clearWorkDir();
        
        FileObject workDir = FileUtil.toFileObject(f);
        
        assertNotNull(f);
        
        src = workDir.createFolder("src");
        
        FileObject cache = workDir.createFolder("cache");
        FileObject build = workDir.createFolder("build");
        FileObject pack = src.createFolder("pack");
        
        file1 = pack.createData("file1.java");
        file2 = pack.createData("file2.java");
        file3 = pack.createData("file3.java");

        CacheFolder.setCacheFolder(cache);

        SOURCE = ClassPathSupport.createClassPath(src);
        MockServices.setServices(CPPImpl.class);
    }
    
    public void testUpdates() throws Exception {
        TaskProvider jtp = new TaskProvider();
        TaskManagerImpl tm = new TaskManagerImpl();
        TaskScanningScopeImpl scope1 = new TaskScanningScopeImpl(file1, file2);
        
        ErrorsCache.setErrors(src.getURL(), SPIAccessor.getInstance().create(new FileObjectIndexable(src, file1)), Arrays.asList(new DiagnosticImpl(ErrorKind.ERROR, "x", 3)), ERROR_CONVERTOR);
        
        jtp.setScope(scope1, Accessor.DEFAULT.createCallback(tm, jtp));
        jtp.waitWorkFinished();
        
        assertTasks(file1, Task.create(file1, "nb-tasklist-error", "x", 3));
        assertTasks(file2);
        
        ErrorsCache.setErrors(src.getURL(), SPIAccessor.getInstance().create(new FileObjectIndexable(src, file2)), Arrays.asList(new DiagnosticImpl(ErrorKind.ERROR, "y", 4)), ERROR_CONVERTOR);
        TaskProvider.refresh(file2);
        jtp.waitWorkFinished();
        
        assertTasks(file1, Task.create(file1, "nb-tasklist-error", "x", 3));
        assertTasks(file2, Task.create(file2, "nb-tasklist-error", "y", 4));
    }
    
    private void assertTasks(FileObject file, Task... tasks) {
        Collection<? extends Task> inTasklist = tasklist.get(file);
        
        if (inTasklist == null || inTasklist.isEmpty()) {
            assertTrue(Arrays.asList(tasks).toString(), tasks.length == 0);
        }
        
        assertEquals(tasks.length, inTasklist.size());
        
        int index = 0;
        
        for (Task t : inTasklist) {
            assertEquals(tasks[index++], t);
        }
    }

    private Map<FileObject, Collection<? extends Task>> tasklist = new HashMap<FileObject, Collection<? extends Task>>();
    
    private class TaskManagerImpl extends TaskManager {

        public void refresh(FileTaskScanner scanner, FileObject... files) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void refresh(FileTaskScanner scanner) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void refresh(TaskScanningScope scope) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void started(PushTaskScanner scanner) {}

        public void finished(PushTaskScanner scanner) {}

        public void setTasks(PushTaskScanner scanner, FileObject resource, List<? extends Task> tasks) {
            tasklist.put(resource, tasks);
        }

        public void clearAllTasks(PushTaskScanner scanner) {
            tasklist.clear();
        }

        public boolean isObserved() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public boolean isCurrentEditorScope() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    private static class TaskScanningScopeImpl extends TaskScanningScope {

        private List<FileObject> files;
        private ChangeableLookup l;
        
        public TaskScanningScopeImpl(FileObject... files) {
            super("", "", null, false);
            this.files = Arrays.asList(files);
            this.l = new ChangeableLookup();
            setFiles(files);
        }
        
        public boolean isInScope(FileObject resource) {
            return files.contains(resource);
        }

        public void attach(Callback callback) {
        }

        public Lookup getLookup() {
            return l;
        }

        public Iterator<FileObject> iterator() {
            return files.iterator();
        }
        
        public void setFiles(FileObject... files) {
            l.setLookupsImpl(Lookups.fixed((Object[]) files));
        }
        
    }
    
    private static class DiagnosticImpl {

        private final ErrorKind k;
        private final String message;
        private final int line;

        public DiagnosticImpl(ErrorKind k, String message, int line) {
            this.k = k;
            this.message = message;
            this.line = line;
        }
        
    }

    private static final Convertor<DiagnosticImpl> ERROR_CONVERTOR = new Convertor<DiagnosticImpl>() {

        public ErrorKind getKind(DiagnosticImpl t) {
            return t.k;
        }

        public int getLineNumber(DiagnosticImpl t) {
            return t.line;
        }

        public String getMessage(DiagnosticImpl t) {
            return t.message;
        }

    };
    
    private static class ChangeableLookup extends ProxyLookup {

        public void setLookupsImpl(Lookup... lookups) {
            super.setLookups(lookups);
        }
        
    }

    public static final class CPPImpl implements ClassPathProvider {
        public ClassPath findClassPath(FileObject file, String type) {
            if (ClassPath.SOURCE.equals(type)) {
                return SOURCE;
            }

            return null;
        }

    }

    @ServiceProvider(service=PathRecognizer.class)
    public static final class PathRecognizerImpl extends PathRecognizer {

        @Override
        public Set<String> getSourcePathIds() {
            return Collections.singleton(ClassPath.SOURCE);
        }

        @Override
        public Set<String> getLibraryPathIds() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getBinaryLibraryPathIds() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> getMimeTypes() {
            return Collections.emptySet();
        }

    }
    
}
