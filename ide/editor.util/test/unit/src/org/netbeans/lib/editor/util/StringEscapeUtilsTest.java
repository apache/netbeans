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
package org.netbeans.lib.editor.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringEscapeUtilsTest {

    public StringEscapeUtilsTest() {
    }

    @Test
    public void testBug223389() {
        String text = "a < b < c > d > e";
        String escapedText = "a &lt; b &lt; c &gt; d &gt; e";
        checkEscapeAndUnEscape(text, escapedText);
    }


    @Test
    public void testBasicEscaping() {
        String text = "";
        String escapedText = "";
        checkEscapeAndUnEscape(text, escapedText);

        text = "test";
        escapedText = "test";
        checkEscapeAndUnEscape(text, escapedText);

        text = "a < b && c > d";
        escapedText = "a &lt; b &amp;&amp; c &gt; d";
        checkEscapeAndUnEscape(text, escapedText);

        text = "\" test \"";
        escapedText = "&quot; test &quot;";
        checkEscapeAndUnEscape(text, escapedText);

        text = " &";
        escapedText = " &amp;";
        checkEscapeAndUnEscape(text, escapedText);

    }

    private void checkEscapeAndUnEscape(String text, String escapedText) {
        assertEquals("Wrong escaping", escapedText, StringEscapeUtils.escapeHtml(text));
        assertEquals("Wrong unescaping", text, StringEscapeUtils.unescapeHtml(escapedText));
        assertEquals("Wrong identity check", text, StringEscapeUtils.unescapeHtml(StringEscapeUtils.escapeHtml(text)));
    }


}
