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
package org.netbeans.qa.form.actions;

import java.io.IOException;
import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.properties.DimensionProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * This test should cover all actions of the Form module
 *
 * List of tested actions is included in the javadoc of each test case
 * 
 * @author Pavel Pribyl
 * @version 0.9 (not finished)
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class actionsTest extends ExtJellyTestCase {

    public String DATA_PROJECT_NAME = "SampleProject";
    public String PACKAGE_NAME = "data";
    public String PROJECT_NAME = "Java";
    private String FILE_NAME = "clear_JFrame";
    public String FRAME_ROOT = "[JFrame]";
    public String workdirpath;
    public Node formnode;
    private ProjectsTabOperator pto;
    private NavigatorOperator inspector;
    private PropertySheetOperator properties;
    ProjectRootNode prn;

    /** Constructor required by JUnit */
    public actionsTest(String name) {
        super(name);
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(actionsTest.class).addTest(
                "testDummy",
                "testDuplicate",
                "testEditContainer",
                "testResizing",
                "testBeans",
                "testManager").gui(true).enableModules(".*").clusters(".*"));
    }

    /** Called before every test case. */
    @Override
    public void setUp() throws IOException {
        openDataProjects(DATA_PROJECT_NAME);
        workdirpath = getWorkDir().getParentFile().getAbsolutePath();
        System.out.println("########  " + getName() + "  #######");

        pto = new ProjectsTabOperator();
        prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();


        formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + FILE_NAME);
        //formnode.select();


    }

    /** Called after every test case. */
    @Override
    public void tearDown() {
    }

    /**
     * Just a helper test to avoid failing wit "Menu pushing ..." in following tests
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    public void testDummy() throws InterruptedException, IOException {
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
    }

    ;

    /** Test case 1.
     * This test case verifies following Form Editor actions:<br />
     * <ul>
     * <li>org.netbeans.modules.form.actions.DuplicateAction</li>
     * <li>org.netbeans.modules.form.actions.SelectLayoutAction</li>
     * <li>org.netbeans.modules.form.actions.InspectorActions</li>
     * </ul>
     * THIS TEST CASE MUST BE RUN<br />
     * It place several components into the form, they are used in following tests too
     *
     */
    public void testDuplicate() throws InterruptedException, IOException {
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);

        Thread.sleep(1000);

        inspector = new NavigatorOperator();

        Node inspectorRootNode = new Node(inspector.getTree(), FRAME_ROOT);
        inspectorRootNode.select();
        inspectorRootNode.expand();


        new Action(null, "Add From Palette|Swing Containers|Panel").perform(inspectorRootNode);

        Node panelNode = new Node(inspectorRootNode, "jPanel1 [JPanel]");
        panelNode.select();

        Thread.sleep(1000);

        new Action(null, "Add From Palette|Swing Controls|Button").performPopup(panelNode);
        Action freedesignAction = new Action(null, "Set Layout|Free Design");

        freedesignAction.performPopup(panelNode);

        Node buttonNode = new Node(panelNode, "jButton1 [JButton]");
        buttonNode.select();
        new Action(null, "Duplicate").performPopup(buttonNode);

        inspectorRootNode.select();

        freedesignAction.performPopup(inspectorRootNode);

        panelNode.select();
        new Action(null, "Duplicate").performPopup(panelNode);

        Thread.sleep(1000);

        ArrayList lines = new ArrayList<String>();

        lines.add("jButton1");
        lines.add("jButton2");
        lines.add("jButton3");
        lines.add("jButton4");
        lines.add("jPanel1");
        lines.add("jPanel2");

        findInCode(lines, new FormDesignerOperator(FILE_NAME));
    }

    /** Test case 2.
     * This test case verifies following Form Editor actions:<br />
     * <ul>
     * <li>org.netbeans.modules.form.actions.EditContainerAction</li>
     * <li>org.netbeans.modules.form.actions.DesignParentAction</li>
     * <li>org.netbeans.modules.form.actions.PropertyAction (?)</li>
     * <li>org.netbeans.modules.form.actions.EditFormAction</li>
     * </ul>
     */
    public void testEditContainer() throws InterruptedException {
        formnode.select();
        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        Thread.sleep(2000);

        inspector = new NavigatorOperator();

        Thread.sleep(2000);
        Node inspectorRootNode = new Node(inspector.getTree(), FRAME_ROOT);
        Node panelNode = new Node(inspectorRootNode, "jPanel1 [JPanel]");
        panelNode.select();

        new Action(null, "Design This Container").performPopup(panelNode);


        FormDesignerOperator opDesigner = new FormDesignerOperator(FILE_NAME);
        opDesigner.source();
        opDesigner.design();

        formnode.select();
        openAction.perform(formnode);
        Thread.sleep(2000);

        inspector = new NavigatorOperator();

        Node inspectorRootNode1 = new Node(inspector.getTree(), FRAME_ROOT);
        Node panelNode1 = new Node(inspectorRootNode1, "jPanel1 [JPanel]");
        panelNode1.select();
        Node buttonNode1 = new Node(panelNode1, "jButton1 [JButton]");


        Thread.sleep(2000);

        new Action(null, "Design Parent|[Top Parent]").performPopup(buttonNode1);

        //Thread.sleep(2000);
        opDesigner.source();
        opDesigner.design();

        formnode.select();
        openAction.perform(formnode);
        Thread.sleep(2000);

        inspector = new NavigatorOperator();
        Node inspectorRootNode2 = new Node(inspector.getTree(), FRAME_ROOT);
        Node panelNode2 = new Node(inspectorRootNode2, "jPanel1 [JPanel]");
        panelNode2.select();
        Node buttonNode2 = new Node(panelNode2, "jButton1 [JButton]");
        new Action(null, "Enclose In|Scroll Pane").performPopup(buttonNode2);

        //Thread.sleep(1000);

        //buttonNode = new Node(panelNode, "jButton2 [JButton]");
        //buttonNode.setComparator(new Operator.DefaultStringComparator(true, false));
        //buttonNode.select();

        //Thread.sleep(1000);

        // buttonNode.select();
        //inspector.pressKey(KeyEvent.VK_ENTER);  

    }

    /** This test case verifies following Form Editor actions:<br />
     * <ul>
     * <li>org.netbeans.modules.form.actions.ChooseSameSizeAction</li>
     * <ul>
     */
    public void testResizing() throws InterruptedException {
        createForm("JFrame Form", "MyJFrame");

        ComponentInspectorOperator inspectorOp = new ComponentInspectorOperator();

        inspectorOp.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspectorOp = new ComponentInspectorOperator();
                Node inspectorRootNode = new Node(inspectorOp.getTree(), FRAME_ROOT);


                new Action(null, "Add From Palette|Swing Containers|Panel").performPopup(new Node(inspectorOp.getTree(), "[JFrame]"));

                Node panelNode = new Node(inspectorRootNode, "jPanel1 [JPanel]");
                panelNode.select();

                new Action(null, "Add From Palette|Swing Controls|Button").performPopup(panelNode);

                new Action(null, "Add From Palette|Swing Controls|Button").performPopup(panelNode);

                Node btn1Node = new Node(panelNode, "jButton1 [JButton]");
                btn1Node.select();
            }
        });


        properties = new PropertySheetOperator();
        FormDesignerOperator opDesigner = new FormDesignerOperator("MyJFrame");
        selectPropertiesTab(properties);
        new DimensionProperty(properties, "preferredSize").setValue("100,50");

        inspectorOp.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                
                ComponentInspectorOperator inspectorOp = new ComponentInspectorOperator();
                Node inspectorRootNode = new Node(inspectorOp.getTree(), FRAME_ROOT);
                Node panelNode = new Node(inspectorRootNode, "jPanel1 [JPanel]");
                Node btn2Node = new Node(panelNode, "jButton2 [JButton]");
                Node btn1Node = new Node(panelNode, "jButton1 [JButton]");
                btn1Node.select();
                btn2Node.addSelectionPath();

                

                btn1Node = new Node(panelNode, "jButton1 [JButton]");
                btn2Node = new Node(panelNode, "jButton2 [JButton]");
                Node[] nodes = {btn1Node, btn2Node};

                new Action(null, "Same Size|Same Width").performPopup(nodes);
            }
        });


        //verify, that source code contains the "grouping" of two JButtons
        String line = "new java.awt.Component[] {jButton1, jButton2}";


        findInCode(line, opDesigner);

    }

    /** Test case 5
     * org.netbeans.modules.form.actions.InstallToPaletteAction
     */
    public void testBeans() throws InterruptedException {
        String beanName = "MyBean";

        createForm("Bean Form", beanName);

        Thread.sleep(3000);

        inspector = new ComponentInspectorOperator();
        Node beanNode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + beanName);
        beanNode.select();
        new ActionNoBlock(null, "Tools|Add to Palette...").perform(beanNode);

        Thread.sleep(2000);

        NbDialogOperator jdo = new NbDialogOperator("Select Palette Category");
        JListOperator jlo = new JListOperator(jdo);
        jlo.selectItem("Beans");
        jdo.btOK().push();
        Thread.sleep(3000);
    }

    /** Test case 6
     * org.netbeans.modules.form.actions.InstallBeanAction
     */
    public void testManager() throws InterruptedException {
        //new ActionNoBlock("Tools|Palette|Swing/AWT Components", null).performMenu();
        Action ac = new Action("Tools|Palette|Swing/AWT Components", null);
        ac.setComparator(new DefaultStringComparator(true, true));
        ac.perform();


        NbDialogOperator nbo = new NbDialogOperator("Palette Manager");
        nbo.btClose().push();
        Thread.sleep(3000);
    }

    private void createForm(String formType, String name) throws InterruptedException {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(DATA_PROJECT_NAME);
        Thread.sleep(3000);
        nfwo.selectCategory("Swing GUI Forms");
        nfwo.selectFileType(formType);
        nfwo.next();
        JTextFieldOperator form_name = new JTextFieldOperator(nfwo, 0);
        form_name.setText(name);
        JComboBoxOperator jcb_package = new JComboBoxOperator(nfwo, 1);
        jcb_package.selectItem("data");
        Thread.sleep(3000);

        if (formType.equals("Bean Form")) {
            nfwo.next();
            JTextFieldOperator class_name = new JTextFieldOperator(nfwo);
            class_name.setText("javax.swing.JButton");
            nfwo.finish();
            log(formType + " is created correctly");
        } else {
            nfwo.finish();
            log(formType + " is created correctly");
            Thread.sleep(3000);
        }

    }

    /*
     * select tab in PropertySheet
     */
    private void selectPropertiesTab(PropertySheetOperator pso) {
        selectTab(pso, 0);
    }

    private void selectBindTab(PropertySheetOperator pso) {
        selectTab(pso, 1);
    }

    private void selectEventsTab(PropertySheetOperator pso) {
        selectTab(pso, 2);
    }

    private void selectCodeTab(PropertySheetOperator pso) {
        selectTab(pso, 3);
    }

    //select tab in PropertySheet
    private void selectTab(PropertySheetOperator pso, int index) {
        JToggleButtonOperator tbo = null;
        if (tbo == null) {
            tbo = new JToggleButtonOperator(pso, " ", index);
        }
        tbo.push();
    }
}