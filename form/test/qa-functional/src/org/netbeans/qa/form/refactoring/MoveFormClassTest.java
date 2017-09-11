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
package org.netbeans.qa.form.refactoring;

import org.netbeans.qa.form.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;


/**
 * Tests form refactoring, 3rd scenarion : Move form class into dif package
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 26 APRIL 2011 WORKS
 */
public class MoveFormClassTest extends ExtJellyTestCase {

    private String CLASS_NAME = "FrameWithBundleToMove"; // NOI18N
//    private String CLASS_NAME = "ClassToMove"; // NOI18N    
    private String NEW_PACKAGE_NAME = "subdata";
    private String PACKAGE_NAME = "." + NEW_PACKAGE_NAME; // NOI18N

    /**
     * Constructor required by JUnit
     * @param testName
     */
    public MoveFormClassTest(String testName) {
        super(testName);
    }

    
      public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(MoveFormClassTest.class).addTest(
                "testCreatePackage", 
                "testRefactoring", 
                "testChangesInJavaFile", 
                "testChangesInPropertiesFile" 
                ).clusters(".*").enableModules(".*").gui(true));

    }

    /** Creates subdata package  */
    public void testCreatePackage() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getTestProjectName());
        prn.select();

        Node formnode = new Node(prn, "Source Packages"); // NOI18N
        formnode.setComparator(new Operator.DefaultStringComparator(true, false));
        formnode.select();

        runNoBlockPopupOverNode("New|Java Package...", formnode); // NOI18N

        NbDialogOperator dialog = new NbDialogOperator("New Java Package");
        new JTextFieldOperator(dialog,0).typeText( getTestPackageName() + PACKAGE_NAME);
        new JButtonOperator(dialog, "Finish").push();
    }

    /** Runs refactoring  */
    public void testRefactoring() throws Exception {
        Node node = openFile(CLASS_NAME);

        /*Task manager takes a long time for scanning and due to this case, file is not opened in time.
        Implemented workaround - sleep for a while
         */
        waitNoEvent(1000);

        runNoBlockPopupOverNode("Refactor|Move...", node); // NOI18N
        waitNoEvent(3000);
        JDialogOperator dialog = new JDialogOperator("Move"); // NOI18N
        JComboBoxOperator combo = new JComboBoxOperator(dialog, 2);
        combo.selectItem( getTestPackageName() + PACKAGE_NAME);

        new JButtonOperator(dialog,"Refactor").clickMouse();

        // this refactoring case takes sometimes a very long time
        // that's way there is following code with for loop
        boolean isClosed = false;
        TimeoutExpiredException lastExc = null;

        for (int i=0; i < 3; i++) {
            try {
                dialog.waitClosed();
                isClosed = true;
            } catch (TimeoutExpiredException e) {
                lastExc = e;
            } catch (Exception e) {
                throw e;
            }
        }

        if (!isClosed) {
            throw (lastExc != null) ? lastExc : new Exception("Something strange while waiting using waitClosed() method");
        }
    }

    /** Tests content of java file */
    public void testChangesInJavaFile() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getTestProjectName());
        prn.select();

        String path = "Source Packages|" + getTestPackageName() + PACKAGE_NAME + "|" + CLASS_NAME + ".java"; // NOI18N
        //p(path);
        Node formnode = new Node(prn, path ); // NOI18N
        formnode.setComparator(new Operator.DefaultStringComparator(true, false));
//        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);

        FormDesignerOperator designer = new FormDesignerOperator(CLASS_NAME);
        
        // new class package
        findInCode("package data.subdata;", designer);
    }

    /** Test changes in property bundle file */
    public void testChangesInPropertiesFile() {
        String sourceFilePath = getFilePathFromDataPackage(NEW_PACKAGE_NAME
                                        + File.separator
                                        +"Bundle.properties");

        String key = "FrameWithBundleToMove.lanButton.text";
        assertTrue("Key \"" + key + "\" not found in Bundle.properties file.",
                findInFile( key, sourceFilePath)); // NOI18N
    }
}    
