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
package org.netbeans.modules.java.disco;

import eu.hansolo.jdktools.Architecture;
import eu.hansolo.jdktools.Latest;
import eu.hansolo.jdktools.PackageType;
import eu.hansolo.jdktools.TermOfSupport;
import io.foojay.api.discoclient.pkg.Distribution;
import io.foojay.api.discoclient.pkg.Pkg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.TableModel;
import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AdvancedPanel extends javax.swing.JPanel {

    protected BundleTableModel tableModel;
    private final DefaultComboBoxModel<Distribution> distrosModel;

    public AdvancedPanel() {
        distrosModel = new DefaultComboBoxModel<>();
        initComponents();

        distributionComboBox.setRenderer(new DistributionListCellRenderer());
        versionComboBox.setRenderer(new VersionListCellRenderer());
        packageTypeComboBox.setRenderer(new PackageTypeListCellRenderer());
        architectureComboBox.setRenderer(new ArchitectureListCellRenderer());
        architectureComboBox.setSelectedItem(OS.getArchitecture());
    }

    @UIEffect
    public @Nullable
    Pkg getSelectedPackage() {
        int index = table.getSelectedRow();
        if (index < 0) {
            return null;
        }
        int modelIndex = table.convertRowIndexToModel(index);
        Pkg bundle = tableModel.getBundles().get(modelIndex);
        return bundle;
    }

    private TableModel createTableModel() {
        if (tableModel == null) {
            tableModel = new BundleTableModel(new ArrayList<>());
        }

        return tableModel;
    }

    @UIEffect
    protected abstract void updateData(Distribution distribution, Integer featureVersion, Architecture architecture, Latest latest, PackageType bundleType, boolean ea);

    protected void updateDistributions(List<Distribution> distros) {
        distrosModel.removeAllElements();
        distros.stream()
                .sorted((o1, o2) -> o1.getUiString().compareTo(o2.getUiString()))
                .forEachOrdered(distrosModel::addElement);
        Client.getInstance().getDistribution(DiscoPlatformInstall.defaultDistribution())
                .filter(distros::contains)
                .ifPresent(distrosModel::setSelectedItem);
    }

    protected void setVersions(List<Integer> versions, Map<Integer, TermOfSupport> lts, int currentJdk) {
        List<Integer> reversedVersions = new ArrayList<>(versions);
        reversedVersions.sort(Collections.reverseOrder());
        VersionListCellRenderer renderer = (VersionListCellRenderer) versionComboBox.getRenderer();
        renderer.setLTS(lts);
        renderer.setCurrentJDK(currentJdk);
        DefaultComboBoxModel versionModel = (DefaultComboBoxModel<Integer>) versionComboBox.getModel();
        reversedVersions.forEach(v -> versionModel.addElement(v));
        versionModel.setSelectedItem(LTSes.latest(lts));
    }

    Distribution getSelectedDistribution() {
        return (Distribution) distributionComboBox.getSelectedItem();
    }

    Integer getSelectedVersion() {
        return (Integer) versionComboBox.getSelectedItem();
    }

    void switchFocus(Distribution distribution, Integer version) {
        if (distribution != null) {
            distributionComboBox.setSelectedItem(distribution);
        }
        if (version != null) {
            versionComboBox.setSelectedItem(version);
        }
    }

    private ComboBoxModel<Integer> createVersionComboboxModel() {
        return new DefaultComboBoxModel<>();
    }

    private ComboBoxModel<Distribution> createDistributionComboboxModel() {
        return distrosModel;
    }

    private ComboBoxModel<PackageType> createPackageTypeComboboxModel() {
        PackageType[] bundleTypes = Arrays.stream(PackageType.values())
                .filter(bundleType -> PackageType.NONE != bundleType)
                .filter(bundleType -> PackageType.NOT_FOUND != bundleType)
                .toArray(PackageType[]::new);
        return new DefaultComboBoxModel<>(bundleTypes);
    }

    private ComboBoxModel<Architecture> createArchitectureComboboxModel() {
        return new DefaultComboBoxModel<>(Architecture.values());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel filterPanel = new javax.swing.JPanel();
        javax.swing.JPanel distributionsPanel = new javax.swing.JPanel();
        javax.swing.JLabel distLabel = new javax.swing.JLabel();
        distributionComboBox = new javax.swing.JComboBox<>();
        javax.swing.JPanel versionsPanel = new javax.swing.JPanel();
        javax.swing.JLabel versionLabel = new javax.swing.JLabel();
        versionComboBox = new javax.swing.JComboBox<>();
        javax.swing.JPanel architecturePanel = new javax.swing.JPanel();
        javax.swing.JLabel architectureLabel = new javax.swing.JLabel();
        architectureComboBox = new javax.swing.JComboBox<>();
        javax.swing.JPanel typePanel = new javax.swing.JPanel();
        javax.swing.JLabel typeLabel = new javax.swing.JLabel();
        packageTypeComboBox = new javax.swing.JComboBox<>();
        javax.swing.JPanel latestPanel = new javax.swing.JPanel();
        javax.swing.JLabel latestLabel = new javax.swing.JLabel();
        latestCheckBox = new javax.swing.JCheckBox();
        javax.swing.JPanel eaPanel = new javax.swing.JPanel();
        javax.swing.JLabel eaLabel = new javax.swing.JLabel();
        eaCheckBox = new javax.swing.JCheckBox();
        tableScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        filterPanel.setLayout(new javax.swing.BoxLayout(filterPanel, javax.swing.BoxLayout.LINE_AXIS));

        distLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(distLabel, org.openide.util.NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.distLabel.text")); // NOI18N

        distributionComboBox.setModel(createDistributionComboboxModel());
        distributionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distributionComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout distributionsPanelLayout = new javax.swing.GroupLayout(distributionsPanel);
        distributionsPanel.setLayout(distributionsPanelLayout);
        distributionsPanelLayout.setHorizontalGroup(
            distributionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(distLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(distributionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(distributionComboBox, 0, 85, Short.MAX_VALUE)
                .addContainerGap())
        );
        distributionsPanelLayout.setVerticalGroup(
            distributionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(distributionsPanelLayout.createSequentialGroup()
                .addComponent(distLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(distributionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        filterPanel.add(distributionsPanel);

        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.versionLabel.text")); // NOI18N

        versionComboBox.setModel(createVersionComboboxModel());
        versionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout versionsPanelLayout = new javax.swing.GroupLayout(versionsPanel);
        versionsPanel.setLayout(versionsPanelLayout);
        versionsPanelLayout.setHorizontalGroup(
            versionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(versionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(versionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(versionComboBox, 0, 80, Short.MAX_VALUE)
                .addContainerGap())
        );
        versionsPanelLayout.setVerticalGroup(
            versionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(versionsPanelLayout.createSequentialGroup()
                .addComponent(versionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(versionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        filterPanel.add(versionsPanel);

        architectureLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(architectureLabel, org.openide.util.NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.architectureLabel.text")); // NOI18N

        architectureComboBox.setModel(createArchitectureComboboxModel());
        architectureComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                architectureComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout architecturePanelLayout = new javax.swing.GroupLayout(architecturePanel);
        architecturePanel.setLayout(architecturePanelLayout);
        architecturePanelLayout.setHorizontalGroup(
            architecturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(architectureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(architecturePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(architectureComboBox, 0, 85, Short.MAX_VALUE)
                .addContainerGap())
        );
        architecturePanelLayout.setVerticalGroup(
            architecturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(architecturePanelLayout.createSequentialGroup()
                .addComponent(architectureLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(architectureComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        filterPanel.add(architecturePanel);

        typeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.typeLabel.text")); // NOI18N

        packageTypeComboBox.setModel(createPackageTypeComboboxModel());
        packageTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packageTypeComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout typePanelLayout = new javax.swing.GroupLayout(typePanel);
        typePanel.setLayout(typePanelLayout);
        typePanelLayout.setHorizontalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(typeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(packageTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        typePanelLayout.setVerticalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addComponent(typeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(packageTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        filterPanel.add(typePanel);

        latestLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(latestLabel, org.openide.util.NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.latestLabel.text")); // NOI18N

        latestCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(latestCheckBox, org.openide.util.NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.latestCheckBox.text")); // NOI18N
        latestCheckBox.setPreferredSize(new java.awt.Dimension(19, 23));
        latestCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latestCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout latestPanelLayout = new javax.swing.GroupLayout(latestPanel);
        latestPanel.setLayout(latestPanelLayout);
        latestPanelLayout.setHorizontalGroup(
            latestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(latestLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
            .addGroup(latestPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(latestCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        latestPanelLayout.setVerticalGroup(
            latestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(latestPanelLayout.createSequentialGroup()
                .addComponent(latestLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(latestCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        filterPanel.add(latestPanel);

        eaLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(eaLabel, org.openide.util.NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.eaLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(eaCheckBox, org.openide.util.NbBundle.getMessage(AdvancedPanel.class, "AdvancedPanel.eaCheckBox.text")); // NOI18N
        eaCheckBox.setPreferredSize(new java.awt.Dimension(19, 23));
        eaCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eaCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout eaPanelLayout = new javax.swing.GroupLayout(eaPanel);
        eaPanel.setLayout(eaPanelLayout);
        eaPanelLayout.setHorizontalGroup(
            eaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(eaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(eaPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(eaCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        eaPanelLayout.setVerticalGroup(
            eaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eaPanelLayout.createSequentialGroup()
                .addComponent(eaLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eaCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        filterPanel.add(eaPanel);

        table.setAutoCreateRowSorter(true);
        table.setModel(createTableModel());
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableScrollPane.setViewportView(table);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void latestCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latestCheckBoxActionPerformed
        filterChanged();
    }//GEN-LAST:event_latestCheckBoxActionPerformed

    private void distributionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distributionComboBoxActionPerformed
        filterChanged();
    }//GEN-LAST:event_distributionComboBoxActionPerformed

    private void versionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionComboBoxActionPerformed
        filterChanged();
    }//GEN-LAST:event_versionComboBoxActionPerformed

    private void packageTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageTypeComboBoxActionPerformed
        filterChanged();
    }//GEN-LAST:event_packageTypeComboBoxActionPerformed

    private void architectureComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_architectureComboBoxActionPerformed
        filterChanged();
    }//GEN-LAST:event_architectureComboBoxActionPerformed

    private void eaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eaCheckBoxActionPerformed
        filterChanged();
    }//GEN-LAST:event_eaCheckBoxActionPerformed

    private void filterChanged() {
        updateData((Distribution) distributionComboBox.getSelectedItem(),
                (Integer) versionComboBox.getSelectedItem(),
                (Architecture) architectureComboBox.getSelectedItem(),
                latestCheckBox.isSelected() ? Latest.OVERALL : Latest.ALL_OF_VERSION,
                (PackageType) packageTypeComboBox.getSelectedItem(),
                eaCheckBox.isSelected());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Architecture> architectureComboBox;
    private javax.swing.JComboBox<Distribution> distributionComboBox;
    private javax.swing.JCheckBox eaCheckBox;
    private javax.swing.JCheckBox latestCheckBox;
    private javax.swing.JComboBox<PackageType> packageTypeComboBox;
    protected javax.swing.JTable table;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JComboBox<Integer> versionComboBox;
    // End of variables declaration//GEN-END:variables
}
