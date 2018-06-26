/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
