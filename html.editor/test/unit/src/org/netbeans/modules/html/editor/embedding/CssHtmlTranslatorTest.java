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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
