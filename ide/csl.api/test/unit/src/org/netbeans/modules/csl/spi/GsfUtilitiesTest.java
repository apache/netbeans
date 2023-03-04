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

package org.netbeans.modules.csl.spi;

import org.netbeans.modules.csl.spi.GsfUtilities;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tor Norbye
 */
public class GsfUtilitiesTest extends NbTestCase {

    public GsfUtilitiesTest(String name) {
        super(name);
    }


    public BaseDocument getDocument(String s) throws Exception {
        // These 3 lines are necessary to avoid BaseDocument's constructor
        // throwing preference-related initialization exceptions
        MockServices.setServices(MockMimeLookup.class);
        Preferences prefs = NbPreferences.root();
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), prefs);

        BaseDocument doc = new BaseDocument(false, "text/plain");
        doc.insertString(0, s, null);
        return doc;
    }

    private void checkSetIndentation(String before, int indent, String after, boolean indentWithTabs) throws Exception {
        int offset = before.indexOf('^');
        // Must indicate caret pos!
        assertTrue(offset != -1);
        before = before.substring(0, offset) + before.substring(offset + 1);

        BaseDocument doc = getDocument(before);

        if (indentWithTabs) {
            CodeStylePreferences.get(doc).getPreferences().putBoolean(SimpleValueNames.EXPAND_TABS, false);
            CodeStylePreferences.get(doc).getPreferences().putInt(SimpleValueNames.TAB_SIZE, 8);
        }

        GsfUtilities.setLineIndentation(doc, offset, indent);
        assertEquals(after, doc.getText(0, doc.getLength()));
    }

    public void testSetLineIndentation1() throws Exception {
        checkSetIndentation("\n^", 4, "\n    ", false);
    }

    public void testSetLineIndentation2() throws Exception {
        checkSetIndentation("^", 4, "    ", false);
    }

    public void testSetLineIndentation3() throws Exception {
        checkSetIndentation("f^oo", 4, "    foo", false);
    }

    public void testSetLineIndentation4() throws Exception {
        checkSetIndentation("\nba^r", 2, "\n  bar", false);
    }

    public void testSetLineIndentation5() throws Exception {
        checkSetIndentation("\nbar^\n", 2, "\n  bar\n", false);
    }

    public void testSetLineIndentation6() throws Exception {
        checkSetIndentation("\n^", 8, "\n\t", true);
    }

    public void testSetLineIndentation6b() throws Exception {
        checkSetIndentation("\n^\n", 8, "\n\t\n", true);
    }

    public void testSetLineIndentation6c() throws Exception {
        checkSetIndentation("\n^                       \n", 8, "\n\t\n", true);
    }

    public void testSetLineIndentation6d() throws Exception {
        checkSetIndentation("\n                       ^\n", 8, "\n\t\n", true);
    }

    public void testSetLineIndentation7() throws Exception {
        checkSetIndentation("\n^", 10, "\n\t  ", true);
    }

    public void testSetLineIndentation7b() throws Exception {
        checkSetIndentation("\n^\n", 10, "\n\t  \n", true);
    }

    public void testSetLineIndentation8() throws Exception {
        checkSetIndentation("\n^", 16, "\n\t\t", true);
    }

    public void testSetLineIndentation8b() throws Exception {
        checkSetIndentation("\n^\n", 16, "\n\t\t\n", true);
    }

    public void testSetLineIndentation9() throws Exception {
        checkSetIndentation("\n^foo\n", 16, "\n\t\tfoo\n", true);
    }

    public void testSetLineIndentation10() throws Exception {
        checkSetIndentation("\n^\tfoo\n", 6, "\n      foo\n", true);
    }

    public void testSetLineIndentation11() throws Exception {
        checkSetIndentation("^        ", 1, " ", false);
    }

    public void testSetLineIndentation12() throws Exception {
        checkSetIndentation("^        ", 0, "", false);
    }

    public void testSetLineIndentation13() throws Exception {
        checkSetIndentation("^        f", 0, "f", false);
    }

    public void testGetRowStart() throws Exception {
        assertEquals(0, GsfUtilities.getRowStart("", 0));
        assertEquals(0, GsfUtilities.getRowStart("abc", 0));
        assertEquals(0, GsfUtilities.getRowStart("abc", 1));
        assertEquals(0, GsfUtilities.getRowStart("abc\n", 2));
        assertEquals(4, GsfUtilities.getRowStart("abc\n", 4));
        assertEquals(4, GsfUtilities.getRowStart("abc\nab\n", 4));
        assertEquals(4, GsfUtilities.getRowStart("abc\nab\n", 5));
        assertEquals(4, GsfUtilities.getRowStart("abc\nab\n", 6));
        assertEquals(4, GsfUtilities.getRowStart("abc\n\n", 4));
        assertEquals(4, GsfUtilities.getRowStart("abc\na\rb\n", 4));
        assertEquals(4, GsfUtilities.getRowStart("abc\na\rb\n", 6));
    }

    public void testGetRowLastNonWhite() throws Exception {
        assertEquals(-1, GsfUtilities.getRowLastNonWhite("", 0));
        assertEquals(2, GsfUtilities.getRowLastNonWhite("abc", 0));
        assertEquals(2, GsfUtilities.getRowLastNonWhite("abc\r", 3));
        assertEquals(2, GsfUtilities.getRowLastNonWhite("abc\r\n", 3));
        assertEquals(2, GsfUtilities.getRowLastNonWhite("abc       ", 10));
        assertEquals(2, GsfUtilities.getRowLastNonWhite("abc       ", 5));
        assertEquals(7, GsfUtilities.getRowLastNonWhite("\ndef\nabc\r", 6));
        assertEquals(-1, GsfUtilities.getRowLastNonWhite("x\n", 2));
    }

    public void testGetRowFirstNonWhite() throws Exception {
        assertEquals(-1, GsfUtilities.getRowFirstNonWhite("", 0));
        assertEquals(-1, GsfUtilities.getRowFirstNonWhite("   ", 0));
        assertEquals(-1, GsfUtilities.getRowFirstNonWhite("abc\r\n", 5));
        assertEquals(-1, GsfUtilities.getRowFirstNonWhite("\nabc", 0));
        assertEquals(4, GsfUtilities.getRowFirstNonWhite("    a", 0));
        assertEquals(6, GsfUtilities.getRowFirstNonWhite("\r\n    a", 2));
        assertEquals(6, GsfUtilities.getRowFirstNonWhite("\r\n    a", 4));
        assertEquals(6, GsfUtilities.getRowFirstNonWhite("\r\n    a", 6));
        assertEquals(2, GsfUtilities.getRowFirstNonWhite("\r\nxy", 4));
    }

    public void testIsRowEmpty() throws Exception {
        // TODO - test fake \r without \n's in there
        assertTrue(GsfUtilities.isRowEmpty("", 0));
        assertTrue(GsfUtilities.isRowEmpty("a\n\n", 2));
        assertTrue(GsfUtilities.isRowEmpty("a\n\n", 3));
        assertTrue(GsfUtilities.isRowEmpty("\n", 0));
        assertTrue(GsfUtilities.isRowEmpty("a\n\r\n", 2));
        assertFalse(GsfUtilities.isRowEmpty("a", 0));
        assertFalse(GsfUtilities.isRowEmpty("a", 1));
        assertFalse(GsfUtilities.isRowEmpty("ab", 1));
        assertFalse(GsfUtilities.isRowEmpty("ab\n", 2));
        assertFalse(GsfUtilities.isRowEmpty("ab\n", 0));
    }

    public void testIsRowWhite() throws Exception {
        assertTrue(GsfUtilities.isRowWhite("", 0));
        assertTrue(GsfUtilities.isRowWhite("  ", 0));
        assertTrue(GsfUtilities.isRowWhite("  ", 1));
        assertTrue(GsfUtilities.isRowWhite("  ", 2));
        assertFalse(GsfUtilities.isRowWhite("a ", 2));
        assertFalse(GsfUtilities.isRowWhite("a ", 1));
        assertFalse(GsfUtilities.isRowWhite("a ", 0));
        assertTrue(GsfUtilities.isRowWhite("\n  \n", 0));
        assertTrue(GsfUtilities.isRowWhite("\n  \n", 1));
        assertTrue(GsfUtilities.isRowWhite("\n  \r\n", 1));
        assertTrue(GsfUtilities.isRowWhite("a\n  \r\n", 2));
        assertFalse(GsfUtilities.isRowWhite("a\na  \r\n", 2));
        assertFalse(GsfUtilities.isRowWhite("a\na  \r\n", 3));
        assertFalse(GsfUtilities.isRowWhite("a\n  a\r\n", 2));
    }

    public void testEndsWith() throws Exception {
        assertTrue(GsfUtilities.endsWith(new StringBuilder("hello"), "lo"));
        assertTrue(GsfUtilities.endsWith(new StringBuilder("hello"), "hello"));
        assertTrue(GsfUtilities.endsWith(new StringBuilder("hello"), ""));
        assertTrue(GsfUtilities.endsWith(new StringBuilder("hello"), "o"));
        assertTrue(GsfUtilities.endsWith(new StringBuilder("<br><br>"), "<br>"));

        assertFalse(GsfUtilities.endsWith(new StringBuilder("hello"), "hallo"));
        assertFalse(GsfUtilities.endsWith(new StringBuilder("hello"), "foohallo"));
        assertFalse(GsfUtilities.endsWith(new StringBuilder(""), "o"));
    }
}
