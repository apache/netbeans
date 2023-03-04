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

    public void testErrors4() throws Exception {
        checkErrors("testfiles/error4.yaml");
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
                assertTrue(result.getItems().isEmpty());
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

    private String replacePhpFragments(String source) {
        StringBuilder sb = new StringBuilder(source);
        YamlParser.replacePhpFragments(sb);
        return sb.toString();
    }
    
    public void testReplacePhpFragments() {
        assertEquals("", replacePhpFragments(""));
        assertEquals("foo bar", replacePhpFragments("foo bar"));
        assertEquals("?>", replacePhpFragments("?>"));
        assertEquals("<?", replacePhpFragments("<?"));
        assertEquals("foo ?>", replacePhpFragments("foo ?>"));
        assertEquals("<? bar", replacePhpFragments("<? bar"));

        assertEquals("    ", replacePhpFragments("<??>"));
        assertEquals("foo:    ", replacePhpFragments("foo:<??>"));
        assertEquals("foo:                   ", replacePhpFragments("foo:<? here goes php ?>"));
        assertEquals("foo           baz", replacePhpFragments("foo <? bar ?> baz"));

        assertEquals("        ", replacePhpFragments("<??><??>"));
        assertEquals("foo:    bar:       qux", replacePhpFragments("foo:<??>bar:<?baz?>qux"));
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
        long start = System.nanoTime();
        YamlParser.replacePhpFragments(source);
        long time = System.nanoTime() - start;
        // takes about 30 ms on my laptop, so I suppose 300 ms should
        // be enough pretty much on any machine
        assertTrue("Slow replacing of php fragments: " + time + " ms", time < 300_000_000L);
    }
}
