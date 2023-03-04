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

package org.netbeans.modules.php.api.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

public class StringUtilsTest extends NbTestCase {
    private static final String TESTING_SEPARATOR = "??NB??";

    public StringUtilsTest(String name) {
        super(name);
    }

    public void testHasText() {
        assertTrue(StringUtils.hasText("a"));

        assertFalse(StringUtils.hasText(null));
        assertFalse(StringUtils.hasText(""));
        assertFalse(StringUtils.hasText(" "));
    }

    public void testIsEmpty() {
        assertFalse(StringUtils.isEmpty("a"));

        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
    }

    public void testImplode() {
        final List<String> items = Arrays.asList("one", "two");
        assertEquals("one*two", StringUtils.implode(items, "*"));
        assertEquals("oneonetwo", StringUtils.implode(items, "one"));
        assertEquals("one" + TESTING_SEPARATOR + "two", StringUtils.implode(items, TESTING_SEPARATOR));
    }

    public void testExplode() {
        final String[] items = {"one", "two"};
        String string = "one*two";
        assertArrayEquals(items, StringUtils.explode(string, "*").toArray(new String[0]));
        string = "one" + TESTING_SEPARATOR + "two";
        assertArrayEquals(items, StringUtils.explode(string, TESTING_SEPARATOR).toArray(new String[0]));

        // test for empty string (relative path ".")
        string = "one" + TESTING_SEPARATOR + "" + TESTING_SEPARATOR + "two";
        assertArrayEquals(new String[] {"one", "", "two"}, StringUtils.explode(string, TESTING_SEPARATOR).toArray(new String[0]));
    }

    public void testPattern() {
        assertNull(StringUtils.getPattern(""));
        assertNull(StringUtils.getPattern("abc"));

        Pattern pattern = StringUtils.getPattern("a*b?c");
        assertNotNull(pattern);
        assertTrue(pattern.matcher("andhtbXc__").matches());
        assertTrue(pattern.matcher("__abXc").matches());
        assertTrue(pattern.matcher("__ABXC").matches());
        assertTrue(pattern.matcher("andhtbXc__").matches());
        assertFalse(pattern.matcher("andhtbc__").matches());
    }

    public void testExactPattern() {
        assertNull(StringUtils.getPattern(""));
        assertNull(StringUtils.getPattern("abc"));

        Pattern pattern = StringUtils.getExactPattern("a*b?c");
        assertNotNull(pattern);
        assertFalse(pattern.matcher("andhtbXc__").matches());
        assertFalse(pattern.matcher("__abXc").matches());
        assertFalse(pattern.matcher("andhtbXc__").matches());
        assertTrue(pattern.matcher("andhtbXc").matches());
        assertTrue(pattern.matcher("AnGGhtbXC").matches());

        pattern = StringUtils.getExactPattern("*.php");
        assertNotNull(pattern);
        assertTrue(pattern.matcher("test.php").matches());
        assertTrue(pattern.matcher("a.php").matches());
        assertTrue(pattern.matcher("A.PHP").matches());
        assertFalse(pattern.matcher("test.phps").matches());
        assertFalse(pattern.matcher("test.php5").matches());
        assertFalse(pattern.matcher("php").matches());

        pattern = StringUtils.getExactPattern("*.php?");
        assertNotNull(pattern);
        assertFalse(pattern.matcher("test.php").matches());
        assertFalse(pattern.matcher("A.PHP").matches());
        assertTrue(pattern.matcher("test.phps").matches());
        assertTrue(pattern.matcher("test.php5").matches());
        assertTrue(pattern.matcher("TEST.PHP5").matches());
        assertFalse(pattern.matcher("php").matches());
    }

    public void testWebalize() {
        assertEquals("my-super-company", StringUtils.webalize("My SuperCompany"));
        assertEquals("my-super-company", StringUtils.webalize("My_Super_Company"));
        assertEquals("my-super-company", StringUtils.webalize("  My   SuperCompany  "));
        assertEquals("hello9", StringUtils.webalize("hello9"));
    }

    public void testCapitalize() {
        assertEquals("Foobarbaz", StringUtils.capitalize("foobarbaz"));
        assertEquals("Foobarbaz", StringUtils.capitalize("Foobarbaz"));
        assertEquals("FOOBARBAZ", StringUtils.capitalize("FOOBARBAZ"));
        assertEquals("FoobarbaZ", StringUtils.capitalize("foobarbaZ"));
    }

    public void testEmptyCapitalize() {
        try {
            StringUtils.capitalize("");
            fail("Empty text can not be capitalized!");
        } catch (Exception ex) {
        }
    }

    public void testDecapitalize() {
        assertEquals("foobarbaz", StringUtils.decapitalize("foobarbaz"));
        assertEquals("foobarbaz", StringUtils.decapitalize("Foobarbaz"));
        assertEquals("fOOBARBAZ", StringUtils.decapitalize("FOOBARBAZ"));
        assertEquals("foobarbaZ", StringUtils.decapitalize("foobarbaZ"));
    }

