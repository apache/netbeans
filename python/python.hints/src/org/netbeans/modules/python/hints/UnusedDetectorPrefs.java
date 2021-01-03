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

/*
 * UnusedDetectorPrefs.java
 *
 * Created on Nov 10, 2008, 9:48:35 AM
 */
package org.netbeans.modules.python.hints;

import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * Preferences where users can configure the unused detector rules
 * 
 */
public class UnusedDetectorPrefs extends javax.swing.JPanel implements ActionListener {
    private Preferences prefs;

    /** Creates new form UnusedDetectorPrefs */
    public UnusedDetectorPrefs(Preferences prefs) {
        initComponents();
        this.prefs = prefs;
        skipParams.setSelected(UnusedDetector.getSkipParameters(prefs));
        skipTupleAssignments.setSelected(UnusedDetector.getSkipTupleAssignments(prefs));
        String ignore = UnusedDetector.getIgnoreNames(prefs);
        if (ignore == null) {
            ignore = "";
        }
        ignoredNames.setText(ignore);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        skipParams = new javax.swing.JCheckBox();
        skipTupleAssignments = new javax.swing.JCheckBox();
        ignoredNames = new javax.swing.JTextField();
        ignoredLabel = new javax.swing.JLabel();

        skipParams.setText(org.openide.util.NbBundle.getMessage(UnusedDetectorPrefs.class, "UnusedDetectorPrefs.skipParams.text")); // NOI18N
        skipParams.addActionListener(this);

        skipTupleAssignments.setText(org.openide.util.NbBundle.getMessage(UnusedDetectorPrefs.class, "UnusedDetectorPrefs.skipTupleAssignments.text")); // NOI18N
        skipTupleAssignments.addActionListener(this);

        ignoredNames.setColumns(25);
        ignoredNames.addActionListener(this);

        ignoredLabel.setLabelFor(ignoredNames);
        ignoredLabel.setText(org.openide.util.NbBundle.getMessage(UnusedDetectorPrefs.class, "UnusedDetectorPrefs.ignoredLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(skipParams)
            .addComponent(skipTupleAssignments)
            .addGroup(layout.createSequentialGroup()
                .addComponent(ignoredLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignoredNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(skipParams)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(skipTupleAssignments)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ignoredLabel)
                    .addComponent(ignoredNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == skipParams) {
            UnusedDetectorPrefs.this.changed(evt);
        }
        else if (evt.getSource() == skipTupleAssignments) {
            UnusedDetectorPrefs.this.changed(evt);
        }
        else if (evt.getSource() == ignoredNames) {
            UnusedDetectorPrefs.this.changed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void changed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changed
        Object source = evt.getSource();
        if (source == ignoredNames) {
            UnusedDetector.setIgnoreNames(prefs, ignoredNames.getText().trim());
        } else if (source == skipParams) {
            UnusedDetector.setSkipParameters(prefs, skipParams.isSelected());
        } else if (source == skipTupleAssignments) {
            UnusedDetector.setSkipTupleAssignments(prefs, skipTupleAssignments.isSelected());
        } else {
            assert false : source;
        }
    }//GEN-LAST:event_changed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ignoredLabel;
    private javax.swing.JTextField ignoredNames;
    private javax.swing.JCheckBox skipParams;
    private javax.swing.JCheckBox skipTupleAssignments;
    // End of variables declaration//GEN-END:variables
}
