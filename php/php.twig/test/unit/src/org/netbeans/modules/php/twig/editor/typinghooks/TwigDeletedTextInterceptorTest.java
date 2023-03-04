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
package org.netbeans.modules.php.twig.editor.typinghooks;

public class TwigDeletedTextInterceptorTest extends TwigTypinghooksTestBase {

    public TwigDeletedTextInterceptorTest(String testName) {
        super(testName);
    }

    // Block
    // Delimiter
    public void testDeleteBlockDelimiter_01() throws Exception {
        deleteChar("{%^%}", "^");
    }

    public void testDeleteBlockDelimiter_02() throws Exception {
        deleteChar("{%%^}", "{%^}");
    }

    public void testDeleteBlockDelimiter_03() throws Exception {
        deleteChar("{^%%}", "^%%}");
    }

    public void testDeleteBlockDelimiter_04() throws Exception {
        deleteChar("{%%}^", "{%%^");
    }

    // Double Quote
    public void testDeleteDoubleQuoteInBlock_01() throws Exception {
        deleteChar("{% set value = \"^\" %}", "{% set value = ^ %}");
    }

    public void testDeleteDoubleQuoteInBlock_02() throws Exception {
        deleteChar("{% set value = \"^test\" %}", "{% set value = ^test\" %}");
    }

    public void testDeleteDoubleQuoteInBlock_03() throws Exception {
        deleteChar("{% set value = \"test\"^ %}", "{% set value = \"test^ %}");
    }

    public void testDeleteDoubleQuoteInBlock_04() throws Exception {
        deleteChar("{% set value = \"\\\"^\" %}", "{% set value = \"^\" %}");
    }

    public void testDeleteDoubleQuoteInBlock_05() throws Exception {
        deleteChar("{% set value = \"te\\\"^st\" %}", "{% set value = \"te^st\" %}");
    }

    public void testDeleteDoubleQuoteInBlock_06() throws Exception {
        deleteChar("{% set value = \"\\\"^test\" %}", "{% set value = \"^test\" %}");
    }

    public void testDeleteDoubleQuoteInBlock_07() throws Exception {
        deleteChar("{% set value = \"test\\\"^\" %}", "{% set value = \"test^\" %}");
    }

    public void testDeleteDoubleQuoteInBlock_08() throws Exception {
        deleteChar("{% set value = '\\\"^' %}", "{% set value = '^' %}");
    }

    public void testDeleteDoubleQuoteInBlock_09() throws Exception {
        deleteChar("{% set value = '\\\"^test' %}", "{% set value = '^test' %}");
    }

    public void testDeleteDoubleQuoteInBlock_10() throws Exception {
        deleteChar("{% set value = 'te\\\"^st' %}", "{% set value = 'te^st' %}");
    }

    public void testDeleteDoubleQuoteInBlock_11() throws Exception {
        deleteChar("{% set value = 'test\\\"^' %}", "{% set value = 'test^' %}");
    }

    // Single Quote
    public void testDeleteSingleQuoteInBlock_01() throws Exception {
        deleteChar("{% set value = '^' %}", "{% set value = ^ %}");
    }

    public void testDeleteSingleQuoteInBlock_02() throws Exception {
        deleteChar("{% set value = '^test' %}", "{% set value = ^test' %}");
    }

    public void testDeleteSingleQuoteInBlock_03() throws Exception {
        deleteChar("{% set value = 'test'^ %}", "{% set value = 'test^ %}");
    }

    public void testDeleteSingleQuoteInBlock_04() throws Exception {
        deleteChar("{% set value = '\\'^' %}", "{% set value = '^' %}");
    }

    public void testDeleteSingleQuoteInBlock_05() throws Exception {
        deleteChar("{% set value = 'te\\'^st' %}", "{% set value = 'te^st' %}");
    }

    public void testDeleteSingleQuoteInBlock_06() throws Exception {
        deleteChar("{% set value = '\\'^test' %}", "{% set value = '^test' %}");
    }

    public void testDeleteSingleQuoteInBlock_07() throws Exception {
        deleteChar("{% set value = 'test\\'^' %}", "{% set value = 'test^' %}");
    }

    public void testDeleteSingleQuoteInBlock_08() throws Exception {
        deleteChar("{% set value = \"\\'^\" %}", "{% set value = \"^\" %}");
    }

    public void testDeleteSingleQuoteInBlock_09() throws Exception {
        deleteChar("{% set value = \"\\'^test\" %}", "{% set value = \"^test\" %}");
    }

    public void testDeleteSingleQuoteInBlock_10() throws Exception {
        deleteChar("{% set value = \"te\\'^st\" %}", "{% set value = \"te^st\" %}");
    }

