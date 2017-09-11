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

package org.openide.nodes;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.AbstractLookup;

/** A lookup that represents content of a Node.getCookie and the node itself.
 *
 *
 * @author  Jaroslav Tulach
 */
final class NodeLookup extends AbstractLookup {
    /** See #40734 and NodeLookupTest and CookieActionIsTooSlowTest.
     * When finding action state for FilterNode, the action might been
     * triggered way to many times, due to initialization in beforeLookup
     * that triggered LookupListener and PROP_COOKIE change.
     */
    static final ThreadLocal<Node> NO_COOKIE_CHANGE = new ThreadLocal<Node>();
    private final AggregatingExecutor EXECUTOR = new AggregatingExecutor();

    /** Set of Classes that we have already queried <type>Class</type> */
    private java.util.Collection<Class> queriedCookieClasses = new ArrayList<Class>();

    /** node we are associated with
     */
    private Node node;

    /** New flat lookup.
     */
    public NodeLookup(Node n) {
        super();

        this.node = n;
        addPair(new CookieSetLkp.SimpleItem<Node>(n));
    }

    /** Calls into Node to find out if it has a cookie of given class.
     * It does special tricks to make CookieSet.Entry work.
     *
     * @param node node to ask
     * @param c class to query
     * @param colleciton to put Pair into if found
     */
    private static void addCookie(Node node, Class<?> c, 
            Collection<AbstractLookup.Pair> collection, 
            java.util.Map<AbstractLookup.Pair, Class> fromPairToClass) {
        Object res;
        Collection<AbstractLookup.Pair> pairs;
        Object prev = CookieSet.entryQueryMode(c);

        try {
            @SuppressWarnings("unchecked")
            Class<? extends Node.Cookie> fake = (Class<? extends Node.Cookie>)c;
            res = node.getCookie(fake);
        } finally {
            pairs = CookieSet.exitQueryMode(prev);
        }

        if (pairs == null) {
            if (res == null) {
                return;
            }

            CookieSetLkp.SimpleItem<Object> orig = new CookieSetLkp.SimpleItem<Object>(res);
            AbstractLookup.Pair p;
            if (res instanceof Node) {
                p = orig;
            } else {
                p = new CookieSet.PairWrap(orig);
            }
            pairs = Collections.singleton(p);
        }

        collection.addAll(pairs);
        for (AbstractLookup.Pair p : pairs) {
            Class<?> oldClazz = fromPairToClass.get(p);
            if (oldClazz == null || c.isAssignableFrom(oldClazz)) {
                fromPairToClass.put(p, c);
            }
        }
    }

    /** Notifies subclasses that a query is about to be processed.
     * @param template the template
     */
    @Override
    protected final void beforeLookup(Template template) {
        Class type = template.getType();
        Set<Node> nds = Node.blockEvents();
        try {
            blockingBeforeLookup(type);
        } finally {
            Node.unblockEvents(nds);
        }
    }
    
    private void blockingBeforeLookup(Class<?> type) {
        if (type == Object.class) {
            // ok, this is likely query for everything
            java.util.Set all;
            Object prev = null;

            try {
                prev = CookieSet.entryAllClassesMode();

                Object ignoreResult = node.getCookie(Node.Cookie.class);
            } finally {
                all = CookieSet.exitAllClassesMode(prev);
            }

            Iterator it = all.iterator();

            while (it.hasNext()) {
                Class c = (Class) it.next();
                updateLookupAsCookiesAreChanged(c);
            }

            // update Node.Cookie if not yet
            if (!queriedCookieClasses.contains(Node.Cookie.class)) {
                updateLookupAsCookiesAreChanged(Node.Cookie.class);
            }
        }

        if (!queriedCookieClasses.contains(type)) {
            updateLookupAsCookiesAreChanged(type);
        }
    }

    public void updateLookupAsCookiesAreChanged(Class toAdd) {
        java.util.Collection<AbstractLookup.Pair> instances;
        java.util.Map<AbstractLookup.Pair, Class> fromPairToQueryClass;
        java.util.Iterator<Class> it;
        // if it is cookie change, do the rescan, try to keep order
        synchronized (this) {
            if (toAdd != null) {
                if (queriedCookieClasses.contains(toAdd)) {
                    // if this class has already been added, go away
                    return;
                }

                queriedCookieClasses.add(toAdd);
            }

            instances = new java.util.LinkedHashSet<AbstractLookup.Pair>(queriedCookieClasses.size());
            fromPairToQueryClass = new java.util.LinkedHashMap<AbstractLookup.Pair, Class>();

            it = /* #74334 */new ArrayList<Class>(queriedCookieClasses).iterator();
            CookieSetLkp.SimpleItem<Node> nodePair = new CookieSetLkp.SimpleItem<Node>(node);
            instances.add(nodePair);
            fromPairToQueryClass.put(nodePair, Node.class);
        }
        while (it.hasNext()) {
            Class c = it.next();
            addCookie(node, c, instances, fromPairToQueryClass);
        }

        final java.util.Map<AbstractLookup.Pair, Class> m = fromPairToQueryClass;

        class Cmp implements java.util.Comparator<AbstractLookup.Pair> {
            public int compare(AbstractLookup.Pair p1, AbstractLookup.Pair p2) {
                Class<?> c1 = m.get(p1);
                Class<?> c2 = m.get(p2);
                
                assert c1 != null : p1 + " not in " + m; // NOI18N
                assert c2 != null : p2 + " not in " + m; // NOI18N
                
                if (c1 == c2) {
                    return 0;
                }

                if (c1.isAssignableFrom(c2)) {
                    return -1;
                }

                if (c2.isAssignableFrom(c1)) {
                    return 1;
                }

                if (c1.isAssignableFrom(p2.getType())) {
                    return -1;
                }

                if (c2.isAssignableFrom(p1.getType())) {
                    return 1;
                }

                return 0;
            }
        }

        java.util.ArrayList<AbstractLookup.Pair> list = new java.util.ArrayList<AbstractLookup.Pair>(instances);
        java.util.Collections.sort(list, new Cmp());

        if (toAdd == null) {
            setPairs(list);
        } else {
            Node prev = NO_COOKIE_CHANGE.get();

            try {
                NO_COOKIE_CHANGE.set(node);

                // doing the setPairs under entryQueryMode guarantees that 
                // FilterNode will ignore the change
                setPairs(list, EXECUTOR);
            } finally {
                NO_COOKIE_CHANGE.set(prev);
            }
        }
    }

    /**
     * Executor that collects all {@link Runnable}s passed to
     * {@link #execute(Runnable)} method in EDT and invokes them later.
     */
    private static class AggregatingExecutor implements Executor, Runnable {

        ArrayList<Runnable> list = new ArrayList<Runnable>();
        private boolean scheduled = false;

        @Override
        public void execute(Runnable command) {
            if (EventQueue.isDispatchThread()) {
                list.add(command);
                if (!scheduled) {
                    scheduled = true;
                    EventQueue.invokeLater(this);
                }
            } else {
                command.run();
            }
        }

        /**
         * Process aggregated commands.
         */
        @Override
        public void run() {
            assert EventQueue.isDispatchThread();
            for (Runnable r : list) {
                r.run();
            }
            list = new ArrayList<Runnable>();
            scheduled = false;
        }
    }
}
