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

package org.netbeans.modules.maven.newproject;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeProvider;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class ChooseArchetypePanel extends JPanel {
    
    private static final RequestProcessor RP = new RequestProcessor(ChooseArchetypePanel.class.getName(),5);
    private static final Logger LOG = Logger.getLogger(ChooseArchetypePanel.class.getName());

    private ChooseWizardPanel wizardPanel;
    private final List<Archetype> archetypes = new ArrayList<Archetype>();

    public ChooseArchetypePanel(ChooseWizardPanel wizPanel) {
        initComponents();
        this.wizardPanel = wizPanel;
        textFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                updateList();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                updateList();
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        });
        DocumentListener dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                wizardPanel.fireChangeEvent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
               wizardPanel.fireChangeEvent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                wizardPanel.fireChangeEvent();
            }
        };
        textArtifactId.getDocument().addDocumentListener(dl);
        textGroupId.getDocument().addDocumentListener(dl);
        textVersion.getDocument().addDocumentListener(dl);
        textRepository.getDocument().addDocumentListener(dl);
        listArtifact.setCellRenderer(new ArchetypeRenderer());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (archetypes.isEmpty()) {
            listArtifact.setCursor(Utilities.createProgressCursor(listArtifact));
            RP.post(new Runnable() {
                @Override public void run() {
                    for (ArchetypeProvider provider : Lookup.getDefault().lookupAll(ArchetypeProvider.class)) {
                        final List<Archetype> added = provider.getArchetypes();
                        LOG.log(Level.FINE, "{0} -> {1}", new Object[] {provider, added});
                        added.sort(new Comparator<Archetype>() {
                            @Override
                            public int compare(Archetype o1, Archetype o2) {
                                int c = o1.getArtifactId().compareTo(o2.getArtifactId());                
                                if(c != 0) {
                                    return c;
                                }
                                String v1 = o1.getVersion();
                                String v2 = o2.getVersion();
                                if(v1 == null && v2 == null) {
                                    return 0;
                                } else if(v1 == null) {
                                    return 1;
                                } else if(v2 == null) {
                                    return -1;
                                }
                                return new DefaultArtifactVersion(v1).compareTo(new DefaultArtifactVersion(v2)) * -1;
                            }
                        });
                        EventQueue.invokeLater(new Runnable() {
                            @Override public void run() {
                                archetypes.addAll(added);
                                updateList();
                            }
                        });
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                            listArtifact.setCursor(null);
                            if (listArtifact.getSelectedIndex() == -1 && listArtifact.getModel().getSize() > 0) {
                                listArtifact.setSelectedIndex(0);
                            }
                        }
                    });
                }
            });
        }
    }

    private void updateList() {        
        Set<String> ids = new HashSet<String>();
        String filter = textFilter.getText();
        List<Archetype> filtered = new ArrayList<Archetype>();
        for (Archetype a : archetypes) {
            if (!showOld.isSelected() && !ids.add(a.getGroupId() + ":" + a.getArtifactId())) {
                        continue;                        
                    }
            if (!filter.isEmpty() && !matches(a.getGroupId(), filter) && !matches(a.getArtifactId(), filter) && !matches(a.getVersion(), filter) &&
                    (a.getRepository() == null || !matches(a.getRepository(), filter))
                    && !matches(a.getName(), filter) && (a.getDescription() == null || !matches(a.getDescription(), filter))) {
                continue;
            }
            filtered.add(a);
        }
        filtered.sort(new Comparator<Archetype>() {
        
            @Override
            public int compare(Archetype o1, Archetype o2) {
                return o1.getArtifactId().compareTo(o2.getArtifactId());
            }
        });
        DefaultListModel model = new DefaultListModel();
        for (Archetype a : filtered) {
            model.addElement(a);
        }
        listArtifact.setModel(model);
    }

    private boolean matches(String field, String filter) {
        return field.toLowerCase(Locale.ENGLISH).contains(filter.toLowerCase(Locale.ENGLISH));
    }

    private class ArchetypeRenderer extends DefaultListCellRenderer {

        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Archetype) {
                Archetype a = (Archetype) value;
                String n = a.getName();
                if (showOld.isSelected()) {
                    n += " (" + a.getVersion() + ")";
                }
                value = n;
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code (275:521)">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelFilter = new javax.swing.JLabel();
        textFilter = new javax.swing.JTextField();
        showOld = new javax.swing.JCheckBox();
        labelArchetypes = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listArtifact = new javax.swing.JList();
        labelGroupId = new javax.swing.JLabel();
        textGroupId = new javax.swing.JTextField();
        labelArtifactId = new javax.swing.JLabel();
        textArtifactId = new javax.swing.JTextField();
        labelVersion = new javax.swing.JLabel();
        textVersion = new javax.swing.JTextField();
        labelRepository = new javax.swing.JLabel();
        textRepository = new javax.swing.JTextField();
        labelDesc = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taDescription = new javax.swing.JTextArea();

        labelFilter.setLabelFor(textFilter);
        org.openide.awt.Mnemonics.setLocalizedText(labelFilter, org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ChooseArchetypePanel.labelFilter.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showOld, org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ChooseArchetypePanel.showOld.text")); // NOI18N
        showOld.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showOldActionPerformed(evt);
            }
        });

        labelArchetypes.setLabelFor(listArtifact);
        org.openide.awt.Mnemonics.setLocalizedText(labelArchetypes, org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ChooseArchetypePanel.labelArchetypes.text")); // NOI18N

        listArtifact.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listArtifact.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listArtifactValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(listArtifact);

        labelGroupId.setLabelFor(textGroupId);
        org.openide.awt.Mnemonics.setLocalizedText(labelGroupId, org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ChooseArchetypePanel.labelGroupId.text")); // NOI18N

        labelArtifactId.setLabelFor(textArtifactId);
        org.openide.awt.Mnemonics.setLocalizedText(labelArtifactId, org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ChooseArchetypePanel.labelArtifactId.text")); // NOI18N

        labelVersion.setLabelFor(textVersion);
        org.openide.awt.Mnemonics.setLocalizedText(labelVersion, org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ChooseArchetypePanel.labelVersion.text")); // NOI18N

        labelRepository.setLabelFor(textRepository);
        org.openide.awt.Mnemonics.setLocalizedText(labelRepository, org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ChooseArchetypePanel.labelRepository.text")); // NOI18N

        labelDesc.setLabelFor(taDescription);
        org.openide.awt.Mnemonics.setLocalizedText(labelDesc, org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "LBL_Description")); // NOI18N

        taDescription.setEditable(false);
        taDescription.setBackground((java.awt.Color)(UIManager.get("TextArea.background")!=null?
            UIManager.get("TextArea.background"):new java.awt.Color(238, 238, 238)));
    taDescription.setColumns(20);
    taDescription.setLineWrap(true);
    taDescription.setRows(5);
    taDescription.setText(org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ChooseArchetypePanel.taDescription.text")); // NOI18N
    taDescription.setWrapStyleWord(true);
    jScrollPane1.setViewportView(taDescription);
    taDescription.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ChooseArchetypePanel.class, "ArchetypesPanel.taDescription.accessibledesc")); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane1)
        .addGroup(layout.createSequentialGroup()
            .addComponent(labelFilter)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(textFilter)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(showOld))
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(labelGroupId)
                .addComponent(labelVersion))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(textGroupId)
                .addComponent(textVersion))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(labelArtifactId)
                .addComponent(labelRepository))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(textArtifactId)
                .addComponent(textRepository))
            .addContainerGap())
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(labelArchetypes)
                .addComponent(labelDesc))
            .addGap(0, 0, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(labelFilter)
                .addComponent(textFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(showOld))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(labelArchetypes)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(labelGroupId)
                .addComponent(textGroupId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(labelArtifactId)
                .addComponent(textArtifactId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(labelVersion)
                .addComponent(labelRepository)
                .addComponent(textVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(textRepository, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(labelDesc)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
    }// </editor-fold>//GEN-END:initComponents

    private void listArtifactValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listArtifactValueChanged
        Archetype a = (Archetype) listArtifact.getSelectedValue();
        if (a != null) {
            textGroupId.setText(a.getGroupId());
            textArtifactId.setText(a.getArtifactId());
            textVersion.setText(a.getVersion());
            textRepository.setText(a.getRepository());
            String d = a.getDescription();
            if (d != null) {
                taDescription.setText(d.replaceAll("\\s+", " ").replaceAll("^ | $", ""));
            } else {
                taDescription.setText(null);
            }
        } else {
            textGroupId.setText(null);
            textArtifactId.setText(null);
            textVersion.setText(null);
            textRepository.setText(null);
            taDescription.setText(null);
        }
        wizardPanel.fireChangeEvent();
    }//GEN-LAST:event_listArtifactValueChanged

    private void showOldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showOldActionPerformed
        updateList();
    }//GEN-LAST:event_showOldActionPerformed

    // Variables declaration - do not modify (275:518)//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelArchetypes;
    private javax.swing.JLabel labelArtifactId;
    private javax.swing.JLabel labelDesc;
    private javax.swing.JLabel labelFilter;
    private javax.swing.JLabel labelGroupId;
    private javax.swing.JLabel labelRepository;
    private javax.swing.JLabel labelVersion;
    private javax.swing.JList listArtifact;
    private javax.swing.JCheckBox showOld;
    private javax.swing.JTextArea taDescription;
    private javax.swing.JTextField textArtifactId;
    private javax.swing.JTextField textFilter;
    private javax.swing.JTextField textGroupId;
    private javax.swing.JTextField textRepository;
    private javax.swing.JTextField textVersion;
    // End of variables declaration (275:519)//GEN-END:variables
    
    void read(WizardDescriptor wizardDescriptor) {
    }

    void store(WizardDescriptor d) {
        if (!textGroupId.getText().isEmpty()) {
            Archetype a = new Archetype();
            a.setGroupId(textGroupId.getText());
            a.setArtifactId(textArtifactId.getText());
            a.setVersion(textVersion.getText());
            String r = textRepository.getText();
            if (!r.isEmpty()) {
                a.setRepository(r);
            }
            d.putProperty(MavenWizardIterator.PROP_ARCHETYPE, a);
        }
    }

    void validate(WizardDescriptor wizardDescriptor) {
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        return !textGroupId.getText().isEmpty() && !textArtifactId.getText().isEmpty() && !textVersion.getText().isEmpty();
    }

}
