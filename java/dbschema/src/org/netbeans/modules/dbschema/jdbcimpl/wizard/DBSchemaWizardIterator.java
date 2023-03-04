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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.openide.loaders.TemplateWizard;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;

/** Iterator implementation which can iterate through two
 * panels which forms dbschema template wizard
 */
public class DBSchemaWizardIterator implements TemplateWizard.Iterator {
    private static final Logger LOG = Logger.getLogger(DBSchemaWizardIterator.class.getName());
    
    static final long serialVersionUID = 9197272899287477324L;
    
    private final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N
    
    private WizardDescriptor.Panel panels[];
    private static String panelNames[];
    private static final int PANEL_COUNT = 3;
    private int panelIndex;
    private static DBSchemaWizardIterator instance;
    private TemplateWizard wizardInstance;
    private boolean guiInitialized;
    private DBSchemaWizardData myData;
    
    public DBSchemaWizardIterator() {
        super();
        panelIndex = 0;
    }
    
    public static synchronized DBSchemaWizardIterator singleton() {
        if(instance == null) {
            instance = new DBSchemaWizardIterator();
        }
        
        return instance;
    }
    
    @Override
    public Set instantiate(TemplateWizard wiz) throws IOException {
//        System.out.println(wiz.getTargetFolder());
        myData.setName(wiz.getTargetName());
        myData.setDestinationPackage(wiz.getTargetFolder());
        
        CaptureSchema capture = new CaptureSchema(myData);
        capture.start();
        
        return null;///Collections.singleton(null);
    }
    
    @Override
    public org.openide.WizardDescriptor.Panel current() {
        return panels[panelIndex];
    }
    
    @Override
    public String name() {
        return panelNames[panelIndex];
    }
    
    @Override
    public boolean hasNext() {
        return panelIndex < PANEL_COUNT - 1;
    }
    
    @Override
    public boolean hasPrevious() {
        return panelIndex > 0;
    }
    
    @Override
    public void nextPanel() {
        if (panelIndex == 1) {//== connection panel
            ((DBSchemaConnectionPanel) panels[1].getComponent()).initData();
            if (! (((DBSchemaTablesPanel) panels[2].getComponent()).init())) {
                return;
            }
        }
        
        panelIndex++;
    }
    
    @Override
    public void previousPanel() {
        panelIndex--;
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    
    @Override
    public void initialize(TemplateWizard wizard) {
        wizardInstance = wizard;
        setDefaultTarget();
        String[] prop = (String[]) wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] stepsNames;
        if (wizard.targetChooser().getClass().toString().trim().equalsIgnoreCase("class org.openide.loaders.TemplateWizard2")) {
            stepsNames = new String[] {
                bundle.getString("TargetLocation") ,
                bundle.getString("TargetLocation"),
                bundle.getString("ConnectionChooser"),
                bundle.getString("TablesChooser")
            };
        } else if (null != prop) {
            stepsNames = new String[] {
                prop[0],
                bundle.getString("TargetLocation"),
                bundle.getString("ConnectionChooser"),
                bundle.getString("TablesChooser")
            };
        } else {
            stepsNames = new String[] {
                bundle.getString("TargetLocation"),
                bundle.getString("ConnectionChooser"),
                bundle.getString("TablesChooser")
            };
        }
        wizardInstance.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); //NOI18N
        wizardInstance.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); //NOI18N
        wizardInstance.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); //NOI18N
        wizardInstance.putProperty(WizardDescriptor.PROP_CONTENT_DATA, stepsNames); //NOI18N
        
        if(!guiInitialized) {
            initialize();
            
            myData = new DBSchemaWizardData();
            panels = new WizardDescriptor.Panel[PANEL_COUNT];
            
            DBSchemaTargetPanel targetPanel = new DBSchemaTargetPanel();
            targetPanel.setPanel(wizard.targetChooser());
            
            java.awt.Component panel = targetPanel.getComponent();
            if (panel instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) panel).putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, stepsNames); //NOI18N
                ((javax.swing.JComponent) panel).putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, 0); //NOI18N
            }
            
            panels[0] = targetPanel.getPanel();
            panels[1] = new DBSchemaConnectionWizardPanel(myData);
            panels[2] = new DBSchemaTablesWizardPanel(myData);
        }
        
        panelIndex = 0;
    }
    
    @Override
    public void uninitialize(TemplateWizard wiz) {
        if (wiz.getValue() == NotifyDescriptor.CANCEL_OPTION) {
            ((DBSchemaTablesPanel) panels[2].getComponent()).uninit();
        }
        
        panels = null;
        myData = null;
        guiInitialized = false;
    }
    
    protected void initialize() {
        if(panelNames == null) {
            panelNames = new String[PANEL_COUNT];
            panelNames[0] = ""; //NOI18N
            panelNames[1] = ""; //NOI18N
            panelNames[2] = ""; //NOI18N
        }
    }
    
    /**
     * Hack which sets the default target to the src/conf or src directory, 
     * whichever exists.
     */
    private void setDefaultTarget() {
        FileObject targetFO;
        try {
            DataFolder target = wizardInstance.getTargetFolder();
            targetFO = target.getPrimaryFile();
        } catch (IOException e) {
            targetFO = null;
        }
        
        Project targetProject = Templates.getProject(wizardInstance);
        if (targetProject != null) {
            FileObject projectDir = targetProject.getProjectDirectory();
            if (targetFO == null || targetFO.equals(projectDir)) {
                FileObject newTargetFO = projectDir.getFileObject("src/conf"); // NOI18N
                if (newTargetFO == null || !newTargetFO.isValid()) {
                    newTargetFO = projectDir.getFileObject("src/META-INF"); // NOI18N
                    // take existence of <projectdir>/src/main as indication
                    // of maven style project layout
                    FileObject tempFo = projectDir.getFileObject("src/main"); // NOI18N
                    if (tempFo != null) {
                        try {
                            newTargetFO = FileUtil.createFolder(tempFo, "resources/META-INF");
                        } catch (IOException ex) {
                            LOG.log(Level.INFO, "Failed to create META-INF folder", ex);
                        }
                    }
                    if (newTargetFO == null) {
                        if (newTargetFO == null || !newTargetFO.isValid()) {
                            newTargetFO = projectDir.getFileObject("src"); // NOI18N
                            if (newTargetFO == null || !newTargetFO.isValid()) {
                                return;
                            }
                        }
                    }
                }

                DataFolder newTarget = DataFolder.findFolder(newTargetFO);
                wizardInstance.setTargetFolder(newTarget);
            }
        }
    }
}
