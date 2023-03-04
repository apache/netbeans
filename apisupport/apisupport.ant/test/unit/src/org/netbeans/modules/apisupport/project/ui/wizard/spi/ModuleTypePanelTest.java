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
package org.netbeans.modules.apisupport.project.ui.wizard.spi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.wizard.TypeChooserPanelImpl;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author akorostelev
 */
public class ModuleTypePanelTest extends TestBase {

    public ModuleTypePanelTest(String testName) {
        super(testName);
    }

    /**
     * Test of create method, of class ModuleTypePanel.
     */
    public void testCreate() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor() {};
        JComponent result = ModuleTypePanel.createComponent(wizardDescriptor);
        // panel is created
        assertNotNull(result);
        // expected panel implementation is returned
        assertTrue(result instanceof TypeChooserPanelImpl);
    }

    public void testPropertyChangeNotifications() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor() {};
        
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        l.ignore(NotifyDescriptor.PROP_MESSAGE, NotifyDescriptor.PROP_TITLE);
        wizardDescriptor.addPropertyChangeListener(l);
        
        JComponent typeChooserPanel = ModuleTypePanel.createComponent(wizardDescriptor);
        // test that changes notifications are received
        ModuleTypePanel.setProjectFolder(wizardDescriptor, new File(""));
        
        l.assertEvents(TypeChooserPanelImpl.IS_NETBEANS_ORG,
                TypeChooserPanelImpl.IS_STANDALONE_OR_SUITE_COMPONENT,
                TypeChooserPanelImpl.SUITE_ROOT,
                TypeChooserPanelImpl.ACTIVE_PLATFORM_ID,
                TypeChooserPanelImpl.ACTIVE_NB_PLATFORM,
                TypeChooserPanelImpl.PROJECT_FOLDER);
    }
    
    public void testIsPanelUpdated() throws IOException {
        WizardDescriptor wizardDescriptor = new WizardDescriptor() {};
        
        PanelUpdatedPCL l = new PanelUpdatedPCL();
        wizardDescriptor.addPropertyChangeListener(l);
        
        JComponent typeChooserPanel = ModuleTypePanel.createComponent(wizardDescriptor);
        // test that changes notifications are received
        File nbRootfolder = new File(getWorkDir(), "testProject");//NOI18N
        ModuleTypePanel.setProjectFolder(wizardDescriptor, nbRootfolder);
        assertTrue("panel updated event type received", l.isPanelUpdatedEvent());
    }

    // XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testPanelDisabledForNbOrg(){
        WizardDescriptor wizardDescriptor = new WizardDescriptor() {};
        JComponent typeChooserPanel = ModuleTypePanel.createComponent(wizardDescriptor);

        ModuleTypePanel.setProjectFolder(wizardDescriptor, 
                new File(nbRootFile(), "testProject"));
        
        assertTrue("project is in Nb Root", 
                ModuleTypePanel.isNetBeansOrg(wizardDescriptor));
        assertFalse("standalone radio buttyon is deselected",
                ModuleTypePanel.isStandalone(wizardDescriptor));
        assertFalse("suite component radio buttyon is deselected",
                ModuleTypePanel.isSuiteComponent(wizardDescriptor));
    }

    public void testPanelValuesForStandaloneModule(){
        WizardDescriptor wizardDescriptor = new WizardDescriptor() {};
        JComponent typeChooserPanel = ModuleTypePanel.createComponent(wizardDescriptor);

        ModuleTypePanel.setProjectFolder(wizardDescriptor, new File(""));
        
        assertFalse("project is NOT in Nb Root", 
                ModuleTypePanel.isNetBeansOrg(wizardDescriptor));
        assertTrue("standalone radios is selected",
                ModuleTypePanel.isStandalone(wizardDescriptor));
        assertNotNull("platrorm is selected",
                ModuleTypePanel.getActivePlatformId(wizardDescriptor));
        assertNotNull("platrorm is selected(test NbPlatform value)",
                (NbPlatform)wizardDescriptor.getProperty(TypeChooserPanelImpl.ACTIVE_NB_PLATFORM));
    }

    // XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testIsNetBeansOrgProperty() {
        WizardDescriptor wizardDescriptor = new WizardDescriptor() {};
        JComponent typeChooserPanel = ModuleTypePanel.createComponent(wizardDescriptor);

        File folder = new File("");
        Boolean isNbOrg = isFolderNbRoot(wizardDescriptor, folder);
        assertNotNull(isNbOrg);
        assertFalse(folder.getAbsolutePath() + " folder is NOT in NetBeans Root", isNbOrg);
        
        folder = new File(nbRootFile(), "testProject");//NOI18N
        isNbOrg = isFolderNbRoot(wizardDescriptor, folder );
        assertNotNull(isNbOrg);
        assertTrue(folder.getAbsolutePath() + " folder is in NetBeans Root", isNbOrg);
        
    }
    
    private Boolean isFolderNbRoot(WizardDescriptor wizardDescriptor, File folder){
        ModuleTypePanel.setProjectFolder(wizardDescriptor, folder);
        return ModuleTypePanel.isNetBeansOrg(wizardDescriptor);
    }
    
    public void testValidate(){
        WizardDescriptor wizardDescriptor = new WizardDescriptor() {};
        JComponent typeChooserPanel = ModuleTypePanel.createComponent(wizardDescriptor);
        // test default state
        ModuleTypePanel.validate(wizardDescriptor);
        assertNull("No error messages", 
                wizardDescriptor.getProperty(WizardDescriptor.PROP_ERROR_MESSAGE));
        
    }
    
    private static class PanelUpdatedPCL implements PropertyChangeListener{
        private boolean isPanelUpdated = false;

        public PanelUpdatedPCL() {
            reset();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            isPanelUpdated = isPanelUpdated || ModuleTypePanel.isPanelUpdated(evt);
        }
        
        public boolean isPanelUpdatedEvent(){
            return isPanelUpdated;
        }
                
        public void reset(){
            isPanelUpdated = false;
        }
    }
    
}
