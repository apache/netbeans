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

package org.netbeans.editor;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.ListCellRenderer;
import javax.swing.KeyStroke;
import java.util.*;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/** The component for displaying and editing just recorded macro.
 * It allows you to define a name for the macro and bound keystrokes to it.
 * 
 * @author Petr Nejedly
 * @version 1.0
 * @deprecated Without any replacement. This class is no longer functional.
 */
public class MacroSavePanel extends javax.swing.JPanel {
    
    private final ResourceBundle bundle = NbBundle.getBundle(BaseKit.class);
    private Vector bindings = new Vector();
    private Class kitClass;
    
    /** Creates new form SaveMacroPanel */
    public MacroSavePanel( Class kitClass ) {
        this.kitClass = kitClass;
        initComponents ();
        
        nameField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MSP_Name")); // NOI18N
        macroField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MSP_Macro")); // NOI18N
        bindingList.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MSP_Keys")); // NOI18N
        getAccessibleContext().setAccessibleName(bundle.getString("MDS_title")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MSP")); // NOI18N
        
        // temporary loss of function
        setMaximumSize( new Dimension( 400, 200 ) );
    }

    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        Dimension max = getMaximumSize();
        if( pref.width > max.width ) pref.width = max.width;
        if( pref.height > max.height ) pref.height = max.height;
	return pref;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        macroPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        macroLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        macroField = new javax.swing.JTextField();
        bindingPanel = new javax.swing.JPanel();
        bindingLabel = new javax.swing.JLabel();
        bindingScrollPane = new javax.swing.JScrollPane();
        bindingList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());

        macroPanel.setLayout(new java.awt.GridBagLayout());

