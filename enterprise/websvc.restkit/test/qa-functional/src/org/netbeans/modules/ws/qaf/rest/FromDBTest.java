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
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 * Tests for New REST web services from Database wizard
 *
 * @author lukas
 */
public class FromDBTest extends CRUDTest {

    protected static Server server = Server.GLASSFISH;

    public FromDBTest(String name) {
        super(name, server);
    }

    public FromDBTest(String name, Server server) {
        super(name, server);
    }

    @Override
    protected String getProjectName() {
        return "FromDB"; //NOI18N
    }

    @Override
    protected String getRestPackage() {
        return "o.n.m.ws.qaf.rest.fromdb"; //NOI18N
    }

    public void testFromDB() throws IOException {
        createPU();
        copyDBSchema();
        //RESTful Web Services from Database
        String restLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromDatabase");
        NewFileWizardOperator nfwo = createNewWSFile(getProject(), restLabel);
        //Entity Classes from Database
        WizardOperator wo = prepareEntityClasses(nfwo, false);
        wo.next();
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 1);
        jcbo.clearText();
        jcbo.typeText(getRestPackage() + ".service"); //NOI18N
        // sometimes Finish button not enabled
        new EventTool().waitNoEvent(1500);
        wo.btFinish().pushNoBlock();
        wo.waitClosed();
        String generationTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.persistence.wizard.fromdb.Bundle", "TXT_EntityClassesGeneration");
        waitDialogClosed(generationTitle, 180000); // wait 3 minutes
        new EventTool().waitNoEvent(1500);
        waitScanFinished();
        String packageName = getRestPackage() + ".service";
        Set<File> files = getFiles(packageName);
        if (!getJavaEEversion().equals(JavaEEVersion.JAVAEE5)) { // see http://netbeans.org/bugzilla/show_bug.cgi?id=189723
            assertEquals("Missing files in package " + packageName, 9, files.size()); //NOI18N
        } else {
            // Java EE 5 - see http://netbeans.org/bugzilla/show_bug.cgi?id=189723
            assertEquals("Missing files in package " + packageName, 8, files.size()); //NOI18N
            packageName = getRestPackage() + ".controller"; //NOI18N
            files = getFiles(packageName);
            assertEquals("Missing files in package " + packageName, 7, files.size()); //NOI18N
            packageName = getRestPackage() + ".controller.exceptions"; //NOI18N
            files = getFiles(packageName);
            assertEquals("Missing files in package " + packageName, 4, files.size()); //NOI18N
        }
        //make sure all REST services nodes are visible in project log. view
        waitRestNodeChildren(7);
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, FromDBTest.class,
                "testFromDB", //NOI18N
                "testDeploy", //NOI18N
                "testUndeploy"); //NOI18N
    }
}