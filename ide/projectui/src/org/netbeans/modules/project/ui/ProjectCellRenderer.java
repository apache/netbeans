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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

/**
 * Cell renderer for {@link Project}.
 */
// #89393: GTK needs cell renderer to implement UIResource to look "natively"
public class ProjectCellRenderer extends JLabel implements ListCellRenderer, UIResource {

    public ProjectCellRenderer() {
        setOpaque(true);
    }

    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // #89393: GTK needs name to render cell renderer "natively"
        setName("ComboBox.listRenderer"); // NOI18N
        if (value instanceof Project) {
            ProjectInformation pi = ProjectUtils.getInformation((Project) value);
            setText(pi.getDisplayName());
            setIcon(pi.getIcon());
        } else {
            setText(value == null ? "" : value.toString()); // NOI18N
            setIcon(null);
        }
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }

    // #89393: GTK needs name to render cell renderer "natively"
    @Override public String getName() {
        String name = super.getName();
        return name == null ? "ComboBox.renderer" : name; // NOI18N
    }

}
