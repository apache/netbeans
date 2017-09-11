/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.i18n.test.internationalize;

import lib.InternationalizationTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author  Jana Maleckova
 */
public class Internationalize extends InternationalizationTestCase {

    String TEST_PACKAGE = "data";
    String FILE_NAME = "TestFrame";
    String TEST_CLASS = "Internationalize";
    String MENU_PATH = "Tools" + TREE_SEPARATOR + "Internationalization" + TREE_SEPARATOR + "Internationalize...";
    // String testName = suite().getClass().toString();
    /**
     * Constructor - Creates new instance of this class
     */
    public Internationalize(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Internationalize("testInternationalize"));
        return suite;
    }

    /**
     * This method contains body of test
     * @return void
     */
    public void testInternationalize() throws Exception {
        System.out.println();
        System.out.println("===================================================================================");
        System.out.println("=   Test :  Internationalize - complex walkthrough                                =");
        System.out.println("=   See testspec of i18n module:                                                  =");
        System.out.println("=   http://beetle.czech/modules/i18n/                                             =");
        System.out.println("===================================================================================");
        System.out.println("Pracovni adresar: " + getDataDir().toString());
        //System.out.println("Golden files " + getGoldenFile().getAbsolutePath());

        //Open project
        openProject(DEFAULT_PROJECT_NAME);

        //Create new Bundle file
        createNewPropertiesFile(getClassNode(DEFAULT_PROJECT_NAME, ROOT_PACKAGE_NAME + TREE_SEPARATOR + DEFAULT_PROJECT_NAME.toLowerCase()), DEFAUL_BUNDLE_NAME);

        //select testing class with strings
        Node testClass = getClassNode(DEFAULT_PROJECT_NAME, ROOT_PACKAGE_NAME + TREE_SEPARATOR + DEFAULT_PROJECT_NAME.toLowerCase() + TREE_SEPARATOR + TEST_CLASS + ".java");
        testClass.select();

        //Open wizard for simple Internationalize
        testClass.callPopup().pushMenuNoBlock(MENU_PATH, TREE_SEPARATOR);

        /*Process of internationalization, all strings from java files 
        should be internationalized
         */
        NbDialogOperator ndo = new NbDialogOperator(TITLE_INTERNATIONALIZE_DIALOG);
        JButtonOperator jbo = new JButtonOperator(ndo, REPLACE_BUTTON);

        //Check if Replace button is enabled
        if (jbo.isEnabled()) {
            while (jbo.isEnabled()) {
                jbo.push();
            }
        } else {
            throw new Exception("Internationalization dialog doesn't found any string in class");

        }

        //Close dialog for internationalization
        JButtonOperator close = new JButtonOperator(ndo, CLOSE_BUTTON);
        close.push();

        //Close and save recently edited files - Bundle, Internationalize.java
        String[] Files = {DEFAUL_BUNDLE_NAME, TEST_CLASS + ".java"};

        for (int i = 0; i < Files.length; i++) {
            closeFileInEditor(Files[i], true);
        }
        
        /*Test if java class was correctly internationalized
         * and bundle file contains all keys with values
        */
        compareBundle(DEFAUL_BUNDLE_NAME);
        compareJavaFile(TEST_CLASS);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
