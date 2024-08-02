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
package org.netbeans.modules.maven.customizer;

import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;

final class MavenProjectPropertiesUiSupport {

    private final ModelHandle2 handle;
    private final Project project;

    private JComponent compilePanel;
    private ComboBoxModel platformComboBoxModel;
    private ComboBoxModel sourceLevelComboBoxModel;

    public MavenProjectPropertiesUiSupport(ModelHandle2 handle, Project project) {
        this.handle = handle;
        this.project = project;
        init();
    }

    private void init() {
        setPlatformComboBoxModel(new CompilePanel.PlatformsModel(project, handle));
        String sourceLevel = SourceLevelQuery.getSourceLevel(project.getProjectDirectory());
        setSourceLevelComboBoxModel(new CompilePanel.SourceLevelComboBoxModel(getPlatformComboBoxModel(), sourceLevel));
        setCompilePanel(new CompilePanel(handle, project, this));
    }

    /**
     * @return the compilePanel
     */
    public JComponent getCompilePanel() {
        return compilePanel;
    }

    /**
     * @param compilePanel the compilePanel to set
     */
    private void setCompilePanel(JComponent compilePanel) {
        this.compilePanel = compilePanel;
    }

    /**
     * @return the platformComboBoxModel
     */
    public ComboBoxModel getPlatformComboBoxModel() {
        return platformComboBoxModel;
    }

    /**
     * @param platformComboBoxModel the platformComboBoxModel to set
     */
    private void setPlatformComboBoxModel(ComboBoxModel platformComboBoxModel) {
        this.platformComboBoxModel = platformComboBoxModel;
    }

    /**
     * @return the sourceLevelComboBoxModel
     */
    public ComboBoxModel getSourceLevelComboBoxModel() {
        return sourceLevelComboBoxModel;
    }

    /**
     * @param sourceLevelComboBoxModel the sourceLevelComboBoxModel to set
     */
    private void setSourceLevelComboBoxModel(ComboBoxModel sourceLevelComboBoxModel) {
        this.sourceLevelComboBoxModel = sourceLevelComboBoxModel;
    }

}
