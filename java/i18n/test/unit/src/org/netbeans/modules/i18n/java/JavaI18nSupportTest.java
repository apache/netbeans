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

package org.netbeans.modules.i18n.java;

import java.lang.reflect.Method;
import org.netbeans.junit.NbTestCase;

/**
 * @author  Marian Petras
 */
public class JavaI18nSupportTest extends NbTestCase {

    private Method decoderMethod;

    public JavaI18nSupportTest() {
        super("JavaI18nSupport test");
    }

    @Override
    public void setUp() {
        decoderMethod = null;
    }

    public void testDecodeUnicodeSeq() throws Exception {
        initDecoderMethod();

        /* empty character: */
        checkTranslation("", "");

        /* ISO control characters: */
        checkTranslation("\\u0000", "\\u0000");
        checkTranslation("\\u0001", "\\u0001");
        checkTranslation("\\u0002", "\\u0002");
        checkTranslation("\\u0003", "\\u0003");
        checkTranslation("\\u0004", "\\u0004");
        checkTranslation("\\u0005", "\\u0005");
        checkTranslation("\\u0006", "\\u0006");
        checkTranslation("\\u0007", "\\u0007");
        checkTranslation("\\u0008", "\\b");         //bell
        checkTranslation("\\u0009", "\\t");         //tab
        checkTranslation("\\u000a", "\\n");         //new line (NL)
        checkTranslation("\\u000b", "\\u000b");
        checkTranslation("\\u000c", "\\f");         //form-feed (FF)
        checkTranslation("\\u000d", "\\r");         //carriage-return (CR)
        checkTranslation("\\u000e", "\\u000e");
        checkTranslation("\\u000f", "\\u000f");
        checkTranslation("\\u0010", "\\u0010");
        checkTranslation("\\u0011", "\\u0011");
        checkTranslation("\\u0012", "\\u0012");
        checkTranslation("\\u0013", "\\u0013");
        checkTranslation("\\u0014", "\\u0014");
        checkTranslation("\\u0015", "\\u0015");
        checkTranslation("\\u0016", "\\u0016");
        checkTranslation("\\u0017", "\\u0017");
        checkTranslation("\\u0018", "\\u0018");
        checkTranslation("\\u0019", "\\u0019");
        checkTranslation("\\u001a", "\\u001a");
        checkTranslation("\\u001b", "\\u001b");
        checkTranslation("\\u001c", "\\u001c");
        checkTranslation("\\u001d", "\\u001d");
        checkTranslation("\\u001e", "\\u001e");
        checkTranslation("\\u001f", "\\u001f");
        checkTranslation("\\u007f", "\\u007f");

        /* common characters */
        checkTranslation("\\u0020", "\u0020");
        checkTranslation("\\u0024", "\u0024");
        checkTranslation("\\u0079", "\u0079");
        checkTranslation("\\u007e", "\u007e");
        checkTranslation("\\u0080", "\u0080");
        checkTranslation("\\u0084", "\u0084");
        checkTranslation("\\u0100", "\u0100");
        checkTranslation("\\u1000", "\u1000");

        /* multi-u sequences: */
        checkTranslation("\\uu0020", "\uu0020");
        checkTranslation("\\uu0020", "\u0020");
        checkTranslation("\\uuu0024", "\uuu0024");
        checkTranslation("\\uuu0024", "\u0024");
        checkTranslation("\\uuuu0079", "\uuuu0079");
        checkTranslation("\\uuuu0079", "\u0079");
        checkTranslation("\\uuuuu007e", "\uuuuu007e");
        checkTranslation("\\uuuuu007e", "\u007e");

        /* Unicode sequence among plain characters: */
        checkTranslation("a", "a");
        checkTranslation("ab", "ab");
        checkTranslation("abc", "abc");
        checkTranslation("\\u1234b", "\u1234b");
        checkTranslation("a\\u1234", "a\u1234");
        checkTranslation("a\\u1234b", "a\u1234b");
        checkTranslation("\\\\u1234b", "\\\\u1234b");

        /* backslash in various contexts: */
        checkTranslation("\\", "\\");
        checkTranslation("a\\", "a\\");
        checkTranslation("\\b", "\\b");
        checkTranslation("\\bcdefg", "\\bcdefg");

        /* broken Unicode sequences: */
        checkTranslation("\\u", "\\u");
        checkTranslation("\\u1", "\\u1");
        checkTranslation("\\u12", "\\u12");
        checkTranslation("\\u123", "\\u123");
        checkTranslation("a\\u", "a\\u");
        checkTranslation("a\\u1", "a\\u1");
        checkTranslation("a\\u12", "a\\u12");
        checkTranslation("a\\u123", "a\\u123");
        checkTranslation("\\ux", "\\ux");
        checkTranslation("\\u1x", "\\u1x");
        checkTranslation("\\u12x", "\\u12x");
        checkTranslation("\\u123x", "\\u123x");
        checkTranslation("\\u123xy", "\\u123xy");
        checkTranslation("\\u123xyz", "\\u123xyz");

        /* Octal value escape sequences: */
        checkTranslation("\\000", "\\u0000");
        checkTranslation("\\001", "\\u0001");
        checkTranslation("\\002", "\\u0002");
        checkTranslation("\\003", "\\u0003");
        checkTranslation("\\004", "\\u0004");
        checkTranslation("\\005", "\\u0005");
        checkTranslation("\\006", "\\u0006");
        checkTranslation("\\007", "\\u0007");
        checkTranslation("\\010", "\\b");         //bell
        checkTranslation("\\011", "\\t");         //tab
        checkTranslation("\\012", "\\n");         //new line (NL)
        checkTranslation("\\013", "\\u000b");
        checkTranslation("\\014", "\\f");         //form-feed (FF)
        checkTranslation("\\015", "\\r");         //carriage-return (CR)
        checkTranslation("\\016", "\\u000e");
        checkTranslation("\\017", "\\u000f");
        checkTranslation("\\020", "\\u0010");
        checkTranslation("\\021", "\\u0011");
        checkTranslation("\\022", "\\u0012");
        checkTranslation("\\023", "\\u0013");
        checkTranslation("\\024", "\\u0014");
        checkTranslation("\\025", "\\u0015");
        checkTranslation("\\026", "\\u0016");
        checkTranslation("\\027", "\\u0017");
        checkTranslation("\\030", "\\u0018");
        checkTranslation("\\031", "\\u0019");
        checkTranslation("\\032", "\\u001a");
        checkTranslation("\\033", "\\u001b");
        checkTranslation("\\034", "\\u001c");
        checkTranslation("\\035", "\\u001d");
        checkTranslation("\\036", "\\u001e");
        checkTranslation("\\037", "\\u001f");
        checkTranslation("\\177", "\\u007f");

        /* octal value sequences - common characters */
        checkTranslation("\\040", "\u0020");
        checkTranslation("\\044", "\u0024");
        checkTranslation("\\171", "\u0079");
        checkTranslation("\\176", "\u007e");
        checkTranslation("\\200", "\u0080");
        checkTranslation("\\204", "\u0084");

        /* octal value sequences - various lengths: */
        checkTranslation("\\001", "\\u0001");
        checkTranslation("\\01", "\\u0001");
        checkTranslation("\\1", "\\u0001");
        checkTranslation("\\044", "\u0024");
        checkTranslation("\\37", "\\u001f");

        /* octal value sequences among plain characters: */
        checkTranslation("\\1b", "\\u0001b");
        checkTranslation("\\01b", "\\u0001b");
        checkTranslation("\\001b", "\\u0001b");
        checkTranslation("\\37b", "\\u001fb");
        checkTranslation("\\141b", "\u0061b");
        checkTranslation("\\171b", "\u0079b");
        checkTranslation("a\\171", "a\u0079");
        checkTranslation("a\\171b", "a\u0079b");
        checkTranslation("\\08a", "\\u00008a");   //"8a" is not part of the seq.
        checkTranslation("\\08k", "\\u00008k");   //"8k" is not part of the seq.
        checkTranslation("\\08z", "\\u00008z");   //"8z" is not part of the seq.
        checkTranslation("\\168a", "\\u000e8a");  //"8a" is not part of the seq.
        checkTranslation("\\168k", "\\u000e8k");  //"8k" is not part of the seq.
        checkTranslation("\\168z", "\\u000e8z");  //"8z" is not part of the seq.

        /* false octal value sequences: */
        checkTranslation("\\\\171b", "\\\\171b");

        /* broken octal value sequences: */
        checkTranslation("\\4", "\\4");
        checkTranslation("\\45", "\\45");
        checkTranslation("\\456", "\\456");
        checkTranslation("\\4567", "\\4567");
        checkTranslation("\\4567t", "\\4567t");
        checkTranslation("\\8", "\\8");
        checkTranslation("\\80", "\\80");
        checkTranslation("\\800", "\\800");
        checkTranslation("\\800k", "\\800k");
        checkTranslation("\\8a", "\\8a");
        checkTranslation("\\8k", "\\8k");
        checkTranslation("\\8z", "\\8z");

        /* escape characters: */
        checkTranslation("\\b", "\\b");
        checkTranslation("\\t", "\\t");
        checkTranslation("\\n", "\\n");
        checkTranslation("\\f", "\\f");
        checkTranslation("\\r", "\\r");
        checkTranslation("\\'", "\\'");
        checkTranslation("\\\"", "\\\"");

        /* illegal escape characters: */
        checkTranslation("\\a", "\\a");
        checkTranslation("\\c", "\\c");
        checkTranslation("\\d", "\\d");
        checkTranslation("\\x", "\\x");
        checkTranslation("\\z", "\\z");
        checkTranslation("\\abcd", "\\abcd");
    }

    private void checkTranslation(String origText,
                                  String expectedTranslation) throws Exception {
        String actualTranslation = (String) decoderMethod.invoke(null, origText);
        assertEquals(expectedTranslation, actualTranslation);
    }

    private void initDecoderMethod() throws Exception {
        Class<JavaI18nSupport> cls = JavaI18nSupport.class;
        decoderMethod = cls.getDeclaredMethod("decodeUnicodeSeq",
                                              String.class);
        decoderMethod.setAccessible(true);
    }

}
