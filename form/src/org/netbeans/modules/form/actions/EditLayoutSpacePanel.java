/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.form.actions;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner.EditableGap;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Pavek
 */
class EditLayoutSpacePanel extends javax.swing.JPanel {
    private EditableGap[] editableGaps;

    EditLayoutSpacePanel(EditableGap[] editableGaps, String componentName) {
        this.editableGaps = editableGaps;
        initComponents();
        if (editableGaps.length == 1) {
            add(singleGapPanel);
            initSingleGapValues();
        } else {
            add(componentGapsPanel);
            initComponentGapsValues(componentName);
        }
    }

    private void initSingleGapValues() {
        EditableGap eg = editableGaps[0];
        singleTitleLabel.setText(NbBundle.getMessage(EditLayoutSpacePanel.class,
                eg.getDimension()==0 ? "CTL_singleTitleLabel_H" : "CTL_singleTitleLabel_V")); // NOI18N
        initGapValues(eg, singleSizeCombo, singleResCheckBox);
        if (eg.definedSize != eg.getActualSize()) {
            actualSizeLabel.setText(NbBundle.getMessage(EditLayoutSpacePanel.class, "FMT_actualSizelabel", // NOI18N
                    Integer.toString(eg.getActualSize())));
        } else {
            actualSizeLabel.setVisible(false);
        }
    }

    private void initComponentGapsValues(String compName) {
        componentsTitle.setText(NbBundle.getMessage(EditLayoutSpacePanel.class, "FMT_componentsTitle", compName)); // NOI18N
        initGapValues(editableGaps[0], leftCombo, resLeftCheckBox);
        initGapValues(editableGaps[1], rightCombo, resRightCheckBox);
        initGapValues(editableGaps[2], topCombo, resTopCheckBox);
        initGapValues(editableGaps[3], bottomCombo, resBottomCheckBox);
    }

    private static void initGapValues(EditableGap eg, JComboBox sizeCombo, JCheckBox resCheckBox) {
        if (eg != null) {
            String selected = null;
            String[] defaultNames = eg.getPaddingDisplayNames();
            if (eg.canHaveDefaultValue() && defaultNames != null) {
                sizeCombo.setModel(new DefaultComboBoxModel(defaultNames));
                if (eg.definedSize == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                    LayoutConstants.PaddingType[] defaultTypes = eg.getPossiblePaddingTypes();
                    if (eg.paddingType == null || defaultTypes == null || defaultTypes.length == 0) {
                        selected = defaultNames[0];
                    } else {
                        for (int i=0; i < defaultTypes.length; i++) {
                            if (eg.paddingType == defaultTypes[i]) {
                                selected = defaultNames[i];
                                break;
                            }
                        }
                    }
                }
            }
            if (selected == null) {
                selected = Integer.toString(eg.definedSize);
            }
            sizeCombo.setSelectedItem(selected);

            resCheckBox.setSelected(eg.resizing);
        } else {
            sizeCombo.setSelectedItem(NbBundle.getMessage(EditLayoutSpacePanel.class, "VALUE_NoEmptySpace")); // NOI18N
            sizeCombo.setEnabled(false);
            resCheckBox.setEnabled(false);
        }
    }

    boolean applyValues() {
        if (editableGaps.length == 1) {
            return applyGapValues(editableGaps[0], singleSizeCombo, singleResCheckBox);
        } else {
            if (editableGaps[0] != null && !applyGapValues(editableGaps[0], leftCombo, resLeftCheckBox)) {
                return false;
            }
            if (editableGaps[1] != null && !applyGapValues(editableGaps[1], rightCombo, resRightCheckBox)) {
                return false;
            }
            if (editableGaps[2] != null && !applyGapValues(editableGaps[2], topCombo, resTopCheckBox)) {
                return false;
            }
            if (editableGaps[3] != null && !applyGapValues(editableGaps[3], bottomCombo, resBottomCheckBox)) {
                return false;
            }
            return true;
        }
    }

