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
package org.netbeans.modules.javascript2.editor;

public class JsCodeCompletionIssueGH5919 extends JsCodeCompletionBase {

    public JsCodeCompletionIssueGH5919(String testName) {
        super(testName);
    }

    public void testGH5919_0_1() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_0.js", "impo^", false);
    }

    public void testGH5919_1_1() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_1.js", "import.^", false);
    }

    public void testGH5919_1_2() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_1.js", "import.me^", false);
    }

    public void testGH5919_1_3() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_1.js", "import.meta.^", false);
    }

    public void testGH5919_1_4() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_1.js", "import.meta.ur^", false);
    }

    public void testGH5919_1_5() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_1.js", "    ne^w.target;", false);
    }

    public void testGH5919_1_6() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_1.js", "    new.^target;", false);
    }

    public void testGH5919_1_7() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_1.js", "    new.targ^et;", false);
    }

    public void testGH5919_2_1() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_2.js", "import/*dummy*/.^", false);
    }

    public void testGH5919_2_2() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_2.js", "import/*dummy*/.me^", false);
    }

    public void testGH5919_2_3() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_2.js", "import/*dummy*/.meta.^", false);
    }

    public void testGH5919_2_4() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_2.js", "import/*dummy*/.meta.ur^", false);
    }

    public void testGH5919_2_5() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_2.js", "    ne^w/*dummy*/.target;", false);
    }

    public void testGH5919_2_6() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_2.js", "    new/*dummy*/.^target;", false);
    }

    public void testGH5919_2_7() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_2.js", "    new/*dummy*/.targ^et;", false);
    }

    public void testGH5919_3_1() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "import/*dummy*/.^/*dummy*/", false);
    }

    public void testGH5919_3_2() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "import/*dummy*/./*dummy*/^", false);
    }

    public void testGH5919_3_3() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "import/*dummy*/./*dummy*/me^", false);
    }

    public void testGH5919_3_4() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "import/*dummy*/./*dummy*/meta.^", false);
    }

    public void testGH5919_3_5() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "import/*dummy*/./*dummy*/meta.ur^", false);
    }

    public void testGH5919_3_6() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "    ne^w/*dummy*/./*dummy*/target;", false);
    }

    public void testGH5919_3_7() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "    new/*dummy*/.^/*dummy*/target;", false);
    }

    public void testGH5919_3_8() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "    new/*dummy*/./*dummy*/^target;", false);
    }

    public void testGH5919_3_9() throws Exception {
        checkCompletion("testfiles/completion/gh5919/gh5919_3.js", "    new/*dummy*/./*dummy*/targ^et;", false);
    }
}
