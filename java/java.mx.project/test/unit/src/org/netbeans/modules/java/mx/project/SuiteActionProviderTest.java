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
package org.netbeans.modules.java.mx.project;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class SuiteActionProviderTest extends SuiteCheck {
    public SuiteActionProviderTest(String s) {
        super(s);
    }
    
    public static junit.framework.Test suite() {
        return suite(SuiteActionProviderTest.class);
    }
    
    public void testActionsEnabledWithProgress() throws Exception {
        File sdkSibling = findSuite("regex");

        FileObject fo = FileUtil.toFileObject(sdkSibling);
        assertNotNull("project directory found", fo);

        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project found", p);
        assertEquals("It is suite project: " + p, "SuiteProject", p.getClass().getSimpleName());

        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        assertNotNull("Action provider found", ap);

        {
            Lookup ctx = fo.getLookup();
            assertTrue("Build is supported", ap.isActionEnabled(ActionProvider.COMMAND_BUILD, ctx));
            assertTrue("Clean is supported", ap.isActionEnabled(ActionProvider.COMMAND_CLEAN, ctx));
            assertTrue("Build & Clean is supported", ap.isActionEnabled(ActionProvider.COMMAND_REBUILD, ctx));

            assertFalse("Move isn't supported", ap.isActionEnabled(ActionProvider.COMMAND_MOVE, ctx));
            assertFalse("Priming isn't (yet) supported", ap.isActionEnabled(ActionProvider.COMMAND_PRIME, ctx));
        }

        class MockProgress extends ActionProgress {

            volatile boolean started;
            volatile Boolean success;
            final CountDownLatch finished = new CountDownLatch(1);

            @Override
            protected void started() {
                this.started = true;
            }

            @Override
            public void finished(boolean success) {
                this.success = success;
                this.finished.countDown();
            }
        }
        MockProgress progress = new MockProgress();

        Lookup ctx = Lookups.fixed(fo, p, progress);
        assertTrue("Build is supported", ap.isActionEnabled(ActionProvider.COMMAND_BUILD, ctx));
        // Do not run clean action here as it breaks next tests.
        ap.invokeAction(ActionProvider.COMMAND_BUILD, ctx);

        assertTrue("Progress started", progress.started);
        progress.finished.await(600, TimeUnit.SECONDS);
        assertNotNull("Progress finished", progress.success);
    }

    
}
