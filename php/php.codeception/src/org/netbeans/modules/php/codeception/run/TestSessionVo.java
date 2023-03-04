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
package org.netbeans.modules.php.codeception.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.extexecution.print.LineProcessors;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.spi.testing.run.OutputLineHandler;
import org.openide.util.NbBundle;
import org.openide.windows.OutputWriter;

/**
 * Value Object for TestSession.
 */
public final class TestSessionVo {

    private final List<TestSuiteVo> testSuites = new ArrayList<>();

    private long time = -1L;
    private int tests = -1;


    public TestSessionVo() {
    }

    public void addTestSuite(TestSuiteVo testSuite) {
        testSuites.add(testSuite);
    }

    public List<TestSuiteVo> getTestSuites() {
        return Collections.unmodifiableList(testSuites);
    }

    public int getTests() {
        return tests;
    }

    public void setTests(int tests) {
        this.tests = tests;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getInitMessage() {
        // XXX if there is an initial message, add it here
        return null;
    }

    @NbBundle.Messages("TestSessionVo.msg.output=Full output can be found in Output window.")
    public String getFinishMessage() {
        if (testSuites.isEmpty()) {
            // no message if we have no testsuites
            return null;
        }
        return Bundle.TestSessionVo_msg_output();
    }

    public OutputLineHandler getOutputLineHandler() {
        return new CodeceptionOutputLineHandler();
    }

    @Override
    public String toString() {
        return String.format("TestSessionVo{time: %d, tests: %d, suites: %d}", time, tests, testSuites.size()); // NOI18N
    }

    //~ Inner classes

    private static final class CodeceptionOutputLineHandler implements OutputLineHandler {

        private static final LineConvertor CONVERTOR = LineConvertors.filePattern(null, Codecept.LINE_PATTERN, null, 1, 2);


        @Override
        public void handleLine(OutputWriter out, String text) {
            LineProcessors.printing(out, CONVERTOR, true).processLine(text);
        }
    }

}
