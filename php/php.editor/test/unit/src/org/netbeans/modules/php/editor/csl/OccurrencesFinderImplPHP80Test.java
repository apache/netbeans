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
package org.netbeans.modules.php.editor.csl;

public class OccurrencesFinderImplPHP80Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP80Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php80/";
    }

    public void testNonCapturingCatches_01() throws Exception {
        checkOccurrences(getTestPath(), "class Exc^eptionA extends Exception", true);
    }

    public void testNonCapturingCatches_02() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Exceptio^nA) {", true);
    }

    public void testNonCapturingCatches_03() throws Exception {
        checkOccurrences(getTestPath(), "} catch (Ex^ceptionA | ExceptionB)", true);
    }

    public void testNonCapturingCatches_04() throws Exception {
        checkOccurrences(getTestPath(), "class Except^ionB extends Exception", true);
    }

    public void testNonCapturingCatches_05() throws Exception {
        checkOccurrences(getTestPath(), "} catch (ExceptionA | E^xceptionB)", true);
    }

    public void testMatchExpression_01a() throws Exception {
        checkOccurrences(getTestPath(), "class Ma^tchExpression", true);
    }

    public void testMatchExpression_01b() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpr^ession::START => self::$start,", true);
    }

    public void testMatchExpression_01c() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpressi^on::SUSPEND => $this->suspend,", true);
    }

    public void testMatchExpression_01d() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpress^ion::STOP => $this->stopState(),", true);
    }

    public void testMatchExpression_01e() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  Ma^tchExpression::default() . MatchExpression::match . $this->match(),", true);
    }

    public void testMatchExpression_01f() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  MatchExpression::default() . ^MatchExpression::match . $this->match(),", true);
    }

    public void testMatchExpression_01g() throws Exception {
        checkOccurrences(getTestPath(), "$instance = new MatchExpr^ession();", true);
    }

    public void testMatchExpression_01h() throws Exception {
        checkOccurrences(getTestPath(), "    default => MatchExpressi^on::default(),", true);
    }

    public void testMatchExpression_02a() throws Exception {
        checkOccurrences(getTestPath(), "        $sta^te = self::STOP;", true);
    }

    public void testMatchExpression_02b() throws Exception {
        checkOccurrences(getTestPath(), "        return match ($s^tate) {", true);
    }

    public void testMatchExpression_03a() throws Exception {
        checkOccurrences(getTestPath(), "    public const ST^ART = \"start\";", true);
    }

    public void testMatchExpression_03b() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpression::STA^RT => self::$start,", true);
    }

    public void testMatchExpression_04a() throws Exception {
        checkOccurrences(getTestPath(), "    private static $sta^rt = \"start state\";", true);
    }

    public void testMatchExpression_04b() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpression::START => self::$st^art,", true);
    }

    public void testMatchExpression_05a() throws Exception {
        checkOccurrences(getTestPath(), "    private $susp^end = \"suspend state\";", true);
    }

    public void testMatchExpression_05b() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpression::SUSPEND => $this->susp^end,", true);
    }

    public void testMatchExpression_06a() throws Exception {
        checkOccurrences(getTestPath(), "            MatchExpression::STOP => $this->stop^State(),", true);
    }

    public void testMatchExpression_06b() throws Exception {
        checkOccurrences(getTestPath(), "    public function stopSt^ate(): string {", true);
    }

    public void testMatchExpression_07a() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  MatchExpression::defau^lt() . MatchExpression::match . $this->match(),", true);
    }

    public void testMatchExpression_07b() throws Exception {
        checkOccurrences(getTestPath(), "    public static function defau^lt(): string {", true);
    }

    public void testMatchExpression_07c() throws Exception {
        checkOccurrences(getTestPath(), "    default => MatchExpression::defa^ult(),", true);
    }

    public void testMatchExpression_08a() throws Exception {
        checkOccurrences(getTestPath(), "    private const matc^h = \"match\"; // context sensitive lexer", true);
    }

    public void testMatchExpression_08b() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  MatchExpression::default() . MatchExpression::mat^ch . $this->match(),", true);
    }

    public void testMatchExpression_09a() throws Exception {
        checkOccurrences(getTestPath(), "            default =>  MatchExpression::default() . MatchExpression::match . $this->mat^ch(),", true);
    }

    public void testMatchExpression_09b() throws Exception {
        checkOccurrences(getTestPath(), "    public function ma^tch(): string {", true);
    }

    public void testMatchExpression_09c() throws Exception {
        checkOccurrences(getTestPath(), "echo $instance->mat^ch();", true);
    }

}
