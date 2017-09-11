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

package org.openide.util;

import org.netbeans.junit.NbTestCase;
import java.util.Comparator;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CharSequencesTest extends NbTestCase {

    public CharSequencesTest(String testName) {
        super(testName);
    }

    @Test
    public void testMaths() {
        processPattern('1', 'ü');
        processPattern('a', 'ü');
        processPattern('ü', '1');
        processPattern('ü', 'a');
        processPattern('1', (char)255);
        processPattern('a', (char)255);
        processPattern((char)255, '1');
        processPattern((char)255, 'a');
        processPattern('1', (char)127);
        processPattern('a', (char)127);
        processPattern((char)127, '1');
        processPattern((char)127, 'a');
        processPattern('1', (char)128);
        processPattern('a', (char)128);
        processPattern((char)128, '1');
        processPattern((char)128, 'a');
        processPattern('1', 'a');
        processPattern('a', '1');
        processPattern('A', 'a');
        processPattern('a', 'A');
    }

    private void processPattern(char filler, char template){
        for(int i = 1; i < 32; i++) {
            for(int j = 0; j < i; j++) {
                char[] t = new char[i];
                for(int k = 0; k < i; k++) {
                    t[k] = filler;
                }
                t[j] = template;
                assertEquals(new String(t), CharSequences.create(t, 0, i).toString());
            }
        }
    }

    @Test
    public void testUmlaut() {
        // #189988 -  Netbeans IDE does not find source files with umlauts in class name
        String name = "ProbenSchlüssel.java";
        CharSequence cs = CharSequences.create(name);
        assertEquals("incorrect conversion", cs.toString(), name);
    }

    /**
     * Test of create method, of class CharSequences.
     */
    @Test
    public void testCreate_3args() {
        char[] buf = "Hello, Platform! Из Санкт Петербурга".toCharArray();
        for (int start = 0; start < buf.length; start++) {
            for (int count = 1; count < buf.length - start; count++) {
                String expResult = new String(buf, start, count);
                CharSequence result = CharSequences.create(buf, start, count);
                assertEquals("["+start+", " + count+"]", expResult, result.toString());
                assertTrue("["+start+", " + count+"]", expResult.contentEquals(result));
            }
        }
    }

    @Test
    public void testANSISubSequence() {
        String str = "12345678901234567890123456789";
        CharSequence sub1 = str.subSequence(1, str.length());
        CharSequence cs = CharSequences.create(str);
        CharSequence sub2 = cs.subSequence(1, cs.length());
        CharSequences.comparator().compare(sub1, sub2);
    }

    @Test
    public void testUnicodeSubSequence() {
        String str = "Длинный Русский текст? Достаточно длинный";
        CharSequence sub1 = str.subSequence(1, str.length());
        CharSequence cs = CharSequences.create(str);
        CharSequence sub2 = cs.subSequence(1, cs.length());
        CharSequences.comparator().compare(sub1, sub2);
    }

    /**
     * Test of create method, of class CharSequences.
     */
    @Test
    public void testCreate_CharSequence() {
        String[] strs = new String[] { "", "1234567", "123456789012345", "12345678901234567890123", "123456789012345678901234",
                                       "1234567890", "1234567890abcdefghkl", "1234567890ABCDEFGHKLabcdefghkl",
                                       ".12345678_", "1234567890abcdefghkl", "1234567890ABCDEFGHKLabcdefghkl",
                                       "Русский                   Текст" };
        for (String str : strs) {
            CharSequence cs = CharSequences.create(str);
            assertEquals(str, cs.toString());
            assertEquals(cs.toString(), str);
            assertEquals(cs.toString(), cs.toString());
            assertTrue(str.contentEquals(cs));
        }
        
        for (int i = 0; i< 1024; i++) {
            StringBuilder buf = new StringBuilder();
            for (int j = 0; j< 64; j++) {
                buf.append(i);
                String str = buf.toString();
                CharSequence cs = CharSequences.create(str);
                assertEquals(str, cs.toString());
                assertEquals(cs.toString(), str);
                assertEquals(cs.toString(), cs.toString());
                assertTrue(str.contentEquals(cs));
            }
        }
    }

    /**
     * Test of comparator method, of class CharSequences.
     */
    @Test
    public void testCaseSensitiveComparator() {
        Comparator<CharSequence> comparator = CharSequences.comparator();
        String[] strs = new String[]{"", "1234567", "123456789012345", "12345678901234567890123", "Русский Текст", "123456789012345678901234"};
        for (String str : strs) {
            assertEquals(0, comparator.compare(str, CharSequences.create(str)));
            assertEquals(0, comparator.compare(CharSequences.create(str), str));
            assertEquals(0, comparator.compare(CharSequences.create(str), CharSequences.create(str)));
        }
    }

    /**
     * Test of empty method, of class CharSequences.
     */
    @Test
    public void testEmpty() {
        assertEquals("", CharSequences.create("").toString());
        assertEquals(CharSequences.create("").toString(), "");
        assertEquals("", CharSequences.empty().toString());
        assertEquals(CharSequences.empty().toString(), "");
        assertSame(CharSequences.empty(), CharSequences.create(""));
    }

    /**
     * Test of isCompact method, of class CharSequences.
     */
    @Test
    public void testIsCompact() {
        String[] strs = new String[]{"", "1234567", "123456789012345", "12345678901234567890123", "Русский Текст", "123456789012345678901234"};
        for (String str : strs) {
            assertFalse(" string is compact but must not be", CharSequences.isCompact(str));
            assertTrue(" string is not compact but must be", CharSequences.isCompact(CharSequences.create(str)));
        }
        assertTrue(" empty string is not compact ", CharSequences.isCompact(CharSequences.empty()));
    }

    /**
     * Test of indexOf method, of class CharSequences.
     */
    @Test
    public void testIndexOf_CharSequence_CharSequence() {
        CharSequence text = CharSequences.create("CharSequences");
        CharSequence seq = CharSequences.create("Sequence");
        assertEquals(4, CharSequences.indexOf(text, "Sequence"));
        assertEquals(4, CharSequences.indexOf(text, seq));
        assertEquals(4, CharSequences.indexOf("CharSequences", "Sequence"));
        assertEquals(4, CharSequences.indexOf("CharSequences", seq));
        assertEquals(-1, CharSequences.indexOf(text, "Sequens"));
    }

    /**
     * Test of indexOf method, of class CharSequences.
     */
    @Test
    public void testIndexOf_3args() {
        CharSequence text = CharSequences.create("CharSequences");
        CharSequence seq = CharSequences.create("Sequence");
        assertEquals(4, CharSequences.indexOf(text, "Sequence", 2));
        assertEquals(4, CharSequences.indexOf(text, seq, 2));
        assertEquals(4, CharSequences.indexOf("CharSequences", "Sequence", 2));
        assertEquals(4, CharSequences.indexOf("CharSequences", seq, 2));
        assertEquals(-1, CharSequences.indexOf("CharSequences", seq, 5));
    }

    @Test
    public void testSizes() {
        // 32-bit JVM
        //String    String CharSequence
        //Length     Size    Size
        //1..2        40      16
        //3..6        48      16
        //7..7        56      16
        //8..10       56      24
        //11..14      64      24
        //15..15      72      24
        //16..18      72      32
        //19..22      80      32
        //23..23      88      32
        //24..26      88      56
        //27..28      96      56
        //29..30      96      64
        //31..34     104      64
        //35..36     112      64
        //37..38     112      72
        //39..42     120      72
        //......................
        //79..82   - 200     112
        char[] buf = "12345678901234567890123456789012345678901234567890".toCharArray();
        int curStrLen = 0;
        int[][] lenSize = new int[][] { { 7, 16 }, { 15, 24 }, { 23, 32 }, {28, 56 }, { 36, 64 }, {42, 72}, {50, 80}};
        for (int j = 0; j < lenSize.length; j++) {
            int strLenLimit = lenSize[j][0];
            int sizeLimit = lenSize[j][1];
            for (; curStrLen <= strLenLimit; curStrLen++) {
                CharSequence cs = CharSequences.create(buf, 0, curStrLen);
                assertSize("Size is too big " + cs, sizeLimit, cs);
                // check we are better than strings
                boolean stringIsBigger = false;
                String str = new String(buf, 0, curStrLen);
                try {
                    assertSize("Size is too big for " + str, sizeLimit, str);
                } catch (AssertionFailedError e) {
//                    System.err.println(e.getMessage());
                    stringIsBigger = true;
                }
                assertTrue("string object is smaller than our char sequence", stringIsBigger);
            }
        }
        // check that our Impl is not worse than default String for Unicode as well
        String rusText = "Русский Текст";
        CharSequence cs = CharSequences.create(rusText);
        assertTrue(rusText.contentEquals(cs));
        int sizeLimit = 56;
        assertSize("Size is too big for " + cs, sizeLimit, cs);
        boolean stringIsBigger = false;
        boolean stringIsSame = false;
        try {
            // make sure our impl is better
            assertSize(rusText, sizeLimit, rusText);
            try {
                // of the same (as in JDK 8)
                assertSize(rusText, sizeLimit - 1, rusText);
            } catch (AssertionFailedError e) {
//                System.err.println(e.getMessage());
                stringIsSame = true;
            }            
        } catch (AssertionFailedError e) {
//                    System.err.println(e.getMessage());
            stringIsBigger = true;
        }
        assertTrue("string object \"" + rusText + "\" is smaller than our char sequence with size " + sizeLimit, stringIsBigger || stringIsSame);
    }
}
