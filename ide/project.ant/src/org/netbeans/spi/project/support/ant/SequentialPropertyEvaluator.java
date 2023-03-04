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

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.BaseUtilities;
import org.openide.util.Mutex;
import org.openide.util.TopologicalSortException;
import org.openide.util.Union2;
import org.openide.util.WeakListeners;

/**
 * A property evaluator based on a series of definitions.
 * <p>
 * Each batch of definitions can refer to properties within itself
 * (so long as there is no cycle) or any previous batch.
 * However the special first provider cannot refer to properties within itself.
 * </p>
 * <p>
 * Acquires {@link ProjectManager#mutex} for all operations, in read mode,
 * and fires changes synchronously. It also expects changes to be fired from property
 * providers in read (or write) access.
 * </p>
 */
final class SequentialPropertyEvaluator implements PropertyEvaluator, ChangeListener {

    private final PropertyProvider preprovider;
    private final PropertyProvider[] providers;
    private Map<String,String> predefs;
    private List<Map<String,String>> orderedDefs;
    private Map<String, String> defs;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * @param preprovider an initial context (may be null)
     * @param providers a sequential list of property groups
     */
    public SequentialPropertyEvaluator(final PropertyProvider preprovider, final PropertyProvider... providers) {
        this.preprovider = preprovider;
        this.providers = providers;
        ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
            public Void run() {
                if (preprovider != null) {
                    predefs = copyAndCompact(preprovider.getProperties());
                    // XXX defer until someone is listening?
                    preprovider.addChangeListener(WeakListeners.change(SequentialPropertyEvaluator.this, preprovider));
                } else {
                    predefs = Collections.emptyMap();
                }
                orderedDefs = new ArrayList<Map<String, String>>(providers.length);
                for (PropertyProvider pp : providers) {
                    orderedDefs.add(copyAndCompact(pp.getProperties()));
                    pp.addChangeListener(WeakListeners.change(SequentialPropertyEvaluator.this, pp));
                }
                return null;
            }
        });
        // XXX defer until someone asks for them?
        defs = evaluateAll(predefs, orderedDefs);
    }

    public String getProperty(final String prop) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {

            public String run() {
                if (defs == null) {
                    return null;
                }
                return defs.get(prop);
            }
        });
    }

    public String evaluate(final String text) {
        if (text == null) {
            throw new NullPointerException("Attempted to pass null to PropertyEvaluator.evaluate");
        }
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {

            public String run() {
                if (defs == null) {
                    return null;
                }
                Union2<String, Set<String>> result = substitute(text, defs, Collections.<String>emptySet());
                assert result.hasFirst() : "Unexpected result " + result + " from " + text + " on " + defs;
                return result.first();
            }
        });
    }

    public Map<String, String> getProperties() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Map<String, String>>() {

            public Map<String, String> run() {
                return defs;
            }
        });
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void stateChanged(ChangeEvent e) {
        assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
        PropertyProvider pp = (PropertyProvider) e.getSource();
        Map<String,String> nue = copyAndCompact(pp.getProperties());
        if (pp == preprovider) {
            if (predefs.equals(nue)) {
                return;
            } else {
                predefs = nue;
            }
        } else {
            int i = Arrays.asList(providers).indexOf(pp);
            if (i == -1) {
                assert false : "got change from unexpected source: " + pp;
                return;
            }
            if (orderedDefs.get(i).equals(nue)) {
                return;
            } else {
                orderedDefs.set(i, nue);
            }
        }
        Map<String,String> newdefs = evaluateAll(predefs, orderedDefs);
        // compose() may return null upon circularity errors
        Map<String, String> _defs = defs != null ? defs : Collections.<String, String>emptyMap();
        Map<String, String> _newdefs = newdefs != null ? newdefs : Collections.<String, String>emptyMap();
        if (!_defs.equals(_newdefs)) {
            Set<String> props = new HashSet<String>(_defs.keySet());
            props.addAll(_newdefs.keySet());
            List<PropertyChangeEvent> events = new LinkedList<PropertyChangeEvent>();
            for (String prop : props) {
                assert prop != null;
                String oldval = _defs.get(prop);
                String newval = _newdefs.get(prop);
                if (newval != null) {
                    if (newval.equals(oldval)) {
                        continue;
                    }
                } else {
                    assert oldval != null : "should not have had " + prop;
                }
                events.add(new PropertyChangeEvent(this, prop, oldval, newval));
            }
            assert !events.isEmpty();
            defs = newdefs;
            for (PropertyChangeEvent ev : events) {
                pcs.firePropertyChange(ev);
            }
        }
    }

    /**
     * Try to substitute property references etc. in an Ant property value string.
     * @param rawval the raw value to be substituted
     * @param predefs a set of properties already defined
     * @param siblingProperties a set of property names that are yet to be defined
     * @return either a String, in case everything can be evaluated now;
     *         or a Set<String> of elements from siblingProperties in case those properties
     *         need to be defined in order to evaluate this one
     */
    private static Union2<String,Set<String>> substitute(String rawval, Map<String,String> predefs, Set<String> siblingProperties) {
        assert rawval != null : "null rawval passed in";
        if (rawval.indexOf('$') == -1) {
            // Shortcut:
            //System.err.println("shortcut");
            return Union2.createFirst(rawval);
        }
        // May need to subst something.
        int idx = 0;
        // Result in progress, if it is to be a String:
        StringBuilder val = new StringBuilder();
        // Or, result in progress, if it is to be a Set<String>:
        Set<String> needed = new HashSet<String>();
        while (true) {
            int shell = rawval.indexOf('$', idx);
            if (shell == -1 || shell == rawval.length() - 1) {
                // No more $, or only as last char -> copy all.
                //System.err.println("no more $");
                if (needed.isEmpty()) {
                    val.append(rawval.substring(idx));
                    return Union2.createFirst(val.toString());
                } else {
                    return Union2.createSecond(needed);
                }
            }
            char c = rawval.charAt(shell + 1);
            if (c == '$') {
                // $$ -> $
                //System.err.println("$$");
                if (needed.isEmpty()) {
                    val.append('$');
                }
                idx += 2;
            } else if (c == '{') {
                // Possibly a property ref.
                int end = rawval.indexOf('}', shell + 2);
                if (end != -1) {
                    // Definitely a property ref.
                    String otherprop = rawval.substring(shell + 2, end);
                    //System.err.println("prop ref to " + otherprop);
                    if (predefs.containsKey(otherprop)) {
                        // Well-defined.
                        if (needed.isEmpty()) {
                            val.append(rawval.substring(idx, shell));
                            val.append(predefs.get(otherprop));
                        }
                        idx = end + 1;
                    } else if (siblingProperties.contains(otherprop)) {
                        needed.add(otherprop);
                        // don't bother updating val, it will not be used anyway
                        idx = end + 1;
                    } else {
                        // No def, leave as is.
                        if (needed.isEmpty()) {
                            val.append(rawval.substring(idx, end + 1));
                        }
                        idx = end + 1;
                    }
                } else {
                    // Unclosed ${ sequence, leave as is.
                    if (needed.isEmpty()) {
                        val.append(rawval.substring(idx));
                        return Union2.createFirst(val.toString());
                    } else {
                        return Union2.createSecond(needed);
                    }
                }
            } else {
                // $ followed by some other char, leave as is.
                // XXX is this actually right?
                if (needed.isEmpty()) {
                    val.append(rawval.substring(idx, idx + 2));
                }
                idx += 2;
            }
        }
    }

    /**
     * Evaluate all properties in a list of property mappings.
     * <p>
     * If there are any cyclic definitions within a single mapping,
     * the evaluation will fail and return null.
     * @param defs an ordered list of property mappings, e.g. {@link EditableProperties} instances
     * @param predefs an unevaluated set of initial definitions
     * @return values for all defined properties, or null if a circularity error was detected
     */
    private static Map<String,String> evaluateAll(Map<String,String> predefs, List<Map<String,String>> defs) {
        Map<String,String> m = new HashMap<String,String>(predefs);
        for (Map<String,String> curr : defs) {
            // Set of properties which we are deferring because they subst sibling properties:
            Map<String,Set<String>> dependOnSiblings = new HashMap<String,Set<String>>();
            for (Map.Entry<String,String> entry : curr.entrySet()) {
                String prop = entry.getKey();
                if (!m.containsKey(prop)) {
                    String rawval = entry.getValue();
                    //System.err.println("subst " + prop + "=" + rawval + " with " + m);
                    Union2<String,Set<String>> o = substitute(rawval, m, curr.keySet());
                    if (o.hasFirst()) {
                        m.put(prop, o.first());
                    } else {
                        dependOnSiblings.put(prop, o.second());
                    }
                }
            }
            Set<String> toSort = new HashSet<String>(dependOnSiblings.keySet());
            for (Set<String> s : dependOnSiblings.values()) {
                toSort.addAll(s);
            }
            List<String> sorted;
            try {
                sorted = BaseUtilities.topologicalSort(toSort, dependOnSiblings);
            } catch (TopologicalSortException e) {
                //System.err.println("Cyclic property refs: " + Arrays.asList(e.unsortableSets()));
                return null;
            }
            Collections.reverse(sorted);
            for (String prop : sorted) {
                if (!m.containsKey(prop)) {
                    String rawval = curr.get(prop);
                    m.put(prop, substitute(rawval, m, /*Collections.EMPTY_SET*/curr.keySet()).first());
                }
            }
        }
        return copyAndCompact(m);
    }

    private static final float COMPACT_LOAD_FACTOR = 0.95f; // #172203: try to minimize heap usage
    private static <K,V> Map<K,V> copyAndCompact(Map<K,V> m) {
        Map<K,V> m2 = new HashMap<K,V>((int) (m.size() / COMPACT_LOAD_FACTOR) + 1, COMPACT_LOAD_FACTOR);
        m2.putAll(m);
        return m2;
    }

}
