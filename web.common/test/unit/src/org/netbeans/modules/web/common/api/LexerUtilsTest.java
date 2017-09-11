/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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