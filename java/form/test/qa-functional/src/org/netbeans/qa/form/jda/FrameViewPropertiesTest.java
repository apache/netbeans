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

import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * Testing properties of JDA FrameView node
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class FrameViewPropertiesTest extends ExtJellyTestCase {
    private String _frameViewName;
    private String _frameViewFileName;    
    private static String NONE_VALUE = "<none>";
    private static String TOOLBAR_NAME = "jToolBar1";
    
    /** Constructor required by JUnit */
    public FrameViewPropertiesTest(String testName) {
        super(testName);
        
        setTestProjectName("JDABasic"+ this.getTimeStamp()); // NOI18N        
        setTestPackageName(getTestProjectName().toLowerCase());
        
        _frameViewName = getTestProjectName() + "View";
        _frameViewFileName = _frameViewName + ".java";
    }
    
     @Override
    public void setUp(){
        
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(FrameViewPropertiesTest.class).addTest(
                "testCreation",
                "testGeneratedCode",
                "testProperties" 
                ).gui(true).clusters(".*").enableModules(".*"));
    }

    /** Creating JDA Basic project */
    public void testCreation() {
        createJDABasicProject();
    }

    
    //** Testing generated code  */
    public void testGeneratedCode() {
        FormDesignerOperator designer = new FormDesignerOperator(_frameViewFileName);
        
        findInCode("setComponent(mainPanel);", designer);
        findInCode("setMenuBar(menuBar);", designer);
        findInCode("setComponent(mainPanel);", designer);
        findInCode("setStatusBar(statusPanel);", designer);
    }        
    
    //** Testing properties of FrameView node */
    public void testProperties() throws InterruptedException {
        FormDesignerOperator designer = new FormDesignerOperator(_frameViewFileName);
        Thread.sleep(500);
        // nothing about toolbar in code
        missInCode("setToolBar("+TOOLBAR_NAME+");", designer);

        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node frameNode = new Node(inspector.treeComponents(), ""); // NOI18N
        Thread.sleep(500);
        // add new JToolBar component
        runPopupOverNode("Add From Palette|Swing Containers|Tool Bar", frameNode); // NOI18N
        
        Node frameView = new Node(frameNode.tree(), "[FrameView]");
        Thread.sleep(500);
        ActionNoBlock act = new ActionNoBlock(null, "Properties");  // NOI18N
        act.perform(frameView);

        // get and set value of property
        NbDialogOperator dialogOp = new NbDialogOperator("[FrameView]");  // NOI18N
        Property prop = new Property(new PropertySheetOperator(dialogOp), "toolBar");  // NOI18N
        Thread.sleep(500);
        // test NONE value
        assertEquals(prop.getValue(), NONE_VALUE);
        
        // set toolbar component
        prop.setValue(TOOLBAR_NAME);
        Thread.sleep(500);
        // close property dialog
        new JButtonOperator(dialogOp,"Close").push();  // NOI18N
        
        // test generated code
        findInCode("setToolBar("+TOOLBAR_NAME+");", designer);
        Thread.sleep(500);
        // get value of property again, test it and set NONE value
        act = new ActionNoBlock(null, "Properties");  // NOI18N
        act.perform(frameView);
        Thread.sleep(500);
        dialogOp = new NbDialogOperator("[FrameView]");  // NOI18N
        prop = new Property(new PropertySheetOperator(dialogOp), "toolBar");  // NOI18N
        Thread.sleep(500);
        assertEquals(prop.getValue(), TOOLBAR_NAME);
        prop.setValue(NONE_VALUE);
        
        // close property dialog
        new JButtonOperator(dialogOp,"Close").push();  // NOI18N
        Thread.sleep(500);
        // nothing about toolbat in code?
        missInCode("setToolBar("+TOOLBAR_NAME+");", designer);
        
        // just check property value
        act = new ActionNoBlock(null, "Properties");  // NOI18N
        act.perform(frameView);
        Thread.sleep(500);
        dialogOp = new NbDialogOperator("[FrameView]");  // NOI18N
        prop = new Property(new PropertySheetOperator(dialogOp), "toolBar");  // NOI18N
        Thread.sleep(500);
        // is selected NONE?
        assertEquals(prop.getValue(), NONE_VALUE);
        Thread.sleep(500);
        // close property dialog
        new JButtonOperator(dialogOp,"Close").push();  // NOI18N
    }
}
