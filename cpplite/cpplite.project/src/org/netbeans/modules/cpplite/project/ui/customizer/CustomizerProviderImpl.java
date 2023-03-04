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
package org.netbeans.modules.cpplite.project.ui.customizer;

import org.netbeans.modules.cpplite.project.CPPLiteProject;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.HelpCtx;

/**
 *
 * @author lahvac
 */
public class CustomizerProviderImpl implements CustomizerProvider2 {

    private final CPPLiteProject project;

    public CustomizerProviderImpl(CPPLiteProject project) {
        this.project = project;
    }

    @Override
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        ProjectCustomizer.createCustomizerDialog("Projects/" + CPPLiteProject.PROJECT_KEY + "/Customizer",
                                                 project.getLookup(),
                                                 preselectedCategory,
                                                 evt -> {},
                                                 HelpCtx.DEFAULT_HELP).setVisible(true);
    }

    @Override
    public void showCustomizer() {
        showCustomizer(null, null);
    }

}
