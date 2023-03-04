/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
