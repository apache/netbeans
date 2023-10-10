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

public class OccurrencesFinderImplGH6075Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplGH6075Test(String testName) {
        super(testName);
    }

    public void testIssueGH6075_01a() throws Exception {
        checkOccurrences(getTestPath(), "class Us^er {", true);
    }

    public void testIssueGH6075_01b() throws Exception {
        checkOccurrences(getTestPath(), "/** @var ?Us^er $user */", true);
    }

    public void testIssueGH6075_01c() throws Exception {
        checkOccurrences(getTestPath(), "/* @var $user2 ?Us^er */", true);
    }

    public void testIssueGH6075_01d() throws Exception {
        checkOccurrences(getTestPath(), "/** @var Us^er|null $user3 */", true);
    }

    public void testIssueGH6075_01e() throws Exception {
        checkOccurrences(getTestPath(), "/* @var $user4 Use^r|null */", true);
    }

}
