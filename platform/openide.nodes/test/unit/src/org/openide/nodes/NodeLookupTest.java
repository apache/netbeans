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

package org.openide.nodes;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Tests whether notification to NodeListener is fired under Mutex.writeAccess
 *
 * @author Jaroslav Tulach
 */
public class NodeLookupTest extends NbTestCase {
    public NodeLookupTest(String name) {
        super(name);
    }
    
    public void testChangesAreFiredFromLookup () {
        CountableLookup lkp = new CountableLookup ();
        Node node = new AbstractNode (createChildren (), lkp);

        checkGetNodesDoesNotInitializeLookup (node, lkp.queries);
        checkInstanceInGetCookie (new Node.Cookie () {}, lkp.ic, node);
        checkInstanceInGetLookup (new Node.Cookie () {}, lkp.ic, node, true);
        checkInstanceInGetLookup ("Some string", lkp.ic, node, true);
    }

    public void testChangesAreFiredFromLookupThruFilterNode () {
        CountableLookup lkp = new CountableLookup ();
        Node node = new FilterNode (new AbstractNode (createChildren (), lkp));

        checkGetNodesDoesNotInitializeLookup (node, lkp.queries);
        //checkInstanceInGetCookie (new Node.Cookie () {}, ic, node);
        checkInstanceInGetLookup (new Node.Cookie () {}, lkp.ic, node, true);
        checkInstanceInGetLookup ("Some string", lkp.ic, node, true);
    }
    
    public void testChangesAreFiredFromLookupAssociatedToFilterNode () {
        CountableLookup lkp = new CountableLookup ();
        Node node = new FilterNode (Node.EMPTY, Children.LEAF, lkp);

        checkGetNodesDoesNotInitializeLookup (node, lkp.queries);
        checkInstanceInGetCookie (new Node.Cookie () {}, lkp.ic, node);
        checkInstanceInGetLookup (new Node.Cookie () {}, lkp.ic, node, true);
        checkInstanceInGetLookup ("Some string", lkp.ic, node, true);
    }

    public void testChangesAreFiredFromLookupThruFilterNodeWithOverWrittenGetCookie () {
        final Node.Cookie myInstance = new Node.Cookie () { };
        final ArrayList queries = new ArrayList ();
        
        InstanceContent ic = new InstanceContent ();
        AbstractLookup lookup = new AbstractLookup (ic);
        Node node = new FilterNode (new AbstractNode (createChildren (), lookup)) {
            public Node.Cookie getCookie (Class clazz) {
                queries.add (clazz);
                
                if (clazz == myInstance.getClass ()) {
                    return myInstance;
                }
                return super.getCookie (clazz);
            }
        };

        checkGetNodesDoesNotInitializeLookup (node, queries);
        checkInstanceInGetCookie (new Node.Cookie () {}, ic, node);
        checkInstanceInGetLookup (new Node.Cookie () {}, ic, node, true);
        // by overwriting the FilterNode.getCookie we disable enhanced support
        // for non-cookie objects in original lookup
        checkInstanceInGetLookup ("Some string", ic, node, false);
        
        assertEquals ("It is possible to get myInstance from getCookie", myInstance, node.getCookie (myInstance.getClass ()));
        assertEquals ("It also possible to get it from getLookup", myInstance, node.getLookup ().lookup (myInstance.getClass ()));
    }
    
    private void checkInstanceInGetCookie (Node.Cookie obj, InstanceContent ic, Node node) {
        assertNull("The node does not contain the object yet", node.getCookie(obj.getClass()));
        
        Listener listener = new Listener ();
        node.addNodeListener(listener);
        
        ic.add (obj);
        listener.assertEvents ("One change in node", 1, -1);

        assertEquals("Can access cookie in the content", obj, node.getCookie(obj.getClass()));

        ic.remove (obj);
        listener.assertEvents ("One change in node", 1, -1);
    }
    
    private void checkInstanceInGetLookup (Object obj, InstanceContent ic, Node node, boolean shouldBeThere) {
        Listener listener = new Listener ();
        Lookup.Result res = node.getLookup().lookupResult(obj.getClass());
        Collection ignore = res.allItems ();
        res.addLookupListener(listener);

        ic.add (obj);
        if (shouldBeThere) {
            listener.assertEvents ("One change in node's lookup", -1, 1);
            assertEquals ("Can access object in content via lookup", obj, node.getLookup ().lookup (obj.getClass ()));
        } else {
            assertNull ("Cannot access object in content via lookup", node.getLookup ().lookup (obj.getClass ()));
        }
            
        
        ic.remove (obj);
        if (shouldBeThere) {
            listener.assertEvents ("One change in node's lookup", -1, 1);
        }
        assertNull ("Cookie is removed", node.getLookup ().lookup (obj.getClass ()));
    }
    
