/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.typinghooks;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpDeletedTextInterceptorTest extends PhpTypinghooksTestBase {

    public PhpDeletedTextInterceptorTest(String testName) {
        super(testName);
    }

    @Override
    protected void deleteChar(String original, String expected) throws Exception {
        super.deleteChar(wrapAsPhp(original), wrapAsPhp(expected));
    }

    @Override
    protected void deleteWord(String original, String expected) throws Exception {
        super.deleteWord(wrapAsPhp(original), wrapAsPhp(expected));
    }

    public void testIssue235426_01() throws Exception {
        String original = "$v1=\"b ${\"v2\"[^} d\";";
        String expected = "$v1=\"b ${\"v2\"^} d\";";
        deleteChar(original, expected);
    }

    public void testIssue235426_02() throws Exception {
        String original = "$v1=\"b ${\"v2\"(^} d\";";
        String expected = "$v1=\"b ${\"v2\"^} d\";";
        deleteChar(original, expected);
    }

    public void testIssue235426_03() throws Exception {
        String original = "$v1=\"b ${\"v2\"[^] d\";";
        String expected = "$v1=\"b ${\"v2\"^ d\";";
        deleteChar(original, expected);
    }

    public void testIssue234188() throws Exception {
        String original = "echo \"foo\\\"^\";\n}";
        String expected = "echo \"foo^\";\n}";
        deleteChar(original, expected);
    }

    public void testDeleteWord() throws Exception {
        deleteWord("$foo_bar_baz^", "$foo_bar_^");
    }

    public void testDeleteWord111303() throws Exception {
        deleteWord("foo::bar^", "foo::^");
        deleteWord("Foo::Bar^", "Foo::^");
        deleteWord("Foo::Bar_Baz^", "Foo::Bar_^");
    }

    public void testDeleteWordx111305() throws Exception {
        deleteWord("foo_bar^", "foo_^");
        deleteWord("x.foo_bar^.y", "x.foo_^.y");
    }

    public void testDeleteWord2() throws Exception {
        deleteWord("foo_bar_baz ^", "foo_bar_baz^");
        deleteWord("foo_bar_^", "foo_^");
    }

    public void testDeleteWord3() throws Exception {
        deleteWord("FooBarBaz^", "FooBar^");
    }

    public void testDeleteWord4_110998() throws Exception {
        deleteWord("Blah::Set^Foo", "Blah::^Foo");
    }

    public void testdeleteWord5() throws Exception {
        deleteWord("foo_bar_^", "foo_^");
    }

    public void testdeleteWords() throws Exception {
        deleteWord("foo bar^", "foo ^");
    }

    public void testDeleteWord4_110998c() throws Exception {
        String before = "  snark^\n";
        String after = "  ^\n";
        deleteWord(before, after);
    }

    public void testDeleteWord4_110998b() throws Exception {
        String before = ""
                + "  snark(%w(a b c))\n"
                + "  snark(%W(a b c))\n"
                + "  snark^\n"
                + "  snark(%Q(a b c))\n"
                + "  snark(%w(a b c))\n";
        String after = ""
                + "  snark(%w(a b c))\n"
                + "  snark(%W(a b c))\n"
                + "  ^\n"
                + "  snark(%Q(a b c))\n"
                + "  snark(%w(a b c))\n";
        deleteWord(before, after);
    }

    public void testFreakOutEditor1() throws Exception {
        String before = "x = method_call(50, <<TOKEN1, \"arg3\", <<TOKEN2, /startofregexp/^\nThis is part of the string\nTOKEN1\nrestofregexp/)";
        String  after = "x = method_call(50, <<TOKEN1, \"arg3\", <<TOKEN2, /startofregexp^\nThis is part of the string\nTOKEN1\nrestofregexp/)";
        deleteChar(before, after);
    }


    public void testBackspace1() throws Exception {
        deleteChar("x^", "^");
    }

    public void testBackspace2() throws Exception {
        deleteChar("x^y", "^y");
    }

    public void testBackspace3() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace4() throws Exception {
        deleteChar("xy^z", "x^z");
    }

    public void testBackspace5() throws Exception {
        deleteChar("x=\"^\"", "x=^");
    }

    public void testBackspace6() throws Exception {
        deleteChar("x='^'", "x=^");
    }

    public void testBackspace7() throws Exception {
        deleteChar("x=(^)", "x=^");
    }

    public void testBackspace7b() throws Exception {
        deleteChar("x=[^]", "x=^");
    }

    public void testBackspace8() throws Exception {
        // See bug 111534
        deleteChar("x={^}", "x=^");
    }

}
