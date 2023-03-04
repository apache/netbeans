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
package org.netbeans.modules.php.atoum.commands;

import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;

public class AtoumTest {

    @Test
    public void testLinePatternUnix() {
        String line = "/home/gapon/NetBeansProjects/atoum-sample/vendor/atoum/atoum/classes/test.php:838";
        Matcher matcher = Atoum.LINE_PATTERN.matcher(line);
        assertTrue(matcher.matches());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/vendor/atoum/atoum/classes/test.php", matcher.group(1));
        assertEquals("838", matcher.group(2));
    }

    @Test
    public void testLinePatternWin() {
        String line = "C:\\Program Files\\myprojects\\test.php:838";
        Matcher matcher = Atoum.LINE_PATTERN.matcher(line);
        assertTrue(matcher.matches());
        assertEquals("C:\\Program Files\\myprojects\\test.php", matcher.group(1));
        assertEquals("838", matcher.group(2));
    }

}