    public void testNoPropertyChangeWhenQueryingForAnExistingCookieBug40734 () throws Exception {
        class MyN extends AbstractNode {
            MyN () {
                super (Children.LEAF);
            }
            
            private Node.Cookie my = new Node.Cookie () {
                public String toString () {
                    return "PlainCookie";
                }
            };
            private Node.Cookie save = new SaveCookie () {
                public void save () {
                }
        
                public String toString () {
                    return "SaveCookie";
                }
            };
            public Node.Cookie getCookie (Class c) {
                if (c == Node.Cookie.class) return my;
                if (c == SaveCookie.class) return save;
                
                return null;
            }
            
            public String toString () {
                return "Node";
            }
        };
        FilterNode fn = new FilterNode (new MyN ());
        
        class L extends NodeAdapter implements LookupListener {
            public String ev;
            public org.openide.util.LookupEvent lookup;
            
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                this.ev = ev.getPropertyName ();
            }
            
            public void resultChanged (org.openide.util.LookupEvent ev) {
                lookup = ev;
            }
        }
        L l = new L ();
        
        fn.addNodeListener (l);
        Lookup.Result res = fn.getLookup().lookupResult(Node.Cookie.class);
        assertEquals ("No event fired0", null, l.ev);
        res.addLookupListener (l);
        Collection items = res.allItems ();
        if (1 != items.size ()) {
            fail ("Wrong items: " + items + " instances: " + res.allInstances ());
        }
        assertEquals ("No event fired1", null, l.ev);
        
        Lookup.Template templSave = new Lookup.Template (SaveCookie.class);
        assertNotNull ("The save cookie is there", fn.getLookup ().lookupItem (templSave));
        assertEquals ("No event fired2", null, l.ev);
        
        assertNotNull ("There is change in all cookies, so event should be fired", l.lookup);
        items = res.allItems ();
        if (2 != items.size ()) {
            fail ("Wrong items: " + items + " instances: " + res.allInstances ());
        }
        
