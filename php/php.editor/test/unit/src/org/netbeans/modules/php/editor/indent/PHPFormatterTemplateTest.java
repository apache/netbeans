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
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.css.editor.indent.CssIndentTaskFactory;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Pisl
 */
public class PHPFormatterTemplateTest extends PHPFormatterTestBase {

    public PHPFormatterTemplateTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        try {
            TestLanguageProvider.register(CssTokenId.language());
            TestLanguageProvider.register(HTMLTokenId.language());
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
            System.out.println("neco spatne");
        }
        CssIndentTaskFactory cssFactory = new CssIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/css"), cssFactory);
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"));
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example other formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
            DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);

            EditorCookie ec = (EditorCookie) dobj.getLookup().lookup(EditorCookie.class);
            assertNotNull(ec);

            return (BaseDocument) ec.openDocument();
        } catch (Exception ex) {
            fail(ex.toString());
            return null;
        }
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/fore_01.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_FOR_PARENS, true);
        reformatFileContents("testfiles/formatting/templates/fore_02.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_FOR_PARENS, true);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/fore_03.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_FOR_PARENS, true);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/fore_04.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_FOR_PARENS, true);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/fore_05.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testFore_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_FOR_PARENS, true);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/templates/fore_06.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testIssue184481_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184481_01.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testIssue184481_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184481_02.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testIssue184481_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184481_03.php", options, true);
    }

    //The testing file can not be edited in NetBeans due to trailing spaces. It's important to keep spaces on the empty lines.
    public void testIssue184481_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184481_04.php", options, true);
    }

    public void testIssue184070_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184070_01.php", options, true);
    }

    public void testIssue184690_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue184690_01.php", options, true);
    }

    public void testPrivate_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/templates/private_01.php", options, true);
    }

    public void testPrivate_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/templates/private_02.php", options, true);
    }

    public void testFncTemplate_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/templates/function_01.php", options, true);
    }

    public void testImplementsOverwriteTemplate_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/templates/implementsOverwrite_01.php", options, true);
    }

    public void testImplementsOverwriteTemplate_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/implementsOverwrite_02.php", options, true);
    }

    public void testImplementsOverwriteTemplate_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/templates/implementsOverwrite_03.php", options, true);
    }

    public void testIssue184141() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/templates/issue184141.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, true);
        reformatFileContents("testfiles/formatting/templates/issue185435_01.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue185435_02.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, false);
        reformatFileContents("testfiles/formatting/templates/issue185435_03.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, false);
        options.put(FmtOptions.TAB_SIZE, 3);
        reformatFileContents("testfiles/formatting/templates/issue185435_04.php", options, true);
    }

    // The test file containscharacters that are converted by default setting of netbeans.
    // Don't edit the test file in NetBeans!!!
    public void testIssue185435_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, false);
        options.put(FmtOptions.TAB_SIZE, 3);
        reformatFileContents("testfiles/formatting/templates/issue185435_05.php", options, true);
    }

    public void testIssue185438_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue185438_01.php", options, true);
    }

    public void testIssue185438_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue185438_02.php", options, true);
    }

    public void testIssue186008_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue186008_01.php", options, true);
    }

    public void testIssue186008_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue186008_02.php", options, true);
    }

    public void testIssue186008_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue186008_03.php", options, true);
    }

    public void testFirstLineInHTML() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/firstLineInHTML_01.php", options, true);
    }

    public void testIssue187665_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue187665_01.php", options, true);
    }

    public void testIssue187665_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue187665_02.php", options, true);
    }

    public void testIssue188656_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue188656_01.php", options, true);
    }

    public void testIssue188656_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue188656_02.php", options, true);
    }

    public void testIssue188656_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue188656_03.php", options, true);
    }

    public void testIssue188656_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.TAB_SIZE, 4);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, true);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        options.put(FmtOptions.INDENT_SIZE, 4);
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.SPACES_PER_TAB, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_04.php", options, true);
    }

    public void testIssue188656_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.TAB_SIZE, 4);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, true);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        options.put(FmtOptions.INDENT_SIZE, 4);
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.SPACES_PER_TAB, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_05.php", options, true);
    }

    public void testIssue188656_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.TAB_SIZE, 4);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, true);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        options.put(FmtOptions.INDENT_SIZE, 4);
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.SPACES_PER_TAB, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_06.php", options, true);
    }

    public void testIssue188656_07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.TAB_SIZE, 4);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, true);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        options.put(FmtOptions.INDENT_SIZE, 4);
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.SPACES_PER_TAB, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_07.php", options, true);
    }

    public void testIssue188656_08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.TAB_SIZE, 4);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, true);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        options.put(FmtOptions.INDENT_SIZE, 4);
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.SPACES_PER_TAB, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_08.php", options, true);
    }

    public void testIssue188656_09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.TAB_SIZE, 4);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, true);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        options.put(FmtOptions.INDENT_SIZE, 4);
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.SPACES_PER_TAB, 4);
        reformatFileContents("testfiles/formatting/templates/issue188656_09.php", options, true);
    }

    public void testIssue191565_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.TAB_SIZE, 4);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, false);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        options.put(FmtOptions.SPACES_PER_TAB, 4);
        reformatFileContents("testfiles/formatting/templates/issue191565_01.php", options, true);
    }

    public void testIssue192220() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue192220.php", options, true);
    }

    public void testIssue198616() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/templates/issue198616.php", options, true);
    }

    public void testIssue187757() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue187757.php", options, true);
    }

    // #259031
    public void testGroupUseWrapNever_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapNever_01.php", options, true);
    }

    public void testGroupUseWrapNever_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapNever_02.php", options, true);
    }

    public void testGroupUseWrapNever_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapNever_03.php", options, true);
    }

    public void testGroupUseWrapNever_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapNever_04.php", options, true);
    }

    public void testGroupUseWrapNever_05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapNever_05.php", options, true);
    }

    public void testGroupUseWrapNever_06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapNever_06.php", options, true);
    }

    public void testGroupUseWrapIfLong_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapIfLong_01.php", options, true);
    }

    public void testGroupUseWrapIfLong_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapIfLong_02.php", options, true);
    }

    public void testGroupUseWrapIfLong_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapIfLong_03.php", options, true);
    }

    public void testGroupUseWrapIfLong_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapIfLong_04.php", options, true);
    }

    public void testGroupUseWrapIfLong_05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapIfLong_05.php", options, true);
    }

    public void testGroupUseWrapIfLong_06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapIfLong_06.php", options, true);
    }

    public void testGroupUseWrapAlways_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapAlways_01.php", options, true);
    }

    public void testGroupUseWrapAlways_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapAlways_02.php", options, true);
    }

    public void testGroupUseWrapAlways_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapAlways_03.php", options, true);
    }

    public void testGroupUseWrapAlways_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapAlways_04.php", options, true);
    }

    public void testGroupUseWrapAlways_05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapAlways_05.php", options, true);
    }

    public void testGroupUseWrapAlways_06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_GROUP_USE_LIST, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/templates/groupUseWrapAlways_06.php", options, true);
    }

    public void testIssue262205_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue262205_01.php", options, true);
    }

    public void testIssue262205_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue262205_02.php", options, true);
    }

    public void testIssue262205_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue262205_03.php", options, true);
    }

    public void testIssue262205_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue262205_04.php", options, true);
    }

    public void testIssue268920() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/templates/issue268920.php", options, true);
    }

}
