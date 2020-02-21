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
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 */
public class CndFilePlusWizardIterator extends CCFSrcFileIterator {

    @Override
    public void initialize(WizardDescriptor wiz) {
        templateWizard = (TemplateWizard) wiz;
        Project project = Templates.getProject(wiz);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        targetChooserDescriptorPanel = new NewCndFilePanel(project, groups, NewCndClassPanelGUI.Kind.CPP, null);
    }

    @Override
    public Set<DataObject> instantiate() throws IOException {
        TemplateWizard wiz = templateWizard;
        DataFolder targetFolder = wiz.getTargetFolder();
        DataObject template = wiz.getTemplate();

        String sourceFileName = wiz.getTargetName();
        FileObject sourceTemplate = template.files().iterator().next();

        Set<DataObject> res = new LinkedHashSet<DataObject>();

        FileObject bro = FileUtil.findBrother(sourceTemplate, "h"); // NOI18N
        if (bro != null) {
            DataObject dobjBro = DataObject.find(bro);
            String headerFileName = (String) wiz.getProperty("headerFileName"); // NOI18N
            DataFolder headerFolderName = (DataFolder) wiz.getProperty("headerFolder"); // NOI18N
            res.add(dobjBro.createFromTemplate(headerFolderName, headerFileName, Collections.singletonMap(CreateFromTemplateHandler.FREE_FILE_EXTENSION, Boolean.TRUE)));
        } else {
            String errmsg = NbBundle.getMessage(CndFilePlusWizardIterator.class, "MSG_missing_file_header_template", sourceTemplate.getName()+".h"); // NOI18N
            NotifyDescriptor.Message msg = new NotifyDescriptor.
                Message(errmsg, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
        }
        res.add(template.createFromTemplate(targetFolder, sourceFileName, Collections.singletonMap(CreateFromTemplateHandler.FREE_FILE_EXTENSION, Boolean.TRUE)));

        return res;
    }

    @Override
    public String name() {
        return "CndFilePlusWizardIterator"; //NOI18N
    }
}
