/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.atoum.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.coverage.FileMetrics;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class CloverLogParserTest extends NbTestCase {

    public CloverLogParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        Reader reader = new BufferedReader(new FileReader(getCoverageLog("coverage.xml")));

        List<Coverage.File> files = CloverLogParser.parse(reader);

        assertNotNull(files);
        assertTrue(!files.isEmpty());
        assertEquals(2, files.size());

        Coverage.File file = files.get(0);
        assertEquals(new File(getDataDir(), "testdata/Calculator.php").getAbsolutePath(), file.getPath());
        FileMetrics metrics = file.getMetrics();
        assertNotNull(metrics);
        assertEquals(68, metrics.getLineCount());
        assertEquals(6, metrics.getStatements());
        assertEquals(6, metrics.getCoveredStatements());
        assertNotNull(file.getLines());
        assertEquals(11, file.getLines().size());
        Coverage.Line line = file.getLines().get(0);
        assertEquals(50, line.getNumber());
        assertEquals(1, line.getHitCount());
        line = file.getLines().get(1);
        assertEquals(51, line.getNumber());
        assertEquals(0, line.getHitCount());
        line = file.getLines().get(10);
        assertEquals(66, line.getNumber());
        assertEquals(0, line.getHitCount());

        file = files.get(1);
        assertEquals(new File(getDataDir(), "testdata/Calculator2.php").getAbsolutePath(), file.getPath());
        metrics = file.getMetrics();
        assertNotNull(metrics);
        assertEquals(65, metrics.getLineCount());
        assertEquals(6, metrics.getStatements());
        assertEquals(4, metrics.getCoveredStatements());
        assertNotNull(file.getLines());
        assertEquals(11, file.getLines().size());
        line = file.getLines().get(0);
        assertEquals(50, line.getNumber());
        assertEquals(0, line.getHitCount());
        line = file.getLines().get(2);
        assertEquals(54, line.getNumber());
        assertEquals(1, line.getHitCount());
        line = file.getLines().get(10);
        assertEquals(66, line.getNumber());
        assertEquals(0, line.getHitCount());
    }

    private File getCoverageLog(String filename) throws Exception {
        File coverageLog = new File(getDataDir(), filename);
        assertTrue(coverageLog.isFile());
        fixContent(coverageLog);
        return coverageLog;
    }

    private void fixContent(File file) throws Exception {
        Path path = file.toPath();
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll("%WORKDIR%", getDataDir().getAbsolutePath());
        Files.write(path, content.getBytes(charset));
    }

}
