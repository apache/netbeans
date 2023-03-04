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

package org.openide.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.lookup.implspi.SharedClassObjectBridge;
import org.openide.util.test.MockPropertyChangeListener;

/** Test SharedClassObject singletons: esp. initialization semantics.
 * @author Jesse Glick
 */
public class SharedClassObjectTest extends NbTestCase {
    
    public SharedClassObjectTest(String name) {
        super(name);
    }
    
    public void testSimpleSCO() throws Exception {
        Class<? extends SharedClassObject> c = makeClazz("SimpleSCO");
        assertTrue(c != SimpleSCO.class);
        assertNull("No instance created yet", SharedClassObject.findObject(c, false));
        SharedClassObject o = SharedClassObject.findObject(c, true);
        assertNotNull(o);
        assertEquals("org.openide.util.SharedClassObjectTest$SimpleSCO", o.getClass().getName());
        assertEquals(c, o.getClass());
        assertEquals("has not been initialized", 0, o.getClass().getField("initcount").getInt(o));
        assertNull(o.getProperty("foo"));
        assertEquals("has been initialized", 1, o.getClass().getField("initcount").getInt(o));
        assertNull(o.getProperty("bar"));
        assertEquals("has been initialized just once", 1, o.getClass().getField("initcount").getInt(null));
        Class<? extends SharedClassObject> c2 = makeClazz("SimpleSCO");
        assertTrue("Call to makeClazz created a fresh class", c != c2);
        SharedClassObject o2 = SharedClassObject.findObject(c2, true);
        o2.getProperty("baz");
        assertEquals(1, o2.getClass().getField("initcount").getInt(null));
    }
    
    public void testClearSharedData() throws Exception {
        Class<? extends SharedClassObject> c = makeClazz("DontClearSharedDataSCO");
        SharedClassObject o = SharedClassObject.findObject(c, true);
        o.putProperty("inited", true);
        assertEquals("DCSD has been initialized", true, o.getProperty("inited"));
        Reference<?> r = new WeakReference<Object>(o);
        o = null;
        assertGC("collected SCO instance", r);
        assertNull("findObject(Class,false) gives nothing after running GC + finalization #1", SharedClassObject.findObject(c));
        o = SharedClassObject.findObject(c, true);
        assertEquals("has still been initialized", true, o.getProperty("inited"));
        c = makeClazz("ClearSharedDataSCO");
        o = SharedClassObject.findObject(c, true);
        o.putProperty("inited", true);
        assertEquals("CSD has been initialized", true, o.getProperty("inited"));
        r = new WeakReference<Object>(o);
        o = null;
        assertGC("collected SCO instance", r);
        assertNull("findObject(Class,false) gives nothing after running GC + finalization #2", SharedClassObject.findObject(c));
        o = SharedClassObject.findObject(c, true);
        assertEquals("is no longer initialized", null, o.getProperty("inited"));
        o.putProperty("inited", true);
        assertEquals("has now been initialized again", true, o.getProperty("inited"));
    }
    
    public void testIllegalState() throws Exception {
        Class<? extends SharedClassObject> c = makeClazz("InitErrorSCO");
        SharedClassObject o = SharedClassObject.findObject(c, true);
        assertNotNull(o);
        try {
            o.getProperty("foo");
            fail("should not be able to do anything with it");
        } catch (IllegalStateException ise) {
            // Good.
        }
        try {
            o.getProperty("bar");
            fail("should still not be able to do anything with it");
        } catch (IllegalStateException ise) {
            // Good.
        }
    }
    
    public void testPropertyChanges() throws Exception {
        Class<? extends SharedClassObject> c = makeClazz("PropFirerSCO");
        Method putprop = c.getMethod("putprop", Object.class, Boolean.TYPE);
        Method getprop = c.getMethod("getprop");
        Field count = c.getField("addCount");
        SharedClassObject o = SharedClassObject.findObject(c, true);
        assertNull(getprop.invoke(o));
        assertEquals(0, count.getInt(o));
        MockPropertyChangeListener l = new MockPropertyChangeListener("key");
        o.addPropertyChangeListener(l);
        assertEquals(1, count.getInt(o));
        MockPropertyChangeListener l2 = new MockPropertyChangeListener("key");
        o.addPropertyChangeListener(l2);
        assertEquals(1, count.getInt(o));
        o.removePropertyChangeListener(l2);
        assertEquals(1, count.getInt(o));
        putprop.invoke(o, "something", false);
        l.assertEventCount(0);
        assertEquals("something", getprop.invoke(o));
        putprop.invoke(o, "somethingelse", true);
        l.assertEventCount(1);
        assertEquals("somethingelse", getprop.invoke(o));
        // Check that setting the same val does not fire an additional change (cf. #37769):
        putprop.invoke(o, "somethingelse", true);
        l.assertEventCount(0);
        assertEquals("somethingelse", getprop.invoke(o));
        // Check equals() as well as ==:
        putprop.invoke(o, new String("somethingelse"), true);
        l.assertEventCount(0);
        assertEquals("somethingelse", getprop.invoke(o));
        o.removePropertyChangeListener(l);
        assertEquals(0, count.getInt(o));
        o.addPropertyChangeListener(l);
        assertEquals(1, count.getInt(o));
        o.removePropertyChangeListener(l);
        assertEquals(0, count.getInt(o));
    }
    
