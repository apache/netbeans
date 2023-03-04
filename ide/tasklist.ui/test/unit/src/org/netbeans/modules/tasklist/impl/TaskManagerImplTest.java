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

package org.netbeans.modules.tasklist.impl;

import java.util.Collections;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner.Callback;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class TaskManagerImplTest extends NbTestCase {
    
    public TaskManagerImplTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 300000;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workDir);
        
        file1 = workDir.createData("file1.txt");
        super.setUp();
    }

    private FileObject file1;
    
    /**IZ #100463
     */
    public void testProviderCanStartImmediately() throws Exception {
        final PushTaskScanner scanner = new PushTaskScanner("", "", null) {
            public void setScope(TaskScanningScope scope, Callback callback) {
                callback.started();
                callback.setTasks(file1, Collections.singletonList(Task.create(file1, "unknown", "x", 2)));
                callback.finished();
            }
        };
        
        TaskManagerImpl impl = new TaskManagerImpl() {
            @Override
            Iterable<? extends FileTaskScanner> getFileScanners() {
                return Collections.<FileTaskScanner>emptyList();
            }

            @Override
            Iterable<? extends PushTaskScanner> getPushScanners() {
                return Collections.singletonList(scanner);
            }
        };
        
        impl.observe(new TaskScanningScope("", "", null) {
            public boolean isInScope(FileObject resource) {
                return (resource == file1);
            }
            
            public void attach(Callback callback) {
            }
            
            public Lookup getLookup() {
                return Lookups.singleton(file1);
            }
            
            public Iterator<FileObject> iterator() {
                return Collections.singletonList(file1).iterator();
            }
        }, TaskFilter.EMPTY);

        impl._waitFinished();
        
        assertEquals(1, impl.getTasks().getTasks().size());
    }

    @RandomlyFails // NB-Core-Build #1732: impl._waitFinished() hangs
    public void testProviderCanRemoveTasks() throws Exception {
        final Callback[] cb = new Callback[1];
        final PushTaskScanner scanner = new PushTaskScanner("", "", null) {
            public void setScope(TaskScanningScope scope, Callback callback) {
                cb[0] = callback;
                callback.started();
                callback.setTasks(file1, Collections.singletonList(Task.create(file1, "unknown", "x", 2)));
                callback.finished();
            }
        };
        
        TaskManagerImpl impl = new TaskManagerImpl() {
            @Override
            Iterable<? extends FileTaskScanner> getFileScanners() {
                return Collections.<FileTaskScanner>emptyList();
            }
            
            @Override
            Iterable<? extends PushTaskScanner> getPushScanners() {
                return Collections.singletonList(scanner);
            }
        };
        
        impl.observe(new TaskScanningScope("", "", null) {
            public boolean isInScope(FileObject resource) {
                return (resource == file1);
            }
            
            public void attach(Callback callback) {
            }
            
            public Lookup getLookup() {
                return Lookups.singleton(file1);
            }
            
            public Iterator<FileObject> iterator() {
                return Collections.singletonList(file1).iterator();
            }
        }, TaskFilter.EMPTY);
        
        impl._waitFinished();
        
        assertEquals(1, impl.getTasks().getTasks().size());

        cb[0].started();
        cb[0].setTasks(file1, Collections.<Task>emptyList());
        cb[0].finished();
        
        assertTrue(impl.getTasks().getTasks().isEmpty());
    }
    
}
