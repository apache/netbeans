/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.debugger;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Test of the Properties class
 * 
 * @author Martin Entlicher
 */
public class PropertiesTest extends TestCase {
    
    public PropertiesTest(String testName) {
        super(testName);
    }
    
    /** Tests just the basic get/set methods. */
    public void testGetSet() throws Exception {
        String prop = "get/set";
        Properties p = Properties.getDefault();
        for (int i = 0; i < 10; i++) {
            int j = 0;
            String app = Integer.toHexString(j);
            testGetSet(p, prop + i + app, true);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (byte) i);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (short) i);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (int) 100*i - 2000);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (long) 1000000000000000L*i - 12345678987654321L);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (float) (1234.1234*i - 5678));
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (double) (1234.1234e200*i*Math.sin(i)));
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, Integer.toBinaryString(i*1234));
            app = Integer.toHexString(++j);
            //testGetSet(p, prop + i, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
            app = Integer.toHexString(++j);
            //testGetSet(p, prop + i, new double[] { 0.1, 1.2, 2.3, 3.4, 4.5, 5.6, 6.7, 7.8, 8.9, 9.1 });
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" });
            app = Integer.toHexString(++j);
            //testGetSet(p, prop + i + app, new String[][] { { "0", "1" }, { "2", "3" }, { "4", "5", "6" }, { "7", "8", "9" } });
            //app = Integer.toHexString(++j);
            //testGetSet(p, prop + i + app, new Rectangle[] { new Rectangle(), new Rectangle(10, 20), new Rectangle(234, -432) });
            //app = Integer.toHexString(++j);
            //testGetSet(p, prop + i + app, new ArrayList(Arrays.asList(new Integer[] { new Integer(i) })));
            testGetSet(p, prop + i + app, new ArrayList(Arrays.asList(new String[] { Integer.toString(i) })));
            //testGetSet(p, prop + i + app, Collections.singleton(new Integer(i)));
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, new ArrayList(Arrays.asList(new Object[] { Integer.toHexString(i), "String "+i })));
            //testGetSet(p, prop + i + app, Arrays.asList(new Object[] { Integer.toHexString(i), "String "+i }));
            //testGetSet(p, prop + i + app, Arrays.asList(new Object[] { new Integer(i), "String "+i }));
            app = Integer.toHexString(++j);
            //testGetSet(p, prop + i + app, Collections.singletonMap(new Integer(i), "i = "+i));
            testGetSet(p, prop + i + app, new HashMap(Collections.singletonMap(Integer.toString(i), "i = "+i)));
        }
    }
    
    public void testReader() throws Exception {
        Properties p = Properties.getDefault();
        assertNull(p.getObject("rect 1", null));
        assertNull(p.getObject("rect 2", null));
        assertNull(p.getObject("test 1", null));
        assertNull(p.getObject("test 2", null));
        assertNull(p.getObject("test 3", null));
        Rectangle r1 = new Rectangle(9876, 1234);
        Rectangle r2 = new Rectangle(987654321, 123456789);
        p.setObject("rect 1", r1);
        p.setObject("rect 2", r2);
        TestObject t1 = new TestObject(12345678);
        TestObject t2 = new TestObject(999999999965490L);
        TestObject t3 = new TestObject(999999999999999999L);
        p.setObject("test 1", t1);
        p.setObject("test 2", t2);
        p.setObject("test 3", t3);
        
        assertEquals(r1, p.getObject("rect 1", null));
        assertEquals(r2, p.getObject("rect 2", null));
        assertEquals(t1, p.getObject("test 1", null));
        assertEquals(t2, p.getObject("test 2", null));
        assertEquals(t3, p.getObject("test 3", null));
    }

    public void testInitializer() throws Exception {
        Properties p = Properties.getDefault();
        p = p.getProperties("test");

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.array"), p.getArray("array", null));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.boolean"), p.getBoolean("boolean", true));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.boolean"), p.getBoolean("boolean", false));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.byte"), p.getByte("byte", (byte) 0));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.byte"), p.getByte("byte", (byte) -1));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.char"), p.getChar("char", (char) 0));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.char"), p.getChar("char", 'a'));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.collection"), p.getCollection("collection", null));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.set"), p.getCollection("set", null));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.list"), p.getCollection("list", null));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.double"), p.getDouble("double", 0.0));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.double"), p.getDouble("double", 1.0));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.float"), p.getFloat("float", 0.0f));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.float"), p.getFloat("float", 1.0f));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.int"), p.getInt("int", 0));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.int"), p.getInt("int", 1));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.long"), p.getLong("long", 0l));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.long"), p.getLong("long", 1234567890123456789l));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.map"), p.getMap("map", null));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.linkedhashmap"), p.getMap("linkedhashmap", null));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.object"), p.getObject("object", null));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.short"), p.getShort("short", (short) 0));
        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.short"), p.getShort("short", (short) 1));

        assertEquals(TestInitializer.DEFAULT_VALUES.get("test.string"), p.getString("string", null));
    }

    public void testListeners() throws Exception {
        // First test that the properties can be collected:
        Properties p = Properties.getDefault();
        p = p.getProperties("listening");
        WeakReference<? extends Properties> pRef = new WeakReference<>(p);
        p = null;
        System.gc();
        NbTestCase.assertGC("The Properties are not collected.", pRef);
        //System.err.println("testListeners(): Properties can be collected O.K.");

        // Then attach a listener and test that they are not collected:
        p = Properties.getDefault().getProperties("listening");
        final PropertyChangeEvent[] evtRef = new PropertyChangeEvent[] { null };
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                evtRef[0] = evt;
            }
        };
        p.addPropertyChangeListener(l);
        pRef = new WeakReference(p);
        p = null;
        System.gc();
        boolean collected;
        try {
            NbTestCase.assertGC("The Properties are not collected.", pRef);
            collected = true;
        } catch (AssertionError ae) {
            collected = false;
        }
        assertFalse("Properties were collected even when we hold a listener!", collected);
        //System.err.println("testListeners(): Properties were not collected with a listener O.K.");

        // The properties we listen on still live...
        p = Properties.getDefault().getProperties("listening");
        p.setDouble("double", Double.NEGATIVE_INFINITY);
        assertNotNull("Property change was not received.", evtRef[0]);
        assertEquals("double", evtRef[0].getPropertyName());
        assertEquals(Double.NEGATIVE_INFINITY, evtRef[0].getNewValue());
        //System.err.println("testListeners(): Properties event O.K.");

        pRef.get().removePropertyChangeListener(l);
        p = null;
        System.gc();
        try {
            NbTestCase.assertGC("The Properties are not collected after remove of listener.", pRef);
        } catch (AssertionError ae) {
            // TODO: File a defect for NbTestCase
            if (!ae.getMessage().endsWith("Not found!!!")) {
                throw ae;
            }
        }
    }

    /** Stress test of multi-threaded get/set */
    public void testStressGetSet() throws Exception {
        Properties p = Properties.getDefault();
        int n = 5;
        ConcurrentGetSet[] cgs = new ConcurrentGetSet[n];
        Thread[] t = new Thread[n];
        for (int i = 0; i < n; i++) {
            cgs[i] = new ConcurrentGetSet(p, "CGS "+i);
            t[i] = new Thread(cgs[i]);
            t[i].start();
        }
        for (int i = 0; i < n; i++) {
            t[i].join();
            if (cgs[i].getException() != null) {
                throw cgs[i].getException();
            }
        }
    }
    
    private static class ConcurrentGetSet implements Runnable {
        
        private Properties p;
        private String prop;
        private TestObject[] t;
        private Exception ex;
        
        public ConcurrentGetSet(Properties p, String prop) {
            this.p = p;
            this.prop = prop;
            t = new TestObject[3];
            t[0] = new TestObject(12345678);
            t[1] = new TestObject(999999999965490L);
            t[2] = new TestObject(999999999999999999L);
        }
    
        public void run() {
            cycle: for (int i = 0; i < 10000; i++) {
                for (int k = 0; k < t.length; k++) {
                    p.setObject(prop+k, t[k]);
                }
                try {
                    if ((i % 1000) == 0) {
                        Thread.currentThread().sleep(1 + (prop.hashCode() % 10));
                    }
                } catch (InterruptedException iex) {}
                for (int k = 0; k < t.length; k++) {
                    try {
                        assertEquals(t[k], p.getObject(prop+k, null));
                    } catch (Exception ex) {
                        this.ex = ex;
                        break cycle;
                    }
                }
            }
        }
        
        public Exception getException() {
            return ex;
        }
    }
    
    private void testGetSet(Properties p, String name, boolean obj) {
        // suppose that the property is not defined
        assertEquals(true, p.getBoolean(name, true));
        assertEquals(false, p.getBoolean(name, false));
        // check set/get:
        p.setBoolean(name, obj);
        assertEquals(obj, p.getBoolean(name, !obj));
    }
    
    private void testGetSet(Properties p, String name, byte obj) {
        // suppose that the property is not defined
        assertEquals((byte) 10, p.getByte(name, (byte) 10));
        assertEquals((byte) 20, p.getByte(name, (byte) 20));
        // check set/get:
        p.setByte(name, obj);
        assertEquals(obj, p.getByte(name, (byte) 0));
    }
    
    private void testGetSet(Properties p, String name, short obj) {
        // suppose that the property is not defined
        assertEquals((short) 10, p.getShort(name, (short) 10));
        assertEquals((short) 20, p.getShort(name, (short) 20));
        // check set/get:
        p.setShort(name, obj);
        assertEquals(obj, p.getShort(name, (short) 0));
    }
    
    private void testGetSet(Properties p, String name, int obj) {
        // suppose that the property is not defined
        assertEquals((int) 10, p.getInt(name, (int) 10));
        assertEquals((int) 20, p.getInt(name, (int) 20));
        // check set/get:
        p.setInt(name, obj);
        assertEquals(obj, p.getInt(name, (int) 0));
    }
    
    private void testGetSet(Properties p, String name, long obj) {
        // suppose that the property is not defined
        assertEquals((long) 10, p.getLong(name, (long) 10));
        assertEquals((long) 20, p.getLong(name, (long) 20));
        // check set/get:
        p.setLong(name, obj);
        assertEquals(obj, p.getLong(name, (long) 0));
    }
    
    private void testGetSet(Properties p, String name, float obj) {
        // suppose that the property is not defined
        assertEquals((float) 10, p.getFloat(name, (float) 10), 0);
        assertEquals((float) 20, p.getFloat(name, (float) 20), 0);
        // check set/get:
        p.setFloat(name, obj);
        assertEquals(obj, p.getFloat(name, (float) 0), 0);
    }
    
    private void testGetSet(Properties p, String name, double obj) {
        // suppose that the property is not defined
        assertEquals((double) 10, p.getDouble(name, (double) 10), 0);
        assertEquals((double) 20, p.getDouble(name, (double) 20), 0);
        // check set/get:
        p.setDouble(name, obj);
        assertEquals(obj, p.getDouble(name, (double) 0), 0);
    }
    
    private void testGetSet(Properties p, String name, String obj) {
        // suppose that the property is not defined
        assertNull(p.getString(name, null));
        assertEquals("10", p.getString(name, "10"));
        assertEquals("20", p.getString(name, "20"));
        // check set/get:
        p.setString(name, obj);
        assertEquals(obj, p.getString(name, ""));
    }
    
    private void testGetSet(Properties p, String name, Object[] obj) {
        // suppose that the property is not defined
        assertNull(p.getArray(name, null));
        assertTrue(Arrays.deepEquals(new String[] { "10" }, p.getArray(name, new String[] { "10" })));
        assertTrue(Arrays.deepEquals(new String[] { "20" }, p.getArray(name, new String[] { "20" })));
        // check set/get:
        p.setArray(name, obj);
        //assertEquals(obj, p.getArray(name, new Object[]{}));
        Object[] ret = p.getArray(name, new Object[]{});
        assertTrue("Expecting: "+Arrays.asList(obj)+"\nbut got: "+Arrays.asList(ret), Arrays.deepEquals(obj, ret));
    }
    
    private void testGetSet(Properties p, String name, Collection obj) {
        // suppose that the property is not defined
        assertNull(p.getCollection(name, null));
        assertEquals(Collections.singleton("10"), p.getCollection(name, Collections.singleton("10")));
        assertEquals(Collections.singletonList("20"), p.getCollection(name, Collections.singletonList("20")));
        // check set/get:
        p.setCollection(name, obj);
        assertEquals(obj, p.getCollection(name, Collections.emptySet()));
    }
    
    private void testGetSet(Properties p, String name, Map obj) {
        // suppose that the property is not defined
        assertNull(p.getMap(name, null));
        assertEquals(Collections.singletonMap("10", "20"), p.getMap(name, Collections.singletonMap("10", "20")));
        // check set/get:
        p.setMap(name, obj);
        assertEquals(obj, p.getMap(name, Collections.emptyMap()));
    }
    
    private void testGetSet(Properties p, String name, Object obj) {
        // suppose that the property is not defined
        assertNull(p.getObject(name, null));
        assertEquals("10", p.getObject(name, "10"));
        assertEquals("20", p.getObject(name, "20"));
        // check set/get:
        p.setObject(name, obj);
        assertEquals(obj, p.getObject(name, ""));
    }

    @DebuggerServiceRegistration(types={Properties.Reader.class})
    public static class TestReader implements Properties.Reader {
        
    
        public String[] getSupportedClassNames() {
            return new String[] { "java.awt.Rectangle", "org.netbeans.api.debugger.PropertiesTest$TestObject" };
        }

        public Object read(String className, Properties properties) {
            if (className.equals("java.awt.Rectangle")) {
                int w = properties.getInt("Rectangle.width", 0);
                int h = properties.getInt("Rectangle.height", 0);
                return new Rectangle(w, h);
            }
            if (className.equals("org.netbeans.api.debugger.PropertiesTest$TestObject")) {
                return new TestObject(properties);
            }
            throw new IllegalArgumentException(className);
        }

        public void write(Object object, Properties properties) {
            if (object instanceof Rectangle) {
                properties.setInt("Rectangle.width", ((Rectangle) object).width);
                properties.setInt("Rectangle.height", ((Rectangle) object).height);
                return ;
            }
            if (object instanceof TestObject) {
                ((TestObject) object).write(properties);
                return ;
            }
            throw new IllegalArgumentException(object.toString());
        }
    }

    @DebuggerServiceRegistration(types={Properties.Initializer.class})
    public static class TestInitializer implements Properties.Initializer {

        public static final String[] PROPERTY_NAMES = new String[] {
            "test.array",
            "test.boolean",
            "test.byte",
            "test.char",
            "test.collection",
            "test.set",
            "test.list",
            "test.double",
            "test.float",
            "test.int",
            "test.long",
            "test.map",
            "test.linkedhashmap",
            "test.object",
            "test.short",
            "test.string",
        };

        public static final Map<String, Object> DEFAULT_VALUES = new HashMap<String, Object>();

        static {
            DEFAULT_VALUES.put(PROPERTY_NAMES[0], new String[] {"TestArray"});
            DEFAULT_VALUES.put(PROPERTY_NAMES[1], Boolean.TRUE);
            DEFAULT_VALUES.put(PROPERTY_NAMES[2], Byte.MAX_VALUE);
            DEFAULT_VALUES.put(PROPERTY_NAMES[3], Character.MIN_SURROGATE);
            DEFAULT_VALUES.put(PROPERTY_NAMES[4], Collections.unmodifiableCollection(Collections.singleton(DEFAULT_VALUES)));
            DEFAULT_VALUES.put(PROPERTY_NAMES[5], Collections.singleton(PROPERTY_NAMES));
            DEFAULT_VALUES.put(PROPERTY_NAMES[6], Collections.singletonList("sl"));
            DEFAULT_VALUES.put(PROPERTY_NAMES[7], Double.NaN);
            DEFAULT_VALUES.put(PROPERTY_NAMES[8], Float.NEGATIVE_INFINITY);
            DEFAULT_VALUES.put(PROPERTY_NAMES[9], Integer.SIZE);
            DEFAULT_VALUES.put(PROPERTY_NAMES[10], Long.reverse(Long.SIZE));
            DEFAULT_VALUES.put(PROPERTY_NAMES[11], Collections.singletonMap("key", "value"));
            DEFAULT_VALUES.put(PROPERTY_NAMES[12], new LinkedHashMap());
            DEFAULT_VALUES.put(PROPERTY_NAMES[13], new Rectangle(10, 10, 20, 20));
            DEFAULT_VALUES.put(PROPERTY_NAMES[14], Short.MIN_VALUE);
            DEFAULT_VALUES.put(PROPERTY_NAMES[15], String.class.getName());
        }

        public String[] getSupportedPropertyNames() {
            return PROPERTY_NAMES;
        }

        public Object getDefaultPropertyValue(String propertyName) {
            return DEFAULT_VALUES.get(propertyName);
        }
        
    }
    
    private static class TestObject {
        
        private boolean boo;
        private char c;
        private int l;
        private String[] strings;
        private Rectangle r;
        private String binaryState;
        
        public TestObject(long state) {
            boo = (state & 1) == 1;
            c = (char) (state & 255);
            l = (int) ((state << 8) & 255);
            strings = new String[l];
            for (int i = 0; i < l; i++) {
                int d = i % 64;
                strings[i] = "arr[i] = "+((state << d) & 1);
            }
            r = new Rectangle((int) (state & 65535), (int) ((state << 16) & 65535));
            binaryState = Long.toBinaryString(state);
        }
        
        public TestObject(Properties p) {
            boo = p.getBoolean("boo", false);
            c = p.getChar("char", (char) 0);
            l = p.getInt("length", 0);
            for (int i = 0; i < l; i++) {
                strings[i] = p.getString("string "+i, null);
            }
            r = (Rectangle) p.getObject("rectangle", null);
            binaryState = p.getString("binaryState", null);
        }
        
        public void write(Properties p) {
            p.setBoolean("boo", boo);
            p.setChar("char", c);
            p.setInt("length", l);
            for (int i = 0; i < l; i++) {
                p.setString("string "+i, strings[i]);
            }
            p.setObject("rectangle", r);
            p.setString("binaryState", binaryState);
        }
        
        public boolean equals(Object obj) {
            if (!(obj instanceof TestObject)) {
                return false;
            }
            TestObject t = (TestObject) obj;
            if (boo != t.boo) return false;
            if (c != t.c) return false;
            if (l != t.l) return false;
            for (int i = 0; i < l; i++) {
                if (!strings[i].equals(t.strings[i])) return false;
            }
            if (!r.equals(t.r)) return false;
            if (!binaryState.equals(t.binaryState)) return false;
            return true;
        }
        
        public int hashCode() {
            return 1234+r.width+r.height;
        }
        
        public String toString() {
            return binaryState;
        }
    }
    
}