    public void testDeleteSingleQuoteInBlock_11() throws Exception {
        deleteChar("{% set value = \"test\\'^\" %}", "{% set value = \"test^\" %}");
    }

    // Curly
    public void testDeleteCurlyInBlock_01() throws Exception {
        deleteChar("{% set value = {^} %}", "{% set value = ^ %}");
    }

    public void testDeleteCurlyInBlock_02() throws Exception {
        deleteChar("{% set value = {}^ %}", "{% set value = {^ %}");
    }

    public void testDeleteCurlyInBlock_03() throws Exception {
        deleteChar("{% set value = {\"test\": {^}} %}", "{% set value = {\"test\": ^} %}");
    }

    public void testDeleteCurlyInBlock_04() throws Exception {
        deleteChar("{% set value = {\"test\": {^} %}", "{% set value = {\"test\": ^} %}");
    }

    public void testDeleteCurlyInBlock_05() throws Exception {
        deleteChar("{% set value = {\"test\": {{^} %}", "{% set value = {\"test\": {^} %}");
    }

    // Parenthesis
    public void testDeleteParenthesisInBlock_01() throws Exception {
        deleteChar("{% (^) %}", "{% ^ %}");
    }

    public void testDeleteParenthesisInBlock_02() throws Exception {
        deleteChar("{% ()^ %}", "{% (^ %}");
    }

    public void testDeleteParenthesisInBlock_03() throws Exception {
        deleteChar("{% ((^)) %}", "{% (^) %}");
    }

    public void testDeleteParenthesisInBlock_04() throws Exception {
        deleteChar("{% ((^) %}", "{% (^) %}");
    }

    public void testDeleteParenthesisInBlock_05() throws Exception {
        deleteChar("{% (((^) %}", "{% ((^) %}");
    }

    // Bracket
    public void testDeleteBracketInBlock_01() throws Exception {
        deleteChar("{% set value = [^] %}", "{% set value = ^ %}");
    }

    public void testDeleteBracketInBlock_02() throws Exception {
        deleteChar("{% set value = []^ %}", "{% set value = [^ %}");
    }

    public void testDeleteBracketInBlock_03() throws Exception {
        deleteChar("{% set value = [\"test\", [^]] %}", "{% set value = [\"test\", ^] %}");
    }

    public void testDeleteBracketInBlock_04() throws Exception {
        deleteChar("{% set value = [\"test\": [^] %}", "{% set value = [\"test\": ^] %}");
    }

    public void testDeleteBracketInBlock_05() throws Exception {
        deleteChar("{% set value = [\"test\": [[^] %}", "{% set value = [\"test\": [^] %}");
    }

    // Variable
    // Delimiter
    public void testDeleteVariableDelimiter_01() throws Exception {
        deleteChar("{{^}}", "^");
    }

    public void testDeleteVariableDelimiter_02() throws Exception {
        deleteChar("{{}^}", "{{^}");
    }

    public void testDeleteVariableDelimiter_03() throws Exception {
        deleteChar("{^{}}", "^{}}");
    }

    public void testDeleteVariableDelimiter_04() throws Exception {
        deleteChar("{{}}^", "{{}^");
    }

    // Double Quote
    public void testDeleteDoubleQuoteInVariable_01() throws Exception {
        deleteChar("{{\"^\"}}", "{{^}}");
        deleteChar("{{ \"^\" }}", "{{ ^ }}");
    }

    public void testDeleteDoubleQuoteInVariable_02() throws Exception {
        deleteChar("{{ \"^test\" }}", "{{ ^test\" }}");
    }

    public void testDeleteDoubleQuoteInVariable_03() throws Exception {
        deleteChar("{{ \"test\"^ }}", "{{ \"test^ }}");
    }

    public void testDeleteDoubleQuoteInVariable_04() throws Exception {
        deleteChar("{{ \"\\\"^\" }}", "{{ \"^\" }}");
    }

    public void testDeleteDoubleQuoteInVariable_05() throws Exception {
        deleteChar("{{ \"te\\\"^st\" }}", "{{ \"te^st\" }}");
    }

    public void testDeleteDoubleQuoteInVariable_06() throws Exception {
        deleteChar("{{ \"\\\"^test\" }}", "{{ \"^test\" }}");
    }

    public void testDeleteDoubleQuoteInVariable_07() throws Exception {
        deleteChar("{{ \"test\\\"^\" }}", "{{ \"test^\" }}");
    }

    public void testDeleteDoubleQuoteInVariable_08() throws Exception {
        deleteChar("{{ '\\\"^' }}", "{{ '^' }}");
    }

