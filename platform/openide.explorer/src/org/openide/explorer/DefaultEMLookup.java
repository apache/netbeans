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
package org.openide.explorer;

import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import java.beans.*;

import java.util.*;


/**
 * Contents of the lookup for a ExplorerManager, is copied
 * from org.openide.windows.DefaultTopComponentLookup and shares
 * its test. If updating, please update both.
 * @author Jaroslav Tulach
 */
final class DefaultEMLookup extends ProxyLookup implements LookupListener, PropertyChangeListener {
    private static final Object PRESENT = new Object();

    /** component to work with */
    private ExplorerManager tc;

    /** lookup listener that is attached to all subnodes */
    private LookupListener listener;

    /** Map of (Node -> node Lookup.Result) the above lookup listener is attached to */
    private Map<Lookup, Lookup.Result> attachedTo;

    /** action map for the top component */
    private Lookup actionMap;

    /** Creates the lookup.
     * @param tc component to work on
     * @param map action map to add to the lookup
    */
    public DefaultEMLookup(ExplorerManager tc, javax.swing.ActionMap map) {
        super();

        this.tc = tc;
        this.listener = WeakListeners.create(LookupListener.class, this, null);
        this.actionMap = Lookups.singleton(map);

        tc.addPropertyChangeListener(WeakListeners.propertyChange(this, tc));

        updateLookups(tc.getSelectedNodes());
    }

    /** Extracts activated nodes from a top component and
     * returns their lookups.
     */
    public void updateLookups(Node[] arr) {
        if (arr == null) {
            arr = new Node[0];
        }

        Lookup[] lookups = new Lookup[arr.length];

        Map<Lookup, Lookup.Result> copy;

        synchronized (this) {
            if (attachedTo == null) {
                copy = Collections.emptyMap();
            } else {
                copy = new HashMap<>(attachedTo);
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
        updateLookups(tc.getSelectedNodes());
    }

    /** Finds out whether a query for a class can be influenced
     * by a state of the "nodes" lookup and whether we should
     * initialize listening
     */
    private static boolean isNodeQuery(Class<?> c) {
        return Node.class.isAssignableFrom(c) || c.isAssignableFrom(Node.class);
    }

    protected synchronized void beforeLookup(Template<?> t) {
        if ((attachedTo == null) && isNodeQuery(t.getType())) {
            Lookup[] arr = getLookups();

            attachedTo = new WeakHashMap<Lookup, Lookup.Result>(arr.length * 2);

            for (int i = 0; i < (arr.length - 2); i++) {
                Lookup.Result res = arr[i].lookup(t);
                res.addLookupListener(listener);
                attachedTo.put(arr[i], res);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES == evt.getPropertyName()) {
            updateLookups((Node[]) evt.getNewValue());
        }
    }

    /**
     * A proxying Lookup impl which yields no results when queried for Node,
     * and will never return any of the listed objects.
     */
    private static final class NoNodeLookup extends Lookup {
        private final Lookup delegate;
        private final Map<Node, Object> verboten;

        public NoNodeLookup(Lookup del, Node[] exclude) {
            delegate = del;
            verboten = new IdentityHashMap<Node, Object>();

            for (int i = 0; i < exclude.length; verboten.put(exclude[i++], PRESENT))
                ;
        }

        public <T> T lookup(Class<T> clazz) {
            if (clazz == Node.class) {
                return null;
            } else {
                T o = delegate.lookup(clazz);

                if (verboten.containsKey(o)) {
                    // There might be another one of the same class.
                    Iterator<? extends T> it = lookup(new Lookup.Template<T>(clazz)).allInstances().iterator();

                    while (it.hasNext()) {
                        T o2 = it.next();

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

        public <T> Lookup.Result<T> lookup(Lookup.Template<T> template) {
            Class<T> clz = template.getType();
            if (clz == Node.class) {
                return Lookup.EMPTY.lookup(new Lookup.Template<T>(clz));
            } else {
                return new ExclusionResult<T>(delegate.lookup(template), verboten);
            }
        }

        /**
         * A lookup result excluding some instances.
         */
        private static final class ExclusionResult<T> extends Lookup.Result<T> implements LookupListener {
            private final Lookup.Result<T> delegate;
            private final Map<Node, Object> verboten;
            private final List<LookupListener> listeners = new ArrayList<LookupListener>();

            public ExclusionResult(Lookup.Result<T> delegate, Map<Node, Object> verboten) {
                this.delegate = delegate;
                this.verboten = verboten;
            }

            public Collection<? extends T> allInstances() {
                Collection<? extends T> c = delegate.allInstances();
                List<T> ret = new ArrayList<T>(c.size()); // upper bound

                for (T o: c) {
                    if (!verboten.containsKey(o)) {
                        ret.add(o);
                    }
                }

                return ret;
            }

            public Set<Class<? extends T>> allClasses() {
                return delegate.allClasses(); // close enough
            }

            public Collection<? extends Item<T>> allItems() {
                Collection<? extends Item<T>> c = delegate.allItems();
                List<Item<T>> ret = new ArrayList<Item<T>>(c.size()); // upper bound

                for (Lookup.Item<T> i : c) {
                    if (Node.class.isAssignableFrom(i.getType())) 
                    {
                        if (verboten.containsKey(i.getInstance())) {
                            continue;
                        }
                    }
                    ret.add(i);
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
     // end of NoNodeLookup
}
