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

package org.netbeans.lib.lexer.test.inc;

import junit.framework.TestCase;
import org.netbeans.lib.lexer.inc.OriginalText;

/**
 * Test for the text that emulates state of a mutable text input
 * before a particular modification.
 *
 * @author mmetelka
 */
public class OriginalTextTest extends TestCase {

    public OriginalTextTest(String testName) {
        super(testName);
    }

    public void test() throws Exception {
        String orig = "abcdef";
        check(orig, 0, 2, "xyz");
        check(orig, 0, 2, "x");
        check(orig, 0, 0, "");
        check(orig, 0, 0, "klmnopqrst");
        check(orig, orig.length(), 0, "");
        check(orig, orig.length(), 0, "klmnopqrst");
        check(orig, orig.length(), 0, "x");
        check(orig, 3, 0, "x");
        check(orig, 3, 1, "xyz");
        check(orig, 3, 3, "xy");
        check(orig, 1, 0, "x");
        check(orig, 1, 1, "xyz");
        check(orig, 1, 3, "xy");
        check(orig, 4, 0, "x");
        check(orig, 4, 1, "xy");
        check(orig, 4, 2, "x");
    }

    private void check(String text, int removeIndex, int removeLength, String insertText) {
        String modText = text.substring(0, removeIndex) + insertText + text.substring(removeIndex + removeLength);
        OriginalText ot = new OriginalText(modText, removeIndex, text.substring(removeIndex, removeIndex + removeLength), insertText.length());
        assertEquals(text.length(), ot.length());
        for (int i = 0; i < text.length(); i++) {
            assertEquals(String.valueOf(i), text.charAt(i), ot.charAt(i));
        }
        for (int i = 0; i < text.length(); i++) {
            for (int j = i; j < text.length(); j++) {
                assertEquals(text.substring(i, j), String.valueOf(ot.toCharArray(i, j)));
            }
        }
        assertEquals(text, ot.toString());
    }
    
}
