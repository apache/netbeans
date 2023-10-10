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

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Iterator for generation of @FacesComponents.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FacesComponentIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_COMPONENT_NS = "http://xmlns.jcp.org/jsf/component"; //NOI18N

    private transient WizardDescriptor.Panel[] panels;
    private int index;

    private FacesComponentPanel facesComponentPanel;

    @Override
    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        String targetName = Templates.getTargetName(wizard);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);
        FileObject template = Templates.getTemplate(wizard);
        DataObject dTemplate = DataObject.find(template);
        Map<String, Object> templateProperties = new HashMap<String, Object>();

        String tagName = (String) wizard.getProperty(FacesComponentPanel.PROP_TAG_NAME);
        String tagNamespace = (String) wizard.getProperty(FacesComponentPanel.PROP_TAG_NAMESPACE);
        Boolean createSampleCode = (Boolean) wizard.getProperty(FacesComponentPanel.PROP_SAMPLE_CODE);
        if (!tagName.isEmpty() && !tagName.equals(tagNameForClassName(targetName))) {
            templateProperties.put("tagName", tagName); //NOI18N
        }
        if (!tagNamespace.isEmpty() && !tagNamespace.equals(DEFAULT_COMPONENT_NS)) {
            templateProperties.put("tagNamespace", tagNamespace); //NOI18N
        }
        if (createSampleCode) {
            templateProperties.put("sampleCode", Boolean.TRUE); //NOI18N
        }
        Project project = Templates.getProject(wizard);
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            JsfVersion version = JsfVersionUtils.forWebModule(webModule);
            if (version != null && version.isAtLeast(JsfVersion.JSF_3_0)) {
                templateProperties.put("jakartaJsfPackages", true); //NOI18N
            } else {
                templateProperties.put("jakartaJsfPackages", false); //NOI18N
            }
        } else {
            templateProperties.put("jakartaJsfPackages", true); //NOI18N
        }
        DataObject result = dTemplate.createFromTemplate(dataFolder, targetName, templateProperties);
        return Collections.singleton(result);
    }

    private static String tagNameForClassName(String className) {
        if (className.isEmpty()) {
            return ""; //NOI18N
        } else {
            return className.substring(0, 1).toLowerCase() + className.substring(1);
        }
    }

    @Override
    public void initialize(TemplateWizard wiz) {
        index = 0;
        Project project = Templates.getProject( wiz );
        panels = createPanels(project, wiz);

        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(TemplateIterator.class, "TITLE_x_of_y", index + 1, panels.length); //NOI18N
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    protected WizardDescriptor.Panel[] createPanels(Project project, TemplateWizard wiz) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        facesComponentPanel = new FacesComponentPanel(wiz);
        WizardDescriptor.Panel javaPanel;
        if (sourceGroups.length == 0) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(FacesComponentIterator.class, "MSG_No_Sources_found")); //NOI18N
            javaPanel = facesComponentPanel;
        } else {
            javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, facesComponentPanel);
        }
        return new WizardDescriptor.Panel[]{javaPanel};
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
}
