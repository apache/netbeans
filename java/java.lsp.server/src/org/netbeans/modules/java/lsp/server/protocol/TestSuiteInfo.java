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
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.util.Preconditions;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
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
    private String name;

    /**
     * The optional module name to be displayed by the Test Explorer.
     */
    private String moduleName;
    
    /**
     * The optional module path used by the Test Explorer.
     */
    private String modulePath;

    /**
     * The file containing this suite (if known).
     */
    private String file;

    /**
     * The range within the specified file where the suite definition is located (if known).
     */
    private Range range;

    /**
     * The state of the tests suite. Can be one of the following values:
     * "loaded" | "started" | "passed" | "failed" | "skipped" | "errored"
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

    public TestSuiteInfo(@NonNull final String name, @NonNull final String state) {
        this.name = Preconditions.checkNotNull(name, "name");
        this.state = Preconditions.checkNotNull(state, "state");
    }

    public TestSuiteInfo(@NonNull final String name, final String moduleName, final String modulePath, final String file, final Range range, @NonNull final String state, final List<TestCaseInfo> tests) {
        this(name, state);
        this.moduleName = moduleName;
        this.modulePath = modulePath;
        this.file = file;
        this.range = range;
        this.tests = tests;
    }

    /**
     * The test suite name to be displayed by the Test Explorer.
     */
    @Pure
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * The test suite name to be displayed by the Test Explorer.
     */
    public TestSuiteInfo setSuiteName(@NonNull final String name) {
        this.name = Preconditions.checkNotNull(name, "name");
        return this;
    }

    /**
     * The optional module name to be displayed by the Test Explorer.
     */
    @Pure
    public String getModuleName() {
        return moduleName;
    }

    /**
     * The optional module name to be displayed by the Test Explorer.
     */
    public TestSuiteInfo setModuleName(final String moduleName) {
        this.moduleName = moduleName;
        return this;
    }
    
    /**
     * The optional module path used by the Test Explorer.
     */
    @Pure
    public String getModulePath() {
        return modulePath;
    }

    /**
     * The optional module path used by the Test Explorer.
     */
    public TestSuiteInfo setModulePath(final String modulePath) {
        this.modulePath = modulePath;
        return this;
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
    public TestSuiteInfo setFile(final String file) {
        this.file = file;
        return this;
    }

    /**
     * The range within the specified file where the suite definition is located (if known).
     */
    @Pure
    public Range getRange() {
        return range;
    }

    /**
     * The range within the specified file where the suite definition is located (if known).
     */
    public TestSuiteInfo setRange(final Range range) {
        this.range = range;
        return this;
    }

    /**
     * The state of the tests suite. Can be one of the following values:
     * "loaded" | "started" | "passed" | "failed" | "skipped" | "errored"
     */
    @Pure
    @NonNull
    public String getState() {
        return state;
    }

    /**
     * The state of the tests suite. Can be one of the following values:
     * "loaded" | "started" | "passed" | "failed" | "skipped" | "errored"
     */
    public TestSuiteInfo setState(@NonNull final String state) {
        this.state = Preconditions.checkNotNull(state, "state");
        return this;
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
    public TestSuiteInfo setTests(List<TestCaseInfo> tests) {
        this.tests = tests;
        return this;
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("name", name);
        b.add("moduleName", moduleName);
        b.add("modulePath", modulePath);
        b.add("file", file);
        b.add("range", range);
        b.add("state", state);
        b.add("tests", tests);
        return b.toString();
    }

    @Override
    @Pure
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.moduleName);
        hash = 67 * hash + Objects.hashCode(this.modulePath);
        hash = 67 * hash + Objects.hashCode(this.file);
        hash = 67 * hash + Objects.hashCode(this.range);
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
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.moduleName, other.moduleName)) {
            return false;
        }
        if (!Objects.equals(this.modulePath, other.modulePath)) {
            return false;
        }
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        if (!Objects.equals(this.range, other.range)) {
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
    public static final class TestCaseInfo {

        /**
         * The test case ID.
         */
        @NonNull
        private String id;

        /**
         * The name to be displayed by the Test Explorer for this test case.
         */
        @NonNull
        private String name;

        /**
         * The file containing this test case (if known).
         */
        private String file;

        /**
         * The range within the specified file where the test case definition is located (if known).
         */
        private Range range;

        /**
         * The state of the test case. Can be one of the following values:
         * "loaded" | "started" | "passed" | "failed" | "skipped" | "errored"
         */
        @NonNull
        private String state;

        /**
         * Stack trace for a test failure.
         */
        private List<String> stackTrace;

        public TestCaseInfo() {
            this("", "", "");
        }

        public TestCaseInfo(@NonNull final String id, @NonNull final String name, @NonNull final String state) {
            this.id = Preconditions.checkNotNull(id, "id");
            this.name = Preconditions.checkNotNull(name, "name");
            this.state = Preconditions.checkNotNull(state, "state");
        }

        public TestCaseInfo(@NonNull final String id, @NonNull final String name, final String file, final Range range, @NonNull final String state, final List<String> stackTrace) {
            this(id, name, state);
            this.file = file;
            this.range = range;
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
        public TestCaseInfo setId(@NonNull final String id) {
            this.id = Preconditions.checkNotNull(id, "id");
            return this;
        }

        /**
         * The name to be displayed by the Test Explorer for this test case.
         */
        @Pure
        @NonNull
        public String getName() {
            return name;
        }

        /**
         * The name to be displayed by the Test Explorer for this test case.
         */
        public TestCaseInfo setName(@NonNull final String name) {
            this.name = Preconditions.checkNotNull(name, "name");
            return this;
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
        public TestCaseInfo setFile(final String file) {
            this.file = file;
            return this;
        }

        /**
         * The range within the specified file where the test case definition is located (if known).
         */
        @Pure
        public Range getRange() {
            return range;
        }

        /**
         * The range within the specified file where the test case definition is located (if known).
         */
        public TestCaseInfo setRange(final Range range) {
            this.range = range;
            return this;
        }

        /**
         * The state of the test case. Can be one of the following values:
         * "loaded" | "started" | "passed" | "failed" | "skipped" | "errored"
         */
        @Pure
        @NonNull
        public String getState() {
            return state;
        }

        /**
         * The state of the test case. Can be one of the following values:
         * "loaded" | "started" | "passed" | "failed" | "skipped" | "errored"
         */
        public TestCaseInfo setState(@NonNull final String state) {
            this.state = Preconditions.checkNotNull(state, "state");
            return this;
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
        public TestCaseInfo setStackTrace(final List<String> stackTrace) {
            this.stackTrace = stackTrace;
            return this;
        }

        @Override
        @Pure
        public String toString() {
            ToStringBuilder b = new ToStringBuilder(this);
            b.add("id", id);
            b.add("name", name);
            b.add("file", file);
            b.add("range", range);
            b.add("state", state);
            b.add("stackTrace", stackTrace);
            return b.toString();
        }

        @Override
        @Pure
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.id);
            hash = 97 * hash + Objects.hashCode(this.name);
            hash = 97 * hash + Objects.hashCode(this.file);
            hash = 97 * hash + Objects.hashCode(this.range);
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
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.file, other.file)) {
                return false;
            }
            if (!Objects.equals(this.range, other.range)) {
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

        public static final String Started = "started";

        public static final String Passed  = "passed";

        public static final String Failed  = "failed";

        public static final String Skipped  = "skipped";

        public static final String Errored  = "errored";
    }
}
