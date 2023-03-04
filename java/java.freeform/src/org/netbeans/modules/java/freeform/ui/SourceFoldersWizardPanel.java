/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.modules.java.freeform.JavaProjectNature;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class SourceFoldersWizardPanel implements WizardDescriptor.Panel, ChangeListener, WizardDescriptor.FinishablePanel {

    private SourceFoldersPanel component;
    private WizardDescriptor wizardDescriptor;

    public SourceFoldersWizardPanel() {
        getComponent().setName(NbBundle.getMessage (NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_SourcePackageFolders"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new SourceFoldersPanel();
            component.setChangeListener(this);
            ((JComponent)component).getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(SourceFoldersWizardPanel.class, "ACSD_SourceFoldersWizardPanel")); // NOI18N            
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( SourceFoldersWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        // Panel is valid without any source folder specified, but
        // Next button is enabled only when there is some soruce 
        // folder specified -> see NewJ2SEFreeformProjectWizardIterator
        // which enables/disables Next button
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        return true;
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        File projectLocation = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_LOCATION);
        File projectFolder = (File)wizardDescriptor.getProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER);
        PropertyEvaluator evaluator = PropertyUtils.sequentialPropertyEvaluator(null, new PropertyProvider[]{
            PropertyUtils.fixedPropertyProvider(
            Collections.singletonMap(ProjectConstants.PROP_PROJECT_LOCATION, projectLocation.getAbsolutePath()))});

        ProjectModel pm = (ProjectModel)wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        if (pm == null ||
                !pm.getBaseFolder().equals(projectLocation) ||
                !pm.getNBProjectFolder().equals(projectFolder)) {
            pm = ProjectModel.createEmptyModel(projectLocation, projectFolder, evaluator);
            wizardDescriptor.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL, pm);
        }
        List<String> l = (List)wizardDescriptor.getProperty(NewJavaFreeformProjectSupport.PROP_EXTRA_JAVA_SOURCE_FOLDERS);
        if (l != null) {
            Iterator<String> it = l.iterator();
            while (it.hasNext()) {
                String path = it.next();
                assert it.hasNext();
                String label = it.next();
                // try to find if the model already contains this source folder
                boolean found = false;
                for (int i = 0; i < pm.getSourceFoldersCount(); i++) {
                    JavaProjectGenerator.SourceFolder existingSf = pm.getSourceFolder(i);
                    if (existingSf.location.equals(path)) {
                        found = true;
                        break;
                    }
                }
                // don't add the folder if it is already in the model
                if (!found) {
                    JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
                    sf.location = path;
                    sf.label = label;
                    sf.type = JavaProjectConstants.SOURCES_TYPE_JAVA;
                    sf.style = JavaProjectNature.STYLE_PACKAGES;
                    pm.addSourceFolder(sf, false);
                }
            }
        }
        
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
        component.setModel(pm, null);
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}
