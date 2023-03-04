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
package org.netbeans.modules.php.editor.lexer;

public class PHP73FeaturesTest extends PHPLexerTestBase {

    public PHP73FeaturesTest(String testName) {
        super(testName);
    }

    // HEREDOC Closing Marker Indentation
    public void testFlexibleHeredocIndentationSpaces_01() throws Exception {
        performTest("lexer/php73/heredoc_indent_spaces_01");
    }

    public void testFlexibleHeredocIndentationSpaces_02() throws Exception {
        performTest("lexer/php73/heredoc_indent_spaces_02");
    }

    public void testFlexibleHeredocIndentationSpaces_03() throws Exception {
        performTest("lexer/php73/heredoc_indent_spaces_03");
    }

    public void testFlexibleHeredocIndentationSpaces_04() throws Exception {
        // no newline at EOF
        performTest("lexer/php73/heredoc_indent_spaces_04");
    }

    public void testFlexibleHeredocIndentationTabs_01() throws Exception {
        performTest("lexer/php73/heredoc_indent_tabs_01");
    }

    public void testFlexibleHeredocIndentationTabs_02() throws Exception {
        performTest("lexer/php73/heredoc_indent_tabs_02");
    }

    public void testFlexibleHeredocIndentationTabs_03() throws Exception {
        performTest("lexer/php73/heredoc_indent_tabs_03");
    }

    // HEREDOC Closing Marker New Line
    public void testFlexibleHeredocNewLine_01() throws Exception {
        performTest("lexer/php73/heredoc_new_line_01");
    }

    public void testFlexibleHeredocNewLine_02() throws Exception {
        performTest("lexer/php73/heredoc_new_line_02");
    }

    public void testFlexibleHeredocNewLine_03() throws Exception {
        performTest("lexer/php73/heredoc_new_line_03");
    }

    public void testFlexibleHeredocNewLine_04() throws Exception {
        performTest("lexer/php73/heredoc_new_line_04");
    }

    // HEREDOC Mixed
    public void testFlexibleHeredocMixed_01() throws Exception {
        performTest("lexer/php73/heredoc_mixed_01");
    }

    public void testFlexibleHeredocMixed_02() throws Exception {
        performTest("lexer/php73/heredoc_mixed_02");
    }

    public void testFlexibleHeredocMixed_03() throws Exception {
        // no new line at EOF
        performTest("lexer/php73/heredoc_mixed_03");
    }

    // NOWDOC Closing Marker Indentation
    public void testFlexibleNowdocIndentationSpaces_01() throws Exception {
        performTest("lexer/php73/nowdoc_indent_spaces_01");
    }

    public void testFlexibleNowdocIndentationSpaces_02() throws Exception {
        performTest("lexer/php73/nowdoc_indent_spaces_02");
    }

    public void testFlexibleNowdocIndentationSpaces_03() throws Exception {
        performTest("lexer/php73/nowdoc_indent_spaces_03");
    }

    public void testFlexibleNowdocIndentationSpaces_04() throws Exception {
        // no newline at EOF
        performTest("lexer/php73/nowdoc_indent_spaces_04");
    }

    public void testFlexibleNowdocIndentationTabs_01() throws Exception {
        performTest("lexer/php73/nowdoc_indent_tabs_01");
    }

    public void testFlexibleNowdocIndentationTabs_02() throws Exception {
        performTest("lexer/php73/nowdoc_indent_tabs_02");
    }

    public void testFlexibleNowdocIndentationTabs_03() throws Exception {
        performTest("lexer/php73/nowdoc_indent_tabs_03");
    }

    // NOWDOC Closing Marker New Line
    public void testFlexibleNowdocNewLine_01() throws Exception {
        performTest("lexer/php73/nowdoc_new_line_01");
    }

    public void testFlexibleNowdocNewLine_02() throws Exception {
        performTest("lexer/php73/nowdoc_new_line_02");
    }

    public void testFlexibleNowdocNewLine_03() throws Exception {
        performTest("lexer/php73/nowdoc_new_line_03");
    }

    public void testFlexibleNowdocNewLine_04() throws Exception {
        performTest("lexer/php73/nowdoc_new_line_04");
    }

    // NOWDOC Mixed
    public void testFlexibleNowdocMixed_01() throws Exception {
        performTest("lexer/php73/nowdoc_mixed_01");
    }

    public void testFlexibleNowdocMixed_02() throws Exception {
        performTest("lexer/php73/nowdoc_mixed_02");
    }

    public void testFlexibleNowdocMixed_03() throws Exception {
        // no new line at EOF
        performTest("lexer/php73/nowdoc_mixed_03");
    }

}
