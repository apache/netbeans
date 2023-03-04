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

package org.netbeans.performance.enterprise.actions;

import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.performance.enterprise.XMLSchemaComponentOperator;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class SchemaViewSwitchTest extends PerformanceTestCase  {

    private static String category, project;
    private XMLSchemaComponentOperator schema;
    private static String testProjectName;
    private static String testSchemaName = "XMLTestSchema2";    
    
    /** Creates a new instance of SchemaViewSwitch */
    public SchemaViewSwitchTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;           
    }
    
    public SchemaViewSwitchTest(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;                
    }    

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(SchemaViewSwitchTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testSchemaViewSwitch() {
        doMeasurement();
    }

    @Override
    public void initialize(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.resources.Bundle","Templates/Project/SOA"); // "Service Oriented Architecture";
        project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.resources.Bundle","Templates/Project/SOA/emptyBpelpro.xml"); // "BPEL Module"
        testProjectName=CommonUtilities.createproject(category, project, true);
        addSchemaDoc(testProjectName,testSchemaName);
        String schemaDocPath = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources")+"|"+testSchemaName+".xsd";
        Node schemaNode = new Node(new ProjectsTabOperator().getProjectRootNode(testProjectName),schemaDocPath);
        schemaNode.performPopupActionNoBlock("Open");
        schema = XMLSchemaComponentOperator.findXMLSchemaComponentOperator(testSchemaName+".xsd");
    }

    private void addSchemaDoc( String projectName, String SchemaName) {
        Node pfn =  new Node(new ProjectsTabOperator().getProjectRootNode(projectName), org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources"));
        pfn.select();
        // Workaround for issue 143497
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        wizard.selectCategory("XML");
        wizard.selectFileType("XML Schema");
        wizard.next();
        NewJavaFileNameLocationStepOperator location = new NewJavaFileNameLocationStepOperator();
        location.setObjectName(SchemaName);
        location.finish();
    }    

    public void prepare() {
    }

    public ComponentOperator open() {
        schema.getDesignButton().pushNoBlock();
        return null;
    }

    @Override
    public void close(){
        schema.getSchemaButton().pushNoBlock();
    }

    @Override
    protected void shutdown() {
        new CloseAllDocumentsAction().performAPI();        
    }
   
}
