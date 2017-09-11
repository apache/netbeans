/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.core.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages a set of pending values.
 * A subset of these values are "accepted" at any given time.
 * Each value in the queue is associated with three sets of keys (some of which may be empty):
 * <ol>
 * <li><em>Required</em> keys prevent the value from being accepted until all have been somehow provided.
 * <li><em>Needed</em> keys are similar but do not affect acceptance order.
 * <li><em>Provided</em> keys can be used to accept other values in the queue.
 * </ol>
 * @param <K> a hashable key, such as {@link String}
 * @param <V> a value object kept in the queue; must be comparable by {@link Object#equals}
 */
final class DependencyQueue<K,V> {

    /** what each value provides */
    private final Map<V,Set<K>> provides = new LinkedHashMap<V,Set<K>>();
    /** what each value requires */
    private final Map<V,Set<K>> requires = new HashMap<V,Set<K>>();
    /** what each value needs */
    private final Map<V,Set<K>> needs = new HashMap<V,Set<K>>();
    /** values which have been accepted so far */
    private final Set<V> accepted = new HashSet<V>();
    /** inverse of {@link #provides}: for each key, values (accepted or not) known to provide it */
    private final Map<K,Set<V>> providers = new HashMap<K,Set<V>>();

    /** Creates an empty queue. */
    public DependencyQueue() {}

    /**
     * Inserts a new value into the queue.
     * Its provided keys are added to those available in the queue.
     * If this value can be immediately accepted, it is returned, together with
     * any other values that can now be accepted as well; the list is ordered
     * so that a value requiring a key comes after some value providing the same key,
     * though a value needing a key may come before or after a value providing it.
     * If a provide-require cycle is encountered, none of those values will be
     * accepted (unless some other value providing one of the keys appears later);
     * whereas provide-require-need cycles may be satisfiable.
     * @param value a new value
     * @param provides keys it provides
     * @param requires keys it requires before it can be accepted
     * @param needs keys it needs to be present in the same offering when it is accepted
     * @return a list, possibly empty, otherwise including the named item,
     *         of values which were not previously accepted but now are;
     *         returned in dependency order
     */
    public synchronized List<V> offer(V value, Set<K> provides, Set<K> requires, Set<K> needs) {
//        System.err.println("offer: " + value + " p=" + provides + " r=" + requires + " n=" + needs);
        this.provides.put(value, Collections.unmodifiableSet(provides));
        this.requires.put(value, Collections.unmodifiableSet(requires));
        this.needs.put(value, Collections.unmodifiableSet(needs));
        for (K k : provides) {
            Set<V> p = providers.get(k);
            if (p == null) {
                p = new LinkedHashSet<V>();
                providers.put(k, p);
            }
            p.add(value);
        }
        LinkedList<V> proposed = new LinkedList<V>();
        if (visit(value, proposed)) {
            accepted.addAll(proposed);
            boolean lookForExtra = true;
            while (lookForExtra) {
                lookForExtra = false;
                List<V> reverseProvides = new ArrayList<V>(this.provides.keySet());
                Collections.reverse(reverseProvides);
                for (V extra : reverseProvides) {
                    if (accepted.contains(extra)) {
                        continue;
                    }
                    LinkedList<V> extraProposed = new LinkedList<V>();
                    if (visit(extra, extraProposed)) {
                        proposed.addAll(extraProposed);
                        accepted.addAll(extraProposed);
                        lookForExtra = true;
                    }
                }
            }
//            System.err.println("  => " + proposed);
//            Set<V> pending = new LinkedHashSet<V>(this.provides.keySet());
//            pending.removeAll(accepted);
//            System.err.println("  pending: " + pending);
            return proposed;
        } else {
//            System.err.println("  => nil");
            return Collections.emptyList();
        }
    }

    private boolean visit(V value, LinkedList<V> proposed) {
        if (proposed.contains(value)) {
            // XXX if in a require (vs. need) loop, should return false
            return true;
        }
        proposed.addFirst(value);
        Set<K> constraints = new LinkedHashSet<K>(requires.get(value));
        constraints.addAll(needs.get(value));
        CONSTRAINT: for (K k : constraints) {
            Set<V> p = providers.get(k);
            if (p != null) {
                for (V v : p) {
                    if (accepted.contains(v)) {
                        continue CONSTRAINT;
                    }
                    if (visit(v, proposed)) {
                        continue CONSTRAINT;
                    } else {
                        // XXX would be better to backtrack?
                        return false;
                    }
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Removes a value from the queue.
     * If it was not in the queue to begin with, does nothing.
     * If it had provided some keys which other accepted values required/needed,
     * they are now rejected.
     * @param value a value which might have been in the queue
     * @return a list, possibly empty, possibly including the named item,
     *         of values which had been accepted but now are not;
     *         returned in reverse dependency order
     */
    public synchronized List<V> retract(V value) {
        Set<K> provided = provides.remove(value);
        if (provided == null) {
            return Collections.emptyList();
        }
        for (K k : provided) {
            Set<V> p = providers.get(k);
            p.remove(value);
            if (p.isEmpty()) {
                providers.remove(k);
            }
        }
        requires.remove(value);
        needs.remove(value);
        if (!accepted.remove(value)) {
            return Collections.emptyList();
        }
        LinkedList<V> proposed = new LinkedList<V>();
        proposed.add(value);
        boolean lookForExtra = true;
        while (lookForExtra) {
            lookForExtra = false;
            for (V extra : this.provides.keySet()) {
                if (!accepted.contains(extra)) {
                    continue;
                }
                Set<K> constraints = new LinkedHashSet<K>(requires.get(extra));
                constraints.addAll(needs.get(extra));
                for (K k : constraints) {
                    boolean sat = false;
                    Set<V> p = providers.get(k);
                    if (p != null) {
                        for (V v : p) {
                            if (accepted.contains(v)) {
                                sat = true;
                                break;
                            }
                        }
                    }
                    if (!sat) {
                        accepted.remove(extra);
                        proposed.addFirst(extra);
                        lookForExtra = true;
                        break;
                    }
                }
            }
        }
        return proposed;
    }

}
