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

package org.netbeans.modules.project.ui.groups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static org.netbeans.modules.project.ui.groups.Bundle.*;
import static org.netbeans.modules.project.ui.groups.ManageGroupsPanel.NONE_GOUP;
import static org.netbeans.modules.project.ui.groups.NewGroupPanel.MAX_NAME;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.NbBundle.Messages;

/**
 * Interface used by the various group editing panels.
 * @author Jesse Glick
 */
public abstract class GroupEditPanel extends JPanel{

    private Category category;
    public static final String PROP_READY = "ready"; // NOI18N

    public Category getCategory() {
        return category;
    }

    protected abstract void applyChanges();

    @Messages("WARN_GroupExists=Another group with the same name exists.")
    protected boolean doCheckExistingGroups(JTextField field, Group actualGroup) {
        getCategory().setErrorMessage(null);
        getCategory().setValid(true);
        String name = field.getText();
                
        if (name != null) {
            if (name.trim().length() <= 0 || name.trim().length() >= MAX_NAME) {
                return false;
            }
            Set<Group> otherGroups = Group.allGroups();
            otherGroups.remove(actualGroup);
            if (name.equalsIgnoreCase(NONE_GOUP)) {
                getCategory().setErrorMessage(WARN_GroupExists());
                return false;
            }
            for (Group group : otherGroups) {
                if (name.equalsIgnoreCase(group.getName())) {
                    getCategory().setErrorMessage(WARN_GroupExists());
                    return false;
                }
            }
        }
        return true;
    }

    void setCategory(ProjectCustomizer.Category category) {
        this.category = category;
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyChanges();
            }
        });
    }
    
    public abstract boolean isReady();
}
