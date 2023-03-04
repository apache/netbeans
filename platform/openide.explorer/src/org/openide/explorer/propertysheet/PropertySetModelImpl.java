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
/*
 * PropertySetModel.java
 *
 * Created on December 28, 2002, 6:57 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.NbBundle;

import java.beans.*;

import java.util.*;

import javax.swing.SwingUtilities;
import javax.swing.event.*;


/** A model defining the expansion state for property sets, and
 *  thus the index order of properties.  The model exposes both
 *  properties and property sets.  For cases where only a subset of
 *  the property sets available on the node should be displayed in
 *  a table, this class can be subclassed to do such filtering.
 *  <P><B>Implementation note:</B>
 *  The current implementation is fairly naive, in order to get the
 *  rest of the new PropertySheet code working - it builds a list of
 *  the properties and property sets it will expose, and rebuilds it
 *  on a change.  This should eventually be replaced by a better
 *  performing implementation.
 *
  * @author  Tim Boudreau
 */
class PropertySetModelImpl implements PropertySetModel, Runnable {
    private static boolean filterHiddenProperties = !Boolean.getBoolean("netbeans.ps.showHiddenProperties"); //NOI18N

    /** Retains the persistent list of sets the user has explicitly
     *  closed, so they remain closed for other similar nodes
     */
    static Set<String> closedSets = new HashSet<String>(5);

    static {
        closedSets.addAll(Arrays.asList(PropUtils.getSavedClosedSetNames()));
    }

    private boolean[] expanded = null;
    private List<FeatureDescriptor> fds = new ArrayList<FeatureDescriptor>();
    private Comparator<Node.Property> comparator = null;
    private transient List<PropertySetModelListener> listenerList;
    private PropertySet[] sets = null;
    private transient int setCount = 0;

    /** A single event that can be reused. */
    private transient PropertySetModelEvent event = null;

    public PropertySetModelImpl() {
    }

    public PropertySetModelImpl(PropertySet[] ps) {
        if (ps == null) {
            ps = new PropertySet[0];
        }

        setPropertySets(ps);
    }

    public int getCount() {
        return fds.size();
    }

    public FeatureDescriptor getFeatureDescriptor(int index) {
        if (index == -1 || index >= fds.size()) {
            return null;
        }

        return fds.get(index);
    }

    public int indexOf(FeatureDescriptor fd) {
        return fds.indexOf(fd);
    }

    public boolean isProperty(int index) {
        return getFeatureDescriptor(index) instanceof Node.Property;
    }

    public void setComparator(Comparator<Node.Property> c) {
        if (c != comparator) {
            firePendingChange(true);
            comparator = c;
            fds.clear();
            init();
            fireChange(true);
        }
    }

    public void setPropertySets(PropertySet[] sets) {
        setCount = (sets == null) ? 0 : sets.length;

        if (sets == null) {
            sets = new PropertySet[0];
        }

        if ((setCount == 0) && !SwingUtilities.isEventDispatchThread()) {
            //Allow thread safe access for node destroyed events to 
            this.sets = new PropertySet[0];
            resetArray(sets);
            fds.clear();
            SwingUtilities.invokeLater(this);

            return;
        }

        firePendingChange(false);
        this.sets = sets;
        resetArray(sets);
        init();
        run();
    }

    public void run() {
        fireChange(false);
    }

    private void init() {
        fds.clear();

        if (comparator == null) {
            initExpandable();
        } else {
            initPlain();
        }
    }

    private void initPlain() {
        if (sets == null) {
            return;
        }

        int pcount = 0;

        for (int i = 0; i < sets.length; i++) {
            Property[] p = sets[i].getProperties();

            if (p == null) {
                throw new NullPointerException("Null is not a legal return" + " value for PropertySet.getProperties()");
            }

            pcount += p.length;
        }

        Property[] props = new Property[pcount];
        int l = 0;

        for (int i = 0; i < sets.length; i++) {
            Property[] p = sets[i].getProperties();
            System.arraycopy(p, 0, props, l, p.length);
            l += p.length;
        }

        Arrays.sort(props, comparator);
        fds.addAll(propsToList(props));
    }

    private void initExpandable() {
        if ((sets == null) || (sets.length == 0)) {
            return;
        }

        Property[] p;

        for (int i = 0; i < sets.length; i++) {
            //Show the set expandable only if it's not the default set
            if (PropUtils.hideSingleExpansion) {
                if (
                    (sets.length > 1) ||
                        ((sets.length == 1) &&
                        (!NbBundle.getMessage(PropertySetModelImpl.class, "CTL_PropertiesNoMnemonic").equals(
                            sets[0].getDisplayName()
                        )))
                ) { //NOI18N
                    fds.add(sets[i]);
                }
            } else {
                if (!PropertySheet.forceTabs) {
                    fds.add(sets[i]);
                }
            }

            if (expanded[i]) {
                p = sets[i].getProperties();

                if (p == null) {
                    throw new NullPointerException(
                        "Null is not a legal " + "return value for PropertySet.getProperties()"
                    );
                }

                if (p.length > 0) {
                    fds.addAll(propsToList(p));
                } else {
                    fds.remove(sets[i]);
                }
            }
        }
    }