        nameLabel.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, bundle.getString( "MSP_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        macroPanel.add(nameLabel, gridBagConstraints);

        macroLabel.setLabelFor(macroField);
        org.openide.awt.Mnemonics.setLocalizedText(macroLabel, bundle.getString( "MSP_Macro"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        macroPanel.add(macroLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        macroPanel.add(nameField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        macroPanel.add(macroField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(macroPanel, gridBagConstraints);

        bindingPanel.setLayout(new java.awt.GridBagLayout());

        bindingLabel.setLabelFor(bindingList);
        org.openide.awt.Mnemonics.setLocalizedText(bindingLabel, bundle.getString("MSP_Keys"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        bindingPanel.add(bindingLabel, gridBagConstraints);

        bindingList.setCellRenderer(new KeySequenceCellRenderer());
        bindingScrollPane.setViewportView(bindingList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        bindingPanel.add(bindingScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("MSP_Add"));
        addButton.setToolTipText(bundle.getString("MSP_AddToolTip"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBindingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        bindingPanel.add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString("MSP_Remove"));
        removeButton.setToolTipText(bundle.getString( "MSP_RemoveToolTip"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBindingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        bindingPanel.add(removeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(bindingPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void removeBindingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBindingActionPerformed
        int index = bindingList.getSelectedIndex();
        if( index >= 0 ) {
            bindings.remove(index);
            bindingList.setListData(bindings);
        }
        if (bindingList.getModel().getSize() <= 0)
            removeButton.setEnabled(false);
        else
            bindingList.setSelectedIndex(0);
    }//GEN-LAST:event_removeBindingActionPerformed

    private void addBindingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBindingActionPerformed
        KeyStroke[] newKeyStrokes = new KeySequenceRequester().getKeySequence();
        
        if (newKeyStrokes != null)
        {
            bindings.add(newKeyStrokes);
            bindingList.setListData(bindings);
            bindingList.setSelectedIndex(0);
            removeButton.setEnabled(true);
        }
    }//GEN-LAST:event_addBindingActionPerformed
    
    public String getMacroName() {
        return nameField.getText();
    }

    public void setMacroName( String name ) {
        nameField.setText( name );
    }

    public String getMacroBody() {
        return macroField.getText();
    }

    public void setMacroBody( String body ) {
        macroField.setText( body );
    }

    /** @return List of KeyStroke[] */
    public List getKeySequences() {
        return new ArrayList( bindings );
    }

    /** @param sequences List of KeyStroke[] bounds to this macro */
    public void setKeySequences( List sequences ) {
        bindings = new Vector( sequences );
        bindingList.setListData( bindings );
    }    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton addButton;
    public javax.swing.JLabel bindingLabel;
    public javax.swing.JList bindingList;
    public javax.swing.JPanel bindingPanel;
    public javax.swing.JScrollPane bindingScrollPane;
    public javax.swing.JTextField macroField;
    public javax.swing.JLabel macroLabel;
    public javax.swing.JPanel macroPanel;
    public javax.swing.JTextField nameField;
    public javax.swing.JLabel nameLabel;
    public javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    
    public void popupNotify() {
        nameField.requestFocus();
    }

    
    private static class KeySequenceCellRenderer extends JLabel implements ListCellRenderer {
        public KeySequenceCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText( Utilities.keySequenceToString( (KeyStroke[])value ) );
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground() );
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground() );
            return this;
        }        
    }
    

    /**
     * Encapsulation for components of dialog asking for new KeySequence
     */
    class KeySequenceRequester {

        KeySequenceInputPanel panel;
        Dialog dial;

        JButton[] buttons = { new JButton(bundle.getString("MSP_ok")),  // NOI18N
                              new JButton(), // NOI18N
                              new JButton(bundle.getString("MSP_cancel"))}; // NOI18N

        KeyStroke[] retVal = null;


        KeySequenceRequester() {
            ((JButton)buttons[0]).getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MSP_ok")); // NOI18N
            ((JButton)buttons[1]).getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MSP_clear")); // NOI18N
            Mnemonics.setLocalizedText((JButton)buttons[1], bundle.getString("MSP_clear"));
            ((JButton)buttons[2]).getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MSP_cancel")); // NOI18N
            ((JButton)buttons[0]).setEnabled( false ); // default initial state

            // Prepare KeySequence input dialog
            panel = new KeySequenceInputPanel();
            panel.addPropertyChangeListener( new PropertyChangeListener() {
                                                 public void propertyChange( PropertyChangeEvent evt ) {
                                                     if( KeySequenceInputPanel.PROP_KEYSEQUENCE != evt.getPropertyName() ) return;
                                                     KeyStroke[] seq = panel.getKeySequence();
                                                     String warn = isAlreadyBounded( seq );
                                                     if (warn == null)
                                                        warn = getCollisionString( seq );
                                                     ((JButton)buttons[0]).setEnabled( seq.length > 0 && warn == null );
                                                     panel.setInfoText( warn == null ? "" : warn );  // NOI18N
                                                 }
                                             } );

            dial = DialogSupport.createDialog(
                bundle.getString("MSP_AddTitle"), // NOI18N
                panel, true, buttons, false, -1, 2, new ActionListener(){
                                            public void actionPerformed( ActionEvent evt ) {
                                                if( evt.getSource() == buttons[1] ) { // Clear pressed
                                                    panel.clear();          // Clear entered KeyStrokes, start again
                                                    panel.requestFocus();   // Make user imediately able to enter new strokes
                                                } else if( evt.getSource() == buttons[0] ) { // OK pressed
                                                    retVal = panel.getKeySequence();
                                                    dial.dispose();  // Done
                                                } else if( evt.getSource() == buttons[2] ) { // OK pressed
                                                    retVal = null;
                                                    dial.dispose();  // Done
                                                }
                                            }
                                        });

        }

        KeyStroke[] getKeySequence() {
            dial.pack();
            panel.requestFocus();
            dial.show();
            return retVal;
        }

        /** Check whether this KeyStroke is already bounded to this macro or not.
         * Disallow to duplicate the KeyStroke.
         */
        String isAlreadyBounded( KeyStroke[] seq ) {
            if( seq.length == 0 ) return null; // NOI18N   not valid sequence, but don't alert user

            Iterator it = bindings.iterator();
            while( it.hasNext() ) {
                if( isOverlapingSequence( (KeyStroke[])it.next(), seq ) ) {
                    return bundle.getString( "MSP_Collision" ); // NOI18N
                }
            }
            return null;  // no colliding sequence
        }

        String getCollisionString( KeyStroke[] seq ) {
//            if( seq.length == 0 ) return null; // NOI18N   not valid sequence, but don't alert user
//
//            Settings.KitAndValue[] kv = Settings.getValueHierarchy( kitClass, SettingsNames.KEY_BINDING_LIST );
//            for (int i = 0; i < kv.length; i++)
//            {
//                Iterator iter = ((List)kv[i].value).iterator();
//                while( iter.hasNext() ) {
//                    MultiKeyBinding b = (MultiKeyBinding)iter.next();
//                    KeyStroke[] ks = b.keys;
//                    if (ks == null && b.key != null)
//                    {
//                        ks = new KeyStroke[1];
//                        ks[0] = b.key;
//                    }
//                    if( ks !=  null && isOverlapingSequence( ks, seq ) ) {
//                        Object[] values = { Utilities.keySequenceToString( ks ), b.actionName };
//                        return MessageFormat.format( bundle.getString( "MSP_FMT_Collision" ), values ); // NOI18N
//                    }
//                }
//            }
            return null;  // no colliding sequence
        }
        
        private boolean isOverlapingSequence( KeyStroke[] s1, KeyStroke[] s2 ) {
            int l = Math.min( s1.length, s2.length );
            if (l == 0)
                return false;
            while( l-- > 0 ) if( !s1[l].equals( s2[l] ) ) return false;
            return true;
        }
    }
    
}
