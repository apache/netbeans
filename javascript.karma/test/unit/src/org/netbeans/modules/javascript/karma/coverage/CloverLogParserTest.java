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

package org.netbeans.modules.javascript.karma.coverage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.clientproject.api.jstesting.Coverage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CloverLogParserTest extends NbTestCase {

    private File sourceDir;


    public CloverLogParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        sourceDir = new File(getWorkDir(), "testdata");
        assertTrue(sourceDir.mkdirs());
    }

    @Override
    protected void tearDown() throws Exception {
        clearWorkDir();
    }

    public void testParseLog() throws Exception {
        ensureTestData();

        Reader reader = new BufferedReader(new FileReader(getCoverageLog("clover.xml")));

        List<Coverage.File> files = CloverLogParser.parse(reader, sourceDir);

        assertNotNull(files);
        assertTrue(!files.isEmpty());
        assertEquals(5, files.size());

        Coverage.File file = files.get(0);
        assertEquals(new File(sourceDir, "app/js/app.js").getAbsolutePath(), file.getPath());
        Coverage.FileMetrics metrics = file.getMetrics();
        assertNotNull(metrics);
        assertEquals(0, metrics.getLineCount());
        assertEquals(4, metrics.getStatements());
        assertEquals(1, metrics.getCoveredStatements());
        assertNotNull(file.getLines());
        assertEquals(4, file.getLines().size());
        Coverage.Line line = file.getLines().get(0);
        assertEquals(5, line.getNumber());
        assertEquals(1, line.getHitCount());
        line = file.getLines().get(1);
        assertEquals(13, line.getNumber());
        assertEquals(0, line.getHitCount());
        line = file.getLines().get(2);
        assertEquals(14, line.getNumber());
        assertEquals(0, line.getHitCount());
        line = file.getLines().get(3);
        assertEquals(15, line.getNumber());
        assertEquals(0, line.getHitCount());

        file = files.get(4);
        assertEquals(new File(sourceDir, "app/js/services.js").getAbsolutePath(), file.getPath());
        metrics = file.getMetrics();
        assertNotNull(metrics);
        assertEquals(0, metrics.getLineCount());
        assertEquals(1, metrics.getStatements());
        assertEquals(1, metrics.getCoveredStatements());
        assertNotNull(file.getLines());
        assertEquals(1, file.getLines().size());
        line = file.getLines().get(0);
        assertEquals(8, line.getNumber());
        assertEquals(1, line.getHitCount());
    }

    private void ensureTestData() throws IOException {
        FileObject fo = FileUtil.toFileObject(sourceDir);
        assertNotNull(FileUtil.createData(fo, "app/js/app.js"));
        assertNotNull(FileUtil.createData(fo, "app/js/controllers.js"));
        assertNotNull(FileUtil.createData(fo, "app/js/directives.js"));
        assertNotNull(FileUtil.createData(fo, "app/js/filters.js"));
        assertNotNull(FileUtil.createData(fo, "app/js/services.js"));
    }

    private File getCoverageLog(String filename) throws Exception {
        File coverageLog = new File(getDataDir(), filename);
        assertTrue(coverageLog.isFile());
        return coverageLog;
    }

}
