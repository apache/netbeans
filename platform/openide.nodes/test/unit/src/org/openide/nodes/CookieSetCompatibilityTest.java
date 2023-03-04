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

import junit.framework.*;
import junit.textui.TestRunner;
import java.util.*;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.*;

import org.netbeans.junit.*;
import javax.swing.event.ChangeListener;

import org.openide.nodes.CookieSet.Factory;

/** Checks whether modified behaviour of cookie set is the same as
 * behaviour of the old one.
 *
 * @author Jaroslav Tulach
 */
public class CookieSetCompatibilityTest extends NbTestCase implements java.lang.reflect.InvocationHandler {
    private ArrayList interfaces = new ArrayList ();
    private ArrayList classes = new ArrayList ();
    private ArrayList instances = new ArrayList ();
    
    public CookieSetCompatibilityTest(String name) {
        super(name);
    }

    
    public void testRandomCheck () throws Exception {
        long seed = System.currentTimeMillis();
        try {
            compare (createOperator (new CookieSet ()), new OldCookieSetFromFebruary2005 (), seed);
        } catch (AssertionFailedError ex) {
            AssertionFailedError n = new AssertionFailedError ("Seed: " + seed + "\n" + ex.getMessage ());
            n.initCause(ex);
            throw n;
        } catch (Exception ex) {
            IllegalStateException n = new IllegalStateException ("Seed: " + seed + "\n" + ex.getMessage ());
            n.initCause(ex);
            throw n;
        }
    }
    
    private void compare (Operator o1, Operator o2, long seed) throws Exception {
        java.util.Random r = new java.util.Random (seed);
        
        interfaces.add (org.openide.cookies.SaveCookie.class);
        interfaces.add (java.io.Serializable.class);
        interfaces.add (Runnable.class);
        interfaces.add (Node.Cookie.class);

        class L implements javax.swing.event.ChangeListener, CookieSet.Factory {
            public int cnt;
            
            public void stateChanged (javax.swing.event.ChangeEvent ev) {
                cnt++;
            }

            
            private HashMap/*Class,Object*/ createdCookies = new HashMap ();
            public Node.Cookie createCookie (Class c) {
                Object o = createdCookies.get (c);
                if (o != null) {
                    return (Node.Cookie)o;
                }
                
                try {
                    Node.Cookie cookie = (Node.Cookie)c.getConstructors()[0].newInstance(new Object[] { CookieSetCompatibilityTest.this });
                    createdCookies.put (c, cookie);
                    return cookie;
                } catch (Exception ex) {
                    throw (AssertionFailedError)new AssertionFailedError (ex.getMessage()).initCause(ex);
                }
                
            }
        }
        
        L listener1 = new L ();
        L listener2 = new L ();
        
        o1.addChangeListener(listener1);
        o2.addChangeListener(listener2);
        
        for (int bigLoop = 0; bigLoop < 1000; bigLoop++) {
            int operation = r.nextInt(10);
            switch (operation) {
                case 0: { // generate new class
                    ArrayList superclasses = new ArrayList ();
                    int number = r.nextInt (interfaces.size () + 1);
                    for (int i = 0; i < number; i++) {
                        int index = r.nextInt (interfaces.size ());
                        Class c = (Class)interfaces.get (index);
                        if (!superclasses.contains (c)) {
                            superclasses.add (c);
                        }
                    }
                    Class c = java.lang.reflect.Proxy.getProxyClass(getClass ().getClassLoader(), (Class[])superclasses.toArray(new Class[0]));
                    classes.add (c);
                    break;
                }
                case 1: { // generate new instance
                    Object o;
                    Class c = randomClass (false, true, r);
                    if (c != null) {
                        o = c.getConstructors()[0].newInstance(new Object[] { this });
                    } else {
                        o = new Integer (r.nextInt());
                    }
                    instances.add (o);
                    break;
                }
                case 2: { // just add
                    if (instances.size () > 0) {
                        int index = r.nextInt (instances.size ());
                        Object o = instances.get (index);
                        if (o instanceof Node.Cookie) {
                            o1.add ((Node.Cookie)o);
                            o2.add ((Node.Cookie)o);
                        }
                    }
                    break;
                }
                case 3: { // just remove
                    if (instances.size () > 0) {
                        int index = r.nextInt (instances.size ());
                        Object o = instances.get (index);
                        if (o instanceof Node.Cookie) {
                            o1.remove ((Node.Cookie)o);
                            o2.remove ((Node.Cookie)o);
                        }
                    }
                    break;
                }
                case 4: { // check the get
                    Class query = randomClass (true, true, r);
                    Object r1 = o1.getCookie(query);
                    Object r2 = o2.getCookie(query);
                    assertSame ("After querying " + query, r1, r2);
                    break;
                }
                case 5: { // single class factory
                    Class c = randomClass (false, true, r);
                    if (c != null) {
                        CookieSet.Factory f = r.nextBoolean() ? listener1 : listener2;

                        o1.add (c, f);
                        o2.add (c, f);
                    }
                    break;
                }
                case 6: { // single class factory
                    Class c = randomClass (false, true, r);
                    if (c != null) {
                        CookieSet.Factory f = r.nextBoolean() ? listener1 : listener2;

                        o1.remove (c, f);
                        o2.remove (c, f);
                    }
                    break;
                }
                case 7: { // array class factory
                    Class[] arr = randomClasses (false, true, r);
                    CookieSet.Factory f = r.nextBoolean() ? listener1 : listener2;

                    o1.add (arr, f);
                    o2.add (arr, f);
                    break;
                }
                case 8: { // single class factory
                    Class[] arr = randomClasses (false, true, r);
                    CookieSet.Factory f = r.nextBoolean() ? listener1 : listener2;

                    o1.remove (arr, f);
                    o2.remove (arr, f);
                    break;
                }
            }
            
            assertEquals ("Listener counts are the same", listener1.cnt, listener2.cnt);
        }
        
    }
    