    private static boolean applyGapValues(EditableGap eg, JComboBox sizeCombo, JCheckBox resCheckBox) {
        int newSize = Integer.MIN_VALUE;
        LayoutConstants.PaddingType newPadding = null;
        Object selSize = sizeCombo.getSelectedItem();
        if (eg.canHaveDefaultValue()) {
            for (int i=0, n=eg.getPaddingDisplayNames().length; i < n; i++) {
                String pdn = eg.getPaddingDisplayNames()[i];
                if (pdn.equals(selSize)) {
                    newSize = LayoutConstants.NOT_EXPLICITLY_DEFINED;
                    if (eg.getPossiblePaddingTypes() != null) {
                        newPadding = eg.getPossiblePaddingTypes()[i];
                    }
                    break;
                }
            }
        }
        if (newSize == Integer.MIN_VALUE) {
            try {
                newSize = Integer.parseInt((String)selSize);
                if (newSize < 0) { // Negative
                    notify("MSG_NegativeSpaceSize"); // NOI18N
                    return false;
                }
                if (newSize > Short.MAX_VALUE) { // Too large
                    notify("MSG_TooLargeSpaceSize"); // NOI18N
                    return false;
                }
            } catch (NumberFormatException nfex) { // Not a nubmer
                notify("MSG_CorruptedSpaceSize"); // NOI18N
                return false;
            }            
        }
        eg.definedSize = newSize;
        eg.paddingType = newPadding;
        eg.resizing = resCheckBox.isSelected();
        return true;
    }

    private static void notify(String messageKey) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(
            NbBundle.getMessage(EditLayoutSpacePanel.class, messageKey));
        DialogDisplayer.getDefault().notify(descriptor);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        componentGapsPanel = new javax.swing.JPanel();
        leftLabel = new javax.swing.JLabel();
        rightLabel = new javax.swing.JLabel();
        topLabel = new javax.swing.JLabel();
        bottomLabel = new javax.swing.JLabel();
        leftCombo = new javax.swing.JComboBox();
        rightCombo = new javax.swing.JComboBox();
        topCombo = new javax.swing.JComboBox();
        bottomCombo = new javax.swing.JComboBox();
        resLeftCheckBox = new javax.swing.JCheckBox();
        resRightCheckBox = new javax.swing.JCheckBox();
        resTopCheckBox = new javax.swing.JCheckBox();
        resBottomCheckBox = new javax.swing.JCheckBox();
        componentsTitle = new javax.swing.JLabel();
        singleGapPanel = new javax.swing.JPanel();
        singleTitleLabel = new javax.swing.JLabel();
        defSizeLabel = new javax.swing.JLabel();
        singleSizeCombo = new javax.swing.JComboBox();
        singleResCheckBox = new javax.swing.JCheckBox();
        actualSizeLabel = new javax.swing.JLabel();

