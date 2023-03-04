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
package org.netbeans.modules.php.symfony2.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class SymfonyCommandsXmlParserTest extends NbTestCase {

    public SymfonyCommandsXmlParserTest(String name) {
        super(name);
    }

    public void testParseCommands() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "symfony2-commands.xml")));

        List<SymfonyCommandVO> commands = new ArrayList<>();
        SymfonyCommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(30, commands.size());

        SymfonyCommandVO command = commands.get(0);
        assertEquals("help", command.getCommand());
        assertEquals("Displays help for a command", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>help [--xml] [command_name]</i><br>"
                + "<br>"
                + "The <i>help</i> command displays help for a given command:<br>"
                + " <br>"
                + "   <i>php app/console help list</i><br>"
                + " <br>"
                + " You can also output the help as XML by using the <i>--xml</i> option:<br>"
                + " <br>"
                + "   <i>php app/console help --xml list</i>", command.getHelp());

        command = commands.get(2);
        assertEquals("assetic:dump", command.getCommand());
        assertEquals("Dumps all assets to the filesystem", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>assetic:dump [--watch] [--force] [--period=\"...\"] [write_to]</i><br>"
                + "<br>", command.getHelp());

        command = commands.get(5);
        assertEquals("cache:warmup", command.getCommand());
        assertEquals("Warms up an empty cache", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>cache:warmup</i><br>"
                + "<br>"
                + "The <i>cache:warmup</i> command warms up the cache.<br>"
                + " <br>"
                + " Before running this command, the cache must be empty.", command.getHelp());

        command = commands.get(29);
        assertEquals("swiftmailer:spool:send", command.getCommand());
    }

    public void testIssue223639() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "issue223639.xml")));

        List<SymfonyCommandVO> commands = new ArrayList<>();
        SymfonyCommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(36, commands.size());

        SymfonyCommandVO command = commands.get(0);
        assertEquals("help", command.getCommand());
        assertEquals("Displays help for a command", command.getDescription());

        command = commands.get(35);
        assertEquals("twig:lint", command.getCommand());
    }

    public void testIssue232490() throws Exception {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(getDataDir(), "issue232490.xml")), StandardCharsets.UTF_8));

        List<SymfonyCommandVO> commands = new ArrayList<>();
        SymfonyCommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(30, commands.size());

        SymfonyCommandVO command = commands.get(0);
        assertEquals("help", command.getCommand());
        assertEquals("Displays help for a command", command.getDescription());

        command = findCommand(commands, "assetic:dump");
        assertNotNull(command);
        assertEquals("assetic:dump", command.getCommand());
        assertEquals("Zapíše všechny assety do souborů.", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>assetic:dump [--watch] [--force] [--period=\"...\"] [write_to]</i><br><br>"
                + "Příliš žluťoučký kůň úpěl ďábelské ódy.", command.getHelp());

        command = commands.get(21);
        assertEquals("doctrine:query:sql", command.getCommand());
    }

//    public void testIssue252901() throws Exception {
//        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "issue252901.xml")));
//
//        List<SymfonyCommandVO> commands = new ArrayList<>();
//        SymfonyCommandsXmlParser.parse(reader, commands);
//
//        assertFalse(commands.isEmpty());
//        assertSame(50, commands.size());
//
//        SymfonyCommandVO command = commands.get(0);
//        assertEquals("help", command.getCommand());
//        assertEquals("Displays help for a command", command.getDescription());
//
//        command = commands.get(8);
//        assertEquals("debug:config", command.getCommand());
//        assertEquals("<html>Usage:<br>"
//                + "<i>debug:config [&lt;name&gt;]</i><br>"
//                + "<i>config:debug</i><br><br>"
//                + "The <i>debug:config</i> command dumps the current configuration for an<br> extension/bundle.<br><br>"
//                + " Either the extension alias or bundle name can be used:<br><br>"
//                + "   <i>php /home/gapon/NetBeansProjects/symfony2/app/console debug:config framework</i><br>"
//                + "   <i>php /home/gapon/NetBeansProjects/symfony2/app/console debug:config FrameworkBundle</i>", command.getHelp());
//
//        command = commands.get(49);
//        assertEquals("translation:update", command.getCommand());
//    }

    private SymfonyCommandVO findCommand(List<SymfonyCommandVO> commands, String commandName) {
        for (SymfonyCommandVO command : commands) {
            if (command.getCommand().equals(commandName)) {
                return command;
            }
        }
        return null;
    }

}
