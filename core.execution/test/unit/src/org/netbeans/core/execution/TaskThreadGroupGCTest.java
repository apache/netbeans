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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.execution;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.windows.IOProvider;

/**
 * Test that a task thread group is cleared when it is done.
 * @see "#36395"
 * @author Jesse Glick
 */
public class TaskThreadGroupGCTest extends NbTestCase {

    public TaskThreadGroupGCTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 60000;
    }

    public void testTTGGC() throws Exception {
        IOProvider.getDefault(); // initialize stdio streams to default, else we will get stack overflow later
        final List<Reference<Thread>> t = new ArrayList<Reference<Thread>>();
        Runnable r = new Runnable() {
            public void run() {
                System.out.println("Running a task in the execution engine...");
                t.add(new WeakReference<Thread>(Thread.currentThread()));
                Runnable r1 = new Runnable() {
                    public void run() {
                        System.out.println("Ran second thread.");
                    }
                };
                Thread nue1 = new Thread(r1);
                nue1.start();
                try {
                    nue1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t.add(new WeakReference<Thread>(nue1));
                Runnable r2 = new Runnable() {
                    public void run() {
                        System.out.println("Ran third thread.");
                    }
                };
                Thread nue2 = new Thread(r2);
                nue2.start();
                t.add(new WeakReference<Thread>(nue2));
                Runnable r3 = new Runnable() {
                    public void run() {
                        fail("Should not have even run.");
                    }
                };
                Thread nue3 = new Thread(r3);
                t.add(new WeakReference<Thread>(nue3));
                System.out.println("done.");
            }
        };
        ExecutorTask task = ExecutionEngine.getDefault().execute("foo", r, null);
        assertEquals(0, task.result());
        assertFalse(t.toString(), t.contains(null));
        r = null;
        task = null;
        assertGC("Collected secondary task thread too", t.get(1));
        assertGC("Collected forked task thread too", t.get(2));
        assertGC("Collected unstarted task thread too", t.get(3));
        /*
        Thread main = t.get(0).get();
        if (main != null) {
            assertFalse(main.isAlive());
            main = null;
        }
         */
        assertGC("Collected task thread " + t.get(0).get(), t.get(0));
    }

}
