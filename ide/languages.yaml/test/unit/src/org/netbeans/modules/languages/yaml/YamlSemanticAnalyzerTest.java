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
package org.netbeans.modules.languages.yaml;

/**
 *
 * @author Tor Norbye
 */
public class YamlSemanticAnalyzerTest extends YamlTestBase {

    public YamlSemanticAnalyzerTest(String testName) {
        super(testName);
    }

    public void testSemanticRails() throws Exception {
        checkSemantic("testfiles/database.yml");
    }

    public void testSemantic1() throws Exception {
        checkSemantic("testfiles/test1.yaml");
    }

    public void testSemantic2() throws Exception {
        checkSemantic("testfiles/test2.yaml");
    }

    public void testSemantic3() throws Exception {
        checkSemantic("testfiles/test3.yaml");
    }

    public void testSemantic4() throws Exception {
        checkSemantic("testfiles/test4.yaml");
    }

    public void testSemantic5() throws Exception {
        checkSemantic("testfiles/test5.yaml");
    }

    public void testSemantic6() throws Exception {
        checkSemantic("testfiles/test6.yaml");
    }

    public void testSemantic7() throws Exception {
        checkSemantic("testfiles/test7.yaml");
    }

    public void testSemantic8() throws Exception {
        checkSemantic("testfiles/test8.yaml");
    }

    public void testSemantic9() throws Exception {
        checkSemantic("testfiles/test9.yaml");
    }

    public void testSemantic10() throws Exception {
        checkSemantic("testfiles/test10.yaml");
    }

    public void testSemantic11() throws Exception {
        checkSemantic("testfiles/test11.yaml");
    }

    public void testSemanticOmap() throws Exception {
        checkSemantic("testfiles/ordered.yaml");
    }

    public void testErb1() throws Exception {
        checkSemantic("testfiles/fixture.yml");
    }

    public void testErb2() throws Exception {
        checkSemantic("testfiles/fixture2.yml");
    }

    public void testErb3() throws Exception {
        checkSemantic("testfiles/fixture3.yml");
    }

    public void testAdvanced1() throws Exception {
        checkSemantic("testfiles/advanced1.yaml");
    }

    public void test143747() throws Exception {
        checkSemantic("testfiles/unicode.yml");
    }
}
