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

package org.netbeans.performance.mobility.footprint;

import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;

/**
 * Measure Mobility Project Workflow Memory footprint
 *
 * @author  mmirilovic@netbeans.org
 */
public class MobilityProjectWorkflow extends MemoryFootprintTestCase {
    
    private String projectName;
            
    /**
     * Creates a new instance of MobilityProjectWorkflow
     *
     * @param testName the name of the test
     */
    public MobilityProjectWorkflow(String testName) {
        super(testName);
        prefix = "Mobility Project Workflow |";
    }
    
    /**
     * Creates a new instance of MobilityProjectWorkflow
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public MobilityProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Mobility Project Workflow |";
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
        CommonUtilities.closeAllDocuments();
        CommonUtilities.closeMemoryToolbar();
    }
    
    public ComponentOperator open(){
//        projectName = CommonUtilities.createproject("Java ME", "Mobile Application", true); //NOI18N
        log("Created project name: "+projectName);
        // get opened editor
        Operator.StringComparator defaultOperator = Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        TopComponentOperator midletEditor = new TopComponentOperator("HelloMIDlet.java");
        Operator.setDefaultStringComparator(defaultOperator);
            
        // switch to Screen Design
        new JToggleButtonOperator(midletEditor, "Screen").pushNoBlock(); //NOI18N
        new EventTool().waitNoEvent(1500);
        
        // switch to Source
        new JToggleButtonOperator(midletEditor, "Source").pushNoBlock(); //NOI18N
        new EventTool().waitNoEvent(3000);
        
        // switch to Screen Design
        new JToggleButtonOperator(midletEditor, "Flow").pushNoBlock(); //NOI18N
        new EventTool().waitNoEvent(1500);
        
//        CommonUtilities.buildProject(projectName);
        
        return null;
    }
    
    @Override
    public void close(){
        log("Deleting project: "+projectName);
        CommonUtilities.deleteProject(projectName);
        log("Deleted...");
    }
    
//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(new MobilityProjectWorkflow("measureMemoryFooprint"));
//    }
    
}
