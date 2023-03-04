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
package org.netbeans.modules.gsf.testrunner.ui;

import org.netbeans.modules.gsf.testrunner.ui.ClassNameTextField;
import junit.framework.TestCase;

/**
 *
 * @author Theofanis Oikonomou
 */
public class ClassNameTextFieldTest extends TestCase {
    
    public ClassNameTextFieldTest(String testName) {
        super(testName);
    }

    private class StringIntPair {
        private final String str;
        private final int value;
        StringIntPair(String str, int value) {
            this.str = str;
            this.value = value;
        }
    }

    /**
     * Test of determineStatus method, of class org.netbeans.modules.junit.ClassNameTextField.
     */
    public void testDetermineStatus() {
        StringIntPair[] testData = new StringIntPair[] {
                new StringIntPair("", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("A", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc", ClassNameTextField.STATUS_VALID),
                new StringIntPair("Abc", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("Abc2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc2.", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("Abc2.", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("a2.", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("A2.", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("2a", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2a.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("A.B", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.B", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A2.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a2.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A.b2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.b2", ClassNameTextField.STATUS_VALID),
        };
        assertEquals(
                "check determined status if no text is passed",
                ClassNameTextField.STATUS_BEFORE_PART,
                new ClassNameTextField().determineStatus());
        for (int i = 0; i < testData.length; i++) {
            assertEquals(
                    "check determined status for text \"" + testData[i].str + '"',
                    testData[i].value,
                    new ClassNameTextField(testData[i].str).determineStatus());
        }
    }

    /**
     * Test of getStatus method, of class org.netbeans.modules.junit.ClassNameTextField.
     */
    public void testGetStatus() {
          StringIntPair[] testData = new StringIntPair[] {
                new StringIntPair("", ClassNameTextField.STATUS_EMPTY),
                new StringIntPair("A", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("ATest", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("abcTest", ClassNameTextField.STATUS_VALID),
                new StringIntPair("Abc", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("AbcTest", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("Abc2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("a2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("abc2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("Abc2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("a2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("A2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2a", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2a.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("A.B", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.BTest", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.B", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.b", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("a.b", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A2.b", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("a2.b", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.b2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.b2Test", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.b2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.b2IT", ClassNameTextField.STATUS_VALID),
        };
        assertEquals(
                "check status if no parameter passed",
                ClassNameTextField.STATUS_EMPTY,
                new ClassNameTextField().getStatus());
        for (int i = 0; i < testData.length; i++) {
            assertEquals(
                    "check status for text \"" + testData[i].str + '"',
                    testData[i].value,
                    new ClassNameTextField(testData[i].str).getStatus());
        }
    }
}
