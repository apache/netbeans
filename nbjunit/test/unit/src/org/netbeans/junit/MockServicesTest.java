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

package org.netbeans.junit;

import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import junit.framework.Test;
import junit.framework.TestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public abstract class MockServicesTest extends TestCase {

    protected MockServicesTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        s.addTestSuite(JreTest.class);
        s.addTestSuite(LookupTest.class);
        return s;
    }

    public interface Choice {
        String value();
    }

    protected abstract <T> Iterator<? extends T> lookup(Class<T> clazz);
    protected abstract void assertChangesFired(int countOfChanges);

    private String getChoice() {
        Iterator<? extends Choice> it = lookup(Choice.class);
        if (it.hasNext()) {
            Choice c = it.next();
            if (it.hasNext()) {
                throw new IllegalStateException("have >1 instance available: " + c + " vs. " + it.next());
            }
            return c.value();
        } else {
            return "default";
        }
    }

    /**
     * Basic test that services are set.
     */
    public void testGetChoice() {
        MockServices.setServices();
        assertChangesFired(1);
        assertEquals("initial value", "default", getChoice());
        MockServices.setServices(MockChoice1.class);
        assertEquals("registered value", "mock1", getChoice());
        assertChangesFired(1);
        MockServices.setServices(MockChoice2.class);
        assertEquals("registered value", "mock2", getChoice());
        assertChangesFired(1);
        MockServices.setServices(MockChoice1.class, MockChoice2.class);
        assertChangesFired(1);
        try {
            getChoice();
            fail("Should not work on >1 choice");
        } catch (IllegalStateException x) {}
    }

    public static final class MockChoice1 implements Choice {
        public MockChoice1() {}
        public String value() {
            return "mock1";
        }
    }

    public static final class MockChoice2 implements Choice {
        public MockChoice2() {}
        public String value() {
            return "mock2";
        }
    }

    /**
     * Check that static registrations in META-INF/services/* continue to be
     * available as services - but with lower priority than the explicitly
     * registered ones.
     */
    public void testBackgroundServicesStillAvailable() {
        MockServices.setServices();
        Iterator<? extends DummyService> i = lookup(DummyService.class);
        assertTrue("statically registered service available", i.hasNext());
        assertEquals("of correct type", DummyServiceImpl.class, i.next().getClass());
        assertFalse("but no more", i.hasNext());
        MockServices.setServices(DummyServiceImpl2.class);
        i = lookup(DummyService.class);
        assertTrue("custom service registered", i.hasNext());
        assertEquals("before static service", DummyServiceImpl2.class, i.next().getClass());
        assertTrue("then static service", i.hasNext());
        assertEquals("of static type", DummyServiceImpl.class, i.next().getClass());
        assertFalse("and that is all", i.hasNext());
    }

    public static final class DummyServiceImpl2 implements DummyService {}

    /**
     * Ensure that attempts to register classes which are not publicly
     * instantiable fail immediately.
     */
    public void testModifierRestrictions() {
        try {
            MockServices.setServices(MockChoice3.class);
            fail("Should not permit nonpublic class to be registered");
        } catch (IllegalArgumentException x) {/* right */}
        try {
            MockServices.setServices(MockChoice4.class);
            fail("Should not permit class w/o public constructor to be registered");
        } catch (IllegalArgumentException x) {/* right */}
        try {
            MockServices.setServices(MockChoice5.class);
            fail("Should not permit class w/o no-arg constructor to be registered");
        } catch (IllegalArgumentException x) {/* right */}
        try {
            MockServices.setServices(MockChoice6.class);
            fail("Should not permit abstract class to be registered");
        } catch (IllegalArgumentException x) {/* right */}
        try {
            MockServices.setServices(Choice.class);
            fail("Should not permit interface to be registered");
        } catch (IllegalArgumentException x) {/* right */}
    }

    private static final class MockChoice3 implements Choice {
        public MockChoice3() {}
        public String value() {
            return "mock3";
        }
    }

    public static final class MockChoice4 implements Choice {
        MockChoice4() {}
        public String value() {
            return "mock4";
        }
    }

    public static final class MockChoice5 implements Choice {
        public MockChoice5(String v) {}
        public String value() {
            return "mock5";
        }
    }
    
    public static abstract class MockChoice6 implements Choice {}

    /**
     * Check that service registrations are available from all threads,
     * not just the thread calling setServices.
     */
    public void testOtherThreads() throws Exception {
        // Ensure EQ thread exists. This will not be a child of current thread group.
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
        MockServices.setServices(MockChoice1.class);
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                assertEquals("registered value in EQ", "mock1", getChoice());
            }
        });
        // This will be a child of current thread group.
        ExecutorService svc = Executors.newSingleThreadExecutor();
        svc.submit(new Runnable() {
            public void run() {
                assertEquals("registered value in thread pool", "mock1", getChoice());
            }
        }).get();
        MockServices.setServices(MockChoice2.class);
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                assertEquals("new registered value in EQ", "mock2", getChoice());
            }
        });
        svc.submit(new Runnable() {
            public void run() {
                assertEquals("new registered value in thread pool", "mock2", getChoice());
            }
        }).get();
    }

    /**
     * Check that services classes can be registered even if they are not
     * loadable by the class loader which loaded MockServices.class.
     * /
    public void testInstancesFromDerivativeClassLoaders() {
        // XXX currently will throw assertion errors
    }
     */

    public static class JreTest extends MockServicesTest {

        public JreTest(String s) {
            super(s);
        }

        @SuppressWarnings("unchecked") // using reflection
        protected <T> Iterator<? extends T> lookup(Class<T> clazz) {
            try {
                Class serviceLoader = Class.forName("java.util.ServiceLoader");
                Method load = serviceLoader.getMethod("load", Class.class);
                Object loader = load.invoke(null, clazz);
                return ((Iterable) loader).iterator();
            } catch (Exception x1) {
                try {
                    Class service = Class.forName("sun.misc.Service");
                    Method providers = service.getMethod("providers", Class.class);
                    return (Iterator) providers.invoke(null, clazz);
                } catch (Exception x2) {
                    throw (AssertionError) new AssertionError("Neither java.util.ServiceLoader nor sun.misc.Service available").initCause(x1.initCause(x2));
                }
            }
        }

        protected void assertChangesFired(int countOfChanges) {
            // no changes listening supported
        }

    }

    public static class LookupTest extends MockServicesTest 
    implements LookupListener {
        private Lookup.Result<?> res;
        private int cnt;
        
        public LookupTest(String s) {
            super(s);
            res = Lookup.getDefault().lookupResult(Object.class);
            res.addLookupListener(this);
            res.allInstances();
        }

        protected <T> Iterator<? extends T> lookup(Class<T> clazz) {
            return Lookup.getDefault().lookupAll(clazz).iterator();
        }

        protected void assertChangesFired(int countOfChanges) {
            if (countOfChanges <= cnt) {
                cnt = 0;
            } else {
                fail("Not enough changes fired: " + cnt + " expected: " + countOfChanges);
            }
        }

        public void resultChanged(LookupEvent ev) {
            cnt++;
        }

    }

}
