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
package org.netbeans.modules.php.editor.verification;

public class WrongOrderOfArgsHintTest extends PHPHintsTestBase {

    public WrongOrderOfArgsHintTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "WrongOrderOfArgsHint/";
    }

    public void testWrongOrderOfArgsHint() throws Exception {
        checkHints(new WrongOrderOfArgsHint(), "testWrongOrderOfArgsHint.php");
    }

    // [NETBEANS-4443] PHP 8.0
    public void testConstructorPropertyPromotion() throws Exception {
        checkHints(new WrongOrderOfArgsHint(), "testConstructorPropertyPromotion.php");
    }

    public void testConstructorPropertyPromotionMultiLines() throws Exception {
        checkHints(new WrongOrderOfArgsHint(), "testConstructorPropertyPromotionMultiLines.php");
    }

    //~ Fix
    public void testWrongOrderOfArgsHintFix_01() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testWrongOrderOfArgsHint.php",
                "function wrongArgs($optional = null, $mand^atory, $secondOpt = array())",
                "Rearrange arguments"
        );
    }

    public void testWrongOrderOfArgsHintFix_02() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testWrongOrderOfArgsHint.php",
                "function wrongArgsFnc($optional = null, $mandat^ory, $secondOpt = array())",
                "Rearrange arguments"
        );
    }

    // [NETBEANS-4443] PHP 8.0
    public void testConstructorPropertyPromotionFix_01() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotion.php",
                "public function __construct(private ?int $optional = 1, $ma^ndatory)",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionFix_02() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotion.php",
                "public function __construct(private ?int $optional1 = 1, $manda^tory, string $optional2 = \"default value\")",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionFix_03() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotion.php",
                "public function __construct(protected ?int $mandatory1, $optional = \"default value\", private string|int $mandator^y2)",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionFix_04() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotion.php",
                "public function __construct(private ?int $mandatory1, private $optional = \"default value\", public string $ma^ndatory2)",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionMultiLinesFix_01() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionMultiLines.php",
                "$manda^tory, // test1",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionMultiLinesFix_02() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionMultiLines.php",
                "$mand^atory, // test2",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionMultiLinesFix_03() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionMultiLines.php",
                "private string|int $mand^atory2, // test3",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionMultiLinesFix_04() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionMultiLines.php",
                "public string $mandator^y2 // test4",
                "Rearrange arguments"
        );
    }
}
