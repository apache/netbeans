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


public class OccurrencesFinderImplNb3108Test extends OccurrencesFinderImplTestBase {

    public OccurrencesFinderImplNb3108Test(String testName) {
        super(testName);
    }

    public void testNb3108_01a() throws Exception {
        checkOccurrences(getTestPath(), "    private $pro^perty = [", true);
    }

    public void testNb3108_01b() throws Exception {
        checkOccurrences(getTestPath(), "        $instance2 = new $this->pr^operty[$this->getIndex()];", true);
    }

    public void testNb3108_02a() throws Exception {
        checkOccurrences(getTestPath(), "    private static $^staticProperty = [", true);
    }

    public void testNb3108_02b() throws Exception {
        checkOccurrences(getTestPath(), "        $instance1 = new self::$stati^cProperty[self::getIndex()];", true);
    }

    public void testNb3108_03a() throws Exception {
        checkOccurrences(getTestPath(), "    private static function ge^tIndex() {", true);
    }

    public void testNb3108_03b() throws Exception {
        checkOccurrences(getTestPath(), "        $instance1 = new self::$staticProperty[self::get^Index()];", true);
    }

    public void testNb3108_03c() throws Exception {
        checkOccurrences(getTestPath(), "        $instance2 = new $this->property[$this->getI^ndex()];", true);
    }

}
