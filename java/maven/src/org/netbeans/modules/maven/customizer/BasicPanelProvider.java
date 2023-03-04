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

package org.netbeans.modules.maven.customizer;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import static org.netbeans.modules.maven.customizer.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milos Kleint 
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-maven", position=100)
public class BasicPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {
    
    @Override
    @Messages("TIT_Basic=General")
    public Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(
                ModelHandle2.PANEL_BASIC, 
                TIT_Basic(), 
                null);
    }
    
    @Override
    public JComponent createComponent(Category category, Lookup context) {
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        Project prj = context.lookup(Project.class);
        return new BasicInfoPanel(handle, category, prj);
    }
    
}
