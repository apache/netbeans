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

package org.netbeans.modules.web.clientproject.api.jstesting;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Parameters;

/**
 * Class holding information about a test run.
 * <p>
 * This class is thread-safe.
 * @since 1.49
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
    private final String testFile;


    private TestRunInfo(Builder builder) {
        assert builder != null;
        assert builder.sessionType != null;
        sessionType = builder.sessionType;
        testFile = builder.testFile;
    }

    /**
     * Get session type.
     * @return session type
     */
    public SessionType getSessionType() {
        return sessionType;
    }

    /**
     * Get test file. Can be {@code null} (in such case, all the test file should be run).
     * @return test file, can be {@code null}
     */
    @CheckForNull
    public String getTestFile() {
        return testFile;
    }

    @Override
    public String toString() {
        return "TestRunInfo{" + "sessionType=" + sessionType + ", testFile=" + testFile + '}'; // NOI18N
    }

    //~ Inner classes

    /**
     * Builder for {@link TestRunInfo}.
     * <p>
     * The default {@link Builder#setSessionType(org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo.SessionType) session type}
     * is {@link SessionType#TEST}.
     * @since 1.49
     */
    public static final class Builder {

        SessionType sessionType = SessionType.TEST;
        String testFile;


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
         * Set test file.
         * @param testFile test file to be tested, can be {@code null} (in such case,
         *        all the test file should be run)
         * @return this instance
         */
        public Builder setTestFile(@NullAllowed String testFile) {
            this.testFile = testFile;
            return this;
        }

        /**
         * Create {@link TestRunInfo}.
         * @return {@link TestRunInfo} instance
         */
        public TestRunInfo build() {
            Parameters.notNull("sessionType", sessionType); // NOI18N
            return new TestRunInfo(this);
        }

    }

}
