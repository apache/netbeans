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
package org.netbeans.modules.php.editor.csl;


public class OccurrencesFinderImplNb5062Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplNb5062Test(String testName) {
        super(testName);
    }

    public void testNb5062_01a() throws Exception {
        checkOccurrences(getTestPath(), "$static1->testMe^thod();", true);
    }

    public void testNb5062_01b() throws Exception {
        checkOccurrences(getTestPath(), "$static2->^testMethod();", true);
    }

    public void testNb5062_01c() throws Exception {
        checkOccurrences(getTestPath(), "$static3->tes^tMethod();", true);
    }

    public void testNb5062_01d() throws Exception {
        checkOccurrences(getTestPath(), "$self1->testMetho^d();", true);
    }

    public void testNb5062_01e() throws Exception {
        checkOccurrences(getTestPath(), "$self2->^testMethod();", true);
    }

    public void testNb5062_01f() throws Exception {
        checkOccurrences(getTestPath(), "$self3->testMetho^d();", true);
    }

    public void testNb5062_01g() throws Exception {
        checkOccurrences(getTestPath(), "public function tes^tMethod() { // Test2", true);
    }

}
