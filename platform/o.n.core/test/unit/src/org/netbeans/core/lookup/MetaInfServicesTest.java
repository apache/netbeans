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

package org.netbeans.core.lookup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.print.PrintServiceLookup;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.ModuleHistory;
import org.netbeans.SetupHid;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/** Test whether modules can really register things in their META-INF/services/class.Name
 * files, and whether this behaves correctly when the modules are disabled/enabled.
 * Note that Plain loads its classpath modules as soon as you ask for it, so these
 * tests do not check what happens on the NetBeans startup classpath.
 * @author Jesse Glick
 */
public class MetaInfServicesTest extends NbTestCase {

    public MetaInfServicesTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    private ModuleManager mgr;
    private Module m1, m2;
    protected @Override void setUp() throws Exception {
        //System.err.println("setUp");
        //Thread.dumpStack();
        clearWorkDir();
        // Load Plain.
        // Make a couple of modules.
        mgr = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    File data = new File(getDataDir(), "lookup");
                    File jars = getWorkDir();
                    File jar1 = SetupHid.createTestJAR(data, jars, "services-jar-1", null);
                    File jar2 = SetupHid.createTestJAR(data, jars, "services-jar-2", null, jar1);
                    m1 = mgr.create(jar1, new ModuleHistory(jar1.getAbsolutePath()), false, false, false);
                    m2 = mgr.create(jar2, new ModuleHistory(jar2.getAbsolutePath()), false, false, false);
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
        assertEquals(Collections.EMPTY_SET, m1.getProblems());
    }
    protected @Override void tearDown() throws Exception {
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    if (m2.isEnabled()) mgr.disable(m2);
                    mgr.delete(m2);
                    if (m1.isEnabled()) mgr.disable(m1);
                    mgr.delete(m1);
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
        m1 = null;
        m2 = null;
        mgr = null;
    }
    protected static final int TWIDDLE_ENABLE = 0;
    protected static final int TWIDDLE_DISABLE = 1;
    protected void twiddle(final Module m, final int action) throws Exception {
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    switch (action) {
                    case TWIDDLE_ENABLE:
                        mgr.enable(m);
                        break;
                    case TWIDDLE_DISABLE:
                        mgr.disable(m);
                        break;
                    default:
                        throw new IllegalArgumentException("bad action: " + action);
                    }
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
    }
    
    /** Fails to work if you have >1 method per class, because setUp gets run more
     * than once (XTest bug I suppose).
     */
    @RandomlyFails // NB-Core-Build #5333: wrong instance (of Implementation1)
    public void testEverything() throws Exception {
        twiddle(m1, TWIDDLE_ENABLE);
        ClassLoader systemClassLoader = Lookup.getDefault().lookup(ClassLoader.class);
        Class<?> xface = systemClassLoader.loadClass("org.foo.Interface");
        Lookup.Result<?> r = Lookup.getDefault().lookupResult(xface);
        List<?> instances = new ArrayList<Object>(r.allInstances());
        // Expect to get Impl1 from first JAR.
        assertEquals(1, instances.size());
        Object instance1 = instances.get(0);
        assertTrue(xface.isInstance(instance1));
        assertEquals("org.foo.impl.Implementation1", instance1.getClass().getName());
        // Expect to have (same) Impl1 + Impl2.
        LookupL l = new LookupL();
        r.addLookupListener(l);
        twiddle(m2, TWIDDLE_ENABLE);
        assertTrue("Turning on a second module with a manifest service fires a lookup change", l.gotSomething());
        instances = new ArrayList<Object>(r.allInstances());
        assertEquals(2, instances.size());
        assertEquals(instance1, instances.get(0));
        assertEquals("org.bar.Implementation2", instances.get(1).getClass().getName());
        // Expect to lose Impl2.
        l.count = 0;
        twiddle(m2, TWIDDLE_DISABLE);
        assertTrue(l.gotSomething());
        instances = new ArrayList<Object>(r.allInstances());
        assertEquals(1, instances.size());
        assertEquals("wrong instance", instance1, instances.get(0));
        // Expect to lose Impl1 too.
        l.count = 0;
        twiddle(m1, TWIDDLE_DISABLE);
        assertTrue(l.gotSomething());
        instances = new ArrayList<Object>(r.allInstances());
        assertEquals(0, instances.size());
        // Expect to not get anything: wrong xface version
        l.count = 0;
        twiddle(m1, TWIDDLE_ENABLE);
        // not really important: assertFalse(l.gotSomething());
        instances = new ArrayList<Object>(r.allInstances());
        /* XXX no longer works now that openide.util tests are in test CP:
        assertEquals(0, instances.size());
        systemClassLoader = Lookup.getDefault().lookup(ClassLoader.class);
        Class<?> xface2 = systemClassLoader.loadClass("org.foo.Interface");
        assertTrue(xface != xface2);
        Lookup.Result<?> r2 = Lookup.getDefault().lookupResult(xface2);
        instances = new ArrayList<Object>(r2.allInstances());
        assertEquals(1, instances.size());
         */
        // Let's also check up on some standard JDK services.
        PrintServiceLookup psl = Lookup.getDefault().lookup(PrintServiceLookup.class);
        assertNotNull("Some META-INF/services/javax.print.PrintServiceLookup was found in " + Lookup.getDefault(), psl);
    }
    
    protected static final class LookupL implements LookupListener {
        public int count = 0;
        public synchronized void resultChanged(LookupEvent ev) {
            count++;
            notifyAll();
        }
        public synchronized boolean gotSomething() throws InterruptedException {
            if (count > 0) return true;
            wait(9999);
            return count > 0;
        }
    }
    
}
