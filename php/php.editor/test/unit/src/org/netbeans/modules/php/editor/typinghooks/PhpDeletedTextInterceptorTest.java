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
