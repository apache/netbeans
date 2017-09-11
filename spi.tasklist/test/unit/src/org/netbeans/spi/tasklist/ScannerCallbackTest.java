/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
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

