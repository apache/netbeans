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

package org.netbeans.modules.ant.freeform.ui;

import javax.swing.JComponent;
import org.netbeans.modules.ant.freeform.spi.ProjectAccessor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * 
 * @author Milan Kubec
 */
@CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-ant-freeform", position=50)
public class GeneralInformationCategoryProvider implements CompositeCategoryProvider {

    public Category createCategory(Lookup context) {
        return Category.create("generalInfo",   //NOI18N
                NbBundle.getMessage(TargetMappingPanel.class, "LBL_ProjectCustomizer_Category_General"),   //NOI18N
                null);
    }

    public JComponent createComponent(Category category, Lookup context) {
        ProjectAccessor acc = context.lookup(ProjectAccessor.class);
        AntProjectHelper helper = acc.getHelper();
        PropertyEvaluator evaluator = acc.getEvaluator();
        return new GeneralInformationPanel(helper, evaluator);
    }
    
}
