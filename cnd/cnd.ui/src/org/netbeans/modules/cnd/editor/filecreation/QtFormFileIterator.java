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

package org.netbeans.modules.cnd.editor.filecreation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.editor.filecreation.NewQtFormPanel.FormType;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 */
public class QtFormFileIterator extends CCFSrcFileIterator {

    @Override
    public Set<DataObject> instantiate() throws IOException {
        TemplateWizard wiz = templateWizard;
        DataFolder targetFolder = wiz.getTargetFolder();

        Set<DataObject> dataObjects = new HashSet<DataObject>();

        String fileName = getFormType().templateFileName;
        DataObject formDataObject = NewQtFormPanel.getTemplateDataObject(fileName);
        dataObjects.add(formDataObject.createFromTemplate(targetFolder, wiz.getTargetName()));

        if (getSourceFileName() != null) {
            Map<String, Boolean> params = Collections.singletonMap(CreateFromTemplateHandler.FREE_FILE_EXTENSION, Boolean.TRUE);
            DataObject sourceDataObject = NewQtFormPanel.getTemplateDataObject("form.cc"); // NOI18N
            dataObjects.add(sourceDataObject.createFromTemplate(targetFolder, getSourceFileName(), params));

            DataObject headerDataObject = NewQtFormPanel.getTemplateDataObject("form.h"); // NOI18N
            dataObjects.add(headerDataObject.createFromTemplate(targetFolder, getHeaderFileName(), params));
        }

        return dataObjects;
    }

    @Override
    protected Panel<WizardDescriptor> createPanel(TemplateWizard wiz) {
        Project project = Templates.getProject(wiz);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        return new NewQtFormPanel(project, groups);
    }

    private FormType getFormType() {
        return ((NewQtFormPanelGUI)targetChooserDescriptorPanel.getComponent()).getFormType();
    }

    private String getSourceFileName() {
        return ((NewQtFormPanelGUI)targetChooserDescriptorPanel.getComponent()).getSourceFileName();
    }

    private String getHeaderFileName() {
        return ((NewQtFormPanelGUI)targetChooserDescriptorPanel.getComponent()).getHeaderFileName();
    }

}
