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


public class GotoDeclarationNb1861Test extends GotoDeclarationTestBase {

    public GotoDeclarationNb1861Test(String testName) {
        super(testName);
    }

    // Magic methods (@method)
    public void testNb1861_01a() throws Exception {
        checkDeclaration(getTestPath(), "        self::staticTestV^oid($test);", " * @method static ^staticTestVoid(Test $test) test comment");
    }

    public void testNb1861_01b() throws Exception {
        checkDeclaration(getTestPath(), "Example::static^TestVoid($test);", " * @method static ^staticTestVoid(Test $test) test comment");
    }

    public void testNb1861_02a() throws Exception {
        checkDeclaration(getTestPath(), "        self::staticTestTy^pe($param1, $param2);", " * @method static int ^staticTestType(Test $param1, $param2)  test comment");
    }

    public void testNb1861_02b() throws Exception {
        checkDeclaration(getTestPath(), "Example::^staticTestType($param1, $param2);", " * @method static int ^staticTestType(Test $param1, $param2)  test comment");
    }

    public void testNb1861_03a() throws Exception {
        checkDeclaration(getTestPath(), "        self::st^aticTestNullable($param1, $param2);", " * @method static ?int ^staticTestNullable(?string $param, int $param2) test comment");
    }

    public void testNb1861_03b() throws Exception {
        checkDeclaration(getTestPath(), "Example::staticTe^stNullable($param1, $param2);", " * @method static ?int ^staticTestNullable(?string $param, int $param2) test comment");
    }

    public void testNb1861_04a() throws Exception {
        checkDeclaration(getTestPath(), "        self::ge^tDefault();", " * @method static ?Example ^getDefault() Description");
    }

    public void testNb1861_04b() throws Exception {
        checkDeclaration(getTestPath(), "Example::getDe^fault();", " * @method static ?Example ^getDefault() Description");
    }

}
