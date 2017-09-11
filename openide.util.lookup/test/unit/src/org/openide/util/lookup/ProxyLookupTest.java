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

package org.openide.util.lookup;

import java.io.Serializable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.implspi.ActiveQueue;

/** Runs all NbLookupTest tests on ProxyLookup and adds few additional.
 */
@SuppressWarnings("unchecked") // XXX ought to be corrected, just a lot of them
public class ProxyLookupTest extends AbstractLookupBaseHid
implements AbstractLookupBaseHid.Impl {
    public ProxyLookupTest(java.lang.String testName) {
        super(testName, null);
    }

    /** Creates an lookup for given lookup. This class just returns 
     * the object passed in, but subclasses can be different.
     * @param lookup in lookup
     * @return a lookup to use
     */
    public Lookup createLookup (Lookup lookup) {
        return createMultiLookup(lookup);
    }

    @Override
    Lookup createMultiLookup(Lookup... all) {
        return new ProxyLookup (all);
    }
    
    public Lookup createInstancesLookup (InstanceContent ic) {
        return new AbstractLookup (ic);
    }
    

    public void clearCaches () {
    }    
    
    
    /** Check whether setLookups method does not fire when there is no
     * change in the lookups.
     */
    public void testProxyListener () {
        ProxyLookup lookup = new ProxyLookup (new Lookup[0]);

        final Lookup.Template<Object> template = new Lookup.Template<Object>(Object.class);
        final Object[] IGNORE = {
            ProxyLookup.ImmutableInternalData.EMPTY,
            ProxyLookup.ImmutableInternalData.EMPTY_ARR,
            ActiveQueue.queue(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptySet()
        };
        
        assertSize("Pretty small", Collections.singleton(lookup), 16, IGNORE);
        
        Lookup.Result<Object> res = lookup.lookup (template);

        assertSize("Bigger", Collections.singleton(lookup), 216, IGNORE);
        
        LL ll = new LL ();
        res.addLookupListener (ll);
        Collection allRes = res.allInstances ();
        
        lookup.setLookups (new Lookup[0]);
        
        if (ll.getCount () != 0) {
           fail ("Calling setLookups (emptyarray) fired a change");
        }
        
        InstanceContent t = new InstanceContent();
        Lookup del = new AbstractLookup (t);
        t.add("Ahoj");
        lookup.setLookups (new Lookup[] { del });
        
        if (ll.getCount () != 1) {
            fail ("Changing lookups did not generate an event");
        }
        
        lookup.setLookups (new Lookup[] { del });
        
        if (ll.getCount () != 0) {
           fail ("Calling setLookups (thesamearray) fired a change");
        }
    }
    
    public void testNoListenersProxyListener () {
        ProxyLookup lookup = new ProxyLookup (new Lookup[0]);
        class E implements Executor {
            Runnable r;
            public void execute(Runnable command) {
                assertNull("NO previous", r);
                r = command;
            }
            public void perform() {
                assertNotNull("We shall have a runnable", r);
                r.run();
                r = null;
            }
        }
        E executor = new E();
                

        final Lookup.Template<Object> template = new Lookup.Template<Object>(Object.class);
        final Object[] IGNORE = {
            ProxyLookup.ImmutableInternalData.EMPTY,
            ProxyLookup.ImmutableInternalData.EMPTY_ARR,
            ActiveQueue.queue(),
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptySet()
        };
        
        assertSize("Pretty small", Collections.singleton(lookup), 16, IGNORE);
        
        Lookup.Result<Object> res = lookup.lookup (template);

        assertSize("Bigger", Collections.singleton(lookup), 216, IGNORE);
        
        LL ll = new LL ();
        res.addLookupListener (ll);
        Collection allRes = res.allInstances ();
        
        lookup.setLookups (executor, new Lookup[0]);
        if (ll.getCount () != 0) {
           fail ("Calling setLookups (emptyarray) fired a change");
        }
        
        InstanceContent t = new InstanceContent();
        Lookup del = new AbstractLookup (t);
        t.add("Ahoj");
        lookup.setLookups (executor, new Lookup[] { del });
        assertEquals("No change yet", 0, ll.getCount());
        executor.perform();
        if (ll.getCount () != 1) {
            fail ("Changing lookups did not generate an event");
        }
        
        lookup.setLookups (executor, new Lookup[] { del });
        if (ll.getCount () != 0) {
           fail ("Calling setLookups (thesamearray) fired a change");
        }
    }

    public void testSetLookups () throws Exception {
        AbstractLookup a1 = new AbstractLookup (new InstanceContent ());
        AbstractLookup a2 = new AbstractLookup (new InstanceContent ());
        
        InstanceContent i3 = new InstanceContent ();
        i3.add (i3);
        AbstractLookup a3 = new AbstractLookup (i3);

        final ProxyLookup p = new ProxyLookup (new Lookup[] { a1, a2 });
        final Lookup.Result res1 = p.lookup (new Lookup.Template (Object.class));
        Collection c1 = res1.allInstances();
        
        Lookup.Result res2 = p.lookup (new Lookup.Template (String.class));
        Collection c2 = res2.allInstances ();
        
        
        assertTrue ("We need two results", res1 != res2);

        final Object blocked = new Object ();

        class L extends Object implements LookupListener {
            public void resultChanged (LookupEvent ev) {
                try {
                    res1.removeLookupListener(this);
                    
                    // waiting for second thread to start #111#
                    blocked.wait ();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    fail ("An exception occured ");
                }
            }
        }
        
        final L listener1 = new L ();
        res1.addLookupListener (listener1);
        

        Runnable newLookupSetter = new Runnable() {
            public void run () {
                synchronized (blocked) {
                    try {
                        p.setLookups (new Lookup[0]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        fail ("setLookups failed.");
                    } finally {
                        // starts the main thread #111#
                        blocked.notify ();
                    }
                }
            }
        };
        
        synchronized (blocked) {
            new Thread (newLookupSetter).start ();
            
            p.setLookups (new Lookup[] { a1, a2, a3 });
        }
    }
    
    public void testProxyLookupTemplateCaching(){
        Lookup lookups[] = new Lookup[1];
        doProxyLookupTemplateCaching(lookups, false);
    }
    
    public void testProxyLookupTemplateCachingOnSizeTwoArray() {
        Lookup lookups[] = new Lookup[2];
        lookups[1] = Lookup.EMPTY;
        doProxyLookupTemplateCaching(lookups, false);
    }
    public void testProxyLookupShallNotAllowModificationOfGetLookups(){
        Lookup lookups[] = new Lookup[1];
        doProxyLookupTemplateCaching(lookups, true);
    }
    
    public void testProxyLookupShallNotAllowModificationOfGetLookupsOnSizeTwoArray() {
        Lookup lookups[] = new Lookup[2];
        lookups[1] = Lookup.EMPTY;
        doProxyLookupTemplateCaching(lookups, true);
    }
    
    public void testIterativeIterator() {
        InstanceContent ic = new InstanceContent();
        AbstractLookup l1 = new AbstractLookup(ic);
        
        class AL extends AbstractLookup {
            @Override
            protected void beforeLookup(Template<?> template) {
                fail("Don't call me! Called for: " + template.getType());
            }
        }
        AL l2 = new AL();
        
        ProxyLookup pl = new ProxyLookup(l1, l2);
        
        ic.add(Integer.valueOf(10));
        
        for (Integer i : pl.lookupAll(Integer.class)) {
            assertEquals(Integer.valueOf(10), i);
            break;
        }
    }
    
    public void testConfuseIterativeIterator() {
        InstanceContent ic1 = new InstanceContent();
        AbstractLookup l1 = new AbstractLookup(ic1);
        InstanceContent ic2 = new InstanceContent();
        AbstractLookup l2 = new AbstractLookup(ic2);
        InstanceContent ic3 = new InstanceContent();
        AbstractLookup l3 = new AbstractLookup(ic3);
        
        ProxyLookup pl = new ProxyLookup(l1, l2, l3);
        Result<Number> res = pl.lookupResult(Number.class);
        
        ic1.add(1);
        ic2.add(2f);
        ic3.add(3d);
        
        int cnt = 0;
        for (Number n : res.allInstances()) {
            cnt += n.intValue();
        }
        assertEquals("Six", 6, cnt);
        final Collection<? extends Number> all = res.allInstances();
        assertEquals("Three numbers: " + all, 3, all.size());
    }
    
    /** Index 0 of lookups will be modified, the rest is up to the 
     * setup code.
     */
    private void doProxyLookupTemplateCaching(Lookup[] lookups, boolean reget) {
        // Create MyProxyLookup with one lookup containing the String object
        InstanceContent inst = new InstanceContent();
        inst.add(new String("Hello World")); //NOI18N
        lookups[0] = new AbstractLookup(inst);
        ProxyLookup proxy = new ProxyLookup(lookups);
        if (reget) {
            lookups = proxy.getLookups();
        }
        
        // Performing template lookup for String object
        Lookup.Result result = proxy.lookup(new Lookup.Template(String.class, null, null));
        int stringTemplateResultSize = result.allInstances().size();
        assertEquals ("Ensure, there is only one instance of String.class in proxyLookup:", //NOI18N
                1, stringTemplateResultSize);
        
        // Changing lookup in proxy lookup, now it will contain 
        // StringBuffer Object instead of String
        InstanceContent ic2 = new InstanceContent();
        ic2.add(new Integer(1234567890));
        lookups[0] = new AbstractLookup(ic2);
        proxy.setLookups(lookups);
        
        assertEquals ("the old result is updated", 0, result.allInstances().size());

        // Instance of String.class should not appear in proxyLookup
        Lookup.Result r2 = proxy.lookup(new Lookup.Template(String.class, null, null));
        assertEquals ("Instance of String.class should not appear in proxyLookup:", //NOI18N
                0, r2.allInstances().size());

        Lookup.Result r3 = proxy.lookup(new Lookup.Template(Integer.class, null, null));
        assertEquals ("There is only one instance of Integer.class in proxyLookup:", //NOI18N
                1, r3.allInstances().size());
    }
    
    public void testListeningAndQueryingByTwoListenersInstancesSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 1);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 1);        
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 1);
    }
    
    public void testListeningAndQueryingByTwoListenersInstancesSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 2);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 2);        
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups2() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 2);
    }
    public void testListeningAndQueryingByTwoListenersInstancesSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(0, 22);
    }
    public void testListeningAndQueryingByTwoListenersClassesSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(1, 22);        
    }
    public void testListeningAndQueryingByTwoListenersItemsSetLookups22() {
        doListeningAndQueryingByTwoListenersSetLookups(2, 22);
    }
    
    private void doListeningAndQueryingByTwoListenersSetLookups(final int type, int depth) {
        ProxyLookup orig = new ProxyLookup();
        ProxyLookup on = orig;
        
        while (--depth > 0) {
            on = new ProxyLookup(new Lookup[] { on });
        }
        
        
        final ProxyLookup lookup = on;
        
        class L implements LookupListener {
            Lookup.Result integer = lookup.lookup(new Lookup.Template(Integer.class));
            Lookup.Result number = lookup.lookup(new Lookup.Template(Number.class));
            Lookup.Result serial = lookup.lookup(new Lookup.Template(Serializable.class));
            
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
        ArrayList arr = new ArrayList();
        for(int i = 0; i < 100; i++) {
            arr.add(new Integer(i));
            
            orig.setLookups(new Lookup[] { Lookups.fixed(arr.toArray()) });
        }
        
        assertEquals("3x100+1 checks", 301, listener.round);
    }

    static Object holder;
    
    public void testProxyWithLiveResultCanBeCollected() {
        Lookup layer0 = Lookups.singleton("Hello");
        Lookup layer1 = new ProxyLookup(new Lookup[] { layer0 });
        Lookup layer2 = new ProxyLookup(new Lookup[] { layer1 });
        Lookup.Result result1 = layer1.lookup(new Lookup.Template(String.class));

        assertEquals("One instance", 1, result1.allInstances().size());

        // this will create ProxyLookup$R which listens on origResult
        Lookup.Result result2 = layer2.lookup(new Lookup.Template(String.class));
        
        // this line is necessary. W/o actually querying the result,
        // it will nether compute it nor attach the listener.
        assertEquals("One instance", 1, result2.allInstances().size());
        
        result2.addLookupListener(new LookupListener() {
            public void resultChanged(LookupEvent ev) {}
        });
        
        Reference ref = new WeakReference(layer2);
        layer2 = null;
        result2 = null;
        try {
            holder = result1;
            assertGC ("The proxy lookup not been garbage collected!", ref);
        } finally {
            holder = null;
        }
    }
    
    public void testArrayIndexAsInIssue119292() throws Exception {
        final ProxyLookup pl = new ProxyLookup();
        final int[] cnt = { 0 };
        
        class L extends Lookup {
            L[] set;
            Lookup l;
            
            public L(String s) {
                l = Lookups.singleton(s);
            }
            
            @Override
            public <T> T lookup(Class<T> clazz) {
                return l.lookup(clazz);
            }

            @Override
            public <T> Result<T> lookup(Template<T> template) {
                return l.lookup(template);
            }

            @Override
            @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
            public boolean equals(Object obj) {
                if (set != null) {
                    cnt[0]++;
                    pl.setLookups(set);
                }
                return super.equals(obj);
            }

            @Override
            public int hashCode() {
                int hash = 3;
                return hash;
            }
        }

        Result<String> res = pl.lookupResult(String.class);
        assertEquals(Collections.EMPTY_LIST, res.allItems());
        
        L[] old = { new L("A"), new L("B") };
        L[] now = { new L("C") };
        
        pl.setLookups(old);
        cnt[0] = 0;
        
        old[0].set = new L[0];
        pl.setLookups(now);

        assertEquals("No call to equals", 0, cnt[0]);
        
        assertEquals("Still assigned to C", Collections.singletonList("C"), res.allInstances());
    }
    
    public void testArrayIndexWithAddRemoveListenerAsInIssue119292() throws Exception {
        final ProxyLookup pl = new ProxyLookup();
        final int[] cnt = { 0 };
        
        class L extends Lookup {
            L[] set;
            Lookup l;
            
            public L(String s) {
                l = Lookups.singleton(s);
            }
            
            @Override
            public <T> T lookup(Class<T> clazz) {
                return l.lookup(clazz);
            }

            @Override
            public <T> Result<T> lookup(Template<T> template) {
                Result<T> r = l.lookup(template);
                return new R<T>(r);
            }

            final class R<T> extends Result<T> {
                private Result<T> delegate;

                public R(Result<T> delegate) {
                    this.delegate = delegate;
                }
                
                @Override
                public void addLookupListener(LookupListener l) {
                    cnt[0]++;
                    if (set != null) {
                        pl.setLookups(set);
                    }
                    delegate.addLookupListener(l);
                }

                @Override
                public void removeLookupListener(LookupListener l) {
                    cnt[0]++;
                    if (set != null) {
                        pl.setLookups(set);
                    }
                    delegate.removeLookupListener(l);
                }

                @Override
                public Collection<? extends T> allInstances() {
                    return delegate.allInstances();
                }
            }
        }

        Result<String> res = pl.lookupResult(String.class);
        assertEquals(Collections.EMPTY_LIST, res.allItems());
        
        L[] old = { new L("A"), new L("B") };
        L[] now = { new L("C") };
        
        pl.setLookups(old);
        cnt[0] = 0;
        
        old[0].set = new L[0];
        pl.setLookups(now);

        if (cnt[0] == 0) {
            fail("There should be calls to listeners");
        }
        
        assertEquals("C is overriden from removeLookupListener", Collections.emptyList(), res.allInstances());
    }
    
    
    public void testArrayIndexWithSetLookupAsInIssue123679() throws Exception {
        final ProxyLookup pl = new ProxyLookup();
        final int[] cnt = { 0 };
        
        class L extends Lookup {
            L[] set;
            Lookup l;
            Collection<? extends Serializable> res;
            
            public L(String s) {
                l = Lookups.singleton(s);
            }
            
            @Override
            public <T> T lookup(Class<T> clazz) {
                return l.lookup(clazz);
            }

            @Override
            public <T> Result<T> lookup(Template<T> template) {
                cnt[0]++;
                if (set != null) {
                    pl.setLookups(set);
                    res = pl.lookupAll(Serializable.class);
                }
                Result<T> r = l.lookup(template);
                return r;
            }
        }

        L[] now = { new L("A"), new L("B") };
        L[] old = { new L("C") };
        pl.setLookups(old);
        old[0].set = now;
        
        Result<String> res = pl.lookupResult(String.class);
        assertEquals("New items visible", 2, res.allItems().size());
        
        
        pl.setLookups(new L("X"), new L("Y"), new L("Z"));
    }
    
    public void testDuplicatedLookupArrayIndexWithSetLookupAsInIssue123679() throws Exception {
        final ProxyLookup pl = new ProxyLookup();
        final int[] cnt = { 0 };
        
        class L extends Lookup {
            L[] set;
            Lookup l;
            Collection<? extends Serializable> res;
            
            public L(String s) {
                l = Lookups.singleton(s);
            }
            
            @Override
            public <T> T lookup(Class<T> clazz) {
                return l.lookup(clazz);
            }

            @Override
            public <T> Result<T> lookup(Template<T> template) {
                cnt[0]++;
                if (set != null) {
                    pl.setLookups(set);
                    res = pl.lookupAll(Serializable.class);
                }
                Result<T> r = l.lookup(template);
                return r;
            }
        }

        L dupl = new L("A");
        L[] now = { dupl };
        L[] old = { new L("C") };
        pl.setLookups(old);
        old[0].set = now;
        
        Result<String> res = pl.lookupResult(String.class);
        assertEquals("New items visible", 1, res.allItems().size());
        
        
        pl.setLookups(old);
    }
    public void testFrequentSwitching() {
        Object o1 = new Object();
        Object o2 = new Object();
        String s1 = new String("foo");
        String s2 = new String("bar");

        Lookup l1 = Lookups.fixed(o1, s1);
        Lookup l2 = Lookups.fixed(o2, s2);

        ProxyLookup lookup = new ProxyLookup(new Lookup[0]);

        Lookup.Result<Object> res1 = lookup.lookupResult(Object.class);
        Lookup.Result<String> res2 = lookup.lookupResult(String.class);

        assertSize("Lookup is small", 1500, lookup);

        for (int i = 0; i < 100; i++) {
            lookup.setLookups(l1);
            lookup.setLookups(l2);
        }
        assertSize("Lookup has grown too much", 1500, lookup);
    }
}
