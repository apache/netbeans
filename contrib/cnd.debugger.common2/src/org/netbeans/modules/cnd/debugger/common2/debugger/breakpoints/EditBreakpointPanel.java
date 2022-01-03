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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.HelpCtx;

/**
 * The panel inserted into the Customize/NewBreakpoint dialog created in 
 *	BreakpointModel.CustomizeBreakpointProcessor
 * and
 *	actions.NewBreakpointAction.NewBreakpointProcessor
 *
 * Modelled on debuggercore/...modules/debugger/ui/actions/AddBreakpointPanel
 * It contains a BreakpointPanel.
 */

public class EditBreakpointPanel extends javax.swing.JPanel 
				implements HelpCtx.Provider {

    public static final String PROP_TYPE = "type"; // NOI18N

    private JComboBox combo_type;
    private JPanel custom_panel;
    private BreakpointPanel customizer;
    private boolean customizing = false;

    private List<BreakpointType> types = new ArrayList<BreakpointType>();	// map index to BreakpointType

    /**
     * If 'editableBreakpoint' is non-null we're customizing
     */

    public EditBreakpointPanel(NativeBreakpoint editableBreakpoint) {
	if (editableBreakpoint != null)
	    customizing = true;

        // get a list of all available breakpoint types
        final List<? extends BreakpointType> breakpointTypes =
                DebuggerManager.getDebuggerManager().lookup(null, BreakpointType.class);

        int defaultEntry = -1;

        initComponents();

        if (breakpointTypes == null) {
            System.out.println("No BreakpointTypes"); // NOI18N
        } else if (breakpointTypes.isEmpty()) {
            System.out.println("Zero BreakpointTypes"); // NOI18N
        } else {
            for (BreakpointType bt : breakpointTypes) {
                String category = bt.getCategoryDisplayName();
                if (customizing) {
                    if (editableBreakpoint.isOfType(bt)) {
                        String type = bt.getTypeDisplayName();
                        types.add(bt);
                        combo_type.addItem(type);
                    }
                } else {
                    if (NativeBreakpointType.isOurs(category)) {
                        String type = bt.getTypeDisplayName();
                        if (bt.isDefault()) {
                            defaultEntry = combo_type.getItemCount();
                        }
                        types.add(bt);
                        combo_type.addItem(type);
                    }
                }
            }
        }

	if (customizing) {
	    combo_type.setSelectedIndex(0);
	    BreakpointType bt = editableBreakpoint.getBreakpointType();

	    switchTo(bt, editableBreakpoint);
	} else {
	    combo_type.addActionListener(new ActionListener() {
                @Override
		public void actionPerformed(ActionEvent e) {
		    int x = combo_type.getSelectedIndex();
		    BreakpointType bt = types.get(x);
		    // System.out.println("Type selected " + x + " " + bt);
		    switchTo(bt, null);
		}
	    } );
	    if (defaultEntry != -1)
		combo_type.setSelectedIndex(defaultEntry);
	}
    }

    public Controller getController() {
	return customizer.getController();
    } 


    private void initComponents() {
	if (customizing) {
	    Catalog.setAccessibleDescription(this,
					     "ACSD_CustomizeBreakpointPanel"); // NOI18N
	} else {
	    Catalog.setAccessibleDescription(this,
					     "ACSD_NewBreakpointPanel"); // NOI18N
	}

	
	setLayout(new GridBagLayout());
	GridBagConstraints constraints;

	JLabel lab_type = new JLabel();
	    lab_type.setText(Catalog.get("CTL_Breakpoint_type"));// NOI18N
	    lab_type.setDisplayedMnemonic(
		Catalog.getMnemonic("MNEM_Breakpoint_type"));	// NOI18N

	    constraints = new GridBagConstraints();
	    constraints.gridwidth = 2;
	    constraints.insets = new Insets(12, 12, 0, 0);
	    add(lab_type, constraints);

	combo_type = new JComboBox();
	    Catalog.setAccessibleDescription(combo_type,
		"ACSD_Breakpoint_type"); // NOI18N
	    combo_type.setMaximumRowCount(12);

	    constraints = new GridBagConstraints();
	    constraints.gridwidth = 0;
	    constraints.insets = new Insets(12, 12, 0, 12);
	    constraints.anchor = GridBagConstraints.WEST;

	    add(combo_type, constraints);

	if (customizing)
	    combo_type.setEnabled(false);
	lab_type.setLabelFor(combo_type);

	custom_panel = new JPanel();
	    custom_panel.setLayout(new BorderLayout());

	    constraints = new GridBagConstraints();
	    constraints.gridwidth = 0;
	    constraints.insets = new Insets(9, 9, 0, 9);
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.weightx = 1.0;
	    constraints.weighty = 1.0;

	    add(custom_panel, constraints);

    }
    
    private void switchTo(BreakpointType bt,
			  NativeBreakpoint editableBreakpoint) {

	if (customizing) {
	    customizer = ((NativeBreakpointType)bt).getCustomizer(editableBreakpoint);
	} else {
	    customizer = ((NativeBreakpointType)bt).getCustomizer(null);
	}

	custom_panel.removeAll();
	custom_panel.add(customizer, "Center");	// NOI18N

	revalidate();

	Window parent = SwingUtilities.windowForComponent(this);
	if (parent != null)	// window might not be realised yet
	    parent.pack();
        firePropertyChange (PROP_TYPE, null, null);
    }

    // Implements HelpCtx.Provider
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("Breakpoints");     // NOI18N
    }

}
