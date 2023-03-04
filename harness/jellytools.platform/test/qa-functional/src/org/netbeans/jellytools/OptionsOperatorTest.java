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
