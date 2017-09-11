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

import java.io.IOException;
import org.openide.util.Exceptions;

import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;

import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Open BPEL project
 *
 * @author  rashid@netbeans.org
 */
public class OpenBPELprojectTest extends PerformanceTestCase {
    
    private static String projectName = "BPELTestProject";
    private JButtonOperator openButton;
    
    /**
     * Creates a new instance of OpenBPELproject
     * @param testName the name of the test
     */
    public OpenBPELprojectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of CreateProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenBPELprojectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }

    public void testOpenBPELproject() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(OpenBPELprojectTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize(){
        CommonUtilities.actionOnProject(projectName, "Close");
    }
    
    public void prepare(){
        new ActionNoBlock("File|Open Project...",null).perform(); //NOI18N
        WizardOperator opd = new WizardOperator("Open Project"); //NOI18N
        JTextComponentOperator path = new JTextComponentOperator(opd,1);
        openButton = new JButtonOperator(opd,"Open Project"); //NOI18N
        String paths = getDataDir() + java.io.File.separator + projectName;
        path.setText(paths);
    }
    
    public ComponentOperator open(){
        openButton.pushNoBlock();
        return null;
    }
    
    @Override
    public void close(){
        CommonUtilities.actionOnProject(projectName, "Close");
    }

    public void shutdown() {
        try {
            this.openDataProjects("BPELTestProject");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
