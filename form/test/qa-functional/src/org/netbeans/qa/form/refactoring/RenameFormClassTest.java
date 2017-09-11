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

import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.qa.form.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests form refactoring, 1st scenarion : Rename component variable
 * and tests value and access rights of inherited properties
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 26 APRIL 2011 WORKS
 */
public class RenameFormClassTest extends ExtJellyTestCase {
    private String CLASS_OLD_NAME = "FrameWithBundle"; // NOI18N
    private String CLASS_NEW_NAME = CLASS_OLD_NAME + "Renamed"; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public RenameFormClassTest(String testName) {
        super(testName);
    }
    
        
    /**
     * Creates suite from particular test cases.
     * @return nb test suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(RenameFormClassTest.class)
                .addTest("testRefactoring", "testChangesInJavaFile", "testChangesInPropertiesFile")
                .clusters(".*").enableModules(".*").gui(true));
    }

    /** Runs refactoring  */
    public void testRefactoring() {
        Node node = openFile(CLASS_OLD_NAME);
        runNoBlockPopupOverNode("Refactor|Rename...", node); // NOI18N

        JDialogOperator dialog = new JDialogOperator("Rename"); // NOI18N
        waitNoEvent(3000);
        new JTextFieldOperator(dialog).setText(CLASS_NEW_NAME);
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        dialog.waitClosed();
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        openFile(CLASS_NEW_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(CLASS_OLD_NAME);
        
        ArrayList<String> lines = new ArrayList<String>();

        // new class name
        lines.add("public class FrameWithBundleRenamed"); // NOI18N

        // new class constructor name
        lines.add("public FrameWithBundleRenamed()"); // NOI18N
        
        // new key name
        lines.add("bundle.getString(\"FrameWithBundleRenamed.lanciaButton.text\")"); // NOI18N
        
        findInCode(lines, designer);
    }
    
    /** Test changes in property bundle file */
    public void testChangesInPropertiesFile() {
        String sourceFilePath = getFilePathFromDataPackage("Bundle.properties");
        //p(sourceFilePath);
        
        assertTrue("New class name \""+CLASS_NEW_NAME+"\" not found in Bundle.properties file.",
                findInFile(CLASS_NEW_NAME,sourceFilePath)); // NOI18N
    }
}    
