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
