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

package org.netbeans.spi.tasklist;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.*;
import org.netbeans.modules.tasklist.trampoline.TaskGroupFactory;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;



/** 
 * Tests for Task class.
 * 
 * @author S. Aubrecht
 */
public class ScannerCallbackTest extends NbTestCase {

    public static final String TASK_GROUP_NAME = "nb-tasklist-unittest";
    
    private MyTaskManager taskManager;
    private MyFileScanner fileScanner;
    private MySimpleScanner simpleScanner;
    
    static {
        String[] layers = new String[] {"org/netbeans/spi/tasklist/resources/mf-layer.xml"};//NOI18N
        IDEInitializer.setup(layers,new Object[0]);
    }
    
    public ScannerCallbackTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        assertNotNull( "make sure we have a task group ready for testing", 
                TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME ) );
        
        taskManager = new MyTaskManager();
        fileScanner = new MyFileScanner();
        simpleScanner = new MySimpleScanner();
    }

    public void testFileScanner() {
        FileTaskScanner.Callback callback = AccessorImpl.DEFAULT.createCallback( taskManager, fileScanner );
        
        callback.refreshAll();
        assertEquals( fileScanner, taskManager.refreshedScanner );
        
        taskManager.refreshedScanner = null;
        FileObject fo = FileUtil.getConfigRoot();
        callback.refresh( fo );
        assertEquals( fileScanner, taskManager.refreshedScanner );
        assertEquals( fo, taskManager.refreshedResources[0] );
    }

    public void testSimpleScanner() {
        PushTaskScanner.Callback callback = AccessorImpl.DEFAULT.createCallback( taskManager, simpleScanner );
        
        callback.started();
        assertEquals( simpleScanner, taskManager.startedScanner );
        
        callback.finished();
        assertEquals( simpleScanner, taskManager.finishedScanner );
        
        callback.clearAllTasks();
        assertEquals( simpleScanner, taskManager.clearedScanner );
        
        
        FileObject fo = FileUtil.getConfigRoot();
        List<? extends Task> tasks = new ArrayList<Task>();
        callback.setTasks(fo, tasks);
        assertEquals( simpleScanner, taskManager.setTasksScanner );
        assertEquals( fo, taskManager.resource );
        assertEquals( tasks, taskManager.tasks );
    }

    private class MyTaskManager extends TaskManager {
        
        FileTaskScanner refreshedScanner;
        FileObject[] refreshedResources;
        PushTaskScanner startedScanner;
        PushTaskScanner finishedScanner;
        PushTaskScanner clearedScanner;
        
        PushTaskScanner setTasksScanner;
        FileObject resource;
        List<? extends Task> tasks;

        public void refresh(FileTaskScanner scanner, FileObject... files) {
            refreshedScanner = scanner;
            refreshedResources = files;
        }

        public void refresh(FileTaskScanner scanner) {
            refreshedScanner = scanner;
        }

        public void refresh(TaskScanningScope scope) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void started(PushTaskScanner scanner) {
            startedScanner = scanner;
        }

        public void finished(PushTaskScanner scanner) {
            finishedScanner = scanner;
        }

        public void setTasks(PushTaskScanner scanner, FileObject resource,
                             List<? extends Task> tasks) {
            setTasksScanner = scanner;
            this.resource = resource;
            this.tasks = tasks;
        }

        public void clearAllTasks(PushTaskScanner scanner) {
            clearedScanner = scanner;
        }

        @Override
        public boolean isObserved() {
            return true;
        }

        @Override
        public boolean isCurrentEditorScope() {
            return false;
        }
        
    }
    
    private class MySimpleScanner extends PushTaskScanner {

        public MySimpleScanner() {
            super( "simple scanner", "simple scanner", null );
        }
        
        public void setScope(TaskScanningScope scope, Callback callback) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    private class MyFileScanner extends FileTaskScanner {

        public MyFileScanner() {
            super( "file scanner", "file scanner", null );
        }
        
        public List<? extends Task> scan(FileObject resource) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void attach(Callback callback) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

