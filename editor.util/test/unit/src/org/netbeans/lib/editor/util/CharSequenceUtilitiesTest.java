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

package org.netbeans.lib.editor.util;

import java.util.Random;
import junit.framework.TestCase;

public class CharSequenceUtilitiesTest extends TestCase {

    private static final int CHARS_LENGTH = 1000;
    private static final int SUBSTR_LENGTH = 100;
    private static final Random rnd = new Random(0);

    public CharSequenceUtilitiesTest(String testName) {
        super(testName);
    }

    public void testCharSequence() {
        char[] chars = new char[CHARS_LENGTH];
        char[] chars_2 = new char[CHARS_LENGTH];
        generateChars(chars);
        generateChars(chars_2);
        String string = new String(chars);
        String string_2 = new String(chars_2);
        
        // textEquals
        assertTrue(CharSequenceUtilities.textEquals(string, string));
        String s = new String(chars);
        assertTrue(CharSequenceUtilities.textEquals(string, s));
        assertTrue(CharSequenceUtilities.textEquals(string, string_2) == string.equals(string_2));
        
        // toString
        assertTrue(CharSequenceUtilities.toString(string).equals(string));
        
        try {
            CharSequenceUtilities.toString(string, -1, 0);
            assertTrue(false);
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharSequenceUtilities.toString(string, 0, CHARS_LENGTH + 1);
            assertTrue(false);
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharSequenceUtilities.toString(string, CHARS_LENGTH, 0);
            assertTrue(false);
        } catch (IndexOutOfBoundsException e) {
        }
        int start = CHARS_LENGTH / 5;
        int end = CHARS_LENGTH - start;
        assertTrue(CharSequenceUtilities.toString(string, start, end).equals(string.substring(start, end)));
        
        // append
        StringBuffer buf = new StringBuffer();
        CharSequenceUtilities.append(buf, string);
        CharSequenceUtilities.append(buf, string_2);
        StringBuffer buff = new StringBuffer();
        buff.append(string);
        buff.append(string_2);
        assertTrue(buff.toString().equals(buf.toString()));
        
        buf = new StringBuffer();
        CharSequenceUtilities.append(buf, string, start, end);
        assertTrue(buf.toString().equals(string.substring(start, end)));
        
        // indexOf
        char ch = string.charAt(start);
        assertTrue(string.indexOf(ch) == CharSequenceUtilities.indexOf(string, ch));
        assertTrue(string.indexOf(ch, 2 * start) == CharSequenceUtilities.indexOf(string, ch, 2 * start));
        
        String eta = string.substring(start, start + SUBSTR_LENGTH);
        assertTrue(string.indexOf(eta) == CharSequenceUtilities.indexOf(string, eta));
        eta = string.substring(2 * start, 2 * start + SUBSTR_LENGTH);
        assertTrue(string.indexOf(eta, start) == CharSequenceUtilities.indexOf(string, eta, start));
        
        // lastIndexOf
        assertTrue(string.lastIndexOf(ch) == CharSequenceUtilities.lastIndexOf(string, ch));
        assertTrue(string.lastIndexOf(ch, 2 * start) == CharSequenceUtilities.lastIndexOf(string, ch, 2 * start));
        
        eta = string.substring(start, start + SUBSTR_LENGTH);
        assertTrue(string.lastIndexOf(eta) == CharSequenceUtilities.lastIndexOf(string, eta));
        eta = string.substring(2 * start, 2 * start + SUBSTR_LENGTH);
        assertTrue(string.lastIndexOf(eta, CHARS_LENGTH) == CharSequenceUtilities.lastIndexOf(string, eta, CHARS_LENGTH));
        
        // trim
        buf = new StringBuffer();
        for (int x = 0; x < SUBSTR_LENGTH; x++) {
            buf.append((char)rnd.nextInt(' ' + 1));
        }
        buf.append(string);
        for (int x = 0; x < SUBSTR_LENGTH; x++) {
            buf.append((char)rnd.nextInt(' ' + 1));
        }
        assertTrue(CharSequenceUtilities.textEquals(string, CharSequenceUtilities.trim(buf.toString())));
        
        // startsWith
        assertTrue(CharSequenceUtilities.startsWith(string, string.substring(0, SUBSTR_LENGTH)));
        
        // endsWith
        assertTrue(CharSequenceUtilities.endsWith(string, string.substring(CHARS_LENGTH - SUBSTR_LENGTH)));
        
        CharSequenceUtilities.checkIndexesValid(0, 3, 3); // start,end,length
        CharSequenceUtilities.checkIndexesValid(1, 3, 3);
        try {
            CharSequenceUtilities.checkIndexesValid(1, 4, 3);
            TestCase.fail("IndexOutOfBoundsException was expected.");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }
    
    public void generateChars(char[] chars) {
        for (int x = 0; x < chars.length; x++) {
            chars[x] = (char) rnd.nextInt();
        }
    }
    
}
