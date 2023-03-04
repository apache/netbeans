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
package org.netbeans.modules.php.analysis.parsers;

import java.io.File;
import java.util.List;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.analysis.results.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CodingStandardsFixerReportParserTest extends NbTestCase {

    public CodingStandardsFixerReportParserTest(String name) {
        super(name);
    }

    public void testParse1xFile() throws Exception {
        List<Result> results = CodingStandardsFixerReportParser.parse(getLogFile("phpcsfixer-1.x-file-log.xml"), getRoot("phpcsfixer/Baz.php"));
        assertNotNull(results);

        assertEquals(1, results.size());
        Result result = results.get(0);
        assertEquals("/home/junichi11/hg/web-main/php.code.analysis/test/unit/data/phpcsfixer/Baz.php", result.getFilePath());
        assertEquals(0, result.getColumn());
        assertEquals(1, result.getLine());
        assertEquals("phpdoc_short_description,blankline_after_open_tag,phpdoc_trim,psr0,line_after_namespace,braces", result.getCategory());
        assertEquals("<pre>"
                + "--- Original\n"
                + "+++ New\n"
                + "@@ @@\n"
                + " <?php\n"
                + "+\n"
                + "      \n"
                + "@@ @@\n"
                + " namespace Foo\\Bar;\n"
                + "+\n"
                + " /**\n"
                + "- * Description of Baz\n"
                + "- *\n"
                + "+ * Description of Baz.\n"
                + "  */\n"
                + "-class Baz2 {\n"
                + "+class Baz\n"
                + "+{\n"
                + " }\n"
                + " \n"
                + "      "
                + "</pre>", result.getDescription());
    }

    public void testParse1xDirectory() throws Exception {
        FileObject root = getRoot("phpcsfixer");
        List<Result> results = CodingStandardsFixerReportParser.parse(getLogFile("phpcsfixer-1.x-directory-log.xml"), root);
        assertNotNull(results);

        assertEquals(2, results.size());
        Result result = results.get(1);
        assertEquals(FileUtil.toFile(root.getFileObject("Qux.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(0, result.getColumn());
        assertEquals(1, result.getLine());
        assertEquals("phpdoc_short_description,blankline_after_open_tag,whitespacy_lines,phpdoc_trim,braces", result.getCategory());
        assertEquals("<pre>"
                + "--- Original\n"
                + "+++ New\n"
                + "@@ @@\n"
                + " <?php\n"
                + "+\n"
                + "      \n"
                + "@@ @@\n"
                + " /**\n"
                + "- * Description of Qux\n"
                + "- *\n"
                + "+ * Description of Qux.\n"
                + "  */\n"
                + "-class Qux {\n"
                + "-    public function some($param) {\n"
                + "-        if (true)\n"
                + "-        {\n"
                + "-            \n"
                + "+class Qux\n"
                + "+{\n"
                + "+    public function some($param)\n"
                + "+    {\n"
                + "+        if (true) {\n"
                + "         }\n"
                + "     }\n"
                + " }\n"
                + " \n"
                + "      "
                + "</pre>", result.getDescription());
    }

    public void testParse2xFile() throws Exception {
        List<Result> results = CodingStandardsFixerReportParser.parse(getLogFile("phpcsfixer-2.x-file-log.xml"), getRoot("phpcsfixer/Baz.php"));
        assertNotNull(results);

        assertEquals(1, results.size());
        Result result = results.get(0);
        assertEquals("/home/junichi11/hg/web-main/php.code.analysis/test/unit/data/phpcsfixer/Baz.php", result.getFilePath());
        assertEquals(0, result.getColumn());
        assertEquals(1, result.getLine());
        assertEquals("line_after_namespace,braces", result.getCategory());
        assertEquals("<pre>"
                + "--- Original\n"
                + "+++ New\n"
                + "@@ @@\n"
                + " namespace Foo\\Bar;\n"
                + "+\n"
                + " /**\n"
                + "  * Description of Baz\n"
                + "  *\n"
                + "  */\n"
                + "-class Baz2 {\n"
                + "+class Baz2\n"
                + "+{\n"
                + " }\n"
                + " \n"
                + "      "
                + "</pre>", result.getDescription());
    }

    public void testParse2xDirectory() throws Exception {
        FileObject root = getRoot("phpcsfixer");
        List<Result> results = CodingStandardsFixerReportParser.parse(getLogFile("phpcsfixer-2.x-directory-log.xml"), root);
        assertNotNull(results);

        assertEquals(2, results.size());
        Result result = results.get(1);
        assertEquals(FileUtil.toFile(root.getFileObject("Qux.php")).getAbsolutePath(), result.getFilePath());
        assertEquals(0, result.getColumn());
        assertEquals(1, result.getLine());
        assertEquals("braces", result.getCategory());
        assertEquals("<pre>"
                + "--- Original\n"
                + "+++ New\n"
                + "@@ @@\n"
                + "  */\n"
                + "-class Qux {\n"
                + "-    public function some($param) {\n"
                + "-        if (true)\n"
                + "-        {\n"
                + "-            \n"
                + "+class Qux\n"
                + "+{\n"
                + "+    public function some($param)\n"
                + "+    {\n"
                + "+        if (true) {\n"
                + "         }\n"
                + "     }\n"
                + " }\n"
                + " \n"
                + "      "
                + "</pre>", result.getDescription());
    }

    private File getLogFile(String name) throws Exception {
        assertNotNull(name);
        File phpcsfixer = new File(getDataDir(), "phpcsfixer");
        File xmlLog = new File(phpcsfixer, name);
        assertTrue(xmlLog.isFile());
        return xmlLog;
    }

    private FileObject getRoot(String name) {
        assertNotNull(name);
        FileObject dataDir = FileUtil.toFileObject(getDataDir());
        return dataDir.getFileObject(name);
    }

}
