/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.server.maven.project;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 * Contributes the "Payara Server Maven Plugin" category to the Maven WAR project's
 * Project Properties dialog when the project declares
 * {@code payara-server-maven-plugin}.
 *
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
public class ServerMavenPropertiesPanelProvider
        implements ProjectCustomizer.CompositeCategoryProvider {

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-maven",
            position = 306
    )
    public static ServerMavenPropertiesPanelProvider createServerMaven() {
        return new ServerMavenPropertiesPanelProvider();
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        if (ServerMavenApplication.getInstance(project) == null) {
            return null;
        }
        return ProjectCustomizer.Category.create(
                "PayaraServerMaven",        // NOI18N
                "Payara Server Maven Plugin", // NOI18N
                null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        if (ServerMavenApplication.getInstance(project) == null) {
            return null;
        }
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        ServerMavenPropertiesPanel panel = new ServerMavenPropertiesPanel(handle, project);
        category.setStoreListener(e -> panel.applyChanges());
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }
}
