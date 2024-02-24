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
package org.openide.windows;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * Contents of the lookup for a top component.
 * Should contain its activated nodes, as well as their lookups merged.
 * Also contains an ActionMap instance which is a {@link DelegateActionMap}.
 * If there is no selection (as opposed to an empty selection), the lookup on Node
 * nonetheless contains one item assignable to Node but with a null instance (!).
 * If a node contains itself or another node in its lookup, this does not produce
 * any duplication in the top component lookup.
 * Queries on Node will return only nodes actually in the activated node list.
 * @author Jaroslav Tulach
 */
final class DefaultTopComponentLookup extends ProxyLookup implements LookupListener {
    private static final Object PRESENT = new Object();

    /** component to work with */
    private Reference<TopComponent> tc;

    /** lookup listener that is attached to all subnodes */
    private LookupListener listener;

    /** Map of (Lookup -> node Lookup.Result) the above lookup listener is attached to */
    private Map<Lookup,Lookup.Result> attachedTo;

    /** action map for the top component */
    private Lookup actionMap;

    /** Creates the lookup.
     * @param tc component to work on
    */
    public DefaultTopComponentLookup(TopComponent tc) {
        super();

        this.tc = new WeakReference<TopComponent>(tc);
        this.listener = WeakListeners.create(LookupListener.class, this, null);
        this.actionMap = Lookups.singleton(new DelegateActionMap(tc));

        updateLookups(tc.getActivatedNodes());
    }

    /** Extracts activated nodes from a top component and
     * returns their lookups.
     */
    public void updateLookups(Node[] arr) {
        if (arr == null) {
            AbstractLookup.Content c = new AbstractLookup.Content();
            AbstractLookup l = new AbstractLookup(c);
            c.addPair(new NoNodesPair());
            setLookups(new Lookup[] { l, actionMap });

            return;
        }

        Lookup[] lookups = new Lookup[arr.length];

        Map<Lookup,Lookup.Result> copy;

        synchronized (this) {
            if (attachedTo == null) {
                copy = Collections.emptyMap();
            } else {
                copy = new HashMap<Lookup,Lookup.Result>(attachedTo);
            }
        }

        for (int i = 0; i < arr.length; i++) {
            lookups[i] = arr[i].getLookup();

            if (copy != null) {
                // node arr[i] remains there, so do not remove it
                copy.remove(arr[i]);
            }
        }

        for (Iterator<Lookup.Result> it = copy.values().iterator(); it.hasNext();) {
            Lookup.Result res = it.next();
            res.removeLookupListener(listener);
        }

        synchronized (this) {
            attachedTo = null;
        }

        setLookups(new Lookup[] { new NoNodeLookup(new ProxyLookup(lookups), arr), Lookups.fixed((Object[])arr), actionMap, });
    }

    /** Change in one of the lookups we delegate to */
    public void resultChanged(LookupEvent ev) {
        TopComponent c = tc.get();
        if (c == null) {
            updateLookups(null);
            return;
        }
        updateLookups(c.getActivatedNodes());
    }

    /** Finds out whether a query for a class can be influenced
     * by a state of the "nodes" lookup and whether we should
     * initialize listening
     */
    private static boolean isNodeQuery(Class<?> c) {
        return Node.class.isAssignableFrom(c) || c.isAssignableFrom(Node.class);
    }

    @Override
    protected synchronized void beforeLookup(Template<?> t) {
        if ((attachedTo == null) && isNodeQuery(t.getType())) {
            Lookup[] arr = getLookups();

            attachedTo = new WeakHashMap<Lookup,Lookup.Result>(arr.length * 2);

            for (int i = 0; i < (arr.length - 2); i++) {
                Lookup.Result<?> res = arr[i].lookup(t);
                res.addLookupListener(listener);
                attachedTo.put(arr[i], res);
            }
        }
    }

    private static final class NoNodesPair extends AbstractLookup.Pair {
        public NoNodesPair() {
        }

        protected boolean creatorOf(Object obj) {
            return false;
        }

        public String getDisplayName() {
            return getId();
        }