        assertEquals ("No event fired3", null, l.ev);
    }
    
    
    //
    // Test to see correct behaviour from getCookie to lookup
    //
    
    public void testNodeIsInItsLookup () {
        CookieNode n = new CookieNode ();
        assertEquals ("Node is there", n, n.getLookup ().lookup (Node.class));
    }
    
    public void testFilterNodeWithOverridenGetCookieIsInTheLookup () {
        doTestFilterNodeWithOverridenGetCookieIsInTheLookup (new CookieNode (), false);
    }
    
    public void testFilterNodeWithOverridenGetCookieIsInTheLookupWitSaveCookieWithoutQuery () {
        doTestFilterNodeWithOverridenGetCookieIsInTheLookup (new CookieNodeWithCookie (), false);
    }
    
    public void testFilterNodeWithOverridenGetCookieIsInTheLookupWitSaveCookie () {
        doTestFilterNodeWithOverridenGetCookieIsInTheLookup (new CookieNodeWithCookie (), true);
    }
    private void doTestFilterNodeWithOverridenGetCookieIsInTheLookup (CookieNode n, boolean queryForCookie) {
        final ArrayList queries = new ArrayList ();
        class MyFN extends FilterNode {
            public MyFN (Node n) {
                super (n);
            }
            
            public Node.Cookie getCookie (Class clazz) {
                assertFalse("Don't hold locks while querying nodes", Thread.holdsLock(getLookup()));
                queries.add (clazz);
                return super.getCookie (clazz);
            }
        }
        
        FilterNode fn = new MyFN (n);
        checkGetNodesDoesNotInitializeLookup (fn, queries);
        checkGetNodesDoesNotInitializeLookup (fn, n.queries);
        
        Lookup l = fn.getLookup ();
        
        if (queryForCookie) {
            l.lookup (SaveCookie.class);
        }
        
        // == must be used instead of equals for nodes!!!
        assertTrue ("Node is there", fn == l.lookup (Node.class));
        Collection c = l.lookupAll(Node.class);
        
        if (!queryForCookie) {
            assertEquals ("Just one node", 1, c.size ());
        } else {
            assertEquals ("The cookie is implemented by the node, thus there are two nodes", 2, c.size ());
        }
        assertTrue ("And the first is the filter", c.iterator ().next () == fn);
    }
    
    public void testFilterNodeThatDoesNotOverrideGetCookie () {
        doTestFilterNodeThatDoesNotOverrideGetCookie (new CookieNode ());
    }
    
    public void testFilterNodeThatDoesNotOverrideGetCookieWithSaveCookie () {
        doTestFilterNodeThatDoesNotOverrideGetCookie (new CookieNodeWithCookie ());
    }
        
    private void doTestFilterNodeThatDoesNotOverrideGetCookie (CookieNode n) {
        FilterNode fn = new FilterNode (n);
        checkGetNodesDoesNotInitializeLookup (fn, n.queries);
        
        Lookup l = fn.getLookup ();
        
        // == must be used instead of equals for nodes!!!
        assertTrue ("Node is there", fn == l.lookup (Node.class));
        Collection c = l.lookupAll(Node.class);
        assertEquals ("Just one node", 1, c.size ());
        assertTrue ("And it is the one", c.iterator ().next () == fn);
    }
    
    public void testChangeInObjectVisibleInLookup () {
        CookieNode n = new CookieNode ();
        n.setSet(CookieSet.createGeneric(null));
        checkInstanceInLookup (new Integer(1), n.cookieSet(), n.getLookup ());
        checkInstanceInLookup (new Node.Cookie() {}, n.cookieSet(), n.getLookup ());
    }
    public void testChangeInCookieVisibleInLookup () {
        CookieNode n = new CookieNode ();
        checkInstanceInLookup (new Node.Cookie() {}, n.cookieSet(), n.getLookup ());
    }

    public void testChangeInCookieVisibleInLookupThruFilterNode () {
        CookieNode n = new CookieNode ();
        FilterNode f = new FilterNode (n);
        checkInstanceInLookup (new Node.Cookie() {}, n.cookieSet(), f.getLookup ());
    }

    public void testChangeInObjectVisibleInLookupThruFilterNode () {
        CookieNode n = new CookieNode ();
        n.setSet(CookieSet.createGeneric(null));
        FilterNode f = new FilterNode (n);
        checkInstanceInLookup (new Node.Cookie() {}, n.cookieSet(), f.getLookup ());
        checkInstanceInLookup (new Integer(2), n.cookieSet(), f.getLookup ());
    }
    
    public void testChangeInCookieVisibleInLookupThruFilterNodeWhenItOverridesGetCookie () {
        CookieNode n = new CookieNode ();
        
        MyFilterNode f = new MyFilterNode (n, false);
        
        checkInstanceInLookup (new Node.Cookie() {}, n.cookieSet(), f.getLookup ());
        checkInstanceInLookup (new Node.Cookie() {}, f.set, f.getLookup ());
    }
    public void testChangeInObjectVisibleInLookupThruFilterNodeWhenItOverridesGetCookie () {
        CookieNode n = new CookieNode ();
        n.setSet(CookieSet.createGeneric(null));
                
        MyFilterNode f = new MyFilterNode (n, true);
        
        checkInstanceInLookup (new Integer(3), n.cookieSet(), f.getLookup ());
        checkInstanceInLookup (new Integer(4), f.set, f.getLookup ());
    }
    
    public void testFilterNodeDelegatesCorrectly () {
        class CN extends CookieNode implements SaveCookie {
            public CN () {
                getCookieSet ().add (this);
            }
            public void save () {
            }
        }
        
        CN save = new CN ();
        FilterNode node = new FilterNode (save);
        
        Class[] classs={ SaveCookie.class, CN.class };
        
        for( int i = 0; i < classs.length; i++ ) {
            Lookup.Template t = new Lookup.Template (classs[i]);
            Object cookie = node.getLookup ().lookup (t.getType ());
            assertTrue ("it is the right class: " + classs[i] + " : " + cookie, classs[i].isInstance(cookie));
            assertTrue (cookie + " == " + save, cookie == save);

            Lookup.Item item = node.getLookup ().lookupItem (t);
            assertTrue ("Should be " + classs[i] + " is " + item.getType(), classs[i].isAssignableFrom(item.getType()));
            assertTrue ("value is the same", item.getInstance() == save);

            Collection c = node.getLookup ().lookup (t).allInstances ();
            assertEquals ("One save cookie", 1, c.size ());
            assertTrue ("It is the cookie", c.iterator ().next () == save);

            c = node.getLookup ().lookup (t).allItems ();
            assertEquals ("One save cookie", 1, c.size ());
            item = (Lookup.Item)c.iterator().next ();
            assertTrue ("Should be " + classs[i] + " is " + item.getType(), classs[i].isAssignableFrom(item.getType()));
            assertTrue ("value is the same", item.getInstance() == save);
        }
    }
    
    private void checkInstanceInLookup (Object obj, CookieSet ic, Lookup l) {
        Listener listener = new Listener ();
        Lookup.Result res = l.lookupResult(Object.class);
        Collection justToEnsureChangesToListenerWillBeFired = res.allItems ();
        res.addLookupListener(listener);
        
        ic.assign(obj.getClass(), obj);
        listener.assertEvents ("One change in lookup", -1, 1);

        assertEquals ("Can access cookie in the content", obj, l.lookup (obj.getClass ()));

        ic.assign(obj.getClass());
        listener.assertEvents ("One change in lookup", -1, 1);
        
        ic.assign(obj.getClass(), obj);
        listener.assertEvents ("One change in lookup", -1, 1);

        assertEquals ("Can access cookie in the content", obj, l.lookup (obj.getClass ()));

        ic.assign(obj.getClass());
        listener.assertEvents ("One change in lookup", -1, 1);
    }
    
    private static void checkGetNodesDoesNotInitializeLookup (final org.openide.nodes.Node n, java.util.List queried) {
        assertEquals ("No queries before", Collections.EMPTY_LIST, queried);
        
        class MyCh extends Children.Keys {
            protected void addNotify () {
                setKeys (java.util.Collections.singleton(n));
            }
            
            public void clear () {
                setKeys (java.util.Collections.EMPTY_LIST);
            }
            
            protected Node[] createNodes (Object key) {
                return new Node[] { n };
            }
        };
        MyCh ch = new MyCh ();     
        
        // initialize the node N
        new AbstractNode (ch).getChildren().getNodes ();
        
        assertEquals ("No queries after", Collections.EMPTY_LIST, queried);
        ch.clear ();
        assertEquals ("No queries after clean either", Collections.EMPTY_LIST, queried);
        
    }
    
    //
    // Garbage collect
    //
    
    public void testAbstractNodeWithoutLookupHasCookieSet () {
        CookieNode n = new CookieNode ();
        try {
            n.cookieSet ();
        } catch (RuntimeException ex) {
            fail ("cannot obtain cookie set");
        }
    }
    
    public void testAbstractNodeWithLookupDoesNotHaveCookieSet () {
        CookieNode n = new CookieNode (Lookup.EMPTY);
        try {
            n.cookieSet ();
            fail ("It should not be possible to obtain cookieSet it should throw an exception");
        } catch (RuntimeException ex) {
        }
        try {
            n.setSet (null);
            fail ("It should not be possible to obtain setCookieSet it should throw an exception");
        } catch (RuntimeException ex) {
        }
    }
    
    
    public void testBackwardCompatibleAbstractNodeLookupCanBeGarbageCollected () {
        AbstractNode n = new AbstractNode (createChildren ());
        
        Lookup l = n.getLookup ();
        assertEquals ("Two invocations share the same lookup", l, n.getLookup ());
        
        java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (l);
        l = null;
        assertGC ("Lookup can be GCed", ref);
    }        

    public void testLazyCookieSet35856 () {
        final CookieNode n = new CookieNode ();
        class CF implements CookieSet.Factory, org.openide.cookies.OpenCookie, org.openide.cookies.EditCookie, org.openide.cookies.ViewCookie {
            public int cnt;
            public Node.Cookie createCookie(Class klass) {
                cnt++;
                assertFalse("Don't hold locks while querying nodes", Thread.holdsLock(n.getLookup()));
                return this;
            }
                
            public void open () {}
            public void edit () {}
            public void view () {}
        }
        CF cf = new CF ();
        // init the node
        n.cookieSet ();
        
        Lookup lookup = n.getLookup ();
        Lookup.Item item;
        Listener l = new Listener ();
        Lookup.Result res = lookup.lookupResult(Node.Cookie.class);
        assertEquals ("Empty", 0, res.allItems ().size ());
        res.addLookupListener(l);
        
        n.cookieSet ().add (org.openide.cookies.OpenCookie.class, cf);
        l.assertEvents ("Changes in lookup as we added Open", -1, 1);
        assertEquals ("One", 1, res.allItems ().size ());
        assertEquals ("Nothing created", 0, cf.cnt);
        
        n.cookieSet ().add (org.openide.cookies.ViewCookie.class, cf);
        item = n.getLookup ().lookupItem (new Lookup.Template (org.openide.nodes.Node.Cookie.class));
        assertEquals ("First is still OpenCookie", org.openide.cookies.OpenCookie.class, item.getType ());
        assertEquals ("Still one result as we do not listen on ViewCookie", 1, res.allItems ().size ());
        l.assertEvents ("Added View, but we are not listening to it", -1, 0);
        item = n.getLookup ().lookupItem (new Lookup.Template (org.openide.cookies.ViewCookie.class));
        l.assertEvents ("Now we listen and here is a change", -1, 1);
        assertEquals ("Included in result", 2, res.allItems ().size ());
        assertEquals ("Included as second", org.openide.cookies.ViewCookie.class, ((Lookup.Item)res.allItems ().toArray ()[1]).getType ());
        assertEquals ("First remain OpenCookie", org.openide.cookies.OpenCookie.class, ((Lookup.Item)res.allItems ().toArray ()[0]).getType ());
        assertEquals ("Still nothing created by factory", 0, cf.cnt);
        
        
        n.cookieSet ().add (org.openide.cookies.EditCookie.class, cf);
        l.assertEvents ("No change yet", -1, 0);
        assertNotNull ("This triggers the EditCookie listening and creates instance", n.getLookup ().lookup (org.openide.cookies.EditCookie.class));
        l.assertEvents ("Another change", -1, 1);
        assertEquals ("One entry created", 1, cf.cnt);
        assertEquals ("Included in result", 3, res.allItems ().size ());
        Lookup.Item[] arr = (Lookup.Item[])res.allItems ().toArray (new Lookup.Item[0]);
        assertEquals ("Included as second", org.openide.cookies.ViewCookie.class, (arr[1]).getType ());
        assertEquals ("First remain OpenCookie", org.openide.cookies.OpenCookie.class, (arr[0]).getType ());
        assertEquals ("Edit is last", org.openide.cookies.EditCookie.class, (arr[2]).getType ());
        
        n.cookieSet ().remove (org.openide.cookies.OpenCookie.class, cf);
        l.assertEvents ("This is a change for sure", -1, 1);
        assertEquals ("Just two there", 2, res.allItems ().size ());
        assertEquals ("View is first", org.openide.cookies.ViewCookie.class, ((Lookup.Item)res.allItems ().toArray ()[0]).getType ());
        assertEquals ("Edit is second", org.openide.cookies.EditCookie.class, ((Lookup.Item)res.allItems ().toArray ()[1]).getType ());
        
    }

    /**
     * Test for bug 250817: Slowness and OutOfMemoryError when selecting 100+
     * nodes.
     *
     * @throws Exception
     */
    public void testBug250817() throws Exception {
        final CookieNode n = new CookieNode();
        class CF implements CookieSet.Factory, org.openide.cookies.OpenCookie,
                org.openide.cookies.ViewCookie {

            @Override
            public Node.Cookie createCookie(Class klass) {
                assertFalse("Don't hold locks while querying nodes",
                        Thread.holdsLock(n.getLookup()));
                return this;
            }

            @Override
            public void open() {
            }

            @Override
            public void view() {
            }
        }
        CF cf = new CF();
        // init the node
        n.cookieSet();

        Lookup lookup = n.getLookup();

        final Listener l = new Listener();
        Lookup.Result res = lookup.lookupResult(Node.Cookie.class);
        assertEquals("Empty", 0, res.allItems().size());
        res.addLookupListener(l);

        n.cookieSet().add(org.openide.cookies.OpenCookie.class, cf);
        l.assertEvents("Changes in lookup as we added Open", -1, 1);

        n.cookieSet().add(org.openide.cookies.ViewCookie.class, cf);

        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Lookup.Item item;
                item = n.getLookup().lookupItem(new Lookup.Template(
                        org.openide.cookies.ViewCookie.class));
                l.assertEvents("Firing of change postponed", -1, 0);
            }
        });

        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                l.assertEvents("Postponed event delivered", -1, 1);
            }
        });
    }

    public void testItIsPossibleToWrapACookieSet () {
        final int[] cnt = { 0, 0 };
        CookieNode n = new CookieNode () {
            {
                class F implements CookieSet.Factory, org.openide.cookies.OpenCookie {
                    public Node.Cookie createCookie(Class klass) {
                        return this;
                    }

                    public void open () {
                        cnt[1]++;
                    }
                }
                getCookieSet ().add (org.openide.cookies.OpenCookie.class, new F ());
            }
            @Override
            public Node.Cookie getCookie (Class c) {
                if (org.openide.cookies.OpenCookie.class == c) {
                    return new org.openide.cookies.OpenCookie () {
                        public void open () {
                            Object s = getCookieSet ().getCookie (org.openide.cookies.OpenCookie.class);
                            cnt[0]++;
                            ((org.openide.cookies.OpenCookie)s).open ();
                        }
                    };
                }
                return super.getCookie (c);
            }
        };
        
        Lookup.Item item = n.getLookup().lookupItem (new Lookup.Template (org.openide.cookies.OpenCookie.class));
        assertNotNull (item);
        assertEquals ("No factory", 0, cnt[1]);
        assertEquals ("No call", 0, cnt[0]);
        
        org.openide.cookies.OpenCookie oc = (org.openide.cookies.OpenCookie)item.getInstance ();
        assertEquals ("No factory", 0, cnt[1]);
        assertEquals ("No call", 0, cnt[0]);
        
        oc.open ();
        assertEquals ("Once factory", 1, cnt[1]);
        assertEquals ("Once call", 1, cnt[0]);
        
    }
    
    public void testGetInstancesWorksFineOnNodeWithCookieSet () throws Exception {
        doGetInstancesWorksFineOnNodeWithCookieSet (false);
    }
    
    public void testGetInstancesWorksFineOnNodeWithCookieSetWhenAskedBefore () throws Exception {
        doGetInstancesWorksFineOnNodeWithCookieSet (true);
    }
    
    private void doGetInstancesWorksFineOnNodeWithCookieSet (boolean askBefore) throws Exception {
        CookieNode n = new CookieNode ();
        
        
        class X implements SaveCookie {
            public void save () {}
        }
        
        class Y implements org.openide.cookies.OpenCookie {
            public void open () {}
        }
        
        class Z implements org.openide.cookies.EditCookie {
            public void edit () {}
        }
        Class[] expect = { 
            org.openide.cookies.OpenCookie.class,
            org.openide.cookies.EditCookie.class,
            org.openide.cookies.SaveCookie.class
        };
        
        n.cookieSet ().add (new X ());
        
        class Fact implements CookieSet.Factory {
            public Node.Cookie createCookie (Class c) {
                if (c == org.openide.cookies.OpenCookie.class) {
                    return new Y ();
                }
                if (c == org.openide.cookies.EditCookie.class) {
                    return new Z ();
                }
                return null;
            }
        }
        n.cookieSet ().add (org.openide.cookies.OpenCookie.class, new Fact ());
        n.cookieSet ().add (new Class[] { org.openide.cookies.EditCookie.class }, new Fact ());
        
        
        if (askBefore) {
            for (int i = 0; i < expect.length; i++) {
                assertNotNull ("Class " + expect[i], n.getLookup().lookup (expect[i]));
            }
        }
        
        Lookup.Template all = new Lookup.Template (Object.class);
        Set s = n.getLookup ().lookup (all).allClasses();
        assertTrue ("Contains X ", s.contains (X.class));
        assertTrue ("Contains EditCookie " + s,  s.contains (org.openide.cookies.EditCookie.class));
        assertTrue ("Contains OpenCookie " + s, s.contains (org.openide.cookies.OpenCookie.class));
        
        s = new HashSet (n.getLookup ().lookup (all).allInstances());
        if (s.size () < 3) {
            fail ("At least three objects should be there: " + s);
        }
        
        BIG: for (int i = 0; i < expect.length; i++) {
            Iterator it = s.iterator();
            while (it.hasNext()) {
                if (expect[i].isInstance(it.next ())) {
                    continue BIG;
                }
            }
            fail ("Class " + expect[i] + " not found in " + s);
        }
    }

    
    public void testLookupClass () {
        Node n = new NodeWhichHasItselfInLookup ();
        doTestNodeLookup (n, Node.class);
        doTestNodeLookup (n, n.getClass ());
    }
    
    public void testLookupClassIfFilterNode () {
        Node n = new FilterNode (new NodeWhichHasItselfInLookup ());
        doTestNodeLookup (n, Node.class);
        doTestNodeLookup (n, n.getClass());
    }
    
    private void doTestNodeLookup (Node n, Class query) {
        Object o1 = n.getLookup ().lookup (query);
        assertEquals ("Found itself in own lookup(<" + query +">).", n, o1);
        Lookup.Result r = n.getLookup().lookupResult(query);
        assertEquals ("Only one instance in result.", 1, r.allInstances ().size ());
        Object o2 = r.allInstances ().iterator ().next ();
        assertEquals ("Found itself in own lookup(<Lookup.Template(" + query +")>).", n, o1);
        assertEquals ("Same node in both results.", o1, o2);
        
        Lookup.Item item = n.getLookup ().lookupItem (new Lookup.Template (query));
        assertNotNull (item);
        assertEquals ("Same node in all results", o2, item.getInstance ());
    }
    
    private static class NodeWhichHasItselfInLookup extends AbstractNode {
        public NodeWhichHasItselfInLookup () {
            super(Children.LEAF);
        }
    }
    
    
    private static Children createChildren () {
        return Children.LEAF;
    }
    
    
    private static class Listener extends Object
    implements LookupListener, NodeListener {
        private int cookies;
        private int lookups;
        
        public void assertEvents (String txt, int cookies, int lookups) {
            
            if (cookies != -1) 
                assertEquals (txt + " cookies", cookies, this.cookies);
            if (lookups != -1) 
                assertEquals (txt + " lookups", lookups, this.lookups);
            
            this.cookies = 0;
            this.lookups = 0;
        }
        
        public void childrenAdded(NodeMemberEvent ev) {
        }
        
        public void childrenRemoved(NodeMemberEvent ev) {
        }
        
        public void childrenReordered(NodeReorderEvent ev) {
        }
        
        public void nodeDestroyed(NodeEvent ev) {
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (Node.PROP_COOKIE == evt.getPropertyName()) {
                cookies++;
            }
        }
        
        public void resultChanged(org.openide.util.LookupEvent ev) {
            lookups++;
        }
        
    } // end of Listener
    
    private static class CookieNode extends AbstractNode {
        public ArrayList queries = new ArrayList ();
        
        public CookieNode () {
            super (createChildren ());
        }
        public CookieNode (Lookup l) {
            super (createChildren (), l);
        }
        
        public CookieSet cookieSet () {
            return getCookieSet ();
        }
        public void setSet (CookieSet s) {
            super.setCookieSet (s);
        }
        
        public Node.Cookie getCookie (Class c) {
            queries.add (c);
            return super.getCookie (c);
        }
        
    } // end of CookieNode
    
    private static class CookieNodeWithCookie extends CookieNode implements SaveCookie {
        public CookieNodeWithCookie () {
            cookieSet ().add (this);
        }
        
        public void save () {
        }
    }
    
    private static class CountableLookup extends AbstractLookup {
        public final InstanceContent ic;
        public final ArrayList queries;
        
        public CountableLookup () {
            this (new InstanceContent (), new ArrayList ());
        }
        
        private CountableLookup (InstanceContent ic, ArrayList queries) {
            super (ic);
            this.ic = ic;
            this.queries = queries;
        }
        
        protected void beforeLookup (Lookup.Template t) {
            super.beforeLookup (t);
            queries.add (t.getType ());
        }
    }
    class MyFilterNode extends FilterNode implements javax.swing.event.ChangeListener {
        public final CookieSet set;

        public MyFilterNode (Node n, boolean generalCookieSet) {
            super (n);
            set = generalCookieSet ? CookieSet.createGeneric(null) : new CookieSet();
            set.addChangeListener(this);
        }

        public Node.Cookie getCookie (Class cl) {
            Node.Cookie c = super.getCookie (cl);
            if (c != null) {
                return c;
            }
            return set.getCookie (cl);
        }

        public void stateChanged (javax.swing.event.ChangeEvent ev) {
            fireCookieChange ();
        }
    }
}

