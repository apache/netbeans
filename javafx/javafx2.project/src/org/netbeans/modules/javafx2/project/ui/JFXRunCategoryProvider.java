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
package org.netbeans.modules.javafx2.project.ui;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Somol
 * @author Tomas Zezula
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", position=450)
public final class JFXRunCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CAT_RUN = "Run"; // NOI18N
    
    @Override
    public Category createCategory(Lookup context) {
        boolean fxProjectEnabled = true;
        final Project project = context.lookup(Project.class);
        if (project != null) {
            final J2SEPropertyEvaluator j2sepe = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            fxProjectEnabled = JFXProjectProperties.isTrue(j2sepe.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED)) //NOI18N
                    && !JFXProjectProperties.isTrue(j2sepe.evaluator().getProperty(JFXProjectProperties.JAVAFX_PRELOADER)); //NOI18N
        }
        if(fxProjectEnabled) {
            return ProjectCustomizer.Category.create(CAT_RUN,
                    NbBundle.getMessage(JFXRunCategoryProvider.class, "LBL_Category_Run"), null); //NOI18N
        }
        return null;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        return new JFXRunPanel(JFXProjectProperties.getInstance(context));
    }

}
