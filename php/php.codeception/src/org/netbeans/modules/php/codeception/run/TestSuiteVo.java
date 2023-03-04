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
import org.openide.filesystems.FileObject;

/**
 * Value Object for TestSuite.
 */
public final class TestSuiteVo {

    private final List<TestCaseVo> testCases = new ArrayList<>();
    private final String name;
    private final long time;


    public TestSuiteVo(String name, long time) {
        assert name != null;
        this.name = name;
        this.time = time;
    }

    void addTestCase(TestCaseVo testCase) {
        testCases.add(testCase);
    }

    public String getName() {
        return name;
    }

    public FileObject getLocation() {
        // file attribute doesn't exist
        return null;
    }

    public List<TestCaseVo> getPureTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    public List<TestCaseVo> getTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return String.format("TestSuiteVo{name: %s, time: %d, cases: %d}", name, time, testCases.size()); // NOI18N
    }

}
