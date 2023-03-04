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

package org.netbeans.modules.project.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

@SuppressWarnings({"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "SIC_INNER_SHOULD_BE_STATIC_ANON"})
public class MainProjectActionTest extends NbTestCase {

    public MainProjectActionTest(String name) {
        super(name);
    }

    @Override protected boolean runInEQ() {
        return true;
    }

    private FileObject p1, p2;
    private TestSupport.TestProject prj1, prj2;

    @Override protected void setUp() throws Exception {
        System.setProperty("sync.project.execution", "true");
        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        FileObject r = FileUtil.createMemoryFileSystem().getRoot();
        p1 = TestSupport.createTestProject(r, "p1");
        prj1 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(p1);
        p2 = TestSupport.createTestProject(r, "p2");
        prj2 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(p2);
    }

    public void testSeqRun() throws Exception {
        final String CMD = "cmd";
        final List<Integer> invocations = new ArrayList<Integer>();
        class BlockingRun implements ActionProvider {
            final int which;
            final boolean success;
            BlockingRun(int which, boolean success) {
                this.which = which;
                this.success = success;
            }
            @Override public String[] getSupportedActions() {
                return new String[] {CMD};
            }
            @Override public boolean isActionEnabled(String command, Lookup context) {
                return true;
            }
            @Override public void invokeAction(String command, Lookup context) {
                ActionProgress listener = ActionProgress.start(context);
                invocations.add(which);
                listener.finished(success);
            }
        }
        BlockingRun ap1 = new BlockingRun(1, true);
        prj1.setLookup(Lookups.singleton(ap1));
        BlockingRun ap2 = new BlockingRun(2, true);
        prj2.setLookup(Lookups.singleton(ap2));
        LookupSensitiveAction a = new MainProjectAction(CMD, "a", null);
        a.actionPerformed(Lookups.fixed(prj1, prj2));
        assertEquals("[1, 2]", invocations.toString());
        ap2 = new BlockingRun(2, false);
        prj2.setLookup(Lookups.singleton(ap2));
        a.actionPerformed(Lookups.fixed(prj1, prj2));
        assertEquals("[1, 2, 1, 2]", invocations.toString());
        ap1 = new BlockingRun(1, false);
        prj1.setLookup(Lookups.singleton(ap1));
        a.actionPerformed(Lookups.fixed(prj1, prj2));
        assertEquals("[1, 2, 1, 2, 1]", invocations.toString());
    }

    public void testDisableMultiRun() throws Exception {
        final String CMD = "cmd";
        final Semaphore s1 = new Semaphore(0);
        final Semaphore s2 = new Semaphore(0);
        class SlowRun implements ActionProvider {
            @Override public String[] getSupportedActions() {
                return new String[] {CMD};
            }
            @Override public boolean isActionEnabled(String command, Lookup context) {
                return true;
            }
            @Override public void invokeAction(String command, Lookup context) {
                final ActionProgress listener = ActionProgress.start(context);
                new Thread() {
                    @Override public void run() {
                        s1.acquireUninterruptibly();
                        listener.finished(true);
                        s2.release();
                    }
                }.start();
            }
        }
        InstanceContent ic = new InstanceContent();
        Lookup context = new AbstractLookup(ic);
        prj1.setLookup(Lookups.singleton(new SlowRun()));
        LookupSensitiveAction a = new MainProjectAction(CMD, null, "a", null, context);
        assertFalse(a.isEnabled());
        ic.add(prj1);
        a.refresh(context, true);
        assertTrue(a.isEnabled());
        a.actionPerformed(context);
        assertFalse(a.isEnabled());
        s1.release();
        s2.acquireUninterruptibly();
        assertTrue(a.isEnabled());
        a.actionPerformed(context);
        assertFalse(a.isEnabled());
        prj2.setLookup(Lookups.singleton(new SlowRun()));
        ic.remove(prj1);
        ic.add(prj2);
        a.refresh(context, true);
        assertTrue(a.isEnabled());
    }

}
