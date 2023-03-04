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
package org.netbeans.test.j2ee;

import junit.framework.Test;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.j2ee.addmethod.AddFinderMethodTest;
import org.netbeans.test.j2ee.addmethod.AddMethodTest;
import org.netbeans.test.j2ee.addmethod.AddSelectMethodTest;
import org.netbeans.test.j2ee.addmethod.CallEJBTest;
import org.netbeans.test.j2ee.addmethod.SendMessageTest;
import org.netbeans.test.j2ee.addmethod.UseDatabaseTest;
import org.netbeans.test.j2ee.cmp.GenerateEntityClassesTest;
import org.netbeans.test.j2ee.hints.EntityRelations;
import org.netbeans.test.j2ee.multiview.CMPRelationshipsTest;
import org.netbeans.test.j2ee.multiview.EBDetailsAndCMPFieldPanelTest;
import org.netbeans.test.j2ee.multiview.EBGeneralAndClassPanelTest;
import org.netbeans.test.j2ee.multiview.EjbModuleTest;
import org.netbeans.test.j2ee.multiview.PagesAndReferencesDDTest;
import org.netbeans.test.j2ee.multiview.SecurityDDTest;
import org.netbeans.test.j2ee.multiview.WebProjectDDTest;
import org.netbeans.test.j2ee.persistence.PersistenceUnitTest;
import org.netbeans.test.j2ee.wizard.MultiSrcRootModsWizardsTest;
import org.netbeans.test.j2ee.wizard.NewProjectWizardsTest;
import org.netbeans.test.j2ee.wizard.WizardsJavaEE5Test;
import org.netbeans.test.j2ee.wizard.WizardsJavaEE7Test;
import org.netbeans.test.j2ee.wizard.WizardsTest;

/**
 * Run all tests in the same instance of the IDE.
 *
 * @author Jiri Skrivanek
 */
public class J2EESuite extends J2eeTestCase {

    public J2EESuite(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        addServerTests(Server.GLASSFISH, conf);//register server
        // EJBValidation
        conf = conf.addTest(EJBValidation.class, "openProjects");
        conf = conf.addTest(AddMethodTest.class,
                "testAddBusinessMethod1InSB",
                "testAddBusinessMethod2InSB",
                "testAddBusinessMethod1InEB",
                "testAddBusinessMethod2InEB",
                "testAddCreateMethod1InEB",
                "testAddCreateMethod2InEB",
                "testAddHomeMethod1InEB",
                "testAddHomeMethod2InEB");
        conf = conf.addTest(AddFinderMethodTest.class,
                "testAddFinderMethod1InEB",
                "testAddFinderMethod2InEB");
        conf = conf.addTest(AddSelectMethodTest.class,
                "testAddSelectMethod1InEB",
                "testAddSelectMethod2InEB");
        conf = conf.addTest(CallEJBTest.class,
                "testCallEJBInServlet",
                "testCallEJB1InSB");
        //  "testCallEJB2InSB");  test needs to be fixed
        conf = conf.addTest(EJBValidation.class, "prepareDatabase");
        conf = conf.addTest(UseDatabaseTest.class, "testUseDatabase1InSB");
        conf = conf.addTest(SendMessageTest.class, "testSendMessage1InSB");
        conf = conf.addTest(EJBValidation.class,
                "testStartServer",
                "testDeployment",
                "testUndeploy",
                "testStopServer");
        conf = conf.addTest(EJBValidation.class, "closeProjects");
        // J2EEValidation
        conf = conf.addTest(J2EEValidation.class);
        // J2eeProjectsTest
        conf = conf.addTest(J2eeProjectsTest.class,
                "testCreateEjbProject",
                "testCreateWebProject",
                "testCreateEmptyJ2eeProject",
                "testAddModulesToJ2eeProject");
        // FreeFormProjects
        conf = conf.addTest(FreeFormProjects.class,
                "testEjbWithSources",
                "testEarWithSources");
        // cmp/GenerateEntityClassesTest
        conf = conf.addTest(GenerateEntityClassesTest.class,
                "testOpenProject",
                "testGenerateBeans");
        // hints/EntityRelations
        conf = conf.addTest(EntityRelations.class);
        // multiview/CMPRelationshipsTest
        conf = conf.addTest(CMPRelationshipsTest.class,
                "testOpenProject",
                "testAddRelationship",
                "testModifyRelationship",
                "testRemoveRelationship");
        // multiview/EBDetailsAndCMPFieldPanelTest
        conf = conf.addTest(EBDetailsAndCMPFieldPanelTest.class,
                "testOpenProject",
                "testEBName",
                "testDescription",
                "testSmallIcon",
                "testLargeIcon",
                "testRevertChanges");
        // multiview/EBGeneralAndClassPanelTest
        conf = conf.addTest(EBGeneralAndClassPanelTest.class,
                "testOpenProject",
                "testEntityNodeName",
                "testEntityName",
                "testEntityPersistanceType",
                "testEntityAbstractName",
                "testEntityPKField",
                "testEntityPKClass",
                "testChangeReentrant",
                "testBeanClassName",
                "testLocalHomeIName",
                "testLocalIName",
                // need to be fixed
                //"testRemoteIName",
                //"testChangePKMultiple",
                //"testChangePK",
                //"testLocalInterfaceCheckBox",
                //"testRemoteInterfaceCheckBox",
                //"testEnableRemoteI",
                //"testDisableRemoteI",
                "testRemoteHomeIName");
        // multiview/EjbModuleTest
        conf = conf.addTest(EjbModuleTest.class,
                "testOpenProject",
                "testRenameDisplayName",
                "testChangeDescription",
                "testAddSmallIcon",
                "testAddLargeIcon");
        // multiview/WebProjectDDTest
        conf = conf.addTest(WebProjectDDTest.class, WebProjectDDTest.webprojectddtests);
        conf = conf.addTest(PagesAndReferencesDDTest.class, WebProjectDDTest.pagesandreferencesddtests);
        conf = conf.addTest(SecurityDDTest.class, WebProjectDDTest.securityddtests);
        // persistence/PersistenceUnitTest
        conf = conf.addTest(PersistenceUnitTest.class,
                "testOpenProject",
                "testPUProviders",
                "testPUDataSource");
        // wizard/NewProjectWizardsTest
        conf = conf.addTest(NewProjectWizardsTest.NewProjectWizardsTest5.class);
        conf = conf.addTest(NewProjectWizardsTest.NewProjectWizardsTest6.class);
        conf = conf.addTest(NewProjectWizardsTest.NewProjectWizardsTest7.class);
        conf = conf.addTest(NewProjectWizardsTest.NewProjectWizardsTest5.class, "closeProjects");        
        // wizard/WizardsTest
        conf = conf.addTest(WizardsTest.Suite.class);
        // wizard/WizardsJavaEE5Test
        conf = conf.addTest(WizardsJavaEE5Test.Suite.class);
        // wizard/WizardsJavaEE7Test
        conf = conf.addTest(WizardsJavaEE7Test.Suite.class);
        // wizard/MultiSrcRootModsWizardsTest
        conf = conf.addTest(MultiSrcRootModsWizardsTest.Suite.class);
        return conf.suite();
    }
}
