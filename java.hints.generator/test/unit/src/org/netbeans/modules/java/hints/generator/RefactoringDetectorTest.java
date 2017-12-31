/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.generator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.hints.generator.RefactoringDetector.EditorPeer;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class RefactoringDetectorTest extends TestBase {

    public RefactoringDetectorTest(String name) {
        super(name);
    }

    public void testStreaming() throws Exception {
        RefactoringDetector.minimalFacts = 1;

        new StreamingTestBase()
            .addFile("test/record/TestRecord.java", "package test.record;\n"+
            "\n"+
            "import java.util.Locale;\n"+
            "\n"+
            "public class TestRecord {\n"+
            "\n"+
            "    private String t1(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t2(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t3(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "}\n")
            .baseline()
            .insert("test/record/TestRecord.java", 142, "L")
            .insert("test/record/TestRecord.java", 143, "o")
            .parse()
            .insert("test/record/TestRecord.java", 144, "c")
            .parse()
            .insert("test/record/TestRecord.java", 145, "f")
            .parse()
            .delete("test/record/TestRecord.java", 145, 1)
            .parse()
            .insert("test/record/TestRecord.java", 145, "a")
            .insert("test/record/TestRecord.java", 146, "l")
            .insert("test/record/TestRecord.java", 147, "e")
            .parse()
            .insert("test/record/TestRecord.java", 148, ".")
            .parse()
            .insert("test/record/TestRecord.java", 149, "U")
            .parse()
            .delete("test/record/TestRecord.java", 149, 1)
            .insert("test/record/TestRecord.java", 149, "US")
            .parse("$1.toLowerCase()");
    }

    public void testStreaming2() throws Exception {
        RefactoringDetector.minimalFacts = 1;

        new StreamingTestBase()
            .addFile("test/record/TestRecord.java", "package test.record;\n"+
            "\n"+
            "import java.util.Locale;\n"+
            "\n"+
            "public class TestRecord {\n"+
            "\n"+
            "    private String t1(String str) {\n"+
            "        return str;\n"+
            "    }\n"+
            "    \n"+
            "    private String t2(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t3(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "}\n")
            .baseline()
            .insert("test/record/TestRecord.java", 129, ".")
            .insert("test/record/TestRecord.java", 130, "t")
            .insert("test/record/TestRecord.java", 131, "o")
            .parse()
            .insert("test/record/TestRecord.java", 132, "L")
            .parse()
            .delete("test/record/TestRecord.java", 130, 3)
            .insert("test/record/TestRecord.java", 130, "toLowerCase()")
            .parse()
            .insert("test/record/TestRecord.java", 142, "L")
            .insert("test/record/TestRecord.java", 143, "o")
            .insert("test/record/TestRecord.java", 144, "c")
            .insert("test/record/TestRecord.java", 145, "a")
            .insert("test/record/TestRecord.java", 146, "l")
            .insert("test/record/TestRecord.java", 147, "e")
            .insert("test/record/TestRecord.java", 148, ".")
            .parse()
            .insert("test/record/TestRecord.java", 149, "U")
            .insert("test/record/TestRecord.java", 150, "S")
            .parse("$1.toLowerCase()");
    }

    public void testStreamingPatternConfirming() throws Exception {
        RefactoringDetector.minimalFacts = 1;

        new StreamingTestBase()
            .addFile("test/record/TestRecord.java", "package test.record;\n"+
            "\n"+
            "import java.util.Locale;\n"+
            "\n"+
            "public class TestRecord {\n"+
            "\n"+
            "    private String t1(String str) {\n"+
            "        return str;\n"+
            "    }\n"+
            "}\n")
            .addFile("test/record/TestRecord2.java", "package test.record;\n"+
            "import java.util.Locale;\n"+
            "public class TestRecord2 {\n"+
            "    private String t2(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "}\n")
            .baseline()
            .insert("test/record/TestRecord.java", 129, ".")
            .insert("test/record/TestRecord.java", 130, "t")
            .insert("test/record/TestRecord.java", 131, "o")
            .parse()
            .insert("test/record/TestRecord.java", 132, "L")
            .parse()
            .delete("test/record/TestRecord.java", 130, 3)
            .insert("test/record/TestRecord.java", 130, "toLowerCase()")
            .parse()
            .insert("test/record/TestRecord.java", 142, "L")
            .insert("test/record/TestRecord.java", 143, "o")
            .insert("test/record/TestRecord.java", 144, "c")
            .insert("test/record/TestRecord.java", 145, "a")
            .insert("test/record/TestRecord.java", 146, "l")
            .insert("test/record/TestRecord.java", 147, "e")
            .insert("test/record/TestRecord.java", 148, ".")
            .parse()
            .insert("test/record/TestRecord.java", 149, "U")
            .insert("test/record/TestRecord.java", 150, "S")
            .parse("$1.toLowerCase()");
    }

    public void testMinimalFactsSingleFile() throws Exception {
        RefactoringDetector.minimalFacts = 3;

        new StreamingTestBase()
            .addFile("test/record/TestRecord.java", "package test.record;\n"+
            "\n"+
            "import java.util.Locale;\n"+
            "\n"+
            "public class TestRecord {\n"+
            "\n"+
            "    private String t1(String str) {\n"+
            "        return str;\n"+
            "    }\n"+
            "    \n"+
            "    private String t2(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t3(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t4(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t5(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "}\n")
            .baseline()
            .insert("test/record/TestRecord.java", 209, "Locale.US")
            .parse()
            .insert("test/record/TestRecord.java", 299, "Locale.US")
            .parse()
            .insert("test/record/TestRecord.java", 389, "Locale.US")
            .parse("$1.toLowerCase()");
    }

    public void testMinimalFactsMultipleFiles() throws Exception {
        RefactoringDetector.minimalFacts = 3;

        new StreamingTestBase()
            .addFile("test/record/TestRecord1.java", "package test.record;\n"+
            "\n"+
            "import java.util.Locale;\n"+
            "\n"+
            "public class TestRecord1 {\n"+
            "\n"+
            "    private String t1(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t2(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "}\n")
            .addFile("test/record/TestRecord2.java", "package test.record;\n"+
            "\n"+
            "import java.util.Locale;\n"+
            "\n"+
            "public class TestRecord2 {\n"+
            "\n"+
            "    private String t1(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t2(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "}\n")
            .addFile("test/record/TestRecord3.java", "package test.record;\n"+
            "\n"+
            "import java.util.Locale;\n"+
            "\n"+
            "public class TestRecord3 {\n"+
            "\n"+
            "    private String t1(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "    \n"+
            "    private String t2(String str) {\n"+
            "        return str.toLowerCase();\n"+
            "    }\n"+
            "}\n")
            .baseline()
            .insert("test/record/TestRecord1.java", 143, "Locale.US")
            .parseFile("test/record/TestRecord1.java")
            .insert("test/record/TestRecord2.java", 143, "Locale.US")
            .parseFile("test/record/TestRecord2.java")
            .insert("test/record/TestRecord3.java", 143, "Locale.US")
            .parseFile("test/record/TestRecord3.java", "$1.toLowerCase()");
    }

    private final class StreamingTestBase {
        private final FileObject sourceRoot;
        private FileObject mainFile;
        private EditorPeer detector;

        public StreamingTestBase() throws Exception {
            sourceRoot = setUpTest();
        }

        public StreamingTestBase addFile(String fileName, String content) throws Exception {
            FileObject file = FileUtil.createData(sourceRoot, fileName);
            if (mainFile == null) {
                mainFile = file;
            }
            copyStringToFile(file, content);
            IndexingManager.getDefault().refreshAllIndices(true, true, sourceRoot);
            SourceUtils.waitScanFinished();
            return this;
        }

        public StreamingTestBase insert(String fileName, int pos, String text) throws Exception {
            FileObject file = FileUtil.createData(sourceRoot, fileName);
            String content = copyFileToString(file);
            content = content.substring(0, pos) + text + content.substring(pos);
            copyStringToFile(file, content);
            IndexingManager.getDefault().refreshAllIndices(true, true, sourceRoot); //XXX: timestamps!
            return this;
        }

        public StreamingTestBase delete(String fileName, int pos, int len) throws Exception {
            FileObject file = FileUtil.createData(sourceRoot, fileName);
            String content = copyFileToString(file);
            content = content.substring(0, pos) + content.substring(pos + len);
            copyStringToFile(file, content);
            IndexingManager.getDefault().refreshAllIndices(true, true, sourceRoot); //XXX: timestamps!
            return this;
        }

        public StreamingTestBase baseline() throws Exception {
            JavaSource.forFileObject(mainFile).runUserActionTask(cc -> {
                cc.toPhase(Phase.RESOLVED); //XXX
                detector = RefactoringDetector.editorPeer(cc);
            }, true);
            
            return this;
        }

        public StreamingTestBase parseFile(String fileName, String... expectedPatterns) throws Exception {
            FileObject file = FileUtil.createData(sourceRoot, fileName);

            return parse(file, expectedPatterns);
        }

        public StreamingTestBase parse(String... expectedPatterns) throws Exception {
            return parse(mainFile, expectedPatterns);
        }

        private StreamingTestBase parse(FileObject file, String... expectedPatterns) throws Exception {
            JavaSource.forFileObject(file).runUserActionTask(cc -> {
                cc.toPhase(Phase.RESOLVED); //XXX
                Set<String> actual = detector.reparse(cc).stream().map(pd -> pd.getInputPattern()).collect(Collectors.toSet());
                Set<String> expected = new HashSet<>(Arrays.asList(expectedPatterns));
                
                assertEquals(expected, actual);
            }, true);

            return this;
        }

    }


}
