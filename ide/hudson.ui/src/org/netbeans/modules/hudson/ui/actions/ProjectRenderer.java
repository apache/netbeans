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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Component;
import java.text.Collator;
import java.util.Comparator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

class ProjectRenderer extends DefaultListCellRenderer/*<Project>*/ {
    
    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null || value instanceof String) {
            return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
        }
        ProjectInformation info = ProjectUtils.getInformation((Project) value);
        JLabel label = (JLabel) super.getListCellRendererComponent(list, info.getDisplayName(), index, isSelected, cellHasFocus);
        label.setIcon(info.getIcon());
        return label;
    }

    static Comparator<Project> comparator() {
        return new Comparator<Project>() {
            Collator COLL = Collator.getInstance();
            @Override public int compare(Project o1, Project o2) {
                int r = COLL.compare(ProjectUtils.getInformation(o1).getDisplayName(),
                                     ProjectUtils.getInformation(o2).getDisplayName());
                if (r != 0) {
                    return r;
                } else {
                    return o1 == o2 ? 0 : o1.hashCode() - o2.hashCode();
                }
            }
        };
    }

}
