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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.yaml;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class YamlParserTest extends YamlTestBase {

    public YamlParserTest(String testName) {
        super(testName);
    }

    public void testErrors1() throws Exception {
        checkErrors("testfiles/error.yaml");
    }

    public void testErrors2() throws Exception {
        checkErrors("testfiles/error2.yaml");
    }

    public void testErrors3() throws Exception {
        checkErrors("testfiles/error3.yaml");
    }

    public void testIssue232192_01() throws Exception {
        checkErrors("testfiles/issue232192_01.yaml");
    }

    public void testIssue232192_02() throws Exception {
        checkErrors("testfiles/issue232192_02.yaml");
    }

    public void testHuge() throws Exception {
        StringBuilder sb = new StringBuilder();
        String s = readFile(getTestFile("testfiles/database.yml"));
        while (sb.length() < 1024 * 1024) {
            sb.append(s);
        }
        String huge = sb.toString();
        final String relFilePath = "generated-huge.yml";
        FileObject workDir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        createFile(workDir, relFilePath, huge);

        FileObject f = workDir.getFileObject(relFilePath);
        Source source = Source.create(f);

        ParserManager.parse(Collections.singleton(source), new UserTask() {

            public @Override
            void run(ResultIterator resultIterator) throws Exception {
                YamlParserResult result = (YamlParserResult) resultIterator.getParserResult();

                assertNotNull(result);

                String text = result.getSnapshot().getText().toString();
                assertNotNull(text);

                List<? extends org.netbeans.modules.csl.api.Error> diagnostics = result.getDiagnostics();
                String annotatedSource = annotateErrors(diagnostics);
                assertDescriptionMatches("testfiles/" + relFilePath, annotatedSource, false, ".errors", false);
                // Make sure we actually skipped parsing this large document!
                assertTrue(result.getRootNodes().size() == 0);
            }
        });
    }

    public void testValidResult() throws Exception {
        // Make sure we get a valid parse result out of an aborted parse
        FileObject fo = getTestFile("testfiles/error3.yaml");

        Source source = Source.create(fo);

        YamlParser parser = new YamlParser() {

            YamlParserResult parse(String source, Snapshot snapshot) {
                throw new RuntimeException("Very bad thing");
            }
        };

        parser.parse(source.createSnapshot(), null, null);

        assertNotNull("Parser result must be nonnull", parser.getResult(null));
    }

    public void testReplacePhpFragments() {
        assertEquals("", YamlParser.replacePhpFragments(""));
        assertEquals("foo bar", YamlParser.replacePhpFragments("foo bar"));
        assertEquals("?>", YamlParser.replacePhpFragments("?>"));
        assertEquals("<?", YamlParser.replacePhpFragments("<?"));
        assertEquals("foo ?>", YamlParser.replacePhpFragments("foo ?>"));
        assertEquals("<? bar", YamlParser.replacePhpFragments("<? bar"));

        assertEquals("    ", YamlParser.replacePhpFragments("<??>"));
        assertEquals("foo:    ", YamlParser.replacePhpFragments("foo:<??>"));
        assertEquals("foo:                   ", YamlParser.replacePhpFragments("foo:<? here goes php ?>"));
        assertEquals("foo           baz", YamlParser.replacePhpFragments("foo <? bar ?> baz"));

        assertEquals("        ", YamlParser.replacePhpFragments("<??><??>"));
        assertEquals("foo:    bar:       qux", YamlParser.replacePhpFragments("foo:<??>bar:<?baz?>qux"));
    }

    public void testReplacePhpFragmentsPerformance() {
        StringBuilder source = new StringBuilder();
        // generate a big file with some php in it
        for (int i = 0; i < 50000; i++) {
            source.append("something\n");
            if (i % 100 == 0) {
                source.append("<? php here ?>");
            }
        }
        long start = System.currentTimeMillis();
        YamlParser.replacePhpFragments(source.toString());
        long time = System.currentTimeMillis() - start;
        // takes about 30 ms on my laptop, so I suppose 300 ms should
        // be enough pretty much on any machine
        assertTrue("Slow replacing of php fragments: " + time + " ms", time < 300);
    }
}
