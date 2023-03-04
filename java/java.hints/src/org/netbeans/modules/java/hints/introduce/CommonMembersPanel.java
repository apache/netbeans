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
package org.netbeans.modules.java.hints.introduce;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.java.hints.introduce.TargetDescription;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.HtmlRenderer.Renderer;

/**
 *
 * @author lahvac
 */
public class CommonMembersPanel extends javax.swing.JPanel {

    private final Iterable<TargetDescription> targets;

    public CommonMembersPanel(Iterable<TargetDescription> targets) {
        this.targets = targets;
    }

    private JComboBox targetsComboBox;
    private JCheckBox duplicates;

    protected final void initialize(JComboBox targetsComboBox, JCheckBox duplicates) {
        this.targetsComboBox = targetsComboBox;
        this.duplicates = duplicates;
        
        duplicates.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                updateTargetsModel();
            }
        });

        updateTargetsModel();
    }

    private void updateTargetsModel() {
        Object originalSelected = targetsComboBox.getSelectedItem();
        DefaultComboBoxModel<TargetDescription> model = new DefaultComboBoxModel<>();
        int lastGood = -1;

        for (TargetDescription td : targets) {
            if (duplicates.isSelected() && !td.allowForDuplicates) continue;
            model.addElement(td);
            if (!td.anonymous) lastGood++;
        }

        targetsComboBox.setModel(model);

        if (originalSelected != null) targetsComboBox.setSelectedItem(originalSelected);
        else if (lastGood >= 0) targetsComboBox.setSelectedIndex(lastGood);
        else if (model.getSize() > 0) targetsComboBox.setSelectedIndex(0);

        targetsComboBox.setEnabled(model.getSize() != 1);
    }

    public Iterable<TargetDescription> getTargets() {
        return targets;
    }

    public TargetDescription getSelectedTarget() {
        return targetDescriptionTest != null ? targetDescriptionTest : (TargetDescription) targetsComboBox.getSelectedItem();
    }

    public static final class TargetsRendererImpl implements ListCellRenderer {
        private final Renderer renderer = HtmlRenderer.createRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof TargetDescription) value = "<html>" + ((TargetDescription) value).displayName;
            return renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    //For tests:
    private TargetDescription targetDescriptionTest;

    void setTargetDescription(String classBinaryName) {
        for (TargetDescription td : targets) {
            if (classBinaryName.equals(td.type.getBinaryName())) {
                this.targetDescriptionTest = td;
                return;
            }
        }

        throw new IllegalStateException();
    }
}
