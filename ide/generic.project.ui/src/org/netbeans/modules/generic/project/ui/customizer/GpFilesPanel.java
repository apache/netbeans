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
package org.netbeans.modules.generic.project.ui.customizer;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Form panel of the "Files" project customizer category.
 */
public class GpFilesPanel extends JPanel {

    public GpFilesPanel() {
        initComponents();
    }

    @NbBundle.Messages({
        "LBL_UseGlobalFilter=Apply the IDE's ignored files pattern",
        "TT_UseGlobalFilter=<html>"
                + "<p>When checked, files matching the IDE's global <b>ignored files pattern</b> "
                + "are hidden in the project view.</p><br>"
                + "<p>See Tools -&gt; Options -&gt; Miscellaneous -&gt; Files</p></html>",
        "LBL_Pattern=Additional ignored files pattern:",
        "TT_Pattern=<html>"
                + "<p>A regular expression matched against the file name.</p>"
                + "<p>Files matching it are hidden in the project view,</p>"
    })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        useGlobalFilterCheckBox = new JCheckBox();
        patternLabel = new JLabel();
        patternField = new JTextField();

        Mnemonics.setLocalizedText(useGlobalFilterCheckBox, NbBundle.getMessage(GpFilesPanel.class, "LBL_UseGlobalFilter")); // NOI18N
        useGlobalFilterCheckBox.setToolTipText(NbBundle.getMessage(GpFilesPanel.class, "TT_UseGlobalFilter")); // NOI18N

        Mnemonics.setLocalizedText(patternLabel, NbBundle.getMessage(GpFilesPanel.class, "LBL_Pattern")); // NOI18N

        patternField.setToolTipText(NbBundle.getMessage(GpFilesPanel.class, "TT_Pattern")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(patternLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(patternField, GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(useGlobalFilterCheckBox)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(patternLabel)
                    .addComponent(patternField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(useGlobalFilterCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public boolean isUseGlobalFilter() {
        return useGlobalFilterCheckBox.isSelected();
    }

    public void setUseGlobalFilter(boolean value) {
        useGlobalFilterCheckBox.setSelected(value);
    }

    public String getPatternText() {
        return patternField.getText();
    }

    public void setPatternText(String value) {
        patternField.setText(value);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JTextField patternField;
    JLabel patternLabel;
    JCheckBox useGlobalFilterCheckBox;
    // End of variables declaration//GEN-END:variables
}
