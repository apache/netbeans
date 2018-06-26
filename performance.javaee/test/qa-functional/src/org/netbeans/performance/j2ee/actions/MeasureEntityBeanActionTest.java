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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test of finishing dialogs from EJB source editor.
 *
 * @author lmartinek@netbeans.org
 */
public class MeasureEntityBeanActionTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private static NbDialogOperator dialog;

    private String popup_menu;
    private String title;
    private String name;
    private Node beanNode;

    /**
     * Creates a new instance of MeasureEntityBeanActionTest
     *
     * @param testName
     */
    public MeasureEntityBeanActionTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of MeasureEntityBeanActionTest
     *
     * @param testName
     * @param performanceDataName
     */
    public MeasureEntityBeanActionTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(MeasureEntityBeanActionTest.class).suite();
    }

    public void testAddBusinessMethod() {
        WAIT_AFTER_OPEN = 2000;
        expectedTime = 2000;
        popup_menu = "Add|Business Method";
        title = "Business Method";
        name = "testBusinessMethod";
        doMeasurement();
    }

    public void testAddSelectMethod() {
        WAIT_AFTER_OPEN = 1000;
        popup_menu = "Add|Select Method";
        title = "Select Method";
        name = "ejbSelectByTest";
        doMeasurement();
    }

    @Override
    public void initialize() {
        // open a java file in the editor
        beanNode = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Enterprise Beans|TestEntityEB");
        final ActionNoBlock action = new ActionNoBlock(null, popup_menu);
        try {
            new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object param) {
                    return action.isEnabled(beanNode) ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return "wait menu is enabled";
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
        new OpenAction().performAPI(beanNode);
        editor = new EditorOperator("TestEntityBean.java");
    }

    @Override
    public void prepare() {
        new ActionNoBlock(null, popup_menu).perform(beanNode);
        dialog = new NbDialogOperator(title);
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        name += CommonUtilities.getTimeIndex();
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(name);
    }

    @Override
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
