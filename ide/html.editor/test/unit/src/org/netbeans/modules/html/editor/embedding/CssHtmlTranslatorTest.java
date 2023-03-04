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
package org.netbeans.modules.html.editor.embedding;

import org.netbeans.modules.html.editor.embedding.CssHtmlTranslator;
import java.util.regex.Matcher;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author Marek Fukala
 */
public class CssHtmlTranslatorTest extends TestBase {

    public CssHtmlTranslatorTest(String name) {
        super(name);
    }

    public void testCDATAFilterPattern() {
        String code1 = "<![CDATA[\n @import \"skills/netbeans.css\";\n @import \"skills/common/shared.css\";   ]]>";

        String code2 = "/*<![CDATA[*/\n @import \"skills/netbeans.css\";\n @import \"skills/common/shared.css\";   /*]]>*/";

        Matcher m = CssHtmlTranslator.CDATA_FILTER_PATTERN.matcher(code1);
        assertTrue(m.matches());

        int from = m.start(CssHtmlTranslator.CDATA_BODY_GROUP_INDEX);
        int to = m.end(CssHtmlTranslator.CDATA_BODY_GROUP_INDEX);
        String in = code1.substring(from, to);
        assertEquals(in, "@import \"skills/netbeans.css\";\n @import \"skills/common/shared.css\";");

        m = CssHtmlTranslator.CDATA_FILTER_PATTERN.matcher(code2);
        assertTrue(m.matches());

        from = m.start(CssHtmlTranslator.CDATA_BODY_GROUP_INDEX);
        to = m.end(CssHtmlTranslator.CDATA_BODY_GROUP_INDEX);
        in = code2.substring(from, to);
        assertEquals(in, "@import \"skills/netbeans.css\";\n @import \"skills/common/shared.css\";");

    }

    public void testIllegalCharsInSelectorPattern() {
        assertFalse(CssHtmlTranslator.ILLEGAL_CHARS_IN_SELECTOR.matcher("ble").find());
        assertTrue(CssHtmlTranslator.ILLEGAL_CHARS_IN_SELECTOR.matcher(".ble").find());
        assertTrue(CssHtmlTranslator.ILLEGAL_CHARS_IN_SELECTOR.matcher("b.le").find());
        assertTrue(CssHtmlTranslator.ILLEGAL_CHARS_IN_SELECTOR.matcher("b{le").find());
        assertTrue(CssHtmlTranslator.ILLEGAL_CHARS_IN_SELECTOR.matcher("b{le}").find());
        assertTrue(CssHtmlTranslator.ILLEGAL_CHARS_IN_SELECTOR.matcher("ble:").find());
        assertTrue(CssHtmlTranslator.ILLEGAL_CHARS_IN_SELECTOR.matcher("validate[required,number]").find());
    }
}