    private Class[] randomClasses (boolean fromInterfaces, boolean fromClasses, Random r) {
        ArrayList arr = new ArrayList ();
        int cnt = r.nextInt((fromInterfaces ? interfaces.size () : 0) + (fromClasses ? classes.size () : 0) + 1);
        while (cnt-- > 0) {
            Class c = randomClass (fromInterfaces, fromClasses, r);
            if (c != null && !arr.contains (c)) {
                arr.add (c);
            }
        }
        return (Class[])arr.toArray (new Class[0]);
    }
    
    private Class randomClass (boolean fromInterfaces, boolean fromClasses, Random r) {
        if (fromInterfaces && fromClasses) {
            if (r.nextBoolean()) {
                return randomClass (interfaces, r);
            } else {
                return randomClass (classes, r);
            }
        }
        
        if (fromInterfaces) {
            return randomClass (interfaces, r);
        }
        
        if (fromClasses) {
            return randomClass (classes, r);
        }
        
        return null;
    }
    
    private Class randomClass (List l, Random r) {
        if (l.size () == 0) return null;
        
        int index = r.nextInt (l.size ());
        return (Class)l.get (index);
    }

    
    public interface Operator {
        public void add (Node.Cookie cookie);
        public void add(Class cookieClass, Factory factory);
        public void add(Class[] cookieClass, Factory factory);

        public void remove (Node.Cookie cookie);
        public void remove(Class cookieClass, Factory factory);
        public void remove(Class[] cookieClass, Factory factory);
        
        public Node.Cookie getCookie (Class clazz);
        public void addChangeListener (ChangeListener l);
        public void removeChangeListener (ChangeListener l);
        
    }
    
