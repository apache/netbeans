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
