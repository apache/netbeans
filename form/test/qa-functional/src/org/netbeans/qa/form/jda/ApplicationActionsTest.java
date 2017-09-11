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
package org.netbeans.qa.form.jda;

import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * Testing properties of JDA FrameView node
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 NOT WORKS NOW
 */
public class ApplicationActionsTest extends ExtJellyTestCase {
    private static String FOO_SIMPLEMETHOD = "FooSimpleMethod";
    private static String FOO_METHOD = "FooMethod";
    private static String FOO_TEXT = "Foo Text";
    private static String FOO_TOOLTIP = "Foo ToolTip";
    private static String FOO_LETTER = "F";
    private static String FOO_ENABLEDPROPTEXT = "fooEnabledProp";
    private static String FOO_SELECTEDPROPTEXT = "fooSelectedProp";        
    
    /** Constructor required by JUnit */
    public ApplicationActionsTest(String testName) {
        super(testName);
        
        setTestProjectName("JDABasic"+ this.getTimeStamp()); // NOI18N        
        setTestPackageName(getTestProjectName().toLowerCase());
        
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(ApplicationActionsTest.class).addTest(
                "testCreateJDAProject",
                "testInvokeWindow",
                "testCreateNewSimpleAction",
                "testCreateNewComplexAction",
                "testGeneratedCodeAndProperties").gui(true).clusters(".*").enableModules(".*"));
    }

    /** Creating JDA Basic project */
    public void testCreateJDAProject() {
        createJDABasicProject();
    }
    
    //** Testing generated code  */
    public void testInvokeWindow() {
        new Action("Window|Other|Application Actions",null).perform();  // NOI18N
        waitAMoment();

        // invoke edit dialog for first action in table
        JTableOperator tableOp = new JTableOperator(getTopComponent());
        tableOp.clickOnCell(1, 1);   // select first row in table

        // invoke edit dialog
        new JButtonOperator(getTopComponent(), "Edit Action").pushNoBlock();  // NOI18N
        waitAMoment();

        
        // closing edit dialog
        new JButtonOperator(new NbDialogOperator("Edit Action Properties"), "OK").pushNoBlock();  // NOI18N
    }        

    //** Testing properties of FrameView node */
    public void testCreateNewSimpleAction() {
        new JButtonOperator(getTopComponent(), "New Action").pushNoBlock();  // NOI18N
        waitAMoment();
        
        CreateNewActionOperator createOp = new CreateNewActionOperator();
        createOp.setMethodName(FOO_SIMPLEMETHOD);

        createOp.selectNode("Source Packages|" + getTestPackageName()
                + "|" + getTestProjectName() + "View.java");  // NOI18N
        
        createOp.ok();        
    }

    //** Testing properties of FrameView node */
    public void testCreateNewComplexAction() {
        new JButtonOperator(getTopComponent(), "New Action").pushNoBlock();  // NOI18N
        waitAMoment();
        
        CreateNewActionOperator createOp = new CreateNewActionOperator();
        createOp.setMethodName(FOO_METHOD);
        createOp.setText(FOO_TEXT);
        createOp.setToolTip(FOO_TOOLTIP);

        createOp.typeLetter(FOO_LETTER);
        createOp.checkAlt(true);
        createOp.checkShift(true);
        createOp.checkCtrl(true);
        createOp.checkMetaMacOnly(true);
        
        createOp.setEnabledPropertyText(FOO_ENABLEDPROPTEXT);
        createOp.setSelectedPropertyText(FOO_SELECTEDPROPTEXT);
        
        createOp.selectNode("Source Packages|" + getTestPackageName()
                + "|" + getTestProjectName() + "View.java");  // NOI18N

        createOp.setSmallIcon();
        NbDialogOperator iconOp = new NbDialogOperator("Select Icon");  // NOI18N
        new JComboBoxOperator(iconOp, 0).selectItem(1);
        new JButtonOperator(iconOp, "OK").push();  // NOI18N

        createOp.setLargeIcon();
        iconOp = new NbDialogOperator("Select Icon");  // NOI18N
        new JComboBoxOperator(iconOp, 0).selectItem(2);
        new JButtonOperator(iconOp, "OK").push();  // NOI18N
        
        createOp.ok();
    }

    public void testGeneratedCodeAndProperties() {
        FormDesignerOperator designer = new FormDesignerOperator(getTestProjectName() + "View.java");  // NOI18N
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add("public void FooSimpleMethod() {");  // NOI18N        
        lines.add("@Action(enabledProperty = \"fooEnabledProp\", selectedProperty = \"fooSelectedProp\")"); // NOI18N
        lines.add("public void FooMethod() {");  // NOI18N
        lines.add("private boolean fooEnabledProp = false;");  // NOI18N
        lines.add("public boolean isFooEnabledProp() {");  // NOI18N
        lines.add("public void setFooEnabledProp(boolean b) {");  // NOI18N
        lines.add("firePropertyChange(\"fooEnabledProp\", old, isFooEnabledProp());");  // NOI18N
        lines.add("private boolean fooSelectedProp = false;");  // NOI18N
        lines.add("public boolean isFooSelectedProp() {");  // NOI18N
        lines.add("public void setFooSelectedProp(boolean b) {");  // NOI18N
        lines.add("firePropertyChange(\"fooSelectedProp\", old, isFooSelectedProp());");  // NOI18N
        
        findInCode(lines, designer);
        
        ProjectRootNode prn = new ProjectsTabOperator().getProjectRootNode(getTestProjectName());
        prn.select();

        String nodePath = "Source Packages|" + getTestPackageName() + ".resources|"
                + getTestProjectName() + "View.properties"; // NOI18N
        Node propNode = new Node(prn, nodePath);
        runPopupOverNode("Edit", propNode);  // NOI18N

        EditorOperator editorOp = new EditorOperator(getTestProjectName() + "View");  // NOI18N
        String fileContent = editorOp.getText();
        
        lines = new ArrayList<String>();
        lines.add("FooSimpleMethod.Action.text=");  // NOI18N        
        lines.add("FooSimpleMethod.Action.shortDescription=");  // NOI18N        
        lines.add("FooMethod.Action.text=Foo Text");  // NOI18N        
        lines.add("FooMethod.Action.accelerator=shift ctrl meta alt pressed F");  // NOI18N        
        lines.add("FooMethod.Action.largeIcon=splash.png");  // NOI18N        
        lines.add("FooMethod.Action.smallIcon=about.png");  // NOI18N        
        lines.add("FooMethod.Action.icon=about.png");  // NOI18N        
        lines.add("FooMethod.Action.shortDescription=Foo ToolTip");  // NOI18N        
        
        for (String line : lines) {
            String msg = "Line \"" + line + "\" not found in "+getTestProjectName() + "View.properties file.";   // NOI18N            
            assertTrue(msg, fileContent.contains(line));
        }        
    }
    
    private TopComponentOperator getTopComponent() {
        return new TopComponentOperator("Application Actions");  // NOI18N
    }
}
