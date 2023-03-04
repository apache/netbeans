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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;

//import org.netbeans.junit.ide.ProjectSupport;


/**
 * Measure Find Usages Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class FindUsages extends MemoryFootprintTestCase {
    
    public static final String suiteName="J2SE Footprints suite";
    
    
    /**
     * Creates a new instance of FindUsages
     * @param testName the name of the test
     */
    public FindUsages(String testName) {
        super(testName);
        prefix = "Find Usages |";
    }
    
    /**
     * Creates a new instance of FindUsages
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public FindUsages(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Find Usages |";
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
        // jEdit project
        log("Opening project jEdit");
        CommonUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+ java.io.File.separator +"jEdit41");
        CommonUtilities.waitForPendingBackgroundTasks();
        
        // invoke Find Usages
        Node filenode = new Node(new SourcePackagesNode("jEdit"), "org.gjt.sp.jedit" + "|" + "jEdit.java");
        filenode.callPopup().pushMenuNoBlock("Find Usages"); // NOI18N
        
        NbDialogOperator findusagesdialog = new NbDialogOperator("Find Usages"); // NOI18N
        new JCheckBoxOperator(findusagesdialog,"Search in Comments").setSelected(true); // NOI18N
        new JButtonOperator(findusagesdialog,"Find").push(); // NOI18N
        
        return new TopComponentOperator("Usages"); // NOI18N
    }
    
    @Override
    public void close(){
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new FindUsages("measureMemoryFooprint"));
    }
    
}
