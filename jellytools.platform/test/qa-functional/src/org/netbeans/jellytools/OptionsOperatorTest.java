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
package org.netbeans.jellytools;

import org.netbeans.jellytools.actions.Action;
import org.netbeans.junit.NbTestSuite;

/** Tests org.netbeans.jellytools.OptionsOperator. */
public class OptionsOperatorTest extends JellyTestCase {

    public static String[] tests = new String[]{
        "testSelectEditor",
        "testSelectFontAndColors",
        "testSelectKeymap",
        "testSelectMiscellaneous",
        "testSelectGeneral",
        "testClose"};

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {

        return (NbTestSuite) createModuleTest(OptionsOperatorTest.class,
                tests);
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public OptionsOperatorTest(String testName) {
        super(testName);
    }
    private static OptionsOperator optionsOperator = null;

    /** Setup */
    @Override
    public void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        // opens Options window
        if (optionsOperator == null) {

            //Make sure the menu has time to load
            new Action(Bundle.getStringTrimmed(
                    "org.netbeans.core.ui.resources.Bundle", "Menu/Tools"), null).performMenu();

            Thread.sleep(1000);


            new Action(Bundle.getStringTrimmed(
                    "org.netbeans.core.ui.resources.Bundle", "Menu/GoTo"), null).performMenu();

            Thread.sleep(1000);

            optionsOperator = OptionsOperator.invoke();
        }
    }

    /** Tear down. */
    @Override
    public void tearDown() {
    }

    /** Test of selectEditor method. */
    public void testSelectEditor() {
        optionsOperator.selectEditor();
    }

    /** Test of selectFontAndColors method. */
    public void testSelectFontAndColors() {
        optionsOperator.selectFontAndColors();
    }

    /** Test of selectKeymap method. */
    public void testSelectKeymap() {
        optionsOperator.selectKeymap();
    }

    /** Test of selectMiscellaneous method. */
    public void testSelectMiscellaneous() {
        optionsOperator.selectMiscellaneous();
    }

    /** Test of selectGeneral method.  */
    public void testSelectGeneral() {
        optionsOperator.selectGeneral();
    }

    /** Test of close method.  */
    public void testClose() {
        optionsOperator.close();
    }
}
