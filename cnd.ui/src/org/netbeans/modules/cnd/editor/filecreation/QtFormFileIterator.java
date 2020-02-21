/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
