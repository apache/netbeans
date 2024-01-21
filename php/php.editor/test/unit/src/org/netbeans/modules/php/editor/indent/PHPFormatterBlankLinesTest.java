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
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPFormatterBlankLinesTest extends PHPFormatterTestBase {

    public PHPFormatterBlankLinesTest(String testName) {
        super(testName);
    }

    public void testIssue181003_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue181003_01.php", options);
    }

    public void testIssue181003_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_02.php", options);
    }

    public void testIssue181003_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_03.php", options);
    }

    public void testIssue181003_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_04.php", options);
    }

    public void testIssue186461_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186461_01.php", options);
    }

    public void testIssue186461_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186461_02.php", options);
    }

    public void testIssue186738_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186738_01.php", options);
    }

    public void testIssue187264_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue187264_01.php", options);
    }

    public void testIssue187264_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue187264_02.php", options);
    }

    public void testIssue201994() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue201994_01.php", options);
    }

    public void testTraitUsesBlankLines_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE_TRAIT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TraitUses01.php", options);
    }

    public void testTraitUsesBlankLines_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE_TRAIT, 1);
        reformatFileContents("testfiles/formatting/blankLines/TraitUses02.php", options);
    }

    public void testBracePlacement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement01.php", options);
    }

    public void testBracePlacement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement02.php", options);
    }

    public void testBracePlacement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement03.php", options);
    }

    public void testAlternativeSyntaxPlacement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_MAX_PRESERVED, 2);
        reformatFileContents("testfiles/formatting/blankLines/AlternativeSyntaxPlacement01.php", options);
    }

    // blank lines
    public void testBLClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Class01.php", options);
    }

    public void testBLTrait01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Trait01.php", options);
    }

    public void testBLAnonymousClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_MAX_PRESERVED, 6);
        reformatFileContents("testfiles/formatting/blankLines/AnonymousClass01.php", options);
    }

    public void testBLClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/blankLines/Class02.php", options);
    }

    public void testBLTrait02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/blankLines/Trait02.php", options);
    }

    public void testBLAnonymousClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.ANONYMOUS_CLASS_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/blankLines/AnonymousClass02.php", options);
    }

    public void testBLClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/blankLines/Class03.php", options);
    }

    public void testBLTrait03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/blankLines/Trait03.php", options);
    }

    public void testBLAnonymousClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.ANONYMOUS_CLASS_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/blankLines/AnonymousClass03.php", options);
    }


    public void testBLFields01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields01.php", options);
    }

    public void testBLFields02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields02.php", options);
    }

    public void testBLFields03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields03.php", options);
    }

    public void testBLFields04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields04.php", options);
    }

    public void testBLFields05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields05.php", options);
    }

    public void testBLFields06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields06.php", options);
    }

    public void testBLFields07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 3);
        reformatFileContents("testfiles/formatting/blankLines/Fields07.php", options);
    }

    public void testBLFields08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/Fields08.php", options);
    }

    public void testBLFields09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/Fields09.php", options);
    }

    public void testBLFields10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/Fields10.php", options);
    }

    public void testBLFields11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 2);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/Fields11.php", options);
    }

    public void testBLFunction01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function01.php", options);
    }

    public void testBLFunction02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function02.php", options);
    }

    public void testBLFunction04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function04.php", options);
    }

    public void testBLNamespace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace01.php", options);
    }

    public void testBLNamespace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace02.php", options);
    }

    public void testBLNamespace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace03.php", options);
    }

    public void testBLSimpleClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass01.php", options);
    }

    public void testBLSimpleClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass02.php", options);
    }

    public void testBLSimpleClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass03.php", options);
    }

    public void testBLSimpleClass04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass04.php", options);
    }

    public void testBLSimpleClass05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass05.php", options);
    }

    public void testBLSimpleClass06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass06.php", options);
    }

    public void testBLSimpleClass07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass07.php", options);
    }

    public void testBLSimpleClass08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass08.php", options);
    }

    public void testBLSimpleClass09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass09.php", options);
    }

    public void testBLSimpleClass10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass10.php", options);
    }

    public void testBLSimpleClass11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass11.php", options);
    }

    public void testBLSimpleClass12() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass12.php", options);
    }

    public void testBLSimpleClass13() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass13.php", options);
    }

    public void testBLSimpleClass14() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass14.php", options);
    }

    public void testBLSimpleClass15() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass15.php", options);
    }

    public void testBLSimpleClass16() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass16.php", options);
    }

    public void testBLSimpleClass17() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass17.php", options);
    }

    public void testBLSimpleTrait01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait01.php", options);
    }

    public void testBLSimpleTrait02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait02.php", options);
    }

    public void testBLSimpleTrait03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait03.php", options);
    }

    public void testBLSimpleTrait04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait04.php", options);
    }

    public void testBLSimpleTrait05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait05.php", options);
    }

    public void testBLSimpleTrait06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait06.php", options);
    }

    public void testBLSimpleTrait07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait07.php", options);
    }

    public void testBLSimpleTrait08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait08.php", options);
    }

    public void testBLSimpleTrait09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait09.php", options);
    }

    public void testBLSimpleTrait10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait10.php", options);
    }

    public void testBLSimpleTrait11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait11.php", options);
    }

    public void testBLSimpleTrait12() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait12.php", options);
    }

    public void testBLSimpleTrait13() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait13.php", options);
    }

    public void testBLSimpleTrait14() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait14.php", options);
    }

    public void testBLSimpleTrait15() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait15.php", options);
    }

    public void testBLSimpleTrait16() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait16.php", options);
    }

    public void testBLSimpleTrait17() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait17.php", options);
    }

    public void testBLSimpleAnonymousClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass01.php", options);
    }

    public void testBLSimpleAnonymousClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass02.php", options);
    }

    public void testBLSimpleAnonymousClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass03.php", options);
    }

    public void testBLSimpleAnonymousClass04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass04.php", options);
    }

    public void testBLSimpleAnonymousClass05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass05.php", options);
    }

    public void testBLSimpleAnonymousClass06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass06.php", options);
    }

    public void testBLSimpleAnonymousClass07() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass07.php", options);
    }

    public void testBLSimpleAnonymousClass08() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 1); // ignore
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass08.php", options);
    }

    public void testBLSimpleAnonymousClass09() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass09.php", options);
    }

    public void testBLSimpleAnonymousClass10() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass10.php", options);
    }

    public void testBLSimpleAnonymousClass11() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 1); // before class end is used
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass11.php", options);
    }

    public void testBLSimpleAnonymousClass12() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS_HEADER, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION_END, 1);
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_NAMESPACE, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass12.php", options);
    }

    public void testBLSimpleAnonymousClass13() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass13.php", options);
    }

    public void testBLSimpleAnonymousClass14() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass14.php", options);
    }

    public void testBLSimpleAnonymousClass15() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass15.php", options);
    }

    public void testBLSimpleAnonymousClass16() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        options.put(FmtOptions.ANONYMOUS_CLASS_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass16.php", options);
    }

    public void testBLSimpleAnonymousClass17() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.ANONYMOUS_CLASS_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleAnonymousClass17.php", options);
    }

    public void testBLSimpleUse01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use01.php", options);
    }

    public void testBLSimpleUse02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use02.php", options);
    }

    public void testBLSimpleUse03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use03.php", options);
    }

    public void testBLSimpleUse04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use04.php", options);
    }

    public void testBLSimpleGroupUse01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/GroupUse01.php", options);
    }

    public void testBLSimpleGroupUse02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/GroupUse02.php", options);
    }

    public void testBLSimpleGroupUse03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/GroupUse03.php", options);
    }

    public void testBLSimpleGroupUse04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/GroupUse04.php", options);
    }

    public void testOpenClosePHPTag01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag01.php", options);
    }

    public void testOpenClosePHPTag02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag02.php", options);
    }

    public void testOpenClosePHPTag03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag03.php", options);
    }

    public void testOpenClosePHPTag04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag04.php", options);
    }

    public void testOpenClosePHPTag05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag05.php", options);
    }

    public void testOpenClosePHPTag06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag06.php", options);
    }

    public void testMaxPreservedBlankLines01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_MAX_PRESERVED, 0);
        reformatFileContents("testfiles/formatting/blankLines/MaxPreservedLines01.php", options);
    }

    public void testMaxPreservedBlankLines02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_MAX_PRESERVED, 1);
        reformatFileContents("testfiles/formatting/blankLines/MaxPreservedLines02.php", options);
    }

    public void testMaxPreservedBlankLines03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_MAX_PRESERVED, 2);
        reformatFileContents("testfiles/formatting/blankLines/MaxPreservedLines03.php", options);
    }

    public void testIssue229703() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue229703.php", options);
    }

    public void testIssue232395_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 5);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue232395_01.php", options);
    }

    public void testIssue232395_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 5);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 1);
        reformatFileContents("testfiles/formatting/blankLines/issue232395_02.php", options);
    }

    public void testIssue232395_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 5);
        reformatFileContents("testfiles/formatting/blankLines/issue232395_03.php", options);
    }

    public void testIssue232395_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 5);
        reformatFileContents("testfiles/formatting/blankLines/issue232395_04.php", options);
    }

    public void testIssue232395_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 5);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue232395_05.php", options);
    }

    public void testIssue232395_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 5);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 1);
        reformatFileContents("testfiles/formatting/blankLines/issue232395_06.php", options);
    }

    public void testIssue232395_07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 5);
        reformatFileContents("testfiles/formatting/blankLines/issue232395_07.php", options);
    }

    public void testIssue232395_08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 5);
        reformatFileContents("testfiles/formatting/blankLines/issue232395_08.php", options);
    }

    public void testIssue234774() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 5);
        reformatFileContents("testfiles/formatting/blankLines/issue234774.php", options);
    }

    public void testIssue234764_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 1);
        reformatFileContents("testfiles/formatting/blankLines/issue234764_01.php", options);
    }

    public void testIssue234764_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 1);
        reformatFileContents("testfiles/formatting/blankLines/issue234764_02.php", options);
    }

    public void testIssue234764_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 3);
        reformatFileContents("testfiles/formatting/blankLines/issue234764_03.php", options);
    }

    public void testIssue234764_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 3);
        reformatFileContents("testfiles/formatting/blankLines/issue234764_04.php", options);
    }

    public void testIssue235710_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        options.put(FmtOptions.SPACE_BETWEEN_OPEN_PHP_TAG_AND_NAMESPACE, true);
        reformatFileContents("testfiles/formatting/blankLines/issue235710_01.php", options);
    }

    public void testIssue235710_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 3);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 3);
        reformatFileContents("testfiles/formatting/blankLines/issue235710_02.php", options);
    }

    public void testIssue235710_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 3);
        reformatFileContents("testfiles/formatting/blankLines/issue235710_03.php", options);
    }

    public void testIssue235710_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 3);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue235710_04.php", options);
    }

    public void testIssue235710_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue235710_05.php", options);
    }

    public void testIssue235710_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 3);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 3);
        reformatFileContents("testfiles/formatting/blankLines/issue235710_06.php", options);
    }

    public void testIssue235710_07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 3);
        reformatFileContents("testfiles/formatting/blankLines/issue235710_07.php", options);
    }

    public void testIssue235710_08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 3);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue235710_08.php", options);
    }

    public void testIssue235972_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue235972_01.php", options);
    }

    public void testIssue235972_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue235972_02.php", options);
    }

    public void testIssue243744() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_OPEN_PHP_TAG, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_NAMESPACE, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue243744.php", options);
    }

    // between a field and a method
    public void testIssue268710_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_01.php", options);
    }

    public void testIssue268710_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_02.php", options);
    }

    public void testIssue268710_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_03.php", options);
    }

    public void testIssue268710_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_04.php", options);
    }

    public void testIssue268710_05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_05.php", options);
    }

    public void testIssue268710_06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_06.php", options);
    }

    // between fields
    public void testIssue268710_07() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_07.php", options);
    }

    public void testIssue268710_08() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_08.php", options);
    }

    public void testIssue268710_09() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_09.php", options);
    }

    public void testIssue268710_10() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_10.php", options);
    }

    public void testIssue268710_11() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, true);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_11.php", options);
    }

    public void testIssue268710_12() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, true);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_12.php", options);
    }

    public void testIssue268710_13() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, true);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_13.php", options);
    }

    public void testIssue268710_14() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, true);
        reformatFileContents("testfiles/formatting/blankLines/issue268710_14.php", options);
    }

    public void testClassConstantVisibility01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility01.php", options);
    }

    public void testClassConstantVisibility02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility02.php", options);
    }

    public void testClassConstantVisibility03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility03.php", options);
    }

    public void testClassConstantVisibility04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility04.php", options);
    }

    public void testClassConstantVisibility05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility05.php", options);
    }

    public void testClassConstantVisibility06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility06.php", options);
    }

    public void testClassConstantVisibility07() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility07.php", options);
    }

    public void testClassConstantVisibility08() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility08.php", options);
    }

    public void testClassConstantVisibility09() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility09.php", options);
    }

    public void testClassConstantVisibility10() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/ClassConstantVisibility10.php", options);
    }

    // PHP 7.4
    // see testBLFields**()
    public void testTypedProperties20_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_01.php", options);
    }

    public void testTypedProperties20_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_02.php", options);
    }

    public void testTypedProperties20_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_03.php", options);
    }

    public void testTypedProperties20_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_04.php", options);
    }

    public void testTypedProperties20_05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_05.php", options);
    }

    public void testTypedProperties20_05a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_05a.php", options);
    }

    public void testTypedProperties20_05b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_05b.php", options);
    }

    public void testTypedProperties20_06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_06.php", options);
    }

    public void testTypedProperties20_06a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_06a.php", options);
    }

    public void testTypedProperties20_06b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_06b.php", options);
    }

    public void testTypedProperties20_07() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 3);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_07.php", options);
    }

    public void testTypedProperties20_07a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 3);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_07a.php", options);
    }

    public void testTypedProperties20_08() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_08.php", options);
    }

    public void testTypedProperties20_09() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_09.php", options);
    }

    public void testTypedProperties20_10() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_10.php", options);
    }

    public void testTypedProperties20_11() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 2);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/TypedProperties20_11.php", options);
    }

    // [NETBEANS-4443] PHP 8.0
    public void testAttributeSyntaxBeforeClass_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeClass_01a.php", options);
    }

    public void testAttributeSyntaxBeforeClass_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 2);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeClass_01b.php", options);
    }

    public void testAttributeSyntaxAfterClass_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterClass_01a.php", options);
    }

    public void testAttributeSyntaxAfterClass_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 2);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterClass_01b.php", options);
    }

    public void testAttributeSyntaxBeforeInterface_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeInterface_01a.php", options);
    }

    public void testAttributeSyntaxBeforeInterface_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeInterface_01b.php", options);
    }

    public void testAttributeSyntaxAfterInterface_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterInterface_01a.php", options);
    }

    public void testAttributeSyntaxAfterInterface_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterInterface_01b.php", options);
    }

    public void testAttributeSyntaxBeforeTrait_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeTrait_01a.php", options);
    }

    public void testAttributeSyntaxBeforeTrait_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeTrait_01b.php", options);
    }

    public void testAttributeSyntaxAfterTrait_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterTrait_01a.php", options);
    }

    public void testAttributeSyntaxAfterTrait_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_CLASS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterTrait_01b.php", options);
    }

    public void testAttributeSyntaxBeforeFunction_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeFunction_01a.php", options);
    }

    public void testAttributeSyntaxBeforeFunction_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 2);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeFunction_01b.php", options);
    }

    public void testAttributeSyntaxAfterFunction_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterFunction_01a.php", options);
    }

    public void testAttributeSyntaxAfterFunction_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FUNCTION, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterFunction_01b.php", options);
    }

    public void testAttributeSyntaxBeforeFields_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeFields_01a.php", options);
    }

    public void testAttributeSyntaxBeforeFields_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeFields_01b.php", options);
    }

    public void testAttributeSyntaxBeforeFields_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeFields_02a.php", options);
    }

    public void testAttributeSyntaxBeforeFields_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBeforeFields_02b.php", options);
    }

    public void testAttributeSyntaxBetweenFields_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBetweenFields_01a.php", options);
    }

    public void testAttributeSyntaxBetweenFields_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBetweenFields_01b.php", options);
    }

    public void testAttributeSyntaxBetweenFields_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, true);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBetweenFields_02a.php", options);
    }

    public void testAttributeSyntaxBetweenFields_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, true);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxBetweenFields_02b.php", options);
    }

    public void testAttributeSyntaxAfterFields_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterFields_01a.php", options);
    }

    public void testAttributeSyntaxAfterFields_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterFields_01b.php", options);
    }

    public void testAttributeSyntaxAfterFields_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterFields_02a.php", options);
    }

    public void testAttributeSyntaxAfterFields_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php80/AttributeSyntaxAfterFields_02b.php", options);
    }

    public void testBLBetweenEnumCases_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 2);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, true);
        reformatFileContents("testfiles/formatting/blankLines/php81/enumCasesBLBetween_01a.php", options);
    }

    public void testBLBetweenEnumCases_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_FIELDS, 2);
        options.put(FmtOptions.BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, false);
        reformatFileContents("testfiles/formatting/blankLines/php81/enumCasesBLBetween_01b.php", options);
    }

    public void testBLAfterEnumCases_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php81/enumCasesBLAfter_01a.php", options);
    }


    public void testBLAfterEnumCases_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_FIELDS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php81/enumCasesBLAfter_01b.php", options);
    }


    public void testBLBeforeEnumCases_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        reformatFileContents("testfiles/formatting/blankLines/php81/enumCasesBLBefore_01a.php", options);
    }


    public void testBLBeforeEnumCases_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        reformatFileContents("testfiles/formatting/blankLines/php81/enumCasesBLBefore_01b.php", options);
    }

    public void testIssueGH4611Methods_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Methods.php", options, false, true);
    }

    public void testIssueGH4611Methods_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Methods.php", options, false, true);
    }

    public void testIssueGH4611Methods_01c() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Methods.php", options, false, true);
    }

    public void testIssueGH4611Methods_01d() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Methods.php", options, false, true);
    }

    public void testIssueGH4611Methods_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Methods.php", options, false, true);
    }

    public void testIssueGH4611Methods_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Methods.php", options, false, true);
    }

    public void testIssueGH4611Methods_02c() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Methods.php", options, false, true);
    }

    public void testIssueGH4611Methods_02d() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Methods.php", options, false, true);
    }

    public void testIssueGH4611Properties_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Properties.php", options, false, true);
    }

    public void testIssueGH4611Properties_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Properties.php", options, false, true);
    }

    public void testIssueGH4611Properties_01c() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Properties.php", options, false, true);
    }

    public void testIssueGH4611Properties_01d() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Properties.php", options, false, true);
    }

    public void testIssueGH4611Properties_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Properties.php", options, false, true);
    }

    public void testIssueGH4611Properties_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Properties.php", options, false, true);
    }

    public void testIssueGH4611Properties_02c() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Properties.php", options, false, true);
    }

    public void testIssueGH4611Properties_02d() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611Properties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_01c() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_01d() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_01e() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_01f() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_01g() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_01h() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_02c() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_02d() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_02e() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_02f() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_02g() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4611BothMethodsAndProperties_02h() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS_END, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4611BothMethodsAndProperties.php", options, false, true);
    }

    public void testIssueGH4609BetweenUseTypes_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_USE_TYPES, 0);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4609BetweenUseTypes_01.php", options, false, true);
    }

    public void testIssueGH4609BetweenUseTypes_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_USE_TYPES, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4609BetweenUseTypes_01.php", options, false, true);
    }

    public void testIssueGH4609BetweenUseTypes_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_USE_TYPES, 0);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4609BetweenUseTypes_02.php", options, false, true);
    }

    public void testIssueGH4609BetweenUseTypes_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_USE_TYPES, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4609BetweenUseTypes_02.php", options, false, true);
    }

    public void testIssueGH4609BetweenUseTypes_03a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_USE_TYPES, 0);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4609BetweenUseTypes_03.php", options, false, true);
    }

    public void testIssueGH4609BetweenUseTypes_03b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_BETWEEN_USE_TYPES, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH4609BetweenUseTypes_03.php", options, false, true);
    }

    public void testAfterUseTraitHasBlankLine_01a() throws Exception {
        // GH-4685
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE_TRAIT, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE_TRAIT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        reformatFileContents("testfiles/formatting/blankLines/AfterUseTrait_01.php", options, false, true);
    }

    public void testAfterUseTraitHasBlankLine_01b() throws Exception {
        // GH-4685
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE_TRAIT, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE_TRAIT,1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        reformatFileContents("testfiles/formatting/blankLines/AfterUseTrait_01.php", options, false, true);
    }

    public void testAfterUseTraitNoBlankLine_01a() throws Exception {
        // GH-4685
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE_TRAIT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE_TRAIT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 0);
        reformatFileContents("testfiles/formatting/blankLines/AfterUseTrait_01.php", options, false, true);
    }

    public void testAfterUseTraitNoBlankLine_01b() throws Exception {
        // GH-4685
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE_TRAIT, 0);
        options.put(FmtOptions.BLANK_LINES_BEFORE_USE_TRAIT, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FIELDS, 1);
        options.put(FmtOptions.BLANK_LINES_BEFORE_FUNCTION, 1);
        reformatFileContents("testfiles/formatting/blankLines/AfterUseTrait_01.php", options, false, true);
    }

    public void testGH6980_NSWithBlock01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH6980_NSWithBlock01.php", options, false, true);
    }

    public void testGH6980_NSWithBlock02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH6980_NSWithBlock02.php", options, false, true);
    }

    public void testGH6980_NSWithBlock03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH6980_NSWithBlock03.php", options, false, true);
    }

    public void testGH6980_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH6980_01.php", options, false, true);
    }

    public void testGH6980_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH6980_02.php", options, false, true);
    }

    public void testGH6980_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_AFTER_USE, 1);
        reformatFileContents("testfiles/formatting/blankLines/issueGH6980_03.php", options, false, true);
    }

}
