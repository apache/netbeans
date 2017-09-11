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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
