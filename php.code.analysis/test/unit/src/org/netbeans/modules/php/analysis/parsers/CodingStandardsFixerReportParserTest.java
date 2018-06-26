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
