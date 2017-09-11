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

import java.util.ResourceBundle;
import java.util.Vector;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.KeyStroke;
import org.openide.util.NbBundle;

/**
 * This class could be used as input of sequence of KeyStrokes.
 * {@link #getKeySequence}
 * One instance could be reused.
 * {@link #clear}
 * When actual keySequence changes, it fires PropertyChangeEvent
 * of property {@link #PROP_KEYSEQUENCE}.
 * There is additional label on the bottom, which could be set
 * with {@link #setInfoText} to pass some information to user.
 *
 * @author  David Konecny
 */

public class KeySequenceInputPanel extends javax.swing.JPanel {

    public final static String PROP_KEYSEQUENCE = "keySequence"; // NOI18N
    private Vector strokes = new Vector();
    private StringBuffer text = new StringBuffer();
    private final ResourceBundle bundle = NbBundle.getBundle(BaseKit.class);

    /** Creates new form KeySequenceInputPanel with empty sequence*/
    public KeySequenceInputPanel() {
        initComponents ();
        
        keySequenceInputField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_LBL_KSIP_Sequence")); // NOI18N
        getAccessibleContext().setAccessibleName(bundle.getString("MSP_AddTitle")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_KSIP")); // NOI18N
        
        keySequenceInputField.setFocusTraversalKeysEnabled(false);
    }

    /**
     * Clears actual sequence of KeyStrokes
     */
    public void clear() {
        strokes.clear();
        text.setLength( 0 );
        keySequenceInputField.setText( text.toString() );
        firePropertyChange( PROP_KEYSEQUENCE, null, null );
    }

    /*
     * Sets the text of JLabel locaten on the bottom of this panel
     */
    public void setInfoText( String s ) {
        collisionLabel.setText( s + ' ' ); // NOI18N
    }

    /**
     * Returns sequence of completed KeyStrokes as KeyStroke[]
     */
    public KeyStroke[] getKeySequence() {
        return (KeyStroke[])strokes.toArray( new KeyStroke[0] );
    }

    /**
     * Makes it trying to be bigger
     */
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        
        if (dim.width < 400)
            dim.width = 400;
        
        return dim;
    }

    /**
     * We're redirecting our focus to proper component.
     */
    public void requestFocus() {
        keySequenceInputField.requestFocus();
    }

    /**
     * Visual part and event handling:
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        keySequenceLabel = new javax.swing.JLabel();
        keySequenceInputField = new javax.swing.JTextField();
        collisionLabel = new javax.swing.JTextArea();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.GridBagLayout());

        keySequenceLabel.setLabelFor(keySequenceInputField);
        org.openide.awt.Mnemonics.setLocalizedText(keySequenceLabel, bundle.getString( "LBL_KSIP_Sequence" ));
        keySequenceLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 8));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(keySequenceLabel, gridBagConstraints);

        keySequenceInputField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                keySequenceInputFieldKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                keySequenceInputFieldKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                keySequenceInputFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(keySequenceInputField, gridBagConstraints);

        collisionLabel.setLineWrap(true);
        collisionLabel.setEditable(false);
        collisionLabel.setRows(2);
        collisionLabel.setForeground(java.awt.Color.red);
        collisionLabel.setBackground(getBackground());
        collisionLabel.setDisabledTextColor(java.awt.Color.red);
        collisionLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(collisionLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void keySequenceInputFieldKeyTyped (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keySequenceInputFieldKeyTyped
        evt.consume();
    }//GEN-LAST:event_keySequenceInputFieldKeyTyped

    private void keySequenceInputFieldKeyReleased (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keySequenceInputFieldKeyReleased
        evt.consume();
        keySequenceInputField.setText( text.toString() );
    }//GEN-LAST:event_keySequenceInputFieldKeyReleased

    private void keySequenceInputFieldKeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keySequenceInputFieldKeyPressed
        
        String inputText = keySequenceInputField.getText();
        if (evt.getModifiers() == 0 && 
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB,0).equals(KeyStroke.getKeyStrokeForEvent( evt )) &&
                inputText!=null && inputText.length()>0){
            keySequenceInputField.transferFocus();
            return;
        }
        
        evt.consume();

        String modif = KeyEvent.getKeyModifiersText( evt.getModifiers() );
        if( isModifier( evt.getKeyCode() ) ) {
            keySequenceInputField.setText( text.toString() + modif + '+' ); //NOI18N
        } else {
            KeyStroke stroke = KeyStroke.getKeyStrokeForEvent( evt );
            strokes.add( stroke );
            text.append( Utilities.keyStrokeToString( stroke ) );
            text.append( ' ' );
            keySequenceInputField.setText( text.toString() );
            firePropertyChange( PROP_KEYSEQUENCE, null, null );
        }
    }//GEN-LAST:event_keySequenceInputFieldKeyPressed

    private boolean isModifier( int keyCode ) {
        return (keyCode == KeyEvent.VK_ALT) ||
               (keyCode == KeyEvent.VK_ALT_GRAPH) ||
               (keyCode == KeyEvent.VK_CONTROL) ||
               (keyCode == KeyEvent.VK_SHIFT) ||
               (keyCode == KeyEvent.VK_META);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JTextArea collisionLabel;
    public javax.swing.JTextField keySequenceInputField;
    public javax.swing.JLabel keySequenceLabel;
    // End of variables declaration//GEN-END:variables
}
