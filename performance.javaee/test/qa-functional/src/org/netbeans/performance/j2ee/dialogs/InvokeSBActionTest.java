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

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of dialogs from EJB source editor.
 *
 * @author lmartinek@netbeans.org
 */
public class InvokeSBActionTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private JDialogOperator jdo;
    private Node openFile;
    private int listItem = 0;
    private String dialogTitle = null;

    /**
     * Creates a new instance of InvokeEJBActionTest
     * @param testName
     */
    public InvokeSBActionTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }

    /**
     * Creates a new instance of InvokeEJBActionTest
     * @param testName
     * @param performanceDataName
     */
    public InvokeSBActionTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(InvokeSBActionTest.class).suite();
    }

    public void testAddBusinessMethodDialogInSB() {
        dialogTitle = "Add Business Method";
        listItem = 0;
        doMeasurement();
    }

    public void testConstructorDialogInSB() {
        dialogTitle = "Generate Constructor";
        listItem = 1;
        doMeasurement();
    }

    public void testAddGetterSetterDialogInSB() {
        dialogTitle = "Generate Getters and Setters";
        listItem = 5;
        doMeasurement();
    }

    public void testEqualsAndHashDialogInSB() {
        dialogTitle = "Generate Equals";
        listItem = 6;
        doMeasurement();
    }

    public void testToStringDialogInSB() {
        dialogTitle = "Generate toString";
        listItem = 7;
        doMeasurement();
    }

    public void testDelegateDialogInSB() {
        dialogTitle = "Generate Delegate";
        listItem = 8;
        expectedTime = 1500;
        doMeasurement();
    }

    public void testOverrideDialogInSB() {
        dialogTitle = "Generate Override";
        listItem = 9;
        doMeasurement();
    }

    public void testAddPropertyDialogInSB() {
        dialogTitle = "Add Property";
        listItem = 10;
        doMeasurement();
    }

    public void testCallEnterpriseBeanDialogInSB() {
        dialogTitle = "Call Enterprise Bean";
        listItem = 11;
        doMeasurement();
    }

    public void testSendEmailDialogInSB() {
        dialogTitle = "Specify Mail Resource";
        listItem = 14;
        doMeasurement();
    }

    public void testCallWebServiceDialogInSB() {
        dialogTitle = "Select Operation";
        listItem = 15;
        doMeasurement();
    }

    public void testGenerateRESTDialogInSB() {
        dialogTitle = "Available REST";
        listItem = 16;
        doMeasurement();
    }

    public void initialize() {
        openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Source Packages|test|TestSessionBean");
        new OpenAction().performAPI(openFile);
        editor = new EditorOperator("TestSessionBean.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
    }

    public void prepare() {
        editor.setCaretPosition(105, 1);
        editor.pushKey(java.awt.event.KeyEvent.VK_INSERT, java.awt.event.KeyEvent.ALT_MASK);
        jdo = new JDialogOperator();
        JListOperator list = new JListOperator(jdo);
        list.setSelectedIndex(listItem);
    }

    public ComponentOperator open() {
        jdo.pushKey(KeyEvent.VK_ENTER);
        return null;
    }

    public void close() {
        new NbDialogOperator(dialogTitle).cancel();
    }

    public void shutdown() {
        editor.closeDiscard();
    }

}