        leftLabel.setLabelFor(leftCombo);
        org.openide.awt.Mnemonics.setLocalizedText(leftLabel, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "EditLayoutSpacePanel.leftLabel.text")); // NOI18N

        rightLabel.setLabelFor(rightCombo);
        org.openide.awt.Mnemonics.setLocalizedText(rightLabel, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "EditLayoutSpacePanel.rightLabel.text")); // NOI18N

        topLabel.setLabelFor(topCombo);
        org.openide.awt.Mnemonics.setLocalizedText(topLabel, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "EditLayoutSpacePanel.topLabel.text")); // NOI18N

        bottomLabel.setLabelFor(bottomCombo);
        org.openide.awt.Mnemonics.setLocalizedText(bottomLabel, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "EditLayoutSpacePanel.bottomLabel.text")); // NOI18N

        leftCombo.setEditable(true);

        rightCombo.setEditable(true);

        topCombo.setEditable(true);

        bottomCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(resLeftCheckBox, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "NAME_SpaceResizable2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resRightCheckBox, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "NAME_SpaceResizable2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resTopCheckBox, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "NAME_SpaceResizable2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resBottomCheckBox, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "NAME_SpaceResizable2")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(componentsTitle, "(Gaps Around Selected Component)"); // NOI18N

        javax.swing.GroupLayout componentGapsPanelLayout = new javax.swing.GroupLayout(componentGapsPanel);
        componentGapsPanel.setLayout(componentGapsPanelLayout);
        componentGapsPanelLayout.setHorizontalGroup(
            componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(componentGapsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(componentGapsPanelLayout.createSequentialGroup()
                        .addGroup(componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(rightLabel)
                            .addComponent(leftLabel)
                            .addComponent(topLabel)
                            .addComponent(bottomLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(leftCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rightCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(topCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bottomCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resLeftCheckBox)
                            .addComponent(resRightCheckBox)
                            .addComponent(resTopCheckBox)
                            .addComponent(resBottomCheckBox)))
                    .addGroup(componentGapsPanelLayout.createSequentialGroup()
                        .addComponent(componentsTitle)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        componentGapsPanelLayout.setVerticalGroup(
            componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(componentGapsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(componentsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leftLabel)
                    .addComponent(leftCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resLeftCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rightLabel)
                    .addComponent(rightCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resRightCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(topLabel)
                    .addComponent(topCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resTopCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(componentGapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bottomLabel)
                    .addComponent(bottomCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resBottomCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(singleTitleLabel, "(Selected Layout Gap: H/ V)"); // NOI18N

        defSizeLabel.setLabelFor(singleSizeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(defSizeLabel, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "EditLayoutSpacePanel.defSizeLabel.text")); // NOI18N

        singleSizeCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(singleResCheckBox, org.openide.util.NbBundle.getMessage(EditLayoutSpacePanel.class, "NAME_SpaceResizable")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(actualSizeLabel, "(Actual Size)"); // NOI18N

        javax.swing.GroupLayout singleGapPanelLayout = new javax.swing.GroupLayout(singleGapPanel);
        singleGapPanel.setLayout(singleGapPanelLayout);
        singleGapPanelLayout.setHorizontalGroup(
            singleGapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(singleGapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(singleGapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(singleGapPanelLayout.createSequentialGroup()
                        .addComponent(defSizeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(singleSizeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(singleResCheckBox))
                    .addGroup(singleGapPanelLayout.createSequentialGroup()
                        .addComponent(singleTitleLabel)
                        .addGap(18, 18, 18)
                        .addComponent(actualSizeLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        singleGapPanelLayout.setVerticalGroup(
            singleGapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(singleGapPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(singleGapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(singleTitleLabel)
                    .addComponent(actualSizeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(singleGapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defSizeLabel)
                    .addComponent(singleSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(singleResCheckBox))
                .addGap(0, 0, 0))
        );

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel actualSizeLabel;
    private javax.swing.JComboBox bottomCombo;
    private javax.swing.JLabel bottomLabel;
    private javax.swing.JPanel componentGapsPanel;
    private javax.swing.JLabel componentsTitle;
    private javax.swing.JLabel defSizeLabel;
    private javax.swing.JComboBox leftCombo;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JCheckBox resBottomCheckBox;
    private javax.swing.JCheckBox resLeftCheckBox;
    private javax.swing.JCheckBox resRightCheckBox;
    private javax.swing.JCheckBox resTopCheckBox;
    private javax.swing.JComboBox rightCombo;
    private javax.swing.JLabel rightLabel;
    private javax.swing.JPanel singleGapPanel;
    private javax.swing.JCheckBox singleResCheckBox;
    private javax.swing.JComboBox singleSizeCombo;
    private javax.swing.JLabel singleTitleLabel;
    private javax.swing.JComboBox topCombo;
    private javax.swing.JLabel topLabel;
    // End of variables declaration//GEN-END:variables
}
