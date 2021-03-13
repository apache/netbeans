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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.List;
import java.util.Objects;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Information about a test suite.
 *
 * @author Dusan Balek
 */
public final class TestSuiteInfo {

    /**
     * The test suite name to be displayed by the Test Explorer.
     */
    @NonNull
    private String suiteName;

    /**
     * The file containing this suite (if known).
     */
    private String file;

    /**
     * The line within the specified file where the suite definition starts (if known).
     */
    private Integer line;

    /**
     * The state of the tests suite. Can be one of the following values:
     * "loaded" | "running" | "completed" | "errored"
     */
    @NonNull
    private String state;

    /**
     * The test cases of the test suite.
     */
    private List<TestCaseInfo> tests;

    public TestSuiteInfo() {
        this("", "");
    }

    public TestSuiteInfo(@NonNull final String suiteName, @NonNull final String state) {
        this.suiteName = Preconditions.checkNotNull(suiteName, "suiteName");
        this.state = Preconditions.checkNotNull(state, "state");
    }

    public TestSuiteInfo(@NonNull final String suiteName, final String file, final Integer line, @NonNull final String state, final List<TestCaseInfo> tests) {
        this(suiteName, state);
        this.file = file;
        this.line = line;
        this.tests = tests;
    }

    /**
     * The test suite name to be displayed by the Test Explorer.
     */
    @Pure
    @NonNull
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * The test suite name to be displayed by the Test Explorer.
     */
    public void setSuiteName(@NonNull final String suiteName) {
        this.suiteName = Preconditions.checkNotNull(suiteName, "suiteName");
    }

    /**
     * The file containing this suite (if known).
     */
    @Pure
    public String getFile() {
        return file;
    }

    /**
     * The file containing this suite (if known).
     */
    public void setFile(final String file) {
        this.file = file;
    }

    /**
     * The line within the specified file where the suite definition starts (if known).
     */
    @Pure
    public Integer getLine() {
        return line;
    }

    /**
     * The line within the specified file where the suite definition starts (if known).
     */
    public void setLine(final Integer line) {
        this.line = line;
    }

    /**
     * The state of the tests suite. Can be one of the following values:
     * "loaded" | "running" | "completed" | "errored"
     */
    @Pure
    @NonNull
    public String getState() {
        return state;
    }

    /**
     * The state of the tests suite. Can be one of the following values:
     * "loaded" | "running" | "completed" | "errored"
     */
    public void setState(@NonNull final String state) {
        this.state = Preconditions.checkNotNull(state, "state");
    }

    /**
     * The test cases of the test suite.
     */
    @Pure
    public List<TestCaseInfo> getTests() {
        return tests;
    }

    /**
     * The test cases of the test suite.
     */
    public void setTests(List<TestCaseInfo> tests) {
        this.tests = tests;
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("suiteName", suiteName);
        b.add("file", file);
        b.add("line", line);
        b.add("state", state);
        b.add("tests", tests);
        return b.toString();
    }

