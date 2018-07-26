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

package org.netbeans.modules.groovy.qaf;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests for Grails specific new file wizards
 *
 * @author lukas
 */
public class GrailsWizardsTest extends GrailsTestCase {

    public GrailsWizardsTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return "GrailsWizards"; //NOI18N
    }

    /**
     * Test create new GSP file
     */
    public void testGSPFile() {
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.gsp.resources.Bundle", "Templates/Groovy/_view.gsp");
        createNewGroovyFile(getProject(), label);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName("MyGSP"); //NOI18N
        op.finish();
    }

    /**
     * Test create new Domain class
     */
    public void testDomainClass() {
        createNewGrailsFile(getProject(), "Templates/Groovy/DomainClass.groovy", "MyDomainClass"); //NOI18N
        EditorOperator eo = new EditorOperator("MyDomainClass.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Controller
     */
    public void testController() {
        createNewGrailsFile(getProject(), "Templates/Groovy/Controller.groovy", "MyController"); //NOI18N
        EditorOperator eo = new EditorOperator("MyControllerController.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Gant script
     */
    public void testGantScript() {
        createNewGrailsFile(getProject(), "Templates/Groovy/GantScript.groovy", "MyGantScript"); //NOI18N
        EditorOperator eo = new EditorOperator("MyGantScript.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Service
     */
    public void testService() {
        createNewGrailsFile(getProject(), "Templates/Groovy/Service.groovy", "MyService"); //NOI18N
        EditorOperator eo = new EditorOperator("MyServiceService.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Tag Library
     */
    public void testTagLib() {
        createNewGrailsFile(getProject(), "Templates/Groovy/TagLib.groovy", "MyTagLib"); //NOI18N
        EditorOperator eo = new EditorOperator("MyTagLibTagLib.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Unit test
     */
    public void testUnitTest() {
        createNewGrailsFile(getProject(), "Templates/Groovy/UnitTest.groovy", "MyUnitTest"); //NOI18N
        EditorOperator eo = new EditorOperator("MyUnitTestTests.groovy"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Integration test
     */
    public void testIntegrationTest() {
        createNewGrailsFile(getProject(), "Templates/Groovy/IntegrationTest.groovy", "MyIntegrationTest"); //NOI18N
        EditorOperator eo = new EditorOperator("MyIntegrationTestTests.groovy"); //NOI18N
        assertNotNull(eo);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GrailsWizardsTest.class)
                .enableModules(".*").clusters(".*")); //NOI18N
    }

}
