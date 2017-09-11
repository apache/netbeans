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

package org.netbeans.modules.xml.multiview.ui;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.openide.util.NbBundle;

/** The simple dialog panel containing labels, text fields and browse buttons.
 * <br>
 * Individual components should be described using SimpleDialogPanel.DialogDescriptor class.
 *
 * @author  mk115033
 * Created on January 03, 2005
 */
public class SimpleDialogPanel extends JPanel {
    private JTextComponent[] jTextComponents;
    private JLabel[] jLabels;
    private JButton[] jButtons;
    private GridBagConstraints gridBagConstraints;

    /** Constructor that creates the simple ADD/EDIT dialog with n labels and n empty text fields,
    * where the shape of the dialog is described using the DialogDescriptor object.
    * @param desc describes components inside the panel
    */      
    public SimpleDialogPanel(DialogDescriptor desc) {
        super();
        initComponents(desc.getLabels(), desc.isTextField(), desc.getSize(), desc.getButtons(), desc.getMnemonics(), desc.getA11yDesc(), desc.includesMnemonics);
        String[] initValues = desc.getInitValues();
        if (initValues!=null)
            for (int i=0;i<initValues.length;i++) {
                jTextComponents[i].setText(initValues[i]);
            }
    }

    private void initComponents(String[] labels, boolean[] isTextField, int size, boolean[] customizers, char[] mnem, String[] a11yDesc, boolean includesMnemonics) {
        setLayout(new GridBagLayout());
        jLabels = new JLabel [labels.length];
        jTextComponents = new JTextComponent [labels.length];
        for (int i=0;i<labels.length;i++) {
            if (!includesMnemonics) {
                jLabels[i] = new JLabel(labels[i]);
            }
            else {
                jLabels[i] = new JLabel();
                org.openide.awt.Mnemonics.setLocalizedText(jLabels[i], labels[i]);
            }
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
            if (isTextField[i])
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            else 
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            add(jLabels[i], gridBagConstraints);
        }
        for (int i=0;i<jTextComponents.length;i++) {
            if (isTextField[i]) { // text field
                jTextComponents[i] = new JTextField();
                ((JTextField)jTextComponents[i]).setColumns(size);
            } else { // text area
                jTextComponents[i] = new JTextArea();
                ((JTextArea)jTextComponents[i]).setRows(3);
                ((JTextArea)jTextComponents[i]).setColumns(size);
            }
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = i;
            gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
            gridBagConstraints.weightx = 1.0;
            jLabels[i].setLabelFor(jTextComponents[i]);
            if (isTextField[i]) {// text field
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                add(jTextComponents[i], gridBagConstraints);
            } else {
                gridBagConstraints.weighty = 1.0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                javax.swing.JScrollPane sp = new javax.swing.JScrollPane(jTextComponents[i]);
                add(sp, gridBagConstraints);
            }
        }
        if (customizers!=null) {
            java.util.List buttonList = new java.util.ArrayList();
            int j=0;
            for (int i=0;i<customizers.length;i++) {
                if (customizers[i]) {
                    JButton button = new JButton();
                    button.setText(NbBundle.getMessage(SimpleDialogPanel.class,"LBL_browse"+String.valueOf(j)));
                    button.setMnemonic(NbBundle.getMessage(SimpleDialogPanel.class,"LBL_browse"+String.valueOf(j++)+"_mnem").charAt(0));
                    button.setMargin(new java.awt.Insets(0, 14, 0, 14));
                                       
                    button.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDialogPanel.class,"ACSD_browse"));
                                       
                    buttonList.add(button);
                    gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.gridx = 2;
                    gridBagConstraints.gridy = i;
                    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 11);
                    add(button, gridBagConstraints);
                }
            }
            jButtons = new JButton[buttonList.size()];
            buttonList.toArray(jButtons);
        }
        if (mnem!=null && !"Aqua".equals( UIManager.getLookAndFeel().getID())) { //NOI18N
            for (int i=0;i<labels.length;i++) {
                jLabels[i].setLabelFor(jTextComponents[i]);
                jLabels[i].setDisplayedMnemonic(mnem[i]);
            }
        }
        if (a11yDesc!=null) {
            for (int i=0;i<jTextComponents.length;i++) {
                jTextComponents[i].getAccessibleContext().setAccessibleDescription(a11yDesc[i]);
            }
        }
    }
    
    /** Returns the values from the text fields.
    * @return text fields values
    */
    public String[] getValues() {
        if (jTextComponents==null) return null;
        String[] values = new String[jTextComponents.length];
        for (int i=0;i<values.length;i++) {
            values[i] = jTextComponents[i].getText();
        }
        return values;
    }
    
    /** Returns the JButton components from the dialog described in DialogDescriptor.
    * @return JButton components that are used for invoking the text field customizers 
    */    
    public JButton[] getCustomizerButtons() {
        return jButtons;
    }
    
    /** Returns the dialog text fields.
    * @return array of text fields
    */    
    public JTextComponent[] getTextComponents() {
        return jTextComponents;
    }
    
    /** Returns the dialog labels. For testing purposes mainly.
     * @return array of labels
     */
    JLabel[] getLabels() {
        return jLabels;
    }

    /** This is the descriptor for the dialog components.
    * Parameters are :<ul>
    * <li>labels = text array for text fields
    * <li>initValues = initialization values for text fields
    * <li>adding = indicates the type of the dialog (ADD/EDIT). Defaultly set to true (ADD dialog)
    * <li>customizers = describes the layout of the customizers buttons.<br>
    * For example setCustomizers(new boolean{false,true,false}) sets the customizer only for the second text field 
    * <li>size = default number of columns for text fields. Defaultly set to 25
    * </ul>
    */    
    public static class DialogDescriptor {
        String[] labels;
        String[] initValues;
        boolean adding;
        boolean[] buttons;
        boolean textField[];
        char[] mnem;
        String[] a11yDesc;
        int size;
        boolean includesMnemonics;
        
        /** the constructor for DialogDescriptor object
        * @param labels labels names
        */
        public DialogDescriptor(String[] labels) {
            this(labels, false);
        }
        
        public DialogDescriptor(String[] labels, boolean includesMnemonics) {
            this.labels=labels;
            size=25;
            adding=true;
            textField = new boolean[labels.length];
            for (int i=0;i<labels.length;i++) {
                textField[i]=true; // setting textFields to text fields
            }
            this.includesMnemonics = includesMnemonics;
        }
        
        public String[] getLabels() {
            return labels;
        }
        
        /** Specifies which text fields should contain the Browse buttons
         * Limited up to 3 Browse bottons (number of "true" items in buttons array)
         * Example : setButtons (new boolean[] {true, false, false, true, false, false});
         */
        public void setButtons(boolean[] buttons) {
            this.buttons=buttons;
        }
        
        public boolean[] getButtons() {
            return buttons;
        }
        
        /** Sets the text fields
         */         
        public void setTextField(boolean[] textField) {
            this.textField=textField;
        }
        
        public boolean[] isTextField() {
            return textField;
        }
        
        /** Sets the init values for text fields
         */        
        public void setInitValues(String[] initValues) {
            this.initValues=initValues;
            adding=false;
        }
        
        public String[] getInitValues() {
            return initValues;
        }
        
        /** Specifies whether the dialog adds or adits values
         */
        public void setAdding(boolean adding) {
            this.adding=adding;
        }
        
        public boolean isAdding() {
            return adding;
        }
        
        /** Sets the longest text field size
         */
        public void setSize(int size) {
            this.size=size;
        }
        
        public int getSize() {
            return size;
        }
        
        /** Sets mnemonics for labels
         * @deprecated Please use DialogDescriptor(String[] labels, boolean includesMnemonics)
         * instead and provide mnemonics directly in labels (via &amp; escape chars)
         */
        public void setMnemonics(char[] mnem) {
            this.mnem=mnem;
        }

        /**
         * @deprecated Please use DialogDescriptor(String[] labels, boolean includesMnemonics)
         * instead and provide mnemonics directly in labels (via &amp; escape chars)
         */
        public char[] getMnemonics() {
            return mnem;
        }
        
        /** Sets A11Y desc for text fields
         */
        public void setA11yDesc(String[] a11yDesc) {
            this.a11yDesc=a11yDesc;
        }
        
        public String[] getA11yDesc() {
            return a11yDesc;
        }
    }
}
