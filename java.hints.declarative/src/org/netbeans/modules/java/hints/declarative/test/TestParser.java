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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
        List<TestCase> result = new LinkedList<TestCase>();
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

        return result.toArray(new TestCase[result.size()]);
    }

    private static @CheckForNull TestCase handleTestCase(int testCaseIndex, String testName, String options, int codeIndex, String testCase) {
        Matcher m = LEADS_TO_HEADER.matcher(testCase);
        String code = null;
        List<String> results = new LinkedList<String>();
        List<Integer> startIndices = new LinkedList<Integer>();
        List<Integer> endIndices = new LinkedList<Integer>();
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
