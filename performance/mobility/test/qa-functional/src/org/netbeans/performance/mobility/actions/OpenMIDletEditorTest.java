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

package org.netbeans.performance.mobility.actions;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.mobility.setup.MobilitySetup;
import org.netbeans.performance.mobility.window.MIDletEditorOperator;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class OpenMIDletEditorTest extends PerformanceTestCase {

    private Node openNode;
    private String targetProject;
    private String midletName;
    private ProjectsTabOperator pto;
    public final static long EXPECTED_TIME = 10000;
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     */
    public OpenMIDletEditorTest(String testName) {
        super(testName);
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java";
        expectedTime = EXPECTED_TIME;
        WAIT_AFTER_OPEN = 20000;
    }

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenMIDletEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java";
        expectedTime = EXPECTED_TIME;
        WAIT_AFTER_OPEN = 20000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(OpenMIDletEditorTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testOpenMIDletEditor() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        pto = ProjectsTabOperator.invoke();
    }

    public void prepare() {
        String documentPath = CommonUtilities.SOURCE_PACKAGES + "|" + "allComponents" + "|" + midletName;
        long nodeTimeout = pto.getTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");

        try {
            openNode = new Node(pto.getProjectRootNode(targetProject), documentPath);
        } catch (TimeoutExpiredException ex) {
            pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", nodeTimeout);
            throw new Error("Cannot find expected node because of Timeout");
        }
        pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", nodeTimeout);

        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }
        openNode.select();
    }

    public ComponentOperator open() {
        JPopupMenuOperator popup = this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        }

        return MIDletEditorOperator.findMIDletEditorOperator(midletName);
    }

    @Override
    public void close() {
        if (testedComponentOperator != null) {
            new Thread("Question dialog discarder") {

                @Override
                public void run() {
                    try {
                        new JButtonOperator(new JDialogOperator("Question"), "Discard").push();
                    } catch (Exception e) {
                        //  There is no need to care about this exception as this dialog is optional
                        e.printStackTrace();
                    }
                }
            }.start();
            ((MIDletEditorOperator) testedComponentOperator).close();
        }
    }

}
