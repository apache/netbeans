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

package org.netbeans.modules.maven.options;

import javax.swing.JComponent;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.options.Bundle.*;

/**
 *
 * @author mkleint
 */
@Registration(projectType = "Groups", position = 200)
public class MavenGroupCategory implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    @Messages("LBL_GroupMaven=Maven")
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create("maven", LBL_GroupMaven(), null, (ProjectCustomizer.Category[]) null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        ProjectGroup grp = context.lookup(ProjectGroup.class);
        assert grp != null;
        
        MavenGroupPanel panel = new MavenGroupPanel(category, grp);

        return panel;
    }
    
}
