/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
