/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.run;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class RunContainerPropertiesVisual extends javax.swing.JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new form ContainerImagePanel
     */
    public RunContainerPropertiesVisual() {
        initComponents();
        volumesTable.setVisible(mountVolumesCheckBox.isSelected());
        nameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @NbBundle.Messages("LBL_RunContainerProperties=Container Properties")
    @Override
    public String getName() {
        return Bundle.LBL_RunContainerProperties();
    }

    public String getContainerName() {
        return UiUtils.getValue(nameTextField);
    }

    public void setContainerName(String name) {
        nameTextField.setText(name);
    }

    public String getCommand() {
        return UiUtils.getValue(commandTextField);
    }

    public void setCommand(String command) {
        commandTextField.setText(command);
    }

    public String getUser() {
        return UiUtils.getValue(userTextField);
    }

    public void setUser(String user) {
        userTextField.setText(user);
    }

    public boolean isInteractive() {
        return interactiveCheckBox.isSelected();
    }

    public void setInteractive(boolean interactive) {
        interactiveCheckBox.setSelected(interactive);
    }

    public boolean hasTty() {
        return ttyCheckBox.isSelected();
    }

    public void setTty(boolean tty) {
        ttyCheckBox.setSelected(tty);
    }
    
    public boolean isPrivileged() {
        return privilegedCheckBox.isSelected();
    }
    
    public void setPrivileged(boolean privileged) {
        privilegedCheckBox.setSelected(privileged);
    }
    
    public boolean areMountVolumesSelected() {
        return mountVolumesCheckBox.isSelected();
    }
    
    public void setMountVolumesSelected(boolean selected) {
        mountVolumesCheckBox.setSelected(selected);
    }
    public Map<String, String> getVolumesTable() {
        Map<String, String> result = new TreeMap<>();
        DefaultTableModel model = (DefaultTableModel) volumesTable.getModel();
        if (volumesTable.isEditing()) {
            volumesTable.getCellEditor().stopCellEditing();
        }
        for (Object o : model.getDataVector()) {
            Vector v = (Vector) o;
            String target = (String) v.elementAt(0);
            String source = (String) v.elementAt(1);
            if (target != null && source != null) {
                result.put(target, source);
            }
        }
        return result;
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        commandLabel = new javax.swing.JLabel();
        commandTextField = new javax.swing.JTextField();
        interactiveCheckBox = new javax.swing.JCheckBox();
        ttyCheckBox = new javax.swing.JCheckBox();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        privilegedCheckBox = new javax.swing.JCheckBox();
        mountVolumesCheckBox = new javax.swing.JCheckBox();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        volumesTable = new javax.swing.JTable();

        commandLabel.setLabelFor(commandTextField);
        org.openide.awt.Mnemonics.setLocalizedText(commandLabel, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.commandLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(interactiveCheckBox, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.interactiveCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ttyCheckBox, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.ttyCheckBox.text")); // NOI18N

        nameLabel.setLabelFor(nameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.nameLabel.text")); // NOI18N

        nameTextField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.userLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(privilegedCheckBox, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.privilegedCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mountVolumesCheckBox, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.mountVolumesCheckBox.text")); // NOI18N
        mountVolumesCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mountVolumesCheckBoxStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.addButton.text")); // NOI18N
        addButton.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.deleteButton.text")); // NOI18N
        deleteButton.setEnabled(false);

        volumesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Target", "Source"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        volumesTable.setEnabled(false);
        volumesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(volumesTable);
        if (volumesTable.getColumnModel().getColumnCount() > 0) {
            volumesTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.volumesTable.columnModel.title0")); // NOI18N
            volumesTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(RunContainerPropertiesVisual.class, "RunContainerPropertiesVisual.volumesTable.columnModel.title1")); // NOI18N
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(interactiveCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(ttyCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(commandLabel)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameTextField)
                    .addComponent(commandTextField)
                    .addComponent(userTextField)))
            .addComponent(privilegedCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mountVolumesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(commandLabel)
                    .addComponent(commandTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel)
                    .addComponent(userTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(interactiveCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ttyCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(privilegedCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mountVolumesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)
                        .addContainerGap(75, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mountVolumesCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mountVolumesCheckBoxStateChanged
        boolean selected = mountVolumesCheckBox.isSelected();
        volumesTable.setVisible(selected);
        volumesTable.setEnabled(selected);
        addButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }//GEN-LAST:event_mountVolumesCheckBoxStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel commandLabel;
    private javax.swing.JTextField commandTextField;
    private javax.swing.JButton deleteButton;
    private javax.swing.JCheckBox interactiveCheckBox;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JCheckBox mountVolumesCheckBox;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JCheckBox privilegedCheckBox;
    private javax.swing.JCheckBox ttyCheckBox;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    private javax.swing.JTable volumesTable;
    // End of variables declaration//GEN-END:variables
}
