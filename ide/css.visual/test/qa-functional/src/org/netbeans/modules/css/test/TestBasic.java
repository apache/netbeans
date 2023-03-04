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
package org.netbeans.modules.css.test;

import java.io.File;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.css.test.operator.StyleRuleEditorOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class TestBasic extends CSSTest{
    private static final String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewFileWizard_Title");
    private static final String createRuleAction = Bundle.getString("org.netbeans.modules.css.actions.Bundle", "Create_Rule");
    
    public TestBasic(String name) {
        super(name);
    }

    public void testNewCSS() throws Exception {
        File projectFile = new File (getDataDir(), projectName);
        openProjects(projectFile.getAbsolutePath());
        System.out.println("running testNewCSS + " + projectFile);
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke(wizardTitle);
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Other");
        nfwo.selectFileType("Cascading Style Sheet");
        nfwo.next();
        WizardOperator newCSSFile = new WizardOperator("New Cascading Style Sheet");
        new JTextFieldOperator(newCSSFile, 0).setText(newFileName);//FileName
        new JTextFieldOperator(newCSSFile, 2).setText("web/css");//Folder
        newCSSFile.finish();
        String text = new EditorOperator(newFileName).getText();
        assertTrue(text.contains("root"));
        assertTrue(text.contains("display:"));
        assertTrue(text.contains("block"));
    }
    
    public void testAddRule() throws Exception{
        EditorOperator eop = openFile(newFileName);
        eop.setCaretPositionToLine(rootRuleLineNumber);
        AbstractButtonOperator abo = eop.getToolbarButton(createRuleAction);
        abo.push();
        StyleRuleEditorOperator styleOperator = new StyleRuleEditorOperator();
        styleOperator.selectHtmlElement("button");
        styleOperator.addRule();
        styleOperator.selectClass("caption", "first");
        styleOperator.addRule();
        styleOperator.selectElementID("33");
        styleOperator.addRule();
        assertEquals("button caption.first #33", styleOperator.getPreview());
        styleOperator.up("33");
        assertEquals("button #33 caption.first", styleOperator.getPreview());
        styleOperator.down("button");
        assertEquals("#33 button caption.first", styleOperator.getPreview());
        styleOperator.ok();
        assertTrue(eop.getText().contains("#33 button caption.first"));
    }
    
    public void testNavigator() throws Exception{
        String navigatorTestFile = "navigatorTest.css";
        openFile(newFileName);
        NavigatorOperator navigatorOperator = NavigatorOperator.invokeNavigator();
        assertNotNull(navigatorOperator);
        JTreeOperator treeOperator = navigatorOperator.getTree();
        Object root = treeOperator.getRoot();
        assertNotNull(root);
        assertEquals("NUMBER OF ROOT CHILD", 2, treeOperator.getChildCount(root));
        openFile(navigatorTestFile).setVerification(true);
        treeOperator = navigatorOperator.getTree();
        root = treeOperator.getRoot();
        assertNotNull(root);
        assertEquals("NUMBER OF ROOT CHILD", 2, treeOperator.getChildCount(root));
        Object firstChild = treeOperator.getChild(root, 0);
        assertEquals("NUMBER OF @MEDIA SCREEN CHILD", 2, treeOperator.getChildCount(firstChild));
        Object aChild = treeOperator.getChild(firstChild, 1);
        assertNotNull("A rule", aChild);
        TreePath path = new TreePath(new Object[]{root, firstChild, aChild});
        treeOperator.clickOnPath(path, 2);
        //        new EditorOperator(navigatorTestFile).
        JEditorPaneOperator editorPane = new EditorOperator(navigatorTestFile).txtEditorPane();
        assertEquals("CARET POSSITION ", 374, editorPane.getCaretPosition());
    }
    
}
