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


package org.netbeans.modules.form.editors2;

import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.editors.*;
import org.openide.explorer.propertysheet.ExPropertyEditor;

import java.beans.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import java.util.ResourceBundle;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author  Pavel Buzek
 */

public class CursorEditor extends PropertyEditorSupport  implements
                                                         ExPropertyEditor, XMLPropertyEditor, 
                                                         org.netbeans.modules.form.NamedPropertyEditor {

    private static Map<String,Integer> CURSOR_TYPES = new HashMap<String,Integer>();
    private static Map<Integer,String> CURSOR_CONSTANTS = new HashMap<Integer,String>();
    static {
        CURSOR_TYPES.put(new Cursor(Cursor.CROSSHAIR_CURSOR).getName(), new Integer(Cursor.CROSSHAIR_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.DEFAULT_CURSOR).getName(), new Integer(Cursor.DEFAULT_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.E_RESIZE_CURSOR).getName(), new Integer(Cursor.E_RESIZE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.HAND_CURSOR).getName(), new Integer(Cursor.HAND_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.MOVE_CURSOR).getName(), new Integer(Cursor.MOVE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.N_RESIZE_CURSOR).getName(), new Integer(Cursor.N_RESIZE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.NE_RESIZE_CURSOR).getName(), new Integer(Cursor.NE_RESIZE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.NW_RESIZE_CURSOR).getName(), new Integer(Cursor.NW_RESIZE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.S_RESIZE_CURSOR).getName(), new Integer(Cursor.S_RESIZE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.SE_RESIZE_CURSOR).getName(), new Integer(Cursor.SE_RESIZE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.SW_RESIZE_CURSOR).getName(), new Integer(Cursor.SW_RESIZE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.TEXT_CURSOR).getName(), new Integer(Cursor.TEXT_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.W_RESIZE_CURSOR).getName(), new Integer(Cursor.W_RESIZE_CURSOR));
        CURSOR_TYPES.put(new Cursor(Cursor.WAIT_CURSOR).getName(), new Integer(Cursor.WAIT_CURSOR));

        CURSOR_CONSTANTS.put(new Integer(Cursor.CROSSHAIR_CURSOR), "java.awt.Cursor.CROSSHAIR_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.DEFAULT_CURSOR), "java.awt.Cursor.DEFAULT_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.E_RESIZE_CURSOR), "java.awt.Cursor.E_RESIZE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.HAND_CURSOR), "java.awt.Cursor.HAND_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.MOVE_CURSOR), "java.awt.Cursor.MOVE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.N_RESIZE_CURSOR), "java.awt.Cursor.N_RESIZE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.NE_RESIZE_CURSOR), "java.awt.Cursor.NE_RESIZE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.NW_RESIZE_CURSOR), "java.awt.Cursor.NW_RESIZE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.S_RESIZE_CURSOR), "java.awt.Cursor.S_RESIZE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.SE_RESIZE_CURSOR), "java.awt.Cursor.SE_RESIZE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.SW_RESIZE_CURSOR), "java.awt.Cursor.SW_RESIZE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.TEXT_CURSOR), "java.awt.Cursor.TEXT_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.W_RESIZE_CURSOR), "java.awt.Cursor.W_RESIZE_CURSOR"); // NOI18N
        CURSOR_CONSTANTS.put(new Integer(Cursor.WAIT_CURSOR), "java.awt.Cursor.WAIT_CURSOR"); // NOI18N
    }

    private Cursor current;

    private PropertyEnv env;

    /** Creates new CursorEditor */
    public CursorEditor() {
        current = new Cursor(Cursor.DEFAULT_CURSOR);
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE); // NOI18N
    }

    @Override
    public Object getValue() {
        return current;
    }

    @Override
    public void setValue(Object value) {
        if (value == null) return;
        if (value instanceof Cursor) {
            current =(Cursor) value;
            firePropertyChange();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getAsText() {
        if (current == null)
            return "null"; // NOI18N
        else
            return current.getName();
    }

    @Override
    public void setAsText(String string) {
        Object o = CURSOR_TYPES.get(string);
        if (o != null) {
            int type =((Integer) o).intValue();
            setValue(new Cursor(type));
        }
    }

    @Override
    public String[] getTags() {
        String [] tags = new String[CURSOR_TYPES.size()];
        int i=0;
        for (java.util.Iterator iter = CURSOR_TYPES.keySet().iterator(); iter.hasNext(); i++)
            tags [i] =(String) iter.next();
        return tags;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        return new CursorPanel();
    }

    @Override
    public String getJavaInitializationString() {
        if (current == null) return null; // no code to generate
        String cursorName = CURSOR_CONSTANTS.get(new Integer(current.getType()));
        if (cursorName != null)
            return "new java.awt.Cursor("+cursorName+")"; // NOI18N
        return "new java.awt.Cursor("+current.getType()+")"; // NOI18N
    }

    class CursorPanel extends JPanel implements PropertyChangeListener {
        private JList list;

        CursorPanel() {
            setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints1;
            list = new JList(new java.util.Vector<String>(CURSOR_TYPES.keySet()));
            list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            if (current != null) {
                list.setSelectedValue(current.getName(), true);
            }
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addPropertyChangeListener(this);

            ResourceBundle bundle = org.openide.util.NbBundle.getBundle(CursorEditor.class);
            JLabel cursorListLabel = new JLabel();
            Mnemonics.setLocalizedText(cursorListLabel, bundle.getString("CTL_SelectCursorName")); // NOI18N
            cursorListLabel.setLabelFor(list);
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints1.insets = new java.awt.Insets(8, 8, 8, 8);
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.weighty = 1.0;
            JScrollPane scrollPane = new JScrollPane(list);
            add(scrollPane, gridBagConstraints1);

            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.insets = new java.awt.Insets(8, 8, 0, 8);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            
            add(cursorListLabel, gridBagConstraints1);

            list.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_SelectCursorName"));
            scrollPane.getVerticalScrollBar().getAccessibleContext().setAccessibleName(bundle.getString("ACSD_CTL_SelectCursorName")); // NOI18N
            scrollPane.getVerticalScrollBar().getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_SelectCursorName")); // NOI18N
            getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CursorCustomEditor"));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
                    && evt.getNewValue() == PropertyEnv.STATE_VALID) {
                Cursor cursor;
                if (list.getSelectedValue() == null) {
                    cursor = null;
                } else {
                    int type = CURSOR_TYPES.get(list.getSelectedValue());
                    cursor = new Cursor(type);
                }
                setValue(cursor);
            }
        }
    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_CURSOR = "Color"; // NOI18N

    public static final String ATTR_ID = "id"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
     * the value should be available via the getValue method.
     * An IOException should be thrown when the value cannot be restored from the specified XML element
     * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from the specified XML element
     */
    @Override
    public void readFromXML(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_CURSOR.equals(element.getNodeName())) {
            throw new java.io.IOException();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        try {
            String id = attributes.getNamedItem(ATTR_ID).getNodeValue();
            setAsText(id);
        } catch (NullPointerException e) {
            throw new java.io.IOException();
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
     * setValue method prior to calling this method.
     * @param doc The XML document to store the XML in - should be used for creating nodes only
     * @return the XML DOM element representing a subtree of XML from which the value should be loaded
     */
    @Override
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_CURSOR);
        el.setAttribute(ATTR_ID, getAsText());
        return el;
    }

    
    // ------------------------------------------
    // NamedPropertyEditor implementation

    /** @return display name of the property editor */
    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getBundle(CursorEditor.class).getString("CTL_CursorEditor_DisplayName");
    }
    
}
