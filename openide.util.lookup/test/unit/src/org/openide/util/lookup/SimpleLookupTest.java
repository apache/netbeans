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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.openide.util.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 * Tests for class SimpleLookup.
 * @author David Strupl
 */
public class SimpleLookupTest extends NbTestCase {

    public SimpleLookupTest(String testName) {
        super(testName);
    }

    public void testEmptyLookup() {
        assertSize("Lookup.EMPTY should be small", 8, Lookup.EMPTY);
    }

    /**
     * Simple tests testing singleton lookup.
     */
    public void testSingleton() {
        //
        Object orig = new Object();
        Lookup p1 = Lookups.singleton(orig);
        Object obj = p1.lookup(Object.class);
        assertTrue(obj == orig);
        assertNull(p1.lookup(String.class)); 
        assertTrue(orig == p1.lookup(Object.class)); // 2nd time, still the same?
        //
        Lookup p2 = Lookups.singleton("test");
        assertNotNull(p2.lookup(Object.class));
        assertNotNull(p2.lookup(String.class));
        assertNotNull(p2.lookup(java.io.Serializable.class));
    }
    
    public void testEmptyFixed() {
        Lookup l = Lookups.fixed();
        assertSize("Lookups.fixed() for empty list of items should be small", 8, l);
        assertSame(Lookup.EMPTY, l);
    }

    public void testSingleItemFixed() {
        Object o = new Object();
        Lookup l = Lookups.fixed(o);
        assertSize("Lookups.fixed(o) for a single item should be small", 24, l);
    }

    /**
     * Simple tests testing fixed lookup.
     */
    public void testFixed() {
        //
        Object[] orig = new Object[] { new Object(), new Object() };
        Lookup p1 = Lookups.fixed(orig);
        Object obj = p1.lookup(Object.class);
        assertTrue(obj == orig[0] || obj == orig[1]);
        assertNull(p1.lookup(String.class)); 
        //
        String[] s = new String[] { "test1", "test2" };
        Lookup p2 = Lookups.fixed((Object[]) s);
        Object obj2 = p2.lookup(Object.class);
        assertNotNull(obj2);
        if (obj2 != s[0] && obj2 != s[1]) {
            fail("Returned objects are not the originals");
        }
        assertNotNull(p2.lookup(String.class));
        assertNotNull(p2.lookup(java.io.Serializable.class));
        Lookup.Template<String> t = new Lookup.Template<String>(String.class);
        Lookup.Result<String> r = p2.lookup(t);
        Collection<? extends String> all = r.allInstances();
        assertTrue(all.size() == 2);
        for (String o : all) {
            assertTrue("allInstances contains wrong objects", o.equals(s[0]) || o.equals(s[1]));
        }
        
        try {
            Lookups.fixed(new Object[] {null});
            fail("No nulls are allowed");
        } catch (NullPointerException ex) {
            // ok, NPE is what we want
        }        
    }

    public void testFixedSubtypes() {
        class A {}
        class B extends A {}
        Lookup l = Lookups.fixed(new A(), new B());
        assertEquals(1, l.lookupAll(B.class).size());
        assertEquals(2, l.lookupAll(A.class).size());
    }

    /**
     * Simple tests testing converting lookup.
     */
    public void testConverting() {
        //
        String[] orig = new String[] { TestConvertor.TEST1, TestConvertor.TEST2 };
        TestConvertor convertor = new TestConvertor();
        Lookup p1 = Lookups.fixed(orig, convertor);
        assertNull("Converting from String to Integer - it should not find String in result", p1.lookup(String.class));
        assertNotNull(p1.lookup(Integer.class));
        assertNotNull(p1.lookup(Integer.class));
        assertTrue("Convertor should be called only once.", convertor.getNumberOfConvertCalls() == 1); 
        Lookup.Template<Integer> t = new Lookup.Template<Integer>(Integer.class);
        Lookup.Result<Integer> r = p1.lookup(t);
        Collection<? extends Integer> all = r.allInstances();
        assertTrue(all.size() == 2);
        for (int i : all) {
            assertTrue("allInstances contains wrong objects", i == TestConvertor.t1 || i == TestConvertor.t2);
        }
    }
    
    private static class TestConvertor implements InstanceContent.Convertor<String,Integer> {
        static final String TEST1 = "test1";
        static final int t1 = 1;
        static final String TEST2 = "test2";
        static final int t2 = 2;
        
        private int numberOfConvertCalls = 0;
        
        public Integer convert(String obj) {
            numberOfConvertCalls++;
            if (obj.equals(TEST1)) {
                return t1;
            }
            if (obj.equals(TEST2)) {
                return t2;
            }
            throw new IllegalArgumentException();
        }
        
        public String displayName(String obj) {
            return obj;
        }
        
        public String id(String obj) {
            if (obj.equals(TEST1)) {
                return TEST1;
            }
            if (obj.equals(TEST2)) {
                return TEST2;
            }
            return null;
        }
        
        public Class<? extends Integer> type(String obj) {
            return Integer.class;
        }
        
