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

package org.netbeans.beaninfo.editors;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import org.netbeans.core.UIExceptions;
import org.openide.awt.Mnemonics;

import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 *
 * @author  IanFormanek, Vladimir Zboril
 */
public class DimensionCustomEditor extends IntegerCustomEditor {

    static final long serialVersionUID =3718340148720193844L;

    /** Creates new form DimensionCustomEditor */
    public DimensionCustomEditor(DimensionEditor editor) {
        super (editor.env );
        initComponents();
        this.editor = editor;
        this.editor.env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        this.editor.env.addPropertyChangeListener(this);

        Dimension dimension = (Dimension)editor.getValue ();
        
        if (dimension == null) dimension = new Dimension (0, 0);
        jLabel1.setText(NbBundle.getMessage(DimensionCustomEditor.class, "CTL_Dimension"));
        Mnemonics.setLocalizedText(widthLabel, NbBundle.getMessage(DimensionCustomEditor.class, "CTL_Width"));
        widthLabel.setLabelFor(widthField);
        Mnemonics.setLocalizedText(heightLabel, NbBundle.getMessage(DimensionCustomEditor.class, "CTL_Height"));
        heightLabel.setLabelFor(heightField);

        widthField.setText (String.valueOf(dimension.width));
        heightField.setText (String.valueOf(dimension.height));
//        HelpCtx.setHelpIDString (this, DimensionCustomEditor.class.getName ());

        widthField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DimensionCustomEditor.class, "ACSD_CTL_Width"));
        heightField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DimensionCustomEditor.class, "ACSD_CTL_Height"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DimensionCustomEditor.class, "ACSD_DimensionCustomEditor"));
        
        setPanel( insidePanel );
        widthField.addKeyListener( this );
        heightField.addKeyListener( this );
        getMap().put( widthField, widthLabel );
        getMap().put( heightField , heightLabel );
    }

    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (280, 160);
    }

    private Object getPropertyValue () throws IllegalStateException {
        try {
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
            if ((width < 0) || (height < 0)) {
                IllegalStateException ise = new IllegalStateException();
                UIExceptions.annotateUser(ise, null, 
                    NbBundle.getMessage(DimensionCustomEditor.class, "CTL_NegativeSize"), null, null);
                throw ise;
            }
            return new Dimension (width, height);
        } catch (NumberFormatException e) {
            IllegalStateException ise = new IllegalStateException();
            UIExceptions.annotateUser(ise, null, 
                NbBundle.getMessage(DimensionCustomEditor.class, "CTL_InvalidValue"), null, null);
            throw ise;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    
    @Override
    protected void updateValues() {
        try {
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
            editor.setValue (new Dimension (width, height));
        } catch (NumberFormatException e) {
            // [PENDING beep]
        }        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        insidePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        widthLabel = new javax.swing.JLabel();
        widthField = new javax.swing.JTextField();
        heightLabel = new javax.swing.JLabel();
        heightField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        insidePanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("jLabel1");
        jLabel1.setLabelFor(insidePanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        insidePanel.add(jLabel1, gridBagConstraints);

        widthLabel.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 17, 0, 0);
        insidePanel.add(widthLabel, gridBagConstraints);

        widthField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        insidePanel.add(widthField, gridBagConstraints);

        heightLabel.setText("jLabel3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 17, 0, 0);
        insidePanel.add(heightLabel, gridBagConstraints);

        heightField.setColumns(5);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        insidePanel.add(heightField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(insidePanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField heightField;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JPanel insidePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField widthField;
    private javax.swing.JLabel widthLabel;
    // End of variables declaration//GEN-END:variables

    private DimensionEditor editor;
}

