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

import java.util.concurrent.ExecutionException;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup.Pair;

@SuppressWarnings("unchecked") // XXX ought to be corrected, just a lot of them
public class AbstractLookupTest extends AbstractLookupBaseHid implements AbstractLookupBaseHid.Impl {
    public AbstractLookupTest(java.lang.String testName) {
        super(testName, null);
    }
    
    //
    // Impl of AbstractLookupBaseHid.Impl
    //

    /** Creates the initial abstract lookup.
     */
    public Lookup createInstancesLookup (InstanceContent ic) {
        return new AbstractLookup (ic, new InheritanceTree ());
    }
    
    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (Lookup lookup) {
        return lookup;
    }

    public void clearCaches () {
    }    

    static class LkpResultCanBeGargageCollectedAndClearsTheResult extends AbstractLookup {
        public int cleared;
        public int dirty;

        synchronized @Override boolean cleanUpResult(Template t) {
            boolean res = super.cleanUpResult (t);
            if (res) {
                cleared++;
            } else {
                dirty++;
            }

            notifyAll ();

            return res;
        }
    }
    public void testResultCanBeGargageCollectedAndClearsTheResult () throws Exception {
        LkpResultCanBeGargageCollectedAndClearsTheResult lkp = new LkpResultCanBeGargageCollectedAndClearsTheResult ();
        assertSize ("24 for AbstractLookup, 8 for two ints", 32, lkp);
        synchronized (lkp) {
            Lookup.Result res = lkp.lookup (new Lookup.Template (getClass ()));
            res.allItems();
            
            WeakReference ref = new WeakReference (res);
            res = null;
            assertGC ("Reference can get cleared", ref);
         
            // wait till we 
            while (lkp.cleared == 0 && lkp.dirty == 0) {
                lkp.wait ();
            }
            
            assertEquals ("No dirty cleanups", 0, lkp.dirty);
            assertEquals ("One final cleanup", 1, lkp.cleared);
        }
        //assertSize ("Everything has been cleaned to original size", 32, lkp);
        
    }
    
    public void testPairCannotBeUsedInMoreThanOneLookupAtOnce () throws Exception {
        /** Simple pair with no data */
        class EmptyPair extends AbstractLookup.Pair {
            protected boolean creatorOf(Object obj) { return false; }
            public String getDisplayName() { return "Empty"; }
            public String getId() { return "empty"; }
            public Object getInstance() { return null; }
            public Class getType() { return Object.class; }
            protected boolean instanceOf(Class c) { return c == getType (); }
        } // end of EmptyPair
        
        AbstractLookup.Content c1 = new AbstractLookup.Content ();
        AbstractLookup.Content c2 = new AbstractLookup.Content ();
        AbstractLookup l1 = new AbstractLookup (c1);
        AbstractLookup l2 = new AbstractLookup (c2);
        
        EmptyPair empty = new EmptyPair ();
        c1.addPair (empty);
        Lookup.Result res = l1.lookup (new Lookup.Template (Object.class));
        assertEquals (
            "Pair is really found", empty, 
            res.allItems ().iterator().next ()
        );
        try {
            c2.addPair (empty);
            fail ("It should not be possible to add pair to two lookups");
        } catch (IllegalStateException ex) {
            // ok, exception is fine
        }
        assertEquals (
            "L2 is still empty", Collections.EMPTY_LIST, 
            new ArrayList (l2.lookup (new Lookup.Template (Object.class)).allItems ())
        );
    }
    
