/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.codeception.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

public class CodeceptionCoverageLogParserTest extends NbTestCase {

    public CodeceptionCoverageLogParserTest(String name) {
        super(name);
    }

    /**
     * Test of parse method, of class CodeceptionCoverageLogParser.
     */
    @Test
    public void testParseLog() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getCoverageLog("codeception-coverage.xml")));
        CoverageImpl coverage = new CoverageImpl();

        CodeceptionCoverageLogParser.parse(reader, coverage);
        assertEquals(1436766630, coverage.getGenerated());
        assertEquals(3, coverage.getFiles().size());

        // 1st file
        CoverageImpl.FileImpl file = (CoverageImpl.FileImpl) coverage.getFiles().get(0);
        assertEquals("/home/junichi11/NetBeansProjects/codeception/src/FizzBuzz.php", file.getPath());
        assertEquals(1, file.getClasses().size());

        // class
        CoverageImpl.ClassImpl clazz = file.getClasses().get(0);
        assertEquals("FizzBuzz", clazz.getName());
        assertEquals("App", clazz.getNamespace());

        // metrics
        ClassMetricsImpl classMetrics = clazz.getMetrics();
        assertNotNull(classMetrics);
        assertEquals(3, classMetrics.getMethods());
        assertEquals(3, classMetrics.getCoveredMethods());
        assertEquals(0, classMetrics.getConditionals());
        assertEquals(0, classMetrics.getCoveredConditionals());
        assertEquals(16, classMetrics.getStatements());
        assertEquals(16, classMetrics.getCoveredStatements());
        assertEquals(19, classMetrics.getElements());
        assertEquals(19, classMetrics.getCoveredElements());

        // line
        assertEquals(19, file.getLines().size());
        CoverageImpl.LineImpl line = (CoverageImpl.LineImpl) file.getLines().get(0);
        assertEquals(12, line.getNumber());
        assertEquals("method", line.getType());
        assertEquals("__construct", line.getName());
        assertEquals(1, line.getCrap());
        assertEquals(3, line.getHitCount());

        line = (CoverageImpl.LineImpl) file.getLines().get(1);
        assertEquals(13, line.getNumber());
        assertEquals("stmt", line.getType());
        assertEquals(null, line.getName());
        assertEquals(-1, line.getCrap());
        assertEquals(3, line.getHitCount());

        // file metrics
        FileMetricsImpl fileMetrics = (FileMetricsImpl) file.getMetrics();
        assertNotNull(fileMetrics);
        assertEquals(42, fileMetrics.getLineCount());
        assertEquals(31, fileMetrics.getNcloc());
        assertEquals(1, fileMetrics.getClasses());
        assertEquals(3, fileMetrics.getMethods());
        assertEquals(3, fileMetrics.getCoveredMethods());
        assertEquals(0, fileMetrics.getConditionals());
        assertEquals(0, fileMetrics.getCoveredConditionals());
        assertEquals(16, fileMetrics.getStatements());
        assertEquals(16, fileMetrics.getCoveredStatements());
        assertEquals(19, fileMetrics.getElements());
        assertEquals(19, fileMetrics.getCoveredElements());

        // 2nd file
        file = (CoverageImpl.FileImpl) coverage.getFiles().get(1);
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/_support/UnitTester.php", file.getPath());
        assertEquals(1, file.getClasses().size());

        // class
        clazz = file.getClasses().get(0);
        assertEquals("UnitTester", clazz.getName());
        assertEquals("global", clazz.getNamespace());

        // metrics
        classMetrics = clazz.getMetrics();
        assertNotNull(classMetrics);
        assertEquals(0, classMetrics.getMethods());
        assertEquals(0, classMetrics.getCoveredMethods());
        assertEquals(0, classMetrics.getConditionals());
        assertEquals(0, classMetrics.getCoveredConditionals());
        assertEquals(0, classMetrics.getStatements());
        assertEquals(0, classMetrics.getCoveredStatements());
        assertEquals(0, classMetrics.getElements());
        assertEquals(0, classMetrics.getCoveredElements());

        // line
        assertEquals(0, file.getLines().size());

        // file metrics
        fileMetrics = (FileMetricsImpl) file.getMetrics();
        assertNotNull(fileMetrics);
        assertEquals(26, fileMetrics.getLineCount());
        assertEquals(8, fileMetrics.getNcloc());
        assertEquals(1, fileMetrics.getClasses());
        assertEquals(0, fileMetrics.getMethods());
        assertEquals(0, fileMetrics.getCoveredMethods());
        assertEquals(0, fileMetrics.getConditionals());
        assertEquals(0, fileMetrics.getCoveredConditionals());
        assertEquals(0, fileMetrics.getStatements());
        assertEquals(0, fileMetrics.getCoveredStatements());
        assertEquals(0, fileMetrics.getElements());
        assertEquals(0, fileMetrics.getCoveredElements());

        // 3rd file
        file = (CoverageImpl.FileImpl) coverage.getFiles().get(2);
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/_support/_generated/UnitTesterActions.php", file.getPath());
        assertEquals(1, file.getClasses().size());

        // class
        clazz = file.getClasses().get(0);
        assertEquals("UnitTesterActions", clazz.getName());
        assertEquals("_generated", clazz.getNamespace());

        // metrics
        classMetrics = clazz.getMetrics();
        assertNotNull(classMetrics);
        assertEquals(23, classMetrics.getMethods());
        assertEquals(0, classMetrics.getCoveredMethods());
        assertEquals(0, classMetrics.getConditionals());
        assertEquals(0, classMetrics.getCoveredConditionals());
        assertEquals(23, classMetrics.getStatements());
        assertEquals(0, classMetrics.getCoveredStatements());
        assertEquals(46, classMetrics.getElements());
        assertEquals(0, classMetrics.getCoveredElements());

        // line
        assertEquals(46, file.getLines().size());
        line = (CoverageImpl.LineImpl) file.getLines().get(0);
        assertEquals(31, line.getNumber());
        assertEquals("method", line.getType());
        assertEquals("assertEquals", line.getName());
        assertEquals(2, line.getCrap());
        assertEquals(0, line.getHitCount());

        // file metrics
        fileMetrics = (FileMetricsImpl) file.getMetrics();
        assertNotNull(fileMetrics);
        assertEquals(348, fileMetrics.getLineCount());
        assertEquals(121, fileMetrics.getNcloc());
        assertEquals(1, fileMetrics.getClasses());
        assertEquals(23, fileMetrics.getMethods());
        assertEquals(0, fileMetrics.getCoveredMethods());
        assertEquals(0, fileMetrics.getConditionals());
        assertEquals(0, fileMetrics.getCoveredConditionals());
        assertEquals(23, fileMetrics.getStatements());
        assertEquals(0, fileMetrics.getCoveredStatements());
        assertEquals(46, fileMetrics.getElements());
        assertEquals(0, fileMetrics.getCoveredElements());
    }

    private File getCoverageLog(String filename) throws Exception {
        File coverageLog = new File(getDataDir(), filename);
        assertTrue(coverageLog.isFile());
        return coverageLog;
    }

}
