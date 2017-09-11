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
package org.netbeans.modules.xml.tax.beans.customizer;

import java.awt.Component;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeNamedObjectMap;
import org.netbeans.tax.TreeAttlistDecl;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.beans.Lib;
import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeAttlistDeclCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID = 5365016831221845705L;
    
    
    //
    // init
    //
    
    /** */
    public TreeAttlistDeclCustomizer () {
        super ();
        
        initComponents ();
        elemNameLabel.setDisplayedMnemonic (Util.THIS.getChar ("TreeAttributeDeclCustomizer.elemNameLabel.mne")); // NOI18N
        tableLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_attlistdecl_attributelist_label")); // NOI18N
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeAttlistDecl getAttlistDecl () {
        return (TreeAttlistDecl)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeAttlistDecl.PROP_ELEMENT_NAME)) {
            updateElementNameComponent ();
        }
    }
    
    /**
     */
    protected final void updateElementNameComponent () {
        elemNameField.setText (getAttlistDecl ().getElementName ());
    }
    
    
    /**
     */
    protected final void updateAttlistDeclElementName () {
        try {
            getAttlistDecl ().setElementName (elemNameField.getText ());
        } catch (TreeException exc) {
            updateElementNameComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateElementNameComponent ();
    }
    
    
    /**
     */
    protected void ownInitComponents () {
        TreeNamedObjectMap attributes = getAttlistDecl ().getAttributeDefs ();
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeElementCustomizer::ownInitComponents: attributes = " + attributes); // NOI18N

        Component attrsCustom = Lib.getCustomizer (TreeAttlistDecl.class, attributes, "attributeDefs"); // "attributeDefs" - name of TreeAttlistDecl property // NOI18N
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeElementCustomizer::ownInitComponents: attrsCustom = " + attrsCustom); // NOI18N

        if (attrsCustom != null) {
            attributeDefsPanel.add (attrsCustom, BorderLayout.CENTER);
        }
    }
    
    /**
     */
    protected final void updateReadOnlyStatus (boolean editable) {
        elemNameField.setEditable (editable);
        attributeDefsPanel.setEnabled (editable); //???
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        elemNameLabel = new javax.swing.JLabel();
        elemNameField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        tableLabel = new javax.swing.JLabel();
        attributeDefsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        elemNameLabel.setText(Util.THIS.getString ("TreeAttributeDeclCustomizer.elemNameLabel.text"));
        elemNameLabel.setLabelFor(elemNameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(elemNameLabel, gridBagConstraints);

        elemNameField.setColumns(20);
        elemNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                elemNameFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(elemNameField, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        tableLabel.setText(Util.THIS.getString ("TEXT_attlistdecl_attributelist_label"));
        tableLabel.setLabelFor(attributeDefsPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        jPanel2.add(tableLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(jPanel2, gridBagConstraints);

        attributeDefsPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(attributeDefsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void elemNameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_elemNameFieldFocusGained
        // Accessibility:
        elemNameField.selectAll ();
    }//GEN-LAST:event_elemNameFieldFocusGained
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel tableLabel;
    private javax.swing.JPanel attributeDefsPanel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel elemNameLabel;
    private javax.swing.JTextField elemNameField;
    // End of variables declaration//GEN-END:variables
    
    
    /** Initialize accesibility
     */
    public void initAccessibility (){
        
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeAttlistDeclCustomizer"));
        elemNameField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_elemNameField"));
        elemNameField.selectAll ();
    }
}