    public void testInitializationCanBeDoneFromAnotherThread () {
        class MyLkp extends AbstractLookup implements Runnable {
            private InstanceContent ic;
            private boolean direct;
            
            public MyLkp (boolean direct) {
                this (direct, new InstanceContent ());
            }
                
            private MyLkp (boolean direct, InstanceContent ic) {
                super (ic);
                this.direct = direct;
                this.ic = ic;
            }
            
            protected @Override void initialize() {
                if (direct) {
                    run ();
                } else {
                    try {
                        Executors.newSingleThreadScheduledExecutor().schedule(this, 0, TimeUnit.MICROSECONDS).get();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (ExecutionException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
            public void run () {
                ic.add (this);
                ic.remove (this);
                ic.set (Collections.nCopies(10, this), null);
                ic.set (Collections.EMPTY_LIST, null);
                ic.add (AbstractLookupTest.this);
            }
        }
        
        assertEquals ("The test should be there", this, new MyLkp (true).lookup (Object.class));
        assertEquals ("and in async mode as well", this, new MyLkp (false).lookup (Object.class));
    }
    
    public void testBeforeLookupIsCalled () {
        class BeforeL extends AbstractLookup {
            public ArrayList list = new ArrayList ();
            public String toAdd;
            public InstanceContent ic;
            
            public BeforeL () {
                this (new InstanceContent ());
            }
            
            private BeforeL (InstanceContent c) {
                super (c);
                this.ic = c;
            }
        
            protected @Override void beforeLookup(Template t) {
                if (toAdd != null) {
                    list.add (0, new SerialPair (toAdd));
                    setPairs (list);
                } else {
                    ic.add (new Integer (1));
                }
            }
        }
        
        BeforeL lookup = new BeforeL ();
        
        lookup.toAdd = "First";
        assertEquals ("First if found", "First", lookup.lookup (String.class));
        
        lookup.toAdd = "2";
        assertEquals ("2 is not first", "2", lookup.lookup (String.class));
        
        Lookup.Result res = lookup.lookup (new Lookup.Template (Object.class));
        for (int i = 3; i < 20; i++) {
            lookup.toAdd = String.valueOf (i);
            assertEquals (i + " items are now there", i, res.allInstances ().size ());
        }
        for (int i = 20; i < 35; i++) {
            lookup.toAdd = String.valueOf (i);
            assertEquals (i + " items are now there", i, res.allItems ().size ());
        }
        
        assertEquals ("Just strings are there now", 1, res.allClasses ().size ());
        lookup.toAdd = null; // this will add integer
        assertEquals ("Two classes now", 2, res.allClasses ().size ());
    }

    public void testInconsistentAfterDeserIssue71744() throws Exception {
        InheritanceTree inhTree = new InheritanceTree();

        AbstractLookup al = new AbstractLookup(new AbstractLookup.Content(), inhTree);
        {

            Collection r = al.lookup(new Lookup.Template(Integer.class)).allInstances();
            assertEquals("None", 0, r.size());
        }

        ICP item = new ICP(10);
        al.addPair(item);
        al.removePair(item);

        AbstractLookup newLookup = (AbstractLookup)reserialize(al);

        newLookup.lookup(Number.class);


        newLookup.addPair(new ICP(Long.valueOf(20L)));

        {

            Collection r = newLookup.lookup(new Lookup.Template(Number.class)).allInstances();
            assertEquals("one", 1, r.size());
/*
            Iterator it = r.iterator();
            assertEquals(10), it.next();
            assertEquals(new Long(20), it.next());*/
        }
    }
    
    public void testRemoveWrongListener() {
        class LL implements LookupListener {
            @Override
            public void resultChanged(LookupEvent ev) {
            }
        }
        LL l1 = new LL();
        LL l2 = new LL();
        
        Object list = AbstractLookup.modifyListenerList(true, l1, null);
        Object newList = AbstractLookup.modifyListenerList(false, l2, list);
        
        assertSame("No change when removing l2 instead of l1", list, newList);
    }

    public void testMatchesIssue130673() {
        class BrokenPairReturningNullID extends Pair<Object> {
            @Override
            protected boolean instanceOf(Class<?> c) {
                return false;
            }

            @Override
            protected boolean creatorOf(Object obj) {
                return false;
            }

            @Override
            public Object getInstance() {
                return null;
            }

            @Override
            public Class<? extends Object> getType() {
                return null;
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public String getDisplayName() {
                return null;
            }
        }
        BrokenPairReturningNullID broken = new BrokenPairReturningNullID();
        
        
        Lookup.Template<String> t = new Lookup.Template<String>(String.class, "ID", null);
        boolean not = AbstractLookup.matches(t, broken, true);
        assertFalse("Does not match the template, but throws no exception", not);
    }
    
    private static final class ICP extends AbstractLookup.Pair {
        private Number s;

        public ICP (Number s) {
            this.s = s;
        }


        protected boolean instanceOf(Class c) {
            return c.isInstance(s);
        }

        protected boolean creatorOf(Object obj) {
            return s == obj;
        }

        public Object getInstance() {
            return s;
        }

        public Class getType() {
            return s.getClass();
        }

        public String getId() {
            return s.toString();
        }

        public String getDisplayName() {
            return getId();
        }

    }
}
