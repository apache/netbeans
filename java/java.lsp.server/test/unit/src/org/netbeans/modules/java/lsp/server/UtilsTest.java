/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

/**
 *
 * @author Martin Entlicher
 */
public class UtilsTest {

    @Test
    public void testEscapeScompletionSnippetSpceialChars() {
        assertEquals("", Utils.escapeCompletionSnippetSpecialChars(""));
        assertEquals("a", Utils.escapeCompletionSnippetSpecialChars("a"));
        assertEquals("\\$", Utils.escapeCompletionSnippetSpecialChars("$"));
        assertEquals("{\\}", Utils.escapeCompletionSnippetSpecialChars("{}"));
        assertEquals("\\${\\}", Utils.escapeCompletionSnippetSpecialChars("${}"));
        assertEquals("\\}", Utils.escapeCompletionSnippetSpecialChars("}"));
        assertEquals("\\$\\${{\\}\\}", Utils.escapeCompletionSnippetSpecialChars("$${{}}"));
        assertEquals("a\\$\n\\}", Utils.escapeCompletionSnippetSpecialChars("a$\n}"));
        assertEquals("\\\\a", Utils.escapeCompletionSnippetSpecialChars("\\a"));

        String nonEscapedStringNotChanged = new String("abcdef");
        assertSame(nonEscapedStringNotChanged, Utils.escapeCompletionSnippetSpecialChars(nonEscapedStringNotChanged));
    }

    @Test
    public void testEncode2JSON() {
        assertEquals("", Utils.encode2JSON(""));
        assertEquals("abcd", Utils.encode2JSON("abcd"));
        assertEquals("'\\\"\\b\\t\\n\\r\\\\", Utils.encode2JSON("'\"\b\t\n\r\\"));
    }

    @Test
    public void testStripHtml() {
        String s = "<div>Pre <span>Text</span> Post</div>";
        String expResult = "Pre Text Post";
        String result = Utils.html2plain(s);
        assertEquals(expResult, result);
    }
    
    /**
     * All newlines should be removed
     */
    @Test
    public void testStripNewlines() {
        String s = "\n<div>Pre <span\n>\nText</span> Post\n</div>";
        String expResult = "Pre Text Post";
        String result = Utils.html2plain(s, true);
        assertEquals(expResult, result);
    }
    
    
    /**
     * Consecutive whitespaces should be collapsed to a single space. Leading/trailing whitespaces
     * removed.
     */
    @Test
    public void testStripConsecutiveWhitespces() {
        String s = "\t <div> Pre <span> Text\t </span>\t\t Post </div>\t";
        String expResult = "Pre Text Post";
        String result = Utils.html2plain(s, true);
        assertEquals(expResult, result);
    }
}
