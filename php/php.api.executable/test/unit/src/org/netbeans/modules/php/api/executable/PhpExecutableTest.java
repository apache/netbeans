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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Pair;

public class PhpExecutableTest extends NbTestCase {

    public PhpExecutableTest(String name) {
        super(name);
    }

    @Test
    public void testParseNullCommand() {
        Pair<String, List<String>> command = PhpExecutable.parseCommand(null);
        assertEquals("", command.first());
        assertTrue(command.second().isEmpty());
    }

    @Test
    public void testParseEmptyCommand() {
        Pair<String, List<String>> command = PhpExecutable.parseCommand("");
        assertEquals("", command.first());
        assertTrue(command.second().isEmpty());
    }

    @Test
    public void testParseOnlyCommand() {
        Pair<String, List<String>> command = PhpExecutable.parseCommand("/usr/bin/php");
        assertEquals("/usr/bin/php", command.first());
        assertTrue(command.second().isEmpty());
    }

    @Test
    public void testParseCommandWithParam1() {
        Pair<String, List<String>> command = PhpExecutable.parseCommand("/usr/bin/php --verbose");
        assertEquals("/usr/bin/php", command.first());
        assertEquals(1, command.second().size());
        assertEquals("--verbose", command.second().get(0));
    }

    @Test
    public void testParseCommandWithParam2() {
        Pair<String, List<String>> command = PhpExecutable.parseCommand("/usr/bin/php /verbose");
        assertEquals("/usr/bin/php", command.first());
        assertEquals(1, command.second().size());
        assertEquals("/verbose", command.second().get(0));
    }

    @Test
    public void testParseCommandWithParams1() {
        Pair<String, List<String>> command = PhpExecutable.parseCommand("/usr/bin/php --verbose --version");
        assertEquals("/usr/bin/php", command.first());
        assertEquals(2, command.second().size());
        assertEquals("--verbose", command.second().get(0));
        assertEquals("--version", command.second().get(1));
    }

    @Test
    public void testParseCommandWithParams2() {
        Pair<String, List<String>> command = PhpExecutable.parseCommand("/usr/bin/php /verbose /version");
        assertEquals("/usr/bin/php", command.first());
        assertEquals(2, command.second().size());
        assertEquals("/verbose", command.second().get(0));
        assertEquals("/version", command.second().get(1));
    }

    public void testInfoCommand() {
        List<String> fullCommand = Arrays.asList(
                "/usr/bin/php",
                "/usr/bin/phpunit",
                "--colors",
                "a=b",
                "a=\"b\"");
        assertEquals("\"/usr/bin/php\" \"/usr/bin/phpunit\" \"--colors\" \"a=b\" \"a=\\\"b\\\"\"", PhpExecutable.InfoInputProcessor.getInfoCommand(fullCommand));
    }

}
