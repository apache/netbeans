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
package org.netbeans.test.j2ee.addmethod;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.j2ee.*;
import org.netbeans.test.j2ee.lib.Utils;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jemmy.EventTool;

/**
 *  Called from EJBValidation test suite.
 * 
 * @author Libor Martinek
 */
public class CallEJBTest extends AddMethodBase {

    private String calledBean;
    private boolean referencedLocal = true;
    private boolean convertExceptions = true;
    private String referenceName;

    /** Creates a new instance of AddMethodTest */
    public CallEJBTest(String name) {
        super(name);
    }

    public void testCallEJB1InSB() throws IOException {
        beanName = "TestingSession";
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle", "LBL_CallEjbAction");
        calledBean = EJBValidation.EJB_PROJECT_NAME + "|TestingEntity";
        toSearchInEditor = "TestingEntityLocalHome lookupTestingEntityBeanLocal()";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    public void testCallEJB2InSB() throws IOException {
        beanName = "TestingSession";
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle", "LBL_CallEjbAction");
        calledBean = EJBValidation.EJB_PROJECT_NAME + "|TestingEntity";
        toSearchInEditor = "TestingEntityRemoteHome lookupMyTestingEntityBean()";
        referencedLocal = false;
        convertExceptions = true;
        referenceName = "ejb/MyTestingEntityBean";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    public void testCallEJBInServlet() throws IOException {
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle", "LBL_CallEjbAction");
        calledBean = EJBValidation.EJB_PROJECT_NAME + "|TestingSession";
        toSearchInEditor = "TestingSessionLocal lookupTestingSessionBeanLocal()";
        referencedLocal = true;
        isDDModified = true;
        saveFile = true;

        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.WEB_PROJECT_NAME),
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_Sources")
                + "|test|TestingServlet.java");
        new OpenAction().performAPI(openFile);
        EditorOperator editor = EditorWindowOperator.getEditor("TestingServlet.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(3000);
        editor.select(30);

        // invoke Add Business Method dialog
        GenerateCodeOperator.openDialog(editorPopup, editor);
        CallEnterpriseBeanDialog dialog = new CallEnterpriseBeanDialog();

        new Node(dialog.tree(), calledBean).select();
        if (referencedLocal) {
            dialog.local();
        } else {
            dialog.remote();
        }
        dialog.checkConvertCheckedExceptionsToRuntimeException(convertExceptions);
        if (referenceName != null) {
            dialog.setReferenceName(referenceName);
        }

        dialog.ok();
        editor.txtEditorPane().waitText(toSearchInEditor);
        new EventTool().waitNoEvent(1000);
        if (saveFile) {
            editor.save();
        }

        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
        Utils utils = new Utils(this);
        File WEB_PROJECT_FILE = new File(new File(getDataDir(), EJBValidation.EAR_PROJECT_NAME), EJBValidation.EAR_PROJECT_NAME + "-war");
        utils.assertFiles(new File(WEB_PROJECT_FILE, "src/java/test"), new String[]{"TestingServlet.java"}, getName() + "_");
        String ddNames[] = {"web.xml",
            "glassfish-web.xml"
        };
        utils.assertFiles(new File(WEB_PROJECT_FILE, "web/WEB-INF"), ddNames, isDDModified ? getName() + "_" : "");

        editor.closeDiscard();

    }

    public void testCallEJBInWS() throws IOException {
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle", "LBL_CallEjbAction");
        calledBean = EJBValidation.EJB_PROJECT_NAME + "|SampleSession";
        toSearchInEditor = "sample.SampleSessionRemote lookupSampleSessionBean()";
        referencedLocal = false;
        isDDModified = true;
        saveFile = true;

        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.WEB_PROJECT_NAME),
                "Web Services|SampleWebService");
        new OpenAction().performAPI(openFile);
        EditorOperator editor = EditorWindowOperator.getEditor("SampleWebServiceImpl.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(3000);
        editor.select(11);

        // invoke Add Business Method dialog
        GenerateCodeOperator.openDialog(editorPopup, editor);
        CallEnterpriseBeanDialog dialog = new CallEnterpriseBeanDialog();

        new Node(dialog.tree(), calledBean).select();
        if (referencedLocal) {
            dialog.local();
        } else {
            dialog.remote();
        }
        dialog.checkConvertCheckedExceptionsToRuntimeException(convertExceptions);
        if (referenceName != null) {
            dialog.setReferenceName(referenceName);
        }

        dialog.ok();

        editor.txtEditorPane().waitText(toSearchInEditor);
        editor.replace("return null;", "return lookupSampleSessionBean().sampleBusinessMethod();");
        if (saveFile) {
            editor.save();
        }

        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
        Utils utils = new Utils(this);
        File WEB_PROJECT_FILE = new File(new File(getDataDir(), EJBValidation.EAR_PROJECT_NAME), EJBValidation.EAR_PROJECT_NAME + "-war");
        utils.assertFiles(new File(WEB_PROJECT_FILE, "src/java/sample"), new String[]{"SampleWebServiceImpl.java"}, getName() + "_");
        String ddNames[] = {"web.xml",
            "glassfish-web.xml",
            "webservices.xml"
        };
        utils.assertFiles(new File(WEB_PROJECT_FILE, "web/WEB-INF"), ddNames, isDDModified ? getName() + "_" : "");

        editor.closeDiscard();
    }

    protected void addMethod() throws IOException {
        EditorOperator editor = EditorWindowOperator.getEditor(beanName + "Bean.java");
        editor.select(20);

        // invoke Add Business Method dialog
        GenerateCodeOperator.openDialog(editorPopup, editor);
        CallEnterpriseBeanDialog dialog = new CallEnterpriseBeanDialog();

        new Node(dialog.tree(), calledBean).select();
        if (referencedLocal) {
            dialog.local();
        } else {
            dialog.remote();
        }
        dialog.checkConvertCheckedExceptionsToRuntimeException(convertExceptions);
        if (referenceName != null) {
            dialog.clearReferenceName();
            dialog.typeReferenceName(referenceName);
        }

        dialog.ok();
        editor.txtEditorPane().waitText(toSearchInEditor);
        new EventTool().waitNoEvent(1000);
        if (saveFile) {
            editor.save();
        }

        compareFiles();
    }
}
