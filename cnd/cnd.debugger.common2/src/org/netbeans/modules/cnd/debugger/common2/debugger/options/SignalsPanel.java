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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;


import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import org.openide.util.HelpCtx;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.explorer.propertysheet.PropertyEnv;

class SignalsPanel extends JPanel implements HelpCtx.Provider, PropertyChangeListener {
    private JLabel sigTableLabel;
    private JScrollPane signalScrollPane;
    private JTable signalTable;
    private SignalTableModel sigmodel;
    private Signals signals;
    private PropertyEditorSupport propertyEditor;


    /**
     * Creates new form CustomizerCompile
     */

    public SignalsPanel(PropertyEditorSupport propertyEditor,
			PropertyEnv env,
			Signals signals) {
        this.propertyEditor = propertyEditor;
        initComponents();                

	// Arrange for propertyChange to get called when OK is pressed.
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);

	assert signals.isClone() : "SignalsPanel.<init>() didn't get a clone";

	this.signals = signals;

	// Transfer out stuff to TableModel
	if (signals.count() == 0)
	    sigmodel.signalsUpdated(null);
	else
	    sigmodel.signalsUpdated(signals);
    }
    
    private Object getPropertyValue() throws IllegalStateException {
	return signals;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) &&
	    evt.getNewValue() == PropertyEnv.STATE_VALID) {

	    // OK was pressed
            propertyEditor.setValue(getPropertyValue());
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("DebuggerOptions" );
    }
    

    private static class SignalCellRenderer extends DefaultTableCellRenderer {

	public SignalCellRenderer() {
	    super();
	}

	// interface TableCellRenderer
        @Override
	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row, int column) {
	    super.getTableCellRendererComponent(table,
						value,
						isSelected,
						hasFocus,
						row,
						column);

	    TableModel model = table.getModel();
	    if (! (model instanceof SignalTableModel))
		return this;
	    SignalTableModel signalModel = (SignalTableModel) model;

	    if (! signalModel.isDefaultValue(row)) {
		Font font = getFont();
		font = font.deriveFont(Font.BOLD);
		setFont(font);
	    }

	    return this;
	}
    }

    private void initComponents() {
        GridBagConstraints gbc;
        setLayout(new GridBagLayout());

	Insets insets = new Insets(12, 12, 12, 12);
	Border margin = new EmptyBorder(insets);
	Border border = new CompoundBorder(new EtchedBorder(), margin);
	setBorder(border);

	sigTableLabel = new JLabel();
	String labelText = Catalog.get("SignalsTable");	// NOI18N
	sigTableLabel.setText(labelText);
	sigTableLabel.setDisplayedMnemonic
	    (Catalog.getMnemonic("MNEM_SignalsTable"));	// NOI18N
	gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.anchor = GridBagConstraints.WEST;
	this.add(sigTableLabel, gbc);


	signalScrollPane = new JScrollPane();
	    sigmodel = new SignalTableModel();
	    signalTable = new JTable(sigmodel);
	    signalScrollPane.setViewportView(signalTable);
	    signalTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    Catalog.setAccessibleName(signalTable,
		"ACSN_SignalsTable");		// NOI18N
	    Catalog.setAccessibleDescription(signalTable,
		"ACSD_SignalsTable");		// NOI18N
	    signalTable.setAutoCreateColumnsFromModel(false);
	    signalTable.setDefaultRenderer(Object.class,
					   new SignalCellRenderer());

	    TableColumnModel columnModel = signalTable.getColumnModel();

		DefaultComboBoxModel combomodel = 
		    new DefaultComboBoxModel(new String[] {
			Catalog.get("Signal_Ignored"), // NOI18N
			Catalog.get("Signal_Caught"), // NOI18N
			Catalog.get("Signal_Default"), // NOI18N
		    });
		JComboBox comboBox = new JComboBox(combomodel);
		comboBox.setEditable(false);
		comboBox.getAccessibleContext().setAccessibleName(
		    Catalog.get("ACSN_HandledCombo") // NOI18Nedit
		);
		TableCellEditor tce = new DefaultCellEditor(comboBox);
	    columnModel.getColumn(3).setCellEditor(tce);

	    columnModel.getColumn(0).setPreferredWidth(15);
	    columnModel.getColumn(1).setPreferredWidth(15);
	    columnModel.getColumn(2).setPreferredWidth(45);
	    columnModel.getColumn(3).setPreferredWidth(25);

	sigTableLabel.setLabelFor(signalTable);

	gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 1;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.insets = new Insets(4, 0, 0, 0);
	add(signalScrollPane, gbc);
    }
}