    private List<Property> propsToList(Property[] p) {
        List<Property> result;

        if (filterHiddenProperties) {
            result = new ArrayList<Property>();

            for (int i = 0; i < p.length; i++) {
                if (!p[i].isHidden()) {
                    result.add(p[i]);
                }
            }
        } else {
            result = Arrays.asList(p);
        }

        return result;
    }

    private void resetArray(PropertySet[] sets) {
        int size = sets.length;

        if ((expanded == null) || (expanded.length < size)) {
            expanded = new boolean[size];
        }

        for (int i = 0; i < sets.length; i++) {
            expanded[i] = !closedSets.contains(sets[i].getDisplayName());
        }
    }

    private int lookupSet(FeatureDescriptor fd) {
        if (sets != null) {
            List l = Arrays.asList(sets);

            return l.indexOf(fd);
        } else {
            return -1;
        }
    }

    public boolean isExpanded(FeatureDescriptor set) {
        int index = lookupSet(set);

        if (index == -1) {
            return false;
        }

        return expanded[index];
    }

    public void toggleExpanded(int index) {
        FeatureDescriptor fd = getFeatureDescriptor(index);

        if (fd instanceof Property) {
            throw new IllegalArgumentException("Cannot expand a property."); //NOI18N
        }

        int setIndex = lookupSet(fd);
        int eventType = expanded[setIndex] ? PropertySetModelEvent.TYPE_INSERT : PropertySetModelEvent.TYPE_REMOVE;
        List <? extends FeatureDescriptor> props = propsToList(sets[setIndex].getProperties());
        int len = props.size();

        expanded[setIndex] = !expanded[setIndex];

        firePendingChange(eventType, index + 1, index + len, false);

        if (!expanded[setIndex]) {
            closedSets.add(fd.getDisplayName());
        } else {
            closedSets.remove(fd.getDisplayName());
        }

        if (expanded[setIndex]) {
            fds.addAll(index + 1, props);
        } else {
            for (int i = index + len; i > index; i--) {
                fds.remove(i);
            }
        }

        fireChange(eventType, index + 1, index + len);
        PropUtils.putSavedClosedSetNames(closedSets);
    }

    public final void addPropertySetModelListener(PropertySetModelListener listener) {
        if (listenerList == null) {
            listenerList = new java.util.ArrayList<PropertySetModelListener>();
        }

        listenerList.add(listener);
    }

    public final void removePropertySetModelListener(PropertySetModelListener listener) {
        if (listenerList != null) {
            listenerList.remove(listener);
        }
    }

    /** Getter which lazily instantiates the single event that
     *  will be used for al model events. */
    private PropertySetModelEvent getEvent() {
        if (event == null) {
            event = new PropertySetModelEvent(this);
        }

        return event;
    }

    /** Fire a change of type
     *  <code>PropertySetModelEvent.TYPE_WHOLESALE_CHANGE</code>. */
    private final void firePendingChange(int type, int start, int end, boolean reordering) {
        if (listenerList == null) {
            return;
        }

        Iterator i = listenerList.iterator();
        PropertySetModelListener curr;
        getEvent().type = PropertySetModelEvent.TYPE_WHOLESALE_CHANGE;
        event.start = start;
        event.end = end;
        event.type = type;
        event.reordering = reordering;

        while (i.hasNext()) {
            curr = (PropertySetModelListener) i.next();
            curr.pendingChange(event);
        }
    }

    /** Fire a change of type
     *  <code>PropertySetModelEvent.TYPE_WHOLESALE_CHANGE</code>. */
    private final void fireChange(boolean reordering) {
        if (listenerList == null) {
            return;
        }

        Iterator i = listenerList.iterator();
        PropertySetModelListener curr;
        getEvent().type = PropertySetModelEvent.TYPE_WHOLESALE_CHANGE;
        event.reordering = reordering;

        while (i.hasNext()) {
            curr = (PropertySetModelListener) i.next();
            curr.wholesaleChange(event);
        }
    }

    /** Fire a change of type
     *  <code>PropertySetModelEvent.TYPE_WHOLESALE_CHANGE</code>. */
    private final void firePendingChange(boolean reordering) {
        if (listenerList == null) {
            return;
        }

        Iterator i = listenerList.iterator();
        PropertySetModelListener curr;
        getEvent().type = PropertySetModelEvent.TYPE_WHOLESALE_CHANGE;
        event.reordering = reordering;

        while (i.hasNext()) {
            curr = (PropertySetModelListener) i.next();
            curr.pendingChange(event);
        }
    }

    /** Fire a change with the given parameters.
     *@param type The integer event type, as defined in <code>PropertySetModelEvent</code>.
     *@param start The first affected row (note that when expanding, the first affected
     *row is the first one <strong>following</strong> the row representing the property
     *set that was expanded)
     *@param end The last affected row.  */
    private final void fireChange(int type, int start, int end) {
        if (listenerList == null) {
            return;
        }

        getEvent().start = start;
        event.end = end;
        event.type = type;
        event.reordering = false;

        Iterator i = listenerList.iterator();
        PropertySetModelListener curr;

        while (i.hasNext()) {
            curr = (PropertySetModelListener) i.next();
            curr.boundedChange(event);
        }
    }

    public Comparator getComparator() {
        return comparator;
    }

    public int getSetCount() {
        return setCount;
    }
}
