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

package org.netbeans.modules.cnd.refactoring.codegen.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;

/**
 *
 */
public class SuperConstructorSelectorPanel extends JPanel {
    private final Map<JRadioButton,ElementNode.Description> buttons = new LinkedHashMap<>();
    private JRadioButton firstButton;

    public SuperConstructorSelectorPanel(ElementNode.Description elementDescription) {
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints;
        int y = 0;
        for(ElementNode.Description cls : elementDescription.getSubs()) {
            JLabel label = new JLabel(cls.getName()+":"); //NOI18N
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = y++;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
            add(label, gridBagConstraints);
            ButtonGroup group = new ButtonGroup();
            boolean first = true;
            for(ElementNode.Description cons : cls.getSubs()) {
                String displayName = cons.getDisplayName();
                if (displayName.length()>50) {
                    displayName = displayName.substring(0, 50)+"...)"; //NOI18N
                }
                JRadioButton button = new JRadioButton(displayName, first);
                if (firstButton == null) {
                    firstButton = button;
                }
                group.add(button);
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = y++;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(0, 18, 6, 6);
                add(button, gridBagConstraints);
                buttons.put(button, cons);
                first = false;
            }
        }
        JPanel panel = new JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(panel, gridBagConstraints);
    }

    public List<CsmDeclaration> getSelectedElements() {
        ArrayList<CsmDeclaration> handles = new ArrayList<>();
        for(Map.Entry<JRadioButton,ElementNode.Description> entry : buttons.entrySet()) {
            if (entry.getKey().isSelected()) {
                handles.add(entry.getValue().getElementHandle());
            }
        }
        return handles;
    }
}
