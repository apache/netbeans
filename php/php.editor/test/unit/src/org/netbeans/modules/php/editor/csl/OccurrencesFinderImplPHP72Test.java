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


public class OccurrencesFinderImplPHP72Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplPHP72Test(String testName) {
        super(testName);
    }

    public void testGroupUseTrailingCommas_01() throws Exception {
        checkOccurrences(getTestPath(), "class ^Foo {", true);
    }

    public void testGroupUseTrailingCommas_02() throws Exception {
        checkOccurrences(getTestPath(), "    Fo^o,", true);
    }

    public void testGroupUseTrailingCommas_03() throws Exception {
        checkOccurrences(getTestPath(), "$foo = new Fo^o();", true);
    }

    public void testGroupUseTrailingCommas_04() throws Exception {
        checkOccurrences(getTestPath(), "class B^ar {", true);
    }

    public void testGroupUseTrailingCommas_05() throws Exception {
        checkOccurrences(getTestPath(), "    Ba^r,", true);
    }

    public void testGroupUseTrailingCommas_06() throws Exception {
        checkOccurrences(getTestPath(), "$bar = new Ba^r();", true);
    }

    public void testGroupUseTrailingCommas_07() throws Exception {
        checkOccurrences(getTestPath(), "class Ba^z {", true);
    }

    public void testGroupUseTrailingCommas_08() throws Exception {
        checkOccurrences(getTestPath(), "    B\\Ba^z,", true);
    }

    public void testGroupUseTrailingCommas_09() throws Exception {
        checkOccurrences(getTestPath(), "$baz = new Ba^z();", true);
    }

}
