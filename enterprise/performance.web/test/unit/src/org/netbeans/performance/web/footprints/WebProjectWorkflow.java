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

package org.netbeans.performance.web.footprints;


import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;

/**
 * Measure Web Project Workflow Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class WebProjectWorkflow extends MemoryFootprintTestCase {

    private String webproject;

    public static final String suiteName="Web Footprints suite";
    
    /**
     * Creates a new instance of WebProjectWorkflow
     * @param testName the name of the test
     */
    public WebProjectWorkflow(String testName) {
        super(testName);
        prefix = "Web Project Workflow |";
    }

    /**
     * Creates a new instance of WebProjectWorkflow
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public WebProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Web Project Workflow |";
    }
    
    public void testMeasureMemoryFootprint() {
        super.testMeasureMemoryFootprint();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        CommonUtilities.closeAllDocuments();
        CommonUtilities.closeMemoryToolbar();
    }
    
    @Override
    public void setUp() {
        //do nothing
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open(){
        // Web project
        //webproject = CommonUtilities.createproject("Samples|Web", "Tomcat Servlet Example", false);
        
        //CommonUtilities.openFile(webproject, "<default package>", "SessionExample.java", true);
        //CommonUtilities.buildProject(webproject);
        //CommonUtilities.deployProject(webproject);
        //CommonUtilities.collapseProject(webproject);
        
        return null;
    }
    
    @Override
    public void close(){
        CommonUtilities.deleteProject(webproject);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new WebProjectWorkflow("measureMemoryFooprint"));
    }
    
}
