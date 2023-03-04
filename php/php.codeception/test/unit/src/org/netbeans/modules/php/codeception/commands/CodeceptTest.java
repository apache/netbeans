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
package org.netbeans.modules.php.codeception.commands;

import java.util.regex.Matcher;
import org.netbeans.junit.NbTestCase;

public class CodeceptTest extends NbTestCase {

    public CodeceptTest(String name) {
        super(name);
    }

    // the same tests as php.phpunit
    public void testLinePatternTestRunner() {
        Matcher matcher = Codecept.LINE_PATTERN.matcher("/home/gapon/test/Calculator.php:635");
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/test/Calculator.php", matcher.group(1));
        assertEquals("635", matcher.group(2));
        assertTrue(Codecept.LINE_PATTERN.matcher("/h o m e/gapon/test/Calculator.php:635").matches());
        assertTrue(Codecept.LINE_PATTERN.matcher("C:\\home\\gapon\\test\\Calculator.php:635").matches());

        assertFalse(Codecept.LINE_PATTERN.matcher("").matches());
    }

    public void testLinePatternOutput() {
        assertTrue(Codecept.LINE_PATTERN.matcher("/home/gapon/test/Calculator.php:635").matches());

        Matcher matcher = Codecept.LINE_PATTERN.matcher("0.1077    6609264   6. PhpUnit_Util_Fileloader::checkAndLoad() /usr/share/php/PhpUnit/Framework/TestSuite.php:385");
        assertTrue(matcher.matches());
        assertEquals("/usr/share/php/PhpUnit/Framework/TestSuite.php", matcher.group(1));
        assertEquals("385", matcher.group(2));
    }

}
