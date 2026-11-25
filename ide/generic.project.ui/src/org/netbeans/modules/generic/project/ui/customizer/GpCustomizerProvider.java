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

package org.netbeans.modules.generic.project.ui.customizer;

import java.awt.Dialog;
import org.netbeans.api.project.Project;
import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;

/**
 * Shows the project properties (customizer) window for a generic project.
 */
// Registered under both service names: the project tree's Properties action
// looks up the base CustomizerProvider, while some consumers use
// CustomizerProvider2. The lazy project lookup (LazyLookup) only matches the
// exact service names declared here, not subtypes.
@ProjectServiceProvider(service={CustomizerProvider2.class, CustomizerProvider.class}, projectType=GenericProject.PROJECT_KEY)
public final class GpCustomizerProvider implements CustomizerProvider2 {

    private final Project prj;

    public GpCustomizerProvider(Project prj) {
        this.prj = prj;
    }

    @Override
    public void showCustomizer() {
        showCustomizer(null, null);
    }

    @Override
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        Dialog dialog = ProjectCustomizer.createCustomizerDialog(
                "Projects/" + GenericProject.PROJECT_KEY + "/Customizer", // NOI18N
                prj.getLookup(),
                preselectedCategory,
                evt -> {
                },
                HelpCtx.DEFAULT_HELP);
        dialog.setVisible(true);
    }

}
