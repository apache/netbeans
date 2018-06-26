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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.test.j2ee.addmethod;

import java.io.IOException;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.j2ee.EJBValidation;

/**
 *  Called from EJBValidation test suite.
 * 
 * @author Libor Martinek
 */
public class AddFinderMethodTest extends AddMethodTest {

    private boolean returnManyCardinality = true;
    protected String ejbql = null;
    private String toSearchFile;

    /** Creates a new instance of AddMethodTest */
    public AddFinderMethodTest(String name) {
        super(name);
    }

    public void testAddFinderMethod1InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddFinderMethodAction");
        methodName = "findByTest1";
        returnManyCardinality = true;
        parameters = null;
        remote = Boolean.FALSE;
        local = Boolean.TRUE;
        ejbql = null;
        toSearchFile = beanName + "LocalHome.java";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    public void testAddFinderMethod2InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddFinderMethodAction");
        methodName = "findByTest3";
        returnManyCardinality = false;
        parameters = new String[][]{{"java.lang.String", "a"}};
        remote = Boolean.TRUE;
        local = Boolean.TRUE;
        ejbql = "SELECT OBJECT(o)\nFROM TestingEntity o\nWHERE o.key = ?1";
        toSearchFile = beanName + "LocalHome.java";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    @Override
    protected void addMethod() throws IOException {
        EditorOperator editor = EditorWindowOperator.getEditor(beanName + "Bean.java");
        editor.select(11);

        // invoke Add Business Method dialog
        GenerateCodeOperator.openDialog(dialogTitle, editor);
        NbDialogOperator dialog = new NbDialogOperator(dialogTitle);
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(methodName);
        if (returnManyCardinality) {
            new JRadioButtonOperator(dialog, "Many").setSelected(true);
        } else {
            new JRadioButtonOperator(dialog, "One").setSelected(true);
        }
        fillParameters(dialog);
        setRemoteLocalCheckBox(dialog);
        if (ejbql != null) {
            lblOper = new JLabelOperator(dialog, "EJB QL:");
            new JTextAreaOperator((JTextArea)lblOper.getLabelFor()).setText(ejbql);
            //new JTextAreaOperator(dialog).setText(ejbql);
        }
        dialog.ok();
        if (toSearchFile != null) {
            Node openFile2 = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                    "Source Packages|test|" + toSearchFile);
            new OpenAction().performAPI(openFile2);
            final EditorOperator editor2 = EditorWindowOperator.getEditor(toSearchFile);
            editor2.txtEditorPane().waitText(methodName);
            // need to wait because sometimes is save() called sooner than it can take effect
            new EventTool().waitNoEvent(300);
            editor2.save();
            editor2.closeDiscard();
        }
        if (saveFile) {
            editor.save();
        }
        compareFiles();
    }
}