    private Operator createOperator (final CookieSet set) {
        class O implements Operator {
            public void add (Node.Cookie cookie) {
                set.add (cookie);
            }
            public void add(Class cookieClass, Factory factory) {
                set.add (cookieClass, factory);
            }
            public void add(Class[] cookieClass, Factory factory) {
                set.add (cookieClass, factory);
            }

            public void remove (Node.Cookie cookie) {
                set.remove (cookie);
            }
            public void remove(Class cookieClass, Factory factory) {
                set.remove (cookieClass, factory);
            }
            public void remove(Class[] cookieClass, Factory factory) {
                set.remove (cookieClass, factory);
            }

            public Node.Cookie getCookie (Class clazz) {
                return set.getCookie (clazz);
            }
            public void addChangeListener (ChangeListener l) {
                set.addChangeListener (l);
            }
            public void removeChangeListener (ChangeListener l) {
                set.removeChangeListener (l);
            }
        }
        return new O ();
    }

    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            // ok we have to implement these
            if ("equals".equals (method.getName())) {
                return Boolean.valueOf (proxy == args[0]);
            }
            if ("hashCode".equals (method.getName ())) {
                return new Integer (System.identityHashCode(proxy));
            }
            if ("toString".equals (method.getName ())) {
                return proxy.getClass () + "@" + System.identityHashCode(proxy);
            }
            
        }
        
        throw new org.openide.util.NotImplementedException ("Method: " + method);
    }
    

    /** This is a copy of the implementation of CookieSet from Feb 2005, rev. 1.16 */
    static final class OldCookieSetFromFebruary2005 extends Object implements Operator {
        /** variable to allow effecient communication with NodeLookup */
        private static java.lang.ThreadLocal QUERY_MODE = new java.lang.ThreadLocal ();

        /** list of cookies (Class, Node.Cookie) */
        private HashMap map = new HashMap (31);

        /** set of listeners */
        private javax.swing.event.EventListenerList listeners = new javax.swing.event.EventListenerList ();

        /** Default constructor. */
        public OldCookieSetFromFebruary2005 () {}

        /** Add a new cookie to the set. If a cookie of the same
        * <em>actual</em> (not representation!) class is already there,
        * it is replaced.
        * <p>Cookies inserted earlier are given preference during lookup,
        * in case a supplied representation class matches more than one cookie
        * in the set.
        *
        * @param cookie cookie to add
        */
        public void add (Node.Cookie cookie) {
            synchronized (this) {
                registerCookie (cookie.getClass (), cookie);
            }

            fireChangeEvent ();
        }

        /** Remove a cookie from the set.
        * @param cookie the cookie to remove
        */
        public void remove (Node.Cookie cookie) {
            synchronized (this) {
                unregisterCookie (cookie.getClass (), cookie);
            }

            fireChangeEvent ();
        }

        /** Get a cookie.
        *
        * @param clazz the representation class
        * @return a cookie assignable to the representation class, or <code>null</code> if there is none
        */
        public Node.Cookie getCookie (Class clazz) {
            Node.Cookie ret = null;
            synchronized (this) {
                R r = findR (clazz);
                if (r == null) {
                    return null;
                }
                ret = r.cookie ();
            }
            if (ret instanceof CookieEntry) {
                if (clazz == QUERY_MODE.get ()) {
                    // we expected to be asked for this class
                    // set cookie entry as a result
                    QUERY_MODE.set (ret);
                    ret = null;
                } else {
                    // unwrap the cookie
                    ret = ((CookieEntry) ret).getCookie(true);
                }
            }

            return ret;
        }

        /** Add a listener to changes in the cookie set.
        * @param l the listener to add
        */
        public void addChangeListener (ChangeListener l) {
            listeners.add (ChangeListener.class, l);
        }

        /** Remove a listener to changes in the cookie set.
        * @param l the listener to remove
        */
        public void removeChangeListener (ChangeListener l) {
            listeners.remove (ChangeListener.class, l);
        }

        /** Node lookup starts its non-important query.
         */
        static Object entryQueryMode (Class c) {
            Object prev = QUERY_MODE.get ();
            QUERY_MODE.set (c);
            return prev;
        }

        /** Exits query mode.
         */
        static org.openide.util.lookup.AbstractLookup.Pair exitQueryMode (Object prev) {
            Object cookie = QUERY_MODE.get ();
            QUERY_MODE.set (prev);
            if (cookie instanceof CookieEntry) {
                return new CookieEntryPair ((CookieEntry)cookie);
            } else {
                return null;
            }
        }

        /** Fires change event
        */
        private void fireChangeEvent () {
            Object[] arr = listeners.getListenerList();
            if (arr.length > 0) {
                javax.swing.event.ChangeEvent ev = null;
                // Process the listeners last to first, notifying
                // those that are interested in this event
                for (int i = arr.length-2; i>=0; i-=2) {
                    if (arr[i] == ChangeListener.class) {
                        if (ev == null) {
                            ev = new javax.swing.event.ChangeEvent (this);
                        }

                        ((ChangeListener)arr[i + 1]).stateChanged (ev);
                    }
                }
            }
        }

        /** Attaches cookie to given class and all its superclasses and
        * superinterfaces.
        *
        * @param c class or null
        * @param cookie cookie to attach
        */
        private void registerCookie (Class c, Node.Cookie cookie) {
            if ((c == null) || !Node.Cookie.class.isAssignableFrom(c)) return;

            R r = findR (c);
            if (r == null) {
                r = new R ();
                map.put (c, r);
            }

            r.add (cookie);

            registerCookie (c.getSuperclass (), cookie);

            Class[] inter = c.getInterfaces ();
            for (int i = 0; i < inter.length; i++) {
                registerCookie (inter[i], cookie);
            }
        }

        /** Removes cookie from the class and all its superclasses and
        * superinterfaces.
        *
        * @param c class or null
        * @param cookie cookie to attach
        */
        private void unregisterCookie (Class c, Node.Cookie cookie) {
            if (
                c == null || !Node.Cookie.class.isAssignableFrom(c)
            ) return;

            // if different cookie is attached to class c stop removing
            R r = findR (c);
            if (r != null) {
                // remove the cookie
                r.remove (cookie);
            }

            unregisterCookie (c.getSuperclass (), cookie);

            Class[] inter = c.getInterfaces ();
            for (int i = 0; i < inter.length; i++) {
                unregisterCookie (inter[i], cookie);
            }
        }

        /** Registers a Factory for given cookie class */
        public void add(Class cookieClass, Factory factory) {
            if (factory == null) {
                throw new IllegalArgumentException();
            }

            synchronized (this) {
                registerCookie (cookieClass, new CookieEntry(factory, cookieClass));
            }

            fireChangeEvent ();
        }

        /** Registers a Factory for given cookie classes */
        public void add(Class[] cookieClass, Factory factory) {
            if (factory == null) {
                throw new IllegalArgumentException();
            }

            synchronized (this) {
                for (int i = 0; i < cookieClass.length; i++) {
                    registerCookie (cookieClass[i], new CookieEntry(factory, cookieClass[i]));
                }
            }

            fireChangeEvent ();
        }    

        /**
         * Unregisters a Factory for given cookie class
         * @since 2.6
         */
        public void remove(Class cookieClass, Factory factory) {
            if (factory == null) {
                throw new IllegalArgumentException();
            }

            synchronized (this) {
                R r = findR(cookieClass);
                if (r != null) {
                    Node.Cookie c = r.cookie();
                    if (c instanceof CookieEntry) {
                        CookieEntry ce = (CookieEntry)c;
                        if (ce.factory == factory) {
                            unregisterCookie (cookieClass, c);
                        }
                    }
                }
            }

            fireChangeEvent ();
        }

        /** 
         * Unregisters a Factory for given cookie classes
         * @since 2.6
         */
        public void remove(Class[] cookieClass, Factory factory) {
            if (factory == null) {
                throw new IllegalArgumentException();
            }

            synchronized (this) {
                for (int i = 0; i < cookieClass.length; i++) {
                    R r = findR(cookieClass[i]);
                    if (r != null) {
                        Node.Cookie c = r.cookie();
                        if (c instanceof CookieEntry) {
                            CookieEntry ce = (CookieEntry)c;
                            if (ce.factory == factory) {
                                unregisterCookie (cookieClass[i], c);
                            }
                        }
                    }
                }
            }

            fireChangeEvent ();
        }    

        /** Finds a result in a map.
         */
        private R findR (Class c) {
            return (R)map.get (c);
        }

        /** Finds base class for a cookie.
         * @param c cookie
         * @return base class
         */
        private static Class baseForCookie (Node.Cookie c) {
            if (c instanceof CookieEntry) {
                return ((CookieEntry)c).klass;
            } 
            return c.getClass ();
        }


        /** Entry for one Cookie */
        private static class CookieEntry implements Node.Cookie {
            /** Factory for the cookie */
            final Factory factory;
            /** Class of the cookie */
            private final Class klass;
            /** A Referenec to the cookie */
            private java.lang.ref.Reference cookie;

            /** Constructs new FactoryEntry */
            public CookieEntry(Factory factory, Class klass) {
                this.factory = factory;
                this.klass = klass;
            }

            /** Getter for the cookie.
             * Synchronized because we don't want to run factory.createCookie
             * symultaneously from two threads.
             */
            public synchronized Node.Cookie getCookie(boolean create) {
                Node.Cookie ret;
                if (create) {
                    if ((cookie == null) || ((ret = (Node.Cookie) cookie.get()) == null)) {
                        ret = factory.createCookie(klass);
                        if (ret == null) return null;
                        cookie = new java.lang.ref.WeakReference(ret);
                    }
                } else {
                    ret = (Node.Cookie) (cookie == null ? null : cookie.get ());
                }

                return ret;
            }
        } // end of CookieEntry

        /** Pair that represents an entry.
         */
        private static final class CookieEntryPair extends org.openide.util.lookup.AbstractLookup.Pair {
            private CookieEntry entry;

            public CookieEntryPair (CookieEntry e) {
                this.entry = e;
            }

            protected boolean creatorOf(Object obj) {
                return obj == entry.getCookie (false);
            }

            public String getDisplayName() {
                return getId ();
            }

            public String getId() {
                return entry.klass.getName ();
            }

            public Object getInstance() {
                return entry.getCookie (true);
            }

            public Class getType() {
                return entry.klass;
            }

            protected boolean instanceOf(Class c) {
                return c.isAssignableFrom(entry.klass);
            }

            public int hashCode () {
                return entry.hashCode () + 5;
            }

            public boolean equals (Object obj) {
                if (obj instanceof CookieEntryPair) {
                    return ((CookieEntryPair)obj).entry == entry;
                }
                return false;
            }
        } // end of CookieEntryPair

        /** Implementation of the result.
         */
        private static final class R extends Object {
            /** list of registered cookies */
            public List cookies;
            /** base class of the first cookie registered here */
            public Class base;

            R() {}

            /** Adds a cookie.
             * @return true if adding should continue on superclasses should continue
             */
            public void add (Node.Cookie cookie) {
                if (cookies == null) {
                    cookies = new ArrayList (1);
                    cookies.add (cookie);
                    base = baseForCookie (cookie);
                    return;
                }

                Class newBase = baseForCookie (cookie);
                if (base == null || newBase.isAssignableFrom (base)) {
                    cookies.set (0, cookie);
                    base = newBase;
                } else {
                    cookies.add (cookie);
                }
            }

            /** Removes a cookie.
             * @return true if empty
             */
            public boolean remove (Node.Cookie cookie) {
                if (cookies == null) {
                    return true;
                }

                if (cookies.remove (cookie) && cookies.size () == 0) {
                    base = null;
                    cookies = null;
                    return true;
                }

                base = baseForCookie ((Node.Cookie)cookies.get (0));

                return false;
            }

            /** @return the cookie for this result or null
             */
            public Node.Cookie cookie () {
                return cookies == null || cookies.isEmpty () ? null : (Node.Cookie)cookies.get (0);
            }
        }
    } // end of OldCookieSetFromFebruary2005
    
}
