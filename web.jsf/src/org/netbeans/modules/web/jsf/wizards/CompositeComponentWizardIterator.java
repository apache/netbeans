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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author alexeybutenko
 */
public final class CompositeComponentWizardIterator implements TemplateWizard.Iterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private String selectedText;
    private static final String RESOURCES_FOLDER = "resources";  //NOI18N
    private static final String COMPONENT_FOLDER = "ezcomp";  //NOI18N


    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        DataObject result = null;
        String targetName = Templates.getTargetName(wizard);
        FileObject targetDir = Templates.getTargetFolder(wizard);
        DataFolder df = DataFolder.findFolder(targetDir);

        FileObject template = Templates.getTemplate( wizard );
        DataObject dTemplate = DataObject.find(template);
        HashMap<String, Object> templateProperties = new HashMap<String, Object>();
        if (selectedText != null) {
            templateProperties.put("implementation", selectedText);   //NOI18N
        }
        Project project = Templates.getProject(wizard);
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            JSFVersion version = JSFVersion.forWebModule(webModule);
            if (version != null && version.isAtLeast(JSFVersion.JSF_2_2)) {
                templateProperties.put("isJSF22", Boolean.TRUE); //NOI18N
            }
        }

        result  = dTemplate.createFromTemplate(df,targetName,templateProperties);
        return Collections.singleton(result);
    }



    @Override
    public void initialize(TemplateWizard wizard) {
        this.wizard = wizard;
        selectedText = (String) wizard.getProperty("selectedText"); //NOI18N

        Project project = Templates.getProject( wizard );
        Sources sources = project.getLookup().lookup(org.netbeans.api.project.Sources.class);

        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);

        WizardDescriptor.Panel folderPanel;
        if (sourceGroups == null || sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }

        FileObject targetFolder = null;
        FileObject resourceFolder = sourceGroups[0].getRootFolder().getFileObject(RESOURCES_FOLDER);
        if (resourceFolder != null) {
            FileObject componentFolder = resourceFolder.getFileObject(COMPONENT_FOLDER);
            if (componentFolder !=null) {
                targetFolder = componentFolder;
            } else {
                targetFolder = resourceFolder;
            }
        }

        if (targetFolder !=null) {
            Templates.setTargetFolder(wizard, targetFolder);
        }
        folderPanel = new CompositeComponentWizardPanel(wizard, sourceGroups, selectedText);

        panels = new WizardDescriptor.Panel[] { folderPanel };

        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer (i)); // NOI18N
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
	}
}

    @Override
    public void uninitialize(TemplateWizard wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return new StringBuilder().
                append(index).
                append(1).
                append(". "). //NOI18N
                append(NbBundle.getMessage(CompositeComponentWizardIterator.class, "MSG_From")). //NOI18N
                append(panels.length).
                toString();
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

}
