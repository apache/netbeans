/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Oracle, Inc.
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
