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


import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;

/**
 * Measure Rename Class Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class RefactoringRename extends MemoryFootprintTestCase {
    
    private static final String rename = "jEdit";
    public static final String suiteName="J2SE Footprints suite";
    

    /**
     * Creates a new instance of RefactoringRename
     *
     * @param testName the name of the test
     */
    public RefactoringRename(String testName) {
        super(testName);
        prefix = "Refactoring Rename |";
    }
    
    /**
     * Creates a new instance of RefactoringRename
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public RefactoringRename(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Refactoring Rename |";
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
        String rename_to, rename_from = rename;
        
        // jEdit project
        log("Opening project jEdit");
        ProjectsTabOperator.invoke();
        CommonUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+ java.io.File.separator +"jEdit41");
        CommonUtilities.waitForPendingBackgroundTasks();
        
        // find existing node
        Node packagenode = new Node(new SourcePackagesNode("jEdit"), "org.gjt.sp.jedit");
        String[] children = packagenode.getChildren();
        for(int i=0; i<children.length; i++) {
            if(children[i].startsWith(rename)){
                rename_from = children[i];
                break;
            }
        }
        
        log(" Trying to rename file "+rename_from);
        
        //generate name for new node
/*        String number = "1";
        if(rename_from.equalsIgnoreCase(rename+".java")) {
            rename_to = rename + number;
        } else {
            number = rename_from.substring(rename.length(),rename_from.indexOf('.'));
            rename_to = rename + (Integer.parseInt(number) + 1);
        }
*/
	rename_to=rename+CommonUtilities.getTimeIndex();
        
        // invoke Rename
        Node filenode = new Node(packagenode, rename_from);
        filenode.callPopup().pushMenuNoBlock("Refactor|Rename..."); // NOI18N
        NbDialogOperator renamedialog = new NbDialogOperator("Rename "); // NOI18N
        JTextFieldOperator txtfNewName = new JTextFieldOperator(renamedialog);
        JButtonOperator btnRefactor = new JButtonOperator(renamedialog,"Refactor"); // NOI18N
        
        // if the project exists, try to generate new name
        rename_to = rename_to+"1";
        log("    ... rename to  " + rename_to);
        txtfNewName.clearText();
        txtfNewName.typeText(rename_to);
        
        new JCheckBoxOperator(renamedialog,"Apply Rename on Comments").changeSelection(true); // NOI18N
        btnRefactor.push();
        
        MainWindowOperator.getDefault().waitStatusText("Save All finished"); // NOI18N
        
        rename_to = rename_from;
	return null;
    }
    
    @Override
    public void close(){
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new RefactoringRename("measureMemoryFootprint"));
    }
    
}
