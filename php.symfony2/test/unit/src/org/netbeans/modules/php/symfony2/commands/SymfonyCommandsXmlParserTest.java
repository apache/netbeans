/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
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
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "issue232490.xml")));

        List<SymfonyCommandVO> commands = new ArrayList<>();
        SymfonyCommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(60, commands.size());

        SymfonyCommandVO command = commands.get(0);
        assertEquals("help", command.getCommand());
        assertEquals("Displays help for a command", command.getDescription());

        command = findCommand(commands, "pl:update-files");
        assertNotNull(command);
        assertEquals("pl:update-files", command.getCommand());
        assertEquals("Update assetic + translations", command.getDescription());
        assertEquals("<html>Usage:<br>"
                + "<i>pl:update-files</i><br><br>"
                + "The <i>pl:update-files -e=prod</i> command executes these commands:<br> <br>"
                + " <i>php app/console assets:install --ansi</i><br>"
                + " <i>php app/console assetic:dump --env=prod --ansi</i><br>"
                + " <i>ITT FRISSÍTI A SZERVERRŐL A FÁJLOKAT</i><br>"
                + " <i>php app/console translation:extract --dir=./src/ --dir=./app/Resources/ --output-dir=./app/Resources/translations --keep en_US --ansi</i>", command.getHelp());

        command = commands.get(59);
        assertEquals("twig:lint", command.getCommand());
    }

    public void testIssue252901() throws Exception {
        Reader reader = new BufferedReader(new FileReader(new File(getDataDir(), "issue252901.xml")));

        List<SymfonyCommandVO> commands = new ArrayList<>();
        SymfonyCommandsXmlParser.parse(reader, commands);

        assertFalse(commands.isEmpty());
        assertSame(50, commands.size());

        SymfonyCommandVO command = commands.get(0);
        assertEquals("help", command.getCommand());
        assertEquals("Displays help for a command", command.getDescription());

        command = commands.get(8);
        assertEquals("debug:config", command.getCommand());
        assertEquals("<html>Usage:<br>"
                + "<i>debug:config [&lt;name&gt;]</i><br>"
                + "<i>config:debug</i><br><br>"
                + "The <i>debug:config</i> command dumps the current configuration for an<br> extension/bundle.<br><br>"
                + " Either the extension alias or bundle name can be used:<br><br>"
                + "   <i>php /home/gapon/NetBeansProjects/symfony2/app/console debug:config framework</i><br>"
                + "   <i>php /home/gapon/NetBeansProjects/symfony2/app/console debug:config FrameworkBundle</i>", command.getHelp());

        command = commands.get(49);
        assertEquals("translation:update", command.getCommand());
    }

    private SymfonyCommandVO findCommand(List<SymfonyCommandVO> commands, String commandName) {
        for (SymfonyCommandVO command : commands) {
            if (command.getCommand().equals(commandName)) {
                return command;
            }
        }
        return null;
    }

}
