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
package org.netbeans.modules.php.spi.testing.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Class holding information about a test run.
 * <p>
 * This class is thread-safe.
 */
public final class TestRunInfo {

    /**
     * Session type.
     */
    public static enum SessionType {
        /**
         * Normal run.
         */
        TEST,
        /**
         * Run under debugger.
         */
        DEBUG,
    }

    private final SessionType sessionType;
    private final List<FileObject> startFiles;
    private final String suiteName;
    private final boolean coverageEnabled;
    private final List<TestInfo> initialTests = new CopyOnWriteArrayList<>();
    private final List<TestInfo> customTests = new CopyOnWriteArrayList<>();
    private final Map<String, Object> parameters = new ConcurrentHashMap<>();

    private volatile boolean rerun = false;


    /**
     * Create new info about test run.
     * @param sessionType run or debug
     * @param workingDirectory working directory
     * @param startFile start file (can be directory)
     * @param suiteName test name, can be {@code null}
     * @param coverageEnabled {@code true} if the coverage is enabled and should be collected
     */
    private TestRunInfo(Builder builder) {
        assert builder != null;
        assert builder.sessionType != null;
        assert builder.startFiles != null;

        this.sessionType = builder.sessionType;
        this.startFiles = builder.startFiles;
        this.suiteName = builder.suiteName;
        this.coverageEnabled = builder.coverageEnabled;
    }

    /**
     * Get session type.
     * @return session type
     */
    public SessionType getSessionType() {
        return sessionType;
    }

    /**
     * Get files (or directories] of test run.
     * @return files (or directories) of test run
     * @since 0.11
     */
    public List<FileObject> getStartFiles() {
        return Collections.unmodifiableList(startFiles);
    }

    /**
     * Get name of the test to be run, can be {@code null}.
     * @return name of the test to be run, can be {@code null}
     * @since 0.3
     */
    @CheckForNull
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * Return {@code true} if all tests are to be run.
     * @return {@code true} if all tests are to be run
     */
    public boolean allTests() {
        return suiteName == null;
    }

    /**
     * Return {@code true} if code coverage should be collected (if supported).
     * @return {@code true} if code coverage should be collected (if supported)
     */
    public boolean isCoverageEnabled() {
        return coverageEnabled;
    }

    /**
     * Set initial tests to be run. These tests are returned if no {@link #getCustomTests() custom tests}
     * exist.
     * @param tests initial tests to be run or empty list, never {@code null}
     * @since 0.12
     */
    public void setInitialTests(Collection<TestInfo> tests) {
        Parameters.notNull("tests", tests); // NOI18N
        initialTests.clear();
        initialTests.addAll(tests);
    }

    /**
     * Get custom tests to be run or empty list, never {@code null}. If there are no custom tests.
     * {@link #setInitialTests(Collection) initial tests} are returned.
     * @return custom tests to be run or empty list, never {@code null}
     */
    public List<TestInfo> getCustomTests() {
        List<TestInfo> tests = customTests;
        if (tests.isEmpty()) {
            tests = initialTests;
        }
        return new ArrayList<>(tests);
    }

    /**
     * Reset custom tests. Usually, there is no need to call this method.
     * @see #getCustomTests()
     * @see #setCustomTests(java.util.Collection)
     */
    public void resetCustomTests() {
        customTests.clear();
    }

    /**
     * Set custom tests to be run.
     * @param tests custom tests to be run or empty list, never {@code null}
     */
    public void setCustomTests(Collection<TestInfo> tests) {
        Parameters.notNull("tests", tests); // NOI18N
        resetCustomTests();
        customTests.addAll(tests);
    }

    /**
     * Check whether this test run is rerun.
     * @return {@code true} if this test run is rerun
     */
    public boolean isRerun() {
        return rerun;
    }

    /**
     * Set whether this test run is rerun.
     * @param rerun {@code true} for rerun, {@code false} otherwise
     */
    public void setRerun(boolean rerun) {
        this.rerun = rerun;
    }

    /**
     * Get custom parameter previously stored using {@link #setParameter(String, Object) store} method.
     * @param <T> type of the parameter
     * @param key key of the parameter
     * @param type type of the parameter
     * @return parameter value or {@code null} if not found
     */
    public <T> T getParameter(String key, Class<T> type) {
        Parameters.notEmpty("key", key); // NOI18N
        Parameters.notNull("type", type); // NOI18N
        Object param = parameters.get(key);
        if (param == null) {
            return null;
        }
        return type.cast(param);
    }