        public String getId() {
            return "none"; // NOI18N
        }

        public Object getInstance() {
            return null;
        }

        public Class getType() {
            return org.openide.nodes.Node.class;
        }

        protected boolean instanceOf(Class c) {
            return c.isAssignableFrom(Node.class);
        }
    }
     // end of NoNodesPair

    // XXX try to use Lookups.exclude; cf. comments in #53058

    /**
     * A proxying Lookup impl which yields no results when queried for Node,
     * and will never return any of the listed objects.
     */
    private static final class NoNodeLookup extends Lookup {
        private final Lookup delegate;
        private final Map<Object,Object> verboten;

        public NoNodeLookup(Lookup del, Object[] exclude) {
            delegate = del;
            verboten = new IdentityHashMap<Object,Object>();

            for (int i = 0; i < exclude.length; verboten.put(exclude[i++], PRESENT)) {
            }
        }

        public <T> T lookup(Class<T> clazz) {
            if (clazz == Node.class) {
                return null;
            } else {
                T o = delegate.lookup(clazz);

                if (verboten.containsKey(o)) {
                    // There might be another one of the same class.
                    for( T o2 : lookupAll(clazz) ) {

                        if (!verboten.containsKey(o2)) {
                            // OK, use this one.
                            return o2;
                        }
                    }

                    // All such instances were excluded.
                    return null;
                } else {
                    return o;
                }
            }
        }

        @SuppressWarnings("unchecked")
        public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
            if (template.getType() == Node.class) {
                return Lookup.EMPTY.lookup(new Lookup.Template(Node.class));
            } else {
                return new ExclusionResult<T>(delegate.lookup(template), verboten);
            }
        }

        /**
         * A lookup result excluding some instances.
         */
        private static final class ExclusionResult<T> extends Lookup.Result<T> implements LookupListener {
            private final Lookup.Result<T> delegate;
            private final Map<Object,Object> verboten;
            private final List<LookupListener> listeners = new ArrayList<LookupListener>();

            public ExclusionResult(Lookup.Result<T> delegate, Map<Object,Object> verboten) {
                this.delegate = delegate;
                this.verboten = verboten;
            }

            public Collection<? extends T> allInstances() {
                Collection<? extends T> c = delegate.allInstances();
                List<T> ret = new ArrayList<T>(c.size()); // upper bound

                for (Iterator<? extends T> it = c.iterator(); it.hasNext();) {
                    T o = it.next();

                    if (!verboten.containsKey(o)) {
                        ret.add(o);
                    }
                }

                return ret;
            }

            @Override
            public Set<Class<? extends T>> allClasses() {
                return delegate.allClasses(); // close enough
            }

            @Override
            public Collection<? extends Lookup.Item<T>> allItems() {
                Collection<? extends Lookup.Item<T>> c = delegate.allItems();
                List<Lookup.Item<T>> ret = new ArrayList<Lookup.Item<T>>(c.size()); // upper bound

                for (Iterator<? extends Lookup.Item<T>> it = c.iterator(); it.hasNext();) {
                    Lookup.Item<T> i = it.next();

                    if (!verboten.containsKey(i.getInstance())) {
                        ret.add(i);
                    }
                }

                return ret;
            }

            public void addLookupListener(LookupListener l) {
                synchronized (listeners) {
                    if (listeners.isEmpty()) {
                        delegate.addLookupListener(this);
                    }

                    listeners.add(l);
                }
            }

            public void removeLookupListener(LookupListener l) {
                synchronized (listeners) {
                    listeners.remove(l);

                    if (listeners.isEmpty()) {
                        delegate.removeLookupListener(this);
                    }
                }
            }

            public void resultChanged(LookupEvent ev) {
                LookupEvent ev2 = new LookupEvent(this);
                LookupListener[] ls;

                synchronized (listeners) {
                    ls = listeners.toArray(new LookupListener[0]);
                }

                for (int i = 0; i < ls.length; i++) {
                    ls[i].resultChanged(ev2);
                }
            }
        }
    }
}
