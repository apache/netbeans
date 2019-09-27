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


public class OccurrencesFinderImplNb1861Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplNb1861Test(String testName) {
        super(testName);
    }

    public void testNb1861_01a() throws Exception {
        checkOccurrences(getTestPath(), " * @method static ^staticTestVoid(Test $test) test comment", true);
    }

    public void testNb1861_01b() throws Exception {
        checkOccurrences(getTestPath(), "        self::staticTes^tVoid($test);", true);
    }

    public void testNb1861_01c() throws Exception {
        checkOccurrences(getTestPath(), "Example::staticTestVoi^d($test);", true);
    }

    public void testNb1861_02a() throws Exception {
        checkOccurrences(getTestPath(), " * @method static int staticTestT^ype(Test $param1, $param2)  test comment", true);
    }

    public void testNb1861_02b() throws Exception {
        checkOccurrences(getTestPath(), "        self::staticT^estType($param1, $param2);", true);
    }

    public void testNb1861_02c() throws Exception {
        checkOccurrences(getTestPath(), "Example::staticTe^stType($param1, $param2);", true);
    }

    public void testNb1861_03a() throws Exception {
        checkOccurrences(getTestPath(), " * @method static ?int stat^icTestNullable(?string $param, int $param2) test comment", true);
    }

    public void testNb1861_03b() throws Exception {
        checkOccurrences(getTestPath(), "        self::staticTestNu^llable($param1, $param2);", true);
    }

    public void testNb1861_03c() throws Exception {
        checkOccurrences(getTestPath(), "Example::staticTe^stNullable($param1, $param2);", true);
    }

    public void testNb1861_04a() throws Exception {
        checkOccurrences(getTestPath(), " * @method static ?Example g^etDefault() Description", true);
    }

    public void testNb1861_04b() throws Exception {
        checkOccurrences(getTestPath(), "        self::get^Default();", true);
    }

    public void testNb1861_04c() throws Exception {
        checkOccurrences(getTestPath(), "Example::^getDefault();", true);
    }

}
