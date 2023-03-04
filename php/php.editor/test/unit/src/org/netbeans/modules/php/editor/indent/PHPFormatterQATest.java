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
import java.util.Map;
import java.util.prefs.Preferences;
import junit.framework.Test;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Filip.Zamboj at Sun.com
 */
public class PHPFormatterQATest extends PHPFormatterTestBase {

    private final String FORMAT_START_MARK = "/*FORMAT_START*/"; //NOI18N
    private final String FORMAT_END_MARK = "/*FORMAT_END*/"; //NOI18N

    public PHPFormatterQATest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(PHPFormatterQATest.class).gui(false).suite();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }


    public void test180332_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/180332.php");
    }

    public void testSpacesAfterObjectRefereneces_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/spacesAfterObjectReferences.php");
    }

    public void test168396_1_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/168396_1.php");
    }

    public void test168396_2_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/168396_2.php");
    }

    public void test168396_3_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/168396_3.php");
    }

    public void test176224_stableFixed() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, false);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, false);
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/176224.php", options);
    }

    // NETBEANS-3391
    public void test176224_stableFixed_psr12() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, true);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, true);
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/176224_psr12.php", options);
    }

    public void test173354_1_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_1.php");
    }

    public void test173354_2_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_2.php");
    }

    public void test173354_3_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_3.php");
    }

    public void test173107_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173107.php");
    }

    public void test160996_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/160996.php");
    }

    public void test162320_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/162320.php");
    }

    public void test162586_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/162586.php");
    }

    public void test173899_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173899.php");
    }

    public void test173903_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173903.php");
    }

    public void test173906_172475_1_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_1.php");
    }

    public void test173906_172475_2_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_2.php");
    }

    public void test173906_172475_3_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_3.php");
    }

    public void test173908_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173908.php");
    }

    public void test174579_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174579.php");
    }

    public void test174578_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174578.php");
    }

    public void test124273_175247_regression() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/regressions/124273_175247.php", options);
    }

    public void test174563_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174563_1.php");
    }

    public void testIfElseStatement_stableFixed() throws Exception {

        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/else_if.php");
    }

    public void test174595_175229() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174595_175229.php", options);
    }

    public void test175229_1() throws Exception {
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/175229_1.php", options);
    }

    public void test175229_2() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/175229_2.php", options);
    }

    //bug transformed to 189562
    public void test124273() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/124273.php", options);
    }

    public void test175425() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/175425.php", options);
    }

    //bug transformed to 189562
    public void test167162_1() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/167162_1.php", options);
    }

    //bug transformed to 189562
    public void test167162_2() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/167162_2.php", options);
    }

    //BUG transformed to 189560
    public void test163071() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/163071.php", options);
    }

    //BUG transformed to 189562
    public void test168187() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/168187.php", options);
    }

    public void test175427() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/175427.php", options);
    }

    public void test188810() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/188810.php", options);
    }
