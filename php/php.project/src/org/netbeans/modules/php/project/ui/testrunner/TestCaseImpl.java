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
package org.netbeans.modules.php.project.ui.testrunner;

import java.util.Collections;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

public class TestCaseImpl implements TestCase {

    private final TestSuiteImpl testSuite;
    private final Testcase testCase;


    TestCaseImpl(TestSuiteImpl testSuite, Testcase testCase) {
        assert testSuite != null;
        assert testCase != null;
        this.testSuite = testSuite;
        this.testCase = testCase;
    }

    @Override
    public void setClassName(String className) {
        Parameters.notWhitespace("className", className); // NOI18N
        testSuite.checkFrozen();
        testCase.setClassName(className);
    }

    @Override
    public void setLocation(Locations.Line location) {
        Parameters.notNull("location", location); // NOI18N
        testSuite.checkFrozen();
        testCase.setLocation(FileUtil.toFile(location.getFile()).getAbsolutePath());
    }

    @Override
    public void setTime(long time) {
        testSuite.checkFrozen();
        testCase.setTimeMillis(time);
        testSuite.updateReport(time, false);
    }

    @Override
    public void setStatus(Status status) {
        Parameters.notNull("status", status); // NOI18N
        testSuite.checkFrozen();
        testCase.setStatus(map(status));
    }

    @Override
    public void setFailureInfo(String message, String[] stackTrace, boolean error, Diff diff) {
        Parameters.notNull("message", message); // NOI18N
        Parameters.notNull("stackTrace", stackTrace); // NOI18N
        Parameters.notNull("diff", diff); // NOI18N
        testSuite.checkFrozen();
        Trouble trouble = new Trouble(error);
        trouble.setStackTrace(createStackTrace(message, stackTrace));
        if (diff.isValid()) {
            Trouble.ComparisonFailure failure = new Trouble.ComparisonFailure(diff.getExpected(), diff.getActual());
            trouble.setComparisonFailure(failure);
        }
        testCase.setTrouble(trouble);
        Manager manager = testSuite.getTestSession().getManager();
        TestSession session = testSuite.getTestSession().getTestSession();
        manager.displayOutput(session, getClassName() + "::"  + testCase.getName() + "()", error); // NOI18N
        manager.displayOutput(session, message, error);
        testCase.addOutputLines(Collections.singletonList("<u>" + testCase.getName() + ":</u>")); // NOI18N
        for (String s : stackTrace) {
            manager.displayOutput(session, s, error);
            testCase.addOutputLines(Collections.singletonList(s.replace("<", "&lt;"))); // NOI18N
        }
        manager.displayOutput(session, "", false); // NOI18N
    }

    private String[] createStackTrace(String message, String[] stackTrace) {
        String[] tmp = new String[stackTrace.length + 1];
        tmp[0] = message;
        System.arraycopy(stackTrace, 0, tmp, 1, stackTrace.length);
        return tmp;
    }

    private String getClassName() {
        String className = testCase.getClassName();
        if (className != null) {
            return className;
        }
        className = testSuite.getTestSuite().getName();
        assert className != null;
        return className;
    }

    //~ Mappers

    private org.netbeans.modules.gsf.testrunner.api.Status map(TestCase.Status status) {
        return org.netbeans.modules.gsf.testrunner.api.Status.valueOf(status.name());
    }

}
