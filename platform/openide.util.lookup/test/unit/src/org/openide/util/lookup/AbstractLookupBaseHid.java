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

package org.openide.util.lookup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

@SuppressWarnings("unchecked") // XXX ought to be corrected, just a lot of them
public class AbstractLookupBaseHid extends NbTestCase {
    private static AbstractLookupBaseHid running;
    private Logger LOG;

    /** instance content to work with */
    InstanceContent ic;
    /** the lookup to work on */
    protected Lookup instanceLookup;
    /** the lookup created to work with */
     Lookup lookup;
    /** implementation of methods that can influence the behaviour */
    Impl impl;
    
    protected AbstractLookupBaseHid(String testName, Impl impl) {
        super(testName);
        if (impl == null && (this instanceof Impl)) {
            impl = (Impl)this;
        }
        this.impl = impl;
    }
    
    protected @Override void setUp() {
        LOG = Logger.getLogger("test." + getName());
        
        this.ic = new InstanceContent ();
        
        beforeActualTest(getName());
        
        this.instanceLookup = createInstancesLookup (ic);
        this.lookup = createLookup (instanceLookup);
        running = this;
    }        
    
    protected @Override void tearDown() {
        running = null;
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    /** The methods to influence test behaviour */
    public static interface Impl {
        /** Creates the initial abstract lookup.
         */
        public Lookup createInstancesLookup (InstanceContent ic);
        /** Creates an lookup for given lookup. This class just returns 
         * the object passed in, but subclasses can be different.
         * @param lookup in lookup
         * @return a lookup to use
         */
        public Lookup createLookup (Lookup lookup);
        
        /** If the impl has any caches that would prevent the system
         * to not garbage collect correctly, then clear them now.
         */
        public void clearCaches ();
    }
    
    private Lookup createInstancesLookup (InstanceContent ic) {
        return impl.createInstancesLookup (ic);
    }
    
    private Lookup createLookup (Lookup lookup) {
        return impl.createLookup (lookup);
    }
    
    Lookup createMultiLookup(Lookup... all) {
        if (all.length == 1) {
            return createMultiLookup(all[0]);
        } 
        Lookup[] clone = all.clone();
        for (int i = 0; i < clone.length; i++) {
            clone[i] = createLookup(clone[i]);
        }
        return new ProxyLookup(clone);
    }
    
    /** instances that we register */
    private static Object[] INSTANCES = new Object[] {
        new Integer (10), 
        new Object ()
    };
    
    public void testReplaceByEqualInstance() {
        Lookup.Result<Integer> res = lookup.lookupResult(Integer.class);
        Listener listener = new Listener();
        res.addLookupListener(listener);
        
        final Integer i1 = Integer.valueOf(432432);
        ic.add(i1);
        
        assertSame("i1 is returned", i1, lookup.lookup(Integer.class));
        assertTrue("One change", listener.listenerCalled);
        listener.listenerCalled = false;
        
        Integer i2 = new Integer(i1);
        assertNotSame(i2, i1);
        
        ic.add(i2);
        assertSame("new instance is in the lookup", i2, lookup.lookup(Integer.class));
        assertFalse("No additional change notified", listener.listenerCalled);
    }
    

    public void testSlowIterate() {
        InstanceContent ic1 = new InstanceContent();
        Lookup a1 = createInstancesLookup(ic1);
        InstanceContent ic2 = new InstanceContent();
        Lookup a2 = createInstancesLookup(ic2);
        
        
        ic1.add(1);
        ic1.add(2);
        ic2.add(3);
        ic2.add(4);
        
        Lookup all = createMultiLookup(a1, a2);
        Lookup.Result<Integer> res = all.lookupResult(Integer.class);
        
        
        final Collection<? extends Integer> fourCol = res.allInstances();
        Iterator<? extends Integer> four = fourCol.iterator();
        assertEquals("Four1", Integer.valueOf(1), four.next());
        assertEquals("Four2", Integer.valueOf(2), four.next());
        
        ic1.remove(1);

        final Collection<? extends Integer> threeCol = res.allInstances();
        Iterator<? extends Integer> three = threeCol.iterator();
        assertEquals("Three2", Integer.valueOf(2), three.next());
        assertEquals("Three3", Integer.valueOf(3), three.next());
        assertEquals("Three4", Integer.valueOf(4), three.next());
        assertFalse("Three ales", three.hasNext());

        assertEquals("This call computes the whole collection"
            + "and may be tempted to store the result in caches."
            + "But it should not, as meanwhile the result has been"
            + "modified", 
            "[1, 2, 3, 4]", new ArrayList<Integer>(fourCol).toString());
        
        assertEquals("Four3",  Integer.valueOf(3), four.next());
        assertEquals("Four4",  Integer.valueOf(4), four.next());
        assertFalse("Ales four", four.hasNext());
        
        Collection<? extends Integer> atTheEnd = res.allInstances();
        assertEquals("Three: " + atTheEnd, 3, atTheEnd.size());
    }

    public void testResultsAreCached() {
        Lookup.Result<Runnable> r1 = lookup.lookupResult(Runnable.class);
        Lookup.Result<Runnable> r2 = lookup.lookupResult(Runnable.class);
        assertSame("The result is cached", r1, r2);
    }
    
    public void testCompareUsingEquals() {
        Integer i1 = new Integer(10);
        Integer i2 = new Integer(10);
        assertEquals(i1, i2);
        assertNotSame(i1, i2);
        
        ic.add(i1);
        
        Template<Integer> t1 = new Lookup.Template<Integer>(Integer.class, null, i1);
        Template<Integer> t2 = new Lookup.Template<Integer>(Integer.class, null, i2);
        
        Lookup.Result<Integer> r2 = lookup.lookup(t2);
        assertEquals("One item", 1, r2.allInstances().size());
        assertEquals(i1, r2.allInstances().iterator().next());
        
        Lookup.Result<Integer> r1 = lookup.lookup(t1);
        
        assertEquals("One item", 1, r1.allInstances().size());
        assertEquals(i1, r1.allInstances().iterator().next());
    }
    
    public void testAddFirstWithExecutorBeforeLookupAssociationFails() {
        doAddFirstWithExecutorBeforeLookupAssociationFails(false);
    }

    private void doAddFirstWithExecutorBeforeLookupAssociationFails(boolean before) {
        ScheduledExecutorService e = Executors.newSingleThreadScheduledExecutor();
        // The next line is replacement for: ic = new AbstractLookup.Content(e);
        ic.attachExecutor(e);
        String msg = "Adding a pair to Content not connected to Lookup is not supported!";
        try {
            ic.addPair(new AbstractLookupMemoryTest.EmptyPair());
            if (before) {
                fail(msg);
            }

        } catch (NullPointerException ex) {
            if (before) {
                assertEquals("OK message", msg, ex.getMessage());
                // OK
                return;
            }
            throw ex;
        }
    }
    
    public void testBeforeLookupIsCalledToInvalidateCaches() {
        class Before extends ProxyLookup {
            Object toAdd;
            
            Before() {
                super(instanceLookup);
            }

            @Override
            protected void beforeLookup(Template<?> template) {
                if (toAdd != null) {
                    ic.add(toAdd);
                    toAdd = null;
                }
            }
            
        }
        Before before = new Before();
        
        Lookup query = createLookup(before);
        
        before.toAdd = Integer.valueOf(10);
        
        Lookup.Result<Long> res = query.lookupResult(Long.class);
        assertTrue("empty", res.allItems().isEmpty());
        
        assertNull("beforeLookup called", before.toAdd);
        
        before.toAdd = Long.valueOf(3L);
        
        Collection<? extends Lookup.Item<Long>> c = res.allItems();
        assertEquals("There is One: ", 1, c.size());
        
        assertEquals(Long.valueOf(3L), c.iterator().next().getInstance());
    }
    
    
    public void testProxyOverProxyLocks() throws Exception {
        final Collection[] noLocks = { null, null };
        
        
        class IntPair extends AbstractLookup.Pair<Integer> {
            Integer value = 1;
            
            @Override
            protected boolean instanceOf(Class<?> c) {
                return c.isAssignableFrom(Integer.class);
            }

            @Override
            protected boolean creatorOf(Object obj) {
                return obj == value;
            }

            @Override
            public Integer getInstance() {
                assertNoLocks();
                return value;
            }

            @Override
            public Class<? extends Integer> getType() {
                return Integer.class;
            }

            @Override
            public String getId() {
                return value.toString();
            }

            @Override
            public String getDisplayName() {
                return value.toString();
            }
            
            public void assertNoLocks() {
                for (Object o : noLocks) {
                    if (o == null) {
                        continue;
                    }
                    assertFalse("Don't hold lock", Thread.holdsLock(o));
                }
            }
        }
        
        ic.addPair(new IntPair());
        
        noLocks[0] = lookup.lookupAll(Integer.class);
        
        assertFalse("Not empty", noLocks[0].isEmpty());
        
        assertEquals("One", 1, noLocks[0].size());
    }
    
    /** Test if first is really first.
     */
    public void testFirst () {
        Integer i1 = 1;
        Integer i2 = 2;
        
        ic.add (i1);
        ic.add (i2);
        
        Integer found = lookup.lookup(Integer.class);
        if (found != i1) {
            fail ("First object is not first: " + found + " != " + i1);
        }
        
        List<Integer> list = new ArrayList<Integer>();
        list.add (i2);
        list.add (i1);
        ic.set (list, null);
        
        found = lookup.lookup (Integer.class);
        if (found != i2) {
            fail ("Second object is not first after reorder: " + found + " != " + i2);
        }
        
    }
    
    public void testToString() {
        String txt = lookup.toString();
        assertNotNull("Something is there", txt);
        assertTrue("Something2: " + txt, txt.length() > 0);
    }


    /** Tests ordering of items in the lookup.
    */
    public void testOrder () {
        addInstances (INSTANCES);

        if (INSTANCES[0] != lookup.lookup (INSTANCES[0].getClass ())) {
            fail ("First object in intances not found");
        }

        Iterator<?> all = lookup.lookupAll(Object.class).iterator();
        checkIterator ("Difference between instances added and found", all, Arrays.asList (INSTANCES));
    }
    
    public void testLookupListenerRemoved() {
        class C0 {
        }
        class C1 {
        }

        Lookup.Result<C0> r0 = lookup.lookupResult(C0.class);

        final AtomicInteger cnt = new AtomicInteger();
        r0.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                cnt.incrementAndGet();
                LOG.fine("r0 notified");
            }
        });
        
        C0 o0 = new C0();
        C1 o1 = new C1();

        LOG.fine("Add o0");
        ic.add(o0);
        assertEquals("One change", 1, cnt.getAndSet(0));

        LOG.fine("Remove o0");
        ic.remove(o0);
        assertEquals("Another change change", 1, cnt.getAndSet(0));

        LOG.fine("Add o1");
        ic.add(o1);
        assertEquals("No change", 0, cnt.getAndSet(0));

        LOG.fine("Remove o1");
        ic.remove(o1);
        assertEquals("No change", 0, cnt.getAndSet(0));

        LOG.fine("Add o0");
        ic.add(o0);
        LOG.fine("Line before should read 'r0 notified' ?");
        assertEquals("One change", 1, cnt.getAndSet(0));
    }
    
    /** Checks the reorder of items in lookup reflects the result.
     * Testing both classes and interfaces, because they are often treated
     * especially.
     */
    public void testReorder () {
        String s1 = "s2";
        String s2 = "s1";
        Runnable r1 = new Runnable () {
            public void run () {}
        };
        Runnable r2 = new Runnable () {
            public void run () {}
        };
        List<Object> l = new ArrayList<Object>();

        l.add (s1);
        l.add (s2);
        l.add (r1);
        l.add (r2);
        ic.set (l, null);
     
        assertEquals ("s1 is found", s1, lookup.lookup (String.class));
        assertEquals ("r1 is found", r1, lookup.lookup (Runnable.class));
        
        Collections.reverse (l);
        
        ic.set (l, null);
        
        assertEquals ("s2 is found", s2, lookup.lookup (String.class));
        assertEquals ("r2 is found", r2, lookup.lookup (Runnable.class));
    }
    
    /** Tries to set empty collection to the lookup.
     */
    public void testSetEmpty () {
        ic.add ("A serializable string");
        lookup.lookup (Serializable.class);
        
        ic.set (Collections.emptyList(), null);
    }
    
    /** Tests a more complex reorder on nodes.
     */
    public void testComplexReorder () {
        Integer i1 = 1;
        Long i2 = 2L;
        
        List<Object> l = new ArrayList<Object>();
        l.add (i1);
        l.add (i2);
        ic.set (l, null);
        
        assertEquals ("Find integer", i1, lookup.lookup (Integer.class));
        assertEquals ("Find long", i2, lookup.lookup (Long.class));
        assertEquals ("Find number", i1, lookup.lookup (Number.class));
        
        Collections.reverse (l);
        
        ic.set (l, null);
        
        assertEquals ("Find integer", i1, lookup.lookup (Integer.class));
        assertEquals ("Find long", i2, lookup.lookup (Long.class));
        assertEquals ("Find number", i2, lookup.lookup (Number.class));
    }
    
    /** Checks whether setPairs keeps the order.
     */
    public void testSetPairs () {
        // test setPairs method
        List<Object> li = new ArrayList<Object>();
        li.addAll (Arrays.asList (INSTANCES));
        ic.set (li, null);
        
        Lookup.Result<Object> res = lookup.lookupResult(Object.class);
        Iterator<?> all = res.allInstances().iterator();
        checkIterator ("Original order not kept", all, li);
        
        // reverse the order
        Collections.reverse (li);
        
        // change the pairs
        LL listener = new LL (res);
        res.addLookupListener (listener);
        ic.set (li, null);
        if (listener.getCount () != 1) {
            fail ("Result has not changed even we set reversed order");
        }
        
        all = res.allInstances ().iterator ();
        checkIterator ("Reversed order not kept", all, li);
    }

    /** Checks whether setPairs fires correct events.
     */
    public void testSetPairsFire () {
        // test setPairs method
        List<Object> li = new ArrayList<Object>();
        li.addAll (Arrays.asList (INSTANCES));
        ic.set (li, null);
        
        Lookup.Result<Integer> res = lookup.lookupResult(Integer.class);
        Iterator<?> all = res.allInstances().iterator();
        checkIterator ("Integer is not there", all, Collections.nCopies (1, INSTANCES[0]));
        
        // change the pairs
        LL listener = new LL (res);
        res.addLookupListener (listener);

        List<Object> l2 = new ArrayList<Object>(li);
        l2.remove (INSTANCES[0]);
        ic.set (l2, null);

        all = lookup.lookupAll(Object.class).iterator();
        checkIterator ("The removed integer is not noticed", all, l2);

        if (listener.getCount () != 1) {
            fail ("Nothing has not been fired");
        }
    }

    /** Checks whether set pairs does not fire when they should not.
    */
    public void testSetPairsDoesNotFire () {
        Object tmp = new Object ();

        List<Object> li = new ArrayList<Object>();
        li.add (tmp);
        li.addAll (Arrays.asList (INSTANCES));
        ic.set (li, null);
        
        Lookup.Result<Integer> res = lookup.lookupResult(Integer.class);
        Iterator<?> all = res.allInstances ().iterator ();
        checkIterator ("Integer is not there", all, Collections.nCopies (1, INSTANCES[0]));
        
        // change the pairs
        LL listener = new LL (res);
        res.addLookupListener (listener);

        List<Object> l2 = new ArrayList<Object>(li);
        l2.remove (tmp);
        ic.set (l2, null);

        all = lookup.lookupAll(Object.class).iterator();
        checkIterator ("The removed integer is not noticed", all, l2);

        if (listener.getCount () != 0) {
            fail ("Something has been fired");
        }
    }
    
    /** Test whether after registration it is possible to find registered objects
    * 
     */
    public void testLookupAndAdd () throws Exception {
        addInstances (INSTANCES);

        for (int i = 0; i < INSTANCES.length; i++) {
            Object obj = INSTANCES[i];
            findAll (lookup, obj.getClass (), true);
        }
    }

    /** Tries to find all classes and superclasses in the lookup.
    */
    private void findAll(Lookup lookup, Class<?> clazz, boolean shouldBeThere) {
        if (clazz == null) return;

        Object found = lookup.lookup (clazz);
        if (found == null) {
            if (shouldBeThere) {
                // should find at either instance or something else, but must
                // find at least something
                fail ("Lookup (" + clazz.getName () + ") found nothing");
            }
        } else {
            if (!shouldBeThere) {
                // should find at either instance or something else, but must
                // find at least something
                fail ("Lookup (" + clazz.getName () + ") found " + found);
            }
        }

        Lookup.Result<?> res = lookup.lookupResult(clazz);
        Collection<?> collection = res.allInstances();

        for (int i = 0; i < INSTANCES.length; i++) {
            boolean isSubclass = clazz.isInstance (INSTANCES[i]);
            boolean isThere = collection.contains (INSTANCES[i]);

            if (isSubclass != isThere) {
                // a problem found
                // should find at either instance or something else, but must
                // find at least something
                fail ("Lookup.Result (" + clazz.getName () + ") for " + INSTANCES[i] + " is subclass: " + isSubclass + " isThere: " + isThere);
            }
        }

        // go on for superclasses

        findAll (lookup, clazz.getSuperclass (), shouldBeThere);

        Class[] ies = clazz.getInterfaces ();
        for (int i = 0; i < ies.length; i++) {
            findAll (lookup, ies[i], shouldBeThere);
        }
    }
    
    /** Test if it is possible to remove a registered object. */
    public void testRemoveRegisteredObject() {
        Integer inst = new Integer(10);
        
        ic.add(inst);
        if (lookup.lookup(inst.getClass()) == null) {
            // should find an instance
            fail("Lookup (" + inst.getClass().getName () + ") found nothing");
        }
        
        ic.remove(inst);
        if (lookup.lookup(inst.getClass()) != null) {
            // should NOT find an instance
            fail("Lookup (" + inst.getClass().getName () +
                ") found an instance after remove operation");
        }
    }
    
    public void testCanReturnReallyStrangeResults () throws Exception {
        class QueryingPair extends AbstractLookup.Pair<Object> {
            private Integer i = 434;
            
            //
            // do the test
            //
            
            public void doTest () throws Exception {
                ic.add (i);
                ic.addPair (this);
                
                Object found = lookup.lookup (QueryingPair.class);
                assertEquals ("This object is found", this, found);
            }
            
            
            //
            // Implementation of pair
            // 
        
            public String getId() {
                return getType ().toString();
            }

            public String getDisplayName() {
                return getId ();
            }

            public Class<?> getType() {
                return getClass ();
            }

            protected boolean creatorOf(Object obj) {
                return obj == this;
            }

            protected boolean instanceOf(Class<?> c) {
                assertEquals ("Integer found or exception is thrown", i, lookup.lookup (Integer.class));
                return c.isAssignableFrom(getType ());
            }

            public Object getInstance() {
                return this;
            }
            
            
        }
        
        
        QueryingPair qp = new QueryingPair ();
        qp.doTest ();
    }
    
    /** Test of firing events. */
    public void testLookupListener() {
        Object inst = 10;
        Lookup.Result<?> res = lookup.lookupResult(inst.getClass());
        res.allInstances ();
        
        LL listener = new LL(res);
        res.addLookupListener(listener);
        
        ic.add(inst);
        if (listener.getCount() == 0) {
            fail("None event fired during NbLookup.addPair()");
        }
        
        ic.remove(inst);
        if (listener.getCount() == 0) {
            fail("None event fired during NbLookup.removePair()");
        }
        
        ic.add(inst);
        if (listener.getCount() == 0) {
            fail("None event fired during second NbLookup.addPair()");
        }
        
        ic.remove(inst);
        if (listener.getCount() == 0) {
            fail("None event fired during second NbLookup.removePair()");
        }
    }
    
    /** Testing identity of the lookup.
     */
    public void testId () {
        Lookup.Template<?> templ;
        int cnt;
        
        addInstances (INSTANCES);
        
        Lookup.Result<?> res = lookup.lookupResult(Object.class);
        for (AbstractLookup.Item<?> item : res.allItems()) {
            
            templ = new Lookup.Template<Object>(null, item.getId(), null);
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 1) {
                fail ("Identity lookup failed. Instances = " + cnt);
            }

            templ = makeTemplate(item.getType(), item.getId());
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 1) {
                fail ("Identity lookup with type failed. Instances = " + cnt);
            }
            
            templ = makeTemplate(this.getClass(), item.getId());
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 0) {
                fail ("Identity lookup with wrong type failed. Instances = " + cnt);
            }
            
            templ = new Lookup.Template<Object>(null, null, item.getInstance());
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 1) {
                fail ("Instance lookup failed. Instances = " + cnt);
            }

            templ = new Lookup.Template<Object>(null, item.getId(), item.getInstance());
            cnt = lookup.lookup (templ).allInstances ().size ();
            if (cnt != 1) {
                fail ("Instance & identity lookup failed. Instances = " + cnt);
            }
            
        }
    }
    private static <T> Lookup.Template<T> makeTemplate(Class<T> clazz, String id) { // captures type parameter
        return new Lookup.Template<T>(clazz, id, null);
    }
    
    /** Tests adding and removing.
     */
    public void testAddAndRemove () throws Exception {
        Object map = new javax.swing.ActionMap ();
        LL ll = new LL ();
        
        Lookup.Result<?> res = lookup.lookupResult(map.getClass());
        res.allItems();
        res.addLookupListener (ll);
        ll.source = res;
        
        ic.add (map);
        
        assertEquals ("First change when adding", ll.getCount (), 1);
        
        ic.remove (map);
        
        assertEquals ("Second when removing", ll.getCount (), 1);
        
        ic.add (map);
        
        assertEquals ("Third when readding", ll.getCount (), 1);
        
        ic.remove (map);
        
        assertEquals ("Forth when reremoving", ll.getCount (), 1);
        
    }
    
    /** Will a class garbage collect even it is registered in lookup.
     */
    public void testGarbageCollect () throws Exception {
        ClassLoader l = new CL ();
        Class<?> c = l.loadClass(Garbage.class.getName());
        Reference<?> ref = new WeakReference<Object>(c);

        lookup.lookup (c);
        
        // now test garbage collection
        c = null;
        l = null;
        impl.clearCaches ();
        assertGC ("The classloader has not been garbage collected!", ref);
    }
                
    /** Items are the same as results.
     */
    public void testItemsAndIntances () {
        addInstances (INSTANCES);
        
        Lookup.Result<Object> r = lookup.lookupResult(Object.class);
        Collection<? extends Lookup.Item<?>> items = r.allItems();
        Collection<?> insts = r.allInstances();
        
        if (items.size () != insts.size ()) {
            fail ("Different size of sets");
        }

        for (Lookup.Item<?> item : items) {
            if (!insts.contains (item.getInstance ())) {
                fail ("Intance " + item.getInstance () + " is missing in " + insts);
            }
        }
    }
    
    /** Checks search for interface.
     */
    public void testSearchForInterface () {
        Lookup.Template<Serializable> t = new Lookup.Template<Serializable>(Serializable.class, null, null);
        
        assertNull("Nothing to find", lookup.lookupItem (t));
        
        Serializable s = new Serializable () {};
        ic.add (s);
        
        Lookup.Item item = lookup.lookupItem (t);
        assertNotNull ("Something found", item);
    }

    /** Test to add broken item if it incorrectly answers instanceOf questions.
     */
    public void testIncorectInstanceOf40364 () {
        final Long sharedLong = new Long (0);
        
        class P extends AbstractLookup.Pair<Object> {
            public boolean isLong;
            
            P (boolean b) {
                isLong = b;
            }
            
            protected boolean creatorOf (Object obj) {
                return obj == sharedLong;
            }
            
            public String getDisplayName () {
                return "";
            }
            
            public String getId () {
                return "";
            }
            
            public Object getInstance () {
                return sharedLong;
            }
            
            public Class<?> getType() {
                return isLong ? Long.class : Number.class;
            }
            
            protected boolean instanceOf(Class<?> c) {
                return c.isAssignableFrom (getType ());
            }
    
            public @Override int hashCode() {
                return getClass ().hashCode ();
            }    

            public @Override boolean equals(Object obj) {
                return obj != null && getClass ().equals (obj.getClass ());
            }
        }
        
        // to create the right structure in the lookup
        lookup.lookup (Object.class);
        lookup.lookup (Long.class);
        lookup.lookup (Number.class);
        
        P lng1 = new P (true);
        ic.addPair (lng1);

        P lng2 = new P (false);
        ic.setPairs (Collections.singleton (lng2));
        
        Collection<? extends Lookup.Item<?>> res = lookup.lookupResult(Object.class).allItems();
        assertEquals ("Just one pair", 1, res.size ());
    }

    public void testAbsolutelyCrazyWayToSimulateIssue48590ByChangingTheBehaviourOfEqualOnTheFly () throws Exception {
        class X implements TestInterfaceInheritanceA, TestInterfaceInheritanceB {
        }
        final X shared = new X ();
        
        class P extends AbstractLookup.Pair<Object> {
            public int howLong;
            
            P (int b) {
                howLong = b;
            }
            
            protected boolean creatorOf (Object obj) {
                return obj == shared;
            }
            
            public String getDisplayName () {
                return "";
            }
            
            public String getId () {
                return "";
            }
            
            public Object getInstance () {
                return shared;
            }
            
            public Class<?> getType() {
                return howLong == 0 ? TestInterfaceInheritanceB.class : TestInterfaceInheritanceA.class;
            }
            
            protected boolean instanceOf(Class<?> c) {
                return c.isAssignableFrom (getType ());
            }
    
            public @Override int hashCode() {
                return getClass ().hashCode ();
            }    

            public @Override boolean equals(Object obj) {
                if (obj instanceof P) {
                    P p = (P)obj;
                    if (this.howLong > 0) {
                        this.howLong--;
                        return false;
                    }
                    if (p.howLong > 0) {
                        p.howLong--;
                        return false;
                    }
                    return getClass ().equals (p.getClass ());
                }
                return false;
            }
        }
        
        // to create the right structure in the lookup
        Lookup.Result<?> a = lookup.lookupResult(TestInterfaceInheritanceA.class);
        Lookup.Result<?> b = lookup.lookupResult(TestInterfaceInheritanceB.class);
        
        P lng1 = new P (0);
        ic.addPair (lng1);
        
        assertEquals ("One in a", 1, a.allItems ().size ());
        assertEquals ("One in b", 1, b.allItems ().size ());

        P lng2 = new P (1);
        

        /* Following call used to generate this exception:
    java.lang.IllegalStateException: Duplicate pair in treePair1:  pair2:  index1: 0 index2: 0 item1: org.openide.util.lookup.AbstractLookupBaseHid$1X@1a457b6 item2: org.openide.util.lookup.AbstractLookupBaseHid$1X@1a457b6 id1: 7a78d3 id2: 929206
	at org.openide.util.lookup.ALPairComparator.compare(ALPairComparator.java:52)
	at java.util.Arrays.mergeSort(Arrays.java:1284)
	at java.util.Arrays.sort(Arrays.java:1223)
	at java.util.Collections.sort(Collections.java:159)
	at org.openide.util.lookup.InheritanceTree.retainAllInterface(InheritanceTree.java:753)
	at org.openide.util.lookup.InheritanceTree.retainAll(InheritanceTree.java:183)
	at org.openide.util.lookup.DelegatingStorage.retainAll(DelegatingStorage.java:83)
	at org.openide.util.lookup.AbstractLookup.setPairsAndCollectListeners(AbstractLookup.java:238)
	at org.openide.util.lookup.AbstractLookup.setPairs(AbstractLookup.java:203)
	at org.openide.util.lookup.AbstractLookup$Content.setPairs(AbstractLookup.java:885)
	at org.openide.util.lookup.AbstractLookupBaseHid.testAbsolutelyCrazyWayToSimulateIssue48590ByChangingTheBehaviourOfEqualOnTheFly(AbstractLookupBaseHid.java:696)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at org.netbeans.junit.NbTestCase.run(NbTestCase.java:119)
    */  
        ic.setPairs (Collections.singleton (lng2));

        
    }
    
    public void testInstancesArePreservedFoundWhenFixing48590 () throws Exception {
        class X implements Runnable, Serializable {
            public void run () {
                
            }
            
            public void assertOnlyMe (String msg, Lookup.Result<?> res) {
                Collection<?> col = res.allInstances();
                assertEquals (msg + " just one", 1, col.size ());
                assertSame (msg + " and it is me", this, col.iterator ().next ());
            }
        }
        
        Lookup.Result<?> runnable = lookup.lookupResult(Runnable.class);
        Lookup.Result<?> serial = lookup.lookupResult(Serializable.class);
        
        
        X x = new X ();
        ic.add (x);
        
        
        x.assertOnlyMe ("x implements it (1)", runnable);
        x.assertOnlyMe ("x implements it (2)", serial);
        
        ic.set (Collections.singleton (x), null);
        
        x.assertOnlyMe ("x implements it (3)", runnable);
        x.assertOnlyMe ("x implements it (4)", serial);
    }
    
    /** Testing lookup of inherited classes. */
    public void testInheritance() {
        class A {}
        class B extends A implements java.rmi.Remote {}
        class BB extends B {}
        class C extends A implements java.rmi.Remote {}
        class D extends A {}
        
        A[] types = {new B(), new BB(), new C(), new D()};
        
        for (int i = 0; i < types.length; i++) {
            ic.add(types[i]);
            if (lookup.lookup(types[i].getClass()) == null) {
                // should find an instance
                fail("Lookup (" + types[i].getClass().getName () + ") found nothing");
            }
        }
        
        int size1, size2;
        
        //interface query
        size1 = lookup.lookupAll(java.rmi.Remote.class).size();
        size2 = countInstances(types, java.rmi.Remote.class);
        
        if (size1 != size2) fail("Lookup with interface failed: " + size1 + " != " + size2);
        
        // superclass query
        size1 = lookup.lookupAll(A.class).size();
        size2 = countInstances(types, A.class);
        
        if (size1 != size2) fail("Lookup with superclass failed: " + size1 + " != " + size2);
    }
    
    /** Test interface inheritance.
     */
    public void testInterfaceInheritance() {
        TestInterfaceInheritanceA[] types = {
            new TestInterfaceInheritanceB() {},
            new TestInterfaceInheritanceBB() {},
            new TestInterfaceInheritanceC() {},
            new TestInterfaceInheritanceD() {}
        };
        
        for (int i = 0; i < types.length; i++) {
            ic.add(types[i]);
            if (lookup.lookup(types[i].getClass()) == null) {
                // should find an instance
                fail("Lookup (" + types[i].getClass().getName () + ") found nothing");
            }
        }
        
        int size1, size2;
        
        //interface query
        LL l = new LL ();
        Lookup.Result<?> res = lookup.lookupResult(java.rmi.Remote.class);
        l.source = res;
        size1 = res.allInstances().size();
        size2 = countInstances(types, java.rmi.Remote.class);
        
        if (size1 != size2) fail("Lookup with interface failed: " + size1 + " != " + size2);
        
        // superclass query
        size1 = lookup.lookupAll(TestInterfaceInheritanceA.class).size();
        size2 = countInstances(types, TestInterfaceInheritanceA.class);
        
        if (size1 != size2) fail("Lookup with superclass failed: " + size1 + " != " + size2);
        
        res.addLookupListener (l);
        ic.remove (types[0]);
        
        if (l.getCount () != 1) {
            fail ("No notification that a Remote is removed");
        }
    }
    
    /** Checks whether the AbstractLookup is guarded against modifications
     * while doing some kind of modification.
     */
    public void testModificationArePreventedWhenDoingModifications () throws Exception {
        BrokenPair broken = new BrokenPair (true, false);
        ic.addPair (broken);
        
        Lookup.Template<BrokenPair> templ = new Lookup.Template<BrokenPair>(BrokenPair.class);
        Lookup.Item<BrokenPair> item = lookup.lookupItem (templ);
        assertEquals ("Broken is found", broken, item);
    }
    
    public void testModificationArePreventedWhenDoingModificationsResult () throws Exception {
        BrokenPair broken = new BrokenPair (false, true);
        ic.addPair (broken);
        
        Lookup.Template<BrokenPair> templ = new Lookup.Template<BrokenPair>(BrokenPair.class);
        
        Collection<? extends BrokenPair> c = lookup.lookup (templ).allInstances();
        assertEquals ("One item", 1, c.size ());
        assertEquals ("Broken is found again", broken, c.iterator().next ());
    }
    
    public void testModificationArePreventedWhenDoingModificationsItemAndResult () throws Exception {
        BrokenPair broken = new BrokenPair (false, true);
        ic.addPair (broken);
        
        Lookup.Template<BrokenPair> templ = new Lookup.Template<BrokenPair>(BrokenPair.class);
        Lookup.Item<BrokenPair> item = lookup.lookupItem (templ);
        assertEquals ("Broken is found", broken, item);
        
        Collection<? extends BrokenPair> c = lookup.lookup(templ).allInstances();
        assertEquals ("One item", 1, c.size ());
        assertEquals ("Broken is found again", broken, c.iterator().next ());
    }

    public void testModificationArePreventedWhenDoingModificationsResultAndItem () throws Exception {
        BrokenPair broken = new BrokenPair (false, true);
        ic.addPair (broken);
        
        Lookup.Template<BrokenPair> templ = new Lookup.Template<BrokenPair>(BrokenPair.class);
        Collection<? extends BrokenPair> c = lookup.lookup(templ).allInstances();
        assertEquals ("One item", 1, c.size ());
        assertEquals ("Broken is found again", broken, c.iterator().next ());
        
        Object item = lookup.lookupItem (templ);
        assertEquals ("Broken is found", broken, item);
    }
    
    public void testAddALotOfPairsIntoTheLookupOneByOne () throws Exception {
        Lookup.Result<Integer> res = lookup.lookupResult(Integer.class);
        for (int i = 0; i < 1000; i++) {
            ic.add(i);
        }
        assertEquals (
            "there is the right count", 
            1000, 
            res.allItems().size ()
        );
    }
    
    public void testAddALotOfPairsIntoTheLookup () throws Exception {
        List<Integer> arr = new ArrayList<Integer>();
        for (int i = 0; i < 1000; i++) {
            arr.add(i);
        }
        ic.set (arr, null);
        
        assertEquals (
            "there is the right count", 
            1000, 
            lookup.lookupResult(Integer.class).allItems().size()
        );
    }

    
    public void testDoubleAddIssue35274 () throws Exception {
        class P extends AbstractLookup.Pair<Object> {
            protected boolean creatorOf(Object obj) { return false; }
            public String getDisplayName() { return ""; }
            public String getId() { return ""; }
            public Object getInstance() { return null; }
            public Class<?> getType() { return Object.class; }
            protected boolean instanceOf(Class<?> c) { return c.isAssignableFrom(getType ()); }
            public @Override int hashCode() {return getClass().hashCode();}
            public @Override boolean equals(Object obj) {return getClass() == obj.getClass();}
        }
        
        P p = new P ();
        
        ic.addPair (p);
        ic.addPair (p);
        
        Lookup.Result<Object> result = lookup.lookupResult(Object.class);
        Collection res = result.allItems ();
        assertEquals ("One item there", 1, res.size ());
        assertTrue ("It is the p", p == res.iterator ().next ());
        
        P p2 = new P ();
        ic.addPair (p2);
        
        Reference<?> ref = new WeakReference<Object>(result);
        result = null;
        assertGC ("The result can disappear", ref);
        
        impl.clearCaches ();
        
        result = lookup.lookupResult(Object.class);
        res = result.allItems ();
        assertEquals ("One item is still there", 1, res.size ());
        assertTrue ("But the p2 replaced p", p2 == res.iterator ().next ());
        
    }
    
    /** Test for proper serialization.
     */
    public void testSerializationSupport () throws Exception {
        doSerializationSupport (1);
    }
    public void testDoubleSerializationSupport () throws Exception {
        doSerializationSupport (2);
    }

    private void doSerializationSupport (int count) throws Exception {
        if (lookup instanceof Serializable) {
            ic.addPair (new SerialPair ("1"));
            ic.addPair (new SerialPair ("2"));
            ic.addPair (new SerialPair ("3"));

            Lookup l = (Lookup)reserialize(lookup);

            assertEquals ("Able to answer simple query", "1", l.lookup (String.class));

            assertEquals ("Three objects there", 3, l.lookup (new Lookup.Template (String.class)).allInstances().size ());

            while (count-- > 0) {
                l = (Lookup)reserialize(l);
            }

            assertEquals ("Able to answer simple query", "1", l.lookup (String.class));

            assertEquals ("Three objects there", 3, l.lookup (new Lookup.Template (String.class)).allInstances().size ());
        }
    }

    /** When a lookup with two different versions of the same class 
     * get's serialized, the results may be very bad. 
     */
    public void testSerializationOfTwoClassesWithTheSameName () throws Exception {
        if (lookup instanceof Serializable) {
            doTwoSerializedClasses (false, false);
        }
    }
    public void testSerializationOfTwoClassesWithTheSameNameButQueryBeforeSave () throws Exception {
        if (lookup instanceof Serializable) {
            doTwoSerializedClasses (true, false);
        }
    }
    public void testSerializationOfTwoClassesWithTheSameNameWithBroken () throws Exception {
        if (lookup instanceof Serializable) {
            doTwoSerializedClasses (false, true);
        }
    }
    public void testSerializationOfTwoClassesWithTheSameNameButQueryBeforeSaveWithBroken () throws Exception {
        if (lookup instanceof Serializable) {
            doTwoSerializedClasses (true, true);
        }
    }
   
    private void doTwoSerializedClasses (boolean queryBeforeSerialization, boolean useBroken) throws Exception {
        ClassLoader loader = new CL ();
        Class c = loader.loadClass (Garbage.class.getName ());

        // in case of InheritanceTree it creates a slot for class Garbage
        lookup.lookup(c);

        // but creates new instance and adds it into the lookup
        // without querying for it
        loader = new CL ();
        c = loader.loadClass (Garbage.class.getName ());

        Object theInstance = c.getDeclaredConstructor().newInstance ();

        ic.addPair (new SerialPair (theInstance));

        Broken2Pair broken = null;
        if (useBroken) {
            broken = new Broken2Pair ();
            ic.addPair (broken);
            
            assertNull (
                "We need to create the slot for the List as " +
                "the Broken2Pair will ask for it after deserialization", 
                lookup.lookup (java.awt.List.class)
            );
        }

        if (queryBeforeSerialization) {
            assertEquals ("Instance is found", theInstance, lookup.lookup (c));
        }
        
        // replace the old lookup with new one
        lookup = (Lookup)reserialize(lookup);
        
        Lookup.Result result = lookup.lookup (new Lookup.Template (Garbage.class));
        assertEquals ("One item is the result", 1, result.allInstances ().size ());
        Object r = result.allInstances ().iterator ().next ();
        assertNotNull("A value is found", r);
        assertEquals ("It is of the right class", Garbage.class, r.getClass());
    }
   
    /** Test of reorder and item change which used to fail on interfaces.
     */
    public void testReoderingIssue13779 () throws Exception {
        LinkedList arr = new LinkedList ();
        
        class R extends Exception implements Cloneable {
        }
        Object o1 = new R ();
        Object o2 = new R ();
        Object o3 = new R ();
        
        arr.add (o1);
        arr.add (o2);
        
        ic.set (arr, null);
        
        Lookup.Result objectResult = lookup.lookup (new Lookup.Template (Exception.class));
        Lookup.Result interfaceResult = lookup.lookup (new Lookup.Template (Cloneable.class));
        objectResult.allItems ();
        interfaceResult.allItems ();
        
        LL l1 = new LL (objectResult);
        LL l2 = new LL (interfaceResult);
        
        objectResult.addLookupListener(l1);
        interfaceResult.addLookupListener(l2);
        
        arr.addFirst (o3);
        
        ic.set (arr, null);
        
        assertEquals ("One change on objects", 1, l1.getCount ());
        assertEquals ("One change on interfaces", 1, l2.getCount ());
        
        arr.addFirst (new Cloneable () { });
        ic.set (arr, null);
        
        assertEquals ("No change on objects", 0, l1.getCount ());
        assertEquals ("But one change on interfaces", 1, l2.getCount ());
        
    }
    
    public void testDeadlockBetweenProxyResultAndLookupIssue47772 () throws Exception {
        final String myModule = "My Module";
        ic.add (myModule);
        
        class MyProxy extends ProxyLookup {
            public MyProxy () {
                super (new Lookup[] { lookup });
            }
        }
        final MyProxy my = new MyProxy ();
        
        final Lookup.Result allModules = my.lookup (new Lookup.Template (String.class));
        
        class PairThatNeedsInfoAboutModules extends AbstractLookup.Pair {
            public String getDisplayName () {
                return "Need a module";
            }
            public String getId () {
                return getDisplayName ();
            }
            public Class getType () {
                return Integer.class;
            }
            protected boolean instanceOf (Class c) {
                if (c == Integer.class) {
                    synchronized (this) {
                        notifyAll ();
                        try {
                            wait (1000);
                        } catch (InterruptedException ex) {
                            fail (ex.getMessage ());
                        }
                    }
                    java.util.Collection coll = allModules.allInstances ();
                    assertEquals ("Size is 1", 1, coll.size ());
                    assertEquals ("My module is there", myModule, coll.iterator ().next ());
                }
                return c.isAssignableFrom (Integer.class);
            }
            
            public Object getInstance () {
                return new Integer (10);
            }
            
            protected boolean creatorOf (Object obj) {
                return new Integer (10).equals (obj);
            }
        }
        
        PairThatNeedsInfoAboutModules pair = new PairThatNeedsInfoAboutModules ();
        ic.addPair (pair);
        
        synchronized (pair) {
            class BlockInInstanceOf implements Runnable {
                public void run () {
                    Integer i = my.lookup(Integer.class);
                    assertEquals (new Integer (10), i);
                }
            }
            BlockInInstanceOf blk = new BlockInInstanceOf ();
            Executors.newSingleThreadScheduledExecutor().schedule(blk, 0, TimeUnit.MICROSECONDS);
            pair.wait ();
        }
        
        java.util.Collection coll = allModules.allInstances ();
        assertEquals ("Size is 1", 1, coll.size ());
        assertEquals ("My module is there", myModule, coll.iterator ().next ());
    }

    public void testAWayToGenerateProblem13779 () {
        ic.add (new Integer (1));
        ic.add (new Integer (2));
        ic.add (new Integer (1));
        ic.add (new Integer (2));
        
        Collection c = lookup.lookup (new Lookup.Template (Integer.class)).allInstances ();
        assertEquals ("There are two objects", 2, c.size ());
        
    }
    
    /** Replacing items with different objects.
     */
    public void testReplacingObjectsDoesNotGenerateException () throws Exception {
        LinkedList arr = new LinkedList ();
        
        class R extends Exception implements Cloneable {
        }
        arr.add (new R ());
        arr.add (new R ());
        
        ic.set (arr, null);
        
        arr.clear();
        
        arr.add (new R ());
        arr.add (new R ());
        
        ic.set (arr, null);
    }

    public void testAfterDeserializationNoQueryIsPeformedOnAlreadyQueriedObjects() throws Exception {
        if (! (lookup instanceof Serializable)) {
            // well this test works only for serializable lookups
            return;
        }
        
        SerialPair my = new SerialPair ("no");
        ic.addPair (my);
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (String.class));
        assertEquals ("One instance", 1, res.allInstances().size ());
        assertEquals ("my.instanceOf called once", 1, my.countInstanceOf);
        
        Lookup serial = (Lookup)reserialize(lookup);
        
        Lookup.Result r2 = serial.lookup(new Lookup.Template(String.class));
        
        assertEquals ("One item", 1, r2.allItems ().size ());
        Object one = r2.allItems().iterator().next ();
        assertEquals ("The right class", SerialPair.class, one.getClass());
        SerialPair p = (SerialPair)one;
        
        assertEquals ("p.instanceOf has not been queried", 0, p.countInstanceOf);
    }
    
    /** Checks the iterator */
    private <T> void checkIterator(String msg, Iterator<? extends T> it1, List<? extends T> list) {
        int cnt = 0;
        Iterator<? extends T> it2 = list.iterator();
        while (it1.hasNext () && it2.hasNext ()) {
            T n1 = it1.next();
            T n2 = it2.next();
            
            if (n1 != n2) {
                fail (msg + " iterator[" + cnt + "] = " + n1 + " but list[" + cnt + "] = " + n2);
            }
            
            cnt++;
        }
        
        if (it1.hasNext ()) {
            fail ("Iterator has more elements than list");
        }
        
        if (it2.hasNext ()) {
            fail ("List has more elements than iterator");
        }
    }
    
    
    public void testResultsAreUnmodifyableOrAtLeastTheyDoNotPropagateToCache() throws Exception {
        String s = "Ahoj";
        
        ic.add(s);
        
        Lookup.Result res = lookup.lookup(new Template(String.class));
        
        for (int i = 1; i < 5; i++) {
            Collection c1 = res.allInstances();
            Collection c2 = res.allClasses();
            Collection c3 = res.allItems();

            assertTrue(i + ": c1 has it", c1.contains(s));
            assertTrue(i + ": c2 has it", c2.contains(s.getClass()));
            assertEquals(i + ": c3 has one", 1, c3.size());
            Lookup.Item item = (Lookup.Item) c3.iterator().next();
            assertEquals(i + ": c3 has it", s, item.getInstance());

            try {
                c1.remove(s);
                assertEquals("No elements now", 0, c1.size());
            } catch (UnsupportedOperationException ex) {
                // ok, this need not be supported
            }
            try {
                c2.remove(s.getClass());
                assertEquals("No elements now", 0, c2.size());
            } catch (UnsupportedOperationException ex) {
                // ok, this need not be supported
            }
            try {
                c3.remove(item);
                assertEquals("No elements now", 0, c3.size());
            } catch (UnsupportedOperationException ex) {
                // ok, this need not be supported
            }
        }
    }
    
    public void testSomeProblemWithDVBFrameworkSeemsToBeInLookup() {
        for (int i = 0; i < 5; i++) {
            ic.add(lookup);
            assertEquals("Can be found", lookup, lookup.lookup(lookup.getClass()));
            ic.set(Collections.EMPTY_LIST, null);
        }        
    }

    public void testListeningAndQueryingByTwoListenersInstances() {
        doListeningAndQueryingByTwoListeners(0);
    }
    public void testListeningAndQueryingByTwoListenersClasses() {
        doListeningAndQueryingByTwoListeners(1);        
    }
    public void testListeningAndQueryingByTwoListenersItems() {
        doListeningAndQueryingByTwoListeners(2);
    }
    
    
    private void doListeningAndQueryingByTwoListeners(final int type) {
        class L implements LookupListener {
            Lookup.Result integer = lookup.lookup(new Template(Integer.class));
            Lookup.Result number = lookup.lookup(new Template(Number.class));
            Lookup.Result serial = lookup.lookup(new Template(Serializable.class));
            
            {
                integer.addLookupListener(this);
                number.addLookupListener(this);
                serial.addLookupListener(this);
            }
            
            int round;
            
            public void resultChanged(LookupEvent ev) {
                Collection c1 = get(type, integer);
                Collection c2 = get(type, number);
                Collection c3 = get(type, serial);
                
                assertEquals("round " + round + " c1 vs. c2", c1, c2);
                assertEquals("round " + round + " c1 vs. c3", c1, c3);
                assertEquals("round " + round + " c2 vs. c3", c2, c3);
                
                round++;
            }            

            private Collection get(int type, Lookup.Result res) {
                Collection c;
                switch(type) {
                    case 0: c = res.allInstances(); break;
                    case 1: c = res.allClasses(); break;
                    case 2: c = res.allItems(); break;
                    default: c = null; fail("Type: " + type); break;
                }
                
                assertNotNull(c);
                return new ArrayList(c);
            }
        }
        
        L listener = new L();
        listener.resultChanged(null);
        
        for(int i = 0; i < 100; i++) {
            ic.add(new Integer(i));
        }
        
        assertEquals("3x100+1 checks", 301, listener.round);
    }
    
    public void testChangeOfNodeDoesNotFireChangeInActionMap() {
        ActionMap am = new ActionMap();
        Lookup s = Lookups.singleton(am);
        doChangeOfNodeDoesNotFireChangeInActionMap(am, s, false, 0);
    }
    public void testChangeOfNodeDoesNotFireChangeInActionMapSimple() {
        ActionMap am = new ActionMap();
        Lookup s = Lookups.singleton(am);
        doChangeOfNodeDoesNotFireChangeInActionMap(am, s, true, 0);
    }

    public void testChangeOfNodeDoesNotFireChangeInActionMapWithBeforeLookupSimple() {
        doChangeOfNodeDoesNotFireChangeInActionMapWithBeforeLookup(true);
    }
    
    public void testChangeOfNodeDoesNotFireChangeInActionMapWithBeforeLookup() {
        doChangeOfNodeDoesNotFireChangeInActionMapWithBeforeLookup(false);
    }
    private void doChangeOfNodeDoesNotFireChangeInActionMapWithBeforeLookup(boolean wrapBySimple) {
        final ActionMap am = new ActionMap();
        
        class Before extends AbstractLookup {
            public InstanceContent ic;
            public Before() {
                this(new InstanceContent());
            }
            
            private Before(InstanceContent ic) {
                super(ic);
                this.ic = ic;
            }

            protected @Override void beforeLookup(Template template) {
                if (ic != null) {
                    ic.add(am);
                    ic = null;
                }
            }
        }
        
        Before s = new Before();
        doChangeOfNodeDoesNotFireChangeInActionMap(am, s, wrapBySimple, 1);
        
        assertNull("beforeLookup called once", s.ic);
    }
    
    private void doChangeOfNodeDoesNotFireChangeInActionMap(final ActionMap am, Lookup actionMapLookup, final boolean wrapBySimple, int firstChange) {
        Lookup[] lookups = { lookup, actionMapLookup };
        
        class Provider implements Lookup.Provider {
            ProxyLookup delegate;
            Lookup query;
            
            public Provider(Lookup[] arr) {
                if (wrapBySimple) {
                    delegate = new ProxyLookup(arr);
                    query = Lookups.proxy(this);
                } else {
                    query = delegate = new ProxyLookup(arr);
                }
            }
            
            public Lookup getLookup() {
                return delegate;
            }
            
            public void setLookups(Lookup... arr) {
                if (wrapBySimple) {
                    delegate = new ProxyLookup(arr);                    
                } else {
                    delegate.setLookups(arr);
                }
            }
        }
        
        Provider p = new Provider(lookups);
        
        Lookup.Result res = p.query.lookup(new Lookup.Template(ActionMap.class));
        LL ll = new LL();
        res.addLookupListener(ll);

        assertEquals("No changes yet", 0, ll.getCount());
        Collection c = res.allInstances();
        assertFalse("Has next", c.isEmpty());
        assertEquals("Correct # of changes in first get", firstChange, ll.getCount());
        
        ActionMap am1 = (ActionMap)c.iterator().next();
        assertEquals("Am is there", am, am1);
        
        
        Object m1 = new InputMap();
        Object m2 = new InputMap();
        
        ic.add(m1);
        assertEquals("No change in ActionMap 1", 0, ll.getCount());
        ic.set(Collections.singletonList(m2), null);
        assertEquals("No change in ActionMap 2", 0, ll.getCount());
        ic.add(m2);
        assertEquals("No change in ActionMap 3", 0, ll.getCount());
        p.setLookups(lookup, actionMapLookup, Lookup.EMPTY);
        assertEquals("No change in ActionMap 4", 0, ll.getCount());
        
        ActionMap am2 = p.query.lookup(ActionMap.class);
        assertEquals("Still the same action map", am, am2);
        
        
        class Before extends AbstractLookup {
            public InstanceContent ic;
            public Before() {
                this(new InstanceContent());
            }
            
            private Before(InstanceContent ic) {
                super(ic);
                this.ic = ic;
            }

            // override the same method as MetaInfServicesLookup overrides
            @Override void beforeLookupResult(Template template) {
                if (ic != null) {
                    ic.add(am);
                    ic = null;
                }
            }
        }
        
        Before s = new Before();
        
        // adding different Before, but returning the same instance
        // this happens with metaInfServices lookup often, moreover
        // it adds the instance in beforeLookup, which confuses a lot
        p.setLookups(new Lookup[]{ lookup, s });
        assertEquals("No change in ActionMap 5", 0, ll.getCount());
        
        
    }

    public void testTasklistsCase() throws Exception {
        ic.remove(new Object());
    }
    
    

    public void testMultipleListeners() {
        Object object = new ImplementationObject();
        ic.add(object);
        
        Listener[] listeners = new Listener[4];
        Lookup.Result result = lookup.lookup(new Lookup.Template(LookupObject.class));
        for(int i = 0; i < listeners.length; ++i) {
            listeners[i] = new Listener();
            result.addLookupListener(listeners[i]);
        }
        // initialize listening
        result.allItems().toArray();
        
        ic.remove(object);
        
        // Apparently, only odd-numbered listeners get called when there are multiple LookupListeners on a result
        //for(int i = 0; i < listeners.length; ++i) {
        //    System.out.println("Listener " + i + ": " + listeners[i].wasCalled());            
        //}
        for(int i = 0; i < listeners.length; ++i) {
            assertTrue("Listener " + i + " called", listeners[i].wasCalled());
        }
    }

    static Object reserialize(Object o) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(o);
        oos.close();

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(is);
        return ois.readObject();
    }
    
    private class Listener implements LookupListener {
        private boolean listenerCalled = false;
        
        public void resultChanged(LookupEvent ev) {
            listenerCalled = true;
        }
        
        public boolean wasCalled() {
            return listenerCalled;
        }
        
        public void reset() {
            listenerCalled = false;
        }
    }
    
    private interface LookupObject {}
    private class ImplementationObject implements LookupObject {}
    private class NullObject implements LookupObject {}
    
    
    public void testReturnSomethingElseThenYouClaimYouWillReturn() {
        class Liar extends AbstractLookup.Pair {
            public Object obj;
            
            protected boolean instanceOf(Class c) {
                return c.isAssignableFrom(String.class);
            }

            protected boolean creatorOf(Object obj) {
                return this.obj == obj;
            }

            public Object getInstance() {
                return this.obj;
            }

            public Class getType() {
                return String.class;
            }

            public String getId() {
                return String.class.getName();
            }

            public String getDisplayName() {
                return getId();
            }
        }
        
        
        Liar l = new Liar();
        l.obj = new Integer(5);
        
        this.ic.addPair(l);
        
        Collection c = lookup.lookup(new Lookup.Template(String.class)).allInstances();
        assertTrue("It is empty: " + c, c.isEmpty());
    }

    public void testCanProxyLookupHaveWrongResults() {
        class L implements LookupListener {
            ProxyLookup pl;
            Lookup.Result<String> original;
            Lookup.Result<String> wrapped;
            boolean ok;

            public void test() {
                pl = new ProxyLookup(lookup);
                original = lookup.lookupResult(String.class);

                original.addLookupListener(this);

                wrapped = pl.lookupResult(String.class);

                assertEquals("Original empty", 0, original.allInstances().size());
                assertEquals("Wrapped empty", 0, wrapped.allInstances().size());

                ic.add("Hello!");
            }

            public void resultChanged(LookupEvent ev) {
                ok = true;

                assertEquals("Original has hello", 1, original.allInstances().size());
                assertEquals("Wrapped has hello", 1, wrapped.allInstances().size());
            }

        }
        L listener = new L();
        listener.test();
        assertTrue("Listener called", listener.ok);
    }

    public void testObjectFromInstanceContentConverterDisappearsIfNotReferenced() {
        Conv converter = new Conv("foo");
        ic.add (converter, converter);
        Lookup lkp = instanceLookup;
        StringBuilder sb = lookup.lookup (StringBuilder.class);
        assertNotNull (sb);
        int hash = System.identityHashCode(sb);
        assertEquals ("foo", sb.toString());
        Reference<StringBuilder> r = new WeakReference<StringBuilder>(sb);
        sb = null;
        assertGC("Lookup held onto object", r);
        sb = lookup.lookup (StringBuilder.class);
        assertNotSame(hash, System.identityHashCode(sb));
        r = new WeakReference<StringBuilder>(sb);
        sb = null;
        assertGC("Lookup held onto object", r);
        ic.remove (converter, converter);
        Reference <InstanceContent.Convertor> cref = new WeakReference<InstanceContent.Convertor>(converter);
        converter = null;
        assertGC("Converter still referenced", cref); 

        sb = lkp.lookup(StringBuilder.class);
        assertNull ("Converter removed from lookup, but object it " +
                "created still present:'" + sb +"'", sb);
        converter = new Conv("bar");
        ic.add (converter, converter);
        assertNotNull (lkp.lookup(StringBuilder.class));
        assertEquals ("bar", lkp.lookup(StringBuilder.class).toString());
    }

    private static class Conv implements InstanceContent.Convertor<Conv, StringBuilder> {
        private final String str;
        private Conv (String str) {
            this.str = str;
        }

        public StringBuilder convert(Conv obj) {
            return new StringBuilder (str);
        }

        public Class<? extends StringBuilder> type(Conv obj) {
            return StringBuilder.class;
        }

        public String id(Conv obj) {
            return "Foo";
        }

        public String displayName(Conv obj) {
            return "Foo";
        }
    } // end of Conv

    public void testCanGCResults() throws Exception {
        class L implements LookupListener {
            int cnt;
            
            public void resultChanged(LookupEvent ev) {
                cnt++;
            }
            
        }
        L listener1 = new L();
        L listener2 = new L();
        
        Lookup.Result<String> res1 = this.instanceLookup.lookupResult(String.class);
        Lookup.Result<String> res2 = this.lookup.lookupResult(String.class);
        
        assertEquals("Empty1", 0, res1.allItems().size());
        assertEquals("Empty2", 0, res2.allItems().size());
        
        res1.addLookupListener(listener1);
        res2.addLookupListener(listener2);
        
        addInstances(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        this.ic.add("Ahoj");
        
        assertEquals("Change1", 1, listener1.cnt);
        assertEquals("Change2", 1, listener2.cnt);
        
        assertEquals("Full1", 1, res1.allItems().size());
        assertEquals("Full2", 1, res2.allItems().size());
        
        
        Reference<Object> ref2 = new WeakReference<Object>(res2);
        if (res1 == res2) {
            res1 = null;
        }
        res2 = null;
        assertGC("Result can disappear", ref2);
    }
    
    void beforeActualTest(String n) {
        if (n.equals("testEqualsIsNotCalledTooMuch")) {
            CntPair.cnt = 0;
            CntPair.hashCnt = 0;
            CntPair.instances = 0;
            int how = 1000;

            for(int i = 0; i < how; i++) {
                this.ic.addPair(new CntPair("x" + i));
            }

            assertEquals("No equals called", 0, CntPair.cnt);
            assertEquals("1000 instances ", how, CntPair.instances);
        }
        if (n.equals("testAddFirstWithExecutorBeforeLookupAssociationFails")) {
            doAddFirstWithExecutorBeforeLookupAssociationFails(true);
        }
    }
    
    public void testEqualsIsNotCalledTooMuch() throws Exception {
        // most of the work done in beforeActualTest

        // desirable: assertEquals("no comparitions", 0, CntPair.cnt);
        // works for InheritanceTree, but not for ArrayStorage, but array
        // storages are generally small
        
        if (CntPair.cnt > 12000) {
            fail("Too much comparitions " + CntPair.cnt);
        }
        if (CntPair.hashCnt > 40000) {
            fail("Too much hashes: " + CntPair.hashCnt);
        }
        
        assertEquals("instaces is enough", 1000, CntPair.instances);
    }
    
    /** Adds instances to the instance lookup.
     */
    private void addInstances (Object... instances) {
        for (int i = 0; i < instances.length; i++) {
            ic.add(instances[i]);
        }
    }
    
    /** Count instances of clazz in an array. */
    private int countInstances (Object[] objs, Class clazz) {
        int count = 0;
        for (int i = 0; i < objs.length; i++) {
            if (clazz.isInstance(objs[i])) count++;
        }
        return count;
    }
    
    /** Counting listener */
    protected static class LL implements LookupListener {
        private int count;
        public Object source;
        public Thread changesIn;
        
        public LL () {
            this (null);
        }
        
        public LL (Object source) {
            this.source = source;
        }
        
        public void resultChanged(LookupEvent ev) {
            if (changesIn != null) {
                assertEquals("Changes in the same thread", changesIn, Thread.currentThread());
            } else {
                changesIn = Thread.currentThread();
            }
            ++count;
            if (source != null) {
                assertSame ("Source is the same", source, ev.getSource ());
//                assertSame ("Result is the same", source, ev.getResult ());
            }
        }

        public int getCount() {
            int i = count;
            count = 0;
            return i;
        }
    };

    /** A set of interfaces for testInterfaceInheritance
     */
    interface TestInterfaceInheritanceA {}
    interface TestInterfaceInheritanceB extends TestInterfaceInheritanceA, java.rmi.Remote {}
    interface TestInterfaceInheritanceBB extends TestInterfaceInheritanceB {}
    interface TestInterfaceInheritanceC extends TestInterfaceInheritanceA, java.rmi.Remote {}
    interface TestInterfaceInheritanceD extends TestInterfaceInheritanceA {}
    
    /** A special class for garbage test */
    public static final class Garbage extends Object implements Serializable {
        static final long serialVersionUID = 435340912534L;
    }
    

    /* A classloader that can load one class in a special way */
    private static class CL extends ClassLoader {
        public CL () {
            super (null);
        }

        public @Override Class findClass(String name) throws ClassNotFoundException {
            if (name.equals (Garbage.class.getName ())) {
                String n = name.replace ('.', '/');
                java.io.InputStream is = getClass ().getResourceAsStream ("/" + n + ".class");
                byte[] arr = new byte[8096];
                try {
                    int cnt = is.read (arr);
                    if (cnt == arr.length) {
                        fail ("Buffer to load the class is not big enough");
                    }

                    return defineClass (name, arr, 0, cnt);
                } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                        fail ("IO Exception");
                        return null;
                }
            } else {
                return null;
            }
        }

        /** Convert obj to other object. There is no need to implement
         * cache mechanism. It is provided by AbstractLookup.Item.getInstance().
         * Method should be called more than once because Lookup holds
         * just weak reference.
         */
        public Object convert(Object obj) {
            return null;
        }

        /** Return type of converted object. */
        public Class type(Object obj) {
            try {
                return loadClass (Garbage.class.getName ());
            } catch (ClassNotFoundException ex) {
                fail ("Class not found");
                throw new InternalError ();
            }
        }
    }
    
    private static final class CntPair extends AbstractLookup.Pair {
        private static int instances;
        private String txt;
        
        public CntPair(String txt) {
            this.txt = txt;
            instances++;
        }

        public static int hashCnt;
        @Override
        public int hashCode() {
            hashCnt++;
            return txt.hashCode() + 3777;
        }

        public static int cnt;
        @Override
        public boolean equals(Object obj) {
            cnt++;
            
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CntPair other = (CntPair) obj;
            if (this.txt != other.txt && (this.txt == null || !this.txt.equals(other.txt))) {
                return false;
            }
            return true;
        }

        protected boolean instanceOf(Class c) {
            return c.isAssignableFrom(String.class);
        }

        protected boolean creatorOf(Object obj) {
            return obj == txt;
        }

        public Object getInstance() {
            return txt;
        }

        public Class getType() {
            return String.class;
        }

        public String getId() {
            return txt;
        }

        public String getDisplayName() {
            return txt;
        }
        
    }

    public static final class SerialPair extends AbstractLookup.Pair
    implements java.io.Serializable {
        static final long serialVersionUID = 54305834L;
        private Object value;
        public transient int countInstanceOf;
        
        public SerialPair (Object value) {
            this.value = value;
        }
        
        protected boolean creatorOf(Object obj) {
            return obj == value;
        }
        
        public String getDisplayName() {
            return getId ();
        }
        
        public String getId() {
            return value.toString();
        }
        
        public Object getInstance() {
            return value;
        }
        
        public Class getType() {
            return value.getClass ();
        }
        
        protected boolean instanceOf(Class c) {
            countInstanceOf++;
            return c.isInstance(value);
        }
    } // end of SerialPair
    
    private static class BrokenPair extends AbstractLookup.Pair {
        private transient ThreadLocal IN = new ThreadLocal ();
        private boolean checkModify;
        private boolean checkQuery;
        
        public BrokenPair (boolean checkModify, boolean checkQuery) {
            this.checkModify = checkModify;
            this.checkQuery = checkQuery;
        }
        
        protected boolean creatorOf(Object obj) { return this == obj; }
        public String getDisplayName() { return "Broken"; }
        public String getId() { return "broken"; }
        public Object getInstance() { return this; }
        public Class getType() { return getClass (); }
        protected boolean instanceOf(Class c) { 
            
            if (checkQuery) {
                if (IN.get () == null) {
                    try {
                        IN.set (this);
                        // broken behaviour, tries to modify the lookup
                        // queries have to survive

                        running.lookup.lookup (java.awt.List.class);

                        // 
                        // creation of new result has to survive as well
                        Lookup.Result myQuery = running.lookup.lookup (new Lookup.Template (java.awt.Button.class));
                        Collection all = myQuery.allItems ();
                    } finally {
                        IN.set (null);
                    }
                }
            }
                

            if (checkModify) {
                //
                // modifications should fail
                //

                try {
                    running.ic.addPair (new SerialPair (""));
                    fail ("Modification from a query should be prohibited");
                } catch (IllegalStateException ex) {
                }
                
                try {
                    running.ic.removePair (this);
                    fail ("This has to throw the exception");
                } catch (IllegalStateException ex) {
                }
                try {
                    running.ic.setPairs (Collections.EMPTY_SET);
                    fail ("This has to throw the exception as well");
                } catch (IllegalStateException ex) {
                }
            }
            
            return c.isAssignableFrom(getType ()); 
        }
    } // end of BrokenPair
    
    private static class Broken2Pair extends AbstractLookup.Pair {
        static final long serialVersionUID = 4532587018501L;
        public transient ThreadLocal IN;
        
        public Broken2Pair () {
        }
        
        private void writeObject (java.io.ObjectOutputStream oos) throws java.io.IOException {
        }
        
        private void readObject (java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
            IN = new ThreadLocal ();
        }
        
        protected boolean creatorOf(Object obj) { return this == obj; }
        public String getDisplayName() { return "Broken"; }
        public String getId() { return "broken"; }
        public Object getInstance() { return this; }
        public Class getType() { return getClass (); }
        protected boolean instanceOf(Class c) { 
            
            // behaviour gets broken only after deserialization
            if (IN != null && IN.get () == null) {
                try {
                    IN.set (this);

                    // creation of new result has to survive as well
                    Lookup.Result myQuery = running.lookup.lookup (new Lookup.Template (java.awt.List.class));
                    Collection all = myQuery.allItems ();
                } finally {
                    IN.set (null);
                }
            }
            
            return c.isAssignableFrom(getType ()); 
        }
    } // end of Broken2Pair    
}
