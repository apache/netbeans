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
package org.netbeans.modules.cordova.project;

import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public class CordovaPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    public Category createCategory(Lookup context) {
            Project project = context.lookup(Project.class);
            assert project != null;
            if (ProjectWebRootQuery.getWebRoots(project).isEmpty()) {
                return null;
            }
            return ProjectCustomizer.Category.create(
                    "phonegap",//NOI18N
                    "Cordova",//NOI18N
                    null);
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        return new CordovaCustomizerPanel(context.lookup(Project.class), category);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org.netbeans.modules.web.clientproject",//NOI18N
            position = 355)
    public static CordovaPanelProvider createRunConfigs() {
        return new CordovaPanelProvider();
    }
    
}
