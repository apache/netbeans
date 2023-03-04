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
package org.netbeans.modules.testng.api;

import java.io.File;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author answer
 */
//suite/test/class
public class TestNGTestSuite extends TestSuite {

    private final TestSession session;
    private FileObject suiteFO = null;
    private long elapsedTime = 0;
    private int expectedTestCases;
    private FileObject cfgFO;

    public TestNGTestSuite(String tcClassName, TestSession testSession) {
        super(tcClassName);
        this.session = testSession;
    }

    public TestNGTestSuite(String name, TestSession session, int expectedTCases, String configFile) {
        super(name);
        this.session = session;
        expectedTestCases = expectedTCases;
        cfgFO = configFile.equals("null") ? null : FileUtil.toFileObject(FileUtil.normalizeFile(new File(configFile)));
    }

    public FileObject getSuiteFO() {
        return cfgFO;
//        if (suiteFO == null) {
//            FileLocator locator = session.getFileLocator();
//            if (locator != null) {
//                suiteFO = locator.find(getName().replace('.', '/') + ".java"); //NOI18N
//            }
//        }
//        return suiteFO;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void finish(int run, int fail, int skip, int confFail, int confSkip) {
        //not needed?
        //TODO: update tcases with proper status
    }

    public TestNGTestcase getTestCase(String testCase, String parameters) {
        for (Testcase tc: getTestcases()) {
            if (tc.getName().equals(parameters != null ? testCase + "(" + parameters + ")" : testCase)) {
                return (TestNGTestcase) tc;
            }
        }
        return null;
    }

}
