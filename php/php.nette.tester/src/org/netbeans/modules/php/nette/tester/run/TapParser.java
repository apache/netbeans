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
package org.netbeans.modules.php.nette.tester.run;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.openide.util.Pair;

public final class TapParser {

    private static enum State {
        NOT_OK,
    }

    private static final Pattern FILE_LINE_PATTERN_1 = Pattern.compile("(?:\\# )?in (?<FILE>[^(]+)\\((?<LINE>\\d+)\\).*"); // NOI18N
    // #255351
    private static final Pattern FILE_LINE_PATTERN_2 = Pattern.compile("(?<FILE>\\S+)\\s+\\:\\s+(?<LINE>\\d+)$"); // NOI18N
    private static final String OK_PREFIX = "ok "; // NOI18N
    private static final String NOT_OK_PREFIX = "not ok "; // NOI18N
    private static final String FAILED_PREFIX = "Failed: "; // NOI18N
    private static final String SKIP_MARK = " #skip"; // NOI18N
    private static final String HELLIP = "... "; // NOI18N
    private static final String XDEBUG_CALLSTACK = "Call Stack"; // NOI18N
    private static final String XDEBUG_HEADER = "#  Time  Memory  Function  Location"; // NOI18N
    private static final Pattern DIFF_LINE_PATTERN = Pattern.compile("diff \"([^\"]+)\" \"([^\"]+)\""); // NOI18N

    private final TestSuiteVo testSuite = new TestSuiteVo();
    private final Set<String> commentLines = new LinkedHashSet<>();

    private TestCaseVo testCase = null;
    private int testCaseCount = 0;
    private State state = null;


    public TapParser() {
    }

    public static boolean isTestCaseStart(String line) {
        return line.startsWith(OK_PREFIX)
                || line.startsWith(NOT_OK_PREFIX);
    }

    @CheckForNull
    public static Pair<String, Integer> getFileLine(String line) {
        Matcher matcher = FILE_LINE_PATTERN_1.matcher(line);
        boolean success = matcher.matches();
        if (!success) {
            matcher = FILE_LINE_PATTERN_2.matcher(line);
            success = matcher.find();
        }
        if (success) {
            return Pair.of(matcher.group("FILE"), Integer.valueOf(matcher.group("LINE"))); // NOI18N
        }
        return null;
    }

    public TestSuiteVo parse(String input, long runtime) {
        for (String line : input.split("\\r?\\n|\\r")) { // NOI18N
            if (!parseLine(line.trim())) {
                break;
            }
        }
        processComments();
        setTimes(runtime);
        return testSuite;
    }

    private boolean parseLine(String line) {
        if (line.startsWith("1..")) { // NOI18N
            return false;
        }
        if (line.startsWith("TAP version ")) { // NOI18N
            return true;
        }
        if (line.startsWith(OK_PREFIX)) {
            processComments();
            assert state == null : state;
            line = line.substring(OK_PREFIX.length());
            if (isSkippedTest(line)) {
                List<String> parts = StringUtils.explode(line, SKIP_MARK);
                addSuiteTest(parts.get(0));
                if (parts.size() > 1) {
                    testCase.setMessage(parts.get(1).trim());
                }
                testCase.setStatus(TestCase.Status.SKIPPED);
                testCase = null;
            } else {
                addSuiteTest(line);
                testCase.setStatus(TestCase.Status.PASSED);
                testCase = null;
            }
        } else if (line.startsWith(NOT_OK_PREFIX)) {
            processComments();
            assert state == null : state;
            state = State.NOT_OK;
            addSuiteTest(line.substring(NOT_OK_PREFIX.length()));
            testCase.setStatus(TestCase.Status.FAILED);
        } else {
            processComment(line);
        }
        return true;
    }

    private boolean isSkippedTest(String line) {
        assert state == null : state;
        return line.contains(SKIP_MARK);
    }

    private void processComment(String line) {
        assert line.startsWith("#") : line;
        // #255351 remove html
        String processedline = line.substring(1).replaceAll("<[^>]+>", " ").trim(); // NOI18N
        if (!StringUtils.hasText(processedline)) {
            return;
        }
        switch (state) {
            case NOT_OK:
                commentLines.add(processedline);
                break;
            default:
                assert false : "Unknown state: " + state;
        }
    }

    private void processComments() {
        if (commentLines.isEmpty()) {
            return;
        }
        assert testCase != null;
        List<String> lines = new ArrayList<>(commentLines);
        commentLines.clear();
        // line with file & line
        String lineWithFileLine = null;
        while (!lines.isEmpty()) {
            int lastIndex = lines.size() - 1;
            String line = lines.get(lastIndex);
            lines.remove(lastIndex);
            Pair<String, Integer> fileLine = getFileLine(line);
            if (fileLine == null) {
                continue;
            }
            lineWithFileLine = line;
            setFileLine(fileLine);
            break;
        }
        // content
        StringBuilder message = null;
        List<String> stackTrace = new ArrayList<>();
        boolean lineSet = false;
        while (!lines.isEmpty()) {
            String line = lines.get(0);
            lines.remove(0);
            if (XDEBUG_CALLSTACK.equals(line)
                    || XDEBUG_HEADER.equals(line)) {
                continue;
            }
            Pair<String, Integer> fileLine = getFileLine(line);
            if (fileLine != null) {
                stackTrace.add(line);
                stackTrace.addAll(processStackTrace(lines));
                lines.clear();
            } else if (line.startsWith("diff \"")) { // NOI18N
                processDiff(line);
            } else {
                if (message == null) {
                    message = new StringBuilder(200);
                }
                if (message.length() > 0) {
                    // unfortunately, \n not supported in the ui
                    if (line.startsWith(HELLIP)) {
                        line = line.substring(HELLIP.length());
                    }
                    message.append(" "); // NOI18N
                } else if (line.startsWith(FAILED_PREFIX)) {
                    line = line.substring(FAILED_PREFIX.length());
                }
                message.append(line);
            }
        }
        if (message != null) {
            testCase.setMessage(message.toString());
        }
        // append file with line number
        stackTrace.add(lineWithFileLine);
        testCase.setStackTrace(stackTrace);
        // reset
        state = null;
    }

    private List<String> processStackTrace(List<String> lines) {
        List<String> stackTrace = new ArrayList<>(lines.size());
        for (String line : lines) {
            stackTrace.add(line);
        }
        return stackTrace;
    }

    private void processDiff(String line) {
        Matcher matcher = DIFF_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            assert false : line;
            return;
        }
        testCase.setDiff(new TestCase.Diff(new DiffReader(matcher.group(1)), new DiffReader(matcher.group(2))));
    }

    private void addSuiteTest(String line) {
        String testName = line;
        testCase = new TestCaseVo(testName);
        testSuite.addTestCase(testCase);
        testCaseCount++;
    }

    private void setFileLine(Pair<String, Integer> fileLine) {
        assert fileLine != null;
        assert testCase != null;
        String file = fileLine.first();
        Integer row = fileLine.second();
        assert file != null : fileLine;
        assert row != null : fileLine;
        testCase.setFile(file);
        testCase.setLine(row);
    }

    private void setTimes(long runtime) {
        long time = 0;
        if (testCaseCount > 0) {
            time = runtime / testCaseCount;
        }
        for (TestCaseVo kase : testSuite.getTestCases()) {
            kase.setTime(time);
        }
    }

    //~ Inner classes

    private static final class DiffReader implements Callable<String> {

        private final String filePath;


        public DiffReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String call() throws IOException {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        }

    }

}
