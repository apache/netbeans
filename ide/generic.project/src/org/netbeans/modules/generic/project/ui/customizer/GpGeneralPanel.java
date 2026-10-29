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
 * Form panel of the "General" project customizer category.
 */
public class GpGeneralPanel extends JPanel {

    public GpGeneralPanel() {
        initComponents();
    }

    @NbBundle.Messages({
        "LBL_Name=Name:",
        "TT_Name=<html>"
                + "<p>The name of the project as shown in the project view.</p>"
                + "<p>Changing it does not rename the project folder. Enter the folder name to reset.</p></html>",
        "LBL_Location=Location:",
        "LBL_MarkerFile=Marker file in project directory",
        "TT_MarkerFile=<html>"
                + "<p>When unchecked, the <b>.nbgp</b> marker file is <b>removed</b> from the project directory.</p>"
                + "<p>The project becomes a <b>local</b> (private) project.</p></html>",
    })
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new JLabel();
        nameField = new JTextField();
        locationLabel = new JLabel();
        locationField = new JTextField();
        markerBox = new JCheckBox();

        Mnemonics.setLocalizedText(nameLabel, NbBundle.getMessage(GpGeneralPanel.class, "LBL_Name")); // NOI18N

        nameField.setToolTipText(NbBundle.getMessage(GpGeneralPanel.class, "TT_Name")); // NOI18N

        Mnemonics.setLocalizedText(locationLabel, NbBundle.getMessage(GpGeneralPanel.class, "LBL_Location")); // NOI18N

        locationField.setEditable(false);

        Mnemonics.setLocalizedText(markerBox, NbBundle.getMessage(GpGeneralPanel.class, "LBL_MarkerFile")); // NOI18N
        markerBox.setToolTipText(NbBundle.getMessage(GpGeneralPanel.class, "TT_MarkerFile")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(nameLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locationLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(locationField, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .addComponent(nameField))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(markerBox)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nameField, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(markerBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public String getNameText() {
        return nameField.getText();
    }

    public void setNameText(String value) {
        nameField.setText(value);
    }

    public String getLocationText() {
        return locationField.getText();
    }

    public void setLocationText(String value) {
        locationField.setText(value);
    }

    public boolean isMarkerEnabled() {
        return markerBox.isSelected();
    }

    public void setMarkerEnabled(boolean value) {
        markerBox.setSelected(value);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JTextField locationField;
    JLabel locationLabel;
    JCheckBox markerBox;
    JTextField nameField;
    JLabel nameLabel;
    // End of variables declaration//GEN-END:variables
}