    /**
     * Set custom parameter.
     * @param key key of the parameter
     * @param value value of the parameter
     */
    public void setParameter(String key, Object value) {
        Parameters.notEmpty("key", key); // NOI18N
        Parameters.notNull("value", value); // NOI18N
        parameters.put(key, value);
    }

    /**
     * Remove custom parameter.
     * @param key key of the parameter
     */
    public void removeParameter(String key) {
        Parameters.notEmpty("key", key); // NOI18N
        parameters.remove(key);
    }

    //~ Inner classes

    /**
     * Builder for {@link TestRunInfo}.
     * @since 0.3
     */
    public static final class Builder {

        SessionType sessionType;
        List<FileObject> startFiles;
        String suiteName;
        boolean coverageEnabled;


        /**
         * Set session type.
         * @param sessionType session type
         * @return this instance
         */
        public Builder setSessionType(@NonNull SessionType sessionType) {
            this.sessionType = sessionType;
            return this;
        }

        /**
         * Set start file (can be directory).
         * @param startFile start file (can be directory)
         * @return this instance
         * @see #setStartFiles(java.util.List)
         */
        public Builder setStartFile(@NonNull FileObject startFile) {
            setStartFiles(Collections.singletonList(startFile));
            return this;
        }

        /**
         * Set start files (can be directories).
         * @param startFiles start files (can be directories), cannot be empty list
         * @return this instance
         * @see #setStartFile(org.openide.filesystems.FileObject)
         * @since 0.11
         */
        public Builder setStartFiles(@NonNull List<FileObject> startFiles) {
            this.startFiles = startFiles;
            return this;
        }

        /**
         * Set test suite name, can be {@code null}.
         * @param suiteName test suite name, can be {@code null}
         * @return this instance
         */
        public Builder setSuiteName(@NullAllowed String suiteName) {
            this.suiteName = suiteName;
            return this;
        }

        /**
         * Set {@code true} if the coverage is enabled and should be collected.
         * @param coverageEnabled {@code true} if the coverage is enabled and should be collected
         * @return this instance
         */
        public Builder setCoverageEnabled(boolean coverageEnabled) {
            this.coverageEnabled = coverageEnabled;
            return this;
        }

        /**
         * Create {@link TestRunInfo}.
         * @return {@link TestRunInfo} instance
         */
        public TestRunInfo build() {
            Parameters.notNull("sessionType", sessionType); // NOI18N
            Parameters.notNull("startFiles", startFiles); // NOI18N
            if (startFiles.isEmpty()) {
                throw new IllegalArgumentException("Start files cannot be empty");
            }
            return new TestRunInfo(this);
        }

    }

    /**
     * Class representing information about a test.
     */
    public static final class TestInfo {

        /**
         * Unknown test type.
         * @since 0.3
         */
        public static final String UNKNOWN_TYPE = "UNKNOWN_TYPE"; // NOI18N

        private final String type;
        private final String name;
        private final String className;
        private final String location;


        /**
         * Create new information about a test.
         * @param type type of the test, typically an identifier of the testing provider, can be {@see #UNKNOWN_TYPE unknown}
         * @param name name of the test
         * @param className class name, can be {@code null}
         * @param location location, can be {@code null}
         */
        public TestInfo(String type, String name, @NullAllowed String className, @NullAllowed String location) {
            Parameters.notEmpty("type", name);
            Parameters.notEmpty("name", name);

            this.type = type;
            this.name = name;
            this.className = className;
            this.location = location;
        }

        /**
         * Get the type of the test, typically an identifier of the testing provider (can be {@see #UNKNOWN_TYPE unknown}).
         * @return the type of the test, typically an identifier of the testing provider (can be {@see #UNKNOWN_TYPE unknown})
         */
        public String getType() {
            return type;
        }

        /**
         * Get the name of the test.
         * @return name of the test
         */
        public String getName() {
            return name;
        }

        /**
         * Get the class name, can be {@code null}.
         * @return class name, can be {@code null}
         */
        @CheckForNull
        public String getClassName() {
            return className;
        }

        /**
         * Get the location, can be {@code null}.
         * @return location, can be {@code null}
         */
        @CheckForNull
        public String getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return "TestInfo{" + "type=" + type + ", name=" + name + ", className=" + className + ", location=" + location + '}'; // NOI18N
        }

    }

}
