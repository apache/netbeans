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
package org.netbeans.modules.php.analysis.parsers;

import java.io.File;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.analysis.results.Result;

public class CodeSnifferReportParserTest extends NbTestCase {

    public CodeSnifferReportParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        List<Result> results = CodeSnifferReportParser.parse(getLogFile("phpcs-log.xml"));
        assertNotNull(results);

        assertEquals(9, results.size());
        Result result = results.get(0);
        assertEquals("/home/gapon/NetBeansProjects/_important/TodoList/config/Config.php", result.getFilePath());
        assertEquals(48, result.getLine());
        assertEquals(7, result.getColumn());
        assertEquals("PSR1: Classes > ClassDeclaration > MissingNamespace", result.getCategory());
        assertEquals("Each class must be in a namespace of at least one level (a top-level vendor name)", result.getDescription());


        result = results.get(6);
        assertEquals("/home/gapon/NetBeansProjects/_important/TodoList/exception/MyPresenter.php", result.getFilePath());
        assertEquals(1, result.getLine());
        assertEquals(1, result.getColumn());
        assertEquals("PSR1: Files > SideEffects > FoundWithSymbols", result.getCategory());
        assertEquals("A file should declare new symbols (classes, functions, constants, etc.) and cause no other side effects, "
                + "or it should execute logic with side effects, but should not do both. The first symbol is defined on line 15 "
                + "and the first side effect is on line 19.", result.getDescription());
    }

    public void testParseLogWithSummary1() throws Exception {
        List<Result> results = CodeSnifferReportParser.parse(getLogFile("phpcs-log-with-summary-1.xml"));
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testParseLogWithSummary2() throws Exception {
        List<Result> results = CodeSnifferReportParser.parse(getLogFile("phpcs-log-with-summary-2.xml"));
        assertNotNull(results);
        assertEquals(1, results.size());

        Result result = results.get(0);
        assertEquals("/home/gapon/Download/PHP_CodeSniffer-master/CodeSniffer/DocGenerators/Generator.php", result.getFilePath());
        assertEquals(31, result.getLine());
        assertEquals(56, result.getColumn());
        assertEquals("PEAR: Classes > ClassDeclaration > OpenBraceNewLine", result.getCategory());
        assertEquals("Opening brace of a class must be on the line after the definition", result.getDescription());
    }

    private File getLogFile(String name) throws Exception {
        assertNotNull(name);
        File xmlLog = new File(getDataDir(), name);
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }

}
