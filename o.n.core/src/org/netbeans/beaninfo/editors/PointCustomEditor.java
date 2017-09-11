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

import java.awt.Point;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.core.UIExceptions;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/** Custom property editor for Point and Dimension
*
* @author   Ian Formanek
*/
public class PointCustomEditor extends IntegerCustomEditor {

    static final long serialVersionUID =-4067033871196801978L;

    private boolean dimensionMode = false;

    private PropertyEnv env;
    
    /** Initializes the Form */
    public PointCustomEditor(PointEditor editor, PropertyEnv env) {
        super( env );
        initComponents ();
        this.editor = editor;
        Point point = (Point)editor.getValue ();
        if (point == null) point = new Point (0, 0);
        xField.setText (Integer.toString(point.x)); // NOI18N
        yField.setText (Integer.toString(point.y)); // NOI18N
        
        xField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_CTL_X"));
        yField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_CTL_Y"));
        
        commonInit( NbBundle.getMessage(PointCustomEditor.class, "CTL_Point"), env );
    }
    
    public PointCustomEditor(DimensionEditor editor, PropertyEnv env) {
        super( env );
        dimensionMode = true;

        initComponents();
        this.editor = editor;
        Dimension dimension = (Dimension)editor.getValue ();
        if (dimension == null) dimension = new Dimension (0, 0);
        xField.setText (Integer.toString(dimension.width));    // NOI18N
        yField.setText (Integer.toString(dimension.height));  // NOI18N
        
        Mnemonics.setLocalizedText(xLabel, NbBundle.getMessage(PointCustomEditor.class, "CTL_Width"));
        xLabel.setLabelFor(xField);
        Mnemonics.setLocalizedText(yLabel, NbBundle.getMessage(PointCustomEditor.class, "CTL_Height"));
        yLabel.setLabelFor(yField);

        xField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_CTL_Width"));
        yField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_CTL_Height"));
        
        commonInit( NbBundle.getMessage(PointCustomEditor.class, "CTL_Dimension"), env );
    }
    
    private void commonInit( String panelTitle, PropertyEnv env ) {
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PointCustomEditor.class, "ACSD_PointCustomEditor"));
        
        setBorder (new javax.swing.border.EmptyBorder(12, 12, 0, 11));
        insidePanel.setBorder (new javax.swing.border.CompoundBorder (
                                   new javax.swing.border.TitledBorder (
                                       new javax.swing.border.EtchedBorder (),
                                       " " + panelTitle + " "
                                   ),
                                   new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));


        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
        
        setPanel( insidePanel );
        xField.addKeyListener(this);
        yField.addKeyListener(this);
        getMap().put( xField , xLabel );
        getMap().put( yField , yLabel );
    }

    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (280, 160);
    }

    private Object getPropertyValue () throws IllegalStateException {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            if ( dimensionMode ) {
                if ((x < 0) || (y < 0)) {
                    IllegalStateException ise = new IllegalStateException();
                    UIExceptions.annotateUser(ise, null,
                                             NbBundle.getMessage(PointCustomEditor.class,
                                                                 "CTL_NegativeSize"),
                                             null, null);
                    throw ise;
                }
                return new Dimension (x, y);
            } else {
                return new Point (x, y);
            }
        } catch (NumberFormatException e) {
            IllegalStateException ise = new IllegalStateException();
            UIExceptions.annotateUser(ise, null,
                                     NbBundle.getMessage(PointCustomEditor.class,
                                                         "CTL_InvalidValue"),
                                     null, null);
            throw ise;
        }
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (
            PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
            &&
            PropertyEnv.STATE_VALID.equals(evt.getNewValue())
        ) {
            editor.setValue(getPropertyValue());
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        insidePanel = new javax.swing.JPanel();
        xLabel = new javax.swing.JLabel();
        xField = new javax.swing.JTextField();
        yLabel = new javax.swing.JLabel();
        yField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        insidePanel.setLayout(new java.awt.GridBagLayout());

        xLabel.setLabelFor(xField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(xLabel, bundle.getString("CTL_X")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        insidePanel.add(xLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        insidePanel.add(xField, gridBagConstraints);

        yLabel.setLabelFor(yField);
        org.openide.awt.Mnemonics.setLocalizedText(yLabel, bundle.getString("CTL_Y")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        insidePanel.add(yLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        insidePanel.add(yField, gridBagConstraints);

        add(insidePanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    protected void updateValues() {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            if ( dimensionMode )
                editor.setValue (new Dimension (x, y));
            else
                editor.setValue (new Point (x, y));
        } catch (NumberFormatException e) {
            // [PENDING beep]
        }
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel insidePanel;
    private javax.swing.JTextField xField;
    private javax.swing.JLabel xLabel;
    private javax.swing.JTextField yField;
    private javax.swing.JLabel yLabel;
    // End of variables declaration//GEN-END:variables

    private ArrayOfIntSupport editor;

}

