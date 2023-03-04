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

package org.netbeans.modules.web.common.api;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.test.CslTestBase;

/**
 *
 * @author marekfukala
 */
public class LexerUtilsTest extends CslTestBase {

    public LexerUtilsTest(String testName) {
        super(testName);
    }
    
    //Bug 199420 - Last rule in css file has no fold
    public void testGetLineOffset_Issue199420() throws BadLocationException {
        String text = "div { \n }";
        //             0123456 78
        assertEquals(0, LexerUtils.getLineOffset(text, 4));
        assertEquals(1, LexerUtils.getLineOffset(text, 9));
    }

    public void testGetLineOffset() throws BadLocationException {
        String text = "one\ntwo\nthree\n";
        //             0123 4567 890123
        //             0           1

        assertEquals(0, LexerUtils.getLineOffset(text, 0));
        assertEquals(0, LexerUtils.getLineOffset(text, 2));
        assertEquals(0, LexerUtils.getLineOffset(text, 3)); //the newline itself

        assertEquals(1, LexerUtils.getLineOffset(text, 4));
        assertEquals(1, LexerUtils.getLineOffset(text, 6));
        assertEquals(1, LexerUtils.getLineOffset(text, 7)); //the new line itself

        assertEquals(2, LexerUtils.getLineOffset(text, 8));
        assertEquals(2, LexerUtils.getLineOffset(text, 10));
        assertEquals(2, LexerUtils.getLineOffset(text, 13)); //the new line itself

        //test null input
        try {
            LexerUtils.getLineOffset(null, 0);
            assertTrue(false);
        } catch (NullPointerException e) {}

        //test bounds check
        try {
            LexerUtils.getLineOffset(text, -1);
            assertTrue(false);
        } catch (BadLocationException e) {}
        
        try {
            LexerUtils.getLineOffset(text, 100);
            assertTrue(false);
        } catch (BadLocationException e) {}

        //check the \r test
        try {
            LexerUtils.getLineOffset("hello\r\nworld!", 8);
            assertTrue(false);
        } catch (IllegalArgumentException e) {}
    }

    public void testTrim() {
        assertEquals("cau", LexerUtils.trim("cau"));
        assertEquals("", LexerUtils.trim(""));
        
        assertNotEquals("cau", LexerUtils.trim("cau2"));
        assertNotEquals("", LexerUtils.trim("x"));
        assertNotEquals("x", LexerUtils.trim(""));
        
        assertEquals("x", LexerUtils.trim(" x"));
        assertEquals("x", LexerUtils.trim("x "));
        assertEquals("x", LexerUtils.trim("   x    "));
        
        assertEquals("hello world", LexerUtils.trim("   hello world "));
        
        assertEquals(".aaa", LexerUtils.trim(".aaa "));
    }

    public void assertEquals(CharSequence ch1, CharSequence ch2) {
        assertTrue(String.format("'%s' != '%s'", ch1.toString(), ch2.toString()), LexerUtils.equals(ch1, ch2, false, false));
    }
    
    public void assertNotEquals(CharSequence ch1, CharSequence ch2) {
        assertFalse(String.format("'%s' == '%s'", ch1.toString(), ch2.toString()), LexerUtils.equals(ch1, ch2, false, false));
    }

}