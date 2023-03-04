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
package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for New REST web services from Entity Classes wizard
 *
 * Duration of this test suite: approx. 3min
 *
 * @author lukas
 */
public class CRUDTest extends RestTestBase {

    private static final Logger LOGGER = Logger.getLogger(CRUDTest.class.getName());

    /**
     * Default constructor.
     *
     * @param testName name of particular test case
     */
    public CRUDTest(String name) {
        super(name, Server.GLASSFISH);
    }

    /**
     * Constructor
     *
     * @param testName name of particular test case
     * @param server type of server to be used
     */
    public CRUDTest(String name, Server server) {
        super(name, server);
    }

    @Override
    protected String getProjectName() {
        return "FromEntities"; //NOI18N
    }

    protected String getRestPackage() {
        return "o.n.m.ws.qaf.rest.crud"; //NOI18N
    }

    /**
     * Create new web project with entity classes from sample database
     * (jdbc/sample), create new RESTful web services from created entities and
     * deploy the project
     */
    public void testRfE() {
        if (!getProjectType().isAntBasedProject()) {
            createPU();
        }
        copyDBSchema();
        //Persistence
        String persistenceLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.ui.resources.Bundle", "Templates/Persistence");
        //Entity Classes from Database
        String fromDbLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "Templates/Persistence/RelatedCMP");
        createNewFile(getProject(), persistenceLabel, fromDbLabel);
        WizardOperator wo = prepareEntityClasses(new WizardOperator(fromDbLabel), getProjectType().isAntBasedProject());
        wo.finish();
        String generationTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "TXT_EntityClassesGeneration");
        waitDialogClosed(generationTitle);
        new EventTool().waitNoEvent(1500);


        //RESTful Web Services from Entity Classes
        String restLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromEntities");
        createNewWSFile(getProject(), restLabel);
        wo = new WizardOperator(restLabel);
        //have to wait until "retrieving message dissapers (see also issue 122802)
        new EventTool().waitNoEvent(2500);
        //Add All >>
        String addAllLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_AddAll");
        new JButtonOperator(wo, addAllLabel).pushNoBlock();
        wo.next();
        //Resource Package
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 1);
        jcbo.clearText();
        jcbo.typeText(getRestPackage() + ".service"); //NOI18N
        //Converter Package - needed only with EE5
        if (getJavaEEversion().equals(JavaEEVersion.JAVAEE5)) {
            jcbo = new JComboBoxOperator(wo, 2);
            jcbo.clearText();
            jcbo.typeText(getRestPackage() + ".controller"); //NOI18N
        }
        wo.btFinish().requestFocus();
        wo.btFinish().push();
        wo.waitClosed();
        new EventTool().waitNoEvent(1500);
        waitScanFinished();

        String packageName = getRestPackage() + ".service";
        Set<File> files = getFiles(packageName);
        Set<File> allFiles = new HashSet<File>(files);
        if (!getJavaEEversion().equals(JavaEEVersion.JAVAEE5)) {
            assertEquals("Missing files in package " + packageName, 9, files.size()); //NOI18N
        } else {
            // Java EE 5 -  see http://netbeans.org/bugzilla/show_bug.cgi?id=189723
            assertEquals("Missing files in package " + packageName, 8, files.size()); //NOI18N
            packageName = getRestPackage() + ".controller"; //NOI18N
            files = getFiles(packageName);
            allFiles.addAll(files);
            assertEquals("Missing files in package " + packageName, 7, files.size()); //NOI18N
            packageName = getRestPackage() + ".controller.exceptions"; //NOI18N
            files = getFiles(packageName);
            allFiles.addAll(files);
            assertEquals("Missing files in package " + packageName, 4, files.size()); //NOI18N
        }
        // ignore ApplicationConfig because it is different across EE levels and is generated asynchronously
        File appConfigFile = null;
        for (File file : allFiles) {
            if (file.getName().contains("ApplicationConfig")) {
                appConfigFile = file;
            }
        }
        allFiles.remove(appConfigFile);
        checkFiles(allFiles);
        //make sure all REST services nodes are visible in project log. view
        waitRestNodeChildren(7);
    }

    /**
     * Test creation of RESTful web service from an entity class which uses
     * property based access. Also tests functionality of the new RESTful web
     * service from entity classes wizard (buttons, updating model in the
     * wizard)
     */
    public void testPropAccess() throws IOException {
        //copy entity class into a project
        FileObject fo = FileUtil.toFileObject(new File(getRestDataDir(), "Person.java.gf")); //NOI18N
        fo.copy(getProjectSourceRoot().createFolder("entity"), "Person", "java"); //NOI18N
        //RESTful Web Services from Entity Classes
        String restLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromEntities");
        createNewWSFile(getProject(), restLabel);
        WizardOperator wo = new WizardOperator(restLabel);
        //have to wait until "retrieving message dissapers (see also issue 130835)
        new EventTool().waitNoEvent(2500);
        JListOperator availableEntities = new JListOperator(wo, 1);
        JListOperator selectedEntities = new JListOperator(wo, 2);

        //XXX - workaround for: http://www.netbeans.org/issues/show_bug.cgi?id=130835
        //Add All >>
        String addAllLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_AddAll");
        String removeAllLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RemoveAll");

        // wait till all classes are loaded into list
        new EventTool().waitNoEvent(2000);
        new JButtonOperator(wo, addAllLabel).push();
        // wait till all classes are moved from one list to another
        new EventTool().waitNoEvent(2000);
        //<< Remove All (see bug #202010)
        new JButtonOperator(wo, removeAllLabel).push();
        //XXX - end
        availableEntities.selectItem("Customer"); //NOI18N
        //Add >
        String addLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_Add");
        new JButtonOperator(wo, addLabel).push();
        availableEntities.selectItem("Product"); //NOI18N
        new JButtonOperator(wo, addLabel).push();
        assertEquals("add failed in selected", 5, selectedEntities.getModel().getSize()); //NOI18N
        assertEquals("add failed in available", 3, availableEntities.getModel().getSize()); //NOI18N
        selectedEntities.selectItem("Product"); //NOI18N
        //< Remove
        String removeLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_Remove");
        new JButtonOperator(wo, removeLabel).push();
        assertEquals("remove failed in selected", 2, selectedEntities.getModel().getSize()); //NOI18N
        assertEquals("remove failed in available", 6, availableEntities.getModel().getSize()); //NOI18N
        //<< Remove All
        new JButtonOperator(wo, "Remove All").push();
        assertEquals("remove all failed in selected", 0, selectedEntities.getModel().getSize()); //NOI18N
        assertEquals("remove all failed in available", 8, availableEntities.getModel().getSize()); //NOI18N
        availableEntities.selectItem("Person"); //NOI18N
        new JButtonOperator(wo, addLabel).push();
        assertEquals("add in selected", 1, selectedEntities.getModel().getSize()); //NOI18N
        assertEquals("add in available", 7, availableEntities.getModel().getSize()); //NOI18N
        wo.next();
        wo.finish();
        wo.waitClosed();
        new EventTool().waitNoEvent(1000);
        waitScanFinished();

        Set<File> files = getFilesFromCustomPkg("service", "entity"); //NOI18N
        if (getJavaEEversion().equals(JavaEEVersion.JAVAEE5)) {
            files.addAll(getFilesFromCustomPkg("controller", "controller.exceptions", "service", "entity")); //NOI18N
        }
        if (JavaEEVersion.JAVAEE6.equals(getJavaEEversion()) || JavaEEVersion.JAVAEE7.equals(getJavaEEversion())) {
            assertEquals("Some files were not generated", 3, files.size()); //NOI18N
        } else {
            assertEquals("Some files were not generated", 7, files.size()); //NOI18N
        }
        checkFiles(files);
        //make sure all REST services nodes are visible in project log. view
        waitRestNodeChildren(8);
    }

    public void testCreateRestClient() throws IOException {
        // not display browser on run
        // open project properties
        //Properties
        String propLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_Properties_Action");
        new ActionNoBlock(null, propLabel).performPopup(getProjectRootNode());
        new EventTool().waitEvent(2000);
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Run" category
        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
        String displayBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
        new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
        // confirm properties dialog
        propertiesDialogOper.ok();
        // "Test RESTful Web Services"
        String testRestActionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.projects.Bundle", "LBL_TestRestBeansAction_Name");
        Node n = getProjectType().isAntBasedProject() ? getProjectRootNode() : getRestNode();
        // "Configure RESR Test Client"
        n.performPopupActionNoBlock(testRestActionName);
        String testRestTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.support.Bundle", "TTL_SelectTarget");
        NbDialogOperator configureDialogOper = new NbDialogOperator(testRestTitle);
        if (getProjectType().isAntBasedProject()) {
            configureDialogOper.ok();
            OutputTabOperator oto = new OutputTabOperator(getProjectName());
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
            oto.waitText("(total time: "); //NOI18N
        } else {
            // cancel for Maven projects because otherwise it opens browser
            configureDialogOper.cancel();
        }
    }

    protected void createPU() {
        //Persistence
        String category = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.ui.resources.Bundle", "Templates/Persistence");
        //Persistence Unit
        String puLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.unit.Bundle", "Templates/Persistence/PersistenceUnit");
        createNewFile(getProject(), category, puLabel);
        String title = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.unit.Bundle", "LBL_NewPersistenceUnit");
        WizardOperator wo = new WizardOperator(title);
        new JTextFieldOperator(wo).setText(getProjectName().replace("Mvn", "") + "PU");
        new JComboBoxOperator(wo, 1).selectItem("jdbc/sample");
        wo.finish();
        new EventTool().waitEvent(2500);
        if (!getProjectType().isAntBasedProject()) {
            new Node(getProjectRootNode(), "Other Sources|src/main/resources|META-INF").expand();
            new EventTool().waitNoEvent(2500);
        }
    }

    protected void copyDBSchema() {
        //copy dbschema file to the project
        FileObject fo = FileUtil.toFileObject(new File(getRestDataDir(), "sampleDB.dbschema")); //NOI18N
        FileObject targetDir = getProject().getProjectDirectory().getFileObject("src");
        if (getProjectType().isAntBasedProject()) {
            targetDir = targetDir.getFileObject("conf"); //NOI18N
        } else {
            targetDir = targetDir.getFileObject("main/resources/META-INF"); //NOI18N
        }
        try {
            fo.copy(targetDir, fo.getName(), fo.getExt());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "db schema not copied", ex);
        }
    }

    /**
     * Go through given from DB wizard and return WizardOperator from the last
     * panel of the wizard
     *
     * @param wo wizard to go through
     * @return last step in the wizard
     */
    protected WizardOperator prepareEntityClasses(WizardOperator wo, boolean createPU) {
        //Add all >>
        String lbl = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_AddAll");
        JButtonOperator allLbl = new JButtonOperator(wo, lbl);
        // wait all classes are loaded
        new EventTool().waitNoEvent(2000);
        allLbl.pushNoBlock();
        // wait all classes are added
        new EventTool().waitNoEvent(2000);
        wo.next();
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 0);
        jcbo.clearText();
        jcbo.typeText(getRestPackage());
