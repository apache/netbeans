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

package org.netbeans.modules.php.symfony.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tomas Mysik
 */
public class SymfonyCommandsXmlParserTest extends NbTestCase {

    public SymfonyCommandsXmlParserTest(String name) {
        super(name);
    }

    public void testParseCommands() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "symfony-commands.xml")));

        List<SymfonyCommandVO> commands = new ArrayList<>();
        SymfonyCommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(62, commands.size());

        SymfonyCommandVO command = commands.get(0);
        assertEquals("help", command.getCommand());
        assertEquals("Displays help for a task", command.getDescription());

        command = commands.get(10);
        assertEquals("doctrine:build-all-reload-test-all", command.getCommand());
        assertEquals("Generates Doctrine model, SQL, initializes database, load data and run all tests", command.getDescription());

        command = commands.get(61);
        assertEquals("test:unit", command.getCommand());
        assertEquals("Launches unit tests", command.getDescription());
    }

//    public void testParseCommandsIssue179717() throws Exception {
//        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "symfony-commands-issue179717.xml")));
//
//        List<SymfonyCommandVO> commands = new ArrayList<>();
//        SymfonyCommandsXmlParser.parse(reader, commands);
//
//        assertFalse(commands.isEmpty());
//        assertSame(82, commands.size());
//
//        SymfonyCommandVO command = commands.get(0);
//        assertEquals("help", command.getCommand());
//        assertEquals("Displays help for a task", command.getDescription());
//
//        command = commands.get(9);
//        assertEquals("apostrophe:repair-tree", command.getCommand());
//        assertEquals("", command.getDescription());
//
//        command = commands.get(10);
//        assertEquals("apostrophe:ssh", command.getCommand());
//        assertEquals("Opens an interactive ssh connection to the specified server using the username, port and hostname in properties.ini", command.getDescription());
//
//        command = commands.get(81);
//        assertEquals("test:unit", command.getCommand());
//        assertEquals("Launches unit tests", command.getDescription());
//    }
}
