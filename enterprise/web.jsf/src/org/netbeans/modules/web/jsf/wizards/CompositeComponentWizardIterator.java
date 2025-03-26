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
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
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
        HashMap<String, Object> templateProperties = new HashMap<>();
        if (selectedText != null) {
            templateProperties.put("implementation", selectedText);   //NOI18N
        }
        Project project = Templates.getProject(wizard);
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            JsfVersion version = JsfVersionUtils.forWebModule(webModule);
            if (version != null && version.isAtLeast(JsfVersion.JSF_4_1)) {
                templateProperties.put("isJSF41", Boolean.TRUE); //NOI18N
            } else if (version != null && version.isAtLeast(JsfVersion.JSF_4_0)) {
                templateProperties.put("isJSF40", Boolean.TRUE); //NOI18N
            } else if (version != null && version.isAtLeast(JsfVersion.JSF_3_0)) {
                templateProperties.put("isJSF30", Boolean.TRUE); //NOI18N
            } else if (version != null && version.isAtLeast(JsfVersion.JSF_2_2)) {
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
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
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