        int getNumberOfConvertCalls() { 
            return numberOfConvertCalls;
        }
    }
    
    public void testLookupItem() {
        SomeInst inst = new SomeInst();
        Lookup.Item item = Lookups.lookupItem(inst, "XYZ");
        
        assertTrue("Wrong instance", item.getInstance() == inst);
        assertTrue("Wrong instance class", item.getType() == inst.getClass());
        assertEquals("Wrong id", "XYZ", item.getId());

        item = Lookups.lookupItem(inst, null);
        assertNotNull("Id must never be null", item.getId());
    }

    public void testLookupItemEquals() {
        SomeInst instA = new SomeInst();
        SomeInst instB = new SomeInst();
        Lookup.Item itemA = Lookups.lookupItem(instA, null);
        Lookup.Item itemB = Lookups.lookupItem(instB, null);
        
        assertTrue("Lookup items shouldn't be equal", !itemA.equals(itemB) && !itemB.equals(itemA));

        itemA = Lookups.lookupItem(instA, null);
        itemB = Lookups.lookupItem(instA, null); // same instance

        assertTrue("Lookup items should be equal", itemA.equals(itemB) && itemB.equals(itemA));
        assertTrue("Lookup items hashcode should be same", itemA.hashCode() == itemB.hashCode());

        itemA = Lookups.lookupItem(new String("VOKURKA"), null);
        itemB = Lookups.lookupItem(new String("VOKURKA"), null);

        assertTrue("Lookup items shouldn't be equal (2)", !itemA.equals(itemB) && !itemB.equals(itemA));
    }
    
    public void testAllClassesIssue42399 () throws Exception {
        Object[] arr = { "Ahoj", new Object () };
        
        Lookup l = Lookups.fixed (arr);
        
        Set<Class<? extends Object>> s = l.lookup(new Lookup.Template<Object>(Object.class)).allClasses();
        
        assertEquals ("Two there", 2, s.size ());
        assertTrue ("Contains Object.class", s.contains (Object.class));
        assertTrue ("Contains string", s.contains (String.class));
        
    }

    public void testLookupItemEarlyInitializationProblem() {
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        LI item = new LI();
        List<AbstractLookup.Pair> pairs1 = new ArrayList<AbstractLookup.Pair>();
        List<AbstractLookup.Pair> pairs2 = new ArrayList<AbstractLookup.Pair>();
        
        assertEquals("Item's instance shouldn't be requested", 0, item.cnt);

        pairs1.add(new ItemPair<Object>(Lookups.<Object>lookupItem(new SomeInst(), null)));
        pairs1.add(new ItemPair<Object>(item));
        pairs1.add(new ItemPair<Object>(Lookups.lookupItem(new Object(), null)));

        pairs2.add(new ItemPair<Object>(item));
        pairs2.add(new ItemPair<Object>(Lookups.lookupItem(new Object(), null)));

        ic.setPairs(pairs1);
        ic.setPairs(pairs2);

        assertEquals("Item's instance shouldn't be requested when added to lookup", 0, item.cnt);
        
        LI item2 = al.lookup(LI.class);
        assertEquals("Item's instance should be requested", 1, item.cnt);
    }

    public void testConvenienceMethods() throws Exception {
        // Just check signatures and basic behavior of #73848.
        Lookup l = Lookups.fixed(new Object[] {1, "hello", 2, "goodbye"});
        Collection<? extends Integer> ints = l.lookupAll(Integer.class);
        assertEquals(Arrays.asList(new Integer[] {1, 2}), new ArrayList<Integer>(ints));
        Lookup.Result<Integer> r = l.lookupResult(Integer.class);
        ints = r.allInstances();
        assertEquals(Arrays.asList(new Integer[] {1, 2}), new ArrayList<Integer>(ints));
    }
    
    private static class SomeInst { }
    
    private static class LI extends Lookup.Item<Object> {

        public long cnt = 0;
        
        public String getDisplayName() {
            return getId();
        }

        public String getId() {
            return getClass() + "@" + hashCode();
        }

        public Object getInstance() {
            cnt++;
            return this;
        }

        public Class<? extends Object> getType() {
            return getClass();
        }
    } // End of LI class

    private static class ItemPair<T> extends AbstractLookup.Pair<T> {
        
        private AbstractLookup.Item<T> item;
        
        public ItemPair(Lookup.Item<T> i) {
            this.item = i;
        }

        protected boolean creatorOf(Object obj) {
            return item.getInstance() == obj;
        }

        public String getDisplayName() {
            return item.getDisplayName ();
        }

        public String getId() {
            return item.getId ();
        }

        public T getInstance() {
            return item.getInstance ();
        }

        public Class<? extends T> getType() {
            return item.getType ();
        }

        protected boolean instanceOf(Class<?> c) {
            return c.isAssignableFrom(getType());
        }

        public @Override boolean equals(Object o) {
            if (o instanceof ItemPair) {
                ItemPair p = (ItemPair)o;
                return item.equals (p.item);
            }
            return false;
        }

        public @Override int hashCode() {
            return item.hashCode ();
        }
    } // end of ItemPair
}
