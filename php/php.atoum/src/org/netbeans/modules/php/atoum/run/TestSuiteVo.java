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
package org.netbeans.modules.php.atoum.run;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;

public final class TestSuiteVo {

    private final String name;
    private final List<TestCaseVo> testCases = new ArrayList<>();

    private String file = null;
    private boolean fileSearched = false;


    public TestSuiteVo(String name) {
        assert name != null;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @CheckForNull
    public String getFile() {
        if (!fileSearched) {
            fileSearched = true;
            for (TestCaseVo testCase : testCases) {
                file = testCase.getFile();
                if (file != null) {
                    break;
                }
            }
        }
        return file;
    }

    public List<TestCaseVo> getTestCases() {
        return testCases;
    }

    public void addTestCase(TestCaseVo testCase) {
        testCases.add(testCase);
    }

    @Override
    public String toString() {
        return "TestSuiteVo{" + "name=" + name + ", testCases=" + testCases + '}'; // NOI18N
    }

}
