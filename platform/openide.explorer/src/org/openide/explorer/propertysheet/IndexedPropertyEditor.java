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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

import java.awt.Component;

import java.beans.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import java.util.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Exceptions;


/**
* This class encapsulates working with indexed properties.
*/
class IndexedPropertyEditor extends Object implements ExPropertyEditor {
    //XXX this class should be rewritten 
    //and moved to the Nodes package where it belongs - TDB
    // -----------------------------------------------------------------------------
    // Private variables
    private Object[] array;
    private PropertyEnv env;
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    private Node.IndexedProperty indexedProperty = null;
    private IndexedEditorPanel currentEditorPanel;

    // -----------------------------------------------------------------------------
    // init
    public IndexedPropertyEditor() {
    }

    // -----------------------------------------------------------------------------
    // ExPropertyEditor implementation
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        env.setChangeImmediate(false);

        FeatureDescriptor details = env.getFeatureDescriptor();

        if (details instanceof Node.IndexedProperty) {
            indexedProperty = (Node.IndexedProperty) details;
        } else {
            throw new IllegalStateException("This is not an array: " + details); // NOI18N
        }
    }

    // -----------------------------------------------------------------------------
    // PropertyEditor implementation
    public void setValue(Object value) {
        if (value == null) {
            array = null;
            firePropertyChange();

            return;
        }

        if (!value.getClass().isArray()) {
            throw new IllegalArgumentException(
                (env != null) ? ("Property whose value is not an array " + env.getFeatureDescriptor().getName())
                              : "Unknown property - not attached yet."
            ); //NOI18N
        }

        if (value.getClass().getComponentType().isPrimitive()) {
            array = Utilities.toObjectArray(value);
        } else {
            array = (Object[]) Array.newInstance(value.getClass().getComponentType(), ((Object[]) value).length);
            System.arraycopy(value, 0, array, 0, array.length);
        }

        firePropertyChange();
    }

    public Object getValue() {
        if (array == null) {
            return null;
        }

        if (indexedProperty.getElementType().isPrimitive()) {
            return Utilities.toPrimitiveArray(array);
        }

        return array;
    }

    public boolean isPaintable() {
        return false;
    }

    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
    }

    public String getJavaInitializationString(int index) {
        if (array[index] == null) {
            return "null"; // NOI18N
        }

        try {
            indexedProperty.getIndexedPropertyEditor().setValue(array[index]);

            return indexedProperty.getIndexedPropertyEditor().getJavaInitializationString();
        } catch (NullPointerException e) {
            return "null"; // NOI18N
        }
    }

    public String getJavaInitializationString() {
        if (array == null) {
            return ""; // NOI18N
        }

        StringBuilder buf = new StringBuilder("new "); // NOI18N
        buf.append(indexedProperty.getElementType().getCanonicalName());

        // empty array
        if (array.length == 0) {
            buf.append("[0]"); // NOI18N
        } else
        // non-empty array
         {
            buf.append("[] {\n\t"); // NOI18N

            for (int i = 0; i < array.length; i++) {
                PropertyEditor ed = indexedProperty.getIndexedPropertyEditor();
                if (ed != null) {
                    ed.setValue(array[i]);
                    buf.append(ed.getJavaInitializationString());
                } else {
                    buf.append("???"); // NOI18N
                }

                if (i != (array.length - 1)) {
                    buf.append(",\n\t"); // NOI18N
                } else {
                    buf.append("\n"); // NOI18N
                }
            }

            buf.append("}"); // NOI18N
        }

        return buf.toString();
    }

    public String getAsText() {
        if (array == null) {
            return "null"; // NOI18N
        }

        StringBuffer buf = new StringBuffer("["); // NOI18N
        PropertyEditor p = null;

        if (indexedProperty != null) {
            p = indexedProperty.getIndexedPropertyEditor();
        }

        for (int i = 0; i < array.length; i++) {
            if (p != null) {
                p.setValue(array[i]);

                // bugfix #27361, append property's value as text instead of java initialization string
                buf.append(p.getAsText());
            } else {
                buf.append("null"); // NOI18N
            }

            if (i != (array.length - 1)) {
                buf.append(", "); // NOI18N
            }
        }

        buf.append("]"); // NOI18N

        return buf.toString();
    }

    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        if (text.equals("null")) { // NOI18N
            setValue(null);

            return;
        }

        if (text.equals("[]")) { // NOI18N
            setValue(Array.newInstance(indexedProperty.getElementType(), 0));

            return;
        }

        int i1 = text.indexOf('[');

        if ((i1 < 0) || ((i1 + 1) >= text.length())) {
            i1 = 0;
        } else {
            i1++;
        }

        int i2 = text.lastIndexOf(']');

        if (i2 < 0) {
            i2 = text.length();
        }

        if ((i2 < i1) || (i2 > text.length())) {
            return;
        } else {
        }

        try {
            PropertyEditor p = indexedProperty.getIndexedPropertyEditor();

            if (p == null) { //Test for no editor, it's not guaranteed there will be one
                throw new IllegalStateException("Indexed type has no property " + "editor");
            }

            text = text.substring(i1, i2);

            StringTokenizer tok = new StringTokenizer(text, ","); // NOI18N
            java.util.List<Object> list = new LinkedList<Object>();

            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();
                p.setAsText(s.trim());
                list.add(p.getValue());
            }

            Object[] a = list.toArray((Object[]) Array.newInstance(getConvertedType(), list.size()));
            setValue(a);
        } catch (Exception x) {
            IllegalArgumentException iae = new IllegalArgumentException();
            Exceptions.attachLocalizedMessage(iae, getString("EXC_ErrorInIndexedSetter"));
            throw iae;
        }
    }

    public String[] getTags() {
        return null;
    }

    public Component getCustomEditor() {
        if (array == null) {
            array = (Object[]) Array.newInstance(getConvertedType(), 0);
            firePropertyChange();
        }

        Node dummy = new DisplayIndexedNode(0);

        // beware - this will function only if the DisplayIndexedNode has
        // one property on the first sheet and the property is of type
        // ValueProp
        Node.Property prop = dummy.getPropertySets()[0].getProperties()[0];

        Node.Property[] np = new Node.Property[] { prop };
        currentEditorPanel = new IndexedEditorPanel(createRootNode(), np);

        return currentEditorPanel;
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    // other methods ........................................................................
    private Node createRootNode() {
        DisplayIndexedNode[] n = new DisplayIndexedNode[array.length];

        for (int i = 0; i < n.length; i++) {
            n[i] = new DisplayIndexedNode(i);
        }

        MyIndexedRootNode idr = new MyIndexedRootNode(n);
        Index ind = idr.getCookie(Index.class);

        for (int i = 0; i < n.length; i++) {
            ind.addChangeListener(org.openide.util.WeakListeners.change(n[i], ind));
        }

        return idr;
    }

    /**
     * Converts indexedProperty.getElementType() class
     */
    private Class getConvertedType() {
        Class type = indexedProperty.getElementType();

        if (type.isPrimitive()) {
            type = Utilities.getObjectType(type);
        }

        return type;
    }

    void firePropertyChange() {
        propertySupport.firePropertyChange("value", null, null); // NOI18N
    }

    private static String getString(String key) {
        return NbBundle.getMessage(IndexedPropertyEditor.class, key);
    }

    /**
     * Makes an attempt to create new value of the property. Usefull
     * especially when enlarging the array.
     */
    private Object defaultValue() {
        Object value = null;

        if (indexedProperty.getElementType().isPrimitive()) {
            if (getConvertedType().equals(Integer.class)) {
                value = new Integer(0);
            }

            if (getConvertedType().equals(Boolean.class)) {
                value = Boolean.FALSE;
            }

            if (getConvertedType().equals(Byte.class)) {
                value = (byte)0;
            }

            if (getConvertedType().equals(Character.class)) {
                value = new Character('\u0000');
            }

            if (getConvertedType().equals(Double.class)) {
                value = 0D;
            }

            if (getConvertedType().equals(Float.class)) {
                value = 0F;
            }

            if (getConvertedType().equals(Long.class)) {
                value = new Long(0L);
            }

            if (getConvertedType().equals(Short.class)) {
                value = (short)0;
            }
        } else {
            try {
                value = getConvertedType().getDeclaredConstructor().newInstance();
            } catch (Exception x) {
                // ignore any exception - if this fails just
                // leave null as the value
            }
        }

        return value;
    }

    /**
     * Node displayed in TableSheetView. Encapsulates a value of
     * one element in the array.
     */
    class DisplayIndexedNode extends AbstractNode implements ChangeListener {
        private int index;

        public DisplayIndexedNode(int index) {
            super(Children.LEAF);
            this.index = index;
            setName(Integer.toString(index));
            setDisplayName(Integer.toString(index));
        }

        protected SystemAction[] createActions() {
            try {
                return new SystemAction[] {
                    SystemAction.get(Class.forName("org.openide.actions.MoveUpAction").asSubclass(SystemAction.class)), // NOI18N
                    SystemAction.get(Class.forName("org.openide.actions.MoveDownAction").asSubclass(SystemAction.class)), // NOI18N
                };
            } catch (ClassNotFoundException cnfe) {
                // silently ignore in case we are in standalone library
                // without these actions
            }

            return null;
        }

        // Create a property sheet:
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);

            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }

            props.put(new ValueProp());

            return sheet;
        }

        /**
         * Destroy doesn't do regular destroy here
         * but changes the whole array and recreates
         * the node hierarchy. Does *not* even call super.destroy()!
         */
        public void destroy() throws java.io.IOException {
            Object[] newArray = (Object[]) Array.newInstance(getConvertedType(), array.length - 1);
            System.arraycopy(array, 0, newArray, 0, index);
            System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);

            // throw away the old array!
            array = newArray;
            IndexedPropertyEditor.this.firePropertyChange();

            if (currentEditorPanel != null) {
                currentEditorPanel.getExplorerManager().setRootContext(createRootNode());
            }
        }

        /**
         * Listens on parent node. Assumes that parent node is
         * Indexed node !
         */
        public void stateChanged(ChangeEvent e) {
            Node parent = getParentNode();
            Index i = parent.getCookie(Index.class);

            if (i != null) {
                int currentIndex = i.indexOf(this);

                if (currentIndex != index) {
                    if (currentIndex > index) {
                        // first swap the values in array
                        // the condition is there in order react by swapping
                        // the values only on one of the two nodes
                        Object tmp = array[index];
                        array[index] = array[currentIndex];
                        array[currentIndex] = tmp;
                    }

                    // update the index variable and change the name
                    index = currentIndex;
                    DisplayIndexedNode.this.firePropertyChange(null, null, null);
                    setDisplayName(Integer.toString(index));
                    IndexedPropertyEditor.this.firePropertyChange();
                }
            }
        }

        private class ValueProp extends PropertySupport {
            public ValueProp() {
                super(
                    indexedProperty.getName(), indexedProperty.getElementType(), indexedProperty.getDisplayName(),
                    indexedProperty.getShortDescription(), indexedProperty.canRead(), indexedProperty.canWrite()
                );
            }

            public Object getValue() {
                if (index < array.length) {
                    return array[index];
                } else {
                    return null;
                }
            }

            public void setValue(Object value)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
                Object oldVal = array[index];
                array[index] = value;
                DisplayIndexedNode.this.firePropertyChange(this.getName(), oldVal, value);
                IndexedPropertyEditor.this.firePropertyChange();
            }

            public PropertyEditor getPropertyEditor() {
                return indexedProperty.getIndexedPropertyEditor();
            }
        }
    }

    /** Root node in the TableSheetView. This node should
     * not be displayed.
     */
    private class MyIndexedRootNode extends IndexedNode {
        public MyIndexedRootNode(Node[] ch) {
            getChildren().add(ch);
            setName("IndexedRoot"); // NOI18N
            setDisplayName(NbBundle.getMessage(IndexedPropertyEditor.class, "CTL_Index"));
        }

        public NewType[] getNewTypes() {
            NewType nt = new NewType() {
                    public void create() {
                        if (array != null) {
                            Object[] newArray = (Object[]) Array.newInstance(getConvertedType(), array.length + 1);
                            System.arraycopy(array, 0, newArray, 0, array.length);

                            // throw away the old array!
                            array = newArray;
                            array[array.length - 1] = defaultValue();
                        } else {
                            // throw away the old array!
                            array = (Object[]) Array.newInstance(getConvertedType(), 1);
                            array[0] = defaultValue();
                        }

                        IndexedPropertyEditor.this.firePropertyChange();

                        DisplayIndexedNode din = new DisplayIndexedNode(array.length - 1);
                        getChildren().add(new Node[] { din });

                        Index i = getCookie(Index.class);
                        i.addChangeListener(org.openide.util.WeakListeners.change(din, i));
                    }
                };

            return new NewType[] { nt };
        }
    }
}
