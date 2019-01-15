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
package org.netbeans.test.php.operators;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.JemmyProperties;

/**
 * Test of org.netbeans.jellytools.NewPHPProjectNameLocationStepOperator
 * @author mrkam@netbeans.org
 */
public class testNewPHPProjectNameLocationStepOperator extends JellyTestCase {
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(testNewPHPProjectNameLocationStepOperator.class);
    }
    
    @Override
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public testNewPHPProjectNameLocationStepOperator(String testName) {
        super(testName);
    }
    
    public void testPHPApplicationInQueueMode() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        doTest();
    }

    public void testPHPApplicationInRobotMode() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        doTest();
    }

    public void testPHPApplicationInSmoothRobotMode() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.SHORTCUT_MODEL_MASK);
        doTest();
    }

    public void testPHPApplicationInShortcutMode() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.SMOOTH_ROBOT_MODEL_MASK);
        doTest();
    }

    public void doTest() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory("PHP");
        // PHP Application
        String webApplicationLabel = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "Templates/Project/PHP/PHPProject.php");
        op.selectProject(webApplicationLabel);
        op.next();
        
        NewPHPProjectNameLocationStepOperator lsop = new NewPHPProjectNameLocationStepOperator();

        String project_name = "NewPHPProject";
//
//        while (lsop.getProjectName().length() > 0) {   
//            lsop.pressKey(KeyEvent.VK_BACK_SPACE);
//        }
        lsop.typeProjectName(project_name);
        assertEquals(project_name, lsop.getProjectName());

        lsop.browseSourceFolder();
        String selectSourceFolder = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_SelectSourceFolderTitle");
        new NbDialogOperator(selectSourceFolder).cancel(); // I18N
        
        String folder = lsop.getSelectedSourcesFolder() + File.separator + "test";
        lsop.typeSourcesFolder(folder);
        assertEquals(folder, lsop.getSelectedSourcesFolder());
        
        String encoding = NewPHPProjectNameLocationStepOperator.ENCODING_UTF8;
        lsop.selectDefaultEncoding(encoding);
        assertEquals(encoding, lsop.getSelectedDefaultEncoding());
        
        lsop.checkPutNetBeansMetadataIntoASeparateDirectory(true);
        assertEquals(true, lsop.cbPutNetBeansMetadataIntoASeparateDirectory().isSelected());
        
        folder = lsop.getMetadataFolder() + File.separator + "test";
        lsop.setMetadataFolder(folder);
        assertEquals(folder, lsop.getMetadataFolder());

        lsop.browseMetadataFolder();

        String selectProjectFolder = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "LBL_SelectProjectFolder");
        new NbDialogOperator(selectProjectFolder).cancel(); // I18N
        
        lsop.cancel();
    }
}