    public void testDeleteDoubleQuoteInVariable_09() throws Exception {
        deleteChar("{{ '\\\"^test' }}", "{{ '^test' }}");
    }

    public void testDeleteDoubleQuoteInVariable_10() throws Exception {
        deleteChar("{{ 'te\\\"^st' }}", "{{ 'te^st' }}");
    }

    public void testDeleteDoubleQuoteInVariable_11() throws Exception {
        deleteChar("{{ 'test\\\"^' }}", "{{ 'test^' }}");
    }

    // Single Quote
    public void testDeleteSingleQuoteInVariable_01() throws Exception {
        deleteChar("{{ '^' }}", "{{ ^ }}");
    }

    public void testDeleteSingleQuoteInVariable_02() throws Exception {
        deleteChar("{{ '^test' }}", "{{ ^test' }}");
    }

    public void testDeleteSingleQuoteInVariable_03() throws Exception {
        deleteChar("{{ 'test'^ }}", "{{ 'test^ }}");
    }

    public void testDeleteSingleQuoteInVariable_04() throws Exception {
        deleteChar("{{ '\\'^' }}", "{{ '^' }}");
    }

    public void testDeleteSingleQuoteInVariable_05() throws Exception {
        deleteChar("{{ 'te\\'^st' }}", "{{ 'te^st' }}");
    }

    public void testDeleteSingleQuoteInVariable_06() throws Exception {
        deleteChar("{{ '\\'^test' }}", "{{ '^test' }}");
    }

    public void testDeleteSingleQuoteInVariable_07() throws Exception {
        deleteChar("{{ 'test\\'^' }}", "{{ 'test^' }}");
    }

    public void testDeleteSingleQuoteInVariable_08() throws Exception {
        deleteChar("{{ \"\\'^\" }}", "{{ \"^\" }}");
    }

    public void testDeleteSingleQuoteInVariable_09() throws Exception {
        deleteChar("{{ \"\\'^test\" }}", "{{ \"^test\" }}");
    }

    public void testDeleteSingleQuoteInVariable_10() throws Exception {
        deleteChar("{{ \"te\\'^st\" }}", "{{ \"te^st\" }}");
    }

    public void testDeleteSingleQuoteInVariable_11() throws Exception {
        deleteChar("{{ \"test\\'^\" }}", "{{ \"test^\" }}");
    }

    // Curly : this can be used in variable?
    public void testDeleteCurlyInVariable_01() throws Exception {
        deleteChar("{{ {^} }}", "{{ ^ }}");
    }

    public void testDeleteCurlyInVariable_02() throws Exception {
        deleteChar("{{ {}^ }}", "{{ {^ }}");
    }

    public void testDeleteCurlyInVariable_03() throws Exception {
        deleteChar("{{ {\"test\": {^} } }}", "{{ {\"test\": ^ } }}");
    }

    public void testDeleteCurlyInVariable_04() throws Exception {
        deleteChar("{{ {\"test\": {^} }}", "{{ {\"test\": ^} }}");
    }

    public void testDeleteCurlyInVariable_05() throws Exception {
        deleteChar("{{ {\"test\": {{^} }}", "{{ {\"test\": {^} }}");
    }

    // Parenthesis
    public void testDeleteParenthesisInVariable_01() throws Exception {
        deleteChar("{{(^) }}", "{{^ }}");
    }

    public void testDeleteParenthesisInVariable_02() throws Exception {
        deleteChar("{{()^ }}", "{{(^ }}");
    }

    public void testDeleteParenthesisInVariable_03() throws Exception {
        deleteChar("{{((^)) }}", "{{(^) }}");
    }

    public void testDeleteParenthesisInVariable_04() throws Exception {
        deleteChar("{{((^) }}", "{{(^) }}");
    }

    public void testDeleteParenthesisInVariable_05() throws Exception {
        deleteChar("{{(((^) }}", "{{((^) }}");
    }

    // Bracket
    public void testDeleteBracketInVariable_01() throws Exception {
        deleteChar("{{ [^] }}", "{{ ^ }}");
    }

    public void testDeleteBracketInVariable_02() throws Exception {
        deleteChar("{{ []^ }}", "{{ [^ }}");
    }

    public void testDeleteBracketInVariable_03() throws Exception {
        deleteChar("{{ [\"test\", [^]] }}", "{{ [\"test\", ^] }}");
    }

    public void testDeleteBracketInVariable_04() throws Exception {
        deleteChar("{{ [\"test\": [^] }}", "{{ [\"test\": ^] }}");
    }

    public void testDeleteBracketInVariable_05() throws Exception {
        deleteChar("{{ [\"test\": [[^] }}", "{{ [\"test\": [^] }}");
    }

}
