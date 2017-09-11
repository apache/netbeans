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
