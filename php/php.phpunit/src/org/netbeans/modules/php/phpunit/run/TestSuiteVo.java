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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class TestSuiteVo {

    private final List<TestCaseVo> testCases = new ArrayList<>();
    private final String name;
    private final String file;
    private final long time;


    @NbBundle.Messages("TestSuiteVo.name.empty=&lt;no suite name>")
    public TestSuiteVo(String name, String file, long time) {
        assert name != null;
        assert file != null;
        this.name = StringUtils.hasText(name) ? name : Bundle.TestSuiteVo_name_empty();
        this.file = file;
        this.time = time;
    }

    void addTestCase(TestCaseVo testCase) {
        testCases.add(testCase);
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public FileObject getLocation() {
        if (file == null) {
            return null;
        }
        File f = new File(file);
        if (!f.isFile()) {
            return null;
        }
        return FileUtil.toFileObject(f);
    }

    public List<TestCaseVo> getPureTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    public List<TestCaseVo> getTestCases() {
        return sanitizedTestCases();
    }

    public long getTime() {
        return time;
    }

    private List<TestCaseVo> sanitizedTestCases() {
        if (!testCases.isEmpty()) {
            return Collections.unmodifiableList(testCases);
        }
        return Collections.singletonList(TestCaseVo.skippedTestCase());
    }

    @Override
    public String toString() {
        return String.format("TestSuiteVo{name: %s, file: %s, time: %d, cases: %d}", name, file, time, testCases.size()); // NOI18N
    }

}
