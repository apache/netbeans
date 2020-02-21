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


package org.netbeans.modules.cnd.debugger.common2.values;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.beans.PropertyEditor;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.explorer.propertysheet.InplaceEditor;


/*
 * PropertyEditor for count limits.
 * We'd like to be able to have the user enter a number but also choose 
 * some pre-canned values from a pull-down:
 * "<disable counting>", "Infinity", "Current Count value",
 */

public class CountLimitEditor extends /* OLD Enhanced */ AsyncEditor
    implements ExPropertyEditor {

    final static String[] sa = {CountLimit.Action_INFINITY,
				CountLimit.Action_CURRENT,
				CountLimit.Action_DISABLE};
    @Override
    public String[] getTags() {
	return sa;
    }

    @Override
    public void setAsText(String newText) {
	// Called when an edit is commited through user action

	CountLimit cl = new CountLimit(newText);
	if (cl.errorMessage() != null)
	    badValue(cl.errorMessage());

	notePending(newText);

	// Propagate the new value to the node property setter which
	// will forward the property to the engine.
	setValue(cl);
    }

    // interface ExPropertyEditor
    @Override
    public void attachEnv(PropertyEnv env) {
	/* LATER

	CountLimitEditorComponent is an attempt to workaround the 
	problems described in IZ 76522. 
	It essentially starts reproducing
	    org.openide.explorer.propertysheet.ComboInplaceEditor
	but the focus stuff just got too complicated.

	CountLimitEditorComponent clec = new CountLimitEditorComponent();
	env.getFeatureDescriptor().setValue("inplaceEditor", clec);
	*/
	env.getFeatureDescriptor().setValue("canEditAsText", true); // NOI18N
    }


    private static class CountLimitEditorComponent
	extends JPanel implements ActionListener,
				  PopupMenuListener,
				  InplaceEditor {

	private final JComboBox comboBox;
	private JPopupMenu popupMenu;

	// state used by InplaceEditor:
	private PropertyModel propertyModel;
	private PropertyEditor propertyEditor;

	public CountLimitEditorComponent() {
	    setLayout(new BorderLayout());
	    comboBox = new JComboBox(sa);
	    comboBox.setEditable(true);

	    putClientProperty("JComboBox.isTableCellEditor", false); // NOI18N
	    comboBox.setLightWeightPopupEnabled(false);

	    comboBox.addActionListener(this);
	    comboBox.addPopupMenuListener(this);
	    add(comboBox, BorderLayout.CENTER);
	}

	// interface ActionListener
        @Override
	public void actionPerformed(ActionEvent e) {
	    System.out.printf("CountLimitEditorComponent.actionPerformed(): %s\n", comboBox.getSelectedItem()); // NOI18N
	    if (comboBox.getSelectedItem() instanceof String) {
		String newSelection = (String) comboBox.getSelectedItem();
	    }
	}

	// interface PopupMenuListener
        @Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	    popupMenu = null;
	}

	// interface PopupMenuListener
        @Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	    popupMenu = null;
	}

	// interface PopupMenuListener
        @Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	    // OLD System.out.printf("popupMenuWillBecomeVisible(): %s\n", e);
	    // OLD popupMenu = (JPopupMenu) e.getSource();
	}

	// interface InplaceEditor
        @Override
	public void addActionListener(ActionListener al) {
	    comboBox.addActionListener(al);
	}

	// interface InplaceEditor
        @Override
	public void removeActionListener(ActionListener al) {
	    comboBox.removeActionListener(al);
	}

	// interface InplaceEditor
        @Override
	public void clear() {
	    propertyEditor = null;
	}

	// interface InplaceEditor
        @Override
	public void connect(PropertyEditor pe, PropertyEnv env) {
	    this.propertyEditor = pe;
	    comboBox.setSelectedItem(pe.getValue());
	}

	// interface InplaceEditor
        @Override
	public JComponent getComponent() {
	    return this;
	}

	// interface InplaceEditor
        @Override
	public KeyStroke[] getKeyStrokes() {
	    return new KeyStroke[0];
	}

	// interface InplaceEditor
        @Override
	public PropertyEditor getPropertyEditor() {
	    return propertyEditor;
	}

	// interface InplaceEditor
        @Override
	public PropertyModel getPropertyModel() {
	    return propertyModel;
	}

	// interface InplaceEditor
        @Override
	public Object getValue() {
	    return comboBox.getSelectedItem();
	}

	// interface InplaceEditor
        @Override
	public boolean isKnownComponent(Component c) {
	    System.out.printf("isKnownComponent(): %s\n", c); // NOI18N
	    if (c == this ||
		c == comboBox ||
		c == comboBox.getEditor().getEditorComponent() ||
		c == popupMenu ||
		comboBox.isPopupVisible()) {
		return true;
	    } else {
		return false;
	    }
	}

	// interface InplaceEditor
        @Override
	public void reset() {
	    comboBox.setSelectedItem(propertyEditor.getValue());
	}

	// interface InplaceEditor
        @Override
	public void setPropertyModel(PropertyModel pm) {
	    this.propertyModel = pm;
	}

	// interface InplaceEditor
        @Override
	public void setValue(Object o) {
	    comboBox.setSelectedItem(o);
	}

	// interface InplaceEditor
        @Override
	public boolean supportsTextEntry() {
	    return true;
	}
    }
}