    @Override
    @Pure
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.suiteName);
        hash = 67 * hash + Objects.hashCode(this.file);
        hash = 67 * hash + Objects.hashCode(this.line);
        hash = 67 * hash + Objects.hashCode(this.state);
        hash = 67 * hash + Objects.hashCode(this.tests);
        return hash;
    }

    @Override
    @Pure
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestSuiteInfo other = (TestSuiteInfo) obj;
        if (!Objects.equals(this.suiteName, other.suiteName)) {
            return false;
        }
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        if (!Objects.equals(this.line, other.line)) {
            return false;
        }
        if (!Objects.equals(this.state, other.state)) {
            return false;
        }
        if (!Objects.equals(this.tests, other.tests)) {
            return false;
        }
        return true;
    }

    /**
     * Information about a test case.
     */
    public static class TestCaseInfo {

        /**
         * The test case ID.
         */
        @NonNull
        private String id;

        /**
         * The short name to be displayed by the Test Explorer for this test case.
         */
        @NonNull
        private String shortName;

        /**
         * The full name to be displayed by the Test Explorer when you hover over
         * this test case.
         */
        @NonNull
        private String fullName;

        /**
         * The file containing this test case (if known).
         */
        private String file;

        /**
         * The line within the specified file where the test case definition starts (if known).
         */
        private Integer line;

        /**
         * The state of the test case. Can be one of the following values:
         * "loaded" | "running" | "passed" | "failed" | "skipped" | "errored"
         */
        @NonNull
        private String state;

        /**
         * Stack trace for a test failure.
         */
        private List<String> stackTrace;

        public TestCaseInfo() {
            this("", "", "", "");
        }

        public TestCaseInfo(@NonNull final String id, @NonNull final String shortName, @NonNull final String fullName, @NonNull final String state) {
            this.id = Preconditions.checkNotNull(id, "id");
            this.shortName = Preconditions.checkNotNull(shortName, "shortName");
            this.fullName = Preconditions.checkNotNull(fullName, "fullName");
            this.state = Preconditions.checkNotNull(state, "state");
        }

        public TestCaseInfo(@NonNull final String id, @NonNull final String shortName, @NonNull final String fullName, final String file, final Integer line, @NonNull final String state, final List<String> stackTrace) {
            this(id, shortName, fullName, state);
            this.file = file;
            this.line = line;
            this.stackTrace = stackTrace;
        }

        /**
         * The test case ID.
         */
        @Pure
        @NonNull
        public String getId() {
            return id;
        }

        /**
         * The test case ID.
         */
        public void setId(@NonNull final String id) {
            this.id = Preconditions.checkNotNull(id, "id");
        }

        /**
         * The short name to be displayed by the Test Explorer for this test case.
         */
        @Pure
        @NonNull
        public String getShortName() {
            return shortName;
        }

        /**
         * The short name to be displayed by the Test Explorer for this test case.
         */
        public void setShortName(@NonNull final String shortName) {
            this.shortName = Preconditions.checkNotNull(shortName, "shortName");
        }

        /**
         * The full name to be displayed by the Test Explorer when you hover over
         * this test case.
         */
        @Pure
        @NonNull
        public String getFullName() {
            return fullName;
        }

        /**
         * The full name to be displayed by the Test Explorer when you hover over
         * this test case.
         */
        public void setFullName(@NonNull final String fullName) {
            this.fullName = Preconditions.checkNotNull(fullName, "fullName");
        }

        /**
         * The file containing this test case (if known).
         */
        @Pure
        public String getFile() {
            return file;
        }

        /**
         * The file containing this test case (if known).
         */
        public void setFile(final String file) {
            this.file = file;
        }

        /**
         * The line within the specified file where the test case definition starts (if known).
         */
        @Pure
        public Integer getLine() {
            return line;
        }

        /**
         * The line within the specified file where the test case definition starts (if known).
         */
        public void setLine(final Integer line) {
            this.line = line;
        }

        /**
         * The state of the test case. Can be one of the following values:
         * "loaded" | "running" | "passed" | "failed" | "skipped" | "errored"
         */
        @Pure
        @NonNull
        public String getState() {
            return state;
        }

        /**
         * The state of the test case. Can be one of the following values:
         * "loaded" | "running" | "passed" | "failed" | "skipped" | "errored"
         */
        public void setState(@NonNull final String state) {
            this.state = Preconditions.checkNotNull(state, "state");
        }

        /**
         * Stack trace for a test failure.
         */
        @Pure
        public List<String> getStackTrace() {
            return stackTrace;
        }

        /**
         * Stack trace for a test failure.
         */
        public void setStackTrace(final List<String> stackTrace) {
            this.stackTrace = stackTrace;
        }

        @Override
        @Pure
        public String toString() {
            ToStringBuilder b = new ToStringBuilder(this);
            b.add("id", id);
            b.add("shortName", shortName);
            b.add("fullName", fullName);
            b.add("file", file);
            b.add("line", line);
            b.add("state", state);
            b.add("stackTrace", stackTrace);
            return b.toString();
        }

        @Override
        @Pure
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.id);
            hash = 97 * hash + Objects.hashCode(this.shortName);
            hash = 97 * hash + Objects.hashCode(this.fullName);
            hash = 97 * hash + Objects.hashCode(this.file);
            hash = 97 * hash + Objects.hashCode(this.line);
            hash = 97 * hash + Objects.hashCode(this.state);
            hash = 97 * hash + Objects.hashCode(this.stackTrace);
            return hash;
        }

        @Override
        @Pure
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TestCaseInfo other = (TestCaseInfo) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            if (!Objects.equals(this.shortName, other.shortName)) {
                return false;
            }
            if (!Objects.equals(this.fullName, other.fullName)) {
                return false;
            }
            if (!Objects.equals(this.file, other.file)) {
                return false;
            }
            if (!Objects.equals(this.line, other.line)) {
                return false;
            }
            if (!Objects.equals(this.state, other.state)) {
                return false;
            }
            if (!Objects.equals(this.stackTrace, other.stackTrace)) {
                return false;
            }
            return true;
        }
    }

    /**
     * Constants for test states.
     */
    public static final class State {

        private State() {}

        public static final String Loaded = "loaded";

        public static final String Running = "running";

        public static final String Completed  = "completed";

        public static final String Passed  = "passed";

        public static final String Failed  = "failed";

        public static final String Skipped  = "skipped";

        public static final String Errored  = "errored";
    }
}
