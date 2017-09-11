/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
