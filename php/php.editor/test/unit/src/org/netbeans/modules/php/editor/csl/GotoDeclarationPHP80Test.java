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

public class GotoDeclarationPHP80Test extends GotoDeclarationTestBase {

    public GotoDeclarationPHP80Test(String testName) {
        super(testName);
    }

    @Override
    protected String getBaseTestFolderPath() {
        return super.getBaseTestFolderPath() + "php80/";
    }

    public void testNonCapturingCatches_01() throws Exception {
        checkDeclaration(getTestPath(), "} catch (Exc^eptionA) {", "class ^ExceptionA extends Exception");
    }

    public void testNonCapturingCatches_02() throws Exception {
        checkDeclaration(getTestPath(), "} catch (E^xceptionA | ExceptionB) {", "class ^ExceptionA extends Exception");
    }

    public void testNonCapturingCatches_03() throws Exception {
        checkDeclaration(getTestPath(), "} catch (ExceptionA | Exception^B) {", "class ^ExceptionB extends Exception");
    }

    public void testMatchExpression_01() throws Exception {
        checkDeclaration(getTestPath(), "            MatchEx^pression::START => self::$start,", "class ^MatchExpression");
    }

    public void testMatchExpression_02() throws Exception {
        checkDeclaration(getTestPath(), "            Match^Expression::SUSPEND => $this->suspend,", "class ^MatchExpression");
    }

    public void testMatchExpression_03() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpre^ssion::STOP => $this->stopState(),", "class ^MatchExpression");
    }

    public void testMatchExpression_04() throws Exception {
        checkDeclaration(getTestPath(), "            default =>  Matc^hExpression::default() . MatchExpression::match . $this->match(),", "class ^MatchExpression");
    }

    public void testMatchExpression_05() throws Exception {
        checkDeclaration(getTestPath(), "            default =>  MatchExpression::default() . MatchExpressi^on::match . $this->match(),", "class ^MatchExpression");
    }

    public void testMatchExpression_06() throws Exception {
        checkDeclaration(getTestPath(), "$instance = new MatchExp^ression();", "class ^MatchExpression");
    }

    public void testMatchExpression_07() throws Exception {
        checkDeclaration(getTestPath(), "    default => MatchExp^ression::default(),", "class ^MatchExpression");
    }

    public void testMatchExpression_08() throws Exception {
        checkDeclaration(getTestPath(), "        return match ($st^ate) {", "        $^state = self::STOP;");
    }

    public void testMatchExpression_09() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::STA^RT => self::$start,", "    public const ^START = \"start\";");
    }

    public void testMatchExpression_10() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::START => self::$s^tart,", "    private static $^start = \"start state\";");
    }

    public void testMatchExpression_11() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::SUSPE^ND => $this->suspend,", "    public const ^SUSPEND = \"suspend\";");
    }

    public void testMatchExpression_12() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::SUSPEND => $this->sus^pend,", "    private $^suspend = \"suspend state\";");
    }

    public void testMatchExpression_13() throws Exception {
        checkDeclaration(getTestPath(), "            MatchExpression::STOP => $this->stop^State(),", "    public function ^stopState(): string {");
    }

    public void testMatchExpression_14() throws Exception {
        checkDeclaration(getTestPath(), "            default =>  MatchExpression::default() . MatchExpression::ma^tch . $this->match(),", "    private const ^match = \"match\"; // context sensitive lexer");
    }

    public void testMatchExpression_15() throws Exception {
        checkDeclaration(getTestPath(), "            default =>  MatchExpression::default() . MatchExpression::match . $this->m^atch(),", "    public function ^match(): string {");
    }

    public void testMatchExpression_16() throws Exception {
        checkDeclaration(getTestPath(), "    default => MatchExpression::defau^lt(),", "    public static function ^default(): string {");
    }

}
