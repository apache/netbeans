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

package org.netbeans.performance.enterprise.footprint;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;


/**
 * Measure J2EE Project Workflow Memory footprint
 *
 * @author  mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class J2EEProjectWorkflow extends MemoryFootprintTestCase {
    
    private String j2eeproject, j2eeproject_ejb, j2eeproject_war, j2eeproject_app;
    
    /**
     * Creates a new instance of J2EEProjectWorkflow
     *
     * @param testName the name of the test
     */
    public J2EEProjectWorkflow(String testName) {
        super(testName);
        prefix = "J2EE Project Workflow |";
    }
    
    /**
     * Creates a new instance of J2EEProjectWorkflow
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public J2EEProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "J2EE Project Workflow |";
    }
    
    @Override
    public void setUp() {
        // do nothing
    }
    
    public void prepare() {
    }
    
    @Override
    public void initialize() {
        super.initialize();
        EPFootprintUtilities.closeAllDocuments();
        EPFootprintUtilities.closeMemoryToolbar();
    }
    
    public ComponentOperator open(){
        // Create, edit, build and execute a sample J2EE project
        // Create, edit, build and execute a sample J2EE project
        j2eeproject = EPFootprintUtilities.creatJ2EEeproject("Enterprise", "Enterprise Application", true);  // NOI18N
        j2eeproject_ejb = j2eeproject + "-ejb";
        j2eeproject_war = j2eeproject + "-war";
        j2eeproject_app = j2eeproject + "-app-client";
        
        //EPFootprintUtilities.openFile(new Node(new ProjectsTabOperator().getProjectRootNode(j2eeproject_war), EPFootprintUtilities.WEB_PAGES + "|index.jsp"),"index.jsp", true);
        EPFootprintUtilities.insertToFile("index.jsp", 11, "Hello World", true);
        
        new EditAction().perform(new Node(new ProjectsTabOperator().getProjectRootNode(j2eeproject_war), "Configuration Files|sun-web.xml")); // NOI18N
        TopComponentOperator xmlEditor = new TopComponentOperator("sun-web.xml");

        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "    <property name=\"javaEncoding\" value=\"UTF8\">", true);
        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "      <description>Encoding for generated Java servlet.</description>", true);
        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "    </property>", true);

       
        if (xmlEditor.isModified() )
            xmlEditor.save();
        
        Node node = new Node(new SourcePackagesNode(j2eeproject_app), new SourcePackagesNode(j2eeproject_app).getChildren()[0]+"|Main.java" );
        EPFootprintUtilities.openFile(node,"Main.java",true);
        EPFootprintUtilities.insertToFile("Main.java", 19, "System.out.println(\"Hello World\");",true);
        
        new SaveAllAction().performAPI();
        
        EPFootprintUtilities.buildProject(j2eeproject);
        //runProject(j2seproject,true);
        //debugProject(j2seproject,true);
        //testProject(j2seproject);
        //collapseProject(j2seproject);
        
        return null;
    }
    
    @Override
    public void close(){
        if (j2eeproject != null) {
            EPFootprintUtilities.deleteProject(j2eeproject);
            EPFootprintUtilities.deleteProject(j2eeproject_war);
            EPFootprintUtilities.deleteProject(j2eeproject_ejb);
            EPFootprintUtilities.deleteProject(j2eeproject_app,false);
        }
    }
    
//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(new J2EEProjectWorkflow("measureMemoryFooprint"));
//    }
    
}
