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

    // PHP 8.4: Property hooks
    public void testConstructorPropertyPromotionWithPropertyHooks() throws Exception {
        checkHints(new WrongOrderOfArgsHint(), "testConstructorPropertyPromotionWithPropertyHooks.php");
    }

    public void testConstructorPropertyPromotionWithPropertyHooksFix_01() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionWithPropertyHooks.php",
                "public function __construct(private ?int $o^ptional = 1 {get{} set{}}, $mandatory) {",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionWithPropertyHooksFix_02() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionWithPropertyHooks.php",
                "public function __construct(private ?int $optional1 = 1 {get => $this->optional1; set => $this->optional1 = $value;}, $mandatory, string $opt^ional2 = \"default value\" {get{} set{}}) {",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionWithPropertyHooksFix_03() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionWithPropertyHooks.php",
                "public function __construct(#[Attr] protected ?int $mandatory1, $o^ptional = \"default value\" {get {} set{}}, private string|int $mandatory2) {",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionWithPropertyHooksFix_04() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionWithPropertyHooks.php",
                "public function __construct(private ?int $mandatory1, private $optio^nal = \"default value\" {get {return $this->optional;} set {$this->optional = $value;}}, public string $mandatory2) {",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionMultiLinesWithPropertyHooks() throws Exception {
        checkHints(new WrongOrderOfArgsHint(), "testConstructorPropertyPromotionMultiLinesWithPropertyHooks.php");
    }

    public void testConstructorPropertyPromotionMultiLinesWithPropertyHooksFix_01() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionMultiLinesWithPropertyHooks.php",
                "        $mand^atory, // test1",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionMultiLinesWithPropertyHooksFix_02() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionMultiLinesWithPropertyHooks.php",
                "        $mandato^ry, // test2",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionMultiLinesWithPropertyHooksFix_03() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionMultiLinesWithPropertyHooks.php",
                "        private string|int $mandatory^2, // test3",
                "Rearrange arguments"
        );
    }

    public void testConstructorPropertyPromotionMultiLinesWithPropertyHooksFix_04() throws Exception {
        applyHint(
                new WrongOrderOfArgsHint(),
                "testConstructorPropertyPromotionMultiLinesWithPropertyHooks.php",
                "        public string $mandat^ory2 // test4",
                "Rearrange arguments"
        );
    }

}
