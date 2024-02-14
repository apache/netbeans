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
package org.openide.explorer.propertysheet;

import org.openide.nodes.*;
import org.openide.util.*;

import java.beans.PropertyChangeEvent;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node.PropertySet;


/**
 * A node used by PropertySheet to display common properties of
 * more nodes.
 * @author David Strupl
 */
final class ProxyNode extends AbstractNode {
    private static final int MAX_NAMES = 2;
    private volatile Node[] original;
    private volatile ArrayList<Node.PropertySet[]> originalPropertySets;
    private NodeListener nl;
    private NodeListener pcl;
    String displayName = null;
    private String shortDescription = null;

    ProxyNode(Node... original) {
        super(Children.LEAF);
        this.original = original;
        nl = new NodeAdapterImpl(true);
        pcl = new NodeAdapterImpl(false);

        for (int i = 0; i < original.length; i++) {
            original[i].addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(pcl, original[i]));
            original[i].addNodeListener(
                org.openide.util.WeakListeners.create(NodeListener.class, nl, original[i])
            );
        }
    }

    public HelpCtx getHelpCtx() {
        for (int i = 0; i < original.length; i++) {
            if (original[i].getHelpCtx() != HelpCtx.DEFAULT_HELP) {
                return original[i].getHelpCtx();
            }
        }

        return HelpCtx.DEFAULT_HELP;
    }

    public Node cloneNode() {
        return new ProxyNode(original);
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set[] computedSet = computePropertySets();

        for (int i = 0; i < computedSet.length; i++) {
            sheet.put(computedSet[i]);
        }

        return sheet;
    }

    /** */
    Node[] getOriginalNodes() {
        return original;
    }

    public String getDisplayName() {
        if (displayName == null) {
            //Issue 40821, don't display extremely long names, they make
            //the property sheet huge if opened in a window
            displayName = getConcatenatedName(MAX_NAMES);
        }

        return displayName;
    }

    private String getConcatenatedName(int limit) {
        Node[] n = getOriginalNodes();
        StringBuffer name = new StringBuffer();
        String delim = NbBundle.getMessage(ProxyNode.class, "CTL_List_Delimiter"); //NOI18N

        for (int i = 0; i < n.length; i++) {
            name.append(n[i].getDisplayName());

            if (i != (n.length - 1)) {
                name.append(delim);
            }

            if ((i >= limit) && (i != (n.length - 1))) {
                name.append(NbBundle.getMessage(ProxyNode.class, "MSG_ELLIPSIS"));

                break;
            }
        }

        return name.toString();
    }

    public String getShortDescription() {
        if (getOriginalNodes().length < MAX_NAMES) {
            return NbBundle.getMessage(ProxyNode.class, "CTL_Multiple_Selection"); //NOI18N
        } else {
            if (shortDescription == null) {
                shortDescription = getConcatenatedName(Integer.MAX_VALUE);
            }

            return shortDescription;
        }
    }
    
    private ArrayList<Node.PropertySet[]> getOriginalPropertySets(Node[] forWhat) {
        if( null == originalPropertySets ) {
            ArrayList<PropertySet[]> arr = new ArrayList<Node.PropertySet[]>( forWhat.length );
            
            for( int i=0; i<forWhat.length; i++) {	    
                Node.PropertySet[] p = forWhat[i].getPropertySets();
                arr.add( p );
            }
            if (original == forWhat) {
                originalPropertySets = arr;
            }
            return arr;
        }
        return originalPropertySets;
    }

    /** Computes intersection of tabs and intersection
     * of properties in those tabs.
     */
    private Sheet.Set[] computePropertySets() {
        Node[] copy = original;
        if (copy.length > 0) {
            final ArrayList<PropertySet[]> ops = getOriginalPropertySets(copy);
            if (ops.isEmpty()) {
                return new Sheet.Set[0];
            }
            Node.PropertySet[] firstSet = ops.get( 0 );
            java.util.Set<Node.PropertySet> sheets = new HashSet<Node.PropertySet>(Arrays.asList(firstSet));

            // compute intersection of all Node.PropertySets for given nodes
            for (int i = 1; i < ops.size(); i++) {
                sheets.retainAll(new HashSet(Arrays.asList(ops.get(i))));
            }

            ArrayList<Sheet.Set> resultSheets = new ArrayList<Sheet.Set>(sheets.size());

            // now for all resulting sheets take common properties
            for (int i = 0; i < firstSet.length; i++) {
                if (!sheets.contains(firstSet[i]) || firstSet[i].isHidden()) {
                    continue;
                }

                Node.PropertySet current = firstSet[i];

                // creates an empty Sheet.Set with same names as current
                Sheet.Set res = new Sheet.Set();
                res.setName(current.getName());
                res.setDisplayName(current.getDisplayName());
                res.setShortDescription(current.getShortDescription());

                String tabName = (String) current.getValue("tabName"); //NOI18N

                if (tabName != null) {
                    res.setValue("tabName", tabName); //NOI18N
                }

                java.util.Set<Property> props = new HashSet<Property>(Arrays.asList(current.getProperties()));

                String propsHelpID = null;

                // intersection of properties from the corresponding tabs
                for (int j = 0; j < ops.size(); j++) {
                    Node.PropertySet[] p = ops.get(j);

                    for (int k = 0; k < p.length; k++) {
                        final String cn = current == null ? null : current.getName();
                        final String pkn = p[k] == null ? null : p[k].getName();
                        if (cn != null && cn.equals(pkn)) {
                            props.retainAll(new HashSet<Property>(Arrays.asList(p[k].getProperties())));
                        }
                    }
                }

                Node.Property[] p = current.getProperties();

                for (int j = 0; j < p.length; j++) {
                    if (!props.contains(p[j])) {
                        continue;
                    }

                    if (p[j].isHidden()) {
                        continue;
                    }

                    ProxyProperty pp = createProxyProperty(copy, p[j].getName(), res.getName());
                    res.put(pp);
                }

                resultSheets.add(res);
            }

            return resultSheets.toArray(new Sheet.Set[0]);
        }

        return new Sheet.Set[0];
    }

    /** Finds properties in original with specified
     * name in all tabs and constructs a ProxyProperty instance.
     */
    private ProxyProperty createProxyProperty(Node[] copy, String propName, String setName) {
        Node.Property[] arr = new Node.Property[copy.length];

        for (int i = 0; i < copy.length; i++) {
            Node.PropertySet[] p = getOriginalPropertySets(copy).get(i);

            for (int j = 0; j < p.length; j++) {
                if (Utilities.compareObjects(setName, p[j].getName())) {
                    Node.Property[] np = p[j].getProperties();

                    for (int k = 0; k < np.length; k++) {
                        if (np[k].getName().equals(propName)) {
                            arr[i] = np[k];
                        }
                    }
                }
            }
        }

        return new ProxyProperty(arr);
    }

    /** Property delegating to an array of Properties. It either
     * delegates to original[0] or applies changes to all
     * original properties.
     */
    static final class ProxyProperty extends Node.Property {
        private Node.Property[] original;

        /** It sets name, displayName and short description.
         * Remembers original.
         */
        public ProxyProperty(Node.Property[] original) {
            super(original[0].getValueType());
            this.original = original;
            setName(original[0].getName());
            setDisplayName(original[0].getDisplayName());
            setShortDescription(original[0].getShortDescription());
        }

        /** Test whether the property is writable.Calls all delegates.
         * If any of them returns false returns false, otherwise return true.
         */
        public boolean canWrite() {
            for (int i = 0; i < original.length; i++) {
                if (!original[i].canWrite()) {
                    return false;
                }
            }

            return true;
        }

        /** Test whether the property is readable. Calls all delegates.
         * If any of them returns false returns false, otherwise return true.
         * @return <CODE>true</CODE> if all delegates returned true
         */
        public boolean canRead() {
            for (int i = 0; i < original.length; i++) {
                if (!original[i].canRead()) {
                    return false;
                }
            }

            return true;
        }

        /** If all values are the same returns the value otherwise returns null.
         * @return the value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception InvocationTargetException an exception during invocation
         */
        public Object getValue() throws IllegalAccessException, java.lang.reflect.InvocationTargetException {
            Object o = original[0].getValue();
            for (int i = 0; i < original.length; i++) {
                if (!equals(o, original[i].getValue())) {
                    throw new DifferentValuesException();
                }
            }
            return o;
        }

        static boolean equals (Object a, Object b) {
            boolean aIsNull = a == null;
            boolean bIsNull = b == null;
            boolean bothNull = aIsNull && (aIsNull == bIsNull);
            if (bothNull) {
                return true;
            }
            boolean nullMismatch = aIsNull != bIsNull;
            if (nullMismatch) {
                return false;
            }
            return a.equals(b);
        }

        /** Set the value. Calls setValue on all delegates.
         * @param val the new value of the property
         * @exception IllegalAccessException cannot access the called method
         * @exception IllegalArgumentException wrong argument
         * @exception InvocationTargetException an exception during invocation
         */
        public void setValue(Object val)
        throws IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            for (int i = 0; i < original.length; i++) {
                original[i].setValue(val);
            }
        }

        /** Retrieve a named attribute with this feature.
         * If all values are the same returns the value otherwise returns null.
         * @param attributeName  The locale-independent name of the attribute
         * @return The value of the attribute.  May be null if
         *      the attribute is unknown.
         */
        public Object getValue(String attributeName) {
            Object o = original[0].getValue(attributeName);

            if (Boolean.FALSE.equals(o)) {
                //issue 38319 - Boolean.FALSE should override even null -
                //relevant primarily to the general hint canEditAsText,
                //but makes sense generally
                return o;
            }

            if (o == null) {
                return null;
            }

            for (int i = 1; i < original.length; i++) {
                if (Boolean.FALSE.equals(original[i])) {
                    // issue 38319, see comment above
                    return original[i];
                }
                if (!o.equals(original[i].getValue(attributeName))) {
                    // Optionally log it and return null
                    if (Boolean.getBoolean("netbeans.ps.logDifferentValues")) {
                        Logger.getLogger(ProxyNode.class.getName()).log(Level.WARNING, null,
                                          new DifferentValuesException("Different values in attribute " +
                                                                       attributeName +
                                                                       " for proxy property " +
                                                                       getDisplayName() +
                                                                       "(" +
                                                                       this +
                                                                       ") first value=" +
                                                                       o +
                                                                       " property " +
                                                                       i + "(" +
                                                                       original[i].getClass().getName() +
                                                                       " returns " +
                                                                       original[i].getValue(attributeName)));
                    }
                    return null;
                }
            }

            return o;
        }

        /** Associate a named attribute with this feature. Calls setValue on all delegates.
         * @param attributeName  The locale-independent name of the attribute
         * @param value  The value.
         */
        public void setValue(String attributeName, Object value) {
            for (int i = 0; i < original.length; i++) {
                original[i].setValue(attributeName, value);
            }
        }

        /**
         * @returns property editor from the first delegate
         */
        public java.beans.PropertyEditor getPropertyEditor() {
            return original[0].getPropertyEditor();
        }

        /** Test whether the property has a default value. If any of
         * the delegates does not support default value returns false,
         * otherwise returns true.
         * @return <code>true</code> if all delegates returned true
         */
        public boolean supportsDefaultValue() {
            for (int i = 0; i < original.length; i++) {
                if (!original[i].supportsDefaultValue()) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Calls restoreDefaultValue on all delegates (original).
         * @exception IllegalAccessException cannot access the called method
         * @exception InvocationTargetException an exception during invocation
         */
        public void restoreDefaultValue() throws IllegalAccessException, java.lang.reflect.InvocationTargetException {
            for (int i = 0; i < original.length; i++) {
                original[i].restoreDefaultValue();
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer("Proxy property for: ");
            sb.append(getDisplayName());
            sb.append('[');

            for (int i = 0; i < original.length; i++) {
                sb.append(original[i].getClass().getName());

                if (i < (original.length - 1)) {
                    sb.append(',');
                }
            }

            sb.append(']');

            return sb.toString();
        }
    }

    /** We cannot return a single value when there are different values */
    static class DifferentValuesException extends RuntimeException {
        public DifferentValuesException() {
            super();
        }

        public DifferentValuesException(String message) {
            super(message);
        }
    }

    private class NodeAdapterImpl extends NodeAdapter {
        private final boolean nodeListener;

        public NodeAdapterImpl(boolean b) {
            this.nodeListener = b;
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (nodeListener) {
                nodePropertyChange(pce);
            } else {
                realPropertyChange(pce);
            }
        }
        
        private void nodePropertyChange(PropertyChangeEvent pce) {
            String nm = pce.getPropertyName();

            if (PROP_COOKIE.equals(nm)) {
                fireCookieChange();
            } else if (PROP_DISPLAY_NAME.equals(nm)) {
                displayName = null;
                fireDisplayNameChange((String) pce.getOldValue(), getDisplayName());
            } else if (PROP_ICON.equals(nm)) {
                fireIconChange();
            } else if (PROP_OPENED_ICON.equals(nm)) {
                fireOpenedIconChange();
            } else if (PROP_NAME.equals(nm)) {
                fireNameChange((String) pce.getOldValue(), getName());
            } else if (PROP_PROPERTY_SETS.equals(nm)) {
                PropertySet[] old = getPropertySets();
                setSheet(createSheet());
                firePropertySetsChange(old, getPropertySets());
            } else if (PROP_SHORT_DESCRIPTION.equals(nm)) {
                fireShortDescriptionChange((String) pce.getOldValue(), getShortDescription());
            } else if (PROP_LEAF.equals(nm)) {
                //Not interesting to property sheet
            } else if (PROP_PARENT_NODE.equals(nm)) {
                //Not interesting to property sheet
            }
        }

        public void nodeDestroyed(NodeEvent ev) {
            int idx = Arrays.asList(ProxyNode.this.original).indexOf((Node) ev.getSource());

            if (idx != -1) {
                HashSet<Node> set = new HashSet<Node>(Arrays.asList(ProxyNode.this.original));
                set.remove(ev.getSource());
                ProxyNode.this.original = set.toArray(new Node[0]);

                if (set.size() == 0) {
                    ProxyNode.this.fireNodeDestroyed();
                }
            }
        }

        private void realPropertyChange(PropertyChangeEvent pce) {
            String nm = pce.getPropertyName();
            Node.PropertySet[] pss = getPropertySets();
            boolean exists = false;

            for (int i = 0; i < pss.length && !exists; i++) {
                Node.Property[] ps = pss[i].getProperties();

                for (int j = 0; j < ps.length && !exists; j++) {
                    if (ps[j].getName().equals(nm)) {
                        exists = true;
                    }
                }
            }
            if( exists ) {
                firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
            }
        }
    }
}