//--

    public void test174873_173906_stablePartial() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_partialTests/174873_173906.php", options);
    }

    public void test174873_173906_1_stablePartial() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_partialTests/174873_173906_1.php");
    }

    public void test152429_1_stable() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/152429_1.php", options);
    }

    public void test152429_2_stable() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/152429_2.php", options);
    }

    /** settings 5,5  **/
    public void testSpacesAfterObjectRefereneces_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/spacesAfterObjectReferences.php", new IndentPrefs(5, 5));
    }

    public void test173354_1_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_1.php", new IndentPrefs(5, 5));
    }

    public void test173354_2_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_2.php", new IndentPrefs(5, 5));
    }

    public void test173354_3_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_3.php", new IndentPrefs(5, 5));
    }

    public void test173107_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173107.php", new IndentPrefs(5, 5));
    }

    public void test160996_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/160996.php", new IndentPrefs(5, 5));
    }

    public void test162320_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/162320.php", new IndentPrefs(5, 5));
    }

    public void test162586_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/162586.php", new IndentPrefs(5, 5));
    }

    public void test173899_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173899.php", new IndentPrefs(5, 5));
    }

    public void test173903_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173903.php", new IndentPrefs(5, 5));
    }

    public void test173906_172475_1_5_5_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_1.php", new IndentPrefs(5, 5));
    }

    public void test173906_172475_2_5_5_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_2.php", new IndentPrefs(5, 5));
    }

    public void test173906_172475_3_5_5_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_3.php", new IndentPrefs(5, 5));
    }

    public void test173908_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173908.php", new IndentPrefs(5, 5));
    }

    public void test174579_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174579.php", new IndentPrefs(5, 5));
    }

    public void test174578_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174578.php", new IndentPrefs(5, 5));
    }

    public void test124273_175247_5_5_regression() throws Exception {

        reformatFileContents("testfiles/formatting/qa/issues/regressions/124273_175247.php", new IndentPrefs(5, 5));
    }

    public void test174563_5_5_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174563_1.php", new IndentPrefs(5, 5));
    }

    public void testIfElseStatement_5_5_stableFixed() throws Exception {

        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/else_if.php", new IndentPrefs(5, 5));
    }

    public void test174595_175229_5_5_stableFixed() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174595_175229.php", options);
    }

    public void test174873_173906_5_5_stablePartial() throws Exception {

        reformatFileContents("testfiles/formatting/qa/issues/stable_partialTests/174873_173906.php", new IndentPrefs(5, 5));
    }

    public void test174873_173906_1_5_5_stablePartial() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_partialTests/174873_173906_1.php", new IndentPrefs(5, 5));
    }

    public void test152429_1_5_5_stable() throws Exception {

        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/152429_1.php", new IndentPrefs(5, 5));
    }

    public void test152429_2_5_5_stable() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/152429_2.php", new IndentPrefs(5, 5));
    }

    public void test152429_3_5_5_stable() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/152429_3.php", new IndentPrefs(5, 5));
    }

    /** settings 10,10  **/
    public void testSpacesAfterObjectRefereneces_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/spacesAfterObjectReferences.php", new IndentPrefs(10, 10));
    }

    public void test173354_1_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_1.php", new IndentPrefs(10, 10));
    }

    public void test173354_2_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_2.php", new IndentPrefs(10, 10));
    }

    public void test173354_3_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173354_3.php", new IndentPrefs(10, 10));
    }

    public void test173107_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173107.php", new IndentPrefs(10, 10));
    }

    public void test160996_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/160996.php", new IndentPrefs(10, 10));
    }

    public void test162320_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/162320.php", new IndentPrefs(10, 10));
    }

    public void test162586_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/162586.php", new IndentPrefs(10, 10));
    }

    public void test173899_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173899.php", new IndentPrefs(10, 10));
    }

    public void test173903_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173903.php", new IndentPrefs(10, 10));
    }

    public void test173906_172475_1_10_10_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_1.php", new IndentPrefs(10, 10));
    }

    public void test173906_172475_2_10_10_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_2.php", new IndentPrefs(10, 10));
    }

    public void test173906_172475_3_10_10_stableFixedIssue() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173906_172475_3.php", new IndentPrefs(10, 10));
    }

    public void test173908_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/173908.php", new IndentPrefs(10, 10));
    }

    public void test174579_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174579.php", new IndentPrefs(10, 10));
    }

    public void test174578_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174578.php", new IndentPrefs(10, 10));
    }

    public void test124273_175247_10_10_regression() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/regressions/124273_175247.php", options);
    }

    public void test174563_10_10_stableFixed() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174563_1.php", new IndentPrefs(10, 10));
    }

    public void testIfElseStatement_10_10_stableFixed() throws Exception {

        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/else_if.php", new IndentPrefs(10, 10));
    }

    public void test174595_175229_10_10_stableFixed() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/174595_175229.php", options);
    }

    public void test174873_173906_10_10_stablePartial() throws Exception {

        reformatFileContents("testfiles/formatting/qa/issues/stable_partialTests/174873_173906.php", new IndentPrefs(10, 10));
    }

    public void test174873_173906_1_10_10_stablePartial() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_partialTests/174873_173906_1.php", new IndentPrefs(10, 10));
    }

    public void test152429_1_10_10_stable() throws Exception {

        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/152429_1.php", new IndentPrefs(10, 10));
    }

    public void test152429_2_10_10_stable() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/152429_2.php", new IndentPrefs(10, 10));
    }

    public void test152429_3_10_10_stable() throws Exception {
        reformatFileContents("testfiles/formatting/qa/issues/stable_fixedIssues/152429_3.php", new IndentPrefs(10, 10));
    }

    private void reformatFileContents(String file) throws Exception {
        reformatFileContents(file, new IndentPrefs(2, 2));
    }

    @Override
    protected void reformatFileContents(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        String fullTxt = doc.getText(0, doc.getLength());
        int formatStart = 0;
        int formatEnd = doc.getLength();
        int startMarkPos = fullTxt.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0) {
            formatStart = startMarkPos + FORMAT_START_MARK.length();
            formatEnd = fullTxt.indexOf(FORMAT_END_MARK);

            if (formatEnd == -1) {
                throw new IllegalStateException();
            }
        }

        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);
        format(doc, formatter, formatStart, formatEnd, false);

        String after = doc.getText(0, doc.getLength());
        if (preferences.getIndentation() != 2 || preferences.getHangingIndentation() != 2) {
            assertDescriptionMatches(file, after, false,
                    "_"
                    + preferences.getIndentation()
                    + "_"
                    + preferences.getHangingIndentation()
                    + ".formatted");
        } else {
            assertDescriptionMatches(file, after, false, ".formatted");
        }
    }

    @Override
    protected void reformatFileContents(String file, Map<String, Object> options) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        int formatStart = 0;
        int formatEnd = text.length();
        int startMarkPos = text.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0){
            formatStart = startMarkPos;
            text = text.substring(0, formatStart) + text.substring(formatStart + FORMAT_START_MARK.length());
            formatEnd = text.indexOf(FORMAT_END_MARK);
            text = text.substring(0, formatEnd) + text.substring(formatEnd + FORMAT_END_MARK.length());
            formatEnd --;
            if (formatEnd == -1){
                throw new IllegalStateException();
            }
        }

        BaseDocument doc = getDocument(text);
        assertNotNull(doc);


        IndentPrefs preferences = new IndentPrefs(4, 4);
        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String option = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer) {
                prefs.putInt(option, ((Integer)value).intValue());
            }
            else if (value instanceof String) {
                prefs.put(option, (String)value);
            }
            else if (value instanceof Boolean) {
                prefs.put(option, ((Boolean)value).toString());
            }
	    else if (value instanceof CodeStyle.BracePlacement) {
		prefs.put(option, ((CodeStyle.BracePlacement)value).name());
	    }
	    else if (value instanceof CodeStyle.WrapStyle) {
		prefs.put(option, ((CodeStyle.WrapStyle)value).name());
	    }
        }

        format(doc, formatter, formatStart, formatEnd, false);
        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }
}
