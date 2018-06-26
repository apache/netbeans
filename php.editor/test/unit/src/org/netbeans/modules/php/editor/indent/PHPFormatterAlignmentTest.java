/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPFormatterAlignmentTest extends PHPFormatterTestBase {

    public PHPFormatterAlignmentTest(String testName) {
        super(testName);
    }

    public void testAlignmentKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_NEW_LINE_AFTER_MODIFIERS, true);
        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords01.php", options);
    }

    public void testAlignmentKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_NEW_LINE_AFTER_MODIFIERS, true);

        options.put(FmtOptions.SPACE_BEFORE_ELSE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, false);
        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords02.php", options);
    }

    public void testAlignmentKeywords03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_NEW_LINE_AFTER_MODIFIERS, true);

        options.put(FmtOptions.SPACE_BEFORE_ELSE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, false);

        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);

        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords03.php", options);
    }

    public void testAlignmentKeywords04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_NEW_LINE_AFTER_MODIFIERS, false);

        options.put(FmtOptions.SPACE_BEFORE_ELSE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, false);

        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, FmtOptions.OBRACE_NEWLINE);

        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords04.php", options);
    }

    public void testGroupAlignmentAssignment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        reformatFileContents("testfiles/formatting/alignment/groupAlignmentAssignment01.php", options);
    }

    public void testIssue209030() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        reformatFileContents("testfiles/formatting/alignment/issue209030.php", options);
    }

    public void testIssue211482() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        reformatFileContents("testfiles/formatting/alignment/issue211482.php", options);
    }

    public void testGroupAlignmentArrayInit01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        reformatFileContents("testfiles/formatting/alignment/groupAlignmentArrayInit01.php", options);
    }

    public void testIssue218847() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_NEW_LINE_AFTER_MODIFIERS, true);
        reformatFileContents("testfiles/formatting/alignment/issue218847.php", options);
    }

    public void testIssue214466() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        reformatFileContents("testfiles/formatting/alignment/issue214466.php", options);
    }

    public void testIssue225003() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        reformatFileContents("testfiles/formatting/alignment/issue225003.php", options);
    }

    public void testIssue225010() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        reformatFileContents("testfiles/formatting/alignment/issue225010.php", options);
    }

    public void testIssue230798() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        reformatFileContents("testfiles/formatting/alignment/issue230798.php", options);
    }

    public void testIssue230949_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, false);
        reformatFileContents("testfiles/formatting/alignment/issue230949_01.php", options);
    }

    public void testIssue230949_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, true);
        reformatFileContents("testfiles/formatting/alignment/issue230949_02.php", options);
    }

    public void testIssue211445_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        options.put(FmtOptions.WRAP_ARRAY_INIT, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/alignment/issue211445_01.php", options);
    }

    public void testIssue211445_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        options.put(FmtOptions.WRAP_ARRAY_INIT, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/alignment/issue211445_02.php", options);
    }

    public void testIssue211445_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        options.put(FmtOptions.WRAP_ARRAY_INIT, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/alignment/issue211445_03.php", options);
    }

    public void testIssue211445_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        options.put(FmtOptions.WRAP_ARRAY_INIT, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/alignment/issue211445_04.php", options);
    }

    public void testIssue211445_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        options.put(FmtOptions.WRAP_ARRAY_INIT, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/alignment/issue211445_05.php", options);
    }

    public void testFinally_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_FINALLY_ON_NEW_LINE, true);
        reformatFileContents("testfiles/formatting/alignment/finally_01.php", options);
    }

    public void testFinally_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.PLACE_FINALLY_ON_NEW_LINE, false);
        reformatFileContents("testfiles/formatting/alignment/finally_02.php", options);
    }

    public void testIssue244566() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        reformatFileContents("testfiles/formatting/alignment/issue244566.php", options);
    }

}
