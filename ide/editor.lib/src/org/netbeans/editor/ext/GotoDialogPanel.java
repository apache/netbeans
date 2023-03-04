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

package org.netbeans.editor.ext;

import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import java.util.List;
import java.util.ArrayList;

import org.netbeans.editor.EditorState;
import org.openide.util.NbBundle;

/**
 * GotoDialogPanel is an UI object for entering line numbers to move caret to.
 * It maintains its own history (stored in EditorState).
 * For proper history functionality, it is needed to call
 * <CODE>updateHistory()</CODE> for valid inserts.
 *
 * @author Miloslav Metelka, Petr Nejedly
 * @version 2.0
 */
public class GotoDialogPanel extends JPanel implements FocusListener {

    static final long serialVersionUID =-8686958102543713464L;
    private static final String HISTORY_KEY = "GotoDialogPanel.history-goto-line"; // NOI18N
    private static final int MAX_ITEMS = 20;

    /** The variable used during updating combo to prevent firing */
    private boolean dontFire = false;
    private KeyEventBlocker blocker;
    private final ResourceBundle bundle = NbBundle.getBundle(org.netbeans.editor.BaseKit.class);

    /** Initializes the UI and fetches the history */
    public GotoDialogPanel() {
        initComponents ();
        getAccessibleContext().setAccessibleName(bundle.getString("goto-title")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_goto")); // NOI18N
        gotoCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_goto-line")); // NOI18N
        List history = (List)EditorState.get( HISTORY_KEY );
        if( history == null ) history = new ArrayList();
        updateCombo( history );
    }

    /** Set the content of the history combo
     * @param content The List of items to be shown in the combo
     */
    protected void updateCombo( List content ) {
        dontFire = true;
        gotoCombo.setModel( new DefaultComboBoxModel( content.toArray() ) );
        dontFire = false;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        gotoLabel = new javax.swing.JLabel();
        gotoCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        gotoLabel.setLabelFor(gotoCombo);
        org.openide.awt.Mnemonics.setLocalizedText(gotoLabel, bundle.getString("goto-line")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(gotoLabel, gridBagConstraints);

        gotoCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 10);
        add(gotoCombo, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents



    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JComboBox gotoCombo;
    protected javax.swing.JLabel gotoLabel;
    // End of variables declaration//GEN-END:variables


    /** @return the current text from the input field */
    public String getValue() {
        return (String)gotoCombo.getEditor().getItem();
    }
    
    /** This method is to be called when caller wishes to add the current 
     * content of the input filed to the history
     */
    public void updateHistory() {
        List history = (List)EditorState.get( HISTORY_KEY );
        if( history == null ) history = new ArrayList();

        Object value = getValue();

        if( history.contains( value ) ) {
            // move it to top
            history.remove( value );
            history.add( 0, value );
        } else {
            // assure it won't hold more than MAX_ITEMS
            if( history.size() >= MAX_ITEMS )
                history = history.subList(0, MAX_ITEMS-1);
            // add the last entered value to the top
            history.add( 0, getValue() );
        }
        EditorState.put( HISTORY_KEY, history );
        
        updateCombo( history );
    }

    /** the method called to ensure that the input field would be a focused
     * component with the content selected
     */
    public void popupNotify(KeyEventBlocker blocker) {
        this.blocker = blocker;
        gotoCombo.getEditor().getEditorComponent().addFocusListener(this);
        gotoCombo.getEditor().selectAll();
        gotoCombo.getEditor().getEditorComponent().requestFocus();
    }

    public javax.swing.JComboBox getGotoCombo()
    {
        return gotoCombo;
    }

    public void focusGained(FocusEvent e) {
        if (blocker != null)
            blocker.stopBlocking();
        ((JComponent)e.getSource()).removeFocusListener(this);
    }

    public void focusLost(FocusEvent e) {
    }
}
