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
package org.netbeans.libs.jstestdriver.api;

/**
 *
 */
public interface TestListener {

    void onTestComplete(TestResult testResult);

    void onTestingFinished();
    
    public static class TestResult {

        public enum Result {

            passed, failed, error, started
        }
        private String result;
        private String message;
        private String stack;
        private String log;
        private String testCaseName;
        private String testName;
        private long time;
        private BrowserInfo browserInfo;

        public TestResult(BrowserInfo browser, String result, String message, String log,
                          String testCaseName, String testName, long time, String stack) {
            this.browserInfo = browser;
            this.result = result;
            this.message = message;
            this.log = log;
            this.testCaseName = testCaseName;
            this.testName = testName;
            this.time = time;
            this.stack = stack;
        }

        public Result getResult() {
            return Result.valueOf(result);
        }

        public String getMessage() {
            return message;
        }

        public String getStack() {
            return stack;
        }

        public String getLog() {
            return log;
        }

        public String getTestCaseName() {
            return testCaseName;
        }

        public String getTestName() {
            return testName;
        }

        public long getDuration() {
            return time;
        }

        public BrowserInfo getBrowserInfo() {
            return browserInfo;
        }
        
    }
}