    public void testEmptyDecapitalize() {
        try {
            StringUtils.decapitalize("");
            fail("Empty text can not be decapitalized!");
        } catch (Exception ex) {
        }
    }

    public void testTruncate() {
        String string = "0123456789";
        int length = string.length(); // 10

        assertEquals(string, StringUtils.truncate(string, 0, 0, "..."));
        assertEquals(string, StringUtils.truncate(string, 0, 2, "..."));

        assertEquals("...", StringUtils.truncate(string, 0, 3, "..."));
        assertEquals("...", StringUtils.truncate(string, 0, -7, "..."));

        assertEquals("0...", StringUtils.truncate(string, 0, 4, "..."));
        assertEquals("0...", StringUtils.truncate(string, 0, 4, null));
        assertEquals("0...", StringUtils.truncate(string, 0, -6, "..."));
        assertEquals("0...", StringUtils.truncate(string, 0, -6, null));

        assertEquals("1...", StringUtils.truncate(string, 1, 4, "..."));
        assertEquals("1...", StringUtils.truncate(string, -9, 4, "..."));
        assertEquals("1...", StringUtils.truncate(string, 1, -5, "..."));
        assertEquals("1...", StringUtils.truncate(string, -9, -5, "..."));

        assertEquals("01234...", StringUtils.truncate(string, 0, 8, "..."));
        assertEquals("01234...", StringUtils.truncate(string, 0, -2, "..."));

        assertEquals("123456789", StringUtils.truncate(string, 1, 9, "..."));
        assertEquals("123456789", StringUtils.truncate(string, -9, 9, "..."));

        assertEquals("12345...", StringUtils.truncate(string, 1, 8, "..."));
        assertEquals("12345...", StringUtils.truncate(string, -9, 8, "..."));
        assertEquals("12345...", StringUtils.truncate(string, 1, -1, "..."));
        assertEquals("12345...", StringUtils.truncate(string, -9, -1, "..."));

        assertEquals(string, StringUtils.truncate(string, 0, length, "..."));
        assertEquals(string, StringUtils.truncate(string, 0, length + 1, "..."));

        assertEquals("123456789", StringUtils.truncate(string, 1, length + 1, "..."));
        assertEquals("123456789", StringUtils.truncate(string, -9, length + 1, "..."));

        assertEquals("9", StringUtils.truncate(string, 9, 3, "..."));
        assertEquals("9", StringUtils.truncate(string, -1, 3, "..."));

        assertEquals(string, StringUtils.truncate(string, -length - 1, 3, "..."));
        assertEquals(string, StringUtils.truncate(string, 0, -length -1, "..."));

        assertEquals("", StringUtils.truncate(string, 0, 0, ""));
        assertEquals("012", StringUtils.truncate(string, 0, 3, ""));
        assertEquals("012", StringUtils.truncate(string, 0, -7, ""));

        assertEquals("23456", StringUtils.truncate(string, 2, 5, ""));
        assertEquals("23456", StringUtils.truncate(string, -8, 5, ""));
        assertEquals("23456", StringUtils.truncate(string, 2, -3, ""));
        assertEquals("23456", StringUtils.truncate(string, -8, -3, ""));

        assertEquals("012345678", StringUtils.truncate(string, 0, length - 1, ""));
        assertEquals(string, StringUtils.truncate(string, 0, length, ""));
        assertEquals(string, StringUtils.truncate(string, 0, length + 1, ""));

        assertEquals("", StringUtils.truncate("", 0, 0, "..."));
        assertEquals("", StringUtils.truncate("", 0, 3, "..."));
        assertEquals("", StringUtils.truncate("", 0, 4, "..."));
        assertEquals("", StringUtils.truncate("", 0, -1, "..."));
        assertEquals("", StringUtils.truncate("", 1, 0, "..."));
        assertEquals("", StringUtils.truncate("", 1, -1, "..."));
        assertEquals("", StringUtils.truncate("", -1, 0, "..."));
        assertEquals("", StringUtils.truncate("", -1, 1, "..."));
        assertEquals("", StringUtils.truncate("", -1, -1, "..."));

        assertEquals("", StringUtils.truncate("", 0, 0, ""));
        assertEquals("", StringUtils.truncate("", 0, 3, ""));
        assertEquals("", StringUtils.truncate("", 0, 4, ""));
        assertEquals("", StringUtils.truncate("", 0, -1, ""));
        assertEquals("", StringUtils.truncate("", 1, 0, ""));
        assertEquals("", StringUtils.truncate("", 1, -1, ""));
        assertEquals("", StringUtils.truncate("", -1, 0, ""));
        assertEquals("", StringUtils.truncate("", -1, 1, ""));
        assertEquals("", StringUtils.truncate("", -1, -1, ""));
    }
}
