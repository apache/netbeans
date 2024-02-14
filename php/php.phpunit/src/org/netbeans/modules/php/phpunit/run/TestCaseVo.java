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
package org.netbeans.modules.php.phpunit.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class TestCaseVo {

    private static final String PHPUNIT_TYPE = "PhpUnit"; // NOI18N
    private static final String EXPECTED_SECTION_START = "--- Expected"; // NOI18N
    private static final String EXPECTED_ROW_START = "-"; // NOI18N
    private static final String ACTUAL_SECTION_START = "+++ Actual"; // NOI18N
    private static final String ACTUAL_ROW_START = "+"; // NOI18N
    private static final String DIFF_SECTION_START = "@@ @@"; // NOI18N

    private final List<String> stacktrace = new ArrayList<>();
    private final String className;
    private final String name;
    private final String file;
    private final int line;
    private final long time;

    private TestCase.Status status = TestCase.Status.PASSED;


    public TestCaseVo(String className, String name, String file, int line, long time) {
        assert name != null;
        this.className = className;
        this.name = name;
        this.file = file;
        this.line = line;
        this.time = time;
    }

    @NbBundle.Messages("TestCaseVo.tests.no=No valid test cases found.")
    static TestCaseVo skippedTestCase() {
        // suite with no testcases => create a fake with error message
        TestCaseVo testCase = new TestCaseVo(null, Bundle.TestCaseVo_tests_no(), null, -1, -1);
        testCase.status = TestCase.Status.SKIPPED;
        return testCase;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return PHPUNIT_TYPE;
    }

    @CheckForNull
    public String getClassName() {
        return className;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public Locations.Line getLocation() {
        if (file == null) {
            return null;
        }
        File f = new File(file);
        if (!f.isFile()) {
            return null;
        }
        return new Locations.Line(FileUtil.toFileObject(f), line);
    }

    public long getTime() {
        return time;
    }

    public String[] getStackTrace() {
        return stacktrace.toArray(new String[0]);
    }

    public TestCase.Diff getDiff() {
        StringBuilder expected = new StringBuilder(100);
        StringBuilder actual = new StringBuilder(100);
        boolean diffStarted = false;
        for (String row : stacktrace) {
            if (row.contains(EXPECTED_SECTION_START) && row.contains(ACTUAL_SECTION_START)) {
                for (String part : row.split("\r?\n")) { // NOI18N
                    if (diffStarted) {
                        if (part.startsWith(EXPECTED_ROW_START)) {
                            addSpace(expected);
                            expected.append(part.substring(EXPECTED_ROW_START.length()));
                        } else if (part.startsWith(ACTUAL_ROW_START)) {
                            addSpace(actual);
                            actual.append(part.substring(ACTUAL_ROW_START.length()));
                        } else {
                            String p = part.substring(1); // remove the first space
                            // remove the first space
                            addSpace(expected);
                            expected.append(p);
                            addSpace(actual);
                            actual.append(p);
                        }
                    } else if (part.equals(DIFF_SECTION_START)) {
                        diffStarted = true;
                    }
                }
                break;
            }
        }
        return new TestCase.Diff(expected.toString(), actual.toString());
    }

    private void addSpace(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append("\n"); // NOI18N
        }
    }

    void addStacktrace(String line) {
        stacktrace.add(line);
    }

    void setErrorStatus() {
        assert status == TestCase.Status.PASSED;
        status = TestCase.Status.ERROR;
    }

    public void setFailureStatus() {
        assert status == TestCase.Status.PASSED;
        status = TestCase.Status.FAILED;
    }

    public TestCase.Status getStatus() {
        return status;
    }

    public boolean isError() {
        return status.equals(TestCase.Status.ERROR);
    }

    public boolean isFailure() {
        return status.equals(TestCase.Status.FAILED);
    }

    @Override
    public String toString() {
        return String.format("TestCaseVo{name: %s, file: %s, line: %d, time: %d, status: %s, stacktrace: %s}", name, file, line, time, status, stacktrace); // NOI18N
    }

}
