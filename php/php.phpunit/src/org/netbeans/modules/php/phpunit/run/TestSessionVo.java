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

package org.netbeans.modules.php.phpunit.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.extexecution.print.LineProcessors;
import org.netbeans.modules.php.phpunit.commands.PhpUnit;
import org.netbeans.modules.php.spi.testing.run.OutputLineHandler;
import org.openide.util.NbBundle;
import org.openide.windows.OutputWriter;

public final class TestSessionVo {

    private final List<TestSuiteVo> testSuites = new ArrayList<>();
    private final String customSuitePath;

    private long time = -1;
    private int tests = -1;


    public TestSessionVo(@NullAllowed String customSuitePath) {
        this.customSuitePath = customSuitePath;
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

    @NbBundle.Messages({
        "# {0} - suite name",
        "TestSessionVo.msg.customSuite=Using custom test suite {0}."
    })
    public String getInitMessage() {
        if (customSuitePath != null) {
            return Bundle.TestSessionVo_msg_customSuite(customSuitePath);
        }
        return null;
    }

    @NbBundle.Messages("TestSessionVo.msg.output=Full output can be found in Output window.")
    @CheckForNull
    public String getFinishMessage() {
        if (testSuites.isEmpty()) {
            // no message if we have no testsuites
            return null;
        }
        return Bundle.TestSessionVo_msg_output();
    }

    @Override
    public String toString() {
        return String.format("TestSessionVo{time: %d, tests: %d, suites: %d}", time, tests, testSuites.size());
    }

    public OutputLineHandler getOutputLineHandler() {
        return new PhpOutputLineHandler();
    }

    //~ Inner classes

    private static final class PhpOutputLineHandler implements OutputLineHandler {

        private static final LineConvertor CONVERTOR = LineConvertors.proxy(
                LineConvertors.filePattern(null, PhpUnit.OUT_LINE_PATTERN, null, 1, 2),
                LineConvertors.filePattern(null, PhpUnit.ERR_LINE_PATTERN, null, 1, 2)
        );


        @Override
        public void handleLine(OutputWriter out, String text) {
            LineProcessors.printing(out, CONVERTOR, true).processLine(text);
        }
    }

}
