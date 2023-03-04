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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.project.ui.groups.Bundle.*;

/**
 *
 * @author mkleint
 */
@Registration(projectType = "Groups", position = 10)
public class GroupMainCategory implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    @Messages("LBL_General=General")
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create("general", LBL_General(), null, (ProjectCustomizer.Category[]) null);
    }

    @Override
    public JComponent createComponent(final ProjectCustomizer.Category category, Lookup context) {
        Group grp = context.lookup(Group.class);
        assert grp != null;
        
        final GroupEditPanel panel = grp.createPropertiesPanel();
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (GroupEditPanel.PROP_READY.equals(evt.getPropertyName())) {
                    category.setValid(panel.isReady());
                }
            }
        });
        panel.setCategory(category);
        return panel;
    }
    
}
