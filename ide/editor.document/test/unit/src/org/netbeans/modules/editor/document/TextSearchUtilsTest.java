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
package org.netbeans.modules.editor.document;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.editor.document.implspi.CharClassifier;

/**
 *
 * @author Miloslav Metelka
 */
public class TextSearchUtilsTest {
    
    public TextSearchUtilsTest() {
    }

    private static final CharClassifier DC = TextSearchUtils.DEFAULT_CLASSIFIER;

    //                                                    01234567890123
    private static final String THREE_IDENTIFIERS_TEXT = "One Two Three";
    //                                        0123456789012345
    private static final String INT_I_TEXT = "int i = ++jkl; ";
    //                                             01234 567890123 4567890
    private static final String MULTI_LINE_TEXT = " ab \n/** jdoc\n * 2nd";
    
    private static final String MULTI_LINE_TEXT1 = "Hello\nWorld\n";
    /**
     * Test of getWordStart method, of class TextSearchUtils.
     */
    @Test
    public void testGetWordStart() throws Exception {
        assertEquals(8, TextSearchUtils.getWordStart(THREE_IDENTIFIERS_TEXT, DC, 13));
        assertEquals(4, TextSearchUtils.getWordStart(THREE_IDENTIFIERS_TEXT, DC, 8));

        assertEquals(10, TextSearchUtils.getWordStart(INT_I_TEXT, DC, 12));
        assertEquals(8, TextSearchUtils.getWordStart(INT_I_TEXT, DC, 10));
    }

    /**
     * Test of getWordEnd method, of class TextSearchUtils.
     */
    @Test
    public void testGetWordEnd() {
        assertEquals(3, TextSearchUtils.getWordEnd(THREE_IDENTIFIERS_TEXT, DC, 0));
        assertEquals(7, TextSearchUtils.getWordEnd(THREE_IDENTIFIERS_TEXT, DC, 4));

        assertEquals(13, TextSearchUtils.getWordEnd(INT_I_TEXT, DC, 10));
        assertEquals(10, TextSearchUtils.getWordEnd(INT_I_TEXT, DC, 8));

        assertEquals(11, TextSearchUtils.getWordEnd(MULTI_LINE_TEXT1, DC, 11));
        assertEquals(6, TextSearchUtils.getWordEnd(MULTI_LINE_TEXT1, DC, 5));
        assertEquals(5, TextSearchUtils.getWordEnd(MULTI_LINE_TEXT1, DC, 4));
    }

    /**
     * Test of getWord method, of class TextSearchUtils.
     */
    @Test
    public void testGetWord() throws Exception {
    }

    /**
     * Test of getNextWordStart method, of class TextSearchUtils.
     */
    @Test
    public void testGetNextWordStart() {
    }

    /**
     * Test of getPreviousWordEnd method, of class TextSearchUtils.
     */
    @Test
    public void testGetPreviousWordEnd() {
    }

    /**
     * Test of getPreviousWordBoundary method, of class TextSearchUtils.
     */
    @Test
    public void testGetPreviousWordStart() {
        assertEquals(8, TextSearchUtils.getPreviousWordStart(THREE_IDENTIFIERS_TEXT, DC, THREE_IDENTIFIERS_TEXT.length()));
        assertEquals(4, TextSearchUtils.getPreviousWordStart(THREE_IDENTIFIERS_TEXT, DC, 8));

        assertEquals(13, TextSearchUtils.getPreviousWordStart(INT_I_TEXT, DC, 15));
        assertEquals(10, TextSearchUtils.getPreviousWordStart(INT_I_TEXT, DC, 13));
        assertEquals(8, TextSearchUtils.getPreviousWordStart(INT_I_TEXT, DC, 10));
        assertEquals(6, TextSearchUtils.getPreviousWordStart(INT_I_TEXT, DC, 8));
        assertEquals(4, TextSearchUtils.getPreviousWordStart(INT_I_TEXT, DC, 6));
        assertEquals(0, TextSearchUtils.getPreviousWordStart(INT_I_TEXT, DC, 4));

        assertEquals(17, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 20));
        assertEquals(15, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 17));
        assertEquals(14, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 15));
        assertEquals(13, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 14));
        assertEquals(9, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 13));
        assertEquals(5, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 9));
        assertEquals(4, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 5));
        assertEquals(1, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 4));
        assertEquals(0, TextSearchUtils.getPreviousWordStart(MULTI_LINE_TEXT, DC, 1));
    }

    /**
     * Test of getNextWhitespace method, of class TextSearchUtils.
     */
    @Test
    public void testGetNextWhitespace() {
    }

    /**
     * Test of getPreviousWhitespace method, of class TextSearchUtils.
     */
    @Test
    public void testGetPreviousWhitespace() {
    }

    /**
     * Test of getNextNonWhitespace method, of class TextSearchUtils.
     */
    @Test
    public void testGetNextNonWhitespace() {
    }

    /**
     * Test of getPreviousNonWhitespace method, of class TextSearchUtils.
     */
    @Test
    public void testGetPreviousNonWhitespace() {
    }

    /**
     * Test of getNextNonNewline method, of class TextSearchUtils.
     */
    @Test
    public void testGetNextNonNewline() {
    }

    /**
     * Test of getPreviousNonNewline method, of class TextSearchUtils.
     */
    @Test
    public void testGetPreviousNonNewline() {
    }

    /**
     * Test of isLineEmpty method, of class TextSearchUtils.
     */
    @Test
    public void testIsLineEmpty() {
    }
    
}
