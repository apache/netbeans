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
package org.netbeans.modules.javascript2.editor;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsCodeCompletionDocumentationTest extends JsCodeCompletionBase {

    public JsCodeCompletionDocumentationTest(String testName) {
        super(testName);
    }

    public void testCompletionDocumentation01() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/documentation01.js", "eer^o(1, 2);", false, "eer");
    }

    public void testCompletionDocumentation02() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/documentation01.js", "vari^able;", false, "vari");
    }

    public void testCompletionDocumentation03() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/documentation01.js", "prom^enna;", false, "prom");
    }

    public void testIssue180805() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/documentation01.js", "tes^t(1, 2);", false, "tes");
    }

    public void testIssue223104() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue223104.js", "testOptionalPa^rameter(\"nevim\", \"nevim2\");", false, "testOptionalPa");
    }

    public void testIssue226631() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue226631.js", "/*HERE*/ maxipe^", false, "maxipe");
    }

    public void testIssue255966_1() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "personsArr^.push({});", false, "personsArr");
    }
    
    public void testIssue255966_2() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "arrS^tr.push(\"test\");", false, "arrS");
    }
    
    
    public void testIssue255966_3() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "arrO^bj[1]", false, "arrO");
    }
    
    public void testIssue255966_4() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "personO^bj[\"firstName\"];", false, "personO");
    }

   
    public void testIssue255966_5() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "personsN^ew.push({});", false, "personsN");
    }
    
    public void testIssue255966_6() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "printOb^js(persons);", false, "printOb");
    }
    public void testIssue255966_7() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "anotherFun^c(\"test\");", false, "anotherFun");
    }
    public void testIssue255966_8() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "getTmp^Object();", false, "getTmp");
    }
    public void testIssue255966_9() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "tryMe^ssage();", false, "tryMe");
    }
    public void testIssue255966_10() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "getSe^lect();", false, "getSe");
    }
    public void testIssue255966_11() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "returnOb^j();", false, "returnOb");
    }
    public void testIssue255966_12() throws Exception {
        checkCompletionDocumentation("testfiles/completion/documentation/issue255966.js", "testT^ype();", false, "testT");
    }
}    
