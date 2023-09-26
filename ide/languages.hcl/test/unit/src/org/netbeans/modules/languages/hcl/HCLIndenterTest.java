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
package org.netbeans.modules.languages.hcl;

import java.util.regex.Pattern;
import javax.swing.text.PlainDocument;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;
import org.junit.Test;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;

/**
 *
 * @author lkishalmi
 */
public class HCLIndenterTest extends NbTestCase {
    
    private PlainDocument doc;

    public HCLIndenterTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        doc = new PlainDocument();
        doc.putProperty("mimeType", "text/x-hcl");
        doc.putProperty(Language.class, HCLLanguage.language);
        CodeStylePreferences.get(doc).getPreferences().putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 2);
    }

    @Test
    public void testEmptyNL1() throws Exception {
        performNewLineIndentationTest("|", "\n");
    }

    @Test
    public void testEmptyNL2() throws Exception {
        performNewLineIndentationTest("\n|", "\n\n");
    }
    
    @Test
    public void testEmptyNL3() throws Exception {
        performNewLineIndentationTest("  |", "  \n");
    }

    @Test
    public void testIndentedNL1() throws Exception {
        performNewLineIndentationTest("  a = 1\n|", "  a = 1\n\n  ");
    }
    
    @Test
    public void testIndentedNL2() throws Exception {
        performNewLineIndentationTest("  a = 1\n \n|", "  a = 1\n \n\n  ");
    }

    @Test
    public void testIndentedBlockNL1() throws Exception {
        performNewLineIndentationTest("locals {|}", "locals {\n}");
    }

    @Test
    public void testIndentedBlockNL2() throws Exception {
        performNewLineIndentationTest("locals {\n  a = [|]}", "locals {\n  a = [\n]}");
    }

    @Test
    public void testIndentedBlockNL3() throws Exception {
        performNewLineIndentationTest("locals {\n  a = [|\n  ]\n}", "locals {\n  a = [\n    \n  ]\n}");
    }

    @Test
    public void testIndentedBlockNL4() throws Exception {
        performNewLineIndentationTest("locals {\n\n|\n}", "locals {\n\n\n  \n}");
    }

    
    @Test
    public void testEmpty1() throws Exception {
        performLineIndentationTest("|", "");
    }
    
    @Test
    public void testEmpty2() throws Exception {
        performLineIndentationTest("\n|\n", "\n\n");
    }

    @Test
    public void testEmpty3() throws Exception {
        performLineIndentationTest("  \n|\n", "  \n\n");
    }

    @Test
    public void testEmpty4() throws Exception {
        performLineIndentationTest("  |\n\n", "\n\n");
    }

    @Test
    public void testEmptyLine() throws Exception {
        performLineIndentationTest("locals {\n  |\n}\n", "locals {\n\n}\n");
    }

    public void testReindent1() throws Exception {
        performSpanIndentationTest("|   locals {\n a = 1\n   \nb = 2\n}|\n", "locals {\n  a = 1\n\n  b = 2\n}\n");
    }
    
    public void testReindent2() throws Exception {
        performSpanIndentationTest("|a = {\nb = [[\n\"one\"\n], [\n\"two\"\n]]\n}|\n", "a = {\n  b = [[\n      \"one\"\n    ], [\n      \"two\"\n  ]]\n}\n");
    }
    
    public void testReindentHeredoc() throws Exception {
        performSpanIndentationTest("|a = <<-EOT\n    This\n    multi\n    line\nEOT|\n", "a = <<-EOT\n    This\n    multi\n    line\nEOT\n");
    }
    
    private void performNewLineIndentationTest(String code, String golden) throws Exception {
        int pos = code.indexOf('|');

        assertNotSame(-1, pos);

        code = code.replaceAll(Pattern.quote("|"), "");

        doc.insertString(0, code, null);
        Indent indent = Indent.get(doc);
        indent.lock();
        indent.indentNewLine(pos);
        indent.unlock();
        assertEquals(golden, doc.getText(0, doc.getLength()));
    }

    private void performLineIndentationTest(String code, String golden) throws Exception {
        int pos = code.indexOf('|');

        assertNotSame(-1, pos);

        code = code.replaceAll(Pattern.quote("|"), "");

        doc.insertString(0, code, null);
        Indent indent = Indent.get(doc);
        indent.lock();
        indent.reindent(pos);
        indent.unlock();
        assertEquals(golden, doc.getText(0, doc.getLength()));
    }

    private void performSpanIndentationTest(String code, String golden) throws Exception {
        String[] parts = code.split(Pattern.quote("|"));

        assertEquals(3, parts.length);

        int start = parts[0].length();
        int end = start + parts[1].length();

        code = parts[0] + parts[1] + parts[2];

        doc.insertString(0, code, null);
        Indent indent = Indent.get(doc);
        indent.lock();
        indent.reindent(start, end);
        indent.unlock();
        assertEquals(golden, doc.getText(0, doc.getLength()));
    }

}
