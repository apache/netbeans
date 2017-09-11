/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
