/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
