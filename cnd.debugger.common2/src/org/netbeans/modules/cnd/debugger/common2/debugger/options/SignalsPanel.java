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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
