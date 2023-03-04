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
package org.netbeans.modules.editor.lib;

import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.document.OnSaveTask;

/**
 *
 * @author Miloslav Metelka
 */
public class BeforeSaveTasksTest extends NbTestCase {

    private static final String MIME_TYPE = "text/x-test-on-save";

    public BeforeSaveTasksTest(String name) {
        super(name);
    }

    public void testOnSaveTasks() {
        MockServices.setServices(MockMimeLookup.class);
        MockMimeLookup.setInstances(MimePath.parse(MIME_TYPE),
                new TestOnSaveTask1.TestFactory1(),
                new TestOnSaveTask2.TestFactory2()
        );
        BaseDocument doc = new BaseDocument(false, MIME_TYPE);
        BeforeSaveTasks.get(doc);
        Runnable beforeSaveRunnable = (Runnable) doc.getProperty("beforeSaveRunnable");
        beforeSaveRunnable.run();
        assertNotNull("TestOnSaveTask2 not created", TestOnSaveTask2.TestFactory2.lastCreatedTask);
        assertTrue("TestOnSaveTask2 not run", TestOnSaveTask2.TestFactory2.lastCreatedTask.taskPerformed);
    }

    private static final class TestOnSaveTask1 implements OnSaveTask {

        boolean taskLocked;

        boolean taskPerformed;

        TestOnSaveTask1(Context context) {
        }

        @Override
        public void performTask() {
            assertTrue("Task not locked", taskLocked);
            assertFalse("Task run multiple times", taskPerformed);
            taskPerformed = true;
        }

        @Override
        public void runLocked(Runnable run) {
            taskLocked = true;
            try {
                run.run();
            } finally {
                taskLocked = false;
            }
        }

        @Override
        public boolean cancel() {
            return true;
        }
        
        static final class TestFactory1 implements OnSaveTask.Factory {

            static TestOnSaveTask1 lastCreatedTask;

            public OnSaveTask createTask(Context context) {
                assertNotNull("Context null", context);
                return (lastCreatedTask = new TestOnSaveTask1(context));
            }

        }

    }

    private static final class TestOnSaveTask2 implements OnSaveTask {

        boolean taskLocked;

        boolean taskPerformed;

        TestOnSaveTask2(OnSaveTask.Context context) {
        }

        @Override
        public void performTask() {
            assertTrue("Task1 not locked", TestOnSaveTask1.TestFactory1.lastCreatedTask.taskLocked);
            assertTrue("Task1 not performed yet", TestOnSaveTask1.TestFactory1.lastCreatedTask.taskPerformed);

            assertTrue("Task not locked", taskLocked);
            assertFalse("Task run multiple times", taskPerformed);
            taskPerformed = true;
        }

        @Override
        public void runLocked(Runnable run) {
            taskLocked = true;
            try {
                run.run();
            } finally {
                taskLocked = false;
            }
        }

        @Override
        public boolean cancel() {
            return true;
        }
        
        static final class TestFactory2 implements OnSaveTask.Factory {

            static TestOnSaveTask2 lastCreatedTask;

            public OnSaveTask createTask(OnSaveTask.Context context) {
                assertNotNull("Context null", context);
                return (lastCreatedTask = new TestOnSaveTask2(context));
            }

        }

    }

}
