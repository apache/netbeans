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

package org.netbeans.modules.java.hints.declarative.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author lahvac
 */
public class TestParser extends Parser {

    private TestResult result;
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        result = new TestResult(snapshot, parse(snapshot.getText().toString()));
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {}

    @Override
    public void removeChangeListener(ChangeListener changeListener) {}

    public static final class TestResult extends Result {

        private final TestCase[] tests;

        public TestResult(Snapshot snapshot, TestCase[] tests) {
            super(snapshot);
            this.tests = tests;
        }

        public TestCase[] getTests() {
            return tests;
        }

        @Override
        protected void invalidate() {}
        
    }

    @MimeRegistration(mimeType=TestTokenId.MIME_TYPE, service=ParserFactory.class)
    public static final class FactoryImpl extends ParserFactory {

        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            System.err.println("create");
            return new TestParser();
        }
        
    }

    public static TestCase[] parse(String tests) {
        //TODO: efficiency?
        List<TestCase> result = new LinkedList<>();
        int codeIndex = -1;
        int testCaseIndex = -1;
        String lastName = null;
        String lastOptions = null;
        Matcher m = TEST_CASE_HEADER.matcher(tests);

        while (m.find()) {
            if (testCaseIndex >= 0) {
                TestCase tc = handleTestCase(testCaseIndex, lastName, lastOptions, codeIndex, tests.substring(codeIndex, m.start()));

                if (tc != null) {
                    result.add(tc);
                }
            }

            codeIndex = m.end();
            testCaseIndex = m.start();
            lastName = m.group(1);
            lastOptions = m.group(2);
        }

        if (testCaseIndex >= 0) {
            TestCase tc = handleTestCase(testCaseIndex, lastName, lastOptions, codeIndex, tests.substring(codeIndex));

            if (tc != null) {
                result.add(tc);
            }
        }

        return result.toArray(new TestCase[0]);
    }

    private static @CheckForNull TestCase handleTestCase(int testCaseIndex, String testName, String options, int codeIndex, String testCase) {
        Matcher m = LEADS_TO_HEADER.matcher(testCase);
        String code = null;
        List<String> results = new LinkedList<>();
        List<Integer> startIndices = new LinkedList<>();
        List<Integer> endIndices = new LinkedList<>();
        int lastStartIndex = -1;
        int lastIndex = -1;

        while (m.find()) {
            if (code == null) {
                code = testCase.substring(0, m.start());
            } else {
                results.add(testCase.substring(lastIndex, m.start()));
                if (!startIndices.isEmpty()) {
                    endIndices.add(startIndices.get(startIndices.size() - 1));
                }
                startIndices.add(lastIndex);
            }

            lastStartIndex = m.start();
            lastIndex = m.end();
        }

        if (code == null) {
            code = testCase;//.substring(0, m.start());
        } else {
            results.add(testCase.substring(lastIndex));
            if (!startIndices.isEmpty()) {
                endIndices.add(startIndices.get(startIndices.size() - 1));
            }
            startIndices.add(lastIndex);
            endIndices.add(testCase.length());
        }

        int[] startIndicesArr = new int[startIndices.size()];
        int[] endIndicesArr = new int[endIndices.size()];

        assert startIndicesArr.length == endIndicesArr.length;

        int c = 0;

        for (Integer i : startIndices) {
            startIndicesArr[c++] = codeIndex + i;
        }

        c = 0;
        
        for (Integer i : endIndices) {
            endIndicesArr[c++] = codeIndex + i;
        }
        
        SourceVersion sourceLevel = DEFAULT_SOURCE_LEVEL;
        
        for (String option : options.split("[ \t]+")) {
            option = option.trim();
            
            if (option.startsWith(SOURCE_LEVEL)) {
                option = option.substring(SOURCE_LEVEL.length());
                
                //XXX: log if not found!
                
                for (SourceVersion v : SourceVersion.values()) {
                    if (option.equals("1." + v.name().substring("RELEASE_".length()))) {
                        sourceLevel = v;
                        break;
                    }
                }
            }

            if (DISABLED.equals(option)) {
                return null;
            }
        }
        
        return new TestCase(testName, sourceLevel, code, results.toArray(new String[0]), testCaseIndex, codeIndex, startIndicesArr, endIndicesArr);
    }

    private static final Pattern TEST_CASE_HEADER = Pattern.compile("%%TestCase[ \t]+([^ \t\n]*)(([ \t]+[^ \t\n]*)*)\n");
    private static final Pattern LEADS_TO_HEADER = Pattern.compile("%%=>\n");
    private static final String SOURCE_LEVEL = "source-level=";
    private static final String DISABLED = "DISABLED";
    private static final SourceVersion DEFAULT_SOURCE_LEVEL = SourceVersion.RELEASE_5;

    public static final class TestCase {
        private final String name;
        private final SourceVersion sourceLevel;
        private final String code;
        private final String[] results;

        private final int testCaseStart;
        private final int codeStart;
        private final int[] resultsStart;
        private final int[] resultsEnd;

        private TestCase(String name, SourceVersion sourceLevel, String code, String[] results, int testCaseStart, int codeStart, int[] resultsStart, int[] resultsEnd) {
            this.name = name;
            this.sourceLevel = sourceLevel;
            this.code = code;
            this.results = results;
            this.testCaseStart = testCaseStart;
            this.codeStart = codeStart;
            this.resultsStart = resultsStart;
            this.resultsEnd = resultsEnd;
        }

        public String getCode() {
            return code;
        }

        public int getCodeStart() {
            return codeStart;
        }

        public String getName() {
            return name;
        }

        public String[] getResults() {
            return results;
        }

        public int[] getResultsStart() {
            return resultsStart;
        }

        public int[] getResultsEnd() {
            return resultsEnd;
        }

        public int getTestCaseStart() {
            return testCaseStart;
        }

        public SourceVersion getSourceLevel() {
            return sourceLevel;
        }

        @Override
        public String toString() {
            return name + ":" + code + ":" + Arrays.toString(results) + ":" + testCaseStart + ":" + codeStart + ":" + Arrays.toString(resultsStart);
        }

    }
}
