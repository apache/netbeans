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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.*;


/** Support for creation of property sets. Allows easy
* addition, modification, and deletion of properties. Also
* permits listening on changes of contained properties.
*
* @author Jaroslav Tulach, Dafe Simonek
*/
public final class Sheet extends Object {
    /** Name for regular Bean property set. */
    public static final String PROPERTIES = "properties"; // NOI18N

    /** Name for expert Bean property set. */
    public static final String EXPERT = "expert"; // NOI18N

    /** list of sets (Sheet.Set) */
    private ArrayList<Set> sets;

    /** array of sets 
     * @GuardedBy("this")
     */
    private Node.PropertySet[] array;

    /** support for changes */
    private PropertyChangeSupport supp = new PropertyChangeSupport(this);

    /** change listener that is attached to each set added to this object */
    private PropertyChangeListener propL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                supp.firePropertyChange(null, null, null);
            }
        };

    /** Default constructor.
    */
    public Sheet() {
        this(new ArrayList<Set>(2));
    }

    /** Copy constrcutor.
    * @param ar array to use
    */
    Sheet(ArrayList<Set> ar) {
        sets = ar;
    }

    /** Obtain the array of property sets.
    * @return the array
    */
    public final Node.PropertySet[] toArray() {
        for (;;) {
            synchronized (this) {
                if (array != null) {
                    return array;
                }
            }
            Node.PropertySet[] l = new Node.PropertySet[sets.size()];
            sets.toArray(l);
            synchronized (this) {
                if (array == null) {
                    array = l;
                }
            }
        }
    }

    /** Create a deep copy of the sheet. Listeners are not copied.
    * @return the cloned object */
    public synchronized Sheet cloneSheet() {
        int len = sets.size();
        ArrayList<Set> l = new ArrayList<Set>(len);

        for (int i = 0; i < len; i++) {
            l.add(sets.get(i).cloneSet());
        }

        return new Sheet(l);
    }

    /** Find the property set with a given name.
    * @param name name of the set
    * @return the property set, or <code>null</code> if no such set exists
    */
    public synchronized Set get(String name) {
        int indx = findIndex(name);

        return (indx == -1) ? null : sets.get(indx);
    }

    /** Add a property set. If the set does not yet exist in the sheet,
    * inserts a new set with the implied name. Otherwise the old set is replaced
    * by the new one.
    *
    * @param set to add
    * @return the previous set with the same name, or <code>null</code> if this is a fresh insertion
    */
    public synchronized Set put(Set set) {
        int indx = findIndex(set.getName());

        Set removed = null;
        if (indx == -1) {
            sets.add(set);
        } else {
            removed = sets.set(indx, set);
            removed.removePropertyChangeListener(propL);
        }
        set.addPropertyChangeListener(propL);
        refresh();
        return removed;
    }

    /** Remove a property set from the sheet.
    * @param set name of set to remove
    * @return removed set, or <code>null</code> if the set could not be found
    */
    public synchronized Set remove(String set) {
        int indx = findIndex(set);

        if (indx != -1) {
            Set s = sets.remove(indx);
            s.removePropertyChangeListener(propL);
            refresh();

            return s;
        } else {
            return null;
        }
    }

    /** Convenience method to create new sheet with only one empty set, named {@link #PROPERTIES}.
    * Display name and hint are settable via the appropriate bundle.
    *
    * @return a new sheet with default property set
    */
    public static Sheet createDefault() {
        Sheet newSheet = new Sheet();

        // create default property set
        newSheet.put(createPropertiesSet());

        return newSheet;
    }

    /** Convenience method to create new sheet set named {@link #PROPERTIES}.
    *
    * @return a new properties sheet set
    */
    public static Sheet.Set createPropertiesSet() {
        Sheet.Set ps = new Sheet.Set();
        ps.setName(PROPERTIES);
        ps.setDisplayName(Node.getString("Properties"));
        ps.setShortDescription(Node.getString("HINT_Properties"));

        return ps;
    }

    /** Convenience method to create new sheet set named {@link #EXPERT}.
    *
    * @return a new expert properties sheet set
    */
    public static Sheet.Set createExpertSet() {
        Sheet.Set ps = new Sheet.Set();
        ps.setExpert(true);
        ps.setName(EXPERT);
        ps.setDisplayName(Node.getString("Expert"));
        ps.setShortDescription(Node.getString("HINT_Expert"));

        return ps;
    }

    /** Add a change listener.
    * @param l the listener */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        supp.addPropertyChangeListener(l);
    }

    /** Remove a change listener.
    * @param l the listener */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        supp.removePropertyChangeListener(l);
    }

    /** Finds index for property set for given name.
    * @param name of the property
    * @return the index or -1 if not found
    */
    private int findIndex(String name) {
        int s = sets.size();

        for (int i = 0; i < s; i++) {
            Node.PropertySet p = (Node.PropertySet) sets.get(i);
            final String pn = p.getName();
            if (pn != null && pn.equals(name)) {
                return i;
            }
        }

        return -1;
    }

    /** Refreshes and fire info about the set
    */
    private void refresh() {
        synchronized (this) {
            array = null;
        }
        supp.firePropertyChange(null, null, null);
    }

    /**
     * A list of bean properties.
     * While there can only be one property of a given name, insertion order is significant.
     */
    public static final class Set extends Node.PropertySet {
        /** list of properties (Node.Property) */
        private List<Node.Property<?>> props;

        /** array of properties */
        private Node.Property<?>[] array;

        /** change listeners listening on this set */
        private PropertyChangeSupport supp = new PropertyChangeSupport(this);

        /** Default constructor.
        */
        public Set() {
            this(new ArrayList<Node.Property<?>>());
        }

        /** @param al array list to use for this property set
        */
        private Set(List<Node.Property<?>> al) {
            props = al;
        }

        /** Clone the property set.
        * @return the clone
        */
        public synchronized Set cloneSet() {
            return new Set(new ArrayList<Node.Property<?>>(props));
        }

        /** Get a property by name.
        * @param name name of the property
        * @return the first property in the list that has this name, <code>null</code> if not found
        */
        public Node.Property<?> get(String name) {
            int indx = findIndex(name);

            return (indx == -1) ? null : props.get(indx);
        }

        /** Get all properties in this set.
        * @return the properties
        */
        synchronized public Node.Property<?>[] getProperties() {
            if (array == null) {
                array = new Node.Property<?>[props.size()];
                props.toArray(array);
            }
            return array;
        }

        /** Add a property to this set, replacing any old one with the same name.
        * @param p the property to add
        * @return the property with the same name that was replaced, or <code>null</code> for a fresh insertion
        */
        public synchronized Node.Property<?> put(Node.Property<?> p) {
            int indx = findIndex(p.getName());
            Node.Property<?> removed;

            if (indx != -1) {
                // replaces the original one
                removed = props.set(indx, p);
            } else {
                // adds this to the end
                props.add(p);
                removed = null;
            }

            // clears computed array and fires into about change of properties
            refresh();

            return removed;
        }

        /** Add several properties to this set, replacing old ones with the same names.
        *
        * @param ar properties to add
        */
        public synchronized void put(Node.Property<?>[] ar) {
            for (int i = 0; i < ar.length; i++) {
                Node.Property<?> p = ar[i];
                p = ar[i];

                int indx = findIndex(p.getName());

                if (indx != -1) {
                    // replaces the original one
                    props.set(indx, p);
                } else {
                    // adds this to the end
                    props.add(p);
                }
            }

            // clears computed array and fires into about change of properties
            refresh();
        }

        /** Remove a property from the set.
        * @param name name of the property to remove
        * @return the removed property, or <code>null</code> if it was not there to begin with
        */
        public synchronized Node.Property<?> remove(String name) {
            int indx = findIndex(name);

            if (indx != -1) {
                try {
                    return props.remove(indx);
                } finally {
                    // clears computed array and fires into about change of properties
                    refresh();
                }
            } else {
                return null;
            }
        }

        /** Add a property change listener.
        * @param l the listener to add */
        public void addPropertyChangeListener(PropertyChangeListener l) {
            supp.addPropertyChangeListener(l);
        }

        /** Remove a property change listener.
        * @param l the listener to remove */
        public void removePropertyChangeListener(PropertyChangeListener l) {
            supp.removePropertyChangeListener(l);
        }

        /** Finds index for property with specified name.
        * @param name of the property
        * @return the index or -1 if not found
        */
        private int findIndex(String name) {
            int s = props.size();

            for (int i = 0; i < s; i++) {
                Node.Property<?> p = props.get(i);

                if (p.getName().equals(name)) {
                    return i;
                }
            }

            return -1;
        }

        /** Notifies change of properties.
        */
        private void refresh() {
            array = null;
            supp.firePropertyChange(null, null, null);
        }
    }
     // end of Set
}
