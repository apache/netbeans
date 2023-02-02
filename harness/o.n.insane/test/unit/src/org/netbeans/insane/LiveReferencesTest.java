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
package org.netbeans.insane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.netbeans.insane.live.LiveReferences;
import org.netbeans.insane.live.Path;
import org.netbeans.insane.scanner.Filter;

public class LiveReferencesTest {

    private final static TestObject testObject = new TestObject(null, "static");

    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_throwsNPE_forNullObjs() {

        try {
            LiveReferences.fromRoots(null, null, null, null);
            fail("NullPoiterException expected.");
        } catch (final NullPointerException e) {
            // good
        }
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsEmptyMap_forEmptyCollection() {

        List<Object> objs = Collections.emptyList();

        Map<Object, Path> result = LiveReferences.fromRoots(objs, null, null, null);

        assertEquals(0, result.size());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsEmptyMap_whenLookingForNull() {

        List<Object> objs = Arrays.asList((String) null);

        Map<Object, Path> result = LiveReferences.fromRoots(objs, null, null, null);

        // this result seems incorrect, an empty or NPE seem more correct
        assertEquals(1, result.size());

        Path path = result.get(null);

        assertNotNull(path.getObject());
        assertEquals("static final java.lang.Object java.util.IdentityHashMap.NULL_KEY->\nnull", path.toString());

        assertNull(path.nextNode().getObject());
        assertNull(path.nextNode().nextNode());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsEmptyMap_forNullRoot() {

        TestObject object1 = new TestObject(null, "object1");
        List<Object> objs = Arrays.asList(object1);
        Set<Object> roots = Collections.singleton(null);

        Map<Object, Path> result = LiveReferences.fromRoots(objs, roots, null, null);

        assertEquals(0, result.size());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsEmptyMap_forLocalVariable() {

        TestObject local = new TestObject(null, "local");
        List<Object> objs = Arrays.asList(local);

        Map<Object, Path> result = LiveReferences.fromRoots(objs, null, null, null);

        assertEquals(0, result.size());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsMap_forStaticVariable() {

        List<Object> objs = Arrays.asList(testObject);

        Map<Object, Path> result = LiveReferences.fromRoots(objs, null, null, null);

        assertEquals(1, result.size());

        Path path = result.get(testObject);

        assertNotNull(path.getObject());
        assertSame(testObject, path.nextNode().getObject());
        assertTrue(path.nextNode().toString().startsWith("org.netbeans.insane.LiveReferencesTest$TestObject"));
        assertNull(path.nextNode().nextNode());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsMap_forThreadLocalObject() {

        ThreadLocal<Object> threadLocal = new ThreadLocal<>();
        Object local = new Object();
        threadLocal.set(local);
        List<Object> objs = Arrays.asList(local);

        try {
            Map<Object, Path> result = LiveReferences.fromRoots(objs, null, null, null);

            assertEquals(1, result.size());

            Path path = result.get(local);

            assertNotNull(path.getObject());
            assertNotNull(path.nextNode().getObject());
            assertNotNull(path.nextNode().nextNode().getObject());
            assertNotNull(path.nextNode().nextNode().nextNode().getObject());
            assertSame(local, path.nextNode().nextNode().nextNode().nextNode().getObject());
            assertNull(path.nextNode().nextNode().nextNode().nextNode().nextNode());
        } finally {
            threadLocal.remove();
        }
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsMap_forObjectGraph() {

        // arrange cyclick graphs
        TestObject object1 = new TestObject(null, "object1");
        TestObject object2 = new TestObject(null, "object2");
        object1.next = object2;
        object2.next = object1;
        TestObject object3 = new TestObject(null, "object3");
        TestObject object4 = new TestObject(null, "object4");
        object3.list.add(object3);
        object2.list.add(object4);
        TestObject object5 = new TestObject(null, "object5");
        object5.next = object5;
        List<Object> objs = Arrays.asList(object1, object2, object4);
        Set<Object> roots = new HashSet<>(Arrays.asList(object5, object1, object3));

        Map<Object, Path> result = LiveReferences.fromRoots(objs, roots, null, null);

        System.err.println(result);

        assertEquals(3, result.size());

        Path path1 = result.get(object1);

        assertSame(roots, path1.getObject());
        assertNotNull(path1.nextNode().getObject());
        assertNotNull(path1.nextNode().nextNode().getObject());
        assertNotNull(path1.nextNode().nextNode().nextNode().getObject());
        assertSame(object1, path1.nextNode().nextNode().nextNode().nextNode().getObject());
        assertNull(path1.nextNode().nextNode().nextNode().nextNode().nextNode());

        Path path2 = result.get(object2);

        assertSame(roots, path2.getObject());
        assertNotNull(path2.nextNode().getObject());
        assertNotNull(path2.nextNode().nextNode().getObject());
        assertNotNull(path2.nextNode().nextNode().nextNode().getObject());
        assertSame(object1, path2.nextNode().nextNode().nextNode().nextNode().getObject());
        assertSame(object2, path2.nextNode().nextNode().nextNode().nextNode().nextNode().getObject());
        assertNull(path2.nextNode().nextNode().nextNode().nextNode().nextNode().nextNode());

        Path path4 = result.get(object4);

        assertSame(roots, path4.getObject());
        assertNotNull(path4.nextNode().getObject());
        assertNotNull(path4.nextNode().nextNode().getObject());
        assertNotNull(path4.nextNode().nextNode().nextNode().getObject());
        assertSame(object1, path4.nextNode().nextNode().nextNode().nextNode().getObject());
        assertSame(object2, path4.nextNode().nextNode().nextNode().nextNode().nextNode().getObject());
        assertSame(object2.list, path4.nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().getObject());
        assertNotNull(path4.nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().getObject());
        assertSame(object4, path4.nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().getObject());
        assertNull(path4.nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().nextNode().nextNode());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsMap_innerNonStaticClass() {

        TestObject object1 = new TestObject(null, "object1");
        List<Object> objs = Arrays.asList(object1);
        Set<Object> roots = Collections.singleton(object1.inner);

        Map<Object, Path> result = LiveReferences.fromRoots(objs, roots, null, null);

        assertEquals(1, result.size());

        Path path1 = result.get(object1);

        assertSame(roots, path1.getObject());
        assertSame(object1.inner, path1.nextNode().getObject());
        assertSame(object1, path1.nextNode().nextNode().getObject());
        assertNull(path1.nextNode().nextNode().nextNode());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsMap_forDefaultAndGivenRoots() {

        // arrange cyclick graphs
        TestObject object1 = new TestObject(null, "object1");
        TestObject object2 = new TestObject(null, "object2");
        object1.next = object2;
        List<Object> objs = Arrays.asList(object2, testObject);
        Set<Object> roots = Collections.singleton(object1);

        Map<Object, Path> result = LiveReferences.fromRoots(objs, roots, null, null);

        assertEquals(2, result.size());

        Path path1 = result.get(object2);

        assertSame(roots, path1.getObject());
        assertSame(object1, path1.nextNode().getObject());
        assertSame(object2, path1.nextNode().nextNode().getObject());
        assertNull(path1.nextNode().nextNode().nextNode());

        Path path2 = result.get(testObject);

        assertNotNull(path2.getObject());
        assertSame(testObject, path2.nextNode().getObject());
        assertTrue(path2.nextNode().toString().startsWith("org.netbeans.insane.LiveReferencesTest$TestObject"));
        assertNull(path2.nextNode().nextNode());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsMap_objectBeingRoot() {

        TestObject object1 = new TestObject(null, "object1");
        List<Object> objs = Arrays.asList(object1);
        Set<Object> roots = Collections.singleton(object1);

        Map<Object, Path> result = LiveReferences.fromRoots(objs, roots, null, null);

        assertEquals(1, result.size());

        Path path1 = result.get(object1);

        assertSame(roots, path1.getObject());
        assertSame(object1, path1.nextNode().getObject());
        assertNull(path1.nextNode().nextNode());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsEmptyMap_ifObjectIsFiltereOut() {

        TestObject object1 = new TestObject(null, "object1");
        List<Object> objs = Arrays.asList(object1);
        Set<Object> roots = Collections.singleton(object1);
        Filter filter = (obj, referredFrom, reference) -> obj != object1;

        Map<Object, Path> result = LiveReferences.fromRoots(objs, roots, null, filter);

        assertEquals(0, result.size());
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_reportsProgress() {

        List<Object> objects = Stream.generate(Object::new).limit(100).
              collect(Collectors.toList());
        Object last = objects.get(objects.size() - 1);
        List<Object> objs = Arrays.asList(last);
        Set<Object> roots = Collections.singleton(objects);
        FakeProgressModel progress = new FakeProgressModel();

        LiveReferences.fromRoots(objs, roots, progress, null);

        assertEquals(1, progress.setRangePropertiesArgs.size());
        assertEquals(0, progress.setRangePropertiesArgs.get(0).value);
        assertEquals(0, progress.setRangePropertiesArgs.get(0).min);

        progress.assertThatLastProgressIsEqualToRangeMax();
        progress.assertThatPrgressValuesAreGrowing();
    }
    //--------------------------------------------------------------------------
    @Test
    public void fromRoots4Arg_returnsNull_whencCanceled() {

        List<Object> objects = Stream.generate(Object::new).limit(100).
              collect(Collectors.toList());
        Object last = objects.get(objects.size() - 1);
        List<Object> objs = Arrays.asList(last);
        Set<Object> roots = Collections.singleton(objects);
        FakeProgressModel progress = new FakeProgressModel();
        progress.setValueThrowsCancelException = true;

        Map<Object, Path> result = LiveReferences.fromRoots(objs, roots, progress, null);

        assertNull(result);
    }
    //--------------------------------------------------------------------------
    @Test
    public void pathEqualityWorks() {

        TestObject object1 = new TestObject(null, "object1");
        List<Object> objs = Arrays.asList(object1);
        Set<Object> roots = Collections.singleton(object1);

        Map<Object, Path> result = LiveReferences.fromRoots(objs, roots, null, null);

        assertEquals(1, result.size());

        Path path1 = result.get(object1);

        assertTrue(path1.equals(path1));
        assertFalse(path1.equals(path1.nextNode()));
    }
    //--------------------------------------------------------------------------
    private static class TestObject {

        TestObject next;
        List<TestObject> list = new ArrayList<>();
        String name;
        Inner inner;
        //----------------------------------------------------------------------
        public TestObject(TestObject next, String name) {

            this.next = next;
            this.name = name;
            this.inner = new Inner();
        }
        //----------------------------------------------------------------------
        @Override
        public String toString() {

            return this.name;
        }
        //----------------------------------------------------------------------
        class Inner {
        }
    }
}
