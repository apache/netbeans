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
package org.netbeans.modules.php.editor.indent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import static junit.framework.TestCase.fail;
import org.netbeans.junit.NbTestCase;
import static org.netbeans.modules.csl.api.test.CslTestBase.readFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class PHPFormatterBlankLinesEOFTest extends PHPFormatterTestBase {

    public PHPFormatterBlankLinesEOFTest(String testName) {
        super(testName);
    }

    public void testEOF_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_EOF, false);
        reformatFileContents("testfiles/formatting/blankLines/eof_01.php", options, false, true);
    }

    public void testEOF_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_EOF, true);
        reformatFileContents("testfiles/formatting/blankLines/eof_01.php", options, false, true);
    }

    public void testEOF_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_EOF, false);
        reformatFileContents("testfiles/formatting/blankLines/eof_02.php", options, false, true);
    }

    public void testEOF_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.BLANK_LINES_EOF, true);
        reformatFileContents("testfiles/formatting/blankLines/eof_02.php", options, false, true);
    }

    @Override
    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, boolean includeJavaVersion, String ext, boolean checkFileExistence, boolean skipMarkers) throws Exception {
        File rubyFile = getDataFile(relFilePath);
        if (checkFileExistence && !rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        File goldenFile = null;
        if (includeJavaVersion) {
            String version = System.getProperty("java.specification.version");
            for (String variant : computeVersionVariantsFor(version)) {
                goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + variant + ext);
                if (goldenFile.exists()) {
                    break;
                }
            }
        }
        if (goldenFile == null || !goldenFile.exists()) {
            goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + ext);
        }
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            } finally {
                fw.close();
            }
            if (failOnMissingGoldenFile()) {
                NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
            }
            return;
        }

        String expected = readFile(goldenFile);

        // don't trim in this EOF test
        String expectedTrimmed = expected;
        String actualTrimmed = description;

        if (expectedTrimmed.equals(actualTrimmed)) {
            return; // Actual and expected content are equals --> Test passed
        } else {
            // We want to ignore different line separators (like \r\n against \n) because they
            // might be causing failing tests on a different operation systems like Windows :]
            String expectedUnified = expectedTrimmed.replace("\r", "");
            String actualUnified = actualTrimmed.replace("\r", "");

            // if there is '**' in the actualUnified, it may stand for whatever word of the expected
            // content in that position.
            if (skipMarkers) {
                String[] linesExpected = expectedUnified.split("\n");
                String[] linesActual = actualUnified.split("\n");
                boolean allMatch = linesExpected.length == linesActual.length;
                for (int i = 0; allMatch && i < linesExpected.length; i++) {
                    String e = linesExpected[i];
                    String a = linesActual[i];
                    Pattern pattern = markerPattern(a);
                    allMatch = pattern == null ? a.equals(e) : pattern.matcher(e).matches();
                }
                if (allMatch) {
                    return;
                }
            }

            if (expectedUnified.equals(actualUnified)) {
                return; // Only difference is in line separation --> Test passed
            }

            // There are some diffrerences between expected and actual content --> Test failed
            fail(getContentDifferences(relFilePath));
        }
    }

    private String getContentDifferences(String relFilePath) {
        StringBuilder sb = new StringBuilder();
        sb.append("Content does not match between '").append(relFilePath).append("' and '").append(relFilePath);
        return sb.toString();
    }

    private Pattern markerPattern(String line) {
        StringBuilder pattern = new StringBuilder();
        int start = 0;
        for (int idx = line.indexOf("*-*"); idx >= 0; start = idx + 3, idx = line.indexOf("*-*", start)) {
            pattern.append("\\s*");
            pattern.append(Pattern.quote(line.substring(start, idx).trim()));
            pattern.append("\\s*\\S*");
        }
        if (start > 0) {
            pattern.append("\\s*");
            pattern.append(Pattern.quote(line.substring(start).trim()));
            return Pattern.compile(pattern.toString());
        } else {
            return null;
        }
    }

    private static List<String> computeVersionVariantsFor(String version) {
        int dot = version.indexOf('.');
        int versionNum = Integer.parseInt(version.substring(dot + 1));
        List<String> versions = new ArrayList<>();
        for (int v = versionNum; v >= 9; v--) {
            versions.add("." + v);
        }
        return versions;
    }

    @Override
    protected String readFile(final FileObject fo) {
        return read(fo);
    }

    public static String read(final FileObject fo) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(FileUtil.toPath(fo)), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return content;
    }
}
