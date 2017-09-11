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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import org.netbeans.junit.NbTestCase;
import org.openide.util.lookup.implspi.ActiveQueue;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ActiveQueueTest extends NbTestCase{

    public ActiveQueueTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 45000;
    }

    public void testMemoryLeak() throws Exception {
        final Class<?> u1 = ActiveQueue.class;
        class L extends URLClassLoader {
            public L() {
                super(new URL[] {u1.getProtectionDomain().getCodeSource().getLocation()}, u1.getClassLoader().getParent());
            }
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(u1.getName()) || name.startsWith(u1.getName() + "$")) {
                    Class c = findLoadedClass(name);
                    if (c == null) {
                        c = findClass(name);
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                } else {
                    return super.loadClass(name, resolve);
                }
            }
        }
        ClassLoader l = new L();
        Class<?> u2 = l.loadClass(u1.getName());
        assertEquals(l, u2.getClassLoader());
        Object obj = new Object();
        @SuppressWarnings("unchecked")
        ReferenceQueue<Object> q = (ReferenceQueue<Object>) u2.getMethod("queue").invoke(null);
        RunnableRef ref = new RunnableRef(obj, q);
        synchronized (ref) {
            obj = null;
            assertGC("Ref should be GC'ed as usual", ref);
            ref.wait();
            assertTrue("Run method has been executed", ref.executed);
        }
        /* We can not expect to have the ActiveQueue GC'ed after fix of issue #256943.
        Reference<?> r = new WeakReference<Object>(u2);
        q = null;
        u2 = null;
        l = null;
        assertGC("#86625: Utilities.class can also be collected now", r);
        */
    }


    private static class RunnableRef extends WeakReference<Object>
    implements Runnable {
        public boolean wait;
        public boolean entered;
        public boolean executed;

        public RunnableRef (Object o) {
            this(o, ActiveQueue.queue());
        }

        public RunnableRef(Object o, ReferenceQueue<Object> q) {
            super(o, q);
        }

        public synchronized void run () {
            entered = true;
            if (wait) {
                // notify we are here
                notify ();
                try {
                    wait ();
                } catch (InterruptedException ex) {
                }
            }
            executed = true;

            notifyAll ();
        }
    }

}