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

}
