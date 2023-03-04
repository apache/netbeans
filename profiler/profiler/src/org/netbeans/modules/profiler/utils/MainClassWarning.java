/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.profiler.utils;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Shows a warning that no main class is set and allows choose a main class.
 *
 * @author Tomas Hurka
 * @author Jiri Rechtacek
 */
@NbBundle.Messages({
    "AD_MainClassWarning=N/A"
})
public class MainClassWarning extends JPanel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private JPanel jPanel1;
    private String message;
    private Lookup.Provider project;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates new form LibrariesChooser
     */
    public MainClassWarning(String message, Lookup.Provider project) {
        this.project = project;
        this.message = message;
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the selected main class.
     *
     * @return name of class or null if no class with the main method is selected
     */
    public String getSelectedMainClass() {
        return ((MainClassChooser) jPanel1).getSelectedMainClass();
    }

    public void addChangeListener(ChangeListener l) {
        ((MainClassChooser) jPanel1).addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        ((MainClassChooser) jPanel1).removeChangeListener(l);
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new MainClassChooser(project, Bundle.CTL_SelectAvaialableMainClasses());

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext()
            .setAccessibleDescription(Bundle.AD_MainClassWarning());
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, this.message);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportView(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
    }
}
