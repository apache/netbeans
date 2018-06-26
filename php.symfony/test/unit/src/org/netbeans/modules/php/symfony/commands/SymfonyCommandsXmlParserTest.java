/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
