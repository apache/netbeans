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

package org.netbeans.performance.j2se.footprints;

import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Measure J2SE Project Workflow Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class J2SEProjectWorkflow extends MemoryFootprintTestCase {

    private String j2seproject;
    public static final String suiteName="J2SE Footprints suite";
    

    /**
     * Creates a new instance of J2SEProjectWorkflow
     * @param testName the name of the test
     */
    public J2SEProjectWorkflow(String testName) {
        super(testName);
        prefix = "J2SE Project Workflow |";
    }
    
    /**
     * Creates a new instance of J2SEProjectWorkflow
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public J2SEProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "J2SE Project Workflow |";
    }
    
    public void testMeasureMemoryFootprint() {
        super.testMeasureMemoryFootprint();
    }

    @Override
    public void setUp() {
        //do nothing
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
        // Create, edit, build and execute a sample J2SE project
        //j2seproject = CommonUtilities.createproject("Samples|Java", "Anagram Game", true);
        
        //CommonUtilities.openFile(j2seproject, "com.toy.anagrams.ui", "Anagrams.java", false);
        //CommonUtilities.editFile(j2seproject, "com.toy.anagrams.ui", "Anagrams.java");
        //CommonUtilities.buildProject(j2seproject);
        //runProject(j2seproject,true);
        //debugProject(j2seproject,true);
        //testProject(j2seproject);
        //collapseProject(j2seproject);
        
        return null;
    }
    
    @Override
    public void close(){
        CommonUtilities.deleteProject(j2seproject);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new J2SEProjectWorkflow("measureMemoryFooprint"));
    }
    
}