    public void testRecursiveInit() throws Exception {
        Class<? extends SharedClassObject> c = makeClazz("RecursiveInitSCO");
        SharedClassObject o = SharedClassObject.findObject(c, true);
        assertEquals(0, c.getField("count").getInt(null));
        o.getProperty("foo");
        assertEquals(1, c.getField("count").getInt(null));
        assertEquals(o, c.getField("INSTANCE").get(null));
    }
    
    public void testAbilityToReadResolveToAnyObject () throws Exception {
        SharedClassObject o = SharedClassObject.findObject (SharedClassObjectWithReadResolve.class, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream ();
        ObjectOutputStream oos = new ObjectOutputStream (os);
        oos.writeObject (o);
        oos.close ();
        
        ObjectInputStream ois = new ObjectInputStream (new ByteArrayInputStream (os.toByteArray()));
        Object result = ois.readObject ();
        ois.close ();
        
        
        assertEquals ("Result should be the string", String.class, result.getClass());
        
    }

    public void testOneInstanceIsLookedUp() throws Exception {
        MockServices.setServices(Instance.class);
        Instance i1 = Lookup.getDefault().lookup(Instance.class);
        assertNotNull("Really found", i1);
        MockServices.setServices();
        Instance i10 = Lookup.getDefault().lookup(Instance.class);
        assertNull("Now it returns null", i10);
        MockServices.setServices(Instance.class);
        Instance i2 = Lookup.getDefault().lookup(Instance.class);
        assertSame("Only one instance is created", i1, i2);
    }

    public void testSharedClassObjectBridge() throws Exception {
        assertEquals(Object.class, SharedClassObjectBridge.newInstance(Object.class).getClass());
        Class<? extends SharedClassObject> c = makeClazz("SimpleSCO");
        SharedClassObject o = SharedClassObjectBridge.newInstance(c);
        assertNotNull(o);
        assertEquals(c, o.getClass());
        assertTrue(o == SharedClassObject.findObject(c, true));
    }

    public static final class Instance extends SharedClassObject {
        public Instance() {
        }
    }
    
    /** Create a fresh Class object from one of this test's inner classes.
     * Produces a new classloader so the class is always fresh.
     */
    private Class<? extends SharedClassObject> makeClazz(String name) throws Exception {
        return Class.forName("org.openide.util.SharedClassObjectTest$" + name, false, new MaskingURLClassLoader()).asSubclass(SharedClassObject.class);
    }
    private static final class MaskingURLClassLoader extends URLClassLoader {
        public MaskingURLClassLoader() {
            super(new URL[] {SharedClassObjectTest.class.getProtectionDomain().getCodeSource().getLocation()},
                  SharedClassObject.class.getClassLoader());
        }

        @Override
        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("org.openide.util.SharedClassObjectTest")) {
                // Do not proxy to parent!
                Class c = findLoadedClass(name);
                if (c != null) return c;
                c = findClass(name);
                if (resolve) resolveClass(c);
                return c;
            } else {
                return super.loadClass(name, resolve);
            }
        }
    }
    
    public static class SimpleSCO extends SharedClassObject {
        public static int initcount = 0;
        private static String firstinit = null;
        protected void initialize() {
            super.initialize();
            initcount++;
            if (initcount > 1) {
                System.err.println("Multiple initializations of SimpleSCO: see http://www.netbeans.org/issues/show_bug.cgi?id=14700");
                System.err.print(firstinit);
                new Throwable("Init #" + initcount + " here").printStackTrace();
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new Throwable("Init #1 here").printStackTrace(new PrintStream(baos));
                firstinit = baos.toString();
                // don't print anything unless there is a problem later
            }
        }
        // Protect against random workings of GC:
        protected boolean clearSharedData() {
            return false;
        }
    }
    
    public static class ClearSharedDataSCO extends SharedClassObject {
        protected boolean clearSharedData() {
            return true;
        }
    }
    
    public static class DontClearSharedDataSCO extends SharedClassObject {
        protected boolean clearSharedData() {
            return false;
        }
    }
    
    // SCO.DataEntry.tryToInitialize in absence of EM will try to print
    // stack trace of unexpected exceptions, so just suppress it
    private static final class QuietException extends NullPointerException {
        public void printStackTrace() {
            // do nothing
        }
    }
    
    public static class InitErrorSCO extends SharedClassObject {
        protected void initialize() {
            throw new QuietException();
        }
    }
    
    public static class PropFirerSCO extends SharedClassObject {
        public int addCount = 0;
        protected void addNotify() {
            super.addNotify();
            addCount++;
        }
        protected void removeNotify() {
            addCount--;
            super.removeNotify();
        }
        public void putprop(Object val, boolean notify) {
            putProperty("key", val, notify);
        }
        public Object getprop() {
            return getProperty("key");
        }
    }
    
    public static class RecursiveInitSCO extends SharedClassObject {
        public static final RecursiveInitSCO INSTANCE = SharedClassObject.findObject(RecursiveInitSCO.class, true);
        public static int count = 0;
        protected void initialize() {
            super.initialize();
            count++;
        }
    }

    public static final class SharedClassObjectWithReadResolve extends SharedClassObject {
        public Object readResolve () throws java.io.ObjectStreamException {
            return "Ahoj";
        }
    }
}
