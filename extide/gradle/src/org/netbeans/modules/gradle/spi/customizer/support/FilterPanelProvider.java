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

package org.netbeans.modules.gradle.spi.customizer.support;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 * Use this <code>CompositeCategoryProvider</code> to register plugin aware
 * project customizer panels. The custom panel will be created if the given
 * plugin is present in the Gradle project.
 *
 * @author lkishalmi
 */
public final class FilterPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String ROOT_PROJECT = "ROOT-PROJECT";

    final ProjectCustomizer.CompositeCategoryProvider original;
    final String plugin;

    public FilterPanelProvider(ProjectCustomizer.CompositeCategoryProvider original, String plugin) {
        this.original = original;
        this.plugin = plugin;
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        Project project = context.lookup(Project.class);
        assert project != null;
        GradleBaseProject gbp = GradleBaseProject.get(project);
        return gbp.getPlugins().contains(plugin) || ROOT_PROJECT.equals(plugin) ? original.createCategory(context) : null;
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        return original.createComponent(category, context);
    }

}
