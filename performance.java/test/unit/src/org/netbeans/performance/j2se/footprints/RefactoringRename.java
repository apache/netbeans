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