//        if (createPU) { // need to check if it's really necessary to create PU and if so this need to be done differently
//            //Create persistence unit
//            String btnLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.Bundle", "LBL_CreatePersistenceUnit");
//            new JButtonOperator(wo, btnLabel).pushNoBlock();
//            //Create Persistence Unit
//            String puDlgTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "LBL_CreatePersistenceUnit");
//            NbDialogOperator ndo = new NbDialogOperator(puDlgTitle);
//            //Create
//            btnLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.unit.Bundle", "LBL_Create");
//            new JButtonOperator(ndo, btnLabel).pushNoBlock();
//            //end create pu dialog
//        }
        return wo;
    }

    protected Set<File> getFiles(String pkg) {
        Set<File> files = new HashSet<File>();
        FileObject fo = getProjectSourceRoot().getFileObject(pkg.replace('.', '/') + "/"); //NOI18N
        if (fo != null) {
            File pkgRoot = FileUtil.toFile(fo);
            File[] filesAndFolders = pkgRoot.listFiles();
            if (filesAndFolders != null) {
                for (int q = 0; q < filesAndFolders.length; q++) {
                    if (!filesAndFolders[q].isDirectory()) {
                        files.add(filesAndFolders[q]);
                    }
                }
            }
        }
        return files;
    }

    protected Set<File> getFilesFromCustomPkg(String... pkg) {
        Set<File> files = new HashSet<File>();
        for (int i = 0; i < pkg.length; i++) {
            FileObject fo = getProjectSourceRoot().getFileObject(pkg[i].replace('.', '/') + "/"); //NOI18N
            File pkgRoot = FileUtil.toFile(fo);
            File[] filesAndFolders = pkgRoot.listFiles();
            if (filesAndFolders != null) {
                for (int q = 0; q < filesAndFolders.length; q++) {
                    if (!filesAndFolders[q].isDirectory()) {
                        files.add(filesAndFolders[q]);
                    }
                }
            }
        }
        return files;
    }

    /**
     * Creates suite from particular test cases. You can define order of
     * testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, CRUDTest.class,
                "testRfE", //NOI18N
                "testPropAccess", //NOI18N
                "testDeploy", //NOI18N
                "testCreateRestClient", //NOI18N
                "testUndeploy" //NOI18N
                ); //NOI18N
    }
}