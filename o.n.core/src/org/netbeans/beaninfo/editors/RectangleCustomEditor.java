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

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.netbeans.core.UIExceptions;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
* @author   Ian Formanek
*/
public class RectangleCustomEditor extends IntegerCustomEditor {
    static final long serialVersionUID =-9015667991684634296L;
   
    /** Initializes the Form */
    public RectangleCustomEditor(RectangleEditor editor, PropertyEnv env) {
        super( env );
        initComponents ();
        this.editor = editor;
        Rectangle rectangle = (Rectangle)editor.getValue ();
        if (rectangle == null) rectangle = new Rectangle (0, 0, 0, 0);
        xField.setText (Integer.toString(rectangle.x)); // NOI18N
        yField.setText (Integer.toString(rectangle.y)); // NOI18N
        widthField.setText (Integer.toString(rectangle.width)); // NOI18N
        heightField.setText (Integer.toString(rectangle.height)); // NOI18N

        ResourceBundle b = NbBundle.getBundle(RectangleCustomEditor.class);
        setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5)));
        jPanel2.setBorder (new javax.swing.border.CompoundBorder (
                               new javax.swing.border.TitledBorder (
                                   new javax.swing.border.EtchedBorder (),
                                   " " + b.getString ("CTL_Rectangle") + " "),
                               new javax.swing.border.EmptyBorder (new java.awt.Insets(5, 5, 5, 5))));

        Mnemonics.setLocalizedText(xLabel, b.getString ("CTL_X"));
        Mnemonics.setLocalizedText(yLabel, b.getString ("CTL_Y"));
        Mnemonics.setLocalizedText(widthLabel, b.getString ("CTL_Width"));
        Mnemonics.setLocalizedText(heightLabel, b.getString ("CTL_Height"));

        xLabel.setLabelFor(xField);
        yLabel.setLabelFor(yField);
        widthLabel.setLabelFor(widthField);
        heightLabel.setLabelFor(heightField);

        xField.getAccessibleContext().setAccessibleDescription(b.getString ("ACSD_CTL_X"));
        yField.getAccessibleContext().setAccessibleDescription(b.getString ("ACSD_CTL_Y"));
        widthField.getAccessibleContext().setAccessibleDescription(b.getString ("ACSD_CTL_Width"));
        heightField.getAccessibleContext().setAccessibleDescription(b.getString ("ACSD_CTL_Height"));
        
        getAccessibleContext().setAccessibleDescription(b.getString ("ACSD_CustomRectangleEditor"));

        setPanel(jPanel2);
        getMap().put(widthField,widthLabel);
        getMap().put(xField,xLabel);
        getMap().put(yField,yLabel);
        getMap().put(heightField,heightLabel);
//        HelpCtx.setHelpIDString (this, RectangleCustomEditor.class.getName ());

        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
    }

    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (280, 160);
    }

    private Object getPropertyValue () throws IllegalStateException {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
            if ((x < 0) || (y < 0) || (width < 0) || (height < 0)) {
                IllegalStateException ise = new IllegalStateException();
                UIExceptions.annotateUser(ise, null,
                                         NbBundle.getMessage(RectangleCustomEditor.class,
                                                             "CTL_NegativeSize"),
                                         null, null);
                throw ise;
            }
            return new Rectangle (x, y, width, height);
        } catch (NumberFormatException e) {
            IllegalStateException ise = new IllegalStateException();
            UIExceptions.annotateUser(ise, null,
                                     NbBundle.getMessage(RectangleCustomEditor.class,
                                                         "CTL_InvalidValue"),
                                     null, null);
            throw ise;
        }
    }


    private void initComponents () {
        setLayout (new java.awt.BorderLayout ());

        jPanel2 = new javax.swing.JPanel ();
        jPanel2.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        xLabel = new javax.swing.JLabel ();
        xLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (xLabel, gridBagConstraints1);

        xField = new javax.swing.JTextField ();
        xField.addKeyListener (this);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (xField, gridBagConstraints1);

        yLabel = new javax.swing.JLabel ();
        yLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (yLabel, gridBagConstraints1);

        yField = new javax.swing.JTextField ();
        yField.addKeyListener(this);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (yField, gridBagConstraints1);

        widthLabel = new javax.swing.JLabel ();
        widthLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (widthLabel, gridBagConstraints1);

        widthField = new javax.swing.JTextField ();
        widthField.addKeyListener(this);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (widthField, gridBagConstraints1);

        heightLabel = new javax.swing.JLabel ();
        heightLabel.setText (null);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add (heightLabel, gridBagConstraints1);

        heightField = new javax.swing.JTextField ();
        heightField.addKeyListener(this);

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets (4, 8, 4, 0);
        gridBagConstraints1.weightx = 1.0;
        jPanel2.add (heightField, gridBagConstraints1);


        add (jPanel2, "Center"); // NOI18N

    }

    @Override
    protected void updateValues () {
        try {
            int x = Integer.parseInt (xField.getText ());
            int y = Integer.parseInt (yField.getText ());
            int width = Integer.parseInt (widthField.getText ());
            int height = Integer.parseInt (heightField.getText ());
            editor.setValue (new Rectangle (x, y, width, height));
        } catch (NumberFormatException e) {
            // [PENDING beep]
        }
    }
    
    private JLabel findLabelFor(JTextField c) {
        return getMap().get(c);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    
    // Variables declaration - do not modify
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel xLabel;
    private javax.swing.JTextField xField;
    private javax.swing.JLabel yLabel;
    private javax.swing.JTextField yField;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField widthField;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField heightField;
    // End of variables declaration

    private RectangleEditor editor;

}

