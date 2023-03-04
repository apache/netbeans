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
package org.netbeans.modules.php.doctrine2.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class Doctrine2CommandsXmlParserTest extends NbTestCase {

    public Doctrine2CommandsXmlParserTest(String name) {
        super(name);
    }

    public void testParseCommands() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "doctrine2-commands.xml")));

        List<Doctrine2CommandVO> commands = new ArrayList<>();
        Doctrine2CommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(19, commands.size());

        Doctrine2CommandVO command = commands.get(0);
        assertEquals("help", command.getCommand());
        assertEquals("Displays help for a command", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>help [--xml] [command_name]</i><br>"
                + "<br>"
                + "The <i>help</i> command displays help for a given command:<br>"
                + "<br>"
                + "   <i>./symfony help list</i><br>"
                + "<br>"
                + " You can also output the help as XML by using the <i>--xml</i> option:<br>"
                + "<br>"
                + "   <i>./symfony help --xml list</i>", command.getHelp());

        command = commands.get(2);
        assertEquals("dbal:import", command.getCommand());
        assertEquals("Import SQL file(s) directly to Database.", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>dbal:import file1 ... [fileN]</i><br>"
                + "<br>"
                + "Import SQL file(s) directly to Database.", command.getHelp());

        command = commands.get(5);
        assertEquals("orm:clear-cache:query", command.getCommand());
        assertEquals("Clear all query cache of the various cache drivers.", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>orm:clear-cache:query</i><br>"
                + "<br>"
                + "Clear all query cache of the various cache drivers.", command.getHelp());

        command = commands.get(18);
        assertEquals("orm:validate-schema", command.getCommand());
    }

    public void testIssue213542() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "commands-issue213542.xml")));

        List<Doctrine2CommandVO> commands = new ArrayList<>();
        Doctrine2CommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(19, commands.size());
    }

    public void testIssue268899() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "commands-issue268899.xml")));

        List<Doctrine2CommandVO> commands = new ArrayList<>();
        Doctrine2CommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(20, commands.size());

        Doctrine2CommandVO command = commands.get(7);
        assertEquals("orm:convert-d1-schema", command.getCommand());
        assertEquals("Converts Doctrine 1.X schema into a Doctrine 2.X schema.", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>orm:convert-d1-schema [--from FROM] [--extend [EXTEND]] [--num-spaces [NUM-SPACES]] [--] &lt;from-path&gt; &lt;to-type&gt; &lt;dest-path&gt;</i><br>"
                + "<i>orm:convert:d1-schema</i><br>"
                + "<br>"
                + "Converts Doctrine 1.X schema into a Doctrine 2.X schema.", command.getHelp());
    }


}
