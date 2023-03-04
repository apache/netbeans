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

import junit.framework.Test;

/**
 * Test of org.netbeans.jellytools.QuestionDialogOperator.
 *
 * @author Jiri Skrivanek
 */
public class QuestionDialogOperatorTest extends NbDialogOperatorTest {

    /** "Question" */
    private static final String TEST_DIALOG_TITLE =
            Bundle.getString("org.openide.text.Bundle", "LBL_SaveFile_Title");
    public static String[] tests = new String[]{
        "testConstructorWithParameter",
        "testLblQuestion"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public QuestionDialogOperatorTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return createModuleTest(QuestionDialogOperatorTest.class, tests);
    }

    /** Shows dialog to test. */
    @Override
    protected void setUp() {
        showTestDialog(TEST_DIALOG_TITLE);
    }

    /** Test constructor with text parameter. */
    public void testConstructorWithParameter() {
        new QuestionDialogOperator(TEST_DIALOG_LABEL).ok();
    }

    /** Test lblQuestion() method. */
    public void testLblQuestion() {
        QuestionDialogOperator qdo = new QuestionDialogOperator();
        String label = qdo.lblQuestion().getText();
        qdo.ok();
        assertEquals("Wrong label found.", TEST_DIALOG_LABEL, label);
    }
}
