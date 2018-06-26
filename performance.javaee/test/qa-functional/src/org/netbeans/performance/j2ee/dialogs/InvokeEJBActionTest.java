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
package org.netbeans.performance.j2ee.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of dialogs from EJB source editor.
 *
 * @author lmartinek@netbeans.org
 */
public class InvokeEJBActionTest extends PerformanceTestCase {

    private JPopupMenuOperator jmpo;
    private Node openFile;
    private String popupMenu = null;
    private String dialogTitle = null;

    /**
     * Creates a new instance of InvokeEJBActionTest
     * @param testName
     */
    public InvokeEJBActionTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }

    /**
     * Creates a new instance of InvokeEJBActionTest
     * @param testName
     * @param performanceDataName
     */
    public InvokeEJBActionTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(InvokeEJBActionTest.class).suite();
    }

    public void testAddBusinessMethodDialogInEJB() {
        popupMenu = "Add|Business Method";
        dialogTitle = "Business Method";
        doMeasurement();
    }

    public void testCreateMethodDialogInEJB() {
        popupMenu = "Add|Create Method";
        dialogTitle = "Create Method";
        doMeasurement();
    }

    public void testFinderMethodDialogInEJB() {
        popupMenu = "Add|Finder Method";
        dialogTitle = "Finder Method";
        doMeasurement();
    }

    public void testHomeMethodDialogInEJB() {
        popupMenu = "Add|Home Method";
        dialogTitle = "Home Method";
        doMeasurement();
    }

    public void testSelectMethodDialogInEJB() {
        popupMenu = "Add|Select Method";
        dialogTitle = "Select Method";
        doMeasurement();
    }

    @Override
    public void initialize() {
        openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Enterprise Beans|TestEntityEB");
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
    }

    public void prepare() {
        jmpo = openFile.callPopup();
        // do nothing
    }

    public ComponentOperator open() {
        jmpo.pushMenu(popupMenu);
        return new NbDialogOperator(dialogTitle);
    }

    @Override
    public void shutdown() {
    }
}
