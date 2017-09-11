/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.nodes;

import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Mutex;

/**
 *
 * @author Andrei Badea
 */
public class ChildrenTest extends NbTestCase {

    private static ClassLoader ccl;

    public ChildrenTest(String name) {
        super(name);
    }

    public void testCanCloneChildren() throws CloneNotSupportedException {
        Children ch = new Children.Array();
        Children nch = (Children)ch.clone();
        assertEquals("No nodes", 0, nch.getNodesCount());
    }

    public void testProjectManagerDeadlockDetector() throws Exception {
        boolean ea = false;
        assert ea = true;
        if (!ea) {
            return;
        }
        ccl = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new PMClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            class NoOp implements Runnable {
                public void run() {}
            }
            final int[] called = { 0 };
            Runnable tester = new Runnable() {
                public void run() {
                    try {
                        called[0]++;
                        Children.MUTEX.readAccess(new NoOp());
                        fail();
                    } catch (IllegalStateException e) {
                        // OK.
                    }
                    try {
                        called[0]++;
                        Children.MUTEX.writeAccess(new NoOp());
                        fail();
                    } catch (IllegalStateException e) {
                        // OK.
                    }
                }
            };
            PM.mutex().readAccess(tester);
            PM.mutex().writeAccess(tester);
            assertEquals(4, called[0]);
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
        WeakReference<Mutex> mutexRef = new WeakReference<Mutex>(PM.mutex);
        PM.mutex = null;
        assertGC("Should GC PM.mutex", mutexRef);
    }

    private static final class PMClassLoader extends ClassLoader {

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals("org.netbeans.api.project.ProjectManager")) {
                return PM.class;
            }
            throw new ClassNotFoundException();
        }
    }

    private static final class PM {

        public static Mutex mutex = new Mutex();

        public static Mutex mutex() {
            return mutex;
        }
    }
}
