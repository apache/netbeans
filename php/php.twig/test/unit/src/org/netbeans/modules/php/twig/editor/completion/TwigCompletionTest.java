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
package org.netbeans.modules.php.twig.editor.completion;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TwigCompletionTest extends TwigCompletionTestBase {

    public TwigCompletionTest(String testName) {
        super(testName);
    }

    public void testCompletion_01() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_01.twig", "{% a^ %}", false);
    }

    public void testCompletion_02() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_02.twig", "{{ a^ }}", false);
    }

    public void testCompletion_03() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_03.twig", "{% ^ %}", false);
    }

    public void testCompletion_04() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_04.twig", "{{ ^ }}", false);
    }

    public void testCompletion_05() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_05.twig", "{% a^", false);
    }

    public void testCompletion_06_01() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_06.twig", "^{% a %}", false);
    }

    public void testCompletion_06_02() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_06.twig", "{^% a %}", false);
    }

    public void testCompletion_06_03() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_06.twig", "{%^ a %}", false);
    }

    public void testCompletion_06_04() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_06.twig", "{% a ^%}", false);
    }

    public void testCompletion_06_05() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_06.twig", "{% a %^}", false);
    }

    public void testCompletion_06_06() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_06.twig", "{% a %}^", false);
    }

    public void testCompletion_07_01() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_07.twig", "^{{ a }}", false);
    }

    public void testCompletion_07_02() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_07.twig", "{^{ a }}", false);
    }

    public void testCompletion_07_03() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_07.twig", "{{^ a }}", false);
    }

    public void testCompletion_07_04() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_07.twig", "{{ a ^}}", false);
    }

    public void testCompletion_07_05() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_07.twig", "{{ a }^}", false);
    }

    public void testCompletion_07_06() throws Exception {
        checkCompletion("testfiles/completion/testCompletion_07.twig", "{{ a }}^", false);
    }

    public void testFilter_01() throws Exception {
        checkCompletion("testfiles/completion/testFilter.twig", "{{ foo|^ }}", false);
    }

    public void testFilter_02() throws Exception {
        checkCompletion("testfiles/completion/testFilter.twig", "{% bar|^ %}", false);
    }

    public void testIssue219569_01() throws Exception {
        checkCompletion("testfiles/completion/issue219569.twig", "{% filter^  %}", false);
    }

    public void testIssue219569_02() throws Exception {
        checkCompletion("testfiles/completion/issue219569.twig", "{% filter ^ %}", false);
    }

    public void testIssue219569_03() throws Exception {
        checkCompletion("testfiles/completion/issue219569.twig", "{% filter  ^%}", false);
    }

    public void testIssue219569_04() throws Exception {
        checkCompletion("testfiles/completion/issue219569.twig", "{% filter cap^ %}", false);
    }

    public void testIssue219569_05() throws Exception {
        checkCompletion("testfiles/completion/issue219569.twig", "{{ foo.^bar }}", false);
    }

    public void testIssue219569_06() throws Exception {
        checkCompletion("testfiles/completion/issue219569.twig", "{{ foo.bar^ }}", false);
    }

    public void testIssue219569_07() throws Exception {
        checkCompletion("testfiles/completion/issue219569.twig", "{{ foo|^ }}", false);
    }

    public void testIssue219569_08() throws Exception {
        checkCompletion("testfiles/completion/issue219569.twig", "{{ foo|cap^ }}", false);
    }

    public void testIssue222602() throws Exception {
        checkCompletion("testfiles/completion/issue222602.twig", "{{ 'use^", false);
    }

}
