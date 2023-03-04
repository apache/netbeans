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

package org.netbeans.modules.php.api.executable;

import java.util.regex.Matcher;
import org.netbeans.junit.NbTestCase;

public class PhpInterpreterTest extends NbTestCase {

    public PhpInterpreterTest(String name) {
        super(name);
    }

    public void testLinePattern0() {
        Matcher matcher = PhpInterpreter.LINE_PATTERNS[0].matcher("Parse error: syntax error, unexpected T_STRING, expecting '(' in /home/gapon/NetBeansProjects/PhpProject1/Calculator.php on line 10");
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject1/Calculator.php", matcher.group(1));
        assertEquals("10", matcher.group(2));
        assertTrue(PhpInterpreter.LINE_PATTERNS[0].matcher("Parse error: syntax error, unexpected T_STRING, expecting '(' in /h o m e/gapon/NetBeansProjects/PhpProject1/Calculator.php on line 10").matches());
        assertTrue(PhpInterpreter.LINE_PATTERNS[0].matcher("Parse error: syntax error, unexpected T_STRING, expecting '(' in C:\\home\\gapon\\NetBeansProjects\\PhpProject1\\Calculator.php on line 10").matches());
        assertTrue(PhpInterpreter.LINE_PATTERNS[0].matcher("Exception: hello world in /home/gapon/NetBeansProjects/PhpProject1/Calculator.php on line 16").matches());

        assertFalse(PhpInterpreter.LINE_PATTERNS[0].matcher("").matches());
    }

    public void testLinePattern1() {
        Matcher matcher = PhpInterpreter.LINE_PATTERNS[1].matcher("    0.0002     115808   1. {main}() /home/gapon/NetBeansProjects/PhpProject1/Calculator.php:0");
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject1/Calculator.php", matcher.group(1));
        assertEquals("0", matcher.group(2));
        assertTrue(PhpInterpreter.LINE_PATTERNS[1].matcher("    0.0002     115808   1. {main}() /h o m e/gapon/NetBeansProjects/PhpProject1/Calculator.php:0").matches());

        assertFalse(PhpInterpreter.LINE_PATTERNS[1].matcher("").matches());
    }

}
