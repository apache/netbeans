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
