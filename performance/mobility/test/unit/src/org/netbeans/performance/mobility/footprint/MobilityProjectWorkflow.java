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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
