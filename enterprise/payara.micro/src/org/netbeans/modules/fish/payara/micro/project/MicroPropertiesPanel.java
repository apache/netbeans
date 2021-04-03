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
package org.netbeans.modules.fish.payara.micro.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import static java.util.stream.Collectors.toList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import static org.netbeans.api.project.ProjectUtils.getPreferences;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.VERSION;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersion;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class MicroPropertiesPanel extends JPanel {

    private final Preferences pref;
    
    private final ComboBoxUpdater<PayaraPlatformVersionAPI> microVersionComboBoxUpdater;
    
    private PayaraPlatformVersionAPI selectedPayaraVersion;
    
    public MicroPropertiesPanel(ModelHandle2 handle, Project project) {
        pref = getPreferences(project, MicroApplication.class, true);
        initComponents();
        String microVersionText = pref.get(VERSION, "");
        PayaraPlatformVersionAPI microVersion = PayaraPlatformVersion.toValue(microVersionText);
        microVersionComboBoxUpdater = new ComboBoxUpdater<PayaraPlatformVersionAPI>(microVersionCombobox, microVersionLabel)  {
            @Override
            public PayaraPlatformVersionAPI getValue() {
                return microVersion != null ? microVersion : PayaraPlatformVersion.EMPTY;
            }

            @Override
            public PayaraPlatformVersionAPI getDefaultValue() {
                return null;
            }

            @Override
            public void setValue(PayaraPlatformVersionAPI microVersion) {
                selectedPayaraVersion = microVersion;
            }
        };
    }
    
    private PayaraPlatformVersionAPI[] getPayaraVersion() {
        List<PayaraPlatformVersionAPI> microVersions = new ArrayList<>();
        microVersions.add(PayaraPlatformVersion.EMPTY);
        microVersions.addAll(
                PayaraPlatformVersion.getVersions()
                        .stream()
                        .sorted(Collections.reverseOrder())
                        .collect(toList())
        );
        return microVersions.toArray(new PayaraPlatformVersionAPI[]{});
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        microVersionLabel = new javax.swing.JLabel();
        microVersionCombobox = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(microVersionLabel, org.openide.util.NbBundle.getMessage(MicroPropertiesPanel.class, "MicroPropertiesPanel.microVersionLabel.text")); // NOI18N

        microVersionCombobox.setModel(new DefaultComboBoxModel(getPayaraVersion()));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(microVersionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(microVersionCombobox, 0, 272, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(microVersionCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(microVersionLabel))
                .addContainerGap(117, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox microVersionCombobox;
    private javax.swing.JLabel microVersionLabel;
    // End of variables declaration//GEN-END:variables

    public void applyChanges() {
       pref.put(VERSION, selectedPayaraVersion.toString());
    }

}
