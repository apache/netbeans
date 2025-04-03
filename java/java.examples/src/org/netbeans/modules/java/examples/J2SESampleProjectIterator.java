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

package org.netbeans.modules.java.examples;

import java.io.File;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * @author Martin Grebac
 */
public class J2SESampleProjectIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private static final long serialVersionUID = 4L;

    int currentIndex;
    PanelConfigureProject basicPanel;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new J2SESampleProjectIterator();
    }
    
    public J2SESampleProjectIterator () {
    }
    
    public void addChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current () {
        return basicPanel;
    }
    
    public boolean hasNext () {
        return false;
    }
    
    public boolean hasPrevious () {
        return false;
    }
    
    @Override public void initialize(WizardDescriptor templateWizard) {
        this.wiz = templateWizard;
        String displayName;
        try {
            displayName = DataObject.find(Templates.getTemplate(wiz)).getNodeDelegate().getDisplayName();
        } catch (DataObjectNotFoundException ex) {
            displayName = "unknown";
        }
        String name = displayName;
        if (name != null) {
            name = name.replace(" ", ""); //NOI18N
        }
        templateWizard.putProperty (WizardProperties.NAME, name);        
        basicPanel = new PanelConfigureProject(displayName);
        currentIndex = 0;
        updateStepsList ();
    }
    
    @Override public void uninitialize(WizardDescriptor templateWizard) {
        basicPanel = null;
        currentIndex = -1;
        this.wiz.putProperty("projdir",null);           //NOI18N
        this.wiz.putProperty("name",null);          //NOI18N
    }
    
    @Override public Set instantiate() throws java.io.IOException {
        File projectLocation = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        FileObject templateFO = Templates.getTemplate(wiz);
        FileObject prjLoc = J2SESampleProjectGenerator.createProjectFromTemplate(
                              templateFO, projectLocation, name);

        Set<DataObject> set = new HashSet<>();
        set.add(DataObject.find(prjLoc));

        // open file from the project specified in the "defaultFileToOpen" attribute
        Object openFile = templateFO.getAttribute("defaultFileToOpen"); // NOI18N
        if (openFile instanceof String) {
            FileObject openFO = prjLoc.getFileObject((String)openFile);
            set.add(DataObject.find(openFO));
        }
        // also open a documentation file registered for this project
        // and copy the .url file for it to the project (#71985)
        FileObject docToOpen = FileUtil.getConfigFile(
            "org-netbeans-modules-java-examples/OpenAfterCreated/" + templateFO.getName() + ".url"); // NOI18N
        if (docToOpen != null) {
            docToOpen = FileUtil.copyFile(docToOpen, prjLoc, "readme"); // NOI18N
            set.add(DataObject.find(docToOpen));
        }

        return set;
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        throw new NoSuchElementException ();
    }
    
    public void previousPanel() {
        throw new NoSuchElementException ();
    }
    
    void updateStepsList() {
        JComponent component = (JComponent) current ().getComponent ();
        if (component == null) {
            return;
        }
        String[] list;
        list = new String[] {
            NbBundle.getMessage(PanelConfigureProject.class, "LBL_NWP1_ProjectTitleName"), // NOI18N
        };
        component.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, list); // NOI18N
        component.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, currentIndex); // NOI18N
    }
    
}
