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
package org.netbeans.modules.hudson.api.ui;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.spi.BuilderConnector.FailureDataProvider;
import org.netbeans.modules.hudson.spi.FailureDataDisplayerImpl;

/**
 * Displayer of failure data. Instances of this class will be passed to
 * {@link FailureDataProvider#showFailures(HudsonJobBuild, FailureDataDisplayer)}
 * and
 * {@link FailureDataProvider#showFailures(HudsonMavenModuleBuild, FailureDataDisplayer)}.
 *
 * Do not implement this interface in your classes. Use
 * {@link FailureDataDisplayerImpl} instead.
 *
 * @author jhavlin
 */
public interface FailureDataDisplayer {

    /**
     * Prepare the displayer for writing. Prepare needed resources.
     */
    void open();

    /**
     * Show a test suite.
     *
     * @param suite Test suite data.
     */
    void showSuite(Suite suite);

    /**
     * Finish writing to the displayer. Close all resources.
     */
    void close();

    /**
     * Info about failed test suite.
     */
    public static final class Suite {

        private String name;
        private String stdout;
        private String stderr;
        private long duration;
        private final List<Case> cases = new ArrayList<Case>();

        public String getName() {
            return name;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public long getDuration() {
            return duration;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setStdout(String stdout) {
            this.stdout = stdout;
        }

        public void setStderr(String stderr) {
            this.stderr = stderr;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public List<Case> getCases() {
            return cases;
        }

        public void addCase(Case cs) {
            cases.add(cs);
        }
    }

    /**
     * Info about failed test case.
     */
    public static final class Case {

        private String className;
        private String name;
        private String errorStackTrace;
        private long duration;

        public String getClassName() {
            return className;
        }

        public String getName() {
            return name;
        }

        public String getErrorStackTrace() {
            return errorStackTrace;
        }

        public long getDuration() {
            return duration;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setErrorStackTrace(String errorStackTrace) {
            this.errorStackTrace = errorStackTrace;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
}
