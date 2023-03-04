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
public class YamlScannerTest extends YamlTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public YamlScannerTest(String testName) {
        super(testName);
    }

    public void testStructureRails() throws Exception {
        checkStructure("testfiles/database.yml");
    }

    public void testStructure1() throws Exception {
        checkStructure("testfiles/test1.yaml");
    }

    public void testStructure2() throws Exception {
        checkStructure("testfiles/test2.yaml");
    }

    public void testStructure3() throws Exception {
        checkStructure("testfiles/test3.yaml");
    }

    public void testStructure4() throws Exception {
        checkStructure("testfiles/test4.yaml");
    }

    public void testStructure5() throws Exception {
        checkStructure("testfiles/test5.yaml");
    }

    public void testStructure6() throws Exception {
        checkStructure("testfiles/test6.yaml");
    }

    public void testStructure7() throws Exception {
        checkStructure("testfiles/test7.yaml");
    }

    public void testStructure8() throws Exception {
        checkStructure("testfiles/test8.yaml");
    }

    public void testStructure10() throws Exception {
        checkStructure("testfiles/test10.yaml");
    }

    public void testStructure11() throws Exception {
        checkStructure("testfiles/test11.yaml");
    }

    public void testStructureOmap() throws Exception {
        checkStructure("testfiles/ordered.yaml");
    }

    public void testErb1() throws Exception {
        checkStructure("testfiles/fixture.yml");
    }

    public void testErb2() throws Exception {
        checkStructure("testfiles/fixture2.yml");
    }

    public void testErb3() throws Exception {
        checkStructure("testfiles/fixture3.yml");
    }

    public void test143747a() throws Exception {
        checkStructure("testfiles/unicode.yml");
    }

    public void testFolds1() throws Exception {
        checkFolds("testfiles/test1.yaml");
    }

    public void testFolds2() throws Exception {
        checkFolds("testfiles/test2.yaml");
    }

    public void testFolds3() throws Exception {
        checkFolds("testfiles/test3.yaml");
    }

    public void testFolds4() throws Exception {
        checkFolds("testfiles/test4.yaml");
    }

    public void testFolds5() throws Exception {
        checkFolds("testfiles/test5.yaml");
    }

    public void testFolds6() throws Exception {
        checkFolds("testfiles/test6.yaml");
    }

    public void testFolds7() throws Exception {
        checkFolds("testfiles/test7.yaml");
    }

    public void testFolds8() throws Exception {
        checkFolds("testfiles/test8.yaml");
    }

    public void testFolds9() throws Exception {
        checkFolds("testfiles/test9.yaml");
    }

    public void testFolds10() throws Exception {
        checkFolds("testfiles/test10.yaml");
    }

    public void testFolds11() throws Exception {
        checkFolds("testfiles/test11.yaml");
    }

    public void testFoldsOmap() throws Exception {
        checkFolds("testfiles/ordered.yaml");
    }

    public void testErb1Folds() throws Exception {
        checkFolds("testfiles/fixture.yml");
    }

    public void testErb2Folds() throws Exception {
        checkFolds("testfiles/fixture2.yml");
    }

    public void testErb3Folds() throws Exception {
        checkFolds("testfiles/fixture3.yml");
    }

    public void test143747b() throws Exception {
        checkFolds("testfiles/unicode.yml");
    }

    public void testUnicodePositions() throws Exception {
        checkFolds("testfiles/unicode2.yml");
    }

    public void testIssue173769() throws Exception {
        checkStructure("testfiles/issue173769.yaml");
    }
}
