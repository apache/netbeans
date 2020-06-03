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
package org.netbeans.modules.cnd.debugger.common2.debugger.assembly;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.Location;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.Exceptions;

/**
 *
 */
public class DisInfoPanel extends JPanel {
    private static DisInfoPanel INSTANCE;
    
    private final JComboBox addressText;
    private final JTextField fileText;
//    private final JTextField functionText;

    public DisInfoPanel() {
        setLayout(new GridBagLayout());
        setToolTipText(Catalog.get("TIP_DisStatus")); // NOI18N

        JLabel addressLabel = new JLabel(Catalog.get("LBL_Address")); // NOI18N
        addressLabel.setToolTipText(Catalog.get("TIP_DisAddress")); // NOI18N
        addressText = new JComboBox();
        addressText.setEditable(true);
        //addressText.addActionListener(new AddressTextAction());
        addressLabel.setLabelFor(addressText);

        // 6754292
        java.awt.GridBagConstraints gridBagConstraints ;
        int gridx = 0;

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = gridx++;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 0);
        add(addressLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = gridx++;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        gridBagConstraints.weightx = 1.0;
        add(addressText, gridBagConstraints);

        JLabel fileLabel = new JLabel();
        fileLabel.setText(Catalog.get("LBL_File")); // NOI18N
        fileLabel.setToolTipText(Catalog.get("TIP_DisFile")); // NOI18N

        fileText = new JTextField();
        fileText.setColumns(15);
        fileText.setHorizontalAlignment(JTextField.LEFT);
        fileText.setEditable(false);
        fileLabel.setLabelFor(fileText);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = gridx++;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 0);
        add(fileLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = gridx++;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(fileText, gridBagConstraints);

//        JLabel functionLabel = new JLabel();
//        functionLabel.setText(Catalog.get("LBL_Function")); // NOI18N
//        functionLabel.setToolTipText(Catalog.get("TIP_DisFunction"));//NOI18N
//
//        functionText = new JTextField();
//        functionText.setColumns(15);
//        functionText.setHorizontalAlignment(JTextField.LEFT);
//        functionText.setEditable(false);
//        functionLabel.setLabelFor(functionText);

//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = gridx++;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.weightx = 0.0;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
//        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 0);
//        add(functionLabel, gridBagConstraints);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = gridx++;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
//        add(functionText, gridBagConstraints);
        
        addressText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addressChanged();
            }
        });
    }
    
    private void addressChanged() {
        String selectedItem = (String)addressText.getSelectedItem();
        
        // Avoid duplication
        boolean found = false;
        for (int i = 0; i < addressText.getItemCount(); i++ ) {
            if (addressText.getItemAt(i).equals(selectedItem)) {
                found = true;
                break;
            }
        }
        if (!found) {
            addressText.addItem(selectedItem);
        }
        
        DisassemblyUtils.showLine(1);
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
	if (debugger instanceof NativeDebuggerImpl) {
            ((NativeDebuggerImpl)debugger).disController().requestDis(selectedItem, 100, true);
        }
    }
    
    public static void setLocation(Location location) {
        if (INSTANCE != null) {
            String src = location.src();
            if (src != null) {
                src = CndPathUtilities.getBaseName(src);
            } else {
                src = "";
            }
            INSTANCE.fileText.setText(src);
//            INSTANCE.functionText.setText(location.func());
//            INSTANCE.addressText.getEditor().setItem(Address.toHexString0x(location.pc(), true));
        }
    }
    
    /**
     * Factory for creating the bar
     */
    public static final class Factory implements SideBarFactory {
        @Override
        public JComponent createSideBar(JTextComponent target) {
            try {
                if (Disassembly.isDisasm(NbEditorUtilities.getDataObject(target.getDocument()).getPrimaryFile().getURL().toString())) {
                    if (INSTANCE == null) {
                        INSTANCE = new DisInfoPanel();
                    }
                    return INSTANCE;
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }
    }
    
}
