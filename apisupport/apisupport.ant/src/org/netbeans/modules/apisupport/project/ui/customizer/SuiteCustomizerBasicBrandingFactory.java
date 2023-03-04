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

package org.netbeans.modules.apisupport.project.ui.customizer;

import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import static org.netbeans.modules.apisupport.project.ui.customizer.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(
    projectType="org-netbeans-modules-apisupport-project-suite",
    position=300,
    category="Build",
    categoryLabel="#LBL_Application"
)
@Messages("LBL_Application=Application")
public class SuiteCustomizerBasicBrandingFactory implements ProjectCustomizer.CompositeCategoryProvider {
    
    public ProjectCustomizer.Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(
                SuiteCustomizer.APPLICATION, 
                LBL_Application(),
                null);
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        SuiteProperties props = context.lookup(SuiteProperties.class);
        BasicCustomizer.SubCategoryProvider prov = context.lookup(BasicCustomizer.SubCategoryProvider.class);
        assert props != null;
        assert prov != null;
        return new SuiteCustomizerBasicBranding(props, category, prov);
    }

}
