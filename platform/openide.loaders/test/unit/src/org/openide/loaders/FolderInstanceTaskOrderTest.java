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

package org.openide.loaders;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.filesystems.*;
import org.openide.cookies.*;
import org.openide.util.*;

import org.netbeans.junit.*;
import org.openide.actions.OpenAction;
import org.openide.actions.SaveAction;

public class FolderInstanceTaskOrderTest extends NbTestCase {
    private Logger err;

    public FolderInstanceTaskOrderTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir ();
        
        err = Logger.getLogger("test." + getName());
        err.info("setUp over: " + getName());
    }

    @RandomlyFails // NB-Core-Build #3078
    public void testReorderingOfExecutionTasksIsOK() throws Exception {
        String[] names = {
            "folder/"
        };
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), names);
        FileObject folder = lfs.findResource("folder");
        DataFolder f = DataFolder.findFolder(folder);

        InstanceDataObject.create(f, null, SaveAction.class);

        err.info("Creating InvCheckFolderInstance");
        ReorderTasksCheck instances = new ReorderTasksCheck(f);
        err.info("Computing result");
        instances.waitFinished(500);
        assertEquals("One task scheduled", 1, instances.tasks.size());

        InstanceDataObject.create(f, null, OpenAction.class);

        instances.waitFinished(500);
        assertEquals("Two tasks scheduled", 2, instances.tasks.size());

        // run in reverse order
        instances.tasks.get(1).run();
        instances.tasks.get(0).run();

        List computed = (List)instances.instanceCreate();
        err.info("Result is here: " + computed);
        assertEquals("Two actions", 2, computed.size());
    }

    public void testRunImmediatelly() throws Exception {
        String[] names = {
            "folder/"
        };
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), names);
        FileObject folder = lfs.findResource("folder");
        DataFolder f = DataFolder.findFolder(folder);

        InstanceDataObject.create(f, null, SaveAction.class);

        err.info("Creating InvCheckFolderInstance");
        RunImmediatelly instances = new RunImmediatelly(f);
        err.info("Computing result");
        List computed = (List)instances.instanceCreate();
        assertEquals("One action", 1, computed.size());

        InstanceDataObject.create(f, null, OpenAction.class);
        computed = (List)instances.instanceCreate();
        err.info("Result is here: " + computed);
        assertEquals("Two actions", 2, computed.size());
    }

    private final class ReorderTasksCheck extends FolderInstance {
        List<Task> tasks = new ArrayList<Task>();

        public ReorderTasksCheck(DataFolder f) {
            super(f);
        }
        
        protected Object createInstance(InstanceCookie[] cookies) throws IOException, ClassNotFoundException {
            ArrayList list = new ArrayList();
            for (int i = 0; i < cookies.length; i++) {
                list.add(cookies[i].instanceCreate());
            }
            return list;
        }
        @Override
        protected Task postCreationTask (Runnable run) {
            Task t = new Task(run);
            tasks.add(t);
            return t;
        }
    }

    private final class RunImmediatelly extends FolderInstance {
        public RunImmediatelly(DataFolder f) {
            super(f);
        }

        protected Object createInstance(InstanceCookie[] cookies) throws IOException, ClassNotFoundException {
            ArrayList list = new ArrayList();
            for (int i = 0; i < cookies.length; i++) {
                list.add(cookies[i].instanceCreate());
            }
            return list;
        }
        @Override
        protected Task postCreationTask (Runnable run) {
            run.run();
            return new FinishedTask();
        }

    }
    private static final class FinishedTask extends Task {
        public FinishedTask() {
            notifyFinished();
        }
    }
}
