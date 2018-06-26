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
package org.netbeans.performance.j2ee.actions;

import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2ee.setup.J2EESetup;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test of finishing dialogs from EJB source editor.
 *
 * @author lmartinek@netbeans.org
 */
public class MeasureSessionBeanActionTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private static NbDialogOperator dialog;
    private String name;

    /**
     * Creates a new instance of MeasureSessionBeanActionTest
     *
     * @param testName
     */
    public MeasureSessionBeanActionTest(String testName) {
        super(testName);
        expectedTime = 2000;
    }

    /**
     * Creates a new instance of MeasureSessionBeanActionTest
     *
     * @param testName
     * @param performanceDataName
     */
    public MeasureSessionBeanActionTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(MeasureSessionBeanActionTest.class).suite();
    }

    public void testAddBusinessMethod() {
        WAIT_AFTER_OPEN = 1000;
        doMeasurement();
    }

    @Override
    public void initialize() {
        // open a java file in the editor
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Enterprise Beans|TestSessionSB");
        new OpenAction().performAPI(openFile);
        editor = new EditorOperator("TestSessionBean.java");
    }

    public void prepare() {
        Node beanNode = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Enterprise Beans|TestSessionSB");
        new ActionNoBlock(null, "Add|Business Method...").perform(beanNode);
        dialog = new NbDialogOperator("Business Method...");
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        name = "testBusinessMethod" + CommonUtilities.getTimeIndex();
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(name);
    }

    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        dialog.ok();
        editor.txtEditorPane().waitText(name);
        return null;
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        editor.closeDiscard();
    }
}
