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

package org.netbeans.performance.mobility.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.mobility.setup.MobilitySetup;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Close Project Property
 *
 * @author  rashid@netbeans.org
 */
public class CloseProjectPropertyTest extends PerformanceTestCase {

    private NbDialogOperator jdo ;
    private static String testProjectName = "MobileApplicationVisualMIDlet";  
    
    /**
     * Creates a new instance of CloseProjectProperty
     * @param testName the name of the test
     */
    public CloseProjectPropertyTest(String testName) {
        super(testName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN=2000;
    }
    
    /**
     * Creates a new instance of CloseProjectProperty
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseProjectPropertyTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN=2000;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(CloseProjectPropertyTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCloseProjectProperty() {
        doMeasurement();
    }

    @Override
    public void initialize(){
       repaintManager().addRegionFilter(repaintManager().IGNORE_STATUS_LINE_FILTER);
    }
    
    public void prepare(){
  
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        pNode.select();
        pNode.performPopupAction("Properties");
         
       jdo = new NbDialogOperator(testProjectName);
       JTreeOperator cattree = new JTreeOperator(jdo);       
       Node cNode = new Node(cattree,"Abilities") ;
       cNode.select();
        
       JButtonOperator addButton = new JButtonOperator(jdo,"Add");
       addButton.pushNoBlock();

       NbDialogOperator add_abil = new NbDialogOperator("Add Ability");
       JComboBoxOperator abilityCombo = new JComboBoxOperator(add_abil); 
       abilityCombo.clearText();
       abilityCombo.typeText("Ability_"+System.currentTimeMillis());
       JButtonOperator abil_okButton = new JButtonOperator(add_abil,"OK");
       abil_okButton.push();
       
    }
    
    public ComponentOperator open(){
       JButtonOperator okButton = new JButtonOperator(jdo,"OK");
       okButton.push();
       return null;
    }
    
    @Override
    public void close(){
    }

    public void shutdown() {
        repaintManager().resetRegionFilters();
    }

}
