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
package org.netbeans.modules.javafx2.samples;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.modules.javafx2.project.api.JavaFXProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Martin Grebac
 * @author Petr Somol
 */
public class JavaFXSampleProjectIterator implements TemplateWizard.Iterator {

    private static final Logger LOG = Logger.getLogger(JavaFXSampleProjectIterator.class.getName());
    
    private static final long serialVersionUID = 4L;

    int currentIndex;
    PanelConfigureProject basicPanel;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new JavaFXSampleProjectIterator();
    }

    public JavaFXSampleProjectIterator() {
    }

    @Override
    public void addChangeListener(javax.swing.event.ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener changeListener) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public org.openide.WizardDescriptor.Panel<WizardDescriptor> current() {
        return basicPanel;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void initialize(org.openide.loaders.TemplateWizard templateWizard) {
        this.wiz = templateWizard;
        String name = templateWizard.getTemplate().getNodeDelegate().getDisplayName();
        if (name != null) {
            name = name.replace(" ", ""); //NOI18N
        }
        templateWizard.putProperty(WizardProperties.NAME, name);
        basicPanel = new PanelConfigureProject(templateWizard.getTemplate().getNodeDelegate().getDisplayName());
        currentIndex = 0;
        updateStepsList();
    }

    @Override
    public void uninitialize(org.openide.loaders.TemplateWizard templateWizard) {
        basicPanel = null;
        currentIndex = -1;
        this.wiz.putProperty("projdir", null);           //NOI18N
        this.wiz.putProperty("name", null);          //NOI18N
    }

    @Override
    public java.util.Set<DataObject> instantiate(org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        File projectLocation = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        if(projectLocation == null) {
            warnIssue204880("Wizard property " + WizardProperties.PROJECT_DIR + " is null."); // NOI18N
            throw new IOException(); // return to wizard
        }
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        if(name == null) {
            warnIssue204880("Wizard property " + WizardProperties.NAME + " is null."); // NOI18N
            throw new IOException(); // return to wizard
        }
        String platformName = (String) wiz.getProperty(JavaFXProjectUtils.PROP_JAVA_PLATFORM_NAME);
        if(platformName == null) {
            warnIssue204880("Wizard property " + JavaFXProjectUtils.PROP_JAVA_PLATFORM_NAME + " is null."); // NOI18N
            throw new IOException(); // return to wizard
        }
        FileObject templateFO = templateWizard.getTemplate().getPrimaryFile();
        FileObject prjLoc = JavaFXSampleProjectGenerator.createProjectFromTemplate(
                templateFO, projectLocation, name, platformName);
        java.util.Set<DataObject> set = new java.util.HashSet<DataObject>();
        set.add(DataObject.find(prjLoc));

        // open file from the project specified in the "defaultFileToOpen" attribute
        Object openFile = templateFO.getAttribute("defaultFileToOpen"); // NOI18N
        if (openFile instanceof String) {
            FileObject openFO = prjLoc.getFileObject((String) openFile);
            set.add(DataObject.find(openFO));
        }
        // also open a documentation file registered for this project
        // and copy the .url file for it to the project (#71985)
        FileObject docToOpen = FileUtil.getConfigFile(
                "org-netbeans-modules-javafx2-samples/OpenAfterCreated/" + templateFO.getName() + ".url"); // NOI18N
        if (docToOpen != null) {
            docToOpen = FileUtil.copyFile(docToOpen, prjLoc, "readme"); // NOI18N
            set.add(DataObject.find(docToOpen));
        }

        return set;
    }

    @Override
    public String name() {
        return current().getComponent().getName();
    }

    @Override
    public void nextPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public void previousPanel() {
        throw new NoSuchElementException();
    }

    void updateStepsList() {
        JComponent component = (JComponent) current().getComponent();
        if (component == null) {
            return;
        }
        String[] list;
        list = new String[]{
            NbBundle.getMessage(PanelConfigureProject.class, "LBL_NWP1_ProjectTitleName"), // NOI18N
        };
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, list); // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, currentIndex); // NOI18N
    }
    
    private void warnIssue204880(final String msg) {
        LOG.log(Level.SEVERE, msg + " (issue 204880)."); // NOI18N
        Exception npe = new NullPointerException(msg + " (issue 204880)."); // NOI18N
        npe.printStackTrace();
        NotifyDescriptor d = new NotifyDescriptor.Message(
                NbBundle.getMessage(JavaFXSampleProjectIterator.class,"WARN_Issue204880"), NotifyDescriptor.ERROR_MESSAGE); // NOI18N
        DialogDisplayer.getDefault().notify(d);
    }
    
}
