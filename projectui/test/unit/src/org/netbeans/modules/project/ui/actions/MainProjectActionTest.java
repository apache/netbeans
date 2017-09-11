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
