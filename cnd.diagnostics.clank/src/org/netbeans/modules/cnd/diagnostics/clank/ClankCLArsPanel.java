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
package org.netbeans.modules.cnd.diagnostics.clank;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.clang.tools.services.checkers.api.ClankCLOptionsProvider;
//import org.clang.tools.services.checkers.api.ClankCLOptionsProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;

/**
 *
 */
final class ClankCLArsPanel extends JPanel{
    private final Preferences preferences;

    public ClankCLArsPanel(Preferences preferences)  {
        if (preferences != null) {
            if (preferences.absolutePath().endsWith("/"+ClankDiagnoticsErrorProvider.NAME)) { //NOI18N
                this.preferences = preferences;
            } else {
                this.preferences = preferences.node(ClankDiagnoticsErrorProvider.NAME);
            }
        } else {
            this.preferences = AuditPreferences.AUDIT_PREFERENCES_ROOT.node(ClankDiagnoticsErrorProvider.NAME);
        }
        initComponents();
        
    }
    
    
    private void initComponents() {
        //get values from the provider
        final String[] args = ClankCLOptionsDeafaultImpl.getArgs();
        
        setLayout(new GridBagLayout());
        
        for (String arg : args) {            
            final JCheckBox checkBox = new JCheckBox(arg);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //put to preferences
                    preferences.putBoolean(arg, checkBox.isSelected());
                }
            });
            checkBox.setSelected(preferences.getBoolean(arg, true));
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);

            add(checkBox, gridBagConstraints);
        }
    }

    
    
}
